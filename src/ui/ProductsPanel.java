package ui;

import data.ProductStore;
import model.Product;
import util.Lang;
import util.Theme;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * ProductsPanel.java
 * صفحة المنتجات والمخزون.
 *   - جدول كل المنتجات (اسم، نوع، سعر، مخزون، حالة المخزون)
 *   - إضافة / تعديل / حذف
 *   - شارة ملونة لحالة المخزون: متوفر / منخفض / نافد
 */
public class ProductsPanel extends JPanel {

    private final ProductStore store;
    private final ProductTableModel model;
    private final JTable table;
    private JScrollPane scrollPane;
    private EmptyState  emptyView;
    private JPanel      contentWrapper;

    public ProductsPanel(ProductStore store) {
        this.store = store;
        this.model = new ProductTableModel(store);
        this.table = buildTable();
        setLayout(new BorderLayout());
        setBackground(Theme.BG_DEEP);

        store.addChangeListener(() -> SwingUtilities.invokeLater(this::refresh));

        build();
    }

    private void build() {
        removeAll();
        add(buildHeader(), BorderLayout.NORTH);

        contentWrapper = new JPanel(new BorderLayout());
        contentWrapper.setBackground(Theme.BG_DEEP);

        scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setBackground(Theme.BG_CARD);
        scrollPane.getViewport().setBackground(Theme.BG_CARD);
        styleScrollBar(scrollPane);

        emptyView = new EmptyState("🛒",
            Lang.t("empty_products_title"),
            Lang.t("empty_products_hint"));

        contentWrapper.add(scrollPane, BorderLayout.CENTER);
        add(contentWrapper, BorderLayout.CENTER);

        refresh();
        revalidate(); repaint();
    }

