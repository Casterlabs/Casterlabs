package co.casterlabs.caffeinated.updater.window;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.DisplayMode;
import java.awt.Graphics;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Taskbar;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.Closeable;
import java.io.IOException;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JDialog;

import co.casterlabs.caffeinated.updater.util.FileUtil;
import lombok.Getter;
import lombok.SneakyThrows;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public class UpdaterDialog extends JDialog implements Closeable {
    private static final long serialVersionUID = 327804372803161092L;

    public static final int WIDTH = 500;
    public static final int HEIGHT = 320;

    public static final Color TRANSPARENT_COLOR = new Color(0, 0, 0, 0);
    public static final Color BACKGROUND_COLOR = parseCSSColor("#121212");
    public static final Color TEXT_COLOR = parseCSSColor("#b5b5b5");

    private @Getter UpdaterPane pane;
    private @Getter UpdaterUI ui;

    public UpdaterDialog() throws IOException {
        super((Dialog) null); // Makes the app appear in the taskbar.

        this.pane = new UpdaterPane(this);
        this.ui = this.pane.getUi();

        this.getContentPane().add(this.pane);

        this.setTitle("Caffeinated Updater");
        this.setAlwaysOnTop(true);
        this.setUndecorated(true);

        // Colors.
        this.setBackground(BACKGROUND_COLOR);
        this.getContentPane().setBackground(BACKGROUND_COLOR);
        this.setForeground(TEXT_COLOR);

        // Size.
        this.setSize(WIDTH, HEIGHT);
        this.setResizable(false);

        // Drag listener.
        {
            DragListener frameDragListener = new DragListener(this);

            this.addMouseListener(frameDragListener);
            this.addMouseMotionListener(frameDragListener);
        }

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

        // Set the icon.
        try {
            URL iconUrl = FileUtil.loadResourceAsUrl("assets/icon.png");
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
    public synchronized void paint(Graphics g) {
        // Wait for an animation frame.
        // This helps with stuttering.
        if (AnimationContext.isAnimationFrame()) {
            super.paint(g);
            AnimationContext.reset();
        }
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);

        if (visible) {
            this.createBufferStrategy(2);
            AnimationContext.setRepaintable(this);
        }
    }

    @Override
    public void close() {
        this.dispose();
        System.exit(0);
    }

    @Override
    public void dispose() {
        AnimationContext.setRepaintable(null);
        super.dispose();
    }

    @SneakyThrows
    public static Color parseCSSColor(String color) {
        return xyz.e3ndr.javawebcolor.Color.parseCSSColor(color).toAWTColor();
    }

    public void setStatus(String status) {
        this.ui.setStatus(status);
    }

    public void setProgress(double progress) {
        if (Taskbar.isTaskbarSupported()) {
            Taskbar taskbar = Taskbar.getTaskbar();

            if (taskbar.isSupported(Taskbar.Feature.PROGRESS_STATE_WINDOW)) {
                if (progress < 0) {
                    taskbar.setWindowProgressState(this, Taskbar.State.OFF);
                } else {
                    int percent = (int) Math.round(progress * 100); // 0-1 -> 0-100

                    taskbar.setWindowProgressState(this, Taskbar.State.NORMAL);
                    taskbar.setWindowProgressValue(this, percent);
                }
            }
        }
    }

    public void setLoading(boolean loading) {
        this.ui.setLoading(loading);
    }

}
