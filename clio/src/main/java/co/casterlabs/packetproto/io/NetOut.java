package co.casterlabs.packetproto.io;

import java.io.IOException;

import lombok.NonNull;

public interface NetOut {

    public NetOut writeByte(byte b) throws IOException;

    public NetOut writeBytes(byte[] b) throws IOException;

    /* -------- */
    /* Helpers  */
    /* -------- */

    default NetOut writeString(@NonNull String str) throws IOException {
        byte[] bytes = UTF8StringIOUtil.stringToBytes(str);

        // STR_SIZE + STR_BYTES... + NULL
        this.writeInt(bytes.length);
        this.writeBytes(bytes);
        this.writeNull();

        return this;
    }

    default NetOut writeNull() throws IOException {
        this.writeByte((byte) 0);
        return this;
    }

    default NetOut writeLong(long l) throws IOException {
        this.writeBytes(BigEndianIOUtil.longToBytes(l));
        return this;
    }

    default NetOut writeInt(int i) throws IOException {
        this.writeBytes(BigEndianIOUtil.intToBytes(i));
        return this;
    }

    default NetOut writeShort(short s) throws IOException {
        this.writeBytes(BigEndianIOUtil.shortToBytes(s));
        return this;
    }

    default NetOut writeBoolean(boolean bl) throws IOException {
        byte value = (byte) (bl ? 1 : 0);

        this.writeByte(value);

        return this;
    }

    default NetOut writeDouble(double d) throws IOException {
        this.writeBytes(BigEndianIOUtil.doubleToBytes(d));
        return this;
    }

    default NetOut writeFloat(float f) throws IOException {
        this.writeBytes(BigEndianIOUtil.floatToBytes(f));
        return this;
    }

}
