package org.srtalliance.libsrt;

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

    /* The init functions are automatically called for us. */

    public String srt_getlasterror_str();

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
     * @param    name    TODO
     * @param    namelen TODO
     * 
     * @implSpec         https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_bind
     * 
     * @return           -1 if error, otherwise 0
     */
    public int srt_bind(int $socket, Void /* sockaddr */ name, int namelen);

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
     * @return           -1 if error, otherwise 0
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
     * Retrieves the remote address to which the socket is connected.
     * 
     * @param    $socket the pointer to the socket.
     * @param    name    TODO
     * @param    namelen TODO
     * 
     * @implSpec         https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_getpeername
     * 
     * @return           -1 if error, otherwise 0
     */
    public int srt_getpeername(int $socket, Void /* sockaddr */ name, int namelen);

    /**
     * Extracts the address to which the socket was bound. Although you should know
     * the address(es) that you have used for binding yourself, this function can be
     * useful for extracting the local outgoing port number when it was specified as
     * 0 with binding for system autoselection. With this function you can extract
     * the port number after it has been autoselected.
     * 
     * @param    $socket the pointer to the socket.
     * @param    name    TODO
     * @param    namelen TODO
     * 
     * @implSpec         https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_getsockname
     * 
     * @return           -1 if error, otherwise 0
     */
    public int srt_getsockname(int $socket, Void /* sockaddr */ name, int namelen);

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
     * @return           -1 if error, otherwise 0
     */
    default int srt_getsockflag_int(int $socket, int sockopt) {
        Memory dest = new Memory(4);

        int retcode = this.srt_getsockflag($socket, sockopt, dest, (int) dest.size());
        if (retcode == SRT_ERROR) {
            throw new RuntimeException(this.srt_getlasterror_str());
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
     * @return           -1 if error, otherwise 0
     */
    default long srt_getsockflag_long(int $socket, int sockopt) {
        Memory dest = new Memory(8);

        int retcode = this.srt_getsockflag($socket, sockopt, dest, (int) dest.size());
        if (retcode == SRT_ERROR) {
            throw new RuntimeException(this.srt_getlasterror_str());
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
     * @return           -1 if error, otherwise 0
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
     * @return           -1 if error, otherwise 0
     */
    default String srt_getsockflag_string(int $socket, int sockopt) {
        Memory dest = new Memory(256); // TODO figureout.

        int retcode = this.srt_getsockflag($socket, sockopt, dest, (int) dest.size());
        if (retcode == SRT_ERROR) {
            throw new RuntimeException(this.srt_getlasterror_str());
        }

        return dest.getString(0);
    }

    // TODO linger.

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
     * @return           -1 if error, otherwise 0
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
     * @return           -1 if error, otherwise 0
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
     * @return           -1 if error, otherwise 0
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
     * @return           -1 if error, otherwise 0
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

    /* -------------------- */
    /* Performance Tracking */
    /* -------------------- */

    /* -------------------- */
    /* Async/Epoll          */
    /* -------------------- */

    /* -------------------- */
    /* Logging              */
    /* -------------------- */

    /* -------------------- */
    /* Time Access          */
    /* -------------------- */

    /* -------------------- */
    /* Diagnostics          */
    /* -------------------- */

}
