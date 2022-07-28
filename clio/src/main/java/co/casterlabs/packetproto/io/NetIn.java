package co.casterlabs.packetproto.io;

import java.io.IOException;

public interface NetIn {

    public byte readByte() throws IOException;

    public byte[] readBytes(int len) throws IOException;

    public int available() throws IOException;

    /* -------- */
    /* Helpers  */
    /* -------- */

    default String readString() throws IOException {
        // STR_SIZE + STR_BYTES... + NULL
        int strLen = this.readInt();
        byte[] strContents = this.readBytes(strLen);
        this.readNull();

        return UTF8StringIOUtil.bytesToString(strContents);
    }

    default void readNull() throws IOException {
        this.readByte(); // Discard.
    }

    default long readLong() throws IOException {
        byte[] bytes = this.readBytes(Long.BYTES);

        return BigEndianIOUtil.bytesToLong(bytes);
    }

    default int readInt() throws IOException {
        byte[] bytes = this.readBytes(Integer.BYTES);

        return BigEndianIOUtil.bytesToInt(bytes);
    }

    default short readShort() throws IOException {
        byte[] bytes = this.readBytes(Short.BYTES);

        return BigEndianIOUtil.bytesToShort(bytes);
    }

    default boolean readBoolean() throws IOException {
        byte value = this.readByte();

        // 0 = false
        // anything else = true
        return value != 0;
    }

    default double readDouble() throws IOException {
        byte[] bytes = this.readBytes(Double.BYTES);

        return BigEndianIOUtil.bytesToDouble(bytes);
    }

    default float readFloat() throws IOException {
        byte[] bytes = this.readBytes(Float.BYTES);

        return BigEndianIOUtil.bytesToFloat(bytes);
    }

}
