package co.casterlabs.kaminari.core;

import java.io.IOException;
import java.io.OutputStream;

public abstract class SimpleOutputStream extends OutputStream {

    public abstract void writeArray(byte[] arr) throws IOException;

    @Override
    public final void write(byte[] b) throws IOException {
        this.writeArray(b);
    }

    @Override
    public final void write(byte[] b, int off, int len) throws IOException {
        byte[] copied = new byte[len];
        System.arraycopy(b, off, copied, 0, len);

        this.writeArray(copied);
    }

    @Override
    public final void write(int b) throws IOException {
        throw new UnsupportedOperationException("Use write(byte[]) instead.");
    }

}
