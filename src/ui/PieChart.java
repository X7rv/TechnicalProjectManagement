package ui;

import util.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * PieChart.java
 * رسم دائري (donut) مع legend جانبية.
 * يستقبل: عنوان → قيمة، ويولّد الألوان تلقائياً.
 */
public class PieChart extends JPanel {

    private Map<String, Double> data = new LinkedHashMap<>();

    public PieChart() {
        setOpaque(false);
    }

    public void setData(Map<String, Double> data) {
        this.data = data;
        repaint();
    }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (data.isEmpty()) return;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        double total = 0;
        for (double v : data.values()) total += v;
        if (total <= 0) {
            g2.setColor(Theme.TEXT_MUTED);
            g2.setFont(Theme.FONT_SMALL);
            g2.drawString("—", getWidth()/2 - 5, getHeight()/2);
            g2.dispose();
            return;
        }

        // الدائرة على اليمين (للعربي)، Legend على اليسار
        int size    = Math.min(getHeight() - 20, 180);
        int circleX = getWidth() - size - 8;
        int circleY = (getHeight() - size) / 2;

        // الـ donut
        double angle = 90;  // نبدأ من فوق
        int i = 0;
        for (var e : data.entrySet()) {
            double pct   = e.getValue() / total;
            double sweep = pct * 360;
            Color  color = Theme.CHART_PALETTE[i % Theme.CHART_PALETTE.length];

            g2.setColor(color);
            g2.fill(new Arc2D.Double(circleX, circleY, size, size, angle, -sweep, Arc2D.PIE));
            angle -= sweep;
            i++;
        }
        // الحفرة في النص (لجعله donut)
        int hole = (int)(size * 0.55);
        g2.setColor(Theme.BG_CARD);
        g2.fillOval(
            circleX + (size - hole)/2,
            circleY + (size - hole)/2,
            hole, hole
        );

        // المجموع في النص
        g2.setColor(Theme.TEXT_PRIMARY);
        g2.setFont(Theme.FONT_HEAD);
        String totalStr = String.format("%,.0f", total);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(totalStr,
            circleX + size/2 - fm.stringWidth(totalStr)/2,
            circleY + size/2 + 2);

        g2.setColor(Theme.TEXT_MUTED);
        g2.setFont(Theme.FONT_SMALL);
        String unit = util.Money.currency();
        FontMetrics fmS = g2.getFontMetrics();
        g2.drawString(unit,
            circleX + size/2 - fmS.stringWidth(unit)/2,
            circleY + size/2 + 16);

        // ── Legend على اليسار ────────────────────────────
        int legendX = 4;
        int legendY = circleY + 6;
        int rowH    = 22;
        g2.setFont(Theme.FONT_SMALL);
        FontMetrics fmL = g2.getFontMetrics();
        i = 0;
        for (var e : data.entrySet()) {
            Color color = Theme.CHART_PALETTE[i % Theme.CHART_PALETTE.length];
            double pct  = e.getValue() / total * 100;

            // مربع اللون
            g2.setColor(color);
            g2.fillRoundRect(legendX, legendY + 4, 10, 10, 3, 3);

            // النص
            g2.setColor(Theme.TEXT_PRIMARY);
            String label = e.getKey() + "  " + String.format("%.0f%%", pct);
            g2.drawString(label, legendX + 16, legendY + 4 + fmL.getAscent() - 1);

            legendY += rowH;
            i++;
            if (i >= 6) break;   // نعرض حد أقصى 6 عناصر
        }
        g2.dispose();
    }

    @Override public Dimension getPreferredSize() {
        return new Dimension(420, 200);
    }
}
