package com.expensemanager.service;

import javax.swing.*;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ThemeManager {

    // 1. Thêm các lựa chọn Theme mới vào Enum
    public enum ThemePreset {
        DARK, LIGHT, OCEAN, FOREST, DRACULA, CUSTOM
    }

    private static Map<String, Color> currentTheme = new HashMap<>();
    private static final List<Runnable> listeners = new ArrayList<>();
    private static boolean isDarkMode = true;
    private static ThemePreset currentPreset = ThemePreset.DARK;

    static {
        applyDarkTheme();
    }

    public static Color getColor(String key) {
        return currentTheme.getOrDefault(key, Color.RED);
    }

    // ===================== 1. DARK THEME =====================
    public static void applyDarkTheme() {
        isDarkMode = true;
        currentPreset = ThemePreset.DARK;
        currentTheme.clear();

        currentTheme.put("bg", new Color(18, 18, 18));
        currentTheme.put("surface", new Color(30, 30, 30));
        currentTheme.put("input", new Color(45, 45, 45));
        currentTheme.put("inputBg", new Color(45, 45, 45));
        currentTheme.put("border", new Color(60, 60, 60));
        currentTheme.put("textPrimary", new Color(240, 240, 240));
        currentTheme.put("textSecondary", new Color(160, 160, 160));
        currentTheme.put("accent", new Color(255, 193, 7)); // Vàng

        putCommonColors();
        notifyListeners();
    }

    // ===================== 2. LIGHT THEME =====================
    public static void applyLightTheme() {
        isDarkMode = false;
        currentPreset = ThemePreset.LIGHT;
        currentTheme.clear();

        currentTheme.put("bg", new Color(245, 245, 245));
        currentTheme.put("surface", new Color(255, 255, 255));
        currentTheme.put("input", new Color(230, 230, 230));
        currentTheme.put("inputBg", new Color(230, 230, 230));
        currentTheme.put("border", new Color(200, 200, 200));
        currentTheme.put("textPrimary", new Color(30, 30, 30));
        currentTheme.put("textSecondary", new Color(100, 100, 100));
        currentTheme.put("accent", new Color(255, 152, 0)); // Cam

        putCommonColors();
        notifyListeners();
    }

    // ===================== 3. OCEAN THEME =====================
    public static void applyOceanTheme() {
        isDarkMode = true;
        currentPreset = ThemePreset.OCEAN;
        currentTheme.clear();

        currentTheme.put("bg", new Color(15, 32, 39));
        currentTheme.put("surface", new Color(32, 58, 67));
        currentTheme.put("input", new Color(44, 83, 100));
        currentTheme.put("inputBg", new Color(44, 83, 100));
        currentTheme.put("border", new Color(58, 107, 128));
        currentTheme.put("textPrimary", new Color(224, 247, 250));
        currentTheme.put("textSecondary", new Color(178, 235, 242));
        currentTheme.put("accent", new Color(0, 188, 212)); // Xanh lơ (Cyan)

        putCommonColors();
        notifyListeners();
    }

    // ===================== 4. FOREST THEME =====================
    public static void applyForestTheme() {
        isDarkMode = true;
        currentPreset = ThemePreset.FOREST;
        currentTheme.clear();

        currentTheme.put("bg", new Color(27, 38, 44));
        currentTheme.put("surface", new Color(34, 61, 60));
        currentTheme.put("input", new Color(46, 90, 78));
        currentTheme.put("inputBg", new Color(46, 90, 78));
        currentTheme.put("border", new Color(68, 121, 99));
        currentTheme.put("textPrimary", new Color(232, 245, 233));
        currentTheme.put("textSecondary", new Color(165, 214, 167));
        currentTheme.put("accent", new Color(76, 175, 80)); // Xanh lá cây

        putCommonColors();
        notifyListeners();
    }

    // ===================== 5. DRACULA THEME =====================
    public static void applyDraculaTheme() {
        isDarkMode = true;
        currentPreset = ThemePreset.DRACULA;
        currentTheme.clear();

        currentTheme.put("bg", new Color(40, 42, 54));
        currentTheme.put("surface", new Color(68, 71, 90));
        currentTheme.put("input", new Color(98, 114, 164));
        currentTheme.put("inputBg", new Color(98, 114, 164));
        currentTheme.put("border", new Color(98, 114, 164));
        currentTheme.put("textPrimary", new Color(248, 248, 242));
        currentTheme.put("textSecondary", new Color(191, 191, 191));
        currentTheme.put("accent", new Color(255, 121, 198)); // Hồng tím

        putCommonColors();
        notifyListeners();
    }

    // Hàm chứa các mã màu dùng chung cho mọi theme (như màu báo lỗi, màu biểu đồ)
    private static void putCommonColors() {
        currentTheme.put("success", new Color(76, 175, 80));
        currentTheme.put("danger", new Color(244, 67, 54));
        currentTheme.put("warning", new Color(255, 152, 0));
        currentTheme.put("progressTrack", new Color(65, 65, 65));

        currentTheme.put("chart0", new Color(46, 204, 113));
        currentTheme.put("chart1", new Color(52, 152, 219));
        currentTheme.put("chart2", new Color(155, 89, 182));
        currentTheme.put("chart3", new Color(230, 126, 34));
        currentTheme.put("chart4", new Color(241, 196, 15));
        currentTheme.put("chart5", new Color(231, 76, 60));
        currentTheme.put("chart6", new Color(26, 188, 156));
        currentTheme.put("chart7", new Color(149, 165, 166));
        currentTheme.put("chart8", new Color(243, 156, 18));
    }

    public static void setTheme(ThemePreset preset) {
        switch (preset) {
            case DARK: applyDarkTheme(); break;
            case LIGHT: applyLightTheme(); break;
            case OCEAN: applyOceanTheme(); break;
            case FOREST: applyForestTheme(); break;
            case DRACULA: applyDraculaTheme(); break;
            case CUSTOM: currentPreset = ThemePreset.CUSTOM; break;
            default: applyDarkTheme(); break;
        }

        forceThemeUI();

        // Buộc Swing cập nhật và vẽ lại toàn bộ cây giao diện ngay lập tức
        for (java.awt.Window window : java.awt.Window.getWindows()) {
            javax.swing.SwingUtilities.updateComponentTreeUI(window);
            window.repaint();
        }
    }

    public static void setCustomColor(String key, Color color) {
        currentTheme.put(key, color);
        currentPreset = ThemePreset.CUSTOM;
        notifyListeners();
    }

    public static ThemePreset getCurrentPreset() {
        return currentPreset;
    }

    public static boolean isDark() {
        return isDarkMode;
    }

    public static void addThemeListener(Runnable listener) {
        listeners.add(listener);
    }

    private static void notifyListeners() {
        for (Runnable listener : listeners) {
            listener.run();
        }
    }

    // Đổi tên từ forceDarkModeUI -> forceThemeUI để phản ánh đúng chức năng
    public static void forceThemeUI() {
        UIManager.put("Panel.background", getColor("bg"));
        UIManager.put("Label.foreground", getColor("textPrimary"));
        UIManager.put("Button.background", getColor("surface"));
        UIManager.put("Button.foreground", getColor("textPrimary"));
        UIManager.put("TextField.background", getColor("input"));
        UIManager.put("TextField.foreground", getColor("textPrimary"));
        UIManager.put("ComboBox.background", getColor("input"));
        UIManager.put("ComboBox.foreground", getColor("textPrimary"));
        UIManager.put("Table.background", getColor("surface"));
        UIManager.put("Table.foreground", getColor("textPrimary"));
        UIManager.put("TableHeader.background", getColor("input"));
        UIManager.put("TableHeader.foreground", getColor("textPrimary"));

        UIManager.put("CheckBox.background", getColor("bg"));
        UIManager.put("CheckBox.foreground", getColor("textPrimary"));
        UIManager.put("RadioButton.background", getColor("bg"));
        UIManager.put("RadioButton.foreground", getColor("textPrimary"));
        UIManager.put("OptionPane.background", getColor("bg"));
        UIManager.put("OptionPane.messageForeground", getColor("textPrimary"));
    }
}