package com.expensemanager.ui;

import com.expensemanager.database.DatabaseUtil;
import com.expensemanager.entity.Transaction;
import com.expensemanager.entity.TransactionType;
import com.expensemanager.observer.*;
import com.expensemanager.service.BudgetManager;
import com.expensemanager.service.FinanceService;
import com.expensemanager.service.StatisticsService;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DashboardPanel extends JPanel implements Observer {

    private static final Logger LOGGER = Logger.getLogger(DashboardPanel.class.getName());

    private final MainFrame mainFrame;
    private final FinanceService financeService;
    private final StatisticsService statsService;
    private final BudgetManager budgetManager;

    private final JLabel lblBalance;
    private final JLabel lblTotalIncome;
    private final JLabel lblTotalExpense;
    private final JLabel lblBudgetStatus;

    // Constructor nhận đúng 1 tham số MainFrame
    public DashboardPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.financeService = new FinanceService();
        this.statsService = new StatisticsService(financeService);
        this.budgetManager = new BudgetManager(financeService);

        setLayout(new BorderLayout());
        setBackground(new Color(30, 30, 30));

        // Tiêu đề
        JLabel title = new JLabel("TỔNG QUAN", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        add(title, BorderLayout.NORTH);

        // Panel chứa các thẻ thông tin
        JPanel content = new JPanel(new GridLayout(2, 2, 15, 15));
        content.setBackground(new Color(30, 30, 30));
        content.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        // Khởi tạo các label
        lblBalance = new JLabel("0 VND", SwingConstants.CENTER);
        lblTotalIncome = new JLabel("0 VND", SwingConstants.CENTER);
        lblTotalExpense = new JLabel("0 VND", SwingConstants.CENTER);
        lblBudgetStatus = new JLabel("Chua dat ngan sach", SwingConstants.CENTER);

        content.add(createCard("So du hien tai", lblBalance));
        content.add(createCard("Tong thu", lblTotalIncome));
        content.add(createCard("Tong chi", lblTotalExpense));
        content.add(createCard("Ngan sach", lblBudgetStatus));

        add(content, BorderLayout.CENTER);

        // Thêm nút thêm giao dịch
        JButton btnAddTransaction = createAddTransactionButton();

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setBackground(new Color(30, 30, 30));
        bottomPanel.add(btnAddTransaction);
        add(bottomPanel, BorderLayout.SOUTH);

        // Dang ky observer
        financeService.attach(this);

        refreshData();
    }

    // Tách method để tránh method quá dài
    private JButton createAddTransactionButton() {
        JButton btnAddTransaction = new JButton("+ Them giao dich");
        btnAddTransaction.setFont(new Font("Arial", Font.BOLD, 16));
        btnAddTransaction.setForeground(Color.WHITE);
        btnAddTransaction.setBackground(new Color(0, 153, 76));
        btnAddTransaction.setFocusPainted(false);
        btnAddTransaction.setBorder(BorderFactory.createEmptyBorder(12, 30, 12, 30));
        btnAddTransaction.addActionListener(e -> {
            AddTransactionDialog dialog = new AddTransactionDialog(mainFrame);
            dialog.setVisible(true);
        });
        return btnAddTransaction;
    }

    private JPanel createCard(String title, JLabel valueLabel) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(50, 50, 50));
        card.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80), 1));

        JLabel lblTitle = new JLabel(title, SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.PLAIN, 13));
        lblTitle.setForeground(Color.LIGHT_GRAY);
        lblTitle.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        card.add(lblTitle, BorderLayout.NORTH);

        valueLabel.setFont(new Font("Arial", Font.BOLD, 22));
        valueLabel.setForeground(Color.WHITE);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    @Override
    public void update(EventType eventType, Object data) {
        SwingUtilities.invokeLater(() -> {
            switch (eventType) {
                case TRANSACTION_ADDED:
                case TRANSACTION_UPDATED:
                case TRANSACTION_DELETED:
                case DATA_LOADED:
                    refreshData();
                    break;
                case BUDGET_CHANGED:
                    updateBudgetOnly();
                    break;
                default:
                    break;
            }
        });
    }

    private void updateBudgetOnly() {
        String budgetMessage = budgetManager.checkBudget();
        lblBudgetStatus.setText(budgetMessage);
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

            // Doi mau theo so du
            if (balance < 0) {
                lblBalance.setForeground(new Color(255, 100, 100));
            } else {
                lblBalance.setForeground(new Color(100, 255, 100));
            }

            String budgetMessage = budgetManager.checkBudget();
            lblBudgetStatus.setText(budgetMessage);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Loi khi tai du lieu dashboard", e);
            lblBalance.setText("Loi tai du lieu");
        }
    }
}