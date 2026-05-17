package ui;

import data.OrderStore;
import data.ProductStore;
import model.Order;
import util.Lang;
import util.Theme;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * OrdersPanel.java
 * صفحة الطلبات - محسّنة بالكامل:
 *   - بحث + فلتر حسب الحالة فوق الجدول
 *   - زرين للإضافة: من المخزون + طلب مخصص
 *   - تحديد متعدد + حذف جماعي
 *   - اختصارات: Ctrl+N للإضافة، Delete للحذف، Ctrl+Z للتراجع، Ctrl+F للبحث
 *   - Toast notifications بدل JOptionPane
 *   - حالة فاضية ودودة لما ما في طلبات
 *   - ترتيب بالضغط على رأس العمود
 *   - زر تراجع (Undo)
 */
public class OrdersPanel extends JPanel {

    private final OrderStore         store;
    private final ProductStore       productStore;
    private final FilteredTableModel model;
    private final JTable             table;
    private JTextField               searchField;
    private JComboBox<String>        statusFilter;
    private JScrollPane              scrollPane;
    private EmptyState               emptyView;
    private JLabel                   statusInfo;
    private JPanel                   contentWrapper;

    public OrdersPanel(OrderStore store, ProductStore productStore) {
        this.store        = store;
        this.productStore = productStore;
        this.model        = new FilteredTableModel(store);
        this.table        = buildTable();
        setLayout(new BorderLayout());
        setBackground(Theme.BG_DEEP);

        store.addChangeListener(() -> SwingUtilities.invokeLater(this::refresh));

        build();
        registerShortcuts();
    }

    // ── البناء ────────────────────────────────────────────────────────────────

    private void build() {
        removeAll();
        add(buildHeader(), BorderLayout.NORTH);

        contentWrapper = new JPanel(new BorderLayout());
        contentWrapper.setBackground(Theme.BG_DEEP);
        contentWrapper.add(buildSearchBar(), BorderLayout.NORTH);

        scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setBackground(Theme.BG_CARD);
        scrollPane.getViewport().setBackground(Theme.BG_CARD);
        styleScrollBar(scrollPane);
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);

        emptyView = new EmptyState("📦",
            Lang.t("empty_orders_title"),
            Lang.t("empty_orders_hint"));

        contentWrapper.add(scrollPane, BorderLayout.CENTER);
        add(contentWrapper, BorderLayout.CENTER);

