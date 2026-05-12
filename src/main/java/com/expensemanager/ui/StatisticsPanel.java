package com.expensemanager.ui;

import com.expensemanager.observer.EventType;
import com.expensemanager.observer.Observer;
import com.expensemanager.service.FinanceService;
import com.expensemanager.service.StatisticsService;
import com.expensemanager.service.BudgetManager;

import javax.swing.*;
import java.awt.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StatisticsPanel extends JPanel implements Observer {

    private static final Logger LOGGER = Logger.getLogger(StatisticsPanel.class.getName());

    private final StatisticsService statsService;
    private final BudgetManager budgetManager;
    private final FinanceService financeService;
    private final JLabel lblIncome;
    private final JLabel lblExpense;
    private final JLabel lblBalance;
    private final JLabel lblStatus;

    // ✅ Constructor chi nhan 2 tham so
    public StatisticsPanel(StatisticsService statsService, BudgetManager budgetManager) {
        this.statsService = statsService;
        this.budgetManager = budgetManager;
        this.financeService = new FinanceService();

        setLayout(new BorderLayout());
        setBackground(new Color(18, 18, 18));

        JLabel title = new JLabel("PHAN TICH THU CHI", SwingConstants.LEFT);
        title.setFont(new Font("Arial", Font.BOLD, 26));
        title.setForeground(Color.WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(25, 35, 25, 0));
        add(title, BorderLayout.NORTH);

        JPanel cardPanel = new JPanel(new GridLayout(1, 3, 30, 0));
        cardPanel.setOpaque(false);
        cardPanel.setBorder(BorderFactory.createEmptyBorder(10, 35, 30, 35));

        lblBalance = new JLabel();
        lblIncome = new JLabel();
        lblExpense = new JLabel();

        cardPanel.add(createStyledCard("So du hien tai", new Color(52, 152, 219), lblBalance));
        cardPanel.add(createStyledCard("Tong thu thang nay", new Color(46, 204, 113), lblIncome));
        cardPanel.add(createStyledCard("Tong chi thang nay", new Color(231, 76, 60), lblExpense));

        JPanel centerContainer = new JPanel(new BorderLayout());
        centerContainer.setOpaque(false);
        centerContainer.add(cardPanel, BorderLayout.NORTH);

        lblStatus = new JLabel("", SwingConstants.CENTER);
        lblStatus.setFont(new Font("Arial", Font.ITALIC, 18));
        lblStatus.setForeground(new Color(200, 200, 200));
        centerContainer.add(lblStatus, BorderLayout.CENTER);

        add(centerContainer, BorderLayout.CENTER);

        // Dang ky observer
        financeService.attach(this);

        refreshData();
    }

    private JPanel createStyledCard(String title, Color accentColor, JLabel valueLabel) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(30, 30, 30));
        card.setBorder(BorderFactory.createMatteBorder(6, 0, 0, 0, accentColor));

        JPanel content = new JPanel(new GridLayout(2, 1, 0, 15));
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JLabel tLabel = new JLabel(title);
        tLabel.setForeground(new Color(190, 190, 190));
        tLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        valueLabel.setFont(new Font("Arial", Font.BOLD, 32));
        valueLabel.setForeground(accentColor);
        valueLabel.setText("0 VND");

        content.add(tLabel);
        content.add(valueLabel);
        card.add(content, BorderLayout.CENTER);
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
                    if (data instanceof FinanceService.BudgetAlert) {
                        FinanceService.BudgetAlert alert = (FinanceService.BudgetAlert) data;
                        String warning = String.format(
                                "CANH BAO: Da vuot ngan sach %.0f VND (%.0f%%)",
                                alert.getExcess(), alert.getPercentage());
                        lblStatus.setText(warning);
                        lblStatus.setForeground(Color.RED);
                    } else {
                        lblStatus.setText(budgetManager.checkBudget());
                        lblStatus.setForeground(new Color(200, 200, 200));
                    }
                    break;
                default:
                    break;
            }
        });
    }

    public void refreshData() {
        try {
            double income = statsService.getTotalIncomeThisMonth();
            double expense = statsService.getTotalExpenseThisMonth();
            double balance = statsService.getBalanceThisMonth();

            lblIncome.setText(String.format("%,.0f VND", income));
            lblExpense.setText(String.format("%,.0f VND", expense));
            lblBalance.setText(String.format("%,.0f VND", balance));

            lblStatus.setText(budgetManager.checkBudget());
            lblStatus.setForeground(new Color(200, 200, 200));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Loi khi refresh thong ke", e);
            lblIncome.setText("Loi");
            lblExpense.setText("Loi");
            lblBalance.setText("Loi");
        }
    }
}