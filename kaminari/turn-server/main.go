package main

import (
	"flag"
	"log"
	"net"
	"os"
	"os/signal"
	"regexp"
	"strconv"
	"syscall"

	"github.com/pion/turn/v2"
)

// Adapted from:
// https://github.com/pion/turn/blob/master/examples/turn-server/tcp/main.go
// https://github.com/pion/turn/blob/master/examples/turn-server/port-range/main.go
// https://github.com/pion/turn/blob/master/examples/turn-server/simple/main.go

// TODO Some port stuff on our host container before we uncomment it here.

func main() {
	// Required
	publicIP := flag.String("public-ip", "", "IP Address that TURN can be contacted by.")
	users := flag.String("users", "", "List of username and password (e.g. \"user=pass,user=pass\")")

	// Optional
	port := flag.Int("port", 3478, "Listening port.")
	// minPortRange := flag.Uint("min-port", 50000, "Minimum listening port.")
	// maxPortRange := flag.Uint("max-port", 55000, "Maximum listening port.")
	realm := flag.String("realm", "casterlabs.co", "Realm (defaults to \"casterlabs.co\")")

	// Parse out the args and validate
	flag.Parse()
	if len(*publicIP) == 0 {
		log.Fatalf("'public-ip' is required")
	} else if len(*users) == 0 {
		log.Fatalf("'users' is required")
	}

	// Create a TCP listener to pass into pion/turn
	tcpListener, err := net.Listen("tcp4", "0.0.0.0:"+strconv.Itoa(*port))
	if err != nil {
		log.Panicf("Failed to create TURN server listener (TCP): %s", err)
	}

	// Create a UDP listener to pass into pion/turn
	// udpListener, err := net.ListenPacket("udp4", "0.0.0.0:"+strconv.Itoa(*port))
	// if err != nil {
	// 	log.Panicf("Failed to create TURN server listener (UDP): %s", err)
	// }

	// Cache -users flag for easy lookup later
	// If passwords are stored they should be saved to your DB hashed using turn.GenerateAuthKey
	usersMap := map[string][]byte{}
	for _, kv := range regexp.MustCompile(`(\w+)=(\w+)`).FindAllStringSubmatch(*users, -1) {
		usersMap[kv[1]] = turn.GenerateAuthKey(kv[1], *realm, kv[2])
	}

	s, err := turn.NewServer(turn.ServerConfig{
		Realm: *realm,
		// Set AuthHandler callback
		// This is called everytime a user tries to authenticate with the TURN server
		// Return the key for that user, or false when no user is found
		AuthHandler: func(username string, realm string, srcAddr net.Addr) ([]byte, bool) {
			if key, ok := usersMap[username]; ok {
				return key, true
			}
			return nil, false
		},

		ListenerConfigs: []turn.ListenerConfig{
			{
				Listener: tcpListener,
				RelayAddressGenerator: &turn.RelayAddressGeneratorStatic{
					RelayAddress: net.ParseIP(*publicIP), // Claim that we are listening on IP passed by user (This should be your Public IP)
					Address:      "0.0.0.0",              // But actually be listening on every interface
				},
			},
		},
		// PacketConnConfigs: []turn.PacketConnConfig{
		// 	{
		// 		PacketConn:            udpListener,
		// 		RelayAddressGenerator: &turn.RelayAddressGeneratorPortRange{
		// 			RelayAddress: net.ParseIP(*publicIP), // Claim that we are listening on IP passed by user (This should be your Public IP)
		// 			Address:      "0.0.0.0",              // But actually be listening on every interface
		// 			MinPort:      uint16(*minPortRange),
		// 			MaxPort:      uint16(*maxPortRange),
		// 		},
		// 	},
		// },
	})
	if err != nil {
		log.Panic(err)
	}

	// Block until user sends SIGINT or SIGTERM
	sigs := make(chan os.Signal, 1)
	signal.Notify(sigs, syscall.SIGINT, syscall.SIGTERM)
	<-sigs

	if err = s.Close(); err != nil {
		log.Panic(err)
	}
}
