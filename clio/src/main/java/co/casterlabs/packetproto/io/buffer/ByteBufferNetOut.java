package co.casterlabs.packetproto.io.buffer;

import java.io.IOException;
import java.nio.ByteBuffer;

import co.casterlabs.packetproto.io.NetOut;
import lombok.Getter;

@Getter
public class ByteBufferNetOut implements NetOut {
    private ByteBuffer buf;

    public ByteBufferNetOut(int capacity) {
        this.buf = ByteBuffer.allocate(capacity);
    }

    @Override
    public NetOut writeByte(byte b) throws IOException {
        this.buf.put(b);
        return this;
    }

    @Override
    public NetOut writeBytes(byte[] b) throws IOException {
        this.buf.put(b);
        return this;
    }

}
