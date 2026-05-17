package ui;

import util.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Toast.java
 * إشعار صغير أنيق يظهر في الزاوية ويختفي تلقائياً.
 * بدائل أجمل من JOptionPane للرسائل القصيرة.
 */
public class Toast {

    public enum Type { SUCCESS, INFO, WARNING, ERROR }

    /** يعرض toast فوق الـ owner. */
    public static void show(Component owner, String message, Type type) {
        SwingUtilities.invokeLater(() -> {
            Window win = SwingUtilities.getWindowAncestor(owner);
            if (win == null) return;

            JWindow toast = new JWindow(win);
            toast.setBackground(new Color(0, 0, 0, 0));
            toast.setFocusable(false);

            ToastPanel panel = new ToastPanel(message, type);
            toast.setContentPane(panel);
            toast.pack();

            // الموضع: أعلى وسط النافذة الأم
            int x = win.getX() + (win.getWidth() - toast.getWidth()) / 2;
            int y = win.getY() + 80;
            toast.setLocation(x, y);
            toast.setOpacity(0f);
            toast.setVisible(true);

            // fade in
            Timer fadeIn = new Timer(20, null);
            fadeIn.addActionListener(e -> {
                float op = toast.getOpacity() + 0.1f;
                if (op >= 1f) { op = 1f; fadeIn.stop(); }
                toast.setOpacity(op);
            });
            fadeIn.start();

            // ينتظر 2.5 ثانية ثم fade out
            Timer hold = new Timer(2500, e -> {
                Timer fadeOut = new Timer(20, null);
                fadeOut.addActionListener(ev -> {
                    float op = toast.getOpacity() - 0.1f;
                    if (op <= 0f) {
                        fadeOut.stop();
                        toast.dispose();
                    } else {
                        toast.setOpacity(op);
                    }
                });
                fadeOut.start();
            });
            hold.setRepeats(false);
            hold.start();
        });
    }

    public static void success(Component owner, String msg) { show(owner, msg, Type.SUCCESS); }
    public static void info   (Component owner, String msg) { show(owner, msg, Type.INFO); }
    public static void warning(Component owner, String msg) { show(owner, msg, Type.WARNING); }
    public static void error  (Component owner, String msg) { show(owner, msg, Type.ERROR); }

    // ── لوحة الـ toast ────────────────────────────────────────────────────────

    private static class ToastPanel extends JPanel {
        private final String message;
        private final Color  accentColor;
        private final String icon;

        ToastPanel(String message, Type type) {
            this.message = message;
            switch (type) {
                case SUCCESS -> { accentColor = Theme.GREEN;  icon = "✓"; }
                case WARNING -> { accentColor = Theme.ACCENT; icon = "⚠"; }
                case ERROR   -> { accentColor = Theme.RED;    icon = "✕"; }
                default      -> { accentColor = Theme.BLUE;   icon = "ℹ"; }
            }
            setOpaque(false);
            int w = Math.max(280, getFontMetrics(Theme.FONT_BODY).stringWidth(message) + 80);
            setPreferredSize(new Dimension(w, 56));
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // الظل
            g2.setColor(new Color(0, 0, 0, 80));
            g2.fill(new RoundRectangle2D.Float(3, 4, getWidth()-6, getHeight()-6, 14, 14));

            // الخلفية
            g2.setColor(Theme.BG_PANEL);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth()-6, getHeight()-8, 12, 12));

            // الشريط الجانبي الملون
            g2.setColor(accentColor);
            g2.fill(new RoundRectangle2D.Float(0, 0, 4, getHeight()-8, 4, 4));

            // الأيقونة
            g2.setColor(accentColor);
            g2.setFont(Theme.FONT_HEAD);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(icon, 18, (getHeight()-8)/2 + fm.getAscent()/2 - 2);

            // النص
            g2.setColor(Theme.TEXT_PRIMARY);
            g2.setFont(Theme.FONT_BODY);
            FontMetrics fm2 = g2.getFontMetrics();
            g2.drawString(message, 46, (getHeight()-8)/2 + fm2.getAscent()/2 - 2);

            g2.dispose();
        }
    }
}
