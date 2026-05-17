package ui;

import data.OrderStore;
import util.Lang;
import util.Settings;
import util.Theme;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * SettingsPanel.java
 * صفحة الإعدادات - 9 خيارات:
 *  ١. اللغة             — عربي / إنجليزي
 *  ٢. اللون الرئيسي     — برتقالي / أزرق / أخضر / بنفسجي
 *  ٣. الثيم             — داكن / فاتح
 *  ٤. حجم النص          — صغير / عادي / كبير
 *  ٥. اسم المالك        — يظهر في الترحيب
 *  ٦. ترتيب الطلبات     — الأحدث/الأقدم/السعر/الحالة
 *  ٧. ضريبة VAT         — تفعيل + نسبة
 *  ٨. أنواع المنتجات    — إضافة/حذف
 *  ٩. مسح كل البيانات   — زر تأكيدي
 *
 * بعد أي تغيير، تستدعي onRebuild لتُعيد بناء كل الواجهة.
 */
public class SettingsPanel extends JPanel {

    public interface RebuildCallback { void rebuild(); }

    private final OrderStore     store;
    private final RebuildCallback onRebuild;

    public SettingsPanel(OrderStore store, RebuildCallback onRebuild) {
        this.store     = store;
        this.onRebuild = onRebuild;
        setBackground(Theme.BG_DEEP);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 24, 24, 24));
        build();
    }

    private void build() {
        removeAll();

        // العنوان
        JLabel title = UiKit.title(Lang.t("settings_title"));
        title.setHorizontalAlignment(Lang.isArabic() ? SwingConstants.RIGHT : SwingConstants.LEFT);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 18, 0));
        add(title, BorderLayout.NORTH);

        // المحتوى - قابل للتمرير
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        content.add(buildOwnerCard());
        content.add(gap());
        content.add(buildLanguageCard());
        content.add(gap());
        content.add(buildThemeCard());
        content.add(gap());
        content.add(buildAccentCard());
        content.add(gap());
        content.add(buildFontSizeCard());
        content.add(gap());
        content.add(buildSortCard());
        content.add(gap());
        content.add(buildVatCard());
        content.add(gap());
        content.add(buildTypesCard());
        content.add(gap());
        content.add(buildClearCard());
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

        revalidate(); repaint();
    }

    private Component gap() { return Box.createRigidArea(new Dimension(0, 12)); }

    // ── 1. اسم المالك ─────────────────────────────────────────────────────────

    private JPanel buildOwnerCard() {
        JPanel card = makeCard();

        JLabel lbl  = sectionTitle(Lang.t("setting_owner"));
        JLabel hint = UiKit.label(Lang.t("setting_owner_hint"), Theme.FONT_SMALL, Theme.TEXT_MUTED);

        JTextField field = UiKit.textField();
        field.setText(Settings.ownerName);

        JButton btnSave = UiKit.primaryBtn(Lang.t("btn_apply"));
        btnSave.setPreferredSize(new Dimension(110, 36));
        btnSave.addActionListener(e -> {
            Settings.ownerName = field.getText().trim();
            Settings.save();
            toast(Lang.t("settings_saved"));
        });

        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.add(field,   BorderLayout.CENTER);
        row.add(btnSave, Lang.isArabic() ? BorderLayout.WEST : BorderLayout.EAST);

        card.add(lbl);
        card.add(Box.createRigidArea(new Dimension(0, 4)));
        card.add(hint);
        card.add(Box.createRigidArea(new Dimension(0, 12)));
        card.add(row);
        return card;
    }

    // ── 2. اللغة ──────────────────────────────────────────────────────────────

    private JPanel buildLanguageCard() {
        return buildChoiceCard(
            Lang.t("setting_language"),
            new String[]{"ar", "en"},
            new String[]{Lang.t("lang_arabic"), Lang.t("lang_english")},
            Settings.language,
            choice -> {
                Settings.language = choice;
                Settings.save();
                onRebuild.rebuild();
            }
        );
    }

    // ── 3. الثيم ──────────────────────────────────────────────────────────────

    private JPanel buildThemeCard() {
        return buildChoiceCard(
            Lang.t("setting_theme"),
            new String[]{"dark", "light"},
            new String[]{Lang.t("theme_dark"), Lang.t("theme_light")},
            Settings.theme,
            choice -> {
                Settings.theme = choice;
                Settings.save();
                onRebuild.rebuild();
            }
        );
    }

    // ── 4. اللون الرئيسي ──────────────────────────────────────────────────────

    private JPanel buildAccentCard() {
        JPanel card = makeCard();
        card.add(sectionTitle(Lang.t("setting_accent")));
        card.add(Box.createRigidArea(new Dimension(0, 12)));

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEADING, 12, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        String[] colors  = {"amber", "blue", "green", "purple"};
        String[] labels  = {Lang.t("color_amber"), Lang.t("color_blue"),
                            Lang.t("color_green"), Lang.t("color_purple")};
        Color[]  swatchC = {new Color(245,166,35), new Color(99,179,237),
                            new Color(34,197,94), new Color(168,85,247)};

        for (int i = 0; i < colors.length; i++) {
            final String code = colors[i];
            row.add(colorSwatch(swatchC[i], labels[i], code.equals(Settings.accentColor),
                () -> {
                    Settings.accentColor = code;
                    Settings.save();
                    onRebuild.rebuild();
                }));
        }
        card.add(row);
        return card;
    }

    private JPanel colorSwatch(Color color, String label, boolean active, Runnable onClick) {
        JPanel wrap = new JPanel();
        wrap.setOpaque(false);
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));

        JPanel sw = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                if (active) {
                    g2.setColor(Theme.TEXT_PRIMARY);
                    g2.setStroke(new BasicStroke(2.5f));
                    g2.drawRoundRect(2, 2, getWidth()-5, getHeight()-5, 12, 12);
                }
                g2.dispose();
            }
        };
        sw.setPreferredSize(new Dimension(46, 46));
        sw.setMaximumSize(new Dimension(46, 46));
        sw.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        sw.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) { onClick.run(); }
        });
        sw.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lbl = UiKit.label(label, Theme.FONT_SMALL, Theme.TEXT_MUTED);
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        lbl.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));

        wrap.add(sw);
        wrap.add(lbl);
        return wrap;
    }

    // ── 5. حجم النص ───────────────────────────────────────────────────────────

    private JPanel buildFontSizeCard() {
        return buildChoiceCard(
            Lang.t("setting_font_size"),
            new String[]{"small", "normal", "large"},
            new String[]{Lang.t("font_small"), Lang.t("font_normal"), Lang.t("font_large")},
            Settings.fontSize,
            choice -> {
                Settings.fontSize = choice;
                Settings.save();
                onRebuild.rebuild();
            }
        );
    }

    // ── 6. ترتيب الطلبات ──────────────────────────────────────────────────────

    private JPanel buildSortCard() {
        JPanel card = makeCard();
        card.add(sectionTitle(Lang.t("setting_sort")));
        card.add(Box.createRigidArea(new Dimension(0, 12)));

        String[] vals  = {"newest", "oldest", "price_high", "price_low", "status"};
        String[] names = {Lang.t("sort_newest"), Lang.t("sort_oldest"),
                          Lang.t("sort_price_high"), Lang.t("sort_price_low"),
                          Lang.t("sort_status")};

        JComboBox<String> cb = UiKit.darkCombo(names);
        for (int i = 0; i < vals.length; i++) {
            if (vals[i].equals(Settings.orderSort)) cb.setSelectedIndex(i);
        }
        cb.setMaximumSize(new Dimension(300, 38));
        cb.setAlignmentX(Component.LEFT_ALIGNMENT);

        cb.addActionListener(e -> {
            int idx = cb.getSelectedIndex();
            if (idx >= 0) {
                Settings.orderSort = vals[idx];
                Settings.save();
                store.applySort();
                toast(Lang.t("settings_saved"));
            }
        });
        card.add(cb);
        return card;
    }

    // ── 7. ضريبة VAT ──────────────────────────────────────────────────────────

    private JPanel buildVatCard() {
        JPanel card = makeCard();
        card.add(sectionTitle(Lang.t("setting_vat")));
        card.add(Box.createRigidArea(new Dimension(0, 12)));

        // مفتاح تفعيل
        JCheckBox chk = new JCheckBox(Lang.t("setting_vat_enabled"));
        chk.setSelected(Settings.vatEnabled);
        chk.setBackground(Theme.BG_CARD);
        chk.setForeground(Theme.TEXT_PRIMARY);
        chk.setFont(Theme.FONT_BODY);
        chk.setAlignmentX(Component.LEFT_ALIGNMENT);

        // حقل النسبة
        JLabel pctLbl = UiKit.label(Lang.t("setting_vat_rate"), Theme.FONT_SMALL, Theme.TEXT_MUTED);
        JTextField pctField = UiKit.textField();
        pctField.setText(String.valueOf(Settings.vatRate));
        pctField.setEnabled(Settings.vatEnabled);
        pctField.setMaximumSize(new Dimension(140, 36));

        JButton btnSave = UiKit.primaryBtn(Lang.t("btn_apply"));
        btnSave.setPreferredSize(new Dimension(110, 36));

        chk.addActionListener(e -> {
            pctField.setEnabled(chk.isSelected());
        });

        btnSave.addActionListener(e -> {
            try {
                double rate = Double.parseDouble(pctField.getText().trim());
                if (rate < 0 || rate > 100) throw new NumberFormatException();
                Settings.vatEnabled = chk.isSelected();
                Settings.vatRate    = rate;
                Settings.save();
                onRebuild.rebuild();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                    Lang.isArabic() ? "نسبة غير صحيحة (0-100)" : "Invalid rate (0-100)",
                    "⚠", JOptionPane.WARNING_MESSAGE);
            }
        });

        JPanel rateRow = new JPanel(new BorderLayout(10, 0));
        rateRow.setOpaque(false);
        rateRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        rateRow.setMaximumSize(new Dimension(420, 40));
        rateRow.add(pctField, BorderLayout.CENTER);
        rateRow.add(btnSave,  Lang.isArabic() ? BorderLayout.WEST : BorderLayout.EAST);

        card.add(chk);
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(pctLbl);
        card.add(Box.createRigidArea(new Dimension(0, 6)));
        card.add(rateRow);
        return card;
    }

    // ── 8. أنواع المنتجات ─────────────────────────────────────────────────────

    private JPanel buildTypesCard() {
        JPanel card = makeCard();
        card.add(sectionTitle(Lang.t("setting_types")));
        card.add(Box.createRigidArea(new Dimension(0, 12)));

        DefaultListModel<String> listModel = new DefaultListModel<>();
        for (String t : Settings.productTypes) listModel.addElement(t);

        JList<String> list = new JList<>(listModel);
        list.setBackground(Theme.BG_INPUT);
        list.setForeground(Theme.TEXT_PRIMARY);
        list.setSelectionBackground(Theme.ACCENT_DIM);
        list.setSelectionForeground(Theme.TEXT_PRIMARY);
        list.setFont(Theme.FONT_BODY);
        list.setFixedCellHeight(32);

        JScrollPane sp = new JScrollPane(list);
        sp.setBorder(new LineBorder(Theme.BORDER_ACCENT, 1));
        sp.setPreferredSize(new Dimension(400, 160));
        sp.setMaximumSize(new Dimension(400, 160));
        sp.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnAdd    = UiKit.primaryBtn(Lang.t("btn_add"));
        JButton btnRemove = UiKit.dangerBtn(Lang.t("btn_remove"));
        btnAdd.setPreferredSize(new Dimension(100, 34));
        btnRemove.setPreferredSize(new Dimension(100, 34));

        btnAdd.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(this, Lang.t("type_add_prompt"));
            if (input != null) {
                input = input.trim();
                if (!input.isEmpty()) {
                    if (Settings.productTypes.contains(input)) {
                        JOptionPane.showMessageDialog(this, Lang.t("type_exists"));
                        return;
                    }
                    Settings.productTypes.add(input);
                    listModel.addElement(input);
                    Settings.save();
                }
            }
        });

        btnRemove.addActionListener(e -> {
            int idx = list.getSelectedIndex();
            if (idx >= 0) {
                Settings.productTypes.remove(idx);
                listModel.remove(idx);
                Settings.save();
            }
        });

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEADING, 10, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnRow.add(btnAdd);
        btnRow.add(btnRemove);

        card.add(sp);
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(btnRow);
        return card;
    }

    // ── 9. مسح كل البيانات ────────────────────────────────────────────────────

    private JPanel buildClearCard() {
        JPanel card = makeCard();

        JLabel lbl  = sectionTitle(Lang.t("setting_clear"));
        JLabel warn = UiKit.label("⚠  " + Lang.t("setting_clear_warn"), Theme.FONT_SMALL, Theme.RED);

        JButton btn = UiKit.dangerBtn(Lang.t("setting_clear_btn"));
        btn.setPreferredSize(new Dimension(240, 38));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);

        btn.addActionListener(e -> {
            int res = JOptionPane.showConfirmDialog(this,
                Lang.t("confirm_clear"),
                Lang.t("setting_clear"),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
            if (res == JOptionPane.YES_OPTION) {
                store.clearAll();
                toast(Lang.t("settings_saved"));
            }
        });

        JPanel btnWrap = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
        btnWrap.setOpaque(false);
        btnWrap.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnWrap.add(btn);

        card.add(lbl);
        card.add(Box.createRigidArea(new Dimension(0, 4)));
        card.add(warn);
        card.add(Box.createRigidArea(new Dimension(0, 12)));
        card.add(btnWrap);
        return card;
    }

    // ── أدوات مساعدة ─────────────────────────────────────────────────────────

    private JPanel makeCard() {
        JPanel p = new JPanel() {
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
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createEmptyBorder(20, 22, 20, 22));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        return p;
    }

    private JLabel sectionTitle(String text) {
        JLabel l = UiKit.label(text, Theme.FONT_HEAD, Theme.TEXT_PRIMARY);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    /** بطاقة عامة بصف أزرار اختيار. */
    private JPanel buildChoiceCard(String title, String[] values, String[] labels,
                                    String current, java.util.function.Consumer<String> onSelect) {
        JPanel card = makeCard();
        card.add(sectionTitle(title));
        card.add(Box.createRigidArea(new Dimension(0, 12)));

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEADING, 8, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        for (int i = 0; i < values.length; i++) {
            final String val = values[i];
            boolean active = val.equals(current);
            JButton b = active ? UiKit.primaryBtn(labels[i]) : UiKit.ghostBtn(labels[i]);
            b.setPreferredSize(new Dimension(120, 36));
            b.addActionListener(e -> onSelect.accept(val));
            row.add(b);
        }
        card.add(row);
        return card;
    }

    /** رسالة منبثقة قصيرة. */
    private void toast(String msg) {
        Toast.success(this, msg);
    }

    private void styleScrollBar(JScrollPane sp) {
        UiKit.styleScrollBar(sp);
    }
}
