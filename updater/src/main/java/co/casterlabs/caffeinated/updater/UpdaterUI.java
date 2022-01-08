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
import java.io.IOException;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.SpringLayout;

import lombok.SneakyThrows;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public class UpdaterUI extends JDialog {
    private static final long serialVersionUID = 327804372803161092L;

    public static final Color BACKGROUND_COLOR = parseCSSColor("#121212");
    public static final Color TEXT_COLOR = parseCSSColor("#b5b5b5");

    private static final int WIDTH = 500;
    private static final int HEIGHT = 320;

    private JLabel statusText;
    private JLabel progressText;

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

            int x = (display.getWidth() / 2) - (WIDTH / 2);
            int y = (display.getHeight() / 2) - (HEIGHT / 2);

            this.setLocation(x, y);
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

        progressText = new JLabel();
        dialogLayout.putConstraint(SpringLayout.SOUTH, progressText, 301, SpringLayout.NORTH, getContentPane());
        dialogLayout.putConstraint(SpringLayout.EAST, progressText, -295, SpringLayout.EAST, getContentPane());
        progressText.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        progressText.setForeground(TEXT_COLOR);
        progressText.setBackground(BACKGROUND_COLOR);
        progressText.setText("12.6 / 24 MB");
        getContentPane().add(progressText);

        statusText = new JLabel();
        dialogLayout.putConstraint(SpringLayout.NORTH, statusText, 255, SpringLayout.NORTH, getContentPane());
        dialogLayout.putConstraint(SpringLayout.SOUTH, statusText, -45, SpringLayout.SOUTH, getContentPane());
        dialogLayout.putConstraint(SpringLayout.NORTH, progressText, 6, SpringLayout.SOUTH, statusText);
        dialogLayout.putConstraint(SpringLayout.WEST, progressText, 0, SpringLayout.WEST, statusText);
        dialogLayout.putConstraint(SpringLayout.WEST, statusText, 70, SpringLayout.WEST, getContentPane());
        dialogLayout.putConstraint(SpringLayout.EAST, statusText, -155, SpringLayout.EAST, getContentPane());
        statusText.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        statusText.setForeground(TEXT_COLOR);
        statusText.setBackground(BACKGROUND_COLOR);
        statusText.setText("Downloading updates...");
        getContentPane().add(statusText);

        LoadingSpinner loadingSpinner = new LoadingSpinner();
        dialogLayout.putConstraint(SpringLayout.NORTH, loadingSpinner, 0, SpringLayout.NORTH, statusText);
        dialogLayout.putConstraint(SpringLayout.WEST, loadingSpinner, 20, SpringLayout.WEST, getContentPane());
        dialogLayout.putConstraint(SpringLayout.SOUTH, loadingSpinner, 0, SpringLayout.SOUTH, progressText);
        dialogLayout.putConstraint(SpringLayout.EAST, loadingSpinner, 70, SpringLayout.WEST, getContentPane());
        getContentPane().add(loadingSpinner);

        // Handle the close button.
        this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
                System.exit(0);
            }
        });

        this.setVisible(true);
    }

    @SneakyThrows
    public static Color parseCSSColor(String color) {
        return xyz.e3ndr.javawebcolor.Color.parseCSSColor(color).toAWTColor();
    }
}
