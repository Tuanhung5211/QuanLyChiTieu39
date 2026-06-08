package com.expensemanager.ui;

import com.expensemanager.util.PremiumManager;
import com.expensemanager.service.SessionManager;
import com.expensemanager.util.ThemeManager;
import javax.swing.*;
import java.awt.*;

public class SettingsPanel extends JPanel implements Themable {

    private MainFrame mainFrame;
    private boolean isVietnamese = true;

    private CardLayout subCardLayout;
    private JPanel subContentPanel;
    private JLabel lblMainTitle;
    private JButton btnAccountTab, btnConfigTab, btnCategoryTab, btnThemeTab;

    private AccountSettingsPanel accountSettingsPanel;
    private SystemConfigPanel systemConfigPanel;
    private CategoryManagerPanel categoryManagerPanel;
    private JPanel themePanel;

    public SettingsPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        if (mainFrame != null) this.isVietnamese = mainFrame.isVietnamese();

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 35, 20, 35));

        initComponents();
        updateLanguageText();
        applyTheme();
    }

    public void applyTheme() {
        setBackground(ThemeManager.getColor("bg"));
        if (lblMainTitle != null) lblMainTitle.setForeground(ThemeManager.getColor("textPrimary"));
        if (subContentPanel != null) subContentPanel.setBackground(ThemeManager.getColor("bg"));
        // Áp dụng theme cho các panel con
        if (accountSettingsPanel != null) accountSettingsPanel.applyTheme();
        if (systemConfigPanel != null) systemConfigPanel.applyTheme();
        if (categoryManagerPanel != null) categoryManagerPanel.applyTheme();
        if (themePanel != null) refreshThemeUI(themePanel);
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

        accountSettingsPanel = new AccountSettingsPanel(mainFrame);
        systemConfigPanel = new SystemConfigPanel(mainFrame);
        categoryManagerPanel = new CategoryManagerPanel(mainFrame);
        themePanel = createThemePanel();

        subCardLayout = new CardLayout();
        subContentPanel = new JPanel(subCardLayout);
        subContentPanel.setOpaque(false);

        subContentPanel.add(createResponsiveWrapper(accountSettingsPanel), "account");
        subContentPanel.add(createResponsiveWrapper(systemConfigPanel), "config");
        subContentPanel.add(createResponsiveWrapper(categoryManagerPanel), "category");
        subContentPanel.add(createResponsiveWrapper(themePanel), "theme");

        bodyContainer.add(subContentPanel, BorderLayout.CENTER);
        add(bodyContainer, BorderLayout.CENTER);
    }

    private JScrollPane createResponsiveWrapper(JPanel targetPanel) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(targetPanel, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(wrapper);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        return scrollPane;
    }

    private JPanel createThemePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15,15,15,15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        JLabel lblThemeTitle = new JLabel(isVietnamese ? "Tùy chỉnh giao diện" : "Theme Customization");
        lblThemeTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblThemeTitle.setName("themeTitle");
        panel.add(lblThemeTitle, gbc);

        JPanel btnThemePreset = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        btnThemePreset.setOpaque(false);
        JButton btnDark = new JButton(isVietnamese ? "Tối" : "Dark");
        JButton btnLight = new JButton(isVietnamese ? "Sáng" : "Light");
        JButton btnCustom = new JButton(isVietnamese ? "Tùy chỉnh màu" : "Custom Colors");
        btnDark.addActionListener(e -> {
            ThemeManager.setTheme(ThemeManager.ThemePreset.DARK);
        });
        btnLight.addActionListener(e -> {
            ThemeManager.setTheme(ThemeManager.ThemePreset.LIGHT);
        });
        btnCustom.addActionListener(e -> {
            if (!PremiumManager.isPremium(SessionManager.getCurrentUserId())) {
                JOptionPane.showMessageDialog(this, isVietnamese ? "Tính năng này yêu cầu Premium!" : "This feature requires Premium!");
                return;
            }
            new ThemeCustomizerDialog(mainFrame, isVietnamese).setVisible(true);
        });
        btnThemePreset.add(btnDark);
        btnThemePreset.add(btnLight);
        btnThemePreset.add(btnCustom);
        panel.add(btnThemePreset, gbc);

        JLabel lblPreview = new JLabel(isVietnamese ? "Xem trước màu sắc:" : "Color preview:");
        lblPreview.setName("previewLabel");
        panel.add(lblPreview, gbc);

        JPanel previewPanel = new JPanel(new GridLayout(2,3,10,10));
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
        panel.setBackground(ThemeManager.getColor("bg"));

        for (Component comp : panel.getComponents()) {
            if (comp instanceof JLabel) {
                JLabel lbl = (JLabel) comp;
                String name = lbl.getName();
                if (name == null || !name.equals("themeTitle")) {
                    lbl.setForeground(ThemeManager.getColor("textPrimary"));
                }
            } else if (comp instanceof JPanel) {
                JPanel subPanel = (JPanel) comp;
                String name = subPanel.getName();
                if (name != null && name.equals("previewPanel")) {
                    // Update color boxes
                    for (Component colorComp : subPanel.getComponents()) {
                        if (colorComp instanceof JPanel) {
                            JPanel colorBox = (JPanel) colorComp;
                            String boxName = colorBox.getName();
                            if (boxName != null && boxName.startsWith("colorBox_")) {
                                String colorKey = boxName.substring("colorBox_".length());
                                colorBox.setBackground(ThemeManager.getColor(colorKey));
                                colorBox.setBorder(BorderFactory.createLineBorder(ThemeManager.getColor("border")));

                                // Update text color in the label
                                for (Component boxChild : colorBox.getComponents()) {
                                    if (boxChild instanceof JLabel) {
                                        ((JLabel) boxChild).setForeground(ThemeManager.getColor("textPrimary"));
                                    }
                                }
                            }
                        }
                    }
                } else {
                    subPanel.setBackground(ThemeManager.getColor("bg"));
                }
            }
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
        Color secondary = ThemeManager.getColor("textSecondary");
        Color inputBg = ThemeManager.getColor("input");

        btnAccountTab.setBackground(inputBg); btnAccountTab.setForeground(secondary);
        btnConfigTab.setBackground(inputBg); btnConfigTab.setForeground(secondary);
        btnCategoryTab.setBackground(inputBg); btnCategoryTab.setForeground(secondary);
        btnThemeTab.setBackground(inputBg); btnThemeTab.setForeground(secondary);

        activeBtn.setBackground(ThemeManager.getColor("accent"));
        activeBtn.setForeground(ThemeManager.getColor("bg"));
    }

    public void refreshData() {
        if (accountSettingsPanel != null) accountSettingsPanel.refreshData();
        if (categoryManagerPanel != null) categoryManagerPanel.refreshCategories();
    }

    public void updateLanguageText() {
        if (mainFrame != null) this.isVietnamese = mainFrame.isVietnamese();

        int panelWidth = this.getWidth();
        if (panelWidth <= 0 && mainFrame != null) panelWidth = mainFrame.getWidth() - 240;
        int fluidWidth = panelWidth - 220 - 25 - 70;
        if (fluidWidth < 500) fluidWidth = 560;

        if (isVietnamese) {
            lblMainTitle.setText("Cài đặt hệ thống");
            btnAccountTab.setText("Thông tin cá nhân");
            btnConfigTab.setText("Cấu hình hệ thống");
            btnCategoryTab.setText("Quản lý danh mục");
            btnThemeTab.setText("Giao diện");
        } else {
            lblMainTitle.setText("System Settings");
            btnAccountTab.setText("Account Settings");
            btnConfigTab.setText("System Configuration");
            btnCategoryTab.setText("Category Manager");
            btnThemeTab.setText("Theme");
        }

        if (accountSettingsPanel != null) accountSettingsPanel.updateResponsiveLayout(isVietnamese, fluidWidth);
        if (systemConfigPanel != null) systemConfigPanel.updateResponsiveLayout(isVietnamese, fluidWidth);
        if (categoryManagerPanel != null) categoryManagerPanel.updateResponsiveLayout(isVietnamese, fluidWidth);
    }

    public void updateLanguageAndResponsive(boolean isVN, int targetFrameWidth) {
        this.isVietnamese = isVN;
        int panelWidth = targetFrameWidth - 240;
        int fluidWidth = panelWidth - 220 - 25 - 70;
        if (fluidWidth < 500) fluidWidth = 560;

        if (isVN) {
            lblMainTitle.setText("Cài đặt hệ thống");
            btnAccountTab.setText("Thông tin cá nhân");
            btnConfigTab.setText("Cấu hình hệ thống");
            btnCategoryTab.setText("Quản lý danh mục");
            btnThemeTab.setText("Giao diện");
        } else {
            lblMainTitle.setText("System Settings");
            btnAccountTab.setText("Account Settings");
            btnConfigTab.setText("System Configuration");
            btnCategoryTab.setText("Category Manager");
            btnThemeTab.setText("Theme");
        }

        if (accountSettingsPanel != null) {
            accountSettingsPanel.updateResponsiveLayout(isVN, fluidWidth);
            accountSettingsPanel.refreshData();
        }
        if (systemConfigPanel != null) systemConfigPanel.updateResponsiveLayout(isVN, fluidWidth);
        if (categoryManagerPanel != null) {
            categoryManagerPanel.updateResponsiveLayout(isVN, fluidWidth);
            categoryManagerPanel.refreshCategories();
        }
        if (themePanel != null) refreshThemeUI(themePanel);

        this.revalidate();
        this.repaint();
    }
}