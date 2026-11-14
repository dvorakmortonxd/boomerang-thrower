package dev.dvorakmortonxd;

import java.awt.event.*;

public class Input implements KeyListener {
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
}
