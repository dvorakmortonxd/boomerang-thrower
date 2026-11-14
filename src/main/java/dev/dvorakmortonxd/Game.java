package dev.dvorakmortonxd;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;

public class Game extends JPanel implements ActionListener {

    // this defines the window dimensions
    public final int windowWidth = 800;
    public final int windowHeight = 800;

    // sets the fps to 60 (1000/60 = around 16)
    public Timer timer = new Timer(16, this);

    // defines the current scene
    public String scene = "main_menu";

    // mouse state
    public int mouseX, mouseY;
    public boolean clicked;

    // the map picture
    public BufferedImage topLayer;

    // the play button in the main menu (300x150, in the middle of the window)
    public Rectangle playButton = new Rectangle(250, 325, 300, 150);

    // the boomerang, x and y are its CENTER
    public BufferedImage boomerangImage;
    public final int boomerangSize = 64;
    public final double boomerangSpeed = 10;
    public double boomerangX = 400; // middle of the window
    public double boomerangY = 400;
    public boolean boomerangFlying = false;   // true while it's thrown
    public boolean boomerangReturning = false; // true when it's on the way back
    public int targetX, targetY;               // where you clicked
    public int boomerangAngle = 0;             // how much it's turned, in degrees
    public final int boomerangSpinSpeed = 20;  // degrees per tick

    // zombies
    public ArrayList<Enemy> enemies = new ArrayList<>();
    public final int maxEnemies = 10;
    public final int ticksBetweenSpawns = 120; // 120 ticks = about 2 seconds
    public int ticksUntilNextSpawn = ticksBetweenSpawns;
    public final int safeZoneRadius = 200; // zombies never walk closer to the middle than this

    public Random random = new Random();

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
        addMouseListener(in);
        addMouseMotionListener(in);
        setFocusable(true);
        // loads the pictures from src/main/resources
        try {
            topLayer = ImageIO.read(getClass().getResource("/toplayer.png"));
            boomerangImage = ImageIO.read(getClass().getResource("/bumerank.png"));
        } catch (Exception e) {
            System.out.println("couldn't load the pictures");
        }
        timer.start();
    }

    public static void main(String[] args) {
        // draw at 1:1 device pixels so the window is the size above on every machine
        // (Flatpak sandboxes report the desktop scale rounded up, which doubles the window)
        if (System.getProperty("sun.java2d.uiScale") == null) {
            System.setProperty("sun.java2d.uiScale", "1");
        }
        JFrame frame = new JFrame("Boomerang Thrower");
        frame.setResizable(false); // NOT resizable
        frame.add(new Game());
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // enables the X button in the title bar
        frame.setLocationRelativeTo(null); // centers the window
        frame.setVisible(true); // duh
    }
}
