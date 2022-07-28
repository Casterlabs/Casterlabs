package co.casterlabs.packetproto.io.buffer;

import java.io.IOException;
import java.nio.ByteBuffer;

import co.casterlabs.packetproto.io.NetIn;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ByteBufferNetIn implements NetIn {
    private ByteBuffer buf;

    public ByteBufferNetIn(byte[] arr) {
        this.buf = ByteBuffer.wrap(arr);
    }

    @Override
    public byte readByte() throws IOException {
        return this.buf.get();
    }

    @Override
    public byte[] readBytes(int len) throws IOException {
        byte[] dst = new byte[len];

        this.buf.get(dst);

        return dst;
    }

    @Override
    public int available() throws IOException {
        return this.buf.capacity() - this.buf.position();
    }

}
