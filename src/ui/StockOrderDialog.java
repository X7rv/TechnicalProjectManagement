package ui;

import data.ProductStore;
import model.Order;
import model.Product;
import util.Lang;
import util.OrderStatus;
import util.Theme;

import javax.swing.*;
import java.awt.*;

/**
 * StockOrderDialog.java
 * نافذة سريعة لإضافة طلب من المخزون.
 * يختار المستخدم منتج من القائمة (يجي السعر تلقائياً)،
 * يدخل الكمية واسم العميل، وخلاص.
 *
 * الحقول الإضافية (إيميل، ملاحظات، إلخ) ما تظهر هنا - للسرعة.
 */
public class StockOrderDialog extends JDialog {

    private Order result = null;

    private final ProductStore  productStore;
    private final JComboBox<Product> cbProduct;
    private final JTextField   tfName;
    private final JTextField   tfEmail;
    private final JTextField   tfQty;
    private final JLabel       lblPrice;
    private final JLabel       lblStock;
    private final JLabel       lblTotal;
    private final JComboBox<String> cbStatus;

    public StockOrderDialog(Frame owner, ProductStore productStore) {
        super(owner, Lang.t("dialog_stock_order"), true);
        this.productStore = productStore;
        getContentPane().setBackground(Theme.BG_CARD);

        // قائمة المنتجات (نأخذ المنتجات اللي عندها مخزون فقط)
        java.util.List<Product> available = new java.util.ArrayList<>();
        for (Product p : productStore.getAll()) {
            if (p.getStock() > 0) available.add(p);
        }
        Product[] arr = available.toArray(new Product[0]);
        cbProduct = new JComboBox<>(arr);
        styleProductCombo(cbProduct);

        tfName  = UiKit.textField();
        tfEmail = UiKit.textField();
        tfQty   = UiKit.textField();
        tfQty.setText("1");

        lblPrice = labelInfo("—");
        lblStock = labelInfo("—");
        lblTotal = bigLabel("0 ر.س");

        cbStatus = UiKit.darkCombo(
            Lang.t("status_paid"),
            Lang.t("status_pending"),
            Lang.t("status_shipped")
        );

        // كل ما يتغير المنتج أو الكمية، نحدّث المعلومات
        cbProduct.addActionListener(e -> updateInfo());
        tfQty.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate (javax.swing.event.DocumentEvent e) { updateInfo(); }
            public void removeUpdate (javax.swing.event.DocumentEvent e) { updateInfo(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateInfo(); }
        });

        updateInfo();

