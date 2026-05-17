package ui;

import util.Theme;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * UiKit.java
 * مكونات واجهة قابلة لإعادة الاستخدام - محسّنة بصرياً.
 */
public class UiKit {

    private UiKit() {}

    // ── النصوص ────────────────────────────────────────────────────────────────

    public static JLabel label(String text, Font font, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font);
        l.setForeground(color);
        l.setOpaque(false);
        return l;
    }

    public static JLabel title(String text) {
        return label(text, Theme.FONT_TITLE, Theme.TEXT_PRIMARY);
    }

    public static JLabel muted(String text) {
        return label(text, Theme.FONT_SMALL, Theme.TEXT_MUTED);
    }

    // ── الأزرار - محسّنة بـ shadow + press effect ──────────────────────────────

    /** زر أساسي ملوّن (Accent). يحتوي على ظل خفيف وحالة press. */
    public static JButton primaryBtn(String text) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                boolean pressed = getModel().isPressed();
                boolean hover   = getModel().isRollover();

                int yOffset = pressed ? 1 : 0;
                int h = getHeight() - (pressed ? 2 : 3);

                // ظل خفيف
                if (!pressed) {
                    g2.setColor(new Color(0, 0, 0, 60));
                    g2.fill(new RoundRectangle2D.Float(0, 2, getWidth(), getHeight()-2, 12, 12));
                }

                // الخلفية - gradient خفيف
                Color top = hover ? Theme.ACCENT.brighter() : Theme.ACCENT;
                Color bot = hover ? Theme.ACCENT          : darken(Theme.ACCENT, 0.85f);
                g2.setPaint(new GradientPaint(0, 0, top, 0, h, bot));
                g2.fill(new RoundRectangle2D.Float(0, yOffset, getWidth(), h, 12, 12));

                g2.dispose();
                super.paintComponent(g);
            }
        };
        styleBtn(b);
        b.setForeground(Theme.BG_DEEP);
        b.setFont(Theme.FONT_BODY.deriveFont(Font.BOLD));
        return b;
    }

    /** زر شفاف بإطار (Ghost). */
    public static JButton ghostBtn(String text) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                boolean pressed = getModel().isPressed();
                boolean hover   = getModel().isRollover();

                int yOffset = pressed ? 1 : 0;
                int h = getHeight() - (pressed ? 1 : 2);

                Color bg = pressed ? darken(Theme.BG_INPUT, 0.85f)
                          : hover  ? Theme.BG_HOVER
                                   : Theme.BG_INPUT;
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0, yOffset, getWidth(), h, 12, 12));

                // border ملوّن خفيف
                g2.setColor(hover ? Theme.ACCENT : Theme.BORDER_ACCENT);
                g2.setStroke(new BasicStroke(1.2f));
                g2.draw(new RoundRectangle2D.Float(0, yOffset, getWidth()-1, h-1, 12, 12));

                g2.dispose();
                super.paintComponent(g);
            }
        };
        styleBtn(b);
        b.setForeground(Theme.ACCENT);
        return b;
    }

    /** زر حذف (Red). */
    public static JButton dangerBtn(String text) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                boolean pressed = getModel().isPressed();
                boolean hover   = getModel().isRollover();

                int yOffset = pressed ? 1 : 0;
                int h = getHeight() - (pressed ? 2 : 3);

                if (!pressed) {
                    g2.setColor(new Color(0, 0, 0, 50));
                    g2.fill(new RoundRectangle2D.Float(0, 2, getWidth(), getHeight()-2, 12, 12));
                }

                Color top = hover ? new Color(255, 100, 100) : Theme.RED;
                Color bot = hover ? Theme.RED                : darken(Theme.RED, 0.80f);
                g2.setPaint(new GradientPaint(0, 0, top, 0, h, bot));
                g2.fill(new RoundRectangle2D.Float(0, yOffset, getWidth(), h, 12, 12));

                g2.dispose();
                super.paintComponent(g);
            }
        };
        styleBtn(b);
        b.setForeground(Color.WHITE);
        b.setFont(Theme.FONT_BODY.deriveFont(Font.BOLD));
        return b;
    }

    private static void styleBtn(JButton b) {
        b.setFont(Theme.FONT_BODY);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setOpaque(false);
        b.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    /** يُغمق لون بنسبة (0-1). */
    private static Color darken(Color c, float factor) {
        return new Color(
            Math.max(0, (int)(c.getRed()   * factor)),
            Math.max(0, (int)(c.getGreen() * factor)),
            Math.max(0, (int)(c.getBlue()  * factor))
        );
    }

    // ── الحقول - محسّنة بحدود مستديرة + focus ring ───────────────────────────

    public static JTextField textField() {
        JTextField tf = new JTextField(20) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.dispose();
                super.paintComponent(g);
            }
            @Override protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hasFocus() ? Theme.ACCENT : Theme.BORDER_ACCENT);
                g2.setStroke(new BasicStroke(hasFocus() ? 1.5f : 1f));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth()-1, getHeight()-1, 10, 10));
                g2.dispose();
            }
        };
        tf.setFont(Theme.FONT_BODY);
        tf.setBackground(Theme.BG_INPUT);
        tf.setForeground(Theme.TEXT_PRIMARY);
        tf.setCaretColor(Theme.ACCENT);
        tf.setOpaque(false);
        tf.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        return tf;
    }

    /** قائمة منسدلة محسّنة بصرياً. */
    public static JComboBox<String> darkCombo(String... items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setFont(Theme.FONT_BODY);
        cb.setBackground(Theme.BG_INPUT);
        cb.setForeground(Theme.TEXT_PRIMARY);
        cb.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Theme.BORDER_ACCENT, 1, true),
            BorderFactory.createEmptyBorder(2, 6, 2, 6)
        ));

        cb.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);
                lbl.setFont(Theme.FONT_BODY);
                lbl.setBackground(isSelected ? Theme.ACCENT_DIM : Theme.BG_INPUT);
                lbl.setForeground(isSelected ? Theme.ACCENT : Theme.TEXT_PRIMARY);
                lbl.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
                list.setBackground(Theme.BG_INPUT);
                list.setSelectionBackground(Theme.ACCENT_DIM);
                return lbl;
            }
        });
        return cb;
    }

    // ── البطاقات - محسّنة بظل خفيف ──────────────────────────────────────────

    public static JPanel card(LayoutManager layout) {
        JPanel p = new JPanel(layout) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // ظل ناعم تحت البطاقة
                g2.setColor(new Color(0, 0, 0, 30));
                g2.fill(new RoundRectangle2D.Float(2, 3, getWidth()-2, getHeight()-2, Theme.RADIUS, Theme.RADIUS));

                // الخلفية
                g2.setColor(Theme.BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth()-2, getHeight()-3, Theme.RADIUS, Theme.RADIUS));

                // إطار خفيف
                g2.setColor(Theme.BORDER);
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth()-3, getHeight()-4, Theme.RADIUS, Theme.RADIUS));
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(Theme.PAD, Theme.PAD, Theme.PAD, Theme.PAD));
        return p;
    }

    public static JSeparator separator() {
        JSeparator sep = new JSeparator();
        sep.setForeground(Theme.BORDER);
        sep.setBackground(Theme.BORDER);
        return sep;
    }

    // ── ScrollBar / Table styling ────────────────────────────────────────────

    /** Scroll-bar داكن وأنيق. */
    public static void styleScrollBar(JScrollPane sp) {
        sp.getVerticalScrollBar().setPreferredSize(new Dimension(10, 0));
        sp.getVerticalScrollBar().setUnitIncrement(16);
        sp.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                thumbColor = Theme.BG_HOVER;
                trackColor = Theme.BG_CARD;
            }
            @Override protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.BG_HOVER);
                g2.fillRoundRect(r.x + 2, r.y + 2, r.width - 4, r.height - 4, 6, 6);
                g2.dispose();
            }
            @Override protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
                // track شفاف
            }
            @Override protected JButton createIncreaseButton(int o) { return zeroBtn(); }
            @Override protected JButton createDecreaseButton(int o) { return zeroBtn(); }
            private JButton zeroBtn() {
                JButton b = new JButton(); b.setPreferredSize(new Dimension(0, 0)); return b;
            }
        });
    }

    /** يطبّق التنسيق القياسي على JTable. */
    public static void styleTable(JTable t) {
        t.setFont(Theme.FONT_BODY);
        t.setRowHeight(Theme.ROW_H);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 1));
        t.setBackground(Theme.BG_CARD);
        t.setForeground(Theme.TEXT_PRIMARY);
        t.setSelectionBackground(Theme.ACCENT_DIM);
        t.setSelectionForeground(Theme.TEXT_PRIMARY);
        t.setComponentOrientation(
            util.Lang.isArabic() ? ComponentOrientation.RIGHT_TO_LEFT
                                  : ComponentOrientation.LEFT_TO_RIGHT
        );

        javax.swing.table.JTableHeader header = t.getTableHeader();
        header.setBackground(Theme.BG_INPUT);
        header.setForeground(Theme.TEXT_MUTED);
        header.setFont(Theme.FONT_SMALL.deriveFont(Font.BOLD));
        header.setReorderingAllowed(false);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, Theme.BORDER_ACCENT));
        header.setPreferredSize(new Dimension(0, Theme.ROW_H));

        javax.swing.table.DefaultTableCellRenderer center =
            new javax.swing.table.DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < t.getColumnCount(); i++) {
            t.getColumnModel().getColumn(i).setCellRenderer(center);
        }
    }
}