        refresh();
        revalidate(); repaint();
    }

    private JPanel buildHeader() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Theme.BG_DEEP);
        bar.setBorder(BorderFactory.createEmptyBorder(20, 24, 10, 24));

        JLabel title = UiKit.title(Lang.t("nav_orders"));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);

        JButton btnStockOrder  = UiKit.primaryBtn(Lang.t("add_stock_order"));
        JButton btnCustomOrder = UiKit.ghostBtn(Lang.t("add_custom_order"));
        JButton btnEdit        = UiKit.ghostBtn(Lang.t("edit_order"));
        JButton btnDuplicate   = UiKit.ghostBtn(Lang.t("btn_duplicate"));
        JButton btnUndo        = UiKit.ghostBtn(Lang.t("btn_undo"));
        JButton btnDelete      = UiKit.dangerBtn(Lang.t("btn_delete"));

        btnStockOrder.setToolTipText(Lang.t("shortcut_add"));
        btnCustomOrder.setToolTipText("Ctrl+Shift+N");
        btnUndo.setToolTipText(Lang.t("shortcut_undo"));
        btnDuplicate.setToolTipText("Ctrl+D");
        btnDelete.setToolTipText("Delete");

        btnStockOrder.addActionListener(e  -> onAddFromStock());
        btnCustomOrder.addActionListener(e -> onAddCustom());
        btnEdit.addActionListener(e        -> onEdit());
        btnDuplicate.addActionListener(e   -> onDuplicate());
        btnUndo.addActionListener(e        -> onUndo());
        btnDelete.addActionListener(e      -> onDelete());

        actions.add(btnStockOrder);
        actions.add(btnCustomOrder);
        actions.add(btnEdit);
        actions.add(btnDuplicate);
        actions.add(btnUndo);
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

    /** شريط البحث + الفلتر فوق الجدول. */
    private JPanel buildSearchBar() {
        JPanel searchBar = new JPanel(new BorderLayout(10, 0));
        searchBar.setBackground(Theme.BG_DEEP);
        searchBar.setBorder(BorderFactory.createEmptyBorder(0, 24, 14, 24));

        searchField = UiKit.textField();
        searchField.setToolTipText(Lang.t("search_placeholder") + "  (Ctrl+F)");
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate (javax.swing.event.DocumentEvent e) { applyFilter(); }
            public void removeUpdate (javax.swing.event.DocumentEvent e) { applyFilter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
        });

        statusFilter = UiKit.darkCombo(
            Lang.t("filter_all"),
            Lang.t("status_paid"),
            Lang.t("status_shipped"),
            Lang.t("status_pending"),
            Lang.t("status_cancelled")
        );
        statusFilter.setPreferredSize(new Dimension(140, 36));
        statusFilter.addActionListener(e -> applyFilter());

        statusInfo = UiKit.muted("");

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEADING, 10, 0));
        left.setOpaque(false);
        left.add(statusFilter);
        left.add(statusInfo);

        searchBar.add(searchField, BorderLayout.CENTER);
        searchBar.add(left, Lang.isArabic() ? BorderLayout.WEST : BorderLayout.EAST);

        return searchBar;
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
        t.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

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

        t.getColumnModel().getColumn(OrderTableModel.COL_STATUS)
            .setCellRenderer(new BadgeRenderer(OrderTableModel::statusColor));

        // ترتيب بالضغط على الرأس
        t.setAutoCreateRowSorter(true);

        t.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) onEdit();
            }
        });

        int[] widths = {80, 130, 140, 130, 100, 50, 95, 100, 95, 95};
        for (int i = 0; i < widths.length; i++)
            t.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        return t;
    }

    // ── التحديث ──────────────────────────────────────────────────────────────

    private void refresh() {
        model.refresh();
        applyFilter();
        updateView();
    }

    private void applyFilter() {
        if (searchField == null || statusFilter == null) return;
        String query  = searchField.getText().trim().toLowerCase();
        String status = (String) statusFilter.getSelectedItem();
        if (status != null && status.equals(Lang.t("filter_all"))) status = null;

        model.setFilter(query, status);
        updateStatusInfo();
        updateView();
    }

    private void updateStatusInfo() {
        int shown = model.getRowCount();
        int total = store.size();
        statusInfo.setText(shown == total
            ? String.format("(%d)", total)
            : String.format("(%d / %d)", shown, total));
    }

    /** يبدّل بين الجدول والحالة الفاضية. */
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

    // ── الاختصارات ───────────────────────────────────────────────────────────

    private void registerShortcuts() {
        InputMap  im = getInputMap(WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK), "addStock");
        am.put("addStock", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { onAddFromStock(); }
        });
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_N,
                InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK), "addCustom");
        am.put("addCustom", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { onAddCustom(); }
        });
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK), "undo");
        am.put("undo", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { onUndo(); }
        });
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "delete");
        am.put("delete", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { onDelete(); }
        });
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK), "search");
        am.put("search", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (searchField != null) searchField.requestFocus();
            }
        });
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK), "duplicate");
        am.put("duplicate", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { onDuplicate(); }
        });
    }

    // ── العمليات ─────────────────────────────────────────────────────────────

    /** طلب من المخزون - يختار منتج موجود ويُنشئ طلب بسرعة. */
    private void onAddFromStock() {
        Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
        Order newOrder = StockOrderDialog.show(owner, productStore);
        if (newOrder == null) return;
        if (!store.add(newOrder)) {
            Toast.error(this, store.getLastError());
            return;
        }
        Toast.success(this, Lang.t("toast_added"));
    }

    /** طلب مخصص - النموذج الكامل (اسم، منتج، نوع، إلخ يكتبهم يدوياً). */
    private void onAddCustom() {
        Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
        Order newOrder = OrderDialog.showAdd(owner);
        if (newOrder == null) return;
        if (!store.add(newOrder)) {
            Toast.error(this, store.getLastError());
            return;
        }
        Toast.success(this, Lang.t("toast_added"));
    }

    private void onEdit() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            Toast.warning(this, Lang.t("err_select"));
            return;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        int storeIdx = model.getStoreIndex(modelRow);
        if (storeIdx < 0) return;

        Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
        Order o = store.get(storeIdx);
        Order snapshot = o.copy();

        if (!OrderDialog.showEdit(owner, o)) return;
        if (!store.update(o)) {
            Toast.error(this, store.getLastError());
            return;
        }
        store.getUndoManager().recordUpdate(o, snapshot);
        Toast.success(this, Lang.t("toast_updated"));
    }

    private void onDelete() {
        int[] viewRows = table.getSelectedRows();
        if (viewRows.length == 0) {
            Toast.warning(this, Lang.t("err_select"));
            return;
        }

        String msg = viewRows.length == 1
            ? Lang.t("confirm_delete")
            : (Lang.isArabic()
                ? "حذف " + viewRows.length + " طلب؟"
                : "Delete " + viewRows.length + " orders?");

        int confirm = JOptionPane.showConfirmDialog(this, msg,
            Lang.t("btn_delete"), JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        int[] storeIndexes = new int[viewRows.length];
        for (int i = 0; i < viewRows.length; i++) {
            int mr = table.convertRowIndexToModel(viewRows[i]);
            storeIndexes[i] = model.getStoreIndex(mr);
        }
        java.util.Arrays.sort(storeIndexes);
        int deleted = 0;
        for (int i = storeIndexes.length - 1; i >= 0; i--) {
            if (storeIndexes[i] >= 0 && store.remove(storeIndexes[i])) deleted++;
        }

        Toast.success(this,
            deleted == 1
                ? Lang.t("toast_deleted")
                : (Lang.isArabic() ? "تم حذف " + deleted + " طلب" : "Deleted " + deleted));
    }

    private void onUndo() {
        if (!store.getUndoManager().canUndo()) {
            Toast.info(this, Lang.t("toast_no_undo"));
            return;
        }
        if (store.performUndo()) {
            Toast.success(this, Lang.t("toast_undone"));
        }
    }

    private void onDuplicate() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            Toast.warning(this, Lang.t("err_select"));
            return;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        int storeIdx = model.getStoreIndex(modelRow);
        if (storeIdx < 0) return;

        Order original = store.get(storeIdx);
        // ننشئ طلب جديد من البيانات نفسها (التاريخ يصير اليوم، الـ ID جديد)
        Order copy = new Order(
            original.getCustomerName(),
            original.getCustomerEmail(),
            original.getProductName(),
            original.getProductType(),
            original.getQuantity(),
            original.getUnitPrice(),
            util.OrderStatus.PENDING   // الطلب الجديد يبدأ كمعلق
        );
        copy.setPriority(original.getPriority());
        copy.setDiscount(original.getDiscount());
        copy.setPaymentMethod(original.getPaymentMethod());
        copy.setNotes(original.getNotes());

        if (!store.add(copy)) {
            Toast.error(this, store.getLastError());
            return;
        }
        Toast.success(this, Lang.t("toast_duplicated"));
    }

    // ── أدوات مساعدة ─────────────────────────────────────────────────────────

    private void styleScrollBar(JScrollPane sp) {
        UiKit.styleScrollBar(sp);
    }

    // ── نموذج جدول مع فلتر ────────────────────────────────────────────────────

    /**
     * يلف OrderTableModel ويضيف بحث وفلتر.
     * filteredIndexes تحفظ مواضع الطلبات المعروضة في المتجر الأصلي.
     */
    private static class FilteredTableModel extends OrderTableModel {
        private final OrderStore source;
        private final java.util.List<Integer> filteredIndexes = new java.util.ArrayList<>();
        private String query  = "";
        private String status = null;

        FilteredTableModel(OrderStore store) {
            super(store);
            this.source = store;
            rebuildFilter();
        }

        void setFilter(String query, String status) {
            this.query  = query == null ? "" : query.toLowerCase();
            this.status = status;
            rebuildFilter();
        }

        @Override public void refresh() {
            rebuildFilter();
        }

        private void rebuildFilter() {
            filteredIndexes.clear();
            for (int i = 0; i < source.size(); i++) {
                Order o = source.get(i);
                if (matches(o)) filteredIndexes.add(i);
            }
            fireTableDataChanged();
        }

        private boolean matches(Order o) {
            if (status != null && !status.equals(o.getStatus())) return false;
            if (query.isEmpty()) return true;
            return o.getCustomerName().toLowerCase().contains(query)
                || o.getProductName().toLowerCase().contains(query)
                || o.getProductType().toLowerCase().contains(query)
                || o.getCustomerEmail().toLowerCase().contains(query)
                || o.getId().toLowerCase().contains(query);
        }

        int getStoreIndex(int filteredRow) {
            if (filteredRow < 0 || filteredRow >= filteredIndexes.size()) return -1;
            return filteredIndexes.get(filteredRow);
        }

        @Override public int getRowCount() { return filteredIndexes.size(); }

        @Override public Object getValueAt(int row, int col) {
            int idx = getStoreIndex(row);
            if (idx < 0) return "";
            return super.getValueAt(idx, col);
        }
    }
}
