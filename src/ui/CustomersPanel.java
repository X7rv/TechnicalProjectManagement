package ui;

import data.CustomerStore;
import data.OrderStore;
import model.Customer;
import util.Lang;
import util.Theme;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * CustomersPanel.java
 * صفحة العملاء - تتولّد من الطلبات وتتحدّث تلقائياً.
 * الأعمدة: الاسم | الإيميل | عدد الطلبات | إجمالي المشتريات | الرتبة
 */
public class CustomersPanel extends JPanel {

    private final CustomerStore   customerStore;
    private final OrderStore      orderStore;
    private DefaultTableModel     tableModel;
    private JScrollPane           scrollPane;
    private EmptyState            emptyView;
    private JPanel                contentWrapper;

    public CustomersPanel(OrderStore orderStore) {
        this.orderStore    = orderStore;
        this.customerStore = new CustomerStore(orderStore);
        setBackground(Theme.BG_DEEP);
        setLayout(new BorderLayout());

        orderStore.addChangeListener(this::syncAndRefresh);
        build();
    }

    private void build() {
        removeAll();

        JLabel title = UiKit.title(Lang.t("nav_customers"));
        title.setBorder(BorderFactory.createEmptyBorder(20, 24, 14, 24));
        title.setHorizontalAlignment(Lang.isArabic() ? SwingConstants.RIGHT : SwingConstants.LEFT);
        add(title, BorderLayout.NORTH);

        String[] cols = {
            Lang.t("col_name"),
            Lang.t("col_email"),
            Lang.t("col_orders_count"),
            Lang.t("col_spent"),
            Lang.t("col_rank")
        };

        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        populateTable();

        JTable table = buildTable(tableModel);
        scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Theme.BG_CARD);
        styleScrollBar(scrollPane);

        emptyView = new EmptyState("👥",
            Lang.t("empty_customers_title"),
            Lang.t("empty_customers_hint"));

        contentWrapper = new JPanel(new BorderLayout());
        contentWrapper.setBackground(Theme.BG_DEEP);
        if (customerStore.size() == 0) {
            contentWrapper.add(emptyView, BorderLayout.CENTER);
        } else {
            contentWrapper.add(scrollPane, BorderLayout.CENTER);
        }
        add(contentWrapper, BorderLayout.CENTER);

        revalidate(); repaint();
    }

    private void updateView() {
        if (contentWrapper == null) return;
        if (customerStore.size() == 0) {
            if (scrollPane.getParent() != null) contentWrapper.remove(scrollPane);
            if (emptyView.getParent() == null) contentWrapper.add(emptyView, BorderLayout.CENTER);
        } else {
            if (emptyView.getParent() != null) contentWrapper.remove(emptyView);
            if (scrollPane.getParent() == null) contentWrapper.add(scrollPane, BorderLayout.CENTER);
        }
        contentWrapper.revalidate();
        contentWrapper.repaint();
    }

    private void populateTable() {
        tableModel.setRowCount(0);
        customerStore.syncFromOrders();
        for (Customer c : customerStore.getAll()) {
            tableModel.addRow(new Object[]{
                c.getName(),
                c.getEmail(),
                c.getOrderCount(),
                c.getTotalFormatted(),
                c.getRank()
            });
        }
    }

    private void syncAndRefresh() {
        if (tableModel != null) {
            SwingUtilities.invokeLater(() -> {
                populateTable();
                updateView();
            });
        }
    }

    private JTable buildTable(DefaultTableModel model) {
        JTable t = new JTable(model) {
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                c.setBackground(row % 2 == 0 ? Theme.BG_CARD : Theme.BG_PANEL);
                c.setForeground(Theme.TEXT_PRIMARY);
                if (isRowSelected(row)) c.setBackground(Theme.ACCENT_DIM);
                return c;
            }
        };
        t.setFont(Theme.FONT_BODY);
        t.setRowHeight(Theme.ROW_H);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 1));
        t.setBackground(Theme.BG_CARD);
        t.setForeground(Theme.TEXT_PRIMARY);
        t.setComponentOrientation(
            Lang.isArabic() ? ComponentOrientation.RIGHT_TO_LEFT : ComponentOrientation.LEFT_TO_RIGHT
        );

        JTableHeader header = t.getTableHeader();
        header.setBackground(Theme.BG_INPUT);
        header.setForeground(Theme.TEXT_MUTED);
        header.setFont(Theme.FONT_SMALL);
        header.setReorderingAllowed(false);

        DefaultTableCellRenderer cr = new DefaultTableCellRenderer();
        cr.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < t.getColumnCount(); i++)
            t.getColumnModel().getColumn(i).setCellRenderer(cr);

        // شارة الرتبة الملونة (آخر عمود)
        t.getColumnModel().getColumn(4).setCellRenderer(
            new BadgeRenderer(CustomersPanel::rankColor));

        return t;
    }

    /** ألوان الرتب - من Theme. */
    private static Color rankColor(String rank) {
        return switch (rank) {
            case "Platinum" -> Theme.RANK_PLATINUM;
            case "Gold"     -> Theme.RANK_GOLD;
            case "Silver"   -> Theme.RANK_SILVER;
            default          -> Theme.RANK_BRONZE;
        };
    }

    private void styleScrollBar(JScrollPane sp) {
        UiKit.styleScrollBar(sp);
    }
}
