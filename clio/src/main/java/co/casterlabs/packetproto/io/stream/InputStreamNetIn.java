package co.casterlabs.packetproto.io.stream;

import java.io.IOException;
import java.io.InputStream;

import co.casterlabs.packetproto.io.NetIn;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InputStreamNetIn implements NetIn {
    private InputStream stream;

    @Override
    public byte readByte() throws IOException {
        return (byte) this.stream.read();
    }

    @Override
    public byte[] readBytes(int len) throws IOException {
        byte[] dst = new byte[len];

        this.stream.read(dst);

        return dst;
    }

    @Override
    public int available() throws IOException {
        return this.stream.available();
    }

}
