package co.casterlabs.caffeinated.updater;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.io.Closeable;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

public class LoadingSpinner extends JPanel implements Closeable {
    private static final long serialVersionUID = 8420714649640311101L;

    private Timer timer = new Timer(50, this::rotationHandler);
    private int rotation = 0;

    private JLabel label;

    public LoadingSpinner() throws IOException {
        this.label = new JLabel();
        this.label.setIcon(new ImageIcon(FileUtil.loadResourceAsUrl("assets/loading-spinner.png")));

        this.add(this.label);

        this.setBackground(UpdaterUI.BACKGROUND_COLOR);
    }

    private void rotationHandler(ActionEvent e) {
        if (this.isVisible()) {
            this.rotation += 10;
            this.rotation %= 360;

            this.repaint();
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;

        float x = this.getWidth() / 2.0f;
        float y = this.getHeight() / 2.0f;

        g2d.rotate(this.rotation / 180.0 * Math.PI, x, y);
    }

    public void start() {
        this.timer.start();
    }

    @Override
    public void close() {
        this.timer.stop();
    }

}
