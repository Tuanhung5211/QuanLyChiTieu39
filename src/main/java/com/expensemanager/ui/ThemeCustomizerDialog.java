package com.expensemanager.ui;

import com.expensemanager.service.PremiumManager;
import com.expensemanager.service.ThemeManager;
import com.expensemanager.service.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class ThemeCustomizerDialog extends JDialog {

    private final Map<String, Color> selectedColors = new LinkedHashMap<>();
    private final Map<String, Color> originalColors = new LinkedHashMap<>();
    private final Map<String, JButton> colorButtons = new LinkedHashMap<>();
    private JPanel previewPanel = null;
    private final boolean isVietnamese;

    private static final String[] COLOR_KEYS = {
            "bg", "surface", "input", "textPrimary", "textSecondary", "accent"
    };

    private static final String[] COLOR_LABELS_VI = {
            "Nền", "Bề mặt", "Ô nhập", "Chữ chính", "Chữ phụ", "Màu nhấn"
    };

    private static final String[] COLOR_LABELS_EN = {
            "Background", "Surface", "Input", "Primary Text", "Secondary Text", "Accent"
    };

    // Bảng màu nhanh: 30 màu phổ biến
    private static final Color[] QUICK_COLORS = {
            Color.WHITE, Color.LIGHT_GRAY, Color.GRAY, Color.DARK_GRAY, Color.BLACK,
            new Color(255, 235, 238), new Color(255, 205, 210), new Color(239, 154, 154), new Color(229, 115, 115), new Color(198, 40, 40),
            new Color(232, 245, 233), new Color(200, 230, 201), new Color(129, 199, 132), new Color(76, 175, 80), new Color(27, 94, 32),
            new Color(227, 242, 253), new Color(187, 222, 251), new Color(100, 181, 246), new Color(33, 150, 243), new Color(13, 71, 161),
            new Color(243, 229, 245), new Color(206, 147, 216), new Color(156, 39, 176), new Color(106, 27, 154), new Color(74, 20, 140),
            new Color(255, 243, 224), new Color(255, 224, 178), new Color(255, 183, 77), new Color(255, 152, 0), new Color(230, 81, 0)
    };

    public ThemeCustomizerDialog(Frame owner, boolean isVietnamese) {
        super(owner, isVietnamese ? "Tùy chỉnh màu sắc" : "Customize Colors", true);
        this.isVietnamese = isVietnamese;

        if (!PremiumManager.isPremium(SessionManager.getCurrentUserId())) {
            JOptionPane.showMessageDialog(this,
                    isVietnamese ? "Tính năng này chỉ dành cho Premium!" : "This feature is for Premium only!");
            dispose();
            return;
        }

        for (String key : COLOR_KEYS) {
            Color current = ThemeManager.getColor(key);
            selectedColors.put(key, current);
            originalColors.put(key, current);
        }

        setSize(600, 500);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(15, 15));
        getContentPane().setBackground(ThemeManager.getColor("bg"));

        // Tiêu đề
        JLabel title = new JLabel(isVietnamese ? "Tùy chỉnh giao diện Premium" : "Premium Theme Customization");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(ThemeManager.getColor("textPrimary"));
        title.setBorder(new EmptyBorder(15, 20, 10, 20));
        add(title, BorderLayout.NORTH);

        // Danh sách màu
        JPanel colorListPanel = new JPanel(new GridLayout(COLOR_KEYS.length, 1, 10, 10));
        colorListPanel.setBackground(ThemeManager.getColor("bg"));
        colorListPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

        String[] labels = isVietnamese ? COLOR_LABELS_VI : COLOR_LABELS_EN;

        for (int i = 0; i < COLOR_KEYS.length; i++) {
            final String key = COLOR_KEYS[i];
            final int index = i;
            JPanel row = new JPanel(new BorderLayout(15, 0));
            row.setOpaque(false);

            JLabel lbl = new JLabel(labels[index]);
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            lbl.setForeground(ThemeManager.getColor("textPrimary"));
            lbl.setPreferredSize(new Dimension(120, 30));
            row.add(lbl, BorderLayout.WEST);

            JButton colorBtn = new JButton();
            colorBtn.setBackground(selectedColors.get(key));
            colorBtn.setOpaque(true);
            colorBtn.setBorderPainted(false);
            colorBtn.setFocusPainted(false);
            colorBtn.setPreferredSize(new Dimension(80, 30));
            colorBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            colorBtn.addActionListener(e -> {
                // Hiển thị popup chọn màu nhanh
                Color chosen = showQuickColorPicker(colorBtn, labels[index], selectedColors.get(key));
                if (chosen != null) {
                    selectedColors.put(key, chosen);
                    colorBtn.setBackground(chosen);
                    updatePreview();
                }
            });

            row.add(colorBtn, BorderLayout.EAST);
            colorButtons.put(key, colorBtn);
            colorListPanel.add(row);
        }

        JScrollPane scrollPane = new JScrollPane(colorListPanel);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        // Preview
        previewPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();
                int margin = 10;

                g2.setColor(selectedColors.get("bg"));
                g2.fillRect(0, 0, w, h);

                g2.setColor(selectedColors.get("surface"));
                g2.fillRoundRect(margin, margin, w / 2 - margin, h - 2 * margin, 15, 15);

                g2.setColor(selectedColors.get("input"));
                g2.fillRoundRect(margin + 10, margin + 30, w / 2 - margin - 20, 30, 10, 10);

                g2.setColor(selectedColors.get("textPrimary"));
                g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                g2.drawString("ABC Title", margin + 20, margin + 55);

                g2.setColor(selectedColors.get("textSecondary"));
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                g2.drawString("Description text", margin + 20, margin + 75);

                g2.setColor(selectedColors.get("accent"));
                g2.fillRoundRect(w / 2 + margin + 10, margin + 10, w / 2 - margin - 20, h - 2 * margin - 20, 15, 15);
                g2.setColor(ThemeManager.getContrastColor(selectedColors.get("accent")));
                g2.drawString("Accent", w / 2 + margin + 30, margin + 40);

                g2.dispose();
            }
        };
        previewPanel.setPreferredSize(new Dimension(200, 150));
        previewPanel.setBorder(BorderFactory.createLineBorder(ThemeManager.getColor("border")));

        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(new EmptyBorder(0, 20, 10, 20));
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        centerPanel.add(previewPanel, BorderLayout.SOUTH);
        add(centerPanel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        buttonPanel.setBackground(ThemeManager.getColor("surface"));
        buttonPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

        JButton btnReset = new JButton(isVietnamese ? "Hoàn tác" : "Reset");
        btnReset.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnReset.setBackground(ThemeManager.getColor("input"));
        btnReset.setForeground(ThemeManager.getColor("textPrimary"));
        btnReset.setFocusPainted(false);
        btnReset.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnReset.setBorder(new EmptyBorder(8, 20, 8, 20));
        btnReset.addActionListener(e -> {
            for (String key : COLOR_KEYS) {
                selectedColors.put(key, originalColors.get(key));
                colorButtons.get(key).setBackground(originalColors.get(key));
            }
            updatePreview();
        });

        JButton btnCancel = new JButton(isVietnamese ? "Hủy" : "Cancel");
        btnCancel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCancel.setBackground(ThemeManager.getColor("input"));
        btnCancel.setForeground(ThemeManager.getColor("textPrimary"));
        btnCancel.setFocusPainted(false);
        btnCancel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCancel.setBorder(new EmptyBorder(8, 20, 8, 20));
        btnCancel.addActionListener(e -> dispose());

        JButton btnSave = new JButton(isVietnamese ? "Lưu giao diện" : "Save Theme");
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSave.setBackground(ThemeManager.getColor("accent"));
        btnSave.setForeground(ThemeManager.getContrastColor(ThemeManager.getColor("accent")));
        btnSave.setFocusPainted(false);
        btnSave.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSave.setBorder(new EmptyBorder(8, 20, 8, 20));
        btnSave.addActionListener(e -> {
            for (Map.Entry<String, Color> entry : selectedColors.entrySet()) {
                ThemeManager.setCustomColor(entry.getKey(), entry.getValue());
            }
            ThemeManager.setTheme(ThemeManager.ThemePreset.CUSTOM);
            com.expensemanager.util.ConfigLocalStorage.saveThemePreset("CUSTOM");
            dispose();
            if (getOwner() instanceof MainFrame) {
                ((MainFrame) getOwner()).refreshAllPanelsThemes();
            }
        });

        buttonPanel.add(btnReset);
        buttonPanel.add(btnCancel);
        buttonPanel.add(btnSave);
        add(buttonPanel, BorderLayout.SOUTH);

        ThemeManager.applyThemeRecursively(this);
    }

    // ========== POPUP BẢNG MÀU NHANH ==========
    private Color showQuickColorPicker(Component parent, String label, Color currentColor) {
        JDialog popup = new JDialog(this, label, true);
        popup.setUndecorated(true);
        popup.setLayout(new BorderLayout());
        popup.setSize(260, 220);
        popup.setLocationRelativeTo(parent);

        JPanel colorGrid = new JPanel(new GridLayout(5, 6, 4, 4));
        colorGrid.setBorder(new EmptyBorder(10, 10, 10, 10));
        colorGrid.setBackground(ThemeManager.getColor("surface"));

        final Color[] selected = { null };

        for (Color c : QUICK_COLORS) {
            JButton colorItem = new JButton();
            colorItem.setBackground(c);
            colorItem.setPreferredSize(new Dimension(30, 30));
            colorItem.setFocusPainted(false);
            colorItem.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
            colorItem.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            colorItem.addActionListener(e -> {
                selected[0] = c;
                popup.dispose();
            });
            colorGrid.add(colorItem);
        }

        popup.add(colorGrid, BorderLayout.CENTER);

        // Nhấn chuột ngoài để đóng
        popup.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent e) {
                popup.dispose();
            }
        });
        popup.setVisible(true); // chờ đến khi chọn hoặc click ngoài

        return selected[0];
    }

    private void updatePreview() {
        if (previewPanel != null) {
            previewPanel.repaint();
        }
    }
}