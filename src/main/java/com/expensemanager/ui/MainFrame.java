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
    private AccountPanel accountPanel;          // <-- THÊM KHAI BÁO

    private FinanceService financeService;
    private StatisticsService statsService;
    private BudgetManager budgetManager;

    public MainFrame() {
        // Khởi tạo service
        try {
            financeService = new FinanceService();
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
        setSize(1100, 650); // đủ rộng cho sidebar
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Sidebar bên trái
        accountPanel = new AccountPanel(this);  // <-- SỬA LẠI TÊN BIẾN
        add(accountPanel, BorderLayout.WEST);

        // Panel trung tâm (CardLayout)
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        dashboardPanel = new DashboardPanel(this);
        historyPanel = new HistoryPanel();
        if (statsService != null && budgetManager != null) {
            statisticsPanel = new StatisticsPanel(statsService, budgetManager);
        } else {
            statisticsPanel = null;
        }

        mainPanel.add(dashboardPanel, "dashboard");
        mainPanel.add(historyPanel, "history");
        if (statisticsPanel != null) {
            mainPanel.add(statisticsPanel, "statistics");
        }

        add(mainPanel, BorderLayout.CENTER);
        add(createNavBar(), BorderLayout.NORTH);

        setVisible(true);
    }

    private JPanel createNavBar() {
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));

        JButton btnDashboard = new JButton("Tổng quan");
        JButton btnHistory = new JButton("Lịch sử");
        JButton btnStatistics = new JButton("Thống kê");
        JButton btnBudget = new JButton("Ngân sách");

        btnDashboard.addActionListener(e -> cardLayout.show(mainPanel, "dashboard"));
        btnHistory.addActionListener(e -> cardLayout.show(mainPanel, "history"));
        btnStatistics.addActionListener(e -> {
            if (statisticsPanel != null) {
                statisticsPanel.refreshData();
                cardLayout.show(mainPanel, "statistics");
            } else {
                JOptionPane.showMessageDialog(this, "Chức năng thống kê chưa sẵn sàng.");
            }
        });
        btnBudget.addActionListener(e -> {
            if (budgetManager != null) {
                new BudgetDialog(this, budgetManager).setVisible(true);
                refreshAllPanels();
            } else {
                JOptionPane.showMessageDialog(this, "Chức năng ngân sách chưa sẵn sàng.");
            }
        });

        navPanel.add(btnDashboard);
        navPanel.add(btnHistory);
        navPanel.add(btnStatistics);
        navPanel.add(btnBudget);

        return navPanel;
    }

    public void refreshAllPanels() {
        try {
            dashboardPanel.refreshData();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            historyPanel.refreshData();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (statisticsPanel != null) {
            try {
                statisticsPanel.refreshData();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}