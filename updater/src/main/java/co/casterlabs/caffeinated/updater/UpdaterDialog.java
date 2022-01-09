package co.casterlabs.caffeinated.updater;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.DisplayMode;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.Closeable;
import java.io.IOException;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.SpringLayout;

import lombok.SneakyThrows;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public class UpdaterDialog extends JDialog implements Closeable {
    private static final long serialVersionUID = 327804372803161092L;

    private static final String[] STREAMERS = {
            "DivideAConquer",
            "FallenWolf",
            "GeenoTV",
            "Glitch3dPenguin"
    };

    public static final int WIDTH = 500;
    public static final int HEIGHT = 320;

    private String chosenStreamer = "DivideAConquer"; // Default is required for WindowBuilder.
    private Image chosenStreamerImage;

    private UpdaterUI ui;

    public static final Color TRANSPARENT_COLOR = new Color(0, 0, 0, 0);
    public static final Color BACKGROUND_COLOR = parseCSSColor("#121212");
    public static final Color TEXT_COLOR = parseCSSColor("#b5b5b5");

    public UpdaterDialog() throws IOException {
        super((Dialog) null); // Makes the app appear in the taskbar.

        SpringLayout layout = new SpringLayout();
        getContentPane().setLayout(layout);

        try {
            this.chosenStreamer = STREAMERS[(int) Math.floor(Math.random() * STREAMERS.length)];
            this.chosenStreamerImage = new ImageIcon(FileUtil.loadResourceAsUrl(String.format("assets/streamers/%s.png", this.chosenStreamer))).getImage();
            FastLogger.logStatic("Chosen Streamer: %s", this.chosenStreamer);
        } catch (Exception e) {
            FastLogger.logException(e);
        }

        this.setTitle("Caffeinated Updater");
        this.setAlwaysOnTop(true);
        this.setUndecorated(true);

        // Colors.
        this.setBackground(TRANSPARENT_COLOR);
        this.getContentPane().setBackground(TRANSPARENT_COLOR);
//        this.getContentPane().setBackground(BACKGROUND_COLOR);
        this.getContentPane().setForeground(TEXT_COLOR);

        // Size.
        this.setSize(WIDTH, HEIGHT);
        this.setResizable(false);

        this.ui = new UpdaterUI(this);
        layout.putConstraint(SpringLayout.NORTH, ui, 0, SpringLayout.NORTH, getContentPane());
        layout.putConstraint(SpringLayout.WEST, ui, 0, SpringLayout.WEST, getContentPane());
        layout.putConstraint(SpringLayout.SOUTH, ui, 0, SpringLayout.SOUTH, getContentPane());
        layout.putConstraint(SpringLayout.EAST, ui, 0, SpringLayout.EAST, getContentPane());
        getContentPane().add(this.ui);

        // Set the location.
        {
            Point mouseLoc = MouseInfo.getPointerInfo().getLocation();
            GraphicsDevice currentScreen = null;

            for (GraphicsDevice screen : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
                if (screen.getDefaultConfiguration().getBounds().contains(mouseLoc)) {
                    currentScreen = screen;
                    break;
                }
            }

            if (currentScreen == null) {
                currentScreen = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
            }

            DisplayMode display = currentScreen.getDisplayMode();

            if (display != null) {
                int x = (display.getWidth() / 2) - (WIDTH / 2);
                int y = (display.getHeight() / 2) - (HEIGHT / 2);

                this.setLocation(x, y);
            }
        }

        // Drag listener.
        {
            DragListener frameDragListener = new DragListener(this);

            this.addMouseListener(frameDragListener);
            this.addMouseMotionListener(frameDragListener);
        }

        // Set the icon.
        try {
            URL iconUrl = FileUtil.loadResourceAsUrl("assets/logo/casterlabs.png");
            ImageIcon img = new ImageIcon(iconUrl);

            this.setIconImage(img.getImage());
        } catch (Exception e) {
            FastLogger.logStatic(LogLevel.SEVERE, "Could not set the dialog's icon.");
            FastLogger.logException(e);
        }

        // Handle the tray's close button.
        this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                close();
            }
        });
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        // Paint the container.
        super.paint(g2d);

        // Enable antialiasing.
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        // Paint the background color
//        g2d.clearRect(0, 0, WIDTH, HEIGHT);
        g2d.setColor(BACKGROUND_COLOR);
        g2d.fillRect(0, 0, WIDTH, HEIGHT);

        // Paint the background image if set
        if (this.chosenStreamerImage != null) {
            // The image is same size as the window.
            g2d.drawImage(this.chosenStreamerImage, 0, 0, null);
        }

        // Paint all children.
        super.paintComponents(g2d);
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);

        if (visible) {
            this.createBufferStrategy(2);
        }
    }

    @Override
    public void close() {
        this.dispose();
        System.exit(0);
    }

    @SneakyThrows
    public static Color parseCSSColor(String color) {
        return xyz.e3ndr.javawebcolor.Color.parseCSSColor(color).toAWTColor();
    }

    public void setStatus(String status) {
        this.ui.setStatus(status);
    }

    public void setProgress(double progress) {
        this.ui.setProgress(progress);
    }

}
