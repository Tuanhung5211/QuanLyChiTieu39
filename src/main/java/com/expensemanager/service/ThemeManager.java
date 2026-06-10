package com.expensemanager.service;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.Color;
import java.awt.Component;
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

    public static Color getContrastColor(Color bg) {
        if (bg == null) return Color.BLACK;
        double luminance = (0.299 * bg.getRed() + 0.587 * bg.getGreen() + 0.114 * bg.getBlue()) / 255.0;
        return luminance < 0.5 ? new Color(245, 245, 245) : new Color(32, 33, 36);
    }

    private static void finalizeThemeColors(Color bg, Color surface, Color accent) {
        currentTheme.put("bg", bg);
        currentTheme.put("surface", surface);
        currentTheme.put("accent", accent);

        double luminance = (0.299 * bg.getRed() + 0.587 * bg.getGreen() + 0.114 * bg.getBlue()) / 255.0;
        isDarkMode = luminance < 0.5;

        if (isDarkMode) {
            currentTheme.put("textPrimary", new Color(245, 245, 245));
            currentTheme.put("textSecondary", new Color(170, 170, 170));
            currentTheme.put("border", surface.brighter().brighter());
            currentTheme.put("inputBg", bg.brighter());
            currentTheme.put("input", bg.brighter());
            currentTheme.put("progressTrack", new Color(65, 65, 65));
        } else {
            currentTheme.put("textPrimary", new Color(32, 33, 36));
            currentTheme.put("textSecondary", new Color(95, 99, 104));
            currentTheme.put("border", new Color(218, 220, 224));
            currentTheme.put("inputBg", new Color(241, 243, 244));
            currentTheme.put("input", new Color(241, 243, 244));
            currentTheme.put("progressTrack", new Color(232, 234, 237));
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
        finalizeThemeColors(new Color(250, 250, 250), new Color(255, 255, 255), new Color(26, 115, 232));
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
        finalizeThemeColors(new Color(255, 255, 255), new Color(248, 249, 250), new Color(33, 150, 243));
    }

    private static void putCommonColors() {
        currentTheme.put("success", new Color(52, 168, 83));
        currentTheme.put("danger", new Color(234, 67, 53));
        currentTheme.put("warning", new Color(251, 188, 5));

        currentTheme.put("chart0", new Color(66, 133, 244));
        currentTheme.put("chart1", new Color(234, 67, 53));
        currentTheme.put("chart2", new Color(251, 188, 5));
        currentTheme.put("chart3", new Color(52, 168, 83));
        currentTheme.put("chart4", new Color(255, 112, 67));
        currentTheme.put("chart5", new Color(171, 71, 188));
        currentTheme.put("chart6", new Color(38, 166, 154));
        currentTheme.put("chart7", new Color(141, 110, 99));
        currentTheme.put("chart8", new Color(120, 144, 156));
    }

    public static void setTheme(ThemePreset preset) {
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
        forceThemeUI();
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

    public static ThemePreset getCurrentPreset() { return currentPreset; }
    public static boolean isDark() { return isDarkMode; }
    public static void addThemeListener(Runnable listener) { listeners.add(listener); }
    private static void notifyListeners() { for (Runnable listener : listeners) { listener.run(); } }

    // ------------------ UI TUỲ CHỈNH CHO COMBOBOX ------------------
    public static class ThemeComboBoxUI extends BasicComboBoxUI {
        @Override
        protected void installDefaults() {
            super.installDefaults();
            // Loại bỏ viền mặc định và áp dụng viền theme
            comboBox.setBorder(BorderFactory.createLineBorder(ThemeManager.getColor("border"), 1, true));
            comboBox.setBackground(ThemeManager.getColor("input"));
            comboBox.setForeground(ThemeManager.getColor("textPrimary"));
            // Đảm bảo renderer dùng để vẽ item cũng đúng màu
            comboBox.setRenderer(new ThemeComboBoxRenderer());
        }
    }

    // Renderer cho popup (giữ nguyên từ trước, đảm bảo đồng bộ)
    public static class ThemeComboBoxRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                                                      int index, boolean isSelected,
                                                      boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            label.setBackground(isSelected ? ThemeManager.getColor("accent") : ThemeManager.getColor("input"));
            label.setForeground(isSelected ?
                    ThemeManager.getContrastColor(ThemeManager.getColor("accent")) :
                    ThemeManager.getColor("textPrimary"));
            label.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
            return label;
        }
    }

    public static void forceThemeUI() {
        // Đăng ký UI class cho tất cả JComboBox
        UIManager.put("ComboBoxUI", ThemeComboBoxUI.class.getName());

        // Các thiết lập khác giữ nguyên
        UIManager.put("Panel.background", getColor("bg"));
        UIManager.put("Label.foreground", getColor("textPrimary"));

        UIManager.put("Button.focus", new Color(0, 0, 0, 0));
        UIManager.put("ToggleButton.focus", new Color(0, 0, 0, 0));
        UIManager.put("CheckBox.focus", new Color(0, 0, 0, 0));
        UIManager.put("TabbedPane.focus", new Color(0, 0, 0, 0));

        UIManager.put("Button.background", getColor("surface"));
        UIManager.put("Button.foreground", getColor("textPrimary"));
        UIManager.put("TextField.background", getColor("inputBg"));
        UIManager.put("TextField.foreground", getColor("textPrimary"));
        // ComboBox background và foreground vẫn được giữ lại để các phần khác tham chiếu
        UIManager.put("ComboBox.background", getColor("inputBg"));
        UIManager.put("ComboBox.foreground", getColor("textPrimary"));

        UIManager.put("Table.background", getColor("surface"));
        UIManager.put("Table.foreground", getColor("textPrimary"));
        UIManager.put("Table.gridColor", getColor("border"));
        UIManager.put("Table.showVerticalLines", false);
        UIManager.put("TableHeader.background", getColor("bg"));
        UIManager.put("TableHeader.foreground", getColor("textSecondary"));

        UIManager.put("CheckBox.background", getColor("bg"));
        UIManager.put("CheckBox.foreground", getColor("textPrimary"));
        UIManager.put("RadioButton.background", getColor("bg"));
        UIManager.put("RadioButton.foreground", getColor("textPrimary"));
        UIManager.put("OptionPane.background", getColor("bg"));
        UIManager.put("OptionPane.messageForeground", getColor("textPrimary"));
    }

    public static void applyThemeRecursively(java.awt.Component comp) {
        if (comp == null) return;

        if (comp instanceof javax.swing.JPanel || comp instanceof javax.swing.JScrollPane || comp instanceof javax.swing.JViewport) {
            if (comp.getName() == null || !comp.getName().startsWith("colorBox_")) {
                comp.setBackground(getColor("bg"));
            }
        }

        if (comp instanceof javax.swing.JTextField || comp instanceof javax.swing.JTextArea || comp instanceof javax.swing.JPasswordField) {
            comp.setBackground(getColor("inputBg"));
            comp.setForeground(getColor("textPrimary"));
            ((javax.swing.text.JTextComponent) comp).setCaretColor(getColor("accent"));
        } else if (comp instanceof javax.swing.JComboBox) {
            // Với UI mới, không cần set border ở đây nữa vì UI sẽ lo, nhưng cứ set lại cho chắc
            comp.setBackground(getColor("inputBg"));
            comp.setForeground(getColor("textPrimary"));
            ((JComboBox<?>) comp).setRenderer(new ThemeComboBoxRenderer());
        }

        if (comp instanceof javax.swing.JTable) {
            javax.swing.JTable table = (javax.swing.JTable) comp;
            table.setBackground(getColor("surface"));
            table.setForeground(getColor("textPrimary"));
            table.setGridColor(getColor("border"));
            table.setIntercellSpacing(new java.awt.Dimension(0, 0));
            table.setRowHeight(40);
            if (table.getTableHeader() != null) {
                table.getTableHeader().setBackground(getColor("bg"));
                table.getTableHeader().setForeground(getColor("textSecondary"));
                table.getTableHeader().setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
                table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, getColor("border")));
            }
        }

        if (comp instanceof javax.swing.JLabel) {
            if (comp.getForeground().getRGB() != getColor("accent").getRGB()) {
                comp.setForeground(getColor("textPrimary"));
            }
        }

        if (comp instanceof javax.swing.JCheckBox || comp instanceof javax.swing.JRadioButton) {
            comp.setBackground(getColor("bg"));
            comp.setForeground(getColor("textPrimary"));
            ((javax.swing.JToggleButton) comp).setOpaque(false);
        }

        if (comp instanceof java.awt.Container) {
            for (java.awt.Component child : ((java.awt.Container) comp).getComponents()) {
                applyThemeRecursively(child);
            }
        }
    }
}