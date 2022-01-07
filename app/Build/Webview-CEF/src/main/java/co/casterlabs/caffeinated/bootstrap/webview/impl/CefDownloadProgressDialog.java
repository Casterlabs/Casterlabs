package co.casterlabs.caffeinated.bootstrap.webview.impl;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.Closeable;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

import co.casterlabs.caffeinated.bootstrap.FileUtil;
import co.casterlabs.caffeinated.util.DualConsumer;
import co.casterlabs.caffeinated.util.async.AsyncTask;
import lombok.SneakyThrows;
import me.friwi.jcefmaven.EnumProgress;
import me.friwi.jcefmaven.IProgressHandler;
import xyz.e3ndr.consoleutil.consolewindow.BarStyle;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

public class CefDownloadProgressDialog implements IProgressHandler, Closeable {
    private FastLogger logger = new FastLogger("Cef Bundle");

    private DualConsumer<EnumProgress, Integer> progressHandler;
    private JDialog progressFrame;

    @Override
    public void handleProgress(EnumProgress state, float percent) {
        // `percent` is 0-100.

        if (percent < 0) {
            this.logger.info(
                "%-13s",
                state
            );
        } else {
            this.logger.info(
                "%-13s %s",
                state,
                BarStyle.ANGLE.format(percent / 100, 30, true)
            );
        }

        if ((state == EnumProgress.DOWNLOADING) && (progressFrame == null)) {
            this.createDownloadDialog();
        }

        if (progressFrame != null) {
            if (state == EnumProgress.INITIALIZED) {
                new AsyncTask(this::close); // Clean up asynchronously.
            } else {
                this.progressHandler.accept(state, (int) percent);
            }
        }
    }

    @SneakyThrows
    private void createDownloadDialog() {
        JDialog frame = new JDialog();

        frame.setLayout(new BorderLayout(0, 0));
        frame.setResizable(false);
        frame.setSize(300, 100);
        frame.setAlwaysOnTop(true);
        frame.setTitle("Casterlabs Caffeinated");

        frame.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                frame.dispose();
                System.exit(0);
            }
        });

        // Setup progress bar
        JProgressBar progressBar = new JProgressBar(0, 100);

        progressBar.setValue(0);
        progressBar.setStringPainted(true);

        frame.getContentPane().add(progressBar, BorderLayout.CENTER);

        // The state text
        JLabel stateText = new JLabel();

        stateText.setFont(stateText.getFont().deriveFont(14f));
        stateText.setHorizontalAlignment(SwingConstants.CENTER);

        frame.getContentPane().add(stateText, BorderLayout.NORTH);

        // Set the icon.
        {
            URL iconUrl = FileUtil.loadResourceAsUrl("assets/logo/casterlabs.png");
            ImageIcon img = new ImageIcon(iconUrl);

            frame.setIconImage(img.getImage());
        }

        DualConsumer<EnumProgress, Integer> progressHandler = (state, progress) -> {
            progressBar.setVisible(progress > 0);
            progressBar.setValue(progress);

            stateText.setText(enumProgressToString(state));
        };

        frame.setVisible(true);

        this.progressFrame = frame;
        this.progressHandler = progressHandler;
    }

    @Override
    public void close() {
        this.progressFrame.dispose();
        this.progressFrame = null;
        this.progressHandler = null;
    }

    private static String enumProgressToString(EnumProgress progress) {
        switch (progress) {
            case DOWNLOADING:
                return "Downloading additional dependencies...";

            case EXTRACTING:
                return "Extracting additional dependencies...";

            case INITIALIZED:
                return "Done!";

            case INITIALIZING:
                return "Initializing additional dependencies...";

            case INSTALL:
                return "Installing additional dependencies...";

            case LOCATING:
                return "Locating additional dependencies...";

            default:
                return "";

        }
    }

}
