package com.expensemanager.ui;

import com.expensemanager.service.BudgetManager;
import com.expensemanager.service.StatisticsService;
import com.expensemanager.service.SessionManager;

import javax.swing.*;
import java.awt.*;

public class StatisticsPanel extends JPanel {
    private StatisticsService statsService;
    private BudgetManager budgetManager;
    private JLabel lblIncome, lblExpense, lblBalance, lblBudgetStatus;

    public StatisticsPanel(StatisticsService statsService, BudgetManager budgetManager) {
        this.statsService = statsService;
        this.budgetManager = budgetManager;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JLabel title = new JLabel("THỐNG KÊ THÁNG NÀY", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        add(title, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 40, 40));
        centerPanel.setBackground(Color.WHITE);

        lblIncome = new JLabel("Tổng thu tháng: 0 VND", SwingConstants.CENTER);
        lblIncome.setFont(new Font("Arial", Font.PLAIN, 16));
        lblIncome.setForeground(new Color(0, 153, 0));

        lblExpense = new JLabel("Tổng chi tháng: 0 VND", SwingConstants.CENTER);
        lblExpense.setFont(new Font("Arial", Font.PLAIN, 16));
        lblExpense.setForeground(Color.RED);

        lblBalance = new JLabel("Số dư tháng: 0 VND", SwingConstants.CENTER);
        lblBalance.setFont(new Font("Arial", Font.BOLD, 18));
        lblBalance.setForeground(Color.BLUE);

        lblBudgetStatus = new JLabel("", SwingConstants.CENTER);
        lblBudgetStatus.setFont(new Font("Arial", Font.ITALIC, 14));

        centerPanel.add(lblIncome);
        centerPanel.add(lblExpense);
        centerPanel.add(lblBalance);
        centerPanel.add(lblBudgetStatus);

        add(centerPanel, BorderLayout.CENTER);

        JButton btnRefresh = new JButton("Làm mới");
        btnRefresh.addActionListener(e -> refreshData());
        add(btnRefresh, BorderLayout.SOUTH);
    }

    public void refreshData() {
        if (statsService == null || budgetManager == null) return;
        String userId = SessionManager.getCurrentUserId();
        if (userId == null) return;

        double income = statsService.getTotalIncomeThisMonth();
        double expense = statsService.getTotalExpenseThisMonth();
        double balance = statsService.getBalanceThisMonth();

        lblIncome.setText(String.format("Tổng thu tháng: %,.0f VND", income));
        lblExpense.setText(String.format("Tổng chi tháng: %,.0f VND", expense));
        lblBalance.setText(String.format("Số dư tháng: %,.0f VND", balance));

        String budgetMessage = budgetManager.checkBudget();
        lblBudgetStatus.setText(budgetMessage);
        lblBudgetStatus.setForeground(budgetMessage.contains("⚠️") ? Color.RED : new Color(0, 153, 0));
    }
}