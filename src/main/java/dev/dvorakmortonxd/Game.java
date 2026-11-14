package dev.dvorakmortonxd;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Game extends JPanel implements ActionListener {

    // this defines the window dimensions
    public final int windowWidth = 800;
    public final int windowHeight = 600;

    // sets the fps to 60 (1000/60 = around 16)
    public Timer timer = new Timer(16, this);

    // defines the current scene
    public String scene = "main_menu";

    // routes to Update.java (actions)
    @Override
    public void actionPerformed(ActionEvent e) {
        Update.performPerTick(this);
    }

    // routes to Update.java (rendering)
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Update.paint(this, g);
    }

    // sets up the window and everything
    public Game() {
        //sets up the window size
        setPreferredSize(new Dimension(windowWidth, windowHeight));
        Input in = new Input(this);
        addKeyListener(in);
        setBackground(Color.BLACK);
        setFocusable(true);
        timer.start();
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Boomerang Thrower");
        frame.setResizable(false); // NOT resizable
        frame.add(new Game());
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // enables the X button in the title bar
        frame.setLocationRelativeTo(null); // centers the window
        frame.setVisible(true); // duh
    }
}
