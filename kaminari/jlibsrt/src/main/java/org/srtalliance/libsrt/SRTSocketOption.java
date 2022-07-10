package org.srtalliance.libsrt;

public class SRTSocketOption {
    // @formatter:off
    public static int SRTO_MSS                 =  0; // the Maximum Transfer Unit
    public static int SRTO_SNDSYN              =  1; // if sending is blocking
    public static int SRTO_RCVSYN              =  2; // if receiving is blocking
    public static int SRTO_ISN                 =  3; // Initial Sequence Number (valid only after srt_connect or srt_accept-ed sockets)
    public static int SRTO_FC                  =  4; // Flight flag size (window size)
    public static int SRTO_SNDBUF              =  5; // maximum buffer in sending queue
    public static int SRTO_RCVBUF              =  6; // UDT receiving buffer size
    public static int SRTO_LINGER              =  7; // waiting for unsent data when closing
    public static int SRTO_UDP_SNDBUF          =  8; // UDP sending buffer size
    public static int SRTO_UDP_RCVBUF          =  9; // UDP receiving buffer size
    // (unused)
    // (unused)
    public static int SRTO_RENDEZVOUS          = 12; // rendezvous connection mode
    public static int SRTO_SNDTIMEO            = 13; // send() timeout
    public static int SRTO_RCVTIMEO            = 14; // recv() timeout
    public static int SRTO_REUSEADDR           = 15; // reuse an existing port or create a new one
    public static int SRTO_MAXBW               = 16; // maximum bandwidth (bytes per second) that the connection can use
    public static int SRTO_STATE               = 17; // current socket state; see UDTSTATUS; read only
    public static int SRTO_EVENT               = 18; // current available events associated with the socket
    public static int SRTO_SNDDATA             = 19; // size of data in the sending buffer
    public static int SRTO_RCVDATA             = 20; // size of data available for recv
    public static int SRTO_SENDER              = 21; // Sender mode (independent of conn mode); for encryption; tsbpd handshake.
    public static int SRTO_TSBPDMODE           = 22; // Enable/Disable TsbPd. Enable -> Tx set origin timestamp; Rx deliver packet at origin time + delay
    public static int SRTO_LATENCY             = 23; // NOT RECOMMENDED. SET: to both SRTO_RCVLATENCY and SRTO_PEERLATENCY. GET: same as SRTO_RCVLATENCY.
    public static int SRTO_INPUTBW             = 24; // Estimated input stream rate.
    public static int SRTO_OHEADBW             = 25; // MaxBW ceiling based on % over input stream rate. Applies when UDT_MAXBW=0 (auto).
    public static int SRTO_PASSPHRASE          = 26; // Crypto PBKDF2 Passphrase (must be 10..79 characters; or empty to disable encryption)
    public static int SRTO_PBKEYLEN            = 27; // Crypto key len in bytes {16;24;32} Default: 16 (AES-128)
    public static int SRTO_KMSTATE             = 28; // Key Material exchange status (UDT_SRTKmState)
    public static int SRTO_IPTTL               = 29; // IP Time To Live (passthru for system sockopt IPPROTO_IP/IP_TTL)
    public static int SRTO_IPTOS               = 30; // IP Type of Service (passthru for system sockopt IPPROTO_IP/IP_TOS)
    public static int SRTO_TLPKTDROP           = 31; // Enable receiver pkt drop
    public static int SRTO_SNDDROPDELAY        = 32; // Extra delay towards latency for sender TLPKTDROP decision (-1 to off)
    public static int SRTO_NAKREPORT           = 33; // Enable receiver to send periodic NAK reports
    public static int SRTO_VERSION             = 34; // Local SRT Version
    public static int SRTO_PEERVERSION         = 35; // Peer SRT Version (from SRT Handshake)
    public static int SRTO_CONNTIMEO           = 36; // Connect timeout in msec. Caller default: 3000; rendezvous (x 10)
    public static int SRTO_DRIFTTRACER         = 37; // Enable or disable drift tracer
    public static int SRTO_MININPUTBW          = 38; // Minimum estimate of input stream rate.
    // (unused)
    public static int SRTO_SNDKMSTATE          = 40; // (GET) the current state of the encryption at the peer side
    public static int SRTO_RCVKMSTATE          = 41; // (GET) the current state of the encryption at the agent side
    public static int SRTO_LOSSMAXTTL          = 42; // Maximum possible packet reorder tolerance (number of packets to receive after loss to send lossreport)
    public static int SRTO_RCVLATENCY          = 43; // TsbPd receiver delay (mSec) to absorb burst of missed packet retransmission
    public static int SRTO_PEERLATENCY         = 44; // Minimum value of the TsbPd receiver delay (mSec) for the opposite side (peer)
    public static int SRTO_MINVERSION          = 45; // Minimum SRT version needed for the peer (peers with less version will get connection reject)
    public static int SRTO_STREAMID            = 46; // A string set to a socket and passed to the listener's accepted socket
    public static int SRTO_CONGESTION          = 47; // Congestion controller type selection
    public static int SRTO_MESSAGEAPI          = 48; // In File mode; use message API (portions of data with boundaries)
    public static int SRTO_PAYLOADSIZE         = 49; // Maximum payload size sent in one UDP packet (0 if unlimited)
    public static int SRTO_TRANSTYPE           = 50; // Transmission type (set of options required for given transmission type)
    public static int SRTO_KMREFRESHRATE       = 51; // After sending how many packets the encryption key should be flipped to the new key
    public static int SRTO_KMPREANNOUNCE       = 52; // How many packets before key flip the new key is annnounced and after key flip the old one decommissioned
    public static int SRTO_ENFORCEDENCRYPTION  = 53; // Connection to be rejected or quickly broken when one side encryption set or bad password
    public static int SRTO_IPV6ONLY            = 54; // IPV6_V6ONLY mode
    public static int SRTO_PEERIDLETIMEO       = 55; // Peer-idle timeout (max time of silence heard from peer) in [ms]
    public static int SRTO_BINDTODEVICE        = 56; // Forward the SOL_SOCKET/SO_BINDTODEVICE option on socket (pass packets only from that device)
    public static int SRTO_GROUPCONNECT        = 57; // Set on a listener to allow group connection (ENABLE_BONDING)
    public static int SRTO_GROUPMINSTABLETIMEO = 58; // Minimum Link Stability timeout (backup mode) in milliseconds (ENABLE_BONDING)
    public static int SRTO_GROUPTYPE           = 59; // Group type to which an accepted socket is about to be added; available in the handshake (ENABLE_BONDING)
    public static int SRTO_PACKETFILTER        = 60; // Add and configure a packet filter
    public static int SRTO_RETRANSMITALGO      = 61; // An option to select packet retransmission algorithm
    // @formatter:on
}