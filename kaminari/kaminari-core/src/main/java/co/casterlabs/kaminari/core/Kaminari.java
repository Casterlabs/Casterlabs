package co.casterlabs.kaminari.core;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import co.casterlabs.kaminari.core.source.Source;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.SneakyThrows;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

public class Kaminari implements Closeable {
    public static final String IMAGE_FORMAT = "argb"; // Note that Kaminari's output will NEVER utilize opacity.
    public static final int BUFFER_FORMAT = BufferedImage.TYPE_INT_ARGB;

    private static final long NANO = (long) 1e+9;

    private @Getter FastLogger logger;

    private @Getter String name;

    private JPanel panel;

    private @Getter BufferedImage frameBuffer;
    private @Getter long framesRendered;
    private @Getter long framesTargeted;
    private @Getter long frameTime;

    private long startTime = -1;
    private long frameInterval;
    private long lastRender;

    private @Getter int frameRate;
    private @Getter int width = -1; // Pixels
    private @Getter int height = -1; // Pixels

    private List<Source> sources = new ArrayList<>();

    private @Getter @Setter OutputStream target;
    private byte[] currentFrameData;
    private Thread targetWriteThread;

    private boolean shouldRender = false;

    static {
        ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);
    }

    public Kaminari(@NonNull String name) {
        this.name = name;
        this.logger = new FastLogger("Kaminari: " + name);

        this.panel = new JPanel();
        this.panel.setBackground(Color.BLACK);

        this.setFramerate(30);

        // Convince the panel to be renderable.
        this.panel.addNotify();
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
        this.panel.setSize(width, height);

        this.frameBuffer = new BufferedImage(this.width, this.height, BUFFER_FORMAT);

        for (Source source : this.sources) {
            this.pack(source);
        }
    }

    /* ---------------- */
    /* Rendering        */
    /* ---------------- */

    public boolean isRenderable() {
        return this.frameBuffer != null;
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
                    this.logger.severe("Unable to write to target, stopping stream.\n%s", e);
                    this.stop();
                }
            }
        } catch (InterruptedException e) {
            Thread.interrupted(); // Clear
            // Exit.
        }
    }

    public void start() throws InterruptedException, IOException {
        assert this.isRenderable() : "This instance is NOT renderable.";
        assert !this.shouldRender : "This instance is already rendering!";

        this.startTime = System.nanoTime();
        this.lastRender = this.startTime + this.frameInterval;
        this.shouldRender = true;
        this.framesRendered = 0;
        this.framesTargeted = 1;
        this.frameTime = 0;

        // Setup the write thread.
        this.targetWriteThread = new Thread(this::_asyncWriteTarget);
        this.targetWriteThread.setName("Kaminari Async Write Thread: " + name);
        this.targetWriteThread.setDaemon(true);
        this.targetWriteThread.start();

        while (this.shouldRender) {
            boolean shouldLog = this.framesRendered % this.frameRate == 0;

            // Frame interval check/sleep.
            {
                long now = System.nanoTime();
                long delta = (now - this.lastRender);
                long wait = (long) ((this.frameInterval - delta) / 1e+6);

                if (wait > 0) {
                    this.logger.trace("Waiting %dms and then rendering.", wait);
                    Thread.sleep(wait);
                } else {
                    this.logger.severe("We're behind!"); // Always log.
                }
            }

            // Render.
            long renderStart = System.currentTimeMillis();

            // Loop over the sources and notify them.
            this.sources.forEach((source) -> source.onRender());

            // Render
            Graphics2D g = this.frameBuffer.createGraphics();
            this.panel.paint(g);
            g.dispose();

            this.currentFrameData = this.getFrameBufferData();

            if (this.target != null) {
                synchronized (this.targetWriteThread) {
                    this.targetWriteThread.notify();
                }
            }

            long renderEnd = System.currentTimeMillis();

            // Debug stats.
            this.frameTime = renderEnd - renderStart;
            this.lastRender += this.frameInterval; // Doing it this way allows the renderer to crank out missed frames rather than
                                                   // skip them.
            this.framesRendered++;
            this.framesTargeted = ((this.lastRender - this.startTime) / this.frameInterval) - 1; // TODO figure out where this one comes from.

            if (shouldLog) {
                this.logger.debug("Rendered frame #%d in %dms.", this.framesRendered, this.frameTime);
                this.logger.debug("Render stats: %d/%d", this.framesRendered, this.framesTargeted);
            }
        }

        this.currentFrameData = null; // Free memory.
    }

    public void stop() {
        this.targetWriteThread.interrupt();
        this.shouldRender = false;
    }

    @Override
    public void close() {
        this.stop();
    }

    /* ---------------- */
    /* Sources          */
    /* ---------------- */

    public void pack(@NonNull Source source) {
        if (!this.sources.contains(source)) {
            return; // NO-OP
        }

        // @formatter:off
        int left   = (int) (source.getX()      * this.width );
        int top    = (int) (source.getY()      * this.height);
        int width  = (int) (source.getWidth()  * this.width );
        int height = (int) (source.getHeight() * this.height);
        int zIndex = this.sources.indexOf(source);
        // @formatter:on

        source.panel.setBounds(left, top, width, height);
        this.panel.setComponentZOrder(source.panel, zIndex);

//        this.logger.debug(
//            "[%s/%s] Pack: left=%d, top=%d, width=%d, height=%d",
//            source.name, source.id,
//            left, top, width, height
//        );
    }

    @SneakyThrows
    public void add(@NonNull Source source) {
        assert !this.sources.contains(source) : "That source is already registered.";

        this.sources.add(source);
        this.panel.add(source.panel);
        this.pack(source);

        source.onMount();
    }

    public void remove(@NonNull Source source) {
        assert this.sources.contains(source) : "That source is not registered.";

        this.panel.remove(source.panel);
        this.sources.remove(source);
        source.onDestroy();
    }

    public byte[] getFrameBufferData() {
        Raster raster = this.frameBuffer.getRaster();
        DataBufferInt buffer = (DataBufferInt) raster.getDataBuffer();
        int[] data = buffer.getData();

        ByteBuffer byteBuffer = ByteBuffer.allocate(data.length * 4);
        IntBuffer intBuffer = byteBuffer.asIntBuffer();
        intBuffer.put(data);

        return byteBuffer.array();
    }

}
