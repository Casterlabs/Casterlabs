package org.srtalliance.libsrt;

import java.net.InetSocketAddress;

import com.sun.jna.Library;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.LongByReference;
import com.sun.jna.ptr.PointerByReference;

import lombok.SneakyThrows;

/**
 * https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md
 * 
 * @implNote All pointer variables start with $.
 * 
 */
public interface SRTNative extends Library {
    static final PointerByReference NULL_PTR = null;

    public static final int SRT_ERROR = -1;

    /* -------------------- */
    /* Initialization */
    /* -------------------- */

    // The init functions are automatically called for us.

    /* -------------------- */
    /* Creating/Configuring Sockets */
    /* -------------------- */

    /**
     * Creates an SRT socket.
     * 
     * @implSpec https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_create_socket
     * 
     * @return   A pointer to the socket.
     */
    public int srt_create_socket();

    /**
     * @deprecated Use {@link #srt_bind(int, InetSocketAddress)}.
     */
    @Deprecated
    public int srt_bind(int $socket, Pointer $name, int namelen);

    /**
     * Binds a socket to a local address and port. Binding specifies the local
     * network interface and the UDP port number to be used for the socket. When the
     * local address is a form of INADDR_ANY, then it's bound to all interfaces.
     * When the port number is 0, then the port number will be system-allocated if
     * necessary.
     * 
     * This call is obligatory for a listening socket before calling srt_listen and
     * for rendezvous mode before calling srt_connect; otherwise it's optional. For
     * a listening socket it defines the network interface and the port where the
     * listener should expect a call request. In the case of rendezvous mode (when
     * the socket has set SRTO_RENDEZVOUS to true both parties connect to one
     * another) it defines the network interface and port from which packets will be
     * sent to the peer, and the port to which the peer is expected to send packets.
     * 
     * For a connecting socket this call can set up the outgoing port to be used in
     * the communication. It is allowed that multiple SRT sockets share one local
     * outgoing port, as long as SRTO_REUSEADDR is set to true (default). Without
     * this call the port will be automatically selected by the system.
     * 
     * @apiNote          This function cannot be called on a socket group. If you
     *                   need to have the group-member socket bound to the specified
     *                   source address before connecting, use srt_connect_bind for
     *                   that purpose.
     * 
     * @param    $socket the pointer to the socket.
     * @param    addr    the address and port to bind on.
     * 
     * @implSpec         https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_bind
     * 
     * @return           -1 if error, otherwise 0.
     */
    default int srt_bind(int $socket, InetSocketAddress addr) {
        SockAddrIn name = new SockAddrIn(SockAddrIn.AF_INET, addr);

        return this.srt_bind($socket, name.getPointer(), name.size());
    }

    // TODO srt_bind_acquire
    // TODO srt_getsockstate
    // TODO srt_getsendbuffer
    // TODO srt_close

    /* -------------------- */
    /* Connecting */
    /* -------------------- */

    /**
     * This sets up the listening state on a socket with a backlog setting that
     * defines how many sockets may be allowed to wait until they are accepted
     * (excessive connection requests are rejected in advance).
     * 
     * @apiNote          The following important options may change the behavior of
     *                   the listener socket and the srt_accept function:
     *                   <ul>
     *                   <li>srt_listen_callback() installs a user function that
     *                   will be called before srt_accept can happen</li>
     *                   <li>SRTO_GROUPCONNECT option allows the listener socket to
     *                   accept group connections</li>
     *                   </ul>
     * 
     * @param    $socket the pointer to the socket.
     * @param    backlog the amount of sockets allowed to wait before being accepted
     *                   (rate limiting).
     * 
     * @implSpec         https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_listen
     * 
     * @return           -1 if error, otherwise 0.
     */
    public int srt_listen(int $socket, int backlog);

    // TODO srt_accept
    // TODO srt_accept_bond
    // TODO srt_connect_callback
    // TODO srt_connect
    // TODO srt_connect_debug
    // TODO srt_connect_bind
    // TODO srt_rendezvous
    // TODO srt_connect_callback

    /* -------------------- */
    /* Options and Properties */
    /* -------------------- */

    /**
     * @deprecated Use {@link #srt_getpeername(int)}.
     */
    @Deprecated
    public int srt_getpeername(int $socket, Pointer $name, int namelen);

    /**
     * Retrieves the remote address to which the socket is connected.
     * 
     * @param    $socket the pointer to the socket.
     * 
     * @implSpec         https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_getpeername
     * 
     * @return           The address of the peer.
     */
    default SockAddrIn srt_getpeername(int $socket) {
        SockAddrIn dest = new SockAddrIn();

        int result = this.srt_getpeername($socket, dest.getPointer(), dest.size());
        if (result == SRT_ERROR) {
            throw new RuntimeException("An error occurred.");
        }

        return dest;
    }

    /**
     * @deprecated Use {@link #srt_getsockname(int)}.
     */
    @Deprecated
    public int srt_getsockname(int $socket, Pointer $name, int namelen);

