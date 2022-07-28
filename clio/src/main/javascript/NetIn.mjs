const textDecoder = new TextDecoder();

class NetIn {

    constructor(dataView) {
        this.view = dataView;
        this.position = 0;
    }

    readByte() {
        return this.view.getInt8(this.position++);
    }

    readBytes(len) {
        const bytes = [];

        for (let idx = 0; idx < len; idx++) {
            bytes[idx] = this.readByte();
        }

        return bytes;
    }

    readString() {
        const strLen = this.readInt32();
        const bytes = this.readBytes(strLen);
        this.readNull(); // Discard

        return textDecoder.decode(new Int8Array(bytes).buffer);
    }

    readNull() {
        this.readByte();
        return null;
    }

    readBigInt64() {
        const read = this.view.getBigInt64(this.position, false);

        this.position += 8;

        return read;
    }

    readInt32() {
        const read = this.view.getInt32(this.position, false);

        this.position += 4;

        return read;
    }

    readInt16() {
        const read = this.view.getInt16(this.position, false);

        this.position += 2;

        return read;
    }

    readBoolean() {
        const byte = this.readByte();

        return byte != 0;
    }

    readFloat64() {
        const read = this.view.getFloat64(this.position, false);

        this.position += 8;

        return read;
    }

    readFloat32() {
        const read = this.view.getFloat32(this.position, false);

        this.position += 4;

        return read;
    }

}

export default NetIn;