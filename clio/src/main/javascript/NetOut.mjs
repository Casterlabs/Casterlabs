const textEncoder = new TextEncoder();

class NetOut {

    constructor(arrayBuffer) {
        this.buffer = arrayBuffer;
        this.view = new DataView(this.buffer);
        this.position = 0;
    }

    writeByte(byte) {
        this.view.setUint8(this.position, byte);
        this.position++;
        return this;
    }

    writeBytes(bytes) {
        for (const byte of bytes) {
            this.writeByte(byte);
        }
        return this;
    }

    writeString(str) {
        const bytes = textEncoder.encode(str);

        this.writeInt32(bytes.length);
        this.writeBytes(bytes);
        this.writeNull();

        return this;
    }

    writeNull() {
        this.writeByte(0);
        return this;
    }

    writeBigInt64(val) {
        this.view.setBigInt64(this.position, val, false);
        this.position += 8;
        return this;
    }

    writeInt32(val) {
        this.view.setInt32(this.position, val, false);
        this.position += 4;
        return this;
    }

    writeInt16(val) {
        this.view.setInt16(this.position, val, false);
        this.position += 2;
        return this;
    }

    writeBoolean(val) {
        const byte = val ? 1 : 0;

        this.writeByte(byte);

        return this;
    }

    writeFloat64(val) {
        this.view.setFloat64(this.position, val);
        this.position += 8;
        return this;
    }

    writeFloat32(val) {
        this.view.setFloat32(this.position, val);
        this.position += 4;
        return this;
    }

}

export default NetOut;