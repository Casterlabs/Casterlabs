package co.casterlabs.caffeinated.updater.window;

import java.awt.Color;
import java.awt.Font;
import java.io.IOException;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

import lombok.NonNull;

public class UpdaterUI extends JPanel {
    private static final long serialVersionUID = -6590073036152631171L;

    private JLabel statusText;

    public UpdaterUI(UpdaterDialog dialog) throws IOException {
        SpringLayout layout = new SpringLayout();

        this.setBackground(UpdaterDialog.BACKGROUND_COLOR);
        this.setSize(UpdaterDialog.WIDTH, UpdaterDialog.HEIGHT);
        this.setLayout(layout);

        // Check for this variable, so we can actually *see* what we're
        // doing in WindowBuilder.
        if (dialog != null) {
            this.setOpaque(false);
            this.setBackground(new Color(0, 0, 0, 0));
        }

        statusText = new JLabel();
        layout.putConstraint(SpringLayout.SOUTH, statusText, -32, SpringLayout.SOUTH, this);
        layout.putConstraint(SpringLayout.EAST, statusText, -149, SpringLayout.EAST, this);
        statusText.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        statusText.setForeground(UpdaterDialog.TEXT_COLOR);
        statusText.setOpaque(false);
        statusText.setText("Checking for updates...");
        this.add(statusText);

        LoadingSpinner loadingSpinner = new LoadingSpinner();
        layout.putConstraint(SpringLayout.NORTH, statusText, 13, SpringLayout.NORTH, loadingSpinner);
        layout.putConstraint(SpringLayout.WEST, statusText, 6, SpringLayout.EAST, loadingSpinner);
        layout.putConstraint(SpringLayout.NORTH, loadingSpinner, 255, SpringLayout.NORTH, this);
        layout.putConstraint(SpringLayout.SOUTH, loadingSpinner, -19, SpringLayout.SOUTH, this);
        layout.putConstraint(SpringLayout.WEST, loadingSpinner, 20, SpringLayout.WEST, this);
        layout.putConstraint(SpringLayout.EAST, loadingSpinner, 70, SpringLayout.WEST, this);
        this.add(loadingSpinner);

        ImageButton closeButton = new ImageButton("close.png", dialog::close);
        layout.putConstraint(SpringLayout.NORTH, closeButton, 10, SpringLayout.NORTH, this);
        layout.putConstraint(SpringLayout.WEST, closeButton, -43, SpringLayout.EAST, this);
        layout.putConstraint(SpringLayout.SOUTH, closeButton, 45, SpringLayout.NORTH, this);
        layout.putConstraint(SpringLayout.EAST, closeButton, -10, SpringLayout.EAST, this);
        this.add(closeButton);

        ImageButton casterlabsBanner = new ImageButton("banner.png", null);
        layout.putConstraint(SpringLayout.NORTH, casterlabsBanner, 10, SpringLayout.NORTH, this);
        layout.putConstraint(SpringLayout.WEST, casterlabsBanner, 10, SpringLayout.WEST, this);
        layout.putConstraint(SpringLayout.SOUTH, casterlabsBanner, 93, SpringLayout.NORTH, this);
        layout.putConstraint(SpringLayout.EAST, casterlabsBanner, 265, SpringLayout.WEST, this);
        add(casterlabsBanner);

    }

    public void setStatus(@NonNull String status) {
        this.statusText.setText(status);
        this.repaint();
    }

}
