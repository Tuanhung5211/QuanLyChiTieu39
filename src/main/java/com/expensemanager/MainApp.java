package com.expensemanager;

import com.expensemanager.ui.LoginFrame;
import com.expensemanager.util.ConfigLocalStorage;
import com.expensemanager.service.ThemeManager;
import javax.swing.SwingUtilities;

public class MainApp {
    public static void main(String[] args) {
        // 1. Đọc theme đã lưu và áp dụng
        String savedPreset = ConfigLocalStorage.loadThemePreset();  // Trả về "DARK", "LIGHT", hoặc "CUSTOM"
        ThemeManager.ThemePreset preset;
        try {
            preset = ThemeManager.ThemePreset.valueOf(savedPreset);
        } catch (Exception e) {
            preset = ThemeManager.ThemePreset.DARK;   // Mặc định nếu file lỗi
        }
        ThemeManager.setTheme(preset);   // Điều này sẽ gọi applyDarkTheme hoặc applyLightTheme, đồng thời lưu lại preset

        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}