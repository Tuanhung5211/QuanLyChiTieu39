package com.expensemanager.ui;

import com.expensemanager.service.StatisticsService;
import com.expensemanager.service.BudgetManager;
import javax.swing.*;
import java.awt.*;

public class StatisticsPanel extends JPanel {
    private StatisticsService statsService;
    private BudgetManager budgetManager;
    private JLabel lblIncome, lblExpense, lblBalance, lblStatus;

    public StatisticsPanel(StatisticsService statsService, BudgetManager budgetManager) {
        this.statsService = statsService;
        this.budgetManager = budgetManager;
        setLayout(new BorderLayout());
        setBackground(new Color(18, 18, 18));

        JLabel title = new JLabel("PHÂN TÍCH THU CHI", SwingConstants.LEFT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(Color.WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(25, 35, 25, 0));
        add(title, BorderLayout.NORTH);

        JPanel cardPanel = new JPanel(new GridLayout(1, 3, 30, 0));
        cardPanel.setOpaque(false);
        cardPanel.setBorder(BorderFactory.createEmptyBorder(10, 35, 30, 35));

        lblBalance = new JLabel(); lblIncome = new JLabel(); lblExpense = new JLabel();

        cardPanel.add(createStyledCard("Số dư hiện tại", new Color(52, 152, 219), lblBalance));
        cardPanel.add(createStyledCard("Tổng thu tháng này", new Color(46, 204, 113), lblIncome));
        cardPanel.add(createStyledCard("Tổng chi tháng này", new Color(231, 76, 60), lblExpense));

        JPanel centerContainer = new JPanel(new BorderLayout());
        centerContainer.setOpaque(false);
        centerContainer.add(cardPanel, BorderLayout.NORTH);

        lblStatus = new JLabel("", SwingConstants.CENTER);
        lblStatus.setFont(new Font("Segoe UI", Font.ITALIC, 18));
        lblStatus.setForeground(new Color(200, 200, 200));
        centerContainer.add(lblStatus, BorderLayout.CENTER);

        add(centerContainer, BorderLayout.CENTER);
        refreshData();
    }

    private JPanel createStyledCard(String title, Color accentColor, JLabel valueLabel) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(30, 30, 30));
        card.setBorder(BorderFactory.createMatteBorder(6, 0, 0, 0, accentColor));
        JPanel content = new JPanel(new GridLayout(2, 1, 0, 15));
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        JLabel tLabel = new JLabel(title); tLabel.setForeground(new Color(190, 190, 190));
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 32)); valueLabel.setForeground(accentColor); valueLabel.setText("0 VND");
        content.add(tLabel); content.add(valueLabel);
        card.add(content, BorderLayout.CENTER);
        return card;
    }

    public void refreshData() {
        lblIncome.setText(String.format("%,.0f VND", statsService.getTotalIncomeThisMonth()));
        lblExpense.setText(String.format("%,.0f VND", statsService.getTotalExpenseThisMonth()));
        lblBalance.setText(String.format("%,.0f VND", statsService.getBalanceThisMonth()));
        lblStatus.setText(budgetManager.checkBudget());
    }
}