package dev.dvorakmortonxd;

import java.awt.*;

public class Update {
    static void performPerTick(Game m) {
        if (m.clicked) {
            System.out.println(m.mouseX + " " + m.mouseY);
            m.clicked = false;
            if (m.scene.equals("main_menu")) {
                if (m.playButton.contains(m.mouseX, m.mouseY)) {
                    m.scene = "top_layer";
                }
            } else if (m.scene.equals("top_layer")) {
                // throw the boomerang (only if it's not already flying)
                if (!m.boomerangFlying) {
                    m.targetX = m.mouseX;
                    m.targetY = m.mouseY;
                    m.boomerangFlying = true;
                    m.boomerangReturning = false;
                    for (Enemy e : m.enemies) {
                        e.hitThisThrow = false;
                    }
                }
            } else if (m.scene.equals("paused")) {
                if (m.shopButton.contains(m.mouseX, m.mouseY)) {
                    m.scene = "shop";
                }
                if (m.quitButton.contains(m.mouseX, m.mouseY)) {
                    System.exit(0);
                }
            } else if (m.scene.equals("shop")) {
                Shop.click(m);
            }
        }
        Music.update(m);
        if (m.scene.equals("top_layer")) {
            moveBoomerang(m);
            spawnEnemies(m);
            moveEnemies(m);
            hitEnemies(m);
        }
        m.repaint();
    }

    static void moveBoomerang(Game m) {
        if (!m.boomerangFlying) return;

        m.boomerangAngle = (m.boomerangAngle + m.boomerangSpinSpeed) % 360;

        if (!m.boomerangReturning) {
            // flying to where you clicked, turn around when we get there
            if (moveBoomerangTowards(m, m.targetX, m.targetY)) {
                m.boomerangReturning = true;
            }
        } else {
            // coming back to the middle, stop when we get there
            if (moveBoomerangTowards(m, m.windowWidth / 2, m.windowHeight / 2)) {
                m.boomerangFlying = false;
            }
        }
    }

    // moves the boomerang one step towards (x, y), returns true when it got there
    static boolean moveBoomerangTowards(Game m, int x, int y) {
        double dx = x - m.boomerangX;
        double dy = y - m.boomerangY;
        double distance = Math.sqrt(dx * dx + dy * dy);

        // close enough, just snap onto the spot
        if (distance <= m.boomerangSpeed) {
            m.boomerangX = x;
            m.boomerangY = y;
            return true;
        }

        // dx / distance is how much of each step goes sideways (same for dy, up/down)
        m.boomerangX += dx / distance * m.boomerangSpeed;
        m.boomerangY += dy / distance * m.boomerangSpeed;
        return false;
    }

    static void spawnEnemies(Game m) {
        m.ticksUntilNextSpawn--;
        if (m.ticksUntilNextSpawn > 0) return;
        m.ticksUntilNextSpawn = m.ticksBetweenSpawns;

        if (m.enemies.size() >= m.maxEnemies) return;

        // pick a random edge, then a random spot along it
        int x = 0, y = 0;
        switch (m.random.nextInt(4)) {
            case 0 -> { x = m.random.nextInt(m.windowWidth); y = 0; }                 // top
            case 1 -> { x = m.random.nextInt(m.windowWidth); y = m.windowHeight; }    // bottom
            case 2 -> { x = 0;             y = m.random.nextInt(m.windowHeight); }    // left
            case 3 -> { x = m.windowWidth; y = m.random.nextInt(m.windowHeight); }    // right
        }

        Enemy e = new Enemy(x, y, m.enemyMaxHealth);
        pickNewWalkTo(m, e);
        m.enemies.add(e);
    }

    // gives the enemy a random spot to walk to that's on the map but not in the middle
    static void pickNewWalkTo(Game m, Enemy e) {
        int middleX = m.windowWidth / 2;
        int middleY = m.windowHeight / 2;
        int margin = Enemy.height; // so they don't walk half off the screen
        double distanceToMiddle;
        do {
            e.walkToX = margin + m.random.nextInt(m.windowWidth - 2 * margin);
            e.walkToY = margin + m.random.nextInt(m.windowHeight - 2 * margin);
            distanceToMiddle = Math.hypot(e.walkToX - middleX, e.walkToY - middleY);
        } while (distanceToMiddle < m.safeZoneRadius);
    }

    static void moveEnemies(Game m) {
        for (Enemy e : m.enemies) {
            double dx = e.walkToX - e.x;
            double dy = e.walkToY - e.y;
            double distance = Math.hypot(dx, dy);

            if (distance <= Enemy.speed) {
                pickNewWalkTo(m, e);
            } else {
                e.x += dx / distance * Enemy.speed;
                e.y += dy / distance * Enemy.speed;
            }
        }
    }

