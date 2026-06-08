package com.expensemanager.ui;

import com.expensemanager.util.PremiumManager;
import com.expensemanager.util.ThemeManager;
import com.expensemanager.service.SessionManager;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class ThemeCustomizerDialog extends JDialog {
    private final Map<String, Color> selectedColors = new HashMap<>();

    public ThemeCustomizerDialog(Frame owner, boolean isVietnamese) {
        super(owner, isVietnamese ? "Tùy chỉnh giao diện" : "Theme Customizer", true);

        if (!PremiumManager.isPremium(SessionManager.getCurrentUserId())) {
            JOptionPane.showMessageDialog(this, isVietnamese ? "Tính năng này chỉ dành cho Premium!" : "This feature is for Premium only!");
            dispose();
            return;
        }

        setSize(600, 400);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(ThemeManager.getColor("bg"));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        String[] keys = {"bg", "surface", "input", "textPrimary", "textSecondary", "accent"};
        String[] labels = isVietnamese ?
                new String[]{"Nền", "Bề mặt", "Ô nhập", "Chữ chính", "Chữ phụ", "Màu nhấn"} :
                new String[]{"Background", "Surface", "Input", "Primary Text", "Secondary Text", "Accent"};

        for (int i = 0; i < keys.length; i++) {
            JPanel rowPanel = new JPanel(new BorderLayout(10, 10));
            rowPanel.setBackground(ThemeManager.getColor("bg"));
            rowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

            JLabel lbl = new JLabel(labels[i]);
            lbl.setForeground(ThemeManager.getColor("textPrimary"));
            lbl.setPreferredSize(new Dimension(120, 40));
            rowPanel.add(lbl, BorderLayout.WEST);

            Color currentColor = ThemeManager.getColor(keys[i]);
            selectedColors.put(keys[i], currentColor);

            JButton colorBtn = new JButton();
            colorBtn.setBackground(currentColor);
            colorBtn.setOpaque(true);
            colorBtn.setBorderPainted(true);
            colorBtn.setFocusPainted(false);
            colorBtn.setPreferredSize(new Dimension(100, 40));

            String keyToChange = keys[i];
            colorBtn.addActionListener(e -> {
                Color newColor = JColorChooser.showDialog(
                    ThemeCustomizerDialog.this,
                    isVietnamese ? "Chọn màu" : "Choose Color",
                    selectedColors.get(keyToChange)
                );
                if (newColor != null) {
                    selectedColors.put(keyToChange, newColor);
                    colorBtn.setBackground(newColor);
                }
            });

            rowPanel.add(colorBtn, BorderLayout.CENTER);
            mainPanel.add(rowPanel);
            mainPanel.add(Box.createVerticalStrut(5));
        }

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBackground(ThemeManager.getColor("bg"));
        scrollPane.getViewport().setBackground(ThemeManager.getColor("bg"));
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel();
        btnPanel.setBackground(ThemeManager.getColor("surface"));
        btnPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton btnSave = new JButton(isVietnamese ? "Lưu" : "Save");
        btnSave.setBackground(ThemeManager.getColor("accent"));
        btnSave.setForeground(ThemeManager.getColor("bg"));
        btnSave.setFocusPainted(false);
        btnSave.setPreferredSize(new Dimension(100, 35));
        btnSave.addActionListener(e -> {
            for (Map.Entry<String, Color> entry : selectedColors.entrySet()) {
                ThemeManager.setCustomColor(entry.getKey(), entry.getValue());
            }
            dispose();
            // Thông báo để MainFrame refresh
            if (owner instanceof MainFrame) {
                ((MainFrame) owner).refreshAllPanelsThemes();
            }
        });

        JButton btnCancel = new JButton(isVietnamese ? "Hủy" : "Cancel");
        btnCancel.setBackground(ThemeManager.getColor("input"));
        btnCancel.setForeground(ThemeManager.getColor("textPrimary"));
        btnCancel.setFocusPainted(false);
        btnCancel.setPreferredSize(new Dimension(100, 35));
        btnCancel.addActionListener(e -> dispose());

        btnPanel.add(btnCancel);
        btnPanel.add(Box.createHorizontalStrut(10));
        btnPanel.add(btnSave);
        add(btnPanel, BorderLayout.SOUTH);
    }
}