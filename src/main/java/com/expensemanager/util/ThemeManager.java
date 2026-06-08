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

        currentTheme.put("accent", new Color(0, 120, 215));

        currentTheme.put("success", new Color(56, 142, 60));
        currentTheme.put("danger", new Color(211, 47, 47));
        currentTheme.put("warning", new Color(255, 140, 0));
        currentTheme.put("progressTrack", new Color(210, 210, 210));

        currentTheme.put("chart0", new Color(39, 174, 96));
        currentTheme.put("chart1", new Color(41, 128, 185));
        currentTheme.put("chart2", new Color(142, 68, 173));
        currentTheme.put("chart3", new Color(211, 84, 0));
        currentTheme.put("chart4", new Color(243, 156, 18));
        currentTheme.put("chart5", new Color(192, 57, 43));
        currentTheme.put("chart6", new Color(22, 160, 133));
        currentTheme.put("chart7", new Color(127, 140, 141));
        currentTheme.put("chart8", new Color(230, 126, 34));

        notifyListeners();
    }

    public static void setTheme(ThemePreset preset) {
        switch (preset) {
            case DARK: applyDarkTheme(); break;
            case LIGHT: applyLightTheme(); break;
            default: break;
        }
        currentPreset = preset;
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
        if (isDarkMode) applyLightTheme();
        else applyDarkTheme();
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