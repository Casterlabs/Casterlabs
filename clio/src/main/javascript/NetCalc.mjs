class NetCalc {

    constructor() {
        this.requiredBytes = 0;
    }

    allocateByte() {
        this.requiredBytes++;
        return this;
    }

    allocateBytes(len) {
        this.requiredBytes += len;
        return this;
    }

    allocateString(str) {
        const utf8Encoder = new TextEncoder();

        const bytes = utf8Encoder.encode(str);

        this.allocateInt32();
        this.requiredBytes += bytes.length;
        this.allocateNull();

        return this;
    }

    allocateNull() {
        this.requiredBytes++;
        return this;
    }

    allocateBigInt64() {
        this.requiredBytes += 8;
        return this;
    }

    allocateInt32() {
        this.requiredBytes += 4;
        return this;
    }

    allocateInt16() {
        this.requiredBytes += 2;
        return this;
    }

    allocateBoolean() {
        this.requiredBytes++;
        return this;
    }

    allocateFloat64() {
        this.requiredBytes += 8;
        return this;
    }

    allocateFloat32() {
        this.requiredBytes += 4;
        return this;
    }

}

export default NetCalc;