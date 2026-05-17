package ui;

import util.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * BarChart.java
 * رسم شريطي أفقي بسيط:
 *   [اسم الحالة]  ▓▓▓▓▓▓▓▓░░░░░░  (12)
 *
 * يستقبل خريطة: عنوان → قيمة، ومجموعة ألوان موازية.
 */
public class BarChart extends JPanel {

    private Map<String, Integer> data    = new LinkedHashMap<>();
    private Map<String, Color>   colors  = new LinkedHashMap<>();

    public BarChart() {
        setOpaque(false);
    }

    public void setData(Map<String, Integer> data, Map<String, Color> colors) {
        this.data   = data;
        this.colors = colors;
        repaint();
    }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (data.isEmpty()) return;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // نحسب أكبر قيمة لقياس الأشرطة
        int max = 1;
        for (int v : data.values()) if (v > max) max = v;

        // أبعاد كل صف
        int rowH      = 26;
        int gap       = 10;
        int labelW    = 90;
        int valueW    = 50;
        int barX      = labelW + 12;
        int barMaxW   = getWidth() - labelW - valueW - 24;

        int y = 4;
        FontMetrics fm = g2.getFontMetrics(Theme.FONT_SMALL);
        g2.setFont(Theme.FONT_SMALL);

        for (var e : data.entrySet()) {
            String label = e.getKey();
            int    val   = e.getValue();
            Color  color = colors.getOrDefault(label, Theme.ACCENT);

            // التسمية
            g2.setColor(Theme.TEXT_PRIMARY);
            g2.drawString(label, 4, y + rowH/2 + fm.getAscent()/2 - 3);

            // خلفية الشريط
            g2.setColor(Theme.BG_INPUT);
            g2.fill(new RoundRectangle2D.Float(barX, y + 6, barMaxW, rowH - 12, 8, 8));

            // الشريط الملون
            int barW = (int) (barMaxW * (val / (double) max));
            if (barW > 0) {
                g2.setColor(color);
                g2.fill(new RoundRectangle2D.Float(barX, y + 6, barW, rowH - 12, 8, 8));
            }

            // الرقم
            g2.setColor(Theme.TEXT_MUTED);
            String num = String.valueOf(val);
            g2.drawString(num, barX + barMaxW + 8, y + rowH/2 + fm.getAscent()/2 - 3);

            y += rowH + gap;
        }
        g2.dispose();
    }

    @Override public Dimension getPreferredSize() {
        int rows = data.size();
        return new Dimension(280, rows * 36 + 12);
    }
}
