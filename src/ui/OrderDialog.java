package ui;

import model.Order;
import util.Lang;
import util.OrderStatus;
import util.Settings;
import util.Theme;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * OrderDialog.java
 * نافذة موحّدة لإضافة طلب جديد أو تعديل طلب موجود.
 * منظّمة في تبويبين:
 *   - الأساسي:   اسم، إيميل، منتج، نوع، كمية، سعر، حالة
 *   - الإضافي:   أولوية، خصم، طريقة دفع، ملاحظات
 */
public class OrderDialog extends JDialog {

    private boolean confirmed = false;
    private final Order existing;

    // الحقول الأساسية
    private final JTextField        tfName;
    private final JTextField        tfEmail;
    private final JTextField        tfProduct;
    private final JComboBox<String> cbType;
    private final JTextField        tfQty;
    private final JTextField        tfPrice;
    private final JComboBox<String> cbStatus;

    // الحقول الإضافية
    private final JComboBox<String> cbPriority;
    private final JTextField        tfDiscount;
    private final JComboBox<String> cbPayment;
    private final JTextArea         taNotes;

    // قيم enum (تخزن بالإنجليزي، تعرض بالعربي)
    private final String[] PRIORITY_VALS = {"urgent", "normal", "low"};
    private final String[] PAYMENT_VALS  = {"cash",   "transfer", "card"};

    public OrderDialog(Frame owner, Order existing) {
        super(owner, existing == null ? Lang.t("dialog_add") : Lang.t("dialog_edit"), true);
        this.existing = existing;
        getContentPane().setBackground(Theme.BG_CARD);

        // إنشاء الحقول الأساسية
        tfName    = UiKit.textField();
        tfEmail   = UiKit.textField();
        tfProduct = UiKit.textField();
        cbType    = UiKit.darkCombo(Settings.productTypes.toArray(new String[0]));
        tfQty     = UiKit.textField();
        tfPrice   = UiKit.textField();
        cbStatus  = UiKit.darkCombo(
            Lang.t("status_paid"),
            Lang.t("status_pending"),
            Lang.t("status_shipped"),
            Lang.t("status_cancelled")
        );

        // الحقول الإضافية
        cbPriority = UiKit.darkCombo(
            Lang.t("priority_urgent"),
            Lang.t("priority_normal"),
            Lang.t("priority_low")
        );
        cbPriority.setSelectedIndex(1); // normal
        tfDiscount = UiKit.textField();
        tfDiscount.setText("0");
        cbPayment = UiKit.darkCombo(
            Lang.t("payment_cash"),
            Lang.t("payment_transfer"),
            Lang.t("payment_card")
        );
        taNotes = new JTextArea(3, 20);
        taNotes.setFont(Theme.FONT_BODY);
        taNotes.setBackground(Theme.BG_INPUT);
        taNotes.setForeground(Theme.TEXT_PRIMARY);
        taNotes.setCaretColor(Theme.ACCENT);
        taNotes.setLineWrap(true);
        taNotes.setWrapStyleWord(true);
        taNotes.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Theme.BORDER_ACCENT, 1, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        // تعبئة القيم الموجودة عند التعديل
        if (existing != null) {
            tfName.setText(existing.getCustomerName());
            tfEmail.setText(existing.getCustomerEmail());
            tfProduct.setText(existing.getProductName());
            cbType.setSelectedItem(existing.getProductType());
            tfQty.setText(String.valueOf(existing.getQuantity()));
            tfPrice.setText(String.valueOf(existing.getUnitPrice()));
            cbStatus.setSelectedItem(OrderStatus.display(existing.getStatus()));
            // إضافية
            cbPriority.setSelectedIndex(indexOf(PRIORITY_VALS, existing.getPriority(), 1));
            tfDiscount.setText(String.valueOf(existing.getDiscount()));
            cbPayment.setSelectedIndex(indexOf(PAYMENT_VALS, existing.getPaymentMethod(), 0));
            taNotes.setText(existing.getNotes());
        } else {
            tfQty.setText("1");
            tfPrice.setText("0.00");
        }

        // التبويبات
        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(Theme.BG_CARD);
        tabs.setForeground(Theme.TEXT_PRIMARY);
        tabs.setFont(Theme.FONT_BODY);
        tabs.addTab(Lang.t("section_main"),  buildMainTab());
        tabs.addTab(Lang.t("section_extra"), buildExtraTab());

        // الأزرار
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

        setLayout(new BorderLayout());
        add(tabs, BorderLayout.CENTER);
        add(btns, BorderLayout.SOUTH);
        pack();
        setMinimumSize(new Dimension(500, 0));
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    /** بناء التبويب الأساسي. */
    private JPanel buildMainTab() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Theme.BG_CARD);
        form.setBorder(BorderFactory.createEmptyBorder(20, 22, 20, 22));

