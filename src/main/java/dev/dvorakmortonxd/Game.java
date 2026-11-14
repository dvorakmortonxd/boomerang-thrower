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
    public final int windowWidth = 900;
    public final int windowHeight = 900;

    // fullscreen (press F)
    public JFrame frame;
    public boolean fullscreen = false;
    // how much the game is stretched, and where it starts on the screen (the black borders are around it)
    public double scale = 1;
    public int offsetX, offsetY;

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
    public Rectangle playButton = new Rectangle(windowWidth / 2 - 150, windowHeight / 2 - 75, 300, 150);

    // the buttons in the pause menu
    public Rectangle shopButton = new Rectangle(windowWidth / 2 - 150, windowHeight / 2 - 150, 300, 120);
    public Rectangle quitButton = new Rectangle(windowWidth / 2 - 150, windowHeight / 2 + 30, 300, 120);

    // the boomerang, x and y are its CENTER
    public BufferedImage boomerangImage;
    public BufferedImage zombieImage;
    public final int boomerangSize = 64;
    public final double boomerangSpeed = 10;
    public double boomerangX = windowWidth / 2; // middle of the window
    public double boomerangY = windowHeight / 2;
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

    // money
    public int coins = 0;
    public final int coinsPerKill = 5;

    // experience
    public int experience = 0;
    public final int experiencePerKill = 10;

    // routes to Update.java (actions)
    @Override
    public void actionPerformed(ActionEvent e) {
        Update.performPerTick(this);
    }

    // routes to Update.java (rendering)
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        // stretch the game as big as it fits, and put it in the middle
        scale = Math.min((double) getWidth() / windowWidth, (double) getHeight() / windowHeight);
        offsetX = (int) ((getWidth() - windowWidth * scale) / 2);
        offsetY = (int) ((getHeight() - windowHeight * scale) / 2);

        // black borders everywhere, then the normal background where the game goes
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(getBackground());
        g.fillRect(offsetX, offsetY, (int) (windowWidth * scale), (int) (windowHeight * scale));

        // the rest of the game draws like the window is still windowWidth x windowHeight,
        // this copy of g moves and stretches everything for it
        Graphics2D stretched = (Graphics2D) g.create();
        stretched.translate(offsetX, offsetY);
        stretched.scale(scale, scale);
        stretched.clipRect(0, 0, windowWidth, windowHeight);
        Update.paint(this, stretched);
        stretched.dispose();
    }

    public void toggleFullscreen() {
        fullscreen = !fullscreen;
        GraphicsDevice screen = frame.getGraphicsConfiguration().getDevice();

        // the window has to be closed for a moment to take away (or give back) its title bar
        screen.setFullScreenWindow(null);
        frame.dispose();
        frame.setUndecorated(fullscreen);
        frame.setVisible(true);

        if (fullscreen) {
            screen.setFullScreenWindow(frame);
        } else {
            frame.pack();
            frame.setLocationRelativeTo(null);
        }
        requestFocusInWindow(); // so the keyboard still works after the switch
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
            zombieImage = ImageIO.read(getClass().getResource("/zombie.png"));
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
        Game game = new Game();
        game.frame = frame;
        frame.add(game);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // enables the X button in the title bar
        frame.setLocationRelativeTo(null); // centers the window
        frame.setVisible(true); // duh
    }
}
