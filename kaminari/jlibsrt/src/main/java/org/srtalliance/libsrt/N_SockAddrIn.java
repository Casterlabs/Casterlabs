package org.srtalliance.libsrt;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;

import com.sun.jna.Structure;

import lombok.NonNull;
import lombok.SneakyThrows;

class N_SockAddrIn extends Structure {
    public static final int AF_INET = 2;

    // @formatter:off
    protected short  sin_family;
    protected short  sin_port;
    protected byte[] sin_addr = new byte[4]; 
    protected byte[] sin_zero = new byte[8];
    // @formatter:on

    public N_SockAddrIn() {
        // NOOP
    }

    public N_SockAddrIn(int family, @NonNull InetSocketAddress addr) {
        this.sin_family = (short) (0xffff & family);
        this.sin_port = (short) addr.getPort();
        this.sin_addr = addr.getAddress().getAddress();
    }

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList(
            "sin_family",
            "sin_port",
            "sin_addr",
            "sin_zero"
        );
    }

    @SneakyThrows
    public InetSocketAddress getAddr() {
        return new InetSocketAddress(
            InetAddress.getByAddress(this.sin_addr),
            this.sin_port
        );
    }

    public int getFamily() {
        return Short.toUnsignedInt(this.sin_family);
    }

    public int getPort() {
        return Short.toUnsignedInt(this.sin_port);
    }

}
