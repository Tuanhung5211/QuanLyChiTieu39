package com.expensemanager.util;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class ThemeManager {
    public enum ThemePreset { DARK, LIGHT, CUSTOM }
    private static ThemePreset currentPreset = ThemePreset.DARK;
    private static Map<String, Color> customColors = new HashMap<>();
    private static List<Runnable> listeners = new ArrayList<>();

    // Dark theme colors
    public static final Color DARK_BG = new Color(18, 18, 18);
    public static final Color DARK_SURFACE = new Color(30, 30, 30);
    public static final Color DARK_INPUT = new Color(40, 40, 40);
    public static final Color DARK_TEXT_PRIMARY = new Color(240, 240, 240);
    public static final Color DARK_TEXT_SECONDARY = new Color(150, 150, 150);
    public static final Color DARK_ACCENT = new Color(255, 193, 7);
    public static final Color DARK_BORDER = new Color(60, 60, 60);
    public static final Color DANGER_RED = new Color(244, 67, 54);
    public static final Color SUCCESS_GREEN = new Color(76, 175, 80);

    // Light theme colors
    public static final Color LIGHT_BG = new Color(240, 240, 240);
    public static final Color LIGHT_SURFACE = new Color(255, 255, 255);
    public static final Color LIGHT_INPUT = new Color(245, 245, 245);
    public static final Color LIGHT_TEXT_PRIMARY = new Color(30, 30, 30);
    public static final Color LIGHT_TEXT_SECONDARY = new Color(100, 100, 100);
    public static final Color LIGHT_ACCENT = new Color(0, 120, 212);
    public static final Color LIGHT_BORDER = new Color(200, 200, 200);

    static {
        loadPreset();
    }

    public static void loadPreset() {
        String preset = ConfigLocalStorage.loadThemePreset();
        try {
            currentPreset = ThemePreset.valueOf(preset);
        } catch (IllegalArgumentException e) {
            currentPreset = ThemePreset.DARK;
        }
        if (currentPreset == ThemePreset.CUSTOM) {
            loadCustomColors();
        }
    }

    private static void loadCustomColors() {
        // TODO: load từ file nếu muốn lưu custom colors
        customColors.clear();
    }

    private static void saveCustomColors() {
        // TODO: save nếu cần
    }

    public static void setTheme(ThemePreset preset) {
        currentPreset = preset;
        ConfigLocalStorage.saveThemePreset(preset.name());
        if (preset != ThemePreset.CUSTOM) {
            customColors.clear();
        } else {
            saveCustomColors();
        }
        notifyListeners();
    }

    public static void setCustomColor(String key, Color color) {
        customColors.put(key, color);
        currentPreset = ThemePreset.CUSTOM;
        notifyListeners();
        saveCustomColors();
    }

    public static Color getColor(String key) {
        if (currentPreset == ThemePreset.CUSTOM && customColors.containsKey(key)) {
            return customColors.get(key);
        }
        boolean isDark = (currentPreset == ThemePreset.DARK);
        switch (key) {
            case "bg": return isDark ? DARK_BG : LIGHT_BG;
            case "surface": return isDark ? DARK_SURFACE : LIGHT_SURFACE;
            case "input": return isDark ? DARK_INPUT : LIGHT_INPUT;
            case "textPrimary": return isDark ? DARK_TEXT_PRIMARY : LIGHT_TEXT_PRIMARY;
            case "textSecondary": return isDark ? DARK_TEXT_SECONDARY : LIGHT_TEXT_SECONDARY;
            case "accent": return isDark ? DARK_ACCENT : LIGHT_ACCENT;
            case "border": return isDark ? DARK_BORDER : LIGHT_BORDER;
            case "danger": return DANGER_RED;
            case "success": return SUCCESS_GREEN;
            default: return isDark ? Color.WHITE : Color.BLACK;
        }
    }

    public static void addThemeListener(Runnable listener) {
        if (!listeners.contains(listener)) listeners.add(listener);
    }

    public static void removeThemeListener(Runnable listener) {
        listeners.remove(listener);
    }

    private static void notifyListeners() {
        for (Runnable r : listeners) {
            r.run();
        }
    }
}