        addRow(form, 0, Lang.t("lbl_name"),    tfName);
        addRow(form, 1, Lang.t("lbl_email"),   tfEmail);
        addRow(form, 2, Lang.t("lbl_product"), tfProduct);
        addRow(form, 3, Lang.t("lbl_type"),    cbType);
        addRow(form, 4, Lang.t("lbl_qty"),     tfQty);
        addRow(form, 5, Lang.t("lbl_price"),   tfPrice);
        addRow(form, 6, Lang.t("col_status"),  cbStatus);
        return form;
    }

    /** بناء تبويب التفاصيل الإضافية. */
    private JPanel buildExtraTab() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Theme.BG_CARD);
        form.setBorder(BorderFactory.createEmptyBorder(20, 22, 20, 22));

        addRow(form, 0, Lang.t("lbl_priority"), cbPriority);
        addRow(form, 1, Lang.t("lbl_discount"), tfDiscount);
        addRow(form, 2, Lang.t("lbl_payment"),  cbPayment);

        // الملاحظات بطول كامل
        GridBagConstraints lc = new GridBagConstraints();
        lc.gridx = 0; lc.gridy = 3;
        lc.anchor = GridBagConstraints.NORTHWEST;
        lc.insets = new Insets(8, 0, 8, 14);
        form.add(UiKit.label(Lang.t("lbl_notes") + " :", Theme.FONT_BODY, Theme.TEXT_MUTED), lc);

        GridBagConstraints fc = new GridBagConstraints();
        fc.gridx = 1; fc.gridy = 3;
        fc.fill = GridBagConstraints.BOTH;
        fc.weightx = 1.0;
        fc.weighty = 1.0;
        fc.insets = new Insets(8, 0, 8, 0);
        JScrollPane sp = new JScrollPane(taNotes);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.setPreferredSize(new Dimension(280, 80));
        form.add(sp, fc);

        return form;
    }

    private void addRow(JPanel form, int row, String label, JComponent field) {
        GridBagConstraints lc = new GridBagConstraints();
        lc.gridx = 0; lc.gridy = row;
        lc.anchor = GridBagConstraints.WEST;
        lc.insets = new Insets(8, 0, 8, 14);
        form.add(UiKit.label(label + " :", Theme.FONT_BODY, Theme.TEXT_MUTED), lc);

        GridBagConstraints fc = new GridBagConstraints();
        fc.gridx = 1; fc.gridy = row;
        fc.fill = GridBagConstraints.HORIZONTAL;
        fc.weightx = 1.0;
        fc.insets = new Insets(8, 0, 8, 0);
        form.add(field, fc);
    }

    /** يبحث عن قيمة في مصفوفة ويرجع مؤشرها (أو الافتراضي إذا لم توجد). */
    private static int indexOf(String[] arr, String val, int def) {
        if (val == null) return def;
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].equals(val)) return i;
        }
        return def;
    }

    private void onSave() {
        String name     = tfName.getText().trim();
        String email    = tfEmail.getText().trim();
        String product  = tfProduct.getText().trim();
        String qtyStr   = tfQty.getText().trim();
        String priceStr = tfPrice.getText().trim();

        if (name.isEmpty() || product.isEmpty()) {
            showError(Lang.t("err_empty"));
            return;
        }

        int qty;
        double price;
        try {
            qty   = Integer.parseInt(qtyStr);
            price = Double.parseDouble(priceStr.replace(",", ""));
            if (qty <= 0 || price < 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            showError(Lang.t("err_number"));
            return;
        }

        // الخصم
        double discount = 0;
        try {
            String dstr = tfDiscount.getText().trim();
            if (!dstr.isEmpty()) {
                discount = Double.parseDouble(dstr);
                if (discount < 0 || discount > 100) {
                    showError(Lang.isArabic() ? "الخصم يجب يكون بين 0-100" : "Discount 0-100");
                    return;
                }
            }
        } catch (NumberFormatException ex) {
            showError(Lang.isArabic() ? "نسبة الخصم غير صحيحة" : "Invalid discount");
            return;
        }

        // عند التعديل نحدّث الكائن مباشرة
        if (existing != null) {
            existing.setCustomerName(name);
            existing.setCustomerEmail(email);
            existing.setProductName(product);
            existing.setProductType((String) cbType.getSelectedItem());
            existing.setQuantity(qty);
            existing.setUnitPrice(price);
            existing.setStatus(OrderStatus.fromDisplay((String) cbStatus.getSelectedItem()));
            existing.setPriority(PRIORITY_VALS[cbPriority.getSelectedIndex()]);
            existing.setDiscount(discount);
            existing.setPaymentMethod(PAYMENT_VALS[cbPayment.getSelectedIndex()]);
            existing.setNotes(taNotes.getText().trim());
        }

        confirmed = true;
        dispose();
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "⚠", JOptionPane.WARNING_MESSAGE);
    }

    // ── واجهة الاستخدام ──────────────────────────────────────────────────────

    /** يفتح النافذة لإضافة طلب جديد. */
    public static Order showAdd(Frame owner) {
        OrderDialog d = new OrderDialog(owner, null);
        d.setVisible(true);
        if (!d.confirmed) return null;

        int qty;
        double price, discount;
        try {
            qty      = Integer.parseInt(d.tfQty.getText().trim());
            price    = Double.parseDouble(d.tfPrice.getText().trim().replace(",", ""));
            discount = Double.parseDouble(d.tfDiscount.getText().trim().isEmpty() ? "0" : d.tfDiscount.getText().trim());
        } catch (NumberFormatException ex) {
            return null;
        }

        Order o = new Order(
            d.tfName.getText().trim(),
            d.tfEmail.getText().trim(),
            d.tfProduct.getText().trim(),
            (String) d.cbType.getSelectedItem(),
            qty,
            price,
            OrderStatus.fromDisplay((String) d.cbStatus.getSelectedItem())
        );
        o.setPriority(d.PRIORITY_VALS[d.cbPriority.getSelectedIndex()]);
        o.setDiscount(discount);
        o.setPaymentMethod(d.PAYMENT_VALS[d.cbPayment.getSelectedIndex()]);
        o.setNotes(d.taNotes.getText().trim());
        return o;
    }

    /** يفتح النافذة لتعديل طلب موجود. */
    public static boolean showEdit(Frame owner, Order existing) {
        OrderDialog d = new OrderDialog(owner, existing);
        d.setVisible(true);
        return d.confirmed;
    }
}
