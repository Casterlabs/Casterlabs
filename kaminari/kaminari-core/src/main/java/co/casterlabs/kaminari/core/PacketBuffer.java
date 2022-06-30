package co.casterlabs.kaminari.core;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class PacketBuffer {
    private ByteBuffer buf;

    public PacketBuffer(int bufferSize, ByteOrder order) {
        this.buf = ByteBuffer.allocateDirect(bufferSize);
        this.buf.order(order);
    }

    public void uint8(int value) {
        byte v = (byte) value;
        buf.put(v);
    }

    public void int8(byte value) {
        buf.put(value);
    }

    public void uint32(long value) {
        int v = (int) value;
        buf.putInt(v);
    }

    public void int32(int value) {
        buf.putInt(value);
    }

    public void uint24(int value) {
        byte[] v = new byte[3];

        ByteBuffer
            .allocate(Integer.BYTES)
            .order(this.buf.order())
            .putInt(value)
            .position(this.buf.order() == ByteOrder.BIG_ENDIAN ? 1 : 0)
            .get(v);

        buf.put(v);
    }

//    public void int24(int value) {
//        byte[] v = new byte[3];
//
//        ByteBuffer
//            .allocate(Integer.BYTES)
//            .order(this.buf.order())
//            .putInt(value)
//            .position(this.buf.order() == ByteOrder.BIG_ENDIAN ? 1 : 0)
//            .get(v);
//
//        buf.put(v);
//    }

    public void raw(byte[] data) {
        buf.put(data);
    }

    public byte[] flush() {
        if (buf.position() == 0) {
            return new byte[0];
        }

        buf.flip();
        byte[] data = new byte[buf.limit()];
        buf.get(data);
        buf.clear();

        return data;
    }

}
