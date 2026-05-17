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
    private JButton btnAccountTab, btnConfigTab, btnCategoryTab;

    private AccountSettingsPanel accountSettingsPanel;
    private SystemConfigPanel systemConfigPanel;
    private CategoryManagerPanel categoryManagerPanel;

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
        btnConfigTab = createSubNavButton("", false);
        btnCategoryTab = createSubNavButton("", false);

        btnAccountTab.addActionListener(e -> switchSubTab("account", btnAccountTab));
        btnConfigTab.addActionListener(e -> switchSubTab("config", btnConfigTab));
        btnCategoryTab.addActionListener(e -> switchSubTab("category", btnCategoryTab));

        sidebarPanel.add(btnAccountTab);
        sidebarPanel.add(Box.createVerticalStrut(10));
        sidebarPanel.add(btnConfigTab);
        sidebarPanel.add(Box.createVerticalStrut(10));
        sidebarPanel.add(btnCategoryTab);
        sidebarPanel.add(Box.createVerticalGlue());
        bodyContainer.add(sidebarPanel, BorderLayout.WEST);

        accountSettingsPanel = new AccountSettingsPanel(mainFrame);
        systemConfigPanel = new SystemConfigPanel(mainFrame);
        categoryManagerPanel = new CategoryManagerPanel(mainFrame);

        subCardLayout = new CardLayout();
        subContentPanel = new JPanel(subCardLayout);
        subContentPanel.setOpaque(false);

        // Gắn trực tiếp ScrollPane (đã bọc target) vào các Tab CardLayout để quản lý chiều cao độc lập
        subContentPanel.add(createResponsiveWrapper(accountSettingsPanel), "account");
        subContentPanel.add(createResponsiveWrapper(systemConfigPanel), "config");
        subContentPanel.add(createResponsiveWrapper(categoryManagerPanel), "category");

        bodyContainer.add(subContentPanel, BorderLayout.CENTER);
        add(bodyContainer, BorderLayout.CENTER);

        updateLanguageText();
    }

    // 🌟 NÂNG CẤP ĐỘC QUYỀN: Bọc JScrollPane tàng hình cho từng mục 🌟
    private JScrollPane createResponsiveWrapper(JPanel targetPanel) {
        // Dùng BorderLayout.NORTH để ép form dính chặt lên trên cùng, tránh sinh khoảng trắng dư
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(targetPanel, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(wrapper);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        // CHIÊU THỨC ẨN THANH CUỘN: Thu hẹp kích thước thanh cuộn về 0px (Vô hình)
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16); // Tăng tốc độ cuộn chuột để mượt mà như native app

        return scrollPane;
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
            btnAccountTab.setText("Thông diễn cá nhân");
            btnConfigTab.setText("Cấu hình hệ thống");
            btnCategoryTab.setText("Quản lý danh mục");
        } else {
            lblMainTitle.setText("System Settings");
            btnAccountTab.setText("Account Settings");
            btnConfigTab.setText("System Configuration");
            btnCategoryTab.setText("Category Manager");
        }

        if (accountSettingsPanel != null) accountSettingsPanel.updateResponsiveLayout(isVietnamese, fluidWidth);
        if (systemConfigPanel != null) systemConfigPanel.updateResponsiveLayout(isVietnamese, fluidWidth);
        if (categoryManagerPanel != null) categoryManagerPanel.updateResponsiveLayout(isVietnamese, fluidWidth);
    }

    public void refreshData() {
        if (accountSettingsPanel != null) accountSettingsPanel.refreshData();
        if (categoryManagerPanel != null) categoryManagerPanel.refreshCategories();
    }

    private void switchSubTab(String targetCard, JButton activeBtn) {
        subCardLayout.show(subContentPanel, targetCard);
        Color secondary = new Color(150, 150, 150);
        Color inputBg = new Color(40, 40, 40);
        btnAccountTab.setBackground(inputBg); btnAccountTab.setForeground(secondary);
        btnConfigTab.setBackground(inputBg); btnConfigTab.setForeground(secondary);
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