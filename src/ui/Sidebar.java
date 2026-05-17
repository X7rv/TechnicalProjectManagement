package ui;

import util.Lang;
import util.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Sidebar.java
 * شريط التنقل الجانبي - محسّن بصرياً.
 */
public class Sidebar extends JPanel {

    public interface NavListener { void onNav(String page); }

    private final String[] PAGE_KEYS = {"nav_overview", "nav_orders", "nav_products", "nav_customers", "nav_settings"};
    private final String[] PAGE_IDS  = {"OVERVIEW",     "ORDERS",     "PRODUCTS",     "CUSTOMERS",     "SETTINGS"};

    private final NavListener listener;
    private String activePage = "OVERVIEW";
    private final JButton[] buttons = new JButton[PAGE_KEYS.length];

    public Sidebar(NavListener listener) {
        this.listener = listener;
        setPreferredSize(new Dimension(Theme.SIDEBAR_W, 0));
        setBackground(Theme.BG_PANEL);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createMatteBorder(0,
            Lang.isArabic() ? 1 : 0, 0,
            Lang.isArabic() ? 0 : 1,
            Theme.BORDER));
        build();
    }

    private void build() {
        removeAll();

        // ── اللوقو محسّن ────────────────────────────────────────────────
        JPanel logo = new JPanel(new BorderLayout());
        logo.setOpaque(false);
        logo.setMaximumSize(new Dimension(Theme.SIDEBAR_W, 80));
        logo.setBorder(BorderFactory.createEmptyBorder(20, 22, 14, 22));

        // أيقونة بطلة + اسم البرنامج
        JLabel iconBadge = new JLabel("◆") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.ACCENT);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.setColor(Theme.BG_DEEP);
                g2.setFont(new Font("Monospaced", Font.BOLD, 18));
                FontMetrics fm = g2.getFontMetrics();
                String t = "T";
                g2.drawString(t,
                    (getWidth() - fm.stringWidth(t)) / 2,
                    (getHeight() + fm.getAscent()) / 2 - 2);
                g2.dispose();
            }
        };
        iconBadge.setPreferredSize(new Dimension(38, 38));

        JLabel appName = new JLabel("TPM");
        appName.setFont(new Font("Monospaced", Font.BOLD, 22));
        appName.setForeground(Theme.TEXT_PRIMARY);

        JLabel tagline = new JLabel("ORDER  MGMT");
        tagline.setFont(new Font("Monospaced", Font.PLAIN, 9));
        tagline.setForeground(Theme.TEXT_MUTED);

        JPanel logoStack = new JPanel();
        logoStack.setOpaque(false);
        logoStack.setLayout(new BoxLayout(logoStack, BoxLayout.Y_AXIS));
        appName.setAlignmentX(Lang.isArabic() ? Component.RIGHT_ALIGNMENT : Component.LEFT_ALIGNMENT);
        tagline.setAlignmentX(Lang.isArabic() ? Component.RIGHT_ALIGNMENT : Component.LEFT_ALIGNMENT);
        logoStack.add(appName);
        logoStack.add(Box.createRigidArea(new Dimension(0, 2)));
        logoStack.add(tagline);

        JPanel iconWrap = new JPanel();
        iconWrap.setOpaque(false);
        iconWrap.setPreferredSize(new Dimension(46, 46));
        iconWrap.setLayout(null);
        iconBadge.setBounds(4, 4, 38, 38);
        iconWrap.add(iconBadge);

        logo.add(logoStack, Lang.isArabic() ? BorderLayout.EAST : BorderLayout.WEST);
        logo.add(iconWrap,  Lang.isArabic() ? BorderLayout.WEST : BorderLayout.EAST);
        add(logo);

        // فاصل أنيق
        JPanel sepWrap = new JPanel(new BorderLayout());
        sepWrap.setOpaque(false);
        sepWrap.setMaximumSize(new Dimension(Theme.SIDEBAR_W, 16));
        sepWrap.setBorder(BorderFactory.createEmptyBorder(4, 22, 8, 22));
        sepWrap.add(UiKit.separator(), BorderLayout.CENTER);
        add(sepWrap);

        // عنوان قسم
        JLabel sectionLbl = UiKit.muted(Lang.isArabic() ? "القوائم" : "MENU");
        sectionLbl.setFont(new Font("SansSerif", Font.BOLD, 9));
        sectionLbl.setBorder(BorderFactory.createEmptyBorder(2, 24, 6, 24));
        sectionLbl.setAlignmentX(Lang.isArabic() ? Component.RIGHT_ALIGNMENT : Component.LEFT_ALIGNMENT);
        JPanel sectionWrap = new JPanel(new BorderLayout());
        sectionWrap.setOpaque(false);
        sectionWrap.setMaximumSize(new Dimension(Theme.SIDEBAR_W, 22));
        sectionWrap.add(sectionLbl, Lang.isArabic() ? BorderLayout.EAST : BorderLayout.WEST);
        add(sectionWrap);

        // ── أزرار التنقل ──────────────────────────────────────────────
        for (int i = 0; i < PAGE_KEYS.length; i++) {
            final String pageId = PAGE_IDS[i];
            JButton btn = createNavBtn(Lang.t(PAGE_KEYS[i]), pageId.equals(activePage));
            btn.addActionListener(e -> navigate(pageId));
            buttons[i] = btn;
            add(btn);
            add(Box.createRigidArea(new Dimension(0, 4)));
        }

        add(Box.createVerticalGlue());

        // ── footer ───────────────────────────────────────────────────
        JPanel footer = new JPanel();
        footer.setOpaque(false);
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));
        footer.setBorder(BorderFactory.createEmptyBorder(8, 20, 18, 20));

        JLabel ver = UiKit.muted("v1.0  •  TPM Enterprise");
        ver.setHorizontalAlignment(SwingConstants.CENTER);
        ver.setAlignmentX(Component.CENTER_ALIGNMENT);
        footer.add(ver);
        add(footer);

        revalidate(); repaint();
    }

    private JButton createNavBtn(String text, boolean active) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getClientProperty("active") == Boolean.TRUE) {
                    // active: خلفية شفافة + شريط جانبي
                    g2.setColor(Theme.ACCENT_DIM);
                    g2.fill(new RoundRectangle2D.Float(10, 0, getWidth()-20, getHeight(), 10, 10));
                    g2.setColor(Theme.ACCENT);
                    int barX = Lang.isArabic() ? getWidth()-12 : 4;
                    g2.fillRoundRect(barX, 8, 4, getHeight()-16, 4, 4);
                } else if (getModel().isRollover()) {
                    g2.setColor(Theme.BG_HOVER);
                    g2.fill(new RoundRectangle2D.Float(10, 0, getWidth()-20, getHeight(), 10, 10));
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.putClientProperty("active", active);
        b.setFont(active ? Theme.FONT_BODY.deriveFont(Font.BOLD) : Theme.FONT_BODY);
        b.setForeground(active ? Theme.ACCENT : Theme.TEXT_MUTED);
        b.setHorizontalAlignment(Lang.isArabic() ? SwingConstants.RIGHT : SwingConstants.LEFT);
        b.setMaximumSize(new Dimension(Theme.SIDEBAR_W, 44));
        b.setPreferredSize(new Dimension(Theme.SIDEBAR_W, 44));
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setOpaque(false);
        b.setBorder(BorderFactory.createEmptyBorder(0, 22, 0, 22));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private void navigate(String pageId) {
        activePage = pageId;
        for (int i = 0; i < PAGE_IDS.length; i++) {
            boolean isActive = PAGE_IDS[i].equals(pageId);
            buttons[i].putClientProperty("active", isActive);
            buttons[i].setForeground(isActive ? Theme.ACCENT : Theme.TEXT_MUTED);
            buttons[i].setFont(isActive ? Theme.FONT_BODY.deriveFont(Font.BOLD) : Theme.FONT_BODY);
            buttons[i].repaint();
        }
        listener.onNav(pageId);
    }
}
