package co.casterlabs.kaminari.core;

import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import co.casterlabs.kaminari.core.audio.AudioConstants;
import co.casterlabs.kaminari.core.scene.Scene;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

public class Kaminari implements Closeable {
    public static final String IMAGE_FORMAT = "argb"; // Note that Kaminari's output will NEVER utilize opacity.
    public static final int BUFFER_FORMAT = BufferedImage.TYPE_INT_ARGB;

    private static final long NANO = (long) 1e+9;

    private @Getter FastLogger logger;

    private @Getter String name;

    private @Getter long framesRendered;
    private @Getter long framesTargeted;
    private @Getter long frameTime;

    private long startTime = -1;
    private long frameInterval;
    private long lastRender;

    private @Getter int frameRate;
    private @Getter int width = -1; // Pixels
    private @Getter int height = -1; // Pixels

    private byte[] blankFrameData;

    private @Getter @Setter int currentSceneIndex = 0;
    public final List<Scene> scenes = new ArrayList<>();

    private @Getter @Setter OutputStream target;
    private @Getter @Setter OutputStream audioTarget;
    private byte[] currentFrameData;
    private Thread targetWriteThread;
    private Thread audioProcessThread;

    private boolean shouldRender = false;

    static {
        ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);
    }

    public Kaminari(@NonNull String name) {
        this.name = name;
        this.logger = new FastLogger("Kaminari: " + name);

        this.setFramerate(30);
    }

    /* ---------------- */
    /* Properties       */
    /* ---------------- */

    public void setFramerate(int rate) {
        this.frameRate = rate;
        this.frameInterval = NANO / rate;
    }

    public void setSize(int width, int height) {
        assert width > 0 : "Width must be greater than 0.";
        assert height > 0 : "Height must be greater than 0.";

        this.width = width;
        this.height = height;

        this.blankFrameData = new byte[width * height * 4];

        for (Scene scene : this.scenes) {
            scene.setSize(width, height);
        }
    }

    /* ---------------- */
    /* Rendering        */
    /* ---------------- */

    public boolean isRenderable() {
        return this.width != -1;
    }

    private void _asyncWriteTarget() {
        try {
            while (true) {
                synchronized (this.targetWriteThread) {
                    this.targetWriteThread.wait();
                }

                try {
                    this.target.write(this.currentFrameData);
                } catch (IOException e) {
                    this.logger.severe("Unable to write to video target, stopping stream.\n%s", e);
                    this.stop();
                }
            }
        } catch (InterruptedException e) {
            Thread.interrupted(); // Clear
            // Exit.
        }
    }

    private void _asyncProcessAudio() {
        while (this.shouldRender) {
            // Bounds check.
            if ((this.currentSceneIndex < 0) || (this.currentSceneIndex >= this.scenes.size())) {
                this.currentSceneIndex = 0;
            }

            // Tell the scene to render.
            Scene currentScene = this.scenes.get(this.currentSceneIndex);

            float[] chunk = currentScene.mixer.read();
            if (this.audioTarget == null) continue; // Discard.

            if (chunk == null) {
                chunk = new float[AudioConstants.AUDIO_CHANNELS];
            }

            try {
                for (float sample : chunk) {
                    byte[] bytes = AudioConstants.destructSample(sample);

                    this.audioTarget.write(bytes);
                }
            } catch (IOException e) {
                this.logger.severe("Unable to write to audio target, stopping stream.\n%s", e);
                this.stop();
            }
        }
    }

    public void start() throws InterruptedException, IOException {
        assert this.isRenderable() : "This instance is NOT renderable.";
        assert !this.shouldRender : "This instance is already rendering!";

        this.shouldRender = true;
        this.framesRendered = 0;
        this.framesTargeted = 1;
        this.frameTime = 0;

        // Setup the write thread.
        this.targetWriteThread = new Thread(this::_asyncWriteTarget);
        this.targetWriteThread.setName("Kaminari Async Video Write Thread: " + this.name);
        this.targetWriteThread.setDaemon(true);
        this.targetWriteThread.start();

        // Setup the audio processing thread.
        this.audioProcessThread = new Thread(this::_asyncProcessAudio);
        this.audioProcessThread.setName("Kaminari Async Audio Process Thread: " + this.name);
        this.audioProcessThread.setDaemon(true);
        this.audioProcessThread.start();

        // Timing stuffs.
        this.startTime = System.nanoTime();
        this.lastRender = this.startTime + this.frameInterval;

        while (this.shouldRender) {
            // Frame interval check/sleep.
            {
                long now = System.nanoTime();
                long delta = (now - this.lastRender);
                long wait = (long) ((this.frameInterval - delta) / 1e+6);

                if (wait > 0) {
                    this.logger.trace("Waiting %dms and then rendering.", wait);
                    Thread.sleep(wait);
                } else {
//                    this.logger.severe("We're behind!"); // Always log.
                }
            }

            // Render.
            long renderStart = System.currentTimeMillis();

            if (this.scenes.isEmpty()) {
                // There are no scenes to render, show black instead.
                this.currentFrameData = this.blankFrameData;
            } else {
                // Bounds check.
                if ((this.currentSceneIndex < 0) || (this.currentSceneIndex >= this.scenes.size())) {
                    this.currentSceneIndex = 0;
                }

                // Tell the scene to render.
                Scene currentScene = this.scenes.get(this.currentSceneIndex);
                currentScene.render();

                // Dump the frame buffer data.
                this.currentFrameData = currentScene.currentFrameData;
            }

            if (this.target != null) {
                // Tell the target writer to write.
                synchronized (this.targetWriteThread) {
                    this.targetWriteThread.notify();
                }
            }

            long renderEnd = System.currentTimeMillis();

            // Debug stats.
            this.frameTime = renderEnd - renderStart;
            this.framesRendered++;
            this.framesTargeted = ((this.lastRender - this.startTime) / this.frameInterval) - 1; // TODO figure out where this one comes from.
            this.lastRender += this.frameInterval; // Doing it this way allows the renderer to crank out missed frames rather than
                                                   // skip them.
        }

        this.currentFrameData = null; // Free memory.
        this.stop();
    }

    public void stop() {
        if (this.targetWriteThread != null) this.targetWriteThread.interrupt();
        if (this.audioProcessThread != null) this.audioProcessThread.interrupt();
        this.shouldRender = false;
    }

    @Override
    public void close() {
        this.stop();
    }

}
