package ui;

import data.Statistics;
import util.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.GeneralPath;
import java.util.List;

/**
 * LineChart.java
 * رسم خطي يعرض المبيعات اليومية.
 * يأخذ قائمة DayRevenue ويرسم خط ناعم مع تعبئة شفافة تحته.
 */
public class LineChart extends JPanel {

    private List<Statistics.DayRevenue> data;

    public LineChart() {
        setOpaque(false);
    }

    public void setData(List<Statistics.DayRevenue> data) {
        this.data = data;
        repaint();
    }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (data == null || data.isEmpty()) return;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int padTop    = 16;
        int padBottom = 28;
        int padLeft   = 50;
        int padRight  = 16;

        int chartW = getWidth()  - padLeft - padRight;
        int chartH = getHeight() - padTop  - padBottom;

        // نحسب أعلى قيمة
        double max = 0;
        for (var d : data) if (d.amount > max) max = d.amount;
        if (max == 0) max = 1;
        // نقرّب الـ max لأقرب رقم جميل
        double niceMax = niceCeil(max);

        // ── شبكة أفقية ──
        g2.setColor(Theme.BORDER);
        g2.setFont(Theme.FONT_SMALL);
        FontMetrics fm = g2.getFontMetrics();
        for (int i = 0; i <= 4; i++) {
            int y = padTop + (chartH * i) / 4;
            g2.setColor(new Color(255, 255, 255, 15));
            g2.drawLine(padLeft, y, padLeft + chartW, y);

            // تسمية المحور Y
            double value = niceMax * (1 - i / 4.0);
            String label = formatShort(value);
            g2.setColor(Theme.TEXT_MUTED);
            g2.drawString(label, 4, y + fm.getAscent()/2 - 2);
        }

        if (data.size() < 2) {
            g2.dispose();
            return;
        }

        // ── الخط ──
        GeneralPath line = new GeneralPath();
        GeneralPath fill = new GeneralPath();
        double stepX = chartW / (double)(data.size() - 1);

        for (int i = 0; i < data.size(); i++) {
            double x = padLeft + stepX * i;
            double y = padTop  + chartH * (1 - data.get(i).amount / niceMax);
            if (i == 0) {
                line.moveTo(x, y);
                fill.moveTo(x, padTop + chartH);
                fill.lineTo(x, y);
            } else {
                line.lineTo(x, y);
                fill.lineTo(x, y);
            }
        }
        fill.lineTo(padLeft + chartW, padTop + chartH);
        fill.closePath();

        // التعبئة الشفافة تحت الخط
        Color accent = Theme.ACCENT;
        g2.setPaint(new GradientPaint(
            0, padTop,
            new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 60),
            0, padTop + chartH,
            new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 5)
        ));
        g2.fill(fill);

        // الخط نفسه
        g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(accent);
        g2.draw(line);

        // نقاط عند كل يوم
        for (int i = 0; i < data.size(); i++) {
            double x = padLeft + stepX * i;
            double y = padTop  + chartH * (1 - data.get(i).amount / niceMax);
            g2.setColor(Theme.BG_CARD);
            g2.fillOval((int)x - 4, (int)y - 4, 8, 8);
            g2.setColor(accent);
            g2.fillOval((int)x - 2, (int)y - 2, 4, 4);
        }

        // ── تسميات المحور X (أول، وسط، آخر) ──
        g2.setColor(Theme.TEXT_MUTED);
        g2.setFont(Theme.FONT_SMALL);
        drawDateLabel(g2, data.get(0).date,                  padLeft,             padTop + chartH + 18);
        drawDateLabel(g2, data.get(data.size()/2).date,      padLeft + chartW/2,  padTop + chartH + 18);
        drawDateLabel(g2, data.get(data.size()-1).date,      padLeft + chartW,    padTop + chartH + 18);

        g2.dispose();
    }

    private void drawDateLabel(Graphics2D g2, String date, int cx, int y) {
        FontMetrics fm = g2.getFontMetrics();
        // نعرض MM-DD فقط للاختصار
        String shortDate = date.length() >= 10 ? date.substring(5) : date;
        int w = fm.stringWidth(shortDate);
        g2.drawString(shortDate, cx - w/2, y);
    }

    /** يقرّب الرقم لأعلى رقم "جميل" قريب. */
    private double niceCeil(double v) {
        if (v <= 0) return 1;
        double exp  = Math.floor(Math.log10(v));
        double base = Math.pow(10, exp);
        double n = v / base;
        if      (n <= 1)   n = 1;
        else if (n <= 2)   n = 2;
        else if (n <= 5)   n = 5;
        else               n = 10;
        return n * base;
    }

    private String formatShort(double v) {
        if (v >= 1_000_000) return String.format("%.1fM", v / 1_000_000);
        if (v >= 1_000)     return String.format("%.0fK", v / 1_000);
        return String.format("%.0f", v);
    }

    @Override public Dimension getPreferredSize() {
        return new Dimension(600, 220);
    }
}
