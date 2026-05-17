package ui;

import data.OrderStore;
import data.ProductStore;
import data.Statistics;
import util.Lang;
import util.Settings;
import util.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * OverviewPanel.java
 * صفحة نظرة عامة موحّدة - تجمع KPIs + التحليلات + الرسومات:
 *   1. ترحيب باسم المالك
 *   2. 4 بطاقات KPI أساسية (مبيعات، طلبات، عملاء، مدفوع)
 *   3. تنبيهات ذكية
 *   4. بطاقات الأرقام التفصيلية (متوسط، أعلى، أقل، الكميات)
 *   5. أفضل عميل + أكثر منتج
 *   6. رسم شريطي + رسم دائري
 *   7. رسم خطي للمبيعات اليومية
 */
public class OverviewPanel extends JPanel {

    private final OrderStore   orderStore;
    private final ProductStore productStore;
    private final Statistics   stats;

    // KPIs أساسية
    private JLabel valRevenue, valOrders, valClients, valPaid;
    private JLabel subRevenue, subOrders, subClients, subPaid;

    // KPIs تحليلية
    private JLabel lblAvg, lblHigh, lblLow, lblUnits;
    private JLabel lblTopCust1, lblTopCust2;
    private JLabel lblTopProd1, lblTopProd2;

    // الرسومات
    private BarChart  barChart;
    private PieChart  pieChart;
    private LineChart lineChart;

    // التنبيهات
    private JPanel alertsContainer;

