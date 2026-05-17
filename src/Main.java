import data.OrderStore;
import data.ProductStore;
import ui.*;
import util.Lang;
import util.Settings;
import util.Theme;

import javax.swing.*;
import java.awt.*;

/**
 * Main.java
 * نقطة الدخول.
 *  - يحمّل الإعدادات أولاً
 *  - يطبّق الثيم
 *  - يفتح النافذة
 */
public class Main extends JFrame {

    private final ProductStore productStore = new ProductStore();
    private final OrderStore   store        = new OrderStore();
    private final CardLayout   cardLayout   = new CardLayout();
    private final JPanel       contentArea  = new JPanel(cardLayout);

    public Main() {
        // ربط المتجرين علشان المخزون يتحدث تلقائياً عند إضافة/حذف طلب
        store.setProductStore(productStore);
        setupFrame();
        buildUI();
    }

    private void setupFrame() {
        setTitle(Lang.t("app_title"));
        setSize(1300, 800);
        setMinimumSize(new Dimension(950, 600));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(Theme.BG_DEEP);
    }

    private void buildUI() {
        getContentPane().removeAll();

        OverviewPanel  overview  = new OverviewPanel(store, productStore);
        OrdersPanel    orders    = new OrdersPanel(store, productStore);
        ProductsPanel  products  = new ProductsPanel(productStore);
        CustomersPanel customers = new CustomersPanel(store);
        SettingsPanel  settings  = new SettingsPanel(store, this::rebuildEverything);

        contentArea.setBackground(Theme.BG_DEEP);
        contentArea.removeAll();
        contentArea.add(overview,  "OVERVIEW");
        contentArea.add(orders,    "ORDERS");
        contentArea.add(products,  "PRODUCTS");
        contentArea.add(customers, "CUSTOMERS");
        contentArea.add(settings,  "SETTINGS");

        Sidebar sidebar = new Sidebar(page -> cardLayout.show(contentArea, page));

        getContentPane().setLayout(new BorderLayout());
        if (Lang.isArabic()) {
            getContentPane().add(sidebar,     BorderLayout.EAST);
            getContentPane().add(contentArea, BorderLayout.CENTER);
        } else {
            getContentPane().add(sidebar,     BorderLayout.WEST);
            getContentPane().add(contentArea, BorderLayout.CENTER);
        }

        cardLayout.show(contentArea, "OVERVIEW");
        applyComponentOrientation(
            Lang.isArabic()
                ? ComponentOrientation.RIGHT_TO_LEFT
                : ComponentOrientation.LEFT_TO_RIGHT
        );

        revalidate(); repaint();
    }

    /** يُستدعى من SettingsPanel بعد أي تغيير لإعادة بناء كل الواجهة. */
    private void rebuildEverything() {
        Theme.apply();
        setTitle(Lang.t("app_title"));
        getContentPane().setBackground(Theme.BG_DEEP);
        buildUI();
    }

    public static void main(String[] args) {
        // تحميل الإعدادات قبل أي شي
        Settings.load();
        Theme.apply();

        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ex) { ex.printStackTrace(); }
        }

        UIManager.put("OptionPane.background",        Theme.BG_CARD);
        UIManager.put("Panel.background",             Theme.BG_CARD);
        UIManager.put("OptionPane.messageForeground", Theme.TEXT_PRIMARY);

        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }
}
