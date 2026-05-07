package com.expensemanager.ui;

import com.expensemanager.database.DatabaseUtil;
import com.expensemanager.entity.Transaction;
import com.expensemanager.entity.TransactionType;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class DashboardPanel extends JPanel {

    private JLabel lblBalance, lblTotalIncome, lblTotalExpense;
    private MainFrame mainFrame;

    public DashboardPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Tiêu đề
        JLabel title = new JLabel("TỔNG QUAN TÀI CHÍNH", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        add(title, BorderLayout.NORTH);

        // Panel chứa thông tin số dư
        JPanel infoPanel = new JPanel(new GridLayout(1, 3, 20, 10));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 40, 40));

        lblBalance = createInfoBox("Số dư hiện tại", "0 VND");
        lblTotalIncome = createInfoBox("Tổng thu", "0 VND");
        lblTotalExpense = createInfoBox("Tổng chi", "0 VND");

        infoPanel.add(lblBalance.getParent()); // getParent() để lấy Panel bọc bên ngoài
        infoPanel.add(lblTotalIncome.getParent());
        infoPanel.add(lblTotalExpense.getParent());

        add(infoPanel, BorderLayout.CENTER);

        // Nút "Thêm giao dịch mới"
        JButton btnAdd = new JButton("+ Thêm giao dịch mới");
        btnAdd.setFont(new Font("Arial", Font.BOLD, 16));
        btnAdd.addActionListener(e -> {
            new AddTransactionDialog(mainFrame);
            refreshData();
        });
        add(btnAdd, BorderLayout.SOUTH);

        // Load dữ liệu lần đầu
        refreshData();
    }

    // Tạo một ô hiển thị thông tin đẹp mắt
    private JLabel createInfoBox(String label, String value) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(240, 248, 255));
        panel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2));

        JLabel lblTitle = new JLabel(label, SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.PLAIN, 14));

        JLabel lblValue = new JLabel(value, SwingConstants.CENTER);
        lblValue.setFont(new Font("Arial", Font.BOLD, 22));

        panel.add(lblTitle, BorderLayout.NORTH);
        panel.add(lblValue, BorderLayout.CENTER);

        add(panel); // Thêm panel vào chính nó (sẽ được đặt lại trong infoPanel)
        return lblValue;
    }

    public void refreshData() {
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
    }
}