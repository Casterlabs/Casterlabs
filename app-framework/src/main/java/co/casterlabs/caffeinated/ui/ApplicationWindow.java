package co.casterlabs.caffeinated.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.WindowConstants;
import javax.swing.border.Border;

import lombok.Getter;
import xyz.e3ndr.fastloggingframework.FastLoggingFramework;

@Getter
public class ApplicationWindow {
    private JFrame frame;
    private JPanel titleBarPanel;
    private JPanel cefPanel;
    private JLabel titleLabel;
    private UILifeCycleListener listener;

    // Helps make it mimic a native application.
    private Border unfocusedBorder = BorderFactory.createMatteBorder(1, 1, 1, 1, Color.GRAY);
    private Border focusedBorder = BorderFactory.createMatteBorder(1, 1, 1, 1, new Color(69, 69, 69));
    private JButton closeButton;

    public ApplicationWindow(UILifeCycleListener listener) {
        this.listener = listener;
        this.frame = new JFrame();

        this.frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        this.frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowActivated(WindowEvent e) {
                frame.getRootPane().setBorder(focusedBorder);
            }

            @Override
            public void windowDeactivated(WindowEvent e) {
                frame.getRootPane().setBorder(unfocusedBorder);
            }

            @Override
            public void windowClosing(WindowEvent e) {
                onClose();
            }
        });

        this.frame.setUndecorated(true);
        this.frame.setResizable(true);
        this.frame.setSize(800, 600);
        this.frame.getRootPane().setBorder(this.unfocusedBorder);

        // TODO add close buttons and minimize and the like to the title panel (?)
        this.titleBarPanel = new JPanel();
        this.frame.getContentPane().add(this.titleBarPanel, BorderLayout.NORTH);
        this.titleBarPanel.setPreferredSize(new Dimension(80, 24));
        this.titleBarPanel.setBackground(Color.LIGHT_GRAY);
        SpringLayout titleBarPanelLayout = new SpringLayout();
        this.titleBarPanel.setLayout(titleBarPanelLayout);

        this.titleLabel = new JLabel("AppTitleBar");
        titleBarPanelLayout.putConstraint(
            SpringLayout.NORTH, this.titleLabel, 5, SpringLayout.NORTH,
            this.titleBarPanel
        );
        titleBarPanelLayout.putConstraint(
            SpringLayout.WEST, this.titleLabel, 10, SpringLayout.WEST,
            this.titleBarPanel
        );
        titleBarPanelLayout.putConstraint(
            SpringLayout.EAST, this.titleLabel, -376, SpringLayout.EAST,
            this.titleBarPanel
        );
        this.titleBarPanel.add(this.titleLabel);

        new FrameDragListener(this.frame, this.titleBarPanel);

        this.closeButton = new JButton("X");
        titleBarPanelLayout.putConstraint(
            SpringLayout.NORTH, this.closeButton, 0, SpringLayout.NORTH,
            this.titleBarPanel
        );
        titleBarPanelLayout.putConstraint(
            SpringLayout.EAST, this.closeButton, 0, SpringLayout.EAST,
            this.titleBarPanel
        );
        this.titleBarPanel.add(this.closeButton);

        this.closeButton.addActionListener((ActionEvent e) -> onClose());

        this.cefPanel = new JPanel();
        this.frame.getContentPane().add(this.cefPanel, BorderLayout.CENTER);
        this.cefPanel.setLayout(new BorderLayout(0, 0));
    }

    private void onClose() {
        if (this.listener.onCloseAttempt()) {
            this.frame.dispose();
            ApplicationUI.getDevtools().destroy();
            FastLoggingFramework.close(); // Faster shutdown.
        }
    }

}
