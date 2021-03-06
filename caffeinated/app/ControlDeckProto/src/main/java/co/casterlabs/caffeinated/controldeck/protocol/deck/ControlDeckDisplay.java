package co.casterlabs.caffeinated.controldeck.protocol.deck;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import co.casterlabs.caffeinated.controldeck.protocol.packets.CD_PacketDisplayTouch;
import co.casterlabs.caffeinated.controldeck.protocol.packets.CD_PacketDisplayUpdate;
import lombok.Getter;
import lombok.SneakyThrows;

@Getter
public class ControlDeckDisplay {
    private ControlDeck deck;

    private int width;
    private int height;

    private JPanel panel;

    ControlDeckDisplay(int width, int height, ControlDeck deck) {
        this.width = width;
        this.height = height;
        this.deck = deck;

        this.panel = new JPanel();

        this.panel.addNotify(); // Convinces the JPanel to render, IDK why.
        this.panel.setSize(getDisplaySize());
    }

    public Dimension getDisplaySize() {
        return new Dimension(this.width, this.height);
    }

    /* -- Internals -- */

    void close() {

    }

    @SneakyThrows
    public void render() {
        BufferedImage image = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = image.createGraphics();
        this.panel.paint(g);
        g.dispose();

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", out);

            byte[] bytes = out.toByteArray();

            this.deck.conn.sendPacket(
                new CD_PacketDisplayUpdate()
                    .setPngData(bytes)
            );
        }
    }

    void onPacketDisplayTouch(CD_PacketDisplayTouch touchPacket) {
        Point point = new Point(touchPacket.getX(), touchPacket.getY());

//        this.panel.dispatchEvent(new MouseEvent(panel, height, height, height, height, height, height, false)); // TODO
    }

}
