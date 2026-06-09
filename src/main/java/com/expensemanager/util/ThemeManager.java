package com.expensemanager.util;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ThemeManager {

    public enum ThemePreset {
        DARK, LIGHT, CUSTOM
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

    // ===================== DARK =====================
    public static void applyDarkTheme() {
        isDarkMode = true;
        currentPreset = ThemePreset.DARK;
        currentTheme.clear();

        currentTheme.put("bg", new Color(18, 18, 18));
        currentTheme.put("surface", new Color(30, 30, 30));
        currentTheme.put("input", new Color(45, 45, 45));
        currentTheme.put("inputBg", new Color(45, 45, 45));   // alias
        currentTheme.put("border", new Color(60, 60, 60));

        currentTheme.put("textPrimary", new Color(240, 240, 240));
        currentTheme.put("textSecondary", new Color(160, 160, 160));

        currentTheme.put("accent", new Color(255, 193, 7));

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

        notifyListeners();
    }

    // ===================== LIGHT =====================
    public static void applyLightTheme() {
        // Light theme is intentionally disabled in this build — always keep dark appearance.
        // To avoid any accidental switch to light, applying light will just re-apply dark theme.
        applyDarkTheme();
    }

    public static void setTheme(ThemePreset preset) {
        // Only dark and custom are supported. If LIGHT requested, fall back to DARK to "bỏ giao diện sáng".
        switch (preset) {
            case DARK: applyDarkTheme(); currentPreset = ThemePreset.DARK; break;
            case LIGHT: applyDarkTheme(); currentPreset = ThemePreset.DARK; break;
            case CUSTOM: currentPreset = ThemePreset.CUSTOM; break;
            default: applyDarkTheme(); currentPreset = ThemePreset.DARK; break;
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

    public static void toggleTheme() {
        // Theme toggle is disabled to enforce dark-only appearance.
        applyDarkTheme();
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
}