    private JPanel buildHeader() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Theme.BG_DEEP);
        bar.setBorder(BorderFactory.createEmptyBorder(20, 24, 14, 24));

        JLabel title = UiKit.title(Lang.t("nav_products"));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);

        JButton btnAdd    = UiKit.primaryBtn(Lang.t("add_product"));
        JButton btnEdit   = UiKit.ghostBtn(Lang.t("edit_order"));
        JButton btnDelete = UiKit.dangerBtn(Lang.t("btn_delete"));

        btnAdd.addActionListener(e    -> onAdd());
        btnEdit.addActionListener(e   -> onEdit());
        btnDelete.addActionListener(e -> onDelete());

        actions.add(btnAdd);
        actions.add(btnEdit);
        actions.add(btnDelete);

        if (Lang.isArabic()) {
            bar.add(actions, BorderLayout.WEST);
            bar.add(title,   BorderLayout.EAST);
        } else {
            bar.add(title,   BorderLayout.WEST);
            bar.add(actions, BorderLayout.EAST);
        }
        return bar;
    }

    // ── الجدول ────────────────────────────────────────────────────────────────

    private JTable buildTable() {
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
        t.setSelectionBackground(Theme.ACCENT_DIM);
        t.setSelectionForeground(Theme.TEXT_PRIMARY);
        t.setComponentOrientation(
            Lang.isArabic() ? ComponentOrientation.RIGHT_TO_LEFT : ComponentOrientation.LEFT_TO_RIGHT
        );
        t.setAutoCreateRowSorter(true);

        JTableHeader header = t.getTableHeader();
        header.setBackground(Theme.BG_INPUT);
        header.setForeground(Theme.TEXT_MUTED);
        header.setFont(Theme.FONT_SMALL);
        header.setReorderingAllowed(false);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER_ACCENT));

        DefaultTableCellRenderer centerR = new DefaultTableCellRenderer();
        centerR.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < model.getColumnCount(); i++)
            t.getColumnModel().getColumn(i).setCellRenderer(centerR);

        t.getColumnModel().getColumn(ProductTableModel.COL_STOCK_STATUS)
            .setCellRenderer(new BadgeRenderer(ProductsPanel::stockColor));

        t.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) onEdit();
            }
        });

        int[] widths = {80, 180, 130, 110, 80, 110};
        for (int i = 0; i < widths.length; i++)
            t.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        return t;
    }

    private void refresh() {
        model.refresh();
        updateView();
    }

    private void updateView() {
        if (contentWrapper == null) return;
        if (store.size() == 0) {
            if (scrollPane.getParent() != null) contentWrapper.remove(scrollPane);
            if (emptyView.getParent() == null) contentWrapper.add(emptyView, BorderLayout.CENTER);
        } else {
            if (emptyView.getParent() != null) contentWrapper.remove(emptyView);
            if (scrollPane.getParent() == null) contentWrapper.add(scrollPane, BorderLayout.CENTER);
        }
        contentWrapper.revalidate();
        contentWrapper.repaint();
    }

    // ── العمليات ─────────────────────────────────────────────────────────────

    private void onAdd() {
        Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
        Product p = ProductDialog.showAdd(owner);
        if (p == null) return;
        if (!store.add(p)) {
            Toast.error(this, store.getLastError());
            return;
        }
        Toast.success(this, Lang.t("toast_product_added"));
    }

    private void onEdit() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            Toast.warning(this, Lang.t("err_select"));
            return;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        Product p = store.get(modelRow);
        Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
        if (!ProductDialog.showEdit(owner, p)) return;
        if (!store.update(p)) {
            Toast.error(this, store.getLastError());
            return;
        }
        Toast.success(this, Lang.t("toast_product_updated"));
    }

    private void onDelete() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            Toast.warning(this, Lang.t("err_select"));
            return;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        Product p = store.get(modelRow);
        int confirm = JOptionPane.showConfirmDialog(this,
            Lang.t("confirm_delete_product") + "\n" + p.getName(),
            Lang.t("btn_delete"), JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        if (!store.remove(modelRow)) {
            Toast.error(this, store.getLastError());
            return;
        }
        Toast.success(this, Lang.t("toast_product_deleted"));
    }

    private void styleScrollBar(JScrollPane sp) {
        UiKit.styleScrollBar(sp);
    }

    /** ألوان حالة المخزون. يقبل النص المعروض (بأي لغة). */
    static Color stockColor(String text) {
        if (text == null) return Theme.TEXT_MUTED;
        if (text.equals(Lang.t("stock_out"))) return Theme.RED;
        if (text.equals(Lang.t("stock_low"))) return Theme.ACCENT;
        return Theme.GREEN;
    }

    // ── نموذج جدول المنتجات ──────────────────────────────────────────────────

    private static class ProductTableModel extends AbstractTableModel {
        static final int COL_ID           = 0;
        static final int COL_NAME         = 1;
        static final int COL_TYPE         = 2;
        static final int COL_PRICE        = 3;
        static final int COL_STOCK        = 4;
        static final int COL_STOCK_STATUS = 5;

        private final ProductStore store;

        ProductTableModel(ProductStore store) { this.store = store; }

        @Override public int getRowCount()    { return store.size(); }
        @Override public int getColumnCount() { return 6; }
        @Override public boolean isCellEditable(int r, int c) { return false; }

        @Override public String getColumnName(int col) {
            return switch (col) {
                case COL_ID           -> Lang.t("col_id");
                case COL_NAME         -> Lang.t("col_product");
                case COL_TYPE         -> Lang.t("col_type");
                case COL_PRICE        -> Lang.t("col_price");
                case COL_STOCK        -> Lang.t("col_stock");
                case COL_STOCK_STATUS -> Lang.t("col_status");
                default -> "";
            };
        }

        @Override public Object getValueAt(int row, int col) {
            Product p = store.get(row);
            return switch (col) {
                case COL_ID           -> p.getId();
                case COL_NAME         -> p.getName();
                case COL_TYPE         -> p.getType();
                case COL_PRICE        -> p.getPriceFormatted();
                case COL_STOCK        -> p.getStock();
                case COL_STOCK_STATUS -> stockLabel(p);
                default -> "";
            };
        }

        @Override public Class<?> getColumnClass(int col) {
            return col == COL_STOCK ? Integer.class : String.class;
        }

        private static String stockLabel(Product p) {
            if (p.isOutOfStock()) return Lang.t("stock_out");
            if (p.isLowStock())   return Lang.t("stock_low");
            return Lang.t("stock_ok");
        }

        void refresh() { fireTableDataChanged(); }
    }
}
