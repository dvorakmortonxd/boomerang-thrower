package dev.dvorakmortonxd;

import java.awt.event.*;

public class Input extends MouseAdapter implements KeyListener {
    private final Game m;
    public Input(Game m) {
        this.m = m;
    }
    // what happens when you PRESS a key on your keyboard
    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_ESCAPE -> System.exit(0);
            case KeyEvent.VK_SPACE -> System.out.println("Hello world!");
        }
    }
    // what happens when you RELEASE a key on your keyboard
    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_SPACE -> System.out.println("Goodbye world!");
        }
    }
    // I don't know what this is, or why it exists, I'll never use it but if this wasn't in the code, it'll throw an error.
    @Override
    public void keyTyped(KeyEvent e) {}

    // mouse position (both, because mouseMoved stops firing while dragging)
    @Override
    public void mouseMoved(MouseEvent e) { pos(e); }
    @Override
    public void mouseDragged(MouseEvent e) { pos(e); }

    // left click
    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            pos(e);
            m.clicked = true;
        }
    }

    private void pos(MouseEvent e) {
        m.mouseX = e.getX();
        m.mouseY = e.getY();
    }
}
