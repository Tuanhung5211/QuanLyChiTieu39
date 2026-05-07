package com.expensemanager.ui;

import com.expensemanager.service.FinanceService;
import com.expensemanager.service.StatisticsService;
import com.expensemanager.service.BudgetManager;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private DashboardPanel dashboardPanel;
    private HistoryPanel historyPanel;
    private StatisticsPanel statisticsPanel;

    // Service
    private FinanceService financeService;
    private StatisticsService statsService;
    private BudgetManager budgetManager;

    public MainFrame() {
        // Khởi tạo service
        financeService = new FinanceService();
        statsService = new StatisticsService(financeService);
        budgetManager = new BudgetManager(financeService);

        setTitle("Quản Lý Chi Tiêu Mini");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 650);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        dashboardPanel = new DashboardPanel(this);
        historyPanel = new HistoryPanel();
        statisticsPanel = new StatisticsPanel(statsService, budgetManager);

        mainPanel.add(dashboardPanel, "dashboard");
        mainPanel.add(historyPanel, "history");
        mainPanel.add(statisticsPanel, "statistics");

        JPanel navBar = createNavBar();
        add(navBar, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);

        setVisible(true);
    }

    private JPanel createNavBar() {
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));

        JButton btnDashboard = new JButton("Tổng quan");
        JButton btnHistory = new JButton("Lịch sử");
        JButton btnStatistics = new JButton("Thống kê");

        btnDashboard.addActionListener(e -> cardLayout.show(mainPanel, "dashboard"));
        btnHistory.addActionListener(e -> cardLayout.show(mainPanel, "history"));
        btnStatistics.addActionListener(e -> {
            statisticsPanel.refreshData();
            cardLayout.show(mainPanel, "statistics");
        });

        JButton btnBudget = new JButton("Ngân sách");
        btnBudget.addActionListener(e -> {
            new BudgetDialog(this, budgetManager).setVisible(true);
            refreshAllPanels();
        });

        navPanel.add(btnDashboard);
        navPanel.add(btnHistory);
        navPanel.add(btnStatistics);
        navPanel.add(btnBudget);

        return navPanel;
    }

    public void refreshAllPanels() {
        dashboardPanel.refreshData();
        historyPanel.refreshData();
        statisticsPanel.refreshData();
    }
}