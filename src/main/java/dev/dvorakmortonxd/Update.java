package dev.dvorakmortonxd;

import java.awt.*;

public class Update {
    static void performPerTick(Game m) {
        m.repaint();
    }
    static void paint(Game m, Graphics g) {
        Toolkit.getDefaultToolkit().sync();
    }
}
