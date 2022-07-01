package co.casterlabs.kaminari.core;

import java.io.Closeable;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

public class Looper implements Closeable {
    private @Getter long framesRendered;
    private @Getter long framesTargeted;
    private @Getter long frameTime;

    private long startTime = -1;
    private @Setter long frameInterval;
    private long lastRender;

    private @Getter boolean running = false;

    @SneakyThrows
    public void start(Runnable handler) {
        assert !this.running : "This instance is already running!";

        this.running = true;
        this.framesRendered = 0;
        this.framesTargeted = 1;
        this.frameTime = 0;

        // Timing stuffs.
        this.startTime = System.nanoTime();
        this.lastRender = this.startTime + this.frameInterval;

        while (this.running) {
            // Frame interval check/sleep.
            {
                long now = System.nanoTime();
                long delta = (now - this.lastRender);
                long wait = (long) ((this.frameInterval - delta) / 1e+6);

                if (wait > 0) {
                    Thread.sleep(wait);
                }
            }

            // Render.
            long renderStart = System.currentTimeMillis();
            handler.run();
            long renderEnd = System.currentTimeMillis();

            // Debug stats.
            this.frameTime = renderEnd - renderStart;
            this.framesRendered++;
            this.framesTargeted = ((this.lastRender - this.startTime) / this.frameInterval) - 1; // TODO figure out where this one comes from.
            this.lastRender += this.frameInterval; // Doing it this way allows the renderer to crank out missed frames rather than
                                                   // skip them.
        }

        this.close();
    }

    public void stop() {
        this.running = false;
    }

    @Override
    public void close() {
        this.stop();
    }

    public void startAsync(Runnable handler, String threadName) {
        Thread thread = new Thread(() -> this.start(handler));
        thread.setName(threadName);
        thread.setDaemon(true);
        thread.start();
    }

}
