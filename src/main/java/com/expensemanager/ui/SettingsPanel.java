package com.expensemanager.ui;

import com.expensemanager.service.PremiumManager;
import com.expensemanager.service.SessionManager;
import com.expensemanager.service.ThemeManager;
import com.expensemanager.util.ConfigLocalStorage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;

public class SettingsPanel extends JPanel {

    private MainFrame mainFrame;
    private boolean isVietnamese = true;

    private CardLayout subCardLayout;
    private JPanel subContentPanel;
    private JLabel lblMainTitle;
    private JButton btnAccountTab, btnConfigTab, btnCategoryTab, btnThemeTab;
    private JButton activeSubBtn;

    private AccountSettingsPanel accountSettingsPanel;
    private SystemConfigPanel systemConfigPanel;
    private CategoryManagerPanel categoryManagerPanel;
    private JPanel themePanel;

    private JComboBox<String> cmbThemePreset;
    private JButton btnCustom;
    private JLabel lblThemeTitle;
    private JLabel lblPreview;

    public SettingsPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        if (mainFrame != null) this.isVietnamese = mainFrame.isVietnamese();

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 35, 20, 35));

        initComponents();
        updateLanguageText();          // chỉ văn bản
        updateLayoutForAllChildren();  // áp dụng ngôn ngữ cho các panel con (responsive)
        applyTheme();
    }

    private void initComponents() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));

        lblMainTitle = new JLabel();
        lblMainTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        headerPanel.add(lblMainTitle, BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);

        JPanel bodyContainer = new JPanel(new BorderLayout(25, 0));
        bodyContainer.setOpaque(false);
        bodyContainer.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        // Sidebar
        JPanel sidebarPanel = new JPanel();
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.getColor("border"), 1, true),
                BorderFactory.createEmptyBorder(15, 12, 15, 12)
        ));
        sidebarPanel.setPreferredSize(new Dimension(220, 0));

        btnAccountTab = createSubNavButton("", true);
        btnConfigTab = createSubNavButton("", false);
        btnCategoryTab = createSubNavButton("", false);
        btnThemeTab = createSubNavButton("", false);

        activeSubBtn = btnAccountTab;

        btnAccountTab.addActionListener(e -> switchSubTab("account", btnAccountTab));
        btnConfigTab.addActionListener(e -> switchSubTab("config", btnConfigTab));
        btnCategoryTab.addActionListener(e -> switchSubTab("category", btnCategoryTab));
        btnThemeTab.addActionListener(e -> switchSubTab("theme", btnThemeTab));

        sidebarPanel.add(btnAccountTab);
        sidebarPanel.add(Box.createVerticalStrut(10));
        sidebarPanel.add(btnConfigTab);
        sidebarPanel.add(Box.createVerticalStrut(10));
        sidebarPanel.add(btnCategoryTab);
        sidebarPanel.add(Box.createVerticalStrut(10));
        sidebarPanel.add(btnThemeTab);
        sidebarPanel.add(Box.createVerticalGlue());

        bodyContainer.add(sidebarPanel, BorderLayout.WEST);

        // Sub-panels
        accountSettingsPanel = new AccountSettingsPanel(mainFrame);
        systemConfigPanel = new SystemConfigPanel(mainFrame);
        categoryManagerPanel = new CategoryManagerPanel(mainFrame);
        themePanel = createThemePanel();

        subCardLayout = new CardLayout();
        subContentPanel = new JPanel(subCardLayout);
        subContentPanel.setOpaque(false);

        subContentPanel.add(wrapInNorthPanel(accountSettingsPanel), "account");
        subContentPanel.add(wrapInNorthPanel(systemConfigPanel), "config");
        subContentPanel.add(wrapInNorthPanel(categoryManagerPanel), "category");
        subContentPanel.add(wrapInNorthPanel(themePanel), "theme");

        JScrollPane scrollPane = new JScrollPane(subContentPanel);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        bodyContainer.add(scrollPane, BorderLayout.CENTER);
        add(bodyContainer, BorderLayout.CENTER);
    }

    private JPanel wrapInNorthPanel(JPanel panel) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(panel, BorderLayout.NORTH);
        return wrapper;
    }

    private JPanel createThemePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        lblThemeTitle = new JLabel();
        lblThemeTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblThemeTitle.setName("themeTitle");
        panel.add(lblThemeTitle, gbc);

        JPanel btnThemePreset = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        btnThemePreset.setOpaque(false);

        cmbThemePreset = new JComboBox<>();
        cmbThemePreset.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        cmbThemePreset.setPreferredSize(new Dimension(230, 38));

        cmbThemePreset.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                int index = cmbThemePreset.getSelectedIndex();
                ThemeManager.ThemePreset selectedPreset = ThemeManager.ThemePreset.DARK;
                switch (index) {
                    case 0: selectedPreset = ThemeManager.ThemePreset.DARK; break;
                    case 1: selectedPreset = ThemeManager.ThemePreset.LIGHT; break;
                    case 2: selectedPreset = ThemeManager.ThemePreset.OCEAN; break;
                    case 3: selectedPreset = ThemeManager.ThemePreset.FOREST; break;
                    case 4: selectedPreset = ThemeManager.ThemePreset.DRACULA; break;
                    case 5: selectedPreset = ThemeManager.ThemePreset.SUNSET; break;
                    case 6: selectedPreset = ThemeManager.ThemePreset.LAVENDER; break;
                    case 7: selectedPreset = ThemeManager.ThemePreset.MATERIAL_LIGHT; break;
                }
                ThemeManager.setTheme(selectedPreset);
                ConfigLocalStorage.saveThemePreset(selectedPreset.name());
            }
        });

        btnCustom = new JButton();
        btnCustom.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCustom.setFocusPainted(false);
        btnCustom.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCustom.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        btnCustom.addActionListener(e -> {
            if (!PremiumManager.isPremium(SessionManager.getCurrentUserId())) {
                JOptionPane.showMessageDialog(this,
                        isVietnamese ? "Tính năng này yêu cầu Premium!" : "This feature requires Premium!",
                        isVietnamese ? "Thông báo" : "Info", JOptionPane.INFORMATION_MESSAGE);
            } else {
                ThemeCustomizerDialog dialog = new ThemeCustomizerDialog(
                        (Frame) SwingUtilities.getWindowAncestor(this), isVietnamese);
                dialog.setVisible(true);
                refreshThemeUI(themePanel);
            }
        });

        btnThemePreset.add(cmbThemePreset);
        btnThemePreset.add(btnCustom);
        panel.add(btnThemePreset, gbc);

        lblPreview = new JLabel();
        lblPreview.setName("previewLabel");
        panel.add(lblPreview, gbc);

        JPanel previewPanel = new JPanel(new GridLayout(2, 3, 10, 10));
        previewPanel.setOpaque(false);
        previewPanel.setName("previewPanel");

        previewPanel.add(createColorBox("bg", "Background"));
        previewPanel.add(createColorBox("surface", "Surface"));
        previewPanel.add(createColorBox("input", "Input"));
        previewPanel.add(createColorBox("textPrimary", "Text Primary"));
        previewPanel.add(createColorBox("textSecondary", "Text Secondary"));
        previewPanel.add(createColorBox("accent", "Accent"));

        panel.add(previewPanel, gbc);
        return panel;
    }

    private JPanel createColorBox(String key, String name) {
        JPanel box = new JPanel(new BorderLayout());
        box.setBackground(ThemeManager.getColor(key));
        box.setBorder(BorderFactory.createLineBorder(ThemeManager.getColor("border")));
        box.setName("colorBox_" + key);

        JLabel lbl = new JLabel(name, SwingConstants.CENTER);
        lbl.setForeground(ThemeManager.getColor("textPrimary"));
        box.add(lbl, BorderLayout.CENTER);
        return box;
    }

    private void refreshThemeUI(JPanel panel) {
        if (panel == null) return;
        ThemeManager.applyThemeRecursively(panel);
        for (Component comp : panel.getComponents()) {
            if (comp instanceof JPanel && "previewPanel".equals(comp.getName())) {
                for (Component colorComp : ((JPanel) comp).getComponents()) {
                    if (colorComp instanceof JPanel) {
                        JPanel colorBox = (JPanel) colorComp;
                        String boxName = colorBox.getName();
                        if (boxName != null && boxName.startsWith("colorBox_")) {
                            String colorKey = boxName.substring("colorBox_".length());
                            colorBox.setBackground(ThemeManager.getColor(colorKey));
                            for (Component boxChild : colorBox.getComponents()) {
                                if (boxChild instanceof JLabel) {
                                    boxChild.setForeground(ThemeManager.getColor("textPrimary"));
                                }
                            }
                        }
                    }
                }
            }
        }

        if (cmbThemePreset != null) {
            cmbThemePreset.setBackground(ThemeManager.getColor("input"));
            cmbThemePreset.setForeground(ThemeManager.getColor("textPrimary"));
            cmbThemePreset.setBorder(BorderFactory.createLineBorder(ThemeManager.getColor("border")));
        }
        if (btnCustom != null) {
            btnCustom.setBackground(ThemeManager.getColor("accent"));
            btnCustom.setForeground(ThemeManager.getContrastColor(ThemeManager.getColor("accent")));
        }

        panel.revalidate();
        panel.repaint();
    }

    private JButton createSubNavButton(String text, boolean isActive) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        if (isActive) {
            btn.setBackground(ThemeManager.getColor("accent"));
            btn.setForeground(ThemeManager.getColor("bg"));
        } else {
            btn.setBackground(ThemeManager.getColor("input"));
            btn.setForeground(ThemeManager.getColor("textSecondary"));
        }
        return btn;
    }

    private void switchSubTab(String targetCard, JButton activeBtn) {
        subCardLayout.show(subContentPanel, targetCard);
        this.activeSubBtn = activeBtn;
        updateSubNavButtonsTheme();
    }

    private void updateSubNavButtonsTheme() {
        Color secondary = ThemeManager.getColor("textSecondary");
        Color inputBg = ThemeManager.getColor("input");

        btnAccountTab.setBackground(inputBg);
        btnAccountTab.setForeground(secondary);
        btnConfigTab.setBackground(inputBg);
        btnConfigTab.setForeground(secondary);
        btnCategoryTab.setBackground(inputBg);
        btnCategoryTab.setForeground(secondary);
        btnThemeTab.setBackground(inputBg);
        btnThemeTab.setForeground(secondary);

        if (activeSubBtn != null) {
            activeSubBtn.setBackground(ThemeManager.getColor("accent"));
            activeSubBtn.setForeground(ThemeManager.getColor("bg"));
        }
    }

    public void applyTheme() {
        ThemeManager.applyThemeRecursively(this);

        if (lblMainTitle != null) lblMainTitle.setForeground(ThemeManager.getColor("textPrimary"));
        updateSubNavButtonsTheme();

        if (accountSettingsPanel != null) accountSettingsPanel.applyTheme();
        if (systemConfigPanel != null) systemConfigPanel.applyTheme();
        if (categoryManagerPanel != null) categoryManagerPanel.applyTheme();
        if (themePanel != null) refreshThemeUI(themePanel);
    }

    public void refreshData() {
        if (accountSettingsPanel != null) accountSettingsPanel.refreshData();
        if (categoryManagerPanel != null) categoryManagerPanel.refreshCategories();
    }

    // Chỉ cập nhật văn bản, không can thiệp layout
    public void updateLanguageText() {
        if (mainFrame != null) this.isVietnamese = mainFrame.isVietnamese();

        if (isVietnamese) {
            lblMainTitle.setText("Cài đặt hệ thống");
            btnAccountTab.setText("Thông tin cá nhân");
            btnConfigTab.setText("Cấu hình hệ thống");
            btnCategoryTab.setText("Quản lý danh mục");
            btnThemeTab.setText("Giao diện");

            if (lblThemeTitle != null) lblThemeTitle.setText("Tùy chỉnh giao diện");
            if (lblPreview != null) lblPreview.setText("Xem trước màu sắc:");
            if (btnCustom != null) btnCustom.setText("Tùy chỉnh màu (Premium)");

            if (cmbThemePreset != null) {
                java.awt.event.ItemListener[] listeners = cmbThemePreset.getItemListeners();
                for (java.awt.event.ItemListener l : listeners) cmbThemePreset.removeItemListener(l);

                int selected = cmbThemePreset.getSelectedIndex();
                cmbThemePreset.setModel(new DefaultComboBoxModel<>(new String[]{
                        "Giao diện Tối (Dark)", "Giao diện Sáng (Light)", "Xanh Đại Dương (Ocean)",
                        "Xanh Lục Bảo (Forest)", "Hồng Màn Đêm (Dracula)", "Hoàng hôn (Sunset)",
                        "Tím oải hương (Lavender)", "Material Light"
                }));
                if (selected >= 0 && selected < cmbThemePreset.getItemCount()) {
                    cmbThemePreset.setSelectedIndex(selected);
                }

                for (java.awt.event.ItemListener l : listeners) cmbThemePreset.addItemListener(l);
            }
        } else {
            lblMainTitle.setText("System Settings");
            btnAccountTab.setText("Account Settings");
            btnConfigTab.setText("System Configuration");
            btnCategoryTab.setText("Category Manager");
            btnThemeTab.setText("Theme");

            if (lblThemeTitle != null) lblThemeTitle.setText("Theme Customization");
            if (lblPreview != null) lblPreview.setText("Color preview:");
            if (btnCustom != null) btnCustom.setText("Custom Colors (Premium)");

            if (cmbThemePreset != null) {
                java.awt.event.ItemListener[] listeners = cmbThemePreset.getItemListeners();
                for (java.awt.event.ItemListener l : listeners) cmbThemePreset.removeItemListener(l);

                int selected = cmbThemePreset.getSelectedIndex();
                cmbThemePreset.setModel(new DefaultComboBoxModel<>(new String[]{
                        "Dark Theme", "Light Theme", "Ocean Blue", "Emerald Forest",
                        "Dracula Night", "Sunset", "Lavender", "Material Light"
                }));
                if (selected >= 0 && selected < cmbThemePreset.getItemCount()) {
                    cmbThemePreset.setSelectedIndex(selected);
                }

                for (java.awt.event.ItemListener l : listeners) cmbThemePreset.addItemListener(l);
            }
        }
    }

    // Cập nhật layout/ngôn ngữ cho các panel con (dùng khi đổi ngôn ngữ hoặc kích thước)
    private void updateLayoutForAllChildren() {
        if (accountSettingsPanel != null) accountSettingsPanel.updateResponsiveLayout(isVietnamese);
        if (systemConfigPanel != null) systemConfigPanel.updateResponsiveLayout(isVietnamese);
        if (categoryManagerPanel != null) categoryManagerPanel.updateResponsiveLayout(isVietnamese);
    }

    public void updateLanguageAndResponsive(boolean isVN, int targetFrameWidth) {
        this.isVietnamese = isVN;
        updateLanguageText();          // cập nhật văn bản cho SettingsPanel
        updateLayoutForAllChildren();  // cập nhật văn bản/layout cho các panel con

        if (themePanel != null) refreshThemeUI(themePanel);

        this.revalidate();
        this.repaint();
    }
}