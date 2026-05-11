package com.expensemanager.ui;

import com.expensemanager.database.DatabaseUtil;
import com.expensemanager.entity.Transaction;
import com.expensemanager.entity.TransactionType;
import com.expensemanager.service.BudgetManager;
import com.expensemanager.service.FinanceService;
import com.expensemanager.service.StatisticsService;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class DashboardPanel extends JPanel {
    private MainFrame mainFrame;
    private FinanceService financeService;
    private StatisticsService statsService;
    private BudgetManager budgetManager;

    private JLabel lblBalance, lblTotalIncome, lblTotalExpense;
    private JLabel lblBudgetStatus;
    private JLabel lblIncomeThisMonth, lblExpenseThisMonth, lblBalanceThisMonth;

    public DashboardPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        // Khởi tạo service (có thể lấy từ MainFrame nếu muốn dùng chung, nhưng tạm thời tạo mới)
        financeService = new FinanceService();
        statsService = new StatisticsService(financeService);
        budgetManager = new BudgetManager(financeService);

        setLayout(new BorderLayout());
        setBackground(new Color(30, 30, 30)); // Dark background

        // Tiêu đề
        JLabel title = new JLabel("DASHBOARD", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        add(title, BorderLayout.NORTH);

        // Panel chứa các thẻ thông tin (Grid 2x2)
        JPanel content = new JPanel(new GridLayout(2, 2, 15, 15));
        content.setBackground(new Color(30, 30, 30));
        content.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        content.add(createCard("Số dư hiện tại", lblBalance = new JLabel("0 VND", SwingConstants.CENTER)));
        content.add(createCard("Tổng thu", lblTotalIncome = new JLabel("0 VND", SwingConstants.CENTER)));
        content.add(createCard("Tổng chi", lblTotalExpense = new JLabel("0 VND", SwingConstants.CENTER)));
        content.add(createCard("Ngân sách", lblBudgetStatus = new JLabel("Chưa đặt ngân sách", SwingConstants.CENTER)));

        add(content, BorderLayout.CENTER);

        // Thêm nút "＋ Thêm giao dịch" ở dưới cùng
        JButton btnAddTransaction = new JButton("＋ Thêm giao dịch");
        btnAddTransaction.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnAddTransaction.setForeground(Color.WHITE);
        btnAddTransaction.setBackground(new Color(0, 153, 76));
        btnAddTransaction.setFocusPainted(false);
        btnAddTransaction.setBorder(BorderFactory.createEmptyBorder(12, 30, 12, 30));
        btnAddTransaction.addActionListener(e -> {
            // Mở hộp thoại thêm giao dịch
            AddTransactionDialog dialog = new AddTransactionDialog(mainFrame);
            dialog.setVisible(true);
            // Sau khi đóng dialog, làm mới dữ liệu
            refreshData();
        });
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setBackground(new Color(30, 30, 30));
        bottomPanel.add(btnAddTransaction);
        add(bottomPanel, BorderLayout.SOUTH);

        refreshData();
    }

    private JPanel createCard(String title, JLabel valueLabel) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(50, 50, 50));
        card.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80), 1));

        JLabel lblTitle = new JLabel(title, SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblTitle.setForeground(Color.LIGHT_GRAY);
        lblTitle.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        card.add(lblTitle, BorderLayout.NORTH);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valueLabel.setForeground(Color.WHITE);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    public void refreshData() {
        try {
            List<Transaction> transactions = DatabaseUtil.getAllTransactions();

            double totalIncome = transactions.stream()
                    .filter(t -> t.getType() == TransactionType.INCOME)
                    .mapToDouble(Transaction::getAmount)
                    .sum();
            double totalExpense = transactions.stream()
                    .filter(t -> t.getType() == TransactionType.EXPENSE)
                    .mapToDouble(Transaction::getAmount)
                    .sum();
            double balance = totalIncome - totalExpense;

            lblTotalIncome.setText(String.format("%,.0f VND", totalIncome));
            lblTotalExpense.setText(String.format("%,.0f VND", totalExpense));
            lblBalance.setText(String.format("%,.0f VND", balance));

            // Cập nhật ngân sách
            String budgetMessage = budgetManager.checkBudget();
            lblBudgetStatus.setText(budgetMessage);
        } catch (Exception e) {
            lblBalance.setText("Lỗi tải dữ liệu");
        }
    }
}