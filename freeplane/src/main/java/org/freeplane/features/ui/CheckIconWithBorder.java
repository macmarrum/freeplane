package org.freeplane.features.ui;

import java.awt.BasicStroke;
import java.awt.Component;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.Icon;
import javax.swing.JMenuItem;

import com.bulenkov.iconloader.util.GraphicsConfig;
import com.bulenkov.iconloader.util.Gray;
import com.bulenkov.iconloader.util.UIUtil;

class CheckIconWithBorder implements Icon {
    
    static CheckIconWithBorder INSTANCE = new CheckIconWithBorder();

    @Override
    public void paintIcon(Component c, Graphics g2, int x, int y) {
        if(! (c instanceof JMenuItem))
            return;
        Graphics2D g = (Graphics2D) g2;
        final GraphicsConfig config = new GraphicsConfig(g);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_DEFAULT);

        g.translate(x+2, y+2);

        final int sz = 13;


        if (((JMenuItem)c).isSelected()) {
          g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
          g.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
          g.drawLine(4, 7, 7, 10);
          g.drawLine(7, 10, sz, 2);
          g.drawLine(4, 5, 7, 8);
          g.drawLine(7, 8, sz, 0);
        }
        else {
            g.drawRoundRect(0, 0, sz, sz - 1, 4, 4);
            g.drawRoundRect(0, 0, sz, sz - 1, 4, 4);
        }

        g.translate(-x-2, -y-2);
        config.restore();
    }

    @Override
    public int getIconWidth() {
        return 13;
    }

    @Override
    public int getIconHeight() {
        // TODO Auto-generated method stub
        return 13;
    }
}
