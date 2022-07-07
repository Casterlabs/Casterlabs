package co.casterlabs.kaminari.core.audio;

import static co.casterlabs.kaminari.core.audio.AudioConstants.*;

import java.io.Closeable;
import java.io.IOException;

import org.jetbrains.annotations.Nullable;

import lombok.SneakyThrows;

public abstract class StreamedAudioContext extends AudioContext implements Closeable {

    private final Object readLock = new Object();

    private byte[] buffer = new byte[AUDIO_BUFFER_SIZE * AUDIO_BYTES_PER_SAMPLE];
    private int bufferProgress = 0;

    private volatile float[] chunk;

    private boolean isRunning = false;
    private Thread readThread;

    protected void startReading() {
        this.isRunning = true;

        this.readThread = new Thread(this::readLoop);
        this.readThread.setDaemon(true);
        this.readThread.setName("AudioContext Read Thread");
        this.readThread.start();
    }

    @SneakyThrows
    private void readLoop() {
        synchronized (this.readLock) {
            while (this.isRunning) {
                // Fill the buffer if possible.
                if (this.bufferProgress < this.buffer.length) {
                    int read = this.read(
                        this.buffer,
                        this.bufferProgress,
                        this.buffer.length - this.bufferProgress
                    );

                    if (read == -1) this.close();

                    this.bufferProgress += read;
                    continue; // Run again.
                }

                this.chunk = makeSamples(this.buffer);
                this.bufferProgress = 0;

                // Wait for the buffer to get cleared.
                try {
                    this.readLock.wait();
                } catch (InterruptedException ignored) {}
            }
        }
    }

    /* Override as needed */
    @Override
    protected @Nullable float[] readChunk0() {
        float[] chunk = this.chunk;

        if (chunk != null) {
            synchronized (this.readLock) {
                this.chunk = null;
                this.readLock.notifyAll();
            }
        }

        return chunk;
    }

    @Override
    public void close() throws IOException {
        if (this.isRunning) {
            this.isRunning = false;
            this.readThread.interrupt();
        }
    }

    protected abstract int read(byte[] buf, int off, int len);

}
