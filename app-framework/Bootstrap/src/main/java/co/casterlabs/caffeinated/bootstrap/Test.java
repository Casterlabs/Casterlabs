package co.casterlabs.caffeinated.bootstrap;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JFrame;
import javax.swing.JLabel;

import co.casterlabs.caffeinated.bootstrap.ui.LafManager;
import co.casterlabs.caffeinated.bootstrap.ui.ThemeableJFrame;

public class Test {

    public static void main(String[] args) {
        // Enable assertions programatically.
        ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);

        ThemeableJFrame frame = LafManager.getFrame();

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(0, 0));
        frame.setSize(400, 400);
        frame.setDarkMode(true);

        JLabel text = new JLabel("Test!");
        text.setForeground(Color.WHITE);

        frame.getContentPane().add(text, BorderLayout.CENTER);
        frame.getContentPane().setBackground(Color.DARK_GRAY);

        frame.setVisible(true);

    }

}