    /**
     * Extracts the address to which the socket was bound. Although you should know
     * the address(es) that you have used for binding yourself, this function can be
     * useful for extracting the local outgoing port number when it was specified as
     * 0 with binding for system autoselection. With this function you can extract
     * the port number after it has been autoselected.
     * 
     * @param    $socket the pointer to the socket.
     * 
     * @implSpec         https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_getsockname
     * 
     * @return           -1 if error, otherwise 0.
     */
    default SockAddrIn srt_getsockname(int $socket) {
        SockAddrIn dest = new SockAddrIn();

        int result = this.srt_getsockname($socket, dest.getPointer(), dest.size());
        if (result == SRT_ERROR) {
            throw new RuntimeException("An error occurred.");
        }

        return dest;
    }

    // TODO srt_getsockopt

    /**
     * 
     * @deprecated See {@link #srt_getsockflag_int32(int, int)},
     *             {@link #srt_getsockflag_int64(int, int)}, and
     *             {@link #srt_getsockflag_String(int, int)}.
     */
    @Deprecated // TODO linger
    public int srt_getsockflag(int $socket, int sockopt, Pointer $optval, int optlen);

    /**
     * Gets the value of the given socket option (from a socket or a group) as an
     * int (int32).
     * 
     * @param    $socket the pointer to the socket.
     * @param    sockopt the option you wish to read.
     * 
     * @implSpec         https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_getsockflag
     *                   and
     *                   https://github.com/Haivision/srt/blob/master/docs/API/API-socket-options.md
     * 
     * @return           -1 if error, otherwise 0.
     */
    default int srt_getsockflag_int(int $socket, int sockopt) {
        Memory dest = new Memory(4);

        int result = this.srt_getsockflag($socket, sockopt, dest, (int) dest.size());
        if (result == SRT_ERROR) {
            throw new RuntimeException("An error occurred.");
        }

        return dest.getInt(0);
    }

    /**
     * Gets the value of the given socket option (from a socket or a group) as a
     * long (int64).
     * 
     * @param    $socket the pointer to the socket.
     * @param    sockopt the option you wish to read.
     * 
     * @implSpec         https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_getsockflag
     *                   and
     *                   https://github.com/Haivision/srt/blob/master/docs/API/API-socket-options.md
     * 
     * @return           -1 if error, otherwise 0.
     */
    default long srt_getsockflag_long(int $socket, int sockopt) {
        Memory dest = new Memory(8);

        int result = this.srt_getsockflag($socket, sockopt, dest, (int) dest.size());
        if (result == SRT_ERROR) {
            throw new RuntimeException("An error occurred.");
        }

        return dest.getLong(0);
    }

    /**
     * Gets the value of the given socket option (from a socket or a group) as a
     * boolean (int32).
     * 
     * @param    $socket the pointer to the socket.
     * @param    sockopt the option you wish to read.
     * 
     * @implSpec         https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_getsockflag
     *                   and
     *                   https://github.com/Haivision/srt/blob/master/docs/API/API-socket-options.md
     * 
     * @return           -1 if error, otherwise 0.
     */
    default boolean srt_getsockflag_boolean(int $socket, int sockopt) {
        int value = this.srt_getsockflag_int($socket, sockopt);

        return value != 0;
    }

    /**
     * Gets the value of the given socket option (from a socket or a group) as a
     * string.
     * 
     * @param    $socket the pointer to the socket.
     * @param    sockopt the option you wish to read.
     * 
     * @implSpec         https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_getsockflag
     *                   and
     *                   https://github.com/Haivision/srt/blob/master/docs/API/API-socket-options.md
     * 
     * @return           -1 if error, otherwise 0.
     */
    default String srt_getsockflag_string(int $socket, int sockopt) {
        Memory dest = new Memory(256); // TODO figureout.

        int result = this.srt_getsockflag($socket, sockopt, dest, (int) dest.size());
        if (result == SRT_ERROR) {
            throw new RuntimeException("An error occurred.");
        }

        return dest.getString(0);
    }

    // TODO linger.

    // TODO srt_setsockopt

    /**
     * 
     * @deprecated See {@link #srt_setsockflag(int, int, int)},
     *             {@link #srt_setsockflag(int, int, long)},
     *             {@link #srt_setsockflag(int, int, boolean)}, and
     *             {@link #srt_setsockflag(int, int, String)}.
     */
    @Deprecated // TODO linger
    public int srt_setsockflag(int $socket, int sockopt, Pointer $optval, int optlen);

    /**
     * Sets an int (int32) value for a socket option for a socket or group.
     * 
     * @param    $socket the pointer to the socket.
     * @param    sockopt the option you wish to set.
     * @param    val     the desired value.
     * 
     * @implSpec         https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_getsockflag
     *                   and
     *                   https://github.com/Haivision/srt/blob/master/docs/API/API-socket-options.md
     * 
     * @return           -1 if error, otherwise 0.
     */
    default int srt_setsockflag(int $socket, int sockopt, int val) {
        Pointer ref = new IntByReference(val).getPointer();

        return this.srt_setsockflag($socket, sockopt, ref, 4);
    }

