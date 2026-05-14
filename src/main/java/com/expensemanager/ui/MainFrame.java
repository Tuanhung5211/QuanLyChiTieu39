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
    private HistoryPanel historyPanel;
    private StatisticsPanel statisticsPanel;
    private SettingsPanel settingsPanel;
    private AccountPanel accountPanel;

    private FinanceService financeService;
    private StatisticsService statsService;
    private BudgetManager budgetManager;

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

        setTitle("Quản Lý Chi Tiêu Mini");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 650);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        accountPanel = new AccountPanel(this);
        add(accountPanel, BorderLayout.WEST);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setBackground(new Color(30, 30, 30));

        dashboardPanel = new DashboardPanel(this, financeService, budgetManager);
        historyPanel = new HistoryPanel(financeService);
        if (statsService != null && budgetManager != null) {
            statisticsPanel = new StatisticsPanel(statsService, budgetManager);
        } else {
            statisticsPanel = null;
        }
        settingsPanel = new SettingsPanel(this);

        mainPanel.add(dashboardPanel, "dashboard");
        mainPanel.add(historyPanel, "history");
        if (statisticsPanel != null) mainPanel.add(statisticsPanel, "statistics");
        mainPanel.add(settingsPanel, "settings");

        add(mainPanel, BorderLayout.CENTER);
        add(createNavBar(), BorderLayout.NORTH);

        setVisible(true);
    }

    private JPanel createNavBar() {
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        navPanel.setBackground(new Color(40, 40, 40));
        navPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(60, 60, 60)));

        JButton btnDashboard = createNavButton("Tổng quan");
        JButton btnHistory = createNavButton("Lịch sử");
        JButton btnStatistics = createNavButton("Thống kê");
        JButton btnBudget = createNavButton("Ngân sách");
        JButton btnSettings = createNavButton("Cài đặt");

        btnDashboard.addActionListener(e -> { dashboardPanel.refreshData(); cardLayout.show(mainPanel, "dashboard"); });
        btnHistory.addActionListener(e -> { historyPanel.refreshData(); cardLayout.show(mainPanel, "history"); });
        btnStatistics.addActionListener(e -> {
            if (statisticsPanel != null) {
                statisticsPanel.refreshData();
                cardLayout.show(mainPanel, "statistics");
            } else JOptionPane.showMessageDialog(this, "Thống kê chưa sẵn sàng.");
        });
        btnBudget.addActionListener(e -> {
            if (budgetManager != null) {
                new BudgetDialog(this, budgetManager).setVisible(true);
                refreshAllPanels();
            } else JOptionPane.showMessageDialog(this, "Ngân sách chưa sẵn sàng.");
        });
        btnSettings.addActionListener(e -> { settingsPanel.refreshData(); cardLayout.show(mainPanel, "settings"); });

        navPanel.add(btnDashboard);
        navPanel.add(btnHistory);
        navPanel.add(btnStatistics);
        navPanel.add(btnBudget);
        navPanel.add(btnSettings);
        return navPanel;
    }

    private JButton createNavButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(Color.LIGHT_GRAY);
        btn.setBackground(new Color(40, 40, 40));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setForeground(Color.WHITE);
                btn.setBackground(new Color(60, 60, 60));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setForeground(Color.LIGHT_GRAY);
                btn.setBackground(new Color(40, 40, 40));
            }
        });
        return btn;
    }

    public void refreshAllPanels() {
        if (financeService != null) financeService.syncFromDatabase();
        try { dashboardPanel.refreshData(); } catch (Exception e) { e.printStackTrace(); }
        try { historyPanel.refreshData(); } catch (Exception e) { e.printStackTrace(); }
        if (statisticsPanel != null) try { statisticsPanel.refreshData(); } catch (Exception e) { e.printStackTrace(); }
        try { settingsPanel.refreshData(); } catch (Exception e) { e.printStackTrace(); }
    }

    public FinanceService getFinanceService() { return financeService; }
    public BudgetManager getBudgetManager() { return budgetManager; }
}