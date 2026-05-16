package com.expensemanager.ui;

import com.expensemanager.service.BudgetManager;
import com.expensemanager.service.FinanceService;
import com.expensemanager.service.StatisticsService;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private DashboardPanel dashboardPanel;
    private StatisticsPanel statisticsPanel;
    private BudgetPanel budgetPanel;
    private SettingsPanel settingsPanel;
    private AccountPanel accountPanel;

    private JButton btnDashboard;
    private JButton btnStatistics;
    private JButton btnBudget;
    private JButton btnSettings;

    private FinanceService financeService;
    private StatisticsService statsService;
    private BudgetManager budgetManager;

    private boolean isVietnamese = true;

    public boolean isVietnamese() {
        return this.isVietnamese;
    }

    // 🌟 KÍCH HOẠT ĐỒNG BỘ: Hàm điều phối dịch thuật thời gian thực trên toàn app
    public void updateGlobalLanguage(boolean isVN) {
        this.isVietnamese = isVN;

        if (btnDashboard != null) btnDashboard.setText(isVN ? "Tổng quan" : "Overview");
        if (btnStatistics != null) btnStatistics.setText(isVN ? "Thống kê" : "Statistics");
        if (btnBudget != null) btnBudget.setText(isVN ? "Ngân sách" : "Budget");
        if (btnSettings != null) btnSettings.setText(isVN ? "Cài đặt" : "Settings");

        if (accountPanel != null) {
            accountPanel.updateLanguage(isVN);
        }

        if (statisticsPanel != null) statisticsPanel.refreshData();
        if (dashboardPanel != null) dashboardPanel.refreshData();
        if (budgetPanel != null) budgetPanel.refreshData();

        this.revalidate();
        this.repaint();
    }

    // 🌟 TÍNH NĂNG MỚI: Hàm thay đổi kích thước ứng dụng cố định 🌟
    public void changeWindowSize(int width, int height) {
        // Cho phép thay đổi kích thước, gán size mới, căn giữa màn hình rồi khóa lại lập tức
        setResizable(true);
        setSize(width, height);
        setLocationRelativeTo(null);
        setResizable(false);

        this.revalidate();
        this.repaint();
    }

    public MainFrame() {
        try {
            financeService = new FinanceService();
            financeService.syncFromDatabase();
        } catch (Exception e) {
            e.printStackTrace();
            financeService = null;
        }
        if (financeService != null) {
            statsService = new StatisticsService(financeService);
            budgetManager = new BudgetManager(financeService);
        } else {
            statsService = null;
            budgetManager = null;
        }

        setTitle("Money Tracker Desktop");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 🌟 KHÓA UI CHÍ SẠM: Đặt kích thước mặc định ban đầu và khóa cứng việc kéo giãn tự do bằng chuột
        setSize(1200, 750);
        setResizable(false);

        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        accountPanel = new AccountPanel(this);
        add(accountPanel, BorderLayout.WEST);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        dashboardPanel = new DashboardPanel(this, financeService, budgetManager);
        if (statsService != null && budgetManager != null) {
            statisticsPanel = new StatisticsPanel(statsService, budgetManager);
            budgetPanel = new BudgetPanel(this, budgetManager);
        } else {
            statisticsPanel = null;
            budgetPanel = null;
        }
        settingsPanel = new SettingsPanel(this);

        mainPanel.add(dashboardPanel, "dashboard");
        if (statisticsPanel != null) mainPanel.add(statisticsPanel, "statistics");
        if (budgetPanel != null) mainPanel.add(budgetPanel, "budget");
        mainPanel.add(settingsPanel, "settings");

        if (financeService != null) {
            financeService.attach(dashboardPanel);
            if (statisticsPanel != null) financeService.attach(statisticsPanel);
            if (budgetPanel != null) financeService.attach(budgetPanel);
            financeService.attach(accountPanel);
        }

        add(mainPanel, BorderLayout.CENTER);
        add(createNavBar(), BorderLayout.NORTH);

        setVisible(true);
    }

    private JPanel createNavBar() {
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 12));
        navPanel.setBackground(new Color(40, 40, 40));
        navPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(60, 60, 60)));

        btnDashboard = createNavButton("Tổng quan");
        btnStatistics = createNavButton("Thống kê");
        btnBudget = createNavButton("Ngân sách");
        btnSettings = createNavButton("Cài đặt");

        btnDashboard.addActionListener(e -> { dashboardPanel.refreshData(); cardLayout.show(mainPanel, "dashboard"); });
        btnStatistics.addActionListener(e -> {
            if (statisticsPanel != null) { statisticsPanel.refreshData(); cardLayout.show(mainPanel, "statistics"); }
        });
        btnBudget.addActionListener(e -> {
            if (budgetPanel != null) { budgetPanel.refreshData(); cardLayout.show(mainPanel, "budget"); }
        });
        btnSettings.addActionListener(e -> { settingsPanel.refreshData(); cardLayout.show(mainPanel, "settings"); });

        navPanel.add(btnDashboard);
        navPanel.add(btnStatistics);
        navPanel.add(btnBudget);
        navPanel.add(btnSettings);
        return navPanel;
    }

    private JButton createNavButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setForeground(Color.LIGHT_GRAY);
        btn.setBackground(new Color(40, 40, 40));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 22, 10, 22));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setForeground(Color.WHITE); btn.setBackground(new Color(60, 60, 60));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setForeground(Color.LIGHT_GRAY); btn.setBackground(new Color(40, 40, 40));
            }
        });
        return btn;
    }

    public void refreshAllPanels() {
        if (financeService != null) financeService.syncFromDatabase();
    }

    public FinanceService getFinanceService() { return financeService; }
    public BudgetManager getBudgetManager() { return budgetManager; }
}