package co.casterlabs.kaminari.core.scene;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import co.casterlabs.kaminari.core.Kaminari;
import co.casterlabs.kaminari.core.source.Source;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;

public class Scene {
    private @Getter Kaminari kaminari;

    private @Getter String id;
    private @Getter String name;

    private JPanel panel = new JPanel();

    private @Getter BufferedImage frameBuffer;
    private Graphics2D g;

    public byte[] currentFrameData;
    private long lastFrameRendered = -1;

    private @Getter int width = -1; // Pixels
    private @Getter int height = -1; // Pixels

    private List<Source> sources = new ArrayList<>();

    public Scene(@NonNull Kaminari kaminari, @NonNull String id, @NonNull String name) {
        this.kaminari = kaminari;
        this.id = id;
        this.name = name;

        // Convince the panel to be renderable.
        this.panel.setOpaque(true);
        this.panel.setBackground(Color.BLACK);
        this.panel.addNotify();

        this.setSize(kaminari.getWidth(), kaminari.getHeight());
    }

    /* ---------------- */
    /* Properties       */
    /* ---------------- */

    public void setSize(int width, int height) {
        assert width > 0 : "Width must be greater than 0.";
        assert height > 0 : "Height must be greater than 0.";

        this.width = width;
        this.height = height;
        this.panel.setSize(width, height);

        this.frameBuffer = new BufferedImage(this.width, this.height, Kaminari.BUFFER_FORMAT);
        this.g = this.frameBuffer.createGraphics();

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

    public void render() throws InterruptedException, IOException {
        assert this.isRenderable() : "This instance is NOT renderable.";

        long lastFrame = this.kaminari.getFramesRendered();

        if (this.lastFrameRendered == lastFrame) {
            return; // Already rendered.
        }
        this.lastFrameRendered = lastFrame;

        // Loop over the sources and notify them.
        this.sources.forEach((source) -> source.onRender());

        // Render
//        this.g.clearRect(0, 0, this.width, this.height);
        this.panel.paint(this.g);

        // Dump the frame buffer data.
        this.currentFrameData = this.getFrameBufferData();
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

        source.onPack();

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

    private byte[] getFrameBufferData() {
        Raster raster = this.frameBuffer.getRaster();
        DataBufferInt buffer = (DataBufferInt) raster.getDataBuffer();
        int[] data = buffer.getData();

        ByteBuffer byteBuffer = ByteBuffer.allocate(data.length * 4);
        IntBuffer intBuffer = byteBuffer.asIntBuffer();
        intBuffer.put(data);

        return byteBuffer.array();
    }

}
