package co.casterlabs.kaminari.core;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Transparency;
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
import lombok.SneakyThrows;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public class Kaminari implements Closeable {
    public static final String IMAGE_FORMAT; // Transparency may be used, always render over a black background if you want
                                             // opaque video.
    public static final int IMAGE_FORMAT_BITS;
    public static final int BUFFER_FORMAT;

    private static final long NANO = (long) 1e+9;

    private @Getter FastLogger logger;

    private @Getter String name;

    private @Getter Looper videoLooper = new Looper();
    private @Getter Looper audioLooper = new Looper();

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

    static {
        ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);

        BUFFER_FORMAT = createImage(1, 1)
//            .getSnapshot()
            .getType();

        switch (BUFFER_FORMAT) {
            case BufferedImage.TYPE_INT_RGB: {
                IMAGE_FORMAT = "rgb32";
                IMAGE_FORMAT_BITS = 32;
                break;
            }

            case BufferedImage.TYPE_INT_ARGB:
            case BufferedImage.TYPE_INT_ARGB_PRE: {
                IMAGE_FORMAT = "argb";
                IMAGE_FORMAT_BITS = 32;
                break;
            }

            case BufferedImage.TYPE_4BYTE_ABGR:
            case BufferedImage.TYPE_4BYTE_ABGR_PRE:
            case BufferedImage.TYPE_INT_BGR: {
                IMAGE_FORMAT = "bgr32";
                IMAGE_FORMAT_BITS = 32;
                break;
            }

            case BufferedImage.TYPE_3BYTE_BGR: {
                IMAGE_FORMAT = "bgr24";
                IMAGE_FORMAT_BITS = 24;
                break;
            }

            case BufferedImage.TYPE_USHORT_565_RGB: {
                IMAGE_FORMAT = "rgb565";
                IMAGE_FORMAT_BITS = 16;
                break;
            }

            case BufferedImage.TYPE_USHORT_555_RGB: {
                IMAGE_FORMAT = "rgb555";
                IMAGE_FORMAT_BITS = 15;
                break;
            }

            case BufferedImage.TYPE_BYTE_GRAY: {
                IMAGE_FORMAT = "gray";
                IMAGE_FORMAT_BITS = 8;
                break;
            }

            case BufferedImage.TYPE_USHORT_GRAY: {
                IMAGE_FORMAT = "gray16be"; // OR le, TODO
                IMAGE_FORMAT_BITS = 16;
                break;
            }

            // BYTE_BINARY and BYTE_INDEXED are stupid.

            default: {
                throw new RuntimeException("Volatile image type unsupported: " + BUFFER_FORMAT);
            }
        }
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
        this.videoLooper.setFrameInterval(NANO / rate);
        this.audioLooper.setFrameInterval(NANO / AudioConstants.AUDIO_RATE);
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

    private void _asyncProcessVideo() {
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

        if (this.currentFrameData == null) {
            this.currentFrameData = this.blankFrameData;
        }

        if (this.target != null) {
            // Tell the target writer to write.
            synchronized (this.targetWriteThread) {
                this.targetWriteThread.notify();
            }
        }
    }

    private void _asyncProcessAudio() {
        float[] chunk = null;

        if (!this.scenes.isEmpty()) {
            // Bounds check.
            if ((this.currentSceneIndex < 0) || (this.currentSceneIndex >= this.scenes.size())) {
                this.currentSceneIndex = 0;
            }

            // Tell the scene to render.
            Scene currentScene = this.scenes.get(this.currentSceneIndex);

            chunk = currentScene.mixer.read();
            if (this.audioTarget == null) return; // Discard.
        }

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

    public void start() throws InterruptedException, IOException {
        assert this.isRenderable() : "This instance is NOT renderable.";
        assert !this.videoLooper.isRunning() : "This instance is already rendering!";

        // Setup the write thread.
        this.targetWriteThread = new Thread(this::_asyncWriteTarget);
        this.targetWriteThread.setName("Kaminari Async Video Write Thread: " + this.name);
        this.targetWriteThread.setDaemon(true);
        this.targetWriteThread.start();

        // Go.
        this.audioLooper.startAsync(this::_asyncProcessAudio, "Kaminari Async Audio Process Thread: " + this.name);
        this.videoLooper.start(this::_asyncProcessVideo);

        this.currentFrameData = null; // Free memory.
        this.stop();
    }

    public void stop() {
        this.videoLooper.stop();
        this.audioLooper.stop();

        if (this.targetWriteThread != null) this.targetWriteThread.interrupt();
    }

    @Override
    public void close() {
        this.stop();
    }

    @SneakyThrows
    public static BufferedImage createImage(int width, int height) {
        BufferedImage img;

        if (GraphicsEnvironment.isHeadless()) {
            img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB_PRE);
            FastLogger.logStatic(LogLevel.WARNING, "GraphicsEnvironment.isHeadless() returned true, expect video rendering to consume more CPU than normal.");
        } else {
            GraphicsConfiguration gcfg = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
            img = gcfg.createCompatibleImage(width, height, Transparency.TRANSLUCENT);
        }

        img.setAccelerationPriority(1);
        return img;
    }

}
