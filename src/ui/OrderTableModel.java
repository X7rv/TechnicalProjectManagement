package ui;

import data.OrderStore;
import model.Order;
import util.Lang;
import util.Money;
import util.OrderStatus;
import util.Theme;

import javax.swing.table.AbstractTableModel;
import java.awt.Color;

/**
 * OrderTableModel.java
 * نموذج بيانات جدول الطلبات.
 * الأعمدة: ID | اسم | إيميل | منتج | نوع | كمية | سعر | إجمالي | تاريخ | حالة
 */
public class OrderTableModel extends AbstractTableModel {

    private final OrderStore store;

    public static final int COL_ID      = 0;
    public static final int COL_NAME    = 1;
    public static final int COL_EMAIL   = 2;
    public static final int COL_PRODUCT = 3;
    public static final int COL_TYPE    = 4;
    public static final int COL_QTY     = 5;
    public static final int COL_PRICE   = 6;
    public static final int COL_TOTAL   = 7;
    public static final int COL_DATE    = 8;
    public static final int COL_STATUS  = 9;

    public OrderTableModel(OrderStore store) { this.store = store; }

    @Override public int getRowCount()    { return store.size(); }
    @Override public int getColumnCount() { return 10; }

    @Override public String getColumnName(int col) {
        return switch (col) {
            case COL_ID      -> Lang.t("col_id");
            case COL_NAME    -> Lang.t("col_name");
            case COL_EMAIL   -> Lang.t("col_email");
            case COL_PRODUCT -> Lang.t("col_product");
            case COL_TYPE    -> Lang.t("col_type");
            case COL_QTY     -> Lang.t("col_qty");
            case COL_PRICE   -> Lang.t("col_price");
            case COL_TOTAL   -> Lang.t("col_total");
            case COL_DATE    -> Lang.t("col_date");
            case COL_STATUS  -> Lang.t("col_status");
            default -> "";
        };
    }

    @Override public Object getValueAt(int row, int col) {
        Order o = store.get(row);
        return switch (col) {
            case COL_ID      -> o.getId();
            case COL_NAME    -> o.getCustomerName();
            case COL_EMAIL   -> o.getCustomerEmail().isEmpty() ? "—" : o.getCustomerEmail();
            case COL_PRODUCT -> o.getProductName();
            case COL_TYPE    -> o.getProductType();
            case COL_QTY     -> o.getQuantity();
            case COL_PRICE   -> Money.format(o.getUnitPrice());
            case COL_TOTAL   -> o.getTotalFormatted();
            case COL_DATE    -> o.getDate();
            case COL_STATUS  -> OrderStatus.display(o.getStatus());
            default -> "";
        };
    }

    @Override public Class<?> getColumnClass(int col) {
        return col == COL_QTY ? Integer.class : String.class;
    }

    @Override public boolean isCellEditable(int row, int col) { return false; }

    public void refresh() { fireTableDataChanged(); }

    /**
     * يرجع لون شارة الحالة.
     * يقبل النص المعروض (بأي لغة) أو المفتاح المخزّن.
     */
    public static Color statusColor(String displayed) {
        String key = OrderStatus.fromDisplay(displayed);
        return switch (key) {
            case OrderStatus.PAID      -> Theme.GREEN;
            case OrderStatus.SHIPPED   -> Theme.BLUE;
            case OrderStatus.PENDING   -> Theme.ACCENT;
            case OrderStatus.CANCELLED -> Theme.RED;
            default                    -> Theme.TEXT_MUTED;
        };
    }
}
