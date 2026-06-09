package com.expensemanager.service;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ThemeManager {

    public enum ThemePreset {
        DARK, LIGHT, OCEAN, FOREST, DRACULA, SUNSET, LAVENDER, MATERIAL_LIGHT, CUSTOM
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

    public static Color getContrastColor(Color background) {
        double luminance = (0.299 * background.getRed() + 0.587 * background.getGreen() + 0.114 * background.getBlue()) / 255;
        return luminance > 0.5 ? Color.BLACK : Color.WHITE;
    }

    private static void finalizeThemeColors(Color bg, Color surface, Color accent) {
        currentTheme.put("bg", bg);
        currentTheme.put("surface", surface);
        currentTheme.put("accent", accent);

        double luminance = (0.299 * bg.getRed() + 0.587 * bg.getGreen() + 0.114 * bg.getBlue()) / 255.0;
        isDarkMode = luminance < 0.5;

        if (isDarkMode) {
            currentTheme.put("textPrimary", new Color(240, 240, 240));
            currentTheme.put("textSecondary", new Color(160, 160, 160));
            currentTheme.put("border", surface.brighter().brighter());
            currentTheme.put("inputBg", bg.brighter().brighter());
            currentTheme.put("input", bg.brighter().brighter());
            currentTheme.put("progressTrack", new Color(65, 65, 65));
        } else {
            currentTheme.put("textPrimary", new Color(30, 30, 30));
            currentTheme.put("textSecondary", new Color(100, 100, 100));
            currentTheme.put("border", surface.darker().darker());
            currentTheme.put("inputBg", surface.darker());
            currentTheme.put("input", surface.darker());
            currentTheme.put("progressTrack", new Color(210, 210, 210));
        }

        putCommonColors();
        notifyListeners();
    }

    public static void applyDarkTheme() {
        currentPreset = ThemePreset.DARK;
        finalizeThemeColors(new Color(18, 18, 18), new Color(30, 30, 30), new Color(255, 193, 7));
    }

    public static void applyLightTheme() {
        currentPreset = ThemePreset.LIGHT;
        finalizeThemeColors(new Color(245, 245, 245), new Color(255, 255, 255), new Color(255, 152, 0));
    }

    public static void applyOceanTheme() {
        currentPreset = ThemePreset.OCEAN;
        finalizeThemeColors(new Color(15, 32, 39), new Color(32, 58, 67), new Color(0, 188, 212));
    }

    public static void applyForestTheme() {
        currentPreset = ThemePreset.FOREST;
        finalizeThemeColors(new Color(27, 38, 44), new Color(34, 61, 60), new Color(76, 175, 80));
    }

    public static void applyDraculaTheme() {
        currentPreset = ThemePreset.DRACULA;
        finalizeThemeColors(new Color(40, 42, 54), new Color(68, 71, 90), new Color(255, 121, 198));
    }

    public static void applySunsetTheme() {
        currentPreset = ThemePreset.SUNSET;
        finalizeThemeColors(new Color(35, 15, 45), new Color(60, 25, 75), new Color(255, 87, 34));
    }

    public static void applyLavenderTheme() {
        currentPreset = ThemePreset.LAVENDER;
        finalizeThemeColors(new Color(230, 220, 245), new Color(245, 240, 255), new Color(156, 39, 176));
    }

    public static void applyMaterialLightTheme() {
        currentPreset = ThemePreset.MATERIAL_LIGHT;
        finalizeThemeColors(new Color(255, 255, 255), new Color(242, 242, 242), new Color(33, 150, 243));
    }

    private static void putCommonColors() {
        currentTheme.put("success", new Color(76, 175, 80));
        currentTheme.put("danger", new Color(244, 67, 54));
        currentTheme.put("warning", new Color(255, 152, 0));
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
        forceThemeUI();
        switch (preset) {
            case DARK: applyDarkTheme(); break;
            case LIGHT: applyLightTheme(); break;
            case OCEAN: applyOceanTheme(); break;
            case FOREST: applyForestTheme(); break;
            case DRACULA: applyDraculaTheme(); break;
            case SUNSET: applySunsetTheme(); break;
            case LAVENDER: applyLavenderTheme(); break;
            case MATERIAL_LIGHT: applyMaterialLightTheme(); break;
            case CUSTOM: currentPreset = ThemePreset.CUSTOM; break;
            default: applyDarkTheme(); break;
        }

        for (Window window : Window.getWindows()) {
            applyToWindow(window);
        }
    }

    public static void applyToWindow(Window window) {
        if (window == null) return;
        if (window instanceof RootPaneContainer) {
            applyThemeRecursively(((RootPaneContainer) window).getContentPane());
        } else {
            applyThemeRecursively(window);
        }
        SwingUtilities.updateComponentTreeUI(window);
        window.repaint();
    }

    public static void setCustomColor(String key, Color color) {
        currentTheme.put(key, color);
        currentPreset = ThemePreset.CUSTOM;
        notifyListeners();
    }

    public static ThemePreset getCurrentPreset() { return currentPreset; }
    public static boolean isDark() { return isDarkMode; }
    public static void addThemeListener(Runnable listener) { listeners.add(listener); }
    private static void notifyListeners() {
        for (Runnable listener : listeners) listener.run();
    }

    public static void forceThemeUI() {
        UIManager.put("Panel.background", getColor("bg"));
        UIManager.put("Panel.foreground", getColor("textPrimary"));
        UIManager.put("Label.foreground", getColor("textPrimary"));
        UIManager.put("Button.background", getColor("surface"));
        UIManager.put("Button.foreground", getColor("textPrimary"));
        UIManager.put("TextField.background", getColor("inputBg"));
        UIManager.put("TextField.foreground", getColor("textPrimary"));
        UIManager.put("ComboBox.background", getColor("inputBg"));
        UIManager.put("ComboBox.foreground", getColor("textPrimary"));
        UIManager.put("Table.background", getColor("surface"));
        UIManager.put("Table.foreground", getColor("textPrimary"));
        UIManager.put("TableHeader.background", getColor("inputBg"));
        UIManager.put("TableHeader.foreground", getColor("textPrimary"));
        UIManager.put("CheckBox.background", getColor("bg"));
        UIManager.put("CheckBox.foreground", getColor("textPrimary"));
        UIManager.put("RadioButton.background", getColor("bg"));
        UIManager.put("RadioButton.foreground", getColor("textPrimary"));
        UIManager.put("OptionPane.background", getColor("surface"));
        UIManager.put("OptionPane.messageForeground", getColor("textPrimary"));
    }

    public static void applyThemeRecursively(Component comp) {
        if (comp == null) return;

        // Ẩn thanh cuộn dọc & tăng tốc độ cuộn
        if (comp instanceof JScrollPane) {
            JScrollPane sp = (JScrollPane) comp;
            sp.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
            sp.getVerticalScrollBar().setUnitIncrement(20);
            sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        }

        if (comp instanceof JPanel || comp instanceof JViewport) {
            if (comp.getName() == null || !comp.getName().startsWith("colorBox_")) {
                comp.setBackground(getColor("bg"));
            }
        }

        if (comp instanceof JTextField || comp instanceof JTextArea || comp instanceof JPasswordField) {
            comp.setBackground(getColor("inputBg"));
            comp.setForeground(getColor("textPrimary"));
            ((JTextComponent) comp).setCaretColor(getColor("accent"));
        } else if (comp instanceof JComboBox) {
            comp.setBackground(getColor("inputBg"));
            comp.setForeground(getColor("textPrimary"));
        } else if (comp instanceof JSpinner) {
            comp.setBackground(getColor("inputBg"));
            comp.setForeground(getColor("textPrimary"));
            JComponent editor = ((JSpinner) comp).getEditor();
            if (editor instanceof JSpinner.DefaultEditor) {
                JTextField tf = ((JSpinner.DefaultEditor) editor).getTextField();
                tf.setBackground(getColor("inputBg"));
                tf.setForeground(getColor("textPrimary"));
                tf.setCaretColor(getColor("accent"));
            }
        }

        if (comp instanceof JTable) {
            JTable table = (JTable) comp;
            table.setBackground(getColor("surface"));
            table.setForeground(getColor("textPrimary"));
            table.setGridColor(getColor("border"));
            if (table.getTableHeader() != null) {
                table.getTableHeader().setBackground(getColor("inputBg"));
                table.getTableHeader().setForeground(getColor("textPrimary"));
            }
        }

        if (comp instanceof JLabel) {
            if (comp.getForeground().getRGB() != getColor("accent").getRGB()) {
                comp.setForeground(getColor("textPrimary"));
            }
        }

        if (comp instanceof JCheckBox || comp instanceof JRadioButton) {
            comp.setBackground(getColor("bg"));
            comp.setForeground(getColor("textPrimary"));
            ((JToggleButton) comp).setOpaque(false);
        }

        if (comp instanceof JButton) {
            JButton btn = (JButton) comp;
            Color bg = btn.getBackground();
            if (bg != null && (bg.equals(getColor("accent")) || bg.equals(getColor("success")) ||
                    bg.equals(getColor("danger")) || bg.equals(getColor("warning")))) {
                btn.setForeground(getContrastColor(bg));
            } else {
                btn.setForeground(getColor("textPrimary"));
            }
        }

        if (comp instanceof Container) {
            for (Component child : ((Container) comp).getComponents()) {
                applyThemeRecursively(child);
            }
        }
    }
}