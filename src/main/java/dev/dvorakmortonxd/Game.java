package dev.dvorakmortonxd;

import javax.imageio.ImageIO;
import javax.sound.sampled.Clip;
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

    // fonts, made once here instead of every frame
    public final Font titleFont = new Font("Arial", Font.BOLD, 60);
    public final Font winFont = new Font("Arial", Font.BOLD, 120);
    public final Font buttonFont = new Font("Arial", Font.PLAIN, 54);
    public final Font counterFont = new Font("Arial", Font.BOLD, 32);
    public final Font cardNameFont = new Font("Arial", Font.BOLD, 30);
    public final Font priceFont = new Font("Arial", Font.PLAIN, 28);
    public final Font smallFont = new Font("Arial", Font.PLAIN, 22);

    // mouse state
    public int mouseX, mouseY;
    public boolean clicked;

    // the map pictures, background is the one that's drawn right now
    public BufferedImage topLayer, caveLayer, hellLayer, gymLayer, schoolLayer;
    public BufferedImage background;
    // the picture behind YOU WIN
    public BufferedImage winImage;

    // music, see Music.java for which one plays when
    public Clip menuMusic, levelMusic, tenseMusic, ratingMusic;
    public Clip playingMusic; // the one playing right now

    // the play button in the main menu (300x150, in the middle of the window)
    public Rectangle playButton = new Rectangle(windowWidth / 2 - 150, windowHeight / 2 - 75, 300, 150);

    // the buttons in the pause menu
    public Rectangle shopButton = new Rectangle(windowWidth / 2 - 150, windowHeight / 2 - 150, 300, 120);
    public Rectangle quitButton = new Rectangle(windowWidth / 2 - 150, windowHeight / 2 + 30, 300, 120);

    // the things you can buy in the shop, 2 by 2
    public Rectangle speedCard = new Rectangle(windowWidth / 2 - 350, 200, 340, 280);
    public Rectangle sizeCard = new Rectangle(windowWidth / 2 + 10, 200, 340, 280);
    public Rectangle strengthCard = new Rectangle(windowWidth / 2 - 350, 510, 340, 280);
    public Rectangle levelCard = new Rectangle(windowWidth / 2 + 10, 510, 340, 280);
    public BufferedImage speedIcon, sizeIcon, strengthIcon;

    // how many times you bought each upgrade (+1), the price depends on it
    public int speedLevel = 1;
    public int sizeLevel = 1;
    public int strengthLevel = 1;

    // level 1 is the grass with zombies, 2 is the cave with skeletons, 3 is hell with devils, 4 is the gym with gymbros, 5 is the school with teachers
    public int level = 1;
    // how much experience you need to go there (you don't lose it)
    public final int experienceForCave = 500;
    public final int experienceForHell = 1500;
    public final int experienceForGym = 5000;
    public final int experienceForSchool = 10000;
    public final int experienceToWin = 30000;

    // the boomerang, x and y are its CENTER
    public BufferedImage boomerangImage;
    public int boomerangSize = 64;
    public double boomerangSpeed = 10;
    public int boomerangDamage = 1;
    public double boomerangX = windowWidth / 2; // middle of the window
    public double boomerangY = windowHeight / 2;
    public boolean boomerangFlying = false;   // true while it's thrown
    public boolean boomerangReturning = false; // true when it's on the way back
    public int targetX, targetY;               // where you clicked
    public int boomerangAngle = 0;             // how much it's turned, in degrees
    public final int boomerangSpinSpeed = 20;  // degrees per tick

    // enemies (zombies, skeletons, devils, gymbros, teachers)
    public BufferedImage zombieImage, skeletonImage, devilImage, gymbroImage, teacherImage;
    public BufferedImage enemyImage;  // the one that's spawning right now
    public int enemyMaxHealth = 3;
    public ArrayList<Enemy> enemies = new ArrayList<>();
    public final int maxEnemies = 25;
    public final int ticksBetweenSpawns = 45; // 60 ticks = about 1 second
    public int ticksUntilNextSpawn = ticksBetweenSpawns;
    public final int safeZoneRadius = 200; // enemies never walk closer to the middle than this

    public Random random = new Random();

    // money
    public int coins = 0;
    public int coinsPerKill = 5; // changes with the level, see the goTo methods in Shop

    // experience
    public int experience = 0;
    public int experiencePerKill = 10; // changes with the level too

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
            caveLayer = ImageIO.read(getClass().getResource("/cavelayer.png"));
            hellLayer = ImageIO.read(getClass().getResource("/helllayer.png"));
            gymLayer = ImageIO.read(getClass().getResource("/gym.png"));
            schoolLayer = ImageIO.read(getClass().getResource("/school.png"));
            winImage = ImageIO.read(getClass().getResource("/479520-3056499407.jpg"));
            boomerangImage = ImageIO.read(getClass().getResource("/bumerank.png"));
            zombieImage = ImageIO.read(getClass().getResource("/zombie.png"));
            skeletonImage = ImageIO.read(getClass().getResource("/skeleton.png"));
            devilImage = ImageIO.read(getClass().getResource("/devil.png"));
            gymbroImage = ImageIO.read(getClass().getResource("/gymbro.png"));
            teacherImage = ImageIO.read(getClass().getResource("/teacher.png"));
            speedIcon = ImageIO.read(getClass().getResource("/speedupgrade.png"));
            sizeIcon = ImageIO.read(getClass().getResource("/sizeupgrade.png"));
            strengthIcon = ImageIO.read(getClass().getResource("/strengthupgrade.png"));
        } catch (Exception e) {
            System.out.println("couldn't load the pictures");
        }
        topLayer = stretchToWindow(topLayer);
        caveLayer = stretchToWindow(caveLayer);
        hellLayer = stretchToWindow(hellLayer);
        gymLayer = stretchToWindow(gymLayer);
        schoolLayer = stretchToWindow(schoolLayer);
        winImage = stretchToWindow(winImage);

        menuMusic = Music.load("/mainmenuloop.wav");
        levelMusic = Music.load("/levelloop.wav");
        tenseMusic = Music.load("/looptense.wav");
        ratingMusic = Music.load("/ratingloop.wav");

        // the game starts on the grass with zombies
        background = topLayer;
        enemyImage = zombieImage;
        timer.start();
    }

    // the maps are 256x256, this makes a window-sized copy once,
    // so they don't have to be stretched again every frame (that's slow)
    private BufferedImage stretchToWindow(BufferedImage small) {
        BufferedImage big = new BufferedImage(windowWidth, windowHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics g = big.getGraphics();
        g.drawImage(small, 0, 0, windowWidth, windowHeight, null);
        g.dispose();
        return big;
    }

    public static void main(String[] args) {
        // draw at 1:1 device pixels so the window is the size above on every machine
        // (Flatpak sandboxes report the desktop scale rounded up, which doubles the window)
        if (System.getProperty("sun.java2d.uiScale") == null) {
            System.setProperty("sun.java2d.uiScale", "1");
        }
        // Java's default way of drawing on Linux (XRender) is super slow at drawing text,
        // the shop has lots of text so it went down to ~10 fps, this turns it off
        System.setProperty("sun.java2d.xrender", "false");
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
