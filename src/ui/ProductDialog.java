package ui;

import model.Product;
import util.Lang;
import util.Settings;
import util.Theme;

import javax.swing.*;
import java.awt.*;

/**
 * ProductDialog.java
 * نافذة إضافة/تعديل المنتج.
 *   - الاسم
 *   - النوع (من إعدادات أنواع المنتجات)
 *   - السعر الافتراضي
 *   - المخزون الأولي
 *   - حد التنبيه
 */
public class ProductDialog extends JDialog {

    private boolean confirmed = false;
    private final Product existing;

    private final JTextField        tfName;
    private final JComboBox<String> cbType;
    private final JTextField        tfPrice;
    private final JTextField        tfStock;
    private final JTextField        tfThreshold;

    public ProductDialog(Frame owner, Product existing) {
        super(owner, existing == null
            ? Lang.t("dialog_add_product")
            : Lang.t("dialog_edit_product"), true);
        this.existing = existing;
        getContentPane().setBackground(Theme.BG_CARD);

        tfName      = UiKit.textField();
        cbType      = UiKit.darkCombo(Settings.productTypes.toArray(new String[0]));
        tfPrice     = UiKit.textField();
        tfStock     = UiKit.textField();
        tfThreshold = UiKit.textField();

        if (existing != null) {
            tfName.setText(existing.getName());
            cbType.setSelectedItem(existing.getType());
            tfPrice.setText(String.valueOf(existing.getDefaultPrice()));
            tfStock.setText(String.valueOf(existing.getStock()));
            tfThreshold.setText(String.valueOf(existing.getLowStockThreshold()));
        } else {
            tfPrice.setText("0.00");
            tfStock.setText("0");
            tfThreshold.setText("5");
        }

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Theme.BG_CARD);
        form.setBorder(BorderFactory.createEmptyBorder(24, 28, 12, 28));

        addRow(form, 0, Lang.t("lbl_product"),      tfName);
        addRow(form, 1, Lang.t("lbl_type"),         cbType);
        addRow(form, 2, Lang.t("lbl_price"),        tfPrice);
        addRow(form, 3, Lang.t("lbl_stock"),        tfStock);
        addRow(form, 4, Lang.t("lbl_low_threshold"),tfThreshold);

        JButton btnSave   = UiKit.primaryBtn(Lang.t("btn_save"));
        JButton btnCancel = UiKit.ghostBtn(Lang.t("btn_cancel"));
        btnSave.setPreferredSize(new Dimension(110, 38));
        btnCancel.setPreferredSize(new Dimension(110, 38));
        btnSave.addActionListener(e -> onSave());
        btnCancel.addActionListener(e -> dispose());

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        btns.setBackground(Theme.BG_CARD);
        btns.setBorder(BorderFactory.createEmptyBorder(4, 0, 20, 0));
        btns.add(btnSave);
        btns.add(btnCancel);

        setLayout(new BorderLayout());
        add(form, BorderLayout.CENTER);
        add(btns, BorderLayout.SOUTH);
        pack();
        setMinimumSize(new Dimension(440, 0));
        setResizable(false);
        setLocationRelativeTo(owner);
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

    private void onSave() {
        String name = tfName.getText().trim();
        if (name.isEmpty()) {
            showError(Lang.t("err_empty_product"));
            return;
        }

        double price;
        int    stock, threshold;
        try {
            price     = Double.parseDouble(tfPrice.getText().trim().replace(",", ""));
            stock     = Integer.parseInt(tfStock.getText().trim());
            threshold = Integer.parseInt(tfThreshold.getText().trim());
            if (price < 0 || stock < 0 || threshold < 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            showError(Lang.t("err_number"));
            return;
        }

        if (existing != null) {
            existing.setName(name);
            existing.setType((String) cbType.getSelectedItem());
            existing.setDefaultPrice(price);
            existing.setStock(stock);
            existing.setLowStockThreshold(threshold);
        }

        confirmed = true;
        dispose();
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "⚠", JOptionPane.WARNING_MESSAGE);
    }

    public static Product showAdd(Frame owner) {
        ProductDialog d = new ProductDialog(owner, null);
        d.setVisible(true);
        if (!d.confirmed) return null;
        try {
            return new Product(
                d.tfName.getText().trim(),
                (String) d.cbType.getSelectedItem(),
                Double.parseDouble(d.tfPrice.getText().trim().replace(",", "")),
                Integer.parseInt(d.tfStock.getText().trim())
            ) {{
                setLowStockThreshold(Integer.parseInt(d.tfThreshold.getText().trim()));
            }};
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public static boolean showEdit(Frame owner, Product existing) {
        ProductDialog d = new ProductDialog(owner, existing);
        d.setVisible(true);
        return d.confirmed;
    }
}
