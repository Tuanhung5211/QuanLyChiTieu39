package com.expensemanager.ui;

import com.expensemanager.util.PremiumManager;
import com.expensemanager.util.ThemeManager;
import com.expensemanager.service.SessionManager;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class ThemeCustomizerDialog extends JDialog {
    private Map<String, JColorChooser> colorChoosers = new HashMap<>();
    private boolean isVietnamese;

    public ThemeCustomizerDialog(Frame owner, boolean isVietnamese) {
        super(owner, isVietnamese ? "Tùy chỉnh giao diện" : "Theme Customizer", true);
        this.isVietnamese = isVietnamese;

        if (!PremiumManager.isPremium(SessionManager.getCurrentUserId())) {
            JOptionPane.showMessageDialog(this, isVietnamese ? "Tính năng này chỉ dành cho Premium!" : "This feature is for Premium only!");
            dispose();
            return;
        }

        setSize(550, 450);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
        String[] keys = {"bg", "surface", "input", "textPrimary", "textSecondary", "accent"};
        String[] labels = isVietnamese ?
                new String[]{"Nền", "Bề mặt", "Ô nhập", "Chữ chính", "Chữ phụ", "Màu nhấn"} :
                new String[]{"Background", "Surface", "Input", "Primary Text", "Secondary Text", "Accent"};

        for (int i = 0; i < keys.length; i++) {
            JLabel lbl = new JLabel(labels[i]);
            lbl.setForeground(ThemeManager.getColor("textPrimary"));
            panel.add(lbl);
            JColorChooser chooser = new JColorChooser(ThemeManager.getColor(keys[i]));
            colorChoosers.put(keys[i], chooser);
            panel.add(chooser);
        }
        panel.setBackground(ThemeManager.getColor("bg"));

        JButton btnSave = new JButton(isVietnamese ? "Lưu" : "Save");
        btnSave.setBackground(ThemeManager.getColor("accent"));
        btnSave.addActionListener(e -> {
            for (Map.Entry<String, JColorChooser> entry : colorChoosers.entrySet()) {
                ThemeManager.setCustomColor(entry.getKey(), entry.getValue().getColor());
            }
            dispose();
            // Thông báo để MainFrame refresh
            if (owner instanceof MainFrame) {
                ((MainFrame) owner).refreshAllPanelsThemes();
            }
        });

        JPanel btnPanel = new JPanel();
        btnPanel.setBackground(ThemeManager.getColor("bg"));
        btnPanel.add(btnSave);
        add(panel, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);
    }
}