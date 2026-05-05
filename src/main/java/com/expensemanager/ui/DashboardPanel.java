package com.expensemanager.ui;

import javax.swing.*;
import java.awt.*;

public class DashboardPanel extends JPanel {
    private JLabel lblBalance, lblIncome, lblExpense;

    public DashboardPanel() {
        setLayout(new GridLayout(3, 1, 10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        lblBalance = new JLabel("Số dư hiện tại: 0 VNĐ", SwingConstants.CENTER);
        lblBalance.setFont(new Font("Arial", Font.BOLD, 24));
        lblBalance.setForeground(Color.BLUE);

        lblIncome = new JLabel("Tổng thu tháng này: 0 VNĐ", SwingConstants.CENTER);
        lblIncome.setFont(new Font("Arial", Font.PLAIN, 18));
        lblIncome.setForeground(new Color(0, 153, 0)); // Xanh lá

        lblExpense = new JLabel("Tổng chi tháng này: 0 VNĐ", SwingConstants.CENTER);
        lblExpense.setFont(new Font("Arial", Font.PLAIN, 18));
        lblExpense.setForeground(Color.RED);

        add(lblBalance);
        add(lblIncome);
        add(lblExpense);
    }

    // Hàm này sẽ gọi FinanceService để update lại UI
    public void refreshData() {
        // double balance = financeService.calculateBalance();
        // lblBalance.setText("Số dư hiện tại: " + balance);
    }
}