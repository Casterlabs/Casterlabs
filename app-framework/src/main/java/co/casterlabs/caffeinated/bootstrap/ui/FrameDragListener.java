package co.casterlabs.caffeinated.bootstrap.ui;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseEvent;

import javax.swing.JFrame;
import javax.swing.event.MouseInputAdapter;

public class FrameDragListener extends MouseInputAdapter {
    private JFrame frame;
    private Point mouseDownCompCoords = null;

    public FrameDragListener(JFrame frame, Component dragComponent) {
        this.frame = frame;

        dragComponent.addMouseListener(this);
        dragComponent.addMouseMotionListener(this);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        this.mouseDownCompCoords = null;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        this.mouseDownCompCoords = e.getPoint();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        Point currCoords = e.getLocationOnScreen();
        this.frame.setLocation(currCoords.x - this.mouseDownCompCoords.x, currCoords.y - this.mouseDownCompCoords.y);
    }

}
