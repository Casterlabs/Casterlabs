package co.casterlabs.caffeinated.updater;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.DisplayMode;
import java.awt.Font;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.Closeable;
import java.io.IOException;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.SpringLayout;

import lombok.NonNull;
import lombok.SneakyThrows;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public class UpdaterUI extends JDialog implements Closeable {
    private static final long serialVersionUID = 327804372803161092L;

    public static final Color BACKGROUND_COLOR = parseCSSColor("#121212");
    public static final Color TEXT_COLOR = parseCSSColor("#b5b5b5");

    private static final int WIDTH = 500;
    private static final int HEIGHT = 320;

    private JLabel statusText;

    public UpdaterUI() throws IOException {
        super((Dialog) null); // Makes the app appear in the taskbar.

        SpringLayout dialogLayout = new SpringLayout();

        this.setTitle("Caffeinated Updater");
        this.setAlwaysOnTop(true);
        this.setUndecorated(true);
        this.getContentPane().setLayout(dialogLayout);

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
            URL iconUrl = FileUtil.loadResourceAsUrl("assets/logo/casterlabs.png");
            ImageIcon img = new ImageIcon(iconUrl);

            this.setIconImage(img.getImage());
        } catch (Exception e) {
            FastLogger.logStatic(LogLevel.SEVERE, "Could not set the dialog's icon.");
            FastLogger.logException(e);
        }

        // Colors.
        this.getContentPane().setBackground(BACKGROUND_COLOR);
        this.getContentPane().setForeground(TEXT_COLOR);

        statusText = new JLabel();
        dialogLayout.putConstraint(SpringLayout.SOUTH, statusText, -32, SpringLayout.SOUTH, getContentPane());
        dialogLayout.putConstraint(SpringLayout.EAST, statusText, -149, SpringLayout.EAST, getContentPane());
        statusText.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        statusText.setForeground(TEXT_COLOR);
        statusText.setBackground(BACKGROUND_COLOR);
        statusText.setText("Downloading updates... (22.6 / 40 MB)");
        getContentPane().add(statusText);

        LoadingSpinner loadingSpinner = new LoadingSpinner();
        dialogLayout.putConstraint(SpringLayout.NORTH, statusText, 13, SpringLayout.NORTH, loadingSpinner);
        dialogLayout.putConstraint(SpringLayout.WEST, statusText, 6, SpringLayout.EAST, loadingSpinner);
        dialogLayout.putConstraint(SpringLayout.NORTH, loadingSpinner, 255, SpringLayout.NORTH, getContentPane());
        dialogLayout.putConstraint(SpringLayout.SOUTH, loadingSpinner, -19, SpringLayout.SOUTH, getContentPane());
        dialogLayout.putConstraint(SpringLayout.WEST, loadingSpinner, 20, SpringLayout.WEST, getContentPane());
        dialogLayout.putConstraint(SpringLayout.EAST, loadingSpinner, 70, SpringLayout.WEST, getContentPane());
        getContentPane().add(loadingSpinner);

        ImageButton closeButton = new ImageButton("close.png", this::close);
        dialogLayout.putConstraint(SpringLayout.NORTH, closeButton, 10, SpringLayout.NORTH, getContentPane());
        dialogLayout.putConstraint(SpringLayout.WEST, closeButton, -43, SpringLayout.EAST, getContentPane());
        dialogLayout.putConstraint(SpringLayout.SOUTH, closeButton, 45, SpringLayout.NORTH, getContentPane());
        dialogLayout.putConstraint(SpringLayout.EAST, closeButton, -10, SpringLayout.EAST, getContentPane());
        getContentPane().add(closeButton);

        // Handle the tray's close button.
        this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                close();
            }
        });
    }

    public void setProgress(double progress) {
        progress = Math.round(progress * 100); // 0-1 -> 0-100

        // This was added in Java9, TODO update my eclipse installation.
//        Taskbar taskbar = Taskbar.getTaskbar();
//        taskbar.setWindowProgressState(this, State.ERROR);
//        taskbar.setWindowProgressValue(this, 50);
    }

    public void setStatus(@NonNull String status) {
        this.statusText.setText(status);
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

}
