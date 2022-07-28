package co.casterlabs.packetproto.io;

import lombok.Getter;
import lombok.NonNull;

@Getter
public class NetCalc {
    private int requiredBytes = 0;

    public NetCalc allocateByte() {
        this.requiredBytes++;
        return this;
    }

    public NetCalc allocateBytes(int bytes) {
        this.requiredBytes += bytes;
        return this;
    }

    public NetCalc allocateString(@NonNull String str) {
        byte[] bytes = UTF8StringIOUtil.stringToBytes(str);

        // STR_SIZE + STR_BYTES... + NULL
        this.allocateInt();
        this.requiredBytes += bytes.length;
        this.allocateNull();

        return this;
    }

    public NetCalc allocateNull() {
        this.requiredBytes++;
        return this;
    }

    public NetCalc allocateLong() {
        this.requiredBytes += Long.BYTES;
        return this;
    }

    public NetCalc allocateInt() {
        this.requiredBytes += Integer.BYTES;
        return this;
    }

    public NetCalc allocateShort() {
        this.requiredBytes += Short.BYTES;
        return this;
    }

    public NetCalc allocateBoolean() {
        this.requiredBytes++;
        return this;
    }

    public NetCalc allocateDouble() {
        this.requiredBytes += Double.BYTES;
        return this;
    }

    public NetCalc allocateFloat() {
        this.requiredBytes += Float.BYTES;
        return this;
    }

}
