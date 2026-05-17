package ui;

import util.Theme;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.function.Function;

/**
 * BadgeRenderer.java
 * Cell renderer يرسم شارة دائرية ملونة حول النص.
 * يستقبل دالة لتحديد اللون من قيمة الخلية.
 *
 * يحل محل 3 نسخ متطابقة كانت في:
 *  - OrdersPanel (StatusBadgeRenderer)
 *  - CustomersPanel (RankBadgeRenderer)
 *  - ProductsPanel (StockBadgeRenderer)
 */
public class BadgeRenderer extends JLabel implements TableCellRenderer {

    private final Function<String, Color> colorFn;

    public BadgeRenderer(Function<String, Color> colorFn) {
        this.colorFn = colorFn;
        setOpaque(false);
        setHorizontalAlignment(SwingConstants.CENTER);
    }

    @Override
    public Component getTableCellRendererComponent(
            JTable t, Object value, boolean sel, boolean focus, int row, int col) {
        String txt = value == null ? "" : value.toString();
        setText(txt);
        setFont(Theme.FONT_SMALL.deriveFont(Font.BOLD));
        putClientProperty("sc", colorFn.apply(txt));
        return this;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        Color sc = (Color) getClientProperty("sc");
        if (sc == null) sc = Theme.TEXT_MUTED;

        int pw = getFontMetrics(getFont()).stringWidth(getText()) + 24;
        int ph = 22;
        int px = (getWidth() - pw) / 2;
        int py = (getHeight() - ph) / 2;

        // الخلفية الشفافة بنفس اللون
        g2.setColor(new Color(sc.getRed(), sc.getGreen(), sc.getBlue(), 35));
        g2.fill(new RoundRectangle2D.Float(px, py, pw, ph, ph, ph));

        // النص
        g2.setColor(sc);
        g2.setFont(getFont());
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(getText(),
            (getWidth() - fm.stringWidth(getText())) / 2,
            py + ph / 2 + fm.getAscent() / 2 - 1);

        g2.dispose();
    }
}
