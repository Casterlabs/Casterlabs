package co.casterlabs.caffeinated.bootstrap;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import co.casterlabs.caffeinated.bootstrap.theming.LafManager;
import co.casterlabs.caffeinated.bootstrap.theming.ThemeableJFrame;

public class Test {

    public static void main(String[] args) {
        // Enable assertions programatically.
        ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);

        ThemeableJFrame frame = LafManager.getFrame();

        JLabel text = new JLabel("Test!", SwingConstants.CENTER);

        frame.getContentPane().add(text, BorderLayout.CENTER);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 400);
        frame.setTitle("Window Test");
        frame.setVisible(true);

        new Thread(() -> {
            try {
                while (true) {
                    ThemeableJFrame.setDarkMode(true);
                    Thread.sleep(5000);
                    ThemeableJFrame.setDarkMode(false);
                    Thread.sleep(5000);
                }
            } catch (Exception ignored) {}
        }).start();

    }

}
