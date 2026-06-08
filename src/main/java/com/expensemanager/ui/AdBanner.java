package com.expensemanager.ui;

import com.expensemanager.util.ThemeManager;
import javax.swing.*;
import java.awt.*;

public class AdBanner extends JPanel {
    private boolean isVietnamese;
    private JButton btnUpgrade;
    private JLabel lblAd;   // ✅ thêm field
    private Runnable onUpgradeClick;

    public AdBanner(boolean isVietnamese, Runnable onUpgradeClick) {
        this.isVietnamese = isVietnamese;
        this.onUpgradeClick = onUpgradeClick;
        setPreferredSize(new Dimension(0, 70));
        setLayout(new BorderLayout());

        lblAd = new JLabel(isVietnamese ? "🌟 Nâng cấp Premium để loại bỏ quảng cáo và tùy chỉnh giao diện" : "🌟 Upgrade to Premium to remove ads and customize theme");
        lblAd.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        add(lblAd, BorderLayout.CENTER);

        btnUpgrade = new JButton(isVietnamese ? "Đăng ký ngay" : "Upgrade Now");
        btnUpgrade.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnUpgrade.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnUpgrade.addActionListener(e -> {
            if (onUpgradeClick != null) onUpgradeClick.run();
        });
        add(btnUpgrade, BorderLayout.EAST);

        applyTheme();   // ✅ gọi sau khi đã có component
    }

    public void updateLanguage(boolean isVN) {
        this.isVietnamese = isVN;
        lblAd.setText(isVN ? "🌟 Nâng cấp Premium để loại bỏ quảng cáo và tùy chỉnh giao diện" : "🌟 Upgrade to Premium to remove ads and customize theme");
        btnUpgrade.setText(isVN ? "Đăng ký ngay" : "Upgrade Now");
    }

    public void applyTheme() {
        setBackground(ThemeManager.getColor("surface"));
        setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, ThemeManager.getColor("border")));
        if (btnUpgrade != null) {
            btnUpgrade.setBackground(ThemeManager.getColor("accent"));
            btnUpgrade.setForeground(ThemeManager.getColor("textPrimary"));
        }
        if (lblAd != null) {
            lblAd.setForeground(ThemeManager.getColor("textPrimary"));
        }
    }
}