        // ── النموذج ────────────────────────────────────────────────────────
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Theme.BG_CARD);
        form.setBorder(BorderFactory.createEmptyBorder(20, 24, 12, 24));

        addRow(form, 0, Lang.t("lbl_product"),  cbProduct);
        addRow(form, 1, Lang.t("col_price"),    lblPrice);
        addRow(form, 2, Lang.t("col_stock"),    lblStock);
        addRow(form, 3, Lang.t("lbl_qty"),      tfQty);
        addRow(form, 4, Lang.t("lbl_name"),     tfName);
        addRow(form, 5, Lang.t("lbl_email"),    tfEmail);
        addRow(form, 6, Lang.t("col_status"),   cbStatus);

        // بطاقة الإجمالي
        JPanel totalPanel = new JPanel(new BorderLayout());
        totalPanel.setBackground(Theme.BG_INPUT);
        totalPanel.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));
        JLabel tLbl = UiKit.label(Lang.t("col_total") + " :", Theme.FONT_BODY, Theme.TEXT_MUTED);
        lblTotal.setHorizontalAlignment(Lang.isArabic() ? SwingConstants.LEFT : SwingConstants.RIGHT);
        lblTotal.setForeground(Theme.ACCENT);
        totalPanel.add(tLbl, Lang.isArabic() ? BorderLayout.EAST : BorderLayout.WEST);
        totalPanel.add(lblTotal, Lang.isArabic() ? BorderLayout.WEST : BorderLayout.EAST);

        // ── الأزرار ────────────────────────────────────────────────────────
        JButton btnSave   = UiKit.primaryBtn(Lang.t("btn_save"));
        JButton btnCancel = UiKit.ghostBtn(Lang.t("btn_cancel"));
        btnSave.setPreferredSize(new Dimension(110, 38));
        btnCancel.setPreferredSize(new Dimension(110, 38));
        btnSave.addActionListener(e -> onSave());
        btnCancel.addActionListener(e -> dispose());

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        btns.setBackground(Theme.BG_CARD);
        btns.setBorder(BorderFactory.createEmptyBorder(8, 0, 16, 0));
        btns.add(btnSave);
        btns.add(btnCancel);

        JPanel south = new JPanel(new BorderLayout());
        south.setBackground(Theme.BG_CARD);
        south.add(totalPanel, BorderLayout.NORTH);
        south.add(btns,       BorderLayout.SOUTH);

        setLayout(new BorderLayout());
        add(form,  BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);
        pack();
        setMinimumSize(new Dimension(480, 0));
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    /** Combo مخصص لعرض اسم المنتج بشكل واضح. */
    private void styleProductCombo(JComboBox<Product> cb) {
        cb.setFont(Theme.FONT_BODY);
        cb.setBackground(Theme.BG_INPUT);
        cb.setForeground(Theme.TEXT_PRIMARY);
        cb.setBorder(BorderFactory.createLineBorder(Theme.BORDER_ACCENT, 1));
        cb.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);
                lbl.setFont(Theme.FONT_BODY);
                lbl.setBackground(isSelected ? Theme.BG_HOVER : Theme.BG_INPUT);
                lbl.setForeground(Theme.TEXT_PRIMARY);
                lbl.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
                list.setBackground(Theme.BG_INPUT);
                if (value instanceof Product p) {
                    lbl.setText(p.getName() + "   —   " + p.getType());
                }
                return lbl;
            }
        });
    }

    private void addRow(JPanel form, int row, String label, JComponent field) {
        GridBagConstraints lc = new GridBagConstraints();
        lc.gridx = 0; lc.gridy = row;
        lc.anchor = GridBagConstraints.WEST;
        lc.insets = new Insets(7, 0, 7, 14);
        form.add(UiKit.label(label + " :", Theme.FONT_BODY, Theme.TEXT_MUTED), lc);

        GridBagConstraints fc = new GridBagConstraints();
        fc.gridx = 1; fc.gridy = row;
        fc.fill = GridBagConstraints.HORIZONTAL;
        fc.weightx = 1.0;
        fc.insets = new Insets(7, 0, 7, 0);
        form.add(field, fc);
    }

    /** يحدّث السعر والمخزون والإجمالي حسب المنتج المختار. */
    private void updateInfo() {
        Product p = (Product) cbProduct.getSelectedItem();
        if (p == null) {
            lblPrice.setText("—");
            lblStock.setText("—");
            lblTotal.setText("0 ر.س");
            return;
        }
        lblPrice.setText(String.format("%,.0f ر.س", p.getDefaultPrice()));
        lblStock.setText(p.getStock() + " " + Lang.t("unit"));

        // الإجمالي
        try {
            int qty = Integer.parseInt(tfQty.getText().trim());
            if (qty < 1) qty = 1;
            double total = qty * p.getDefaultPrice();
            lblTotal.setText(String.format("%,.0f ر.س", total));
        } catch (NumberFormatException ex) {
            lblTotal.setText("—");
        }
    }

    private void onSave() {
        Product p = (Product) cbProduct.getSelectedItem();
        if (p == null) {
            error(Lang.t("err_no_product_selected"));
            return;
        }
        String name = tfName.getText().trim();
        if (name.isEmpty()) {
            error(Lang.t("err_empty"));
            return;
        }
        int qty;
        try {
            qty = Integer.parseInt(tfQty.getText().trim());
            if (qty <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            error(Lang.t("err_number"));
            return;
        }
        // التحقق من المخزون
        if (qty > p.getStock()) {
            error(String.format(Lang.t("err_insufficient_stock"), p.getStock()));
            return;
        }

        result = new Order(
            name,
            tfEmail.getText().trim(),
            p.getName(),
            p.getType(),
            qty,
            p.getDefaultPrice(),
            OrderStatus.fromDisplay((String) cbStatus.getSelectedItem())
        );
        dispose();
    }

    private void error(String msg) {
        JOptionPane.showMessageDialog(this, msg, "⚠", JOptionPane.WARNING_MESSAGE);
    }

    private JLabel labelInfo(String txt) {
        JLabel l = new JLabel(txt);
        l.setFont(Theme.FONT_BODY);
        l.setForeground(Theme.TEXT_PRIMARY);
        return l;
    }

    private JLabel bigLabel(String txt) {
        JLabel l = new JLabel(txt);
        l.setFont(new Font("SansSerif", Font.BOLD, 18));
        l.setForeground(Theme.TEXT_PRIMARY);
        return l;
    }

    /**
     * يفتح النافذة. إذا ما في منتجات بمخزون، يطلع رسالة ويرجع null.
     * @return الطلب الجديد، أو null إذا أُلغي.
     */
    public static Order show(Frame owner, ProductStore productStore) {
        // نتحقق إن في منتجات بمخزون
        boolean hasStock = false;
        for (Product p : productStore.getAll()) {
            if (p.getStock() > 0) { hasStock = true; break; }
        }
        if (!hasStock) {
            JOptionPane.showMessageDialog(owner,
                Lang.t("err_no_stock"),
                Lang.t("dialog_stock_order"),
                JOptionPane.WARNING_MESSAGE);
            return null;
        }

        StockOrderDialog d = new StockOrderDialog(owner, productStore);
        d.setVisible(true);
        return d.result;
    }
}
