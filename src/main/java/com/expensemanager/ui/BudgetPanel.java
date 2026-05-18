package com.expensemanager.ui;

import com.expensemanager.database.DatabaseUtil;
import com.expensemanager.entity.Budget;
import com.expensemanager.entity.Transaction;
import com.expensemanager.entity.TransactionType;
import com.expensemanager.exception.InvalidAmountException;
import com.expensemanager.observer.EventType;
import com.expensemanager.observer.Observer;
import com.expensemanager.service.BudgetManager;
import com.expensemanager.service.FinanceService;
import com.expensemanager.service.SessionManager;
import com.expensemanager.util.InputValidator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.util.Locale;

public class BudgetPanel extends JPanel implements Observer {
    private MainFrame mainFrame;
    private BudgetManager budgetManager;
    private FinanceService financeService;

    private JLabel lblTitle;
    private JLabel lblMonthYear, lblBudgetLimit, lblSpent, lblRemaining, lblStatus;
    private JProgressBar progressBar;
    private JButton btnSetBudget;
    private boolean isVietnamese = true;

    // Biến đệm cục bộ ép hiển thị hạn mức mới ngay lập tức không thông qua độ trễ DB
    private Double localBudgetLimit = null;

    private final Color BG_COLOR = new Color(18, 18, 18);
    private final Color SURFACE_COLOR = new Color(30, 30, 30);
    private final Color INPUT_BG = new Color(45, 45, 45);
    private final Color ACCENT_YELLOW = new Color(255, 193, 7);
    private final Color TEXT_PRIMARY = new Color(240, 240, 240);
    private final Color TEXT_SECONDARY = new Color(160, 160, 160);

