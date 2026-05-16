package com.expensemanager.ui;

import com.expensemanager.service.SessionManager;
import javax.swing.*;
import java.awt.*;

public class SettingsPanel extends JPanel {
    private MainFrame mainFrame;
    private boolean isVietnamese = true;

    private CardLayout subCardLayout;
    private JPanel subContentPanel;
    private JLabel lblMainTitle;
    private JButton btnAccountTab, btnLanguageTab, btnCategoryTab, btnSizeTab;

    private AccountSettingsPanel accountSettingsPanel;
    private LanguageSettingsPanel languageSettingsPanel;
    private WindowSizeSettingsPanel windowSizeSettingsPanel;
    private AddCategoryPanel addCategoryPanel;

    public SettingsPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        if (mainFrame != null) this.isVietnamese = mainFrame.isVietnamese();

        setLayout(new BorderLayout());
        setBackground(new Color(18, 18, 18));
        setBorder(BorderFactory.createEmptyBorder(20, 35, 20, 35));

        // --- KHU VỰC HEADER TRÊN CÙNG ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));

        lblMainTitle = new JLabel();
        lblMainTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblMainTitle.setForeground(new Color(240, 240, 240));
        headerPanel.add(lblMainTitle, BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);

        // --- KHU VỰC THÂN ĐIỀU HƯỚNG CHÍNH ---
        JPanel bodyContainer = new JPanel(new BorderLayout(25, 0));
        bodyContainer.setOpaque(false);
        bodyContainer.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JPanel sidebarPanel = new JPanel();
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBackground(new Color(30, 30, 30));
        sidebarPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(45, 45, 45), 1, true),
                BorderFactory.createEmptyBorder(15, 12, 15, 12)
        ));
        sidebarPanel.setPreferredSize(new Dimension(220, 0));

        btnAccountTab = createSubNavButton("", true);
        btnLanguageTab = createSubNavButton("", false);
        btnSizeTab = createSubNavButton("", false);
        btnCategoryTab = createSubNavButton("", false);

        btnAccountTab.addActionListener(e -> switchSubTab("account", btnAccountTab));
        btnLanguageTab.addActionListener(e -> switchSubTab("language", btnLanguageTab));
        btnSizeTab.addActionListener(e -> switchSubTab("size", btnSizeTab));
        btnCategoryTab.addActionListener(e -> switchSubTab("category", btnCategoryTab));

        sidebarPanel.add(btnAccountTab);
        sidebarPanel.add(Box.createVerticalStrut(10));
        sidebarPanel.add(btnLanguageTab);
        sidebarPanel.add(Box.createVerticalStrut(10));
        sidebarPanel.add(btnSizeTab);
        sidebarPanel.add(Box.createVerticalStrut(10));
        sidebarPanel.add(btnCategoryTab);
        sidebarPanel.add(Box.createVerticalGlue());
        bodyContainer.add(sidebarPanel, BorderLayout.WEST);

        accountSettingsPanel = new AccountSettingsPanel(mainFrame);
        languageSettingsPanel = new LanguageSettingsPanel(mainFrame);
        windowSizeSettingsPanel = new WindowSizeSettingsPanel(mainFrame);
        addCategoryPanel = new AddCategoryPanel(mainFrame);

        subCardLayout = new CardLayout();
        subContentPanel = new JPanel(subCardLayout);
        subContentPanel.setOpaque(false);
        subContentPanel.add(createResponsiveWrapper(accountSettingsPanel), "account");
        subContentPanel.add(createResponsiveWrapper(languageSettingsPanel), "language");
        subContentPanel.add(createResponsiveWrapper(windowSizeSettingsPanel), "size");
        subContentPanel.add(createResponsiveWrapper(addCategoryPanel), "category");

        JScrollPane scrollPane = new JScrollPane(subContentPanel);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        bodyContainer.add(scrollPane, BorderLayout.CENTER);

        add(bodyContainer, BorderLayout.CENTER);

        updateLanguageText();
    }

    private JPanel createResponsiveWrapper(JPanel targetPanel) {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1.0; gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        wrapper.add(targetPanel, gbc);

        gbc.gridy = 1; gbc.weighty = 1.0; gbc.fill = GridBagConstraints.BOTH;
        JPanel filler = new JPanel(); filler.setOpaque(false);
        wrapper.add(filler, gbc);
        return wrapper;
    }

    public void updateLanguageText() {
        if (mainFrame != null) this.isVietnamese = mainFrame.isVietnamese();

        int panelWidth = this.getWidth();
        if (panelWidth <= 0 && mainFrame != null) {
            panelWidth = mainFrame.getWidth() - 240;
        }
        int fluidWidth = panelWidth - 220 - 25 - 70;
        if (fluidWidth < 500) fluidWidth = 560;

        if (isVietnamese) {
            lblMainTitle.setText("Cài đặt hệ thống");
            btnAccountTab.setText("Thông tin tài khoản");
            btnLanguageTab.setText("Ngôn ngữ");
            btnSizeTab.setText("Kích thước cửa sổ");
            btnCategoryTab.setText("Thêm Danh mục");
        } else {
            lblMainTitle.setText("System Settings");
            btnAccountTab.setText("Account Settings");
            btnLanguageTab.setText("Language");
            btnSizeTab.setText("Window Size");
            btnCategoryTab.setText("Add Category");
        }

        if (accountSettingsPanel != null) accountSettingsPanel.updateResponsiveLayout(isVietnamese, fluidWidth);
        if (languageSettingsPanel != null) languageSettingsPanel.updateResponsiveLayout(isVietnamese, fluidWidth);
        if (windowSizeSettingsPanel != null) windowSizeSettingsPanel.updateResponsiveLayout(isVietnamese, fluidWidth);
        if (addCategoryPanel != null) addCategoryPanel.updateResponsiveLayout(isVietnamese, fluidWidth);
    }

    public void refreshData() {
        if (accountSettingsPanel != null) accountSettingsPanel.refreshData();
    }

    private void switchSubTab(String targetCard, JButton activeBtn) {
        subCardLayout.show(subContentPanel, targetCard);
        Color secondary = new Color(150, 150, 150);
        Color inputBg = new Color(40, 40, 40);
        btnAccountTab.setBackground(inputBg); btnAccountTab.setForeground(secondary);
        btnLanguageTab.setBackground(inputBg); btnLanguageTab.setForeground(secondary);
        btnSizeTab.setBackground(inputBg); btnSizeTab.setForeground(secondary);
        btnCategoryTab.setBackground(inputBg); btnCategoryTab.setForeground(secondary);
        activeBtn.setBackground(new Color(255, 193, 7));
        activeBtn.setForeground(new Color(18, 18, 18));
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
            btn.setBackground(new Color(255, 193, 7));
            btn.setForeground(new Color(18, 18, 18));
        } else {
            btn.setBackground(new Color(40, 40, 40));
            btn.setForeground(new Color(150, 150, 150));
        }
        return btn;
    }
}