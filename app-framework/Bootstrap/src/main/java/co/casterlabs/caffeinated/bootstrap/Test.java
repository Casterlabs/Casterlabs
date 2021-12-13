package co.casterlabs.caffeinated.bootstrap;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JFrame;
import javax.swing.JLabel;

import co.casterlabs.caffeinated.bootstrap.ui.LafManager;

public class Test {

    public static void main(String[] args) {
        LafManager.setupLaf();

        JFrame frame = new JFrame();

        frame.setLayout(new BorderLayout(0, 0));
        frame.setSize(400, 400);

        JLabel text = new JLabel("Test!");
        text.setForeground(Color.WHITE);

        frame.getContentPane().add(text, BorderLayout.CENTER);
        frame.getContentPane().setBackground(Color.BLUE);

        LafManager.frameInit(frame);

        frame.setVisible(true);
    }

}
