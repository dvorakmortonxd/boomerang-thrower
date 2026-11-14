package dev.dvorakmortonxd;

import java.awt.*;

public class Update {
    static void performPerTick(Game m) {
        if (m.clicked) {
            System.out.println(m.mouseX + " " + m.mouseY);
            m.clicked = false;
        }
        m.repaint();
    }
    static void paint(Game m, Graphics g) {
        Toolkit.getDefaultToolkit().sync();
        g.setColor(Color.BLACK);
        if (new Rectangle(300, 300, 300, 150).contains(m.mouseX, m.mouseY)) { g.setColor(Color.GRAY); }
        g.fillRect(300, 300, 300, 150);
    }
}