    public OverviewPanel(OrderStore orderStore, ProductStore productStore) {
        this.orderStore   = orderStore;
        this.productStore = productStore;
        this.stats        = new Statistics(orderStore);

        setBackground(Theme.BG_DEEP);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 24, 24, 24));

        orderStore.addChangeListener(this::refresh);
        productStore.addChangeListener(this::refresh);

        build();
    }

    private void build() {
        removeAll();

        // الترحيب فوق
        String welcomeText = Settings.ownerName.isEmpty()
            ? Lang.t("welcome") + "  👋"
            : String.format(Lang.t("welcome_named"), Settings.ownerName);
        JLabel welcome = UiKit.title(welcomeText);
        welcome.setHorizontalAlignment(Lang.isArabic() ? SwingConstants.RIGHT : SwingConstants.LEFT);
        welcome.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));
        add(welcome, BorderLayout.NORTH);

        // المحتوى داخل scroll
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        content.add(buildKpiRow());
        content.add(gap());
        content.add(buildAlertsContainer());
        content.add(buildExtendedKpis());
        content.add(gap());
        content.add(buildTopRow());
        content.add(gap());
        content.add(buildChartsRow());
        content.add(gap());
        content.add(buildLineChartCard());
        content.add(Box.createRigidArea(new Dimension(0, 20)));

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBackground(Theme.BG_DEEP);
        scroll.getViewport().setBackground(Theme.BG_DEEP);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        styleScrollBar(scroll);
        add(scroll, BorderLayout.CENTER);

        refresh();
        revalidate(); repaint();
    }

    private Component gap() { return Box.createRigidArea(new Dimension(0, 14)); }

    // ── الصف 1: KPIs أساسية ──────────────────────────────────────────────────

    private JPanel buildKpiRow() {
        JPanel stats = new JPanel(new GridLayout(1, 4, 14, 0));
        stats.setOpaque(false);
        stats.setAlignmentX(Component.LEFT_ALIGNMENT);
        stats.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        valRevenue = bigLabel("0 ر.س");
        valOrders  = bigLabel("0");
        valClients = bigLabel("0");
        valPaid    = bigLabel("0");

        subRevenue = subLabel("—");
        subOrders  = subLabel("—");
        subClients = subLabel("—");
        subPaid    = subLabel("—");

        stats.add(kpiCard("total_sales",   valRevenue, subRevenue, Theme.GREEN));
        stats.add(kpiCard("total_orders",  valOrders,  subOrders,  Theme.ACCENT));
        stats.add(kpiCard("total_clients", valClients, subClients, Theme.BLUE));
        stats.add(kpiCard("total_paid",    valPaid,    subPaid,    new Color(180,220,255)));
        return stats;
    }

    // ── الصف 2: التنبيهات ────────────────────────────────────────────────────

    private JPanel buildAlertsContainer() {
        alertsContainer = new JPanel();
        alertsContainer.setOpaque(false);
        alertsContainer.setLayout(new BoxLayout(alertsContainer, BoxLayout.Y_AXIS));
        alertsContainer.setAlignmentX(Component.LEFT_ALIGNMENT);
        return alertsContainer;
    }

    private void refreshAlerts() {
        alertsContainer.removeAll();

        int pending     = stats.countPending();
        long lowStock   = productStore.countLowStock();
        long outStock   = productStore.countOutOfStock();
        boolean anyAlert = pending > 0 || lowStock > 0 || outStock > 0;

        if (!anyAlert) {
            // ما نظهر شي إذا ما في تنبيهات (للحفاظ على مساحة)
            alertsContainer.revalidate();
            alertsContainer.repaint();
            return;
        }

        // فراغ قبل التنبيهات
        alertsContainer.add(Box.createRigidArea(new Dimension(0, 4)));

        if (pending > 0) {
            alertsContainer.add(makeAlert("⚠️",
                String.format(Lang.t("alert_pending_fmt"), pending), Theme.ACCENT));
            alertsContainer.add(Box.createRigidArea(new Dimension(0, 8)));
        }
        if (outStock > 0) {
            alertsContainer.add(makeAlert("🔴",
                String.format(Lang.t("alert_out_stock_fmt"), outStock), Theme.RED));
            alertsContainer.add(Box.createRigidArea(new Dimension(0, 8)));
        }
        if (lowStock > 0) {
            alertsContainer.add(makeAlert("🟡",
                String.format(Lang.t("alert_low_stock_fmt"), lowStock), Theme.ACCENT));
            alertsContainer.add(Box.createRigidArea(new Dimension(0, 8)));
        }

        alertsContainer.revalidate();
        alertsContainer.repaint();
    }

    private JPanel makeAlert(String icon, String text, Color color) {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // خلفية شفافة بنفس اللون
                g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 28));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                // إطار خفيف بنفس اللون
                g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 80));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth()-1, getHeight()-1, 12, 12));
                // شريط جانبي بارز
                int barX = Lang.isArabic() ? getWidth()-5 : 0;
                g2.setColor(color);
                g2.fill(new RoundRectangle2D.Float(barX, 4, 4, getHeight()-8, 4, 4));
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setLayout(new FlowLayout(FlowLayout.LEADING, 14, 10));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));

        JLabel iconLbl = new JLabel(icon);
        iconLbl.setFont(new Font("SansSerif", Font.PLAIN, 20));
        JLabel textLbl = UiKit.label(text, Theme.FONT_BODY, Theme.TEXT_PRIMARY);
        textLbl.setFont(Theme.FONT_BODY.deriveFont(Font.BOLD));

        p.add(iconLbl);
        p.add(textLbl);
        return p;
    }

    // ── الصف 3: KPIs تحليلية ─────────────────────────────────────────────────

    private JPanel buildExtendedKpis() {
        JPanel row = new JPanel(new GridLayout(1, 4, 14, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        lblAvg   = bigLabel("0 ر.س");
        lblHigh  = bigLabel("0 ر.س");
        lblLow   = bigLabel("0 ر.س");
        lblUnits = bigLabel("0");

        row.add(simpleKpi(Lang.t("avg_order"),     lblAvg,   Theme.ACCENT));
        row.add(simpleKpi(Lang.t("highest_order"), lblHigh,  Theme.GREEN));
        row.add(simpleKpi(Lang.t("lowest_order"),  lblLow,   Theme.BLUE));
        row.add(simpleKpi(Lang.t("units_sold"),    lblUnits, new Color(180,140,255)));

        return row;
    }

    private JPanel simpleKpi(String title, JLabel valLbl, Color color) {
        JPanel p = new JPanel(new BorderLayout(0, 6)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 25));
                g2.fill(new RoundRectangle2D.Float(2, 3, getWidth()-2, getHeight()-2, Theme.RADIUS, Theme.RADIUS));
                g2.setColor(Theme.BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth()-2, getHeight()-3, Theme.RADIUS, Theme.RADIUS));
                // شريط أعلى صغير ملوّن
                g2.setColor(color);
                g2.fillRoundRect(14, 14, 32, 3, 3, 3);
                g2.setColor(Theme.BORDER);
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth()-3, getHeight()-4, Theme.RADIUS, Theme.RADIUS));
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(22, 18, 16, 18));

        valLbl.setForeground(Theme.TEXT_PRIMARY);
        JLabel t = UiKit.label(title, Theme.FONT_SMALL, Theme.TEXT_MUTED);
        p.add(t, BorderLayout.NORTH);
        p.add(valLbl, BorderLayout.CENTER);
        return p;
    }

    // ── الصف 4: أفضل عميل + أكثر منتج ────────────────────────────────────────

    private JPanel buildTopRow() {
        JPanel row = new JPanel(new GridLayout(1, 2, 14, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        JPanel cust = makeCard(new BorderLayout(0, 4));
        cust.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));
        JLabel custTitle = UiKit.label("🏆  " + Lang.t("top_customer"),
            Theme.FONT_SMALL, Theme.TEXT_MUTED);
        lblTopCust1 = new JLabel("—");
        lblTopCust1.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblTopCust1.setForeground(Theme.TEXT_PRIMARY);
        lblTopCust2 = UiKit.label("—", Theme.FONT_SMALL, Theme.GREEN);

        JPanel custCenter = new JPanel();
        custCenter.setOpaque(false);
        custCenter.setLayout(new BoxLayout(custCenter, BoxLayout.Y_AXIS));
        lblTopCust1.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblTopCust2.setAlignmentX(Component.LEFT_ALIGNMENT);
        custCenter.add(lblTopCust1);
        custCenter.add(Box.createRigidArea(new Dimension(0, 4)));
        custCenter.add(lblTopCust2);

        cust.add(custTitle, BorderLayout.NORTH);
        cust.add(custCenter, BorderLayout.CENTER);

        JPanel prod = makeCard(new BorderLayout(0, 4));
        prod.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));
        JLabel prodTitle = UiKit.label("⭐  " + Lang.t("top_product"),
            Theme.FONT_SMALL, Theme.TEXT_MUTED);
        lblTopProd1 = new JLabel("—");
        lblTopProd1.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblTopProd1.setForeground(Theme.TEXT_PRIMARY);
        lblTopProd2 = UiKit.label("—", Theme.FONT_SMALL, Theme.ACCENT);

        JPanel prodCenter = new JPanel();
        prodCenter.setOpaque(false);
        prodCenter.setLayout(new BoxLayout(prodCenter, BoxLayout.Y_AXIS));
        lblTopProd1.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblTopProd2.setAlignmentX(Component.LEFT_ALIGNMENT);
        prodCenter.add(lblTopProd1);
        prodCenter.add(Box.createRigidArea(new Dimension(0, 4)));
        prodCenter.add(lblTopProd2);

        prod.add(prodTitle, BorderLayout.NORTH);
        prod.add(prodCenter, BorderLayout.CENTER);

        row.add(cust);
        row.add(prod);
        return row;
    }

    // ── الصف 5: الرسومات ─────────────────────────────────────────────────────

    private JPanel buildChartsRow() {
        JPanel row = new JPanel(new GridLayout(1, 2, 14, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 280));

        JPanel statusCard = makeCard(new BorderLayout(0, 12));
        statusCard.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));
        statusCard.add(UiKit.label("📊  " + Lang.t("chart_status"),
            Theme.FONT_HEAD, Theme.TEXT_PRIMARY), BorderLayout.NORTH);
        barChart = new BarChart();
        statusCard.add(barChart, BorderLayout.CENTER);

        JPanel typeCard = makeCard(new BorderLayout(0, 12));
        typeCard.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));
        typeCard.add(UiKit.label("🥧  " + Lang.t("chart_types"),
            Theme.FONT_HEAD, Theme.TEXT_PRIMARY), BorderLayout.NORTH);
        pieChart = new PieChart();
        typeCard.add(pieChart, BorderLayout.CENTER);

        row.add(statusCard);
        row.add(typeCard);
        return row;
    }

    // ── الصف 6: المبيعات اليومية ─────────────────────────────────────────────

    private JPanel buildLineChartCard() {
        JPanel card = makeCard(new BorderLayout(0, 12));
        card.setBorder(BorderFactory.createEmptyBorder(18, 22, 22, 22));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 280));
        card.add(UiKit.label("📈  " + Lang.t("chart_timeline"),
            Theme.FONT_HEAD, Theme.TEXT_PRIMARY), BorderLayout.NORTH);
        lineChart = new LineChart();
        card.add(lineChart, BorderLayout.CENTER);
        return card;
    }

    // ── التحديث ──────────────────────────────────────────────────────────────

    private void refresh() {
        SwingUtilities.invokeLater(() -> {
            if (valRevenue == null) return;

            // KPIs أساسية
            double rev   = orderStore.totalRevenue();
            int    ord   = orderStore.size();
            int    cust  = orderStore.uniqueCustomerCount();
            long   paid  = orderStore.countPaid();

            valRevenue.setText(String.format("%,.0f ر.س", rev));
            valOrders.setText(String.valueOf(ord));
            valClients.setText(String.valueOf(cust));
            valPaid.setText(String.valueOf(paid));

            subRevenue.setText(ord == 0 ? "لا توجد طلبات بعد"     : String.format("من أصل %d طلب", ord));
            subOrders.setText (ord == 0 ? "ابدأ بإضافة طلب"        : String.format("%d طلب نشط",    ord));
            subClients.setText(cust== 0 ? "لا يوجد عملاء بعد"      : String.format("%d عميل فريد",  cust));
            subPaid.setText   (ord == 0 ? "بانتظار أول دفعة"       : String.format("%d من %d مكتمل", paid, ord));

            // KPIs تحليلية
            lblAvg.setText(String.format("%,.0f ر.س",  stats.averageOrderValue()));
            lblHigh.setText(String.format("%,.0f ر.س", stats.highestOrder()));
            lblLow.setText(String.format("%,.0f ر.س",  stats.lowestOrder()));
            lblUnits.setText(String.valueOf(stats.totalUnitsSold()));

            // أفضل عميل
            String[] tc = stats.topCustomer();
            if (tc != null) { lblTopCust1.setText(tc[0]); lblTopCust2.setText(tc[1]); }
            else            { lblTopCust1.setText("—");   lblTopCust2.setText("—");   }

            // أكثر منتج
            String[] tp = stats.topProduct();
            if (tp != null) { lblTopProd1.setText(tp[0]); lblTopProd2.setText(tp[1]); }
            else            { lblTopProd1.setText("—");   lblTopProd2.setText("—");   }

            // الرسومات
            if (barChart != null) {
                Map<String, Color> statusColors = new LinkedHashMap<>();
                statusColors.put(util.OrderStatus.PAID,      Theme.GREEN);
                statusColors.put(util.OrderStatus.SHIPPED,   Theme.BLUE);
                statusColors.put(util.OrderStatus.PENDING,   Theme.ACCENT);
                statusColors.put(util.OrderStatus.CANCELLED, Theme.RED);

                // نحوّل المفاتيح للنصوص المعروضة
                Map<String, Integer> displayData = new LinkedHashMap<>();
                Map<String, Color>   displayColors = new LinkedHashMap<>();
                stats.statusDistribution().forEach((key, value) -> {
                    String displayed = util.OrderStatus.display(key);
                    displayData.put(displayed, value);
                    displayColors.put(displayed, statusColors.get(key));
                });
                barChart.setData(displayData, displayColors);
            }
            if (pieChart != null) {
                pieChart.setData(stats.typeDistribution());
            }
            if (lineChart != null) {
                lineChart.setData(stats.revenuePerDay(30));
            }

            refreshAlerts();
        });
    }

    // ── أدوات ─────────────────────────────────────────────────────────────────

    private JLabel bigLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 24));
        l.setForeground(Theme.TEXT_PRIMARY);
        return l;
    }

    private JLabel subLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.FONT_SMALL);
        l.setForeground(Theme.TEXT_MUTED);
        return l;
    }

    private JPanel kpiCard(String labelKey, JLabel valLbl, JLabel subLbl, Color accent) {
        // بطاقة بشريط جانبي ملوّن
        JPanel p = new JPanel(new BorderLayout(12, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // ظل ناعم
                g2.setColor(new Color(0, 0, 0, 25));
                g2.fill(new RoundRectangle2D.Float(2, 3, getWidth()-2, getHeight()-2, Theme.RADIUS, Theme.RADIUS));
                // الخلفية
                g2.setColor(Theme.BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth()-2, getHeight()-3, Theme.RADIUS, Theme.RADIUS));
                // الشريط الجانبي الملوّن (4px)
                int barX = Lang.isArabic() ? getWidth()-6 : 0;
                g2.setColor(accent);
                g2.fill(new RoundRectangle2D.Float(barX, 0, 4, getHeight()-3, 4, 4));
                // إطار
                g2.setColor(Theme.BORDER);
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth()-3, getHeight()-4, Theme.RADIUS, Theme.RADIUS));
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));

        JLabel titleLbl = UiKit.label(Lang.t(labelKey), Theme.FONT_SMALL, Theme.TEXT_MUTED);
        titleLbl.setFont(Theme.FONT_SMALL.deriveFont(Font.BOLD));

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        titleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        valLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        subLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        valLbl.setForeground(Theme.TEXT_PRIMARY);
        subLbl.setForeground(accent);
        subLbl.setFont(Theme.FONT_SMALL.deriveFont(Font.BOLD));

        center.add(titleLbl);
        center.add(Box.createRigidArea(new Dimension(0, 8)));
        center.add(valLbl);
        center.add(Box.createRigidArea(new Dimension(0, 4)));
        center.add(subLbl);

        p.add(center, BorderLayout.CENTER);
        return p;
    }

    private JPanel makeCard(LayoutManager layout) {
        JPanel p = new JPanel(layout) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), Theme.RADIUS, Theme.RADIUS));
                g2.setColor(Theme.BORDER);
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth()-1, getHeight()-1, Theme.RADIUS, Theme.RADIUS));
                g2.dispose();
            }
        };
        p.setOpaque(false);
        return p;
    }

    private void styleScrollBar(JScrollPane sp) {
        UiKit.styleScrollBar(sp);
    }
}
