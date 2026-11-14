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
            }
        }
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

        Enemy e = new Enemy(x, y);
        pickNewWalkTo(m, e);
        m.enemies.add(e);
    }

    // gives the zombie a random spot to walk to that's on the map but not in the middle
    static void pickNewWalkTo(Game m, Enemy e) {
        int middleX = m.windowWidth / 2;
        int middleY = m.windowHeight / 2;
        int margin = Enemy.size; // so they don't walk half off the screen
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
            Rectangle zombie = new Rectangle((int) e.x - Enemy.size / 2, (int) e.y - Enemy.size / 2, Enemy.size, Enemy.size);
            if (!e.hitThisThrow && boomerang.intersects(zombie)) {
                e.health--;
                e.hitThisThrow = true;
            }
        }

        // remove every zombie that has no health left
        m.enemies.removeIf(e -> e.health <= 0);
    }

    static void paint(Game m, Graphics g) {
        Toolkit.getDefaultToolkit().sync();
        if (m.scene.equals("main_menu")) {
            Rectangle b = m.playButton;
            g.setColor(Color.BLACK);
            if (b.contains(m.mouseX, m.mouseY)) {
                g.setColor(Color.GRAY);
            }
            g.fillRect(b.x, b.y, b.width, b.height);

            // put the text in the middle of the button
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.PLAIN, 54));
            String text = "Play pls";
            int textWidth = g.getFontMetrics().stringWidth(text);
            int textX = b.x + (b.width - textWidth) / 2;
            int textY = b.y + b.height / 2 + 18; // +18 because text is drawn from its bottom
            g.drawString(text, textX, textY);
        }
        if (m.scene.equals("top_layer")) {
            // draws the map stretched over the whole window
            g.drawImage(m.topLayer, 0, 0, m.windowWidth, m.windowHeight, null);

            // zombies, with a health bar under each one
            for (Enemy e : m.enemies) {
                int left = (int) e.x - Enemy.size / 2;
                int top = (int) e.y - Enemy.size / 2;
                g.setColor(new Color(0, 60, 0)); // very dark green
                g.fillRect(left, top, Enemy.size, Enemy.size);

                int barY = top + Enemy.size + 4;
                int barWidth = Enemy.size * e.health / Enemy.maxHealth;
                g.setColor(Color.DARK_GRAY);
                g.fillRect(left, barY, Enemy.size, 5);
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
        }
    }
}