    /**
     * Sets a boolean value for a socket option for a socket or group.
     * 
     * @param    $socket the pointer to the socket.
     * @param    sockopt the option you wish to set.
     * @param    val     the desired value.
     * 
     * @implSpec         https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_getsockflag
     *                   and
     *                   https://github.com/Haivision/srt/blob/master/docs/API/API-socket-options.md
     * 
     * @return           -1 if error, otherwise 0.
     */
    default int srt_setsockflag(int $socket, int sockopt, boolean val) {
        return this.srt_setsockflag($socket, sockopt, val ? 1 : 0);
    }

    /**
     * Sets a long (int64) value for a socket option for a socket or group.
     * 
     * @param    $socket the pointer to the socket.
     * @param    sockopt the option you wish to set.
     * @param    val     the desired value.
     * 
     * @implSpec         https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_getsockflag
     *                   and
     *                   https://github.com/Haivision/srt/blob/master/docs/API/API-socket-options.md
     * 
     * @return           -1 if error, otherwise 0.
     */
    default int srt_setsockflag(int $socket, int sockopt, long val) {
        Pointer ref = new LongByReference(val).getPointer();

        return this.srt_setsockflag($socket, sockopt, ref, 8);
    }

    /**
     * Sets a String value for a socket option for a socket or group.
     * 
     * @param    $socket the pointer to the socket.
     * @param    sockopt the option you wish to set.
     * @param    val     the desired value.
     * 
     * @implSpec         https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_getsockflag
     *                   and
     *                   https://github.com/Haivision/srt/blob/master/docs/API/API-socket-options.md
     * 
     * @return           -1 if error, otherwise 0.
     */
    @SneakyThrows
    default int srt_setsockflag(int $socket, int sockopt, String val) {
        byte[] contents = val.getBytes(Native.getDefaultStringEncoding());

        Pointer ref = new Pointer(contents.length);
        ref.setString(0, val);

        return this.srt_setsockflag($socket, sockopt, ref, contents.length);
    }

    // TODO linger.

    /**
     * Get SRT version value. The version format in hex is 0xXXYYZZ for x.y.z in
     * human readable form, where x = ("%d", (version>>16) & 0xff), etc. <br />
     * <br />
     * Here's a snippet for parsing the hex value into a human readable string:
     * 
     * <pre>
     * String versionHex = String.format("%06x", NATIVE.srt_getversion()); // Expect 6 digits, pad if needed.
     * int major = Integer.parseInt(versionHex.substring(0, 2), 16);
     * int minor = Integer.parseInt(versionHex.substring(2, 4), 16);
     * int patch = Integer.parseInt(versionHex.substring(4, 6), 16);
     *
     * SRT_VERSION = String.format("v%d.%d.%d", major, minor, patch);
     * </pre>
     *
     * @implSpec https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_getversion
     * 
     * @return   the hex value of the version.
     */
    public int srt_getversion();

    /* -------------------- */
    /* Transmission         */
    /* -------------------- */

    // TODO srt_send
    // TODO srt_sendmsg
    // TODO srt_sendmsg2
    // TODO srt_recv
    // TODO srt_recvmsg
    // TODO srt_recvmsg2
    // TODO srt_sendfile
    // TODO srt_recvfile

    /* -------------------- */
    /* Performance Tracking */
    /* -------------------- */

    // TODO srt_bstats
    // TODO srt_bistats

    /* -------------------- */
    /* Async/Epoll          */
    /* -------------------- */

    // TODO srt_epoll_create
    // TODO srt_epoll_add_usock
    // TODO srt_epoll_add_ssock
    // TODO srt_epoll_update_usock
    // TODO srt_epoll_update_ssock
    // TODO srt_epoll_remove_usock
    // TODO srt_epoll_remove_ssock
    // TODO srt_epoll_wait
    // TODO srt_epoll_uwait
    // TODO srt_epoll_clear_usocks
    // TODO srt_epoll_set
    // TODO srt_epoll_release

    /* -------------------- */
    /* Logging              */
    /* -------------------- */

    // TODO srt_setloglevel
    // TODO srt_addlogfa
    // TODO srt_dellogfa
    // TODO srt_resetlogfa
    // TODO srt_setloghandler
    // TODO srt_setlogflags

    /* -------------------- */
    /* Time Access          */
    /* -------------------- */

    // TODO srt_time_now
    // TODO srt_connection_time
    // TODO srt_clock_type

    /* -------------------- */
    /* Diagnostics          */
    /* -------------------- */

    // TODO srt_getlasterror
    // TODO srt_strerror
    // TODO srt_getlasterror_str
    // TODO srt_clearlasterror
    // TODO srt_rejectreason_str
    // TODO srt_setrejectreason
    // TODO srt_getrejectreason

}
