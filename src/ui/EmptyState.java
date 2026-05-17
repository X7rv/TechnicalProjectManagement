package ui;

import util.Theme;

import javax.swing.*;
import java.awt.*;

/**
 * EmptyState.java
 * لوحة "حالة فاضية" — تظهر بدل الجدول لما ما في بيانات.
 */
public class EmptyState extends JPanel {

    public EmptyState(String icon, String title, String hint) {
        setOpaque(false);
        setLayout(new GridBagLayout());

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        JLabel iconLbl = new JLabel(icon);
        iconLbl.setFont(new Font("SansSerif", Font.PLAIN, 56));
        iconLbl.setForeground(Theme.TEXT_MUTED);
        iconLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(Theme.FONT_HEAD);
        titleLbl.setForeground(Theme.TEXT_PRIMARY);
        titleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLbl.setBorder(BorderFactory.createEmptyBorder(14, 0, 4, 0));

        JLabel hintLbl = new JLabel(hint);
        hintLbl.setFont(Theme.FONT_SMALL);
        hintLbl.setForeground(Theme.TEXT_MUTED);
        hintLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        center.add(iconLbl);
        center.add(titleLbl);
        center.add(hintLbl);
        add(center);
    }
}