    public BudgetPanel(MainFrame mainFrame, BudgetManager budgetManager) {
        this.mainFrame = mainFrame;
        this.budgetManager = budgetManager;
        // 🌟 KHẮC PHỤC: Lấy FinanceService trực tiếp từ mainFrame để dứt điểm lỗi biến cannot find symbol
        if (mainFrame != null) {
            this.financeService = mainFrame.getFinanceService();
            this.isVietnamese = mainFrame.isVietnamese();
        }

        setLayout(new BorderLayout());
        setBackground(BG_COLOR);
        setBorder(BorderFactory.createEmptyBorder(30, 45, 30, 45));

        lblTitle = new JLabel("", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(ACCENT_YELLOW);
        lblTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 25, 0));
        add(lblTitle, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(SURFACE_COLOR);
        centerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(50, 50, 50), 1, true),
                BorderFactory.createEmptyBorder(25, 30, 30, 30)
        ));
        centerPanel.setMaximumSize(new Dimension(800, 450));

        lblMonthYear = new JLabel("", SwingConstants.CENTER);
        lblMonthYear.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblMonthYear.setForeground(TEXT_PRIMARY);
        lblMonthYear.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(lblMonthYear);
        centerPanel.add(Box.createVerticalStrut(25));

        lblBudgetLimit = new JLabel("", SwingConstants.CENTER);
        lblBudgetLimit.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        lblBudgetLimit.setForeground(TEXT_PRIMARY);
        lblBudgetLimit.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(lblBudgetLimit);
        centerPanel.add(Box.createVerticalStrut(12));

        lblSpent = new JLabel("", SwingConstants.CENTER);
        lblSpent.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        lblSpent.setForeground(TEXT_SECONDARY);
        lblSpent.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(lblSpent);
        centerPanel.add(Box.createVerticalStrut(12));

        lblRemaining = new JLabel("", SwingConstants.CENTER);
        lblRemaining.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblRemaining.setForeground(new Color(76, 175, 80));
        lblRemaining.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(lblRemaining);
        centerPanel.add(Box.createVerticalStrut(25));

        progressBar = new JProgressBar(0, 100);
        progressBar.setPreferredSize(new Dimension(450, 30));
        progressBar.setMaximumSize(new Dimension(450, 30));
        progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);
        progressBar.setStringPainted(true);
        progressBar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        progressBar.setBackground(INPUT_BG);
        progressBar.setForeground(new Color(76, 175, 80));
        progressBar.setBorder(BorderFactory.createLineBorder(new Color(65, 65, 65), 1));
        centerPanel.add(progressBar);
        centerPanel.add(Box.createVerticalStrut(20));

        lblStatus = new JLabel("", SwingConstants.CENTER);
        lblStatus.setFont(new Font("Segoe UI", Font.ITALIC, 16));
        lblStatus.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(lblStatus);
        centerPanel.add(Box.createVerticalStrut(30));

        btnSetBudget = new JButton();
        btnSetBudget.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnSetBudget.setBackground(ACCENT_YELLOW);
        btnSetBudget.setForeground(BG_COLOR);
        btnSetBudget.setFocusPainted(false);
        btnSetBudget.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnSetBudget.setBorder(BorderFactory.createEmptyBorder(12, 35, 12, 35));
        btnSetBudget.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSetBudget.addActionListener(e -> openSetBudgetDialog());

        btnSetBudget.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btnSetBudget.setBackground(new Color(255, 205, 50)); }
            @Override public void mouseExited(MouseEvent e) { btnSetBudget.setBackground(ACCENT_YELLOW); }
        });
        centerPanel.add(btnSetBudget);

        JPanel wrapperPanel = new JPanel(new GridBagLayout());
        wrapperPanel.setOpaque(false);
        wrapperPanel.add(centerPanel);
        add(wrapperPanel, BorderLayout.CENTER);

        updateLanguageText(this.isVietnamese);
    }

    private void openSetBudgetDialog() {
        JTextField txtLimit = new JTextField();
        txtLimit.setBackground(INPUT_BG);
        txtLimit.setForeground(TEXT_PRIMARY);
        txtLimit.setCaretColor(ACCENT_YELLOW);
        txtLimit.setFont(new Font("Segoe UI", Font.BOLD, 16));
        txtLimit.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 70, 70)),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        JPanel panel = new JPanel(new GridLayout(2, 1, 5, 8));
        panel.setOpaque(false);

        JLabel lblHint = new JLabel(isVietnamese ? "Nhập hạn mức chi tiêu mới (VND):" : "Enter new monthly budget limit (VND):");
        lblHint.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lblHint.setForeground(TEXT_PRIMARY);
        panel.add(lblHint);
        panel.add(txtLimit);

        UIManager.put("OptionPane.background", SURFACE_COLOR);
        UIManager.put("Panel.background", SURFACE_COLOR);
        UIManager.put("OptionPane.messageForeground", TEXT_PRIMARY);

        int result = JOptionPane.showConfirmDialog(this, panel,
                isVietnamese ? "Thiết lập ngân sách" : "Setup Expense Budget",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                double limit = InputValidator.validateAmount(txtLimit.getText(), isVietnamese);

                int month = LocalDate.now().getMonthValue();
                int year = LocalDate.now().getYear();

                // 🌟 TỐI ƯU SẠCH SẼ: Gọi trực tiếp tầng nghiệp vụ thuần Java an toàn, không dùng Reflection nữa
                budgetManager.setBudget(month, year, limit);

                this.localBudgetLimit = limit;
                refreshData();
                if (mainFrame != null) mainFrame.refreshAllPanels();
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(),
                        isVietnamese ? "Lỗi nhập liệu" : "Input Validation Error", JOptionPane.WARNING_MESSAGE);
            } catch (InvalidAmountException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(),
                        isVietnamese ? "Lỗi nghiệp vụ" : "Business Rule Violation", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    public void refreshData() {
        if (budgetManager == null || financeService == null) return;

        String status = budgetManager.checkBudget();
        lblStatus.setText(status);
        if (status != null && (status.contains("⚠️") || status.contains("Vượt") || status.contains("Exceeded"))) {
            lblStatus.setForeground(new Color(244, 67, 54));
        } else if (status != null && (status.contains("✅") || status.contains("Còn") || status.contains("Within"))) {
            lblStatus.setForeground(new Color(76, 175, 80));
        } else {
            lblStatus.setForeground(TEXT_SECONDARY);
        }

        int month = LocalDate.now().getMonthValue();
        int year = LocalDate.now().getYear();

        if (isVietnamese) {
            lblMonthYear.setText("Tháng " + month + " Năm " + year);
        } else {
            String monthName = java.time.Month.of(month).getDisplayName(java.time.format.TextStyle.FULL, Locale.ENGLISH);
            lblMonthYear.setText(monthName + " " + year);
        }

        String userId = SessionManager.getCurrentUserId();
        if (userId == null) {
            String currentUsername = SessionManager.getCurrentUsername();
            if (currentUsername != null) {
                com.expensemanager.entity.User currentUser = DatabaseUtil.getUserByUsername(currentUsername);
                if (currentUser != null) userId = currentUser.getId();
            }
        }

        if (userId != null) {
            // 🌟 KHẮC PHỤC: Lọc chính xác tổng tiền CHI TIÊU của RIÊNG THÁNG/NĂM hiện tại
            double spent = financeService.getAllTransactions().stream()
                    .filter(t -> t != null && t.getType() == TransactionType.EXPENSE)
                    .filter(t -> t.getDateTime().getMonthValue() == month)
                    .filter(t -> t.getDateTime().getYear() == year)
                    .mapToDouble(Transaction::getAmount)
                    .sum();

            double limit = 0;
            if (this.localBudgetLimit != null) {
                limit = this.localBudgetLimit;
            } else {
                Budget budget = DatabaseUtil.getBudget(month, year, userId);
                if (budget != null) {
                    limit = budget.getLimit();
                }
            }

            if (limit > 0) {
                lblBudgetLimit.setText(isVietnamese ?
                        String.format("Hạn mức tháng này: %,.0f đ", limit) :
                        String.format("Monthly Limit: %,.0f VND", limit));
            } else {
                lblBudgetLimit.setText(isVietnamese ? "Hạn mức tháng này: Chưa thiết lập" : "Monthly Limit: Not Set Yet");
            }

            lblSpent.setText(isVietnamese ?
                    String.format("Số tiền đã chi tiêu: %,.0f đ", spent) :
                    String.format("Total Amount Spent: %,.0f VND", spent));

            if (limit > 0) {
                double remaining = limit - spent;
                lblRemaining.setText(isVietnamese ?
                        String.format("Số dư còn lại: %,.0f đ", remaining) :
                        String.format("Remaining Balance: %,.0f VND", remaining));

                if (remaining < 0) {
                    lblRemaining.setForeground(new Color(244, 67, 54));
                } else {
                    lblRemaining.setForeground(new Color(76, 175, 80));
                }
                int percent = (int) Math.round((spent / limit) * 100);
                progressBar.setValue(Math.min(percent, 100));
                progressBar.setString(percent + "%");
                progressBar.setForeground(percent > 100 ? new Color(244, 67, 54) : new Color(76, 175, 80));
            } else {
                lblRemaining.setText(isVietnamese ? "Số dư còn lại: 0 đ" : "Remaining Balance: 0 VND");
                progressBar.setValue(0);
                progressBar.setString("0%");
            }
        }
    }

    @Override
    public void update(EventType eventType, Object data) {
        if (eventType == EventType.BUDGET_CHANGED && data instanceof Double) {
            this.localBudgetLimit = (Double) data;
        }
        if (eventType == EventType.TRANSACTION_ADDED ||
                eventType == EventType.TRANSACTION_UPDATED ||
                eventType == EventType.TRANSACTION_DELETED ||
                eventType == EventType.BUDGET_CHANGED ||
                eventType == EventType.DATA_LOADED) {
            SwingUtilities.invokeLater(() -> refreshData());
        }
    }

    public void updateLanguageText(boolean isVN) {
        this.isVietnamese = isVN;

        if (lblTitle != null) {
            lblTitle.setText(isVN ? "NGÂN SÁCH CHI TIÊU THÁNG" : "MONTHLY EXPENSE BUDGET");
        }
        if (btnSetBudget != null) {
            btnSetBudget.setText(isVN ? "Thay đổi hạn mức ngân sách" : "Adjust Budget Limit");
        }

        refreshData();
    }
}