    static void hitEnemies(Game m) {
        if (!m.boomerangFlying) return;

        int half = m.boomerangSize / 2;
        Rectangle boomerang = new Rectangle((int) m.boomerangX - half, (int) m.boomerangY - half, m.boomerangSize, m.boomerangSize);

        for (Enemy e : m.enemies) {
            Rectangle body = new Rectangle((int) e.x - Enemy.width / 2, (int) e.y - Enemy.height / 2, Enemy.width, Enemy.height);
            if (!e.hitThisThrow && boomerang.intersects(body)) {
                e.health -= m.boomerangDamage;
                e.hitThisThrow = true;
                if (e.health <= 0) {
                    m.coins += m.coinsPerKill;
                    m.experience += m.experiencePerKill;
                }
            }
        }

        // remove every enemy that has no health left
        m.enemies.removeIf(e -> e.health <= 0);
    }

    static void paint(Game m, Graphics g) {
        Toolkit.getDefaultToolkit().sync();
        switch (m.scene) {
            case "main_menu" -> drawButton(m, g, m.playButton, "Play pls");
            case "top_layer" -> drawGame(m, g);
            case "paused" -> {
                // the game stays frozen behind the menu
                drawGame(m, g);
                darkenScreen(m, g);
                drawButton(m, g, m.shopButton, "Shop");
                drawButton(m, g, m.quitButton, "Quit");
            }
            case "shop" -> Shop.paint(m, g);
            case "win" -> drawWinScreen(m, g);
        }
    }

    static void drawWinScreen(Game m, Graphics g) {
        g.drawImage(m.winImage, 0, 0, null);

        // black text a bit lower right first, so the yellow text is readable on any picture
        int middle = m.windowWidth / 2;
        g.setFont(m.winFont);
        g.setColor(Color.BLACK);
        drawCenteredText(g, "YOU WIN", middle + 5, m.windowHeight / 2 + 5);
        g.setColor(Color.YELLOW);
        drawCenteredText(g, "YOU WIN", middle, m.windowHeight / 2);

        g.setFont(m.smallFont);
        g.setColor(Color.BLACK);
        drawCenteredText(g, "press ESC to quit", middle + 2, m.windowHeight - 38);
        g.setColor(Color.WHITE);
        drawCenteredText(g, "press ESC to quit", middle, m.windowHeight - 40);
    }

    static void drawGame(Game m, Graphics g) {
        // the map (it's already as big as the window, see Game.stretchToWindow)
        g.drawImage(m.background, 0, 0, null);

        // enemies, with a health bar under each one
        for (Enemy e : m.enemies) {
            int left = (int) e.x - Enemy.width / 2;
            int top = (int) e.y - Enemy.height / 2;
            g.drawImage(m.enemyImage, left, top, Enemy.width, Enemy.height, null);

            int barY = top + Enemy.height + 4;
            int barWidth = Enemy.width * e.health / e.maxHealth;
            g.setColor(Color.DARK_GRAY);
            g.fillRect(left, barY, Enemy.width, 5);
            g.setColor(Color.RED);
            g.fillRect(left, barY, barWidth, 5);
        }

        // the boomerang, turned around its center
        // (we turn a copy of g, otherwise everything drawn after would be turned too)
        Graphics2D turned = (Graphics2D) g.create();
        turned.rotate(Math.toRadians(m.boomerangAngle), m.boomerangX, m.boomerangY);
        int half = m.boomerangSize / 2;
        turned.drawImage(m.boomerangImage, (int) m.boomerangX - half, (int) m.boomerangY - half, m.boomerangSize, m.boomerangSize, null);
        turned.dispose();

        // coin counter in the top left corner
        g.setColor(Color.YELLOW);
        g.setFont(m.counterFont);
        g.drawString("Coins: " + m.coins, 20, 45);

        // experience counter in the bottom left corner
        g.setColor(Color.CYAN);
        g.drawString("XP: " + m.experience, 20, m.windowHeight - 20);
    }

    // a black see-through layer over the whole window
    static void darkenScreen(Game m, Graphics g) {
        g.setColor(new Color(0, 0, 0, 150)); // the last number is how see-through it is (0-255)
        g.fillRect(0, 0, m.windowWidth, m.windowHeight);
    }

    // a black button that turns gray when the mouse is on it, with the text in the middle
    static void drawButton(Game m, Graphics g, Rectangle b, String text) {
        g.setColor(Color.BLACK);
        if (b.contains(m.mouseX, m.mouseY)) {
            g.setColor(Color.GRAY);
        }
        g.fillRect(b.x, b.y, b.width, b.height);

        g.setColor(Color.WHITE);
        g.setFont(m.buttonFont);
        int textWidth = g.getFontMetrics().stringWidth(text);
        int textX = b.x + (b.width - textWidth) / 2;
        int textY = b.y + b.height / 2 + 18; // +18 because text is drawn from its bottom
        g.drawString(text, textX, textY);
    }

    // draws the text so its middle (left to right) is at centerX, y is where its bottom goes
    static void drawCenteredText(Graphics g, String text, int centerX, int y) {
        int textWidth = g.getFontMetrics().stringWidth(text);
        g.drawString(text, centerX - textWidth / 2, y);
    }
}
