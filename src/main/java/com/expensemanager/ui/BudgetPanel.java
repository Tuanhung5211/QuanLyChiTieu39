package com.expensemanager.ui;

import com.expensemanager.database.DatabaseUtil;
import com.expensemanager.entity.Budget;
import com.expensemanager.exception.InvalidAmountException;
import com.expensemanager.observer.EventType;
import com.expensemanager.observer.Observer;
import com.expensemanager.service.BudgetManager;
import com.expensemanager.service.FinanceService;
import com.expensemanager.service.SessionManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;

public class BudgetPanel extends JPanel implements Observer {
    // ĐÃ THÊM BIẾN MAINFRAME Ở ĐÂY CHUẨN KIẾN TRÚC
    private MainFrame mainFrame;
    private BudgetManager budgetManager;
    private FinanceService financeService;

    private JLabel lblMonthYear, lblBudgetLimit, lblSpent, lblRemaining, lblStatus;
    private JProgressBar progressBar;
    private JButton btnSetBudget;

    private final Color BG_COLOR = new Color(18, 18, 18);
    private final Color SURFACE_COLOR = new Color(30, 30, 30);
    private final Color INPUT_BG = new Color(45, 45, 45);
    private final Color ACCENT_YELLOW = new Color(255, 193, 7);
    private final Color TEXT_PRIMARY = new Color(240, 240, 240);
    private final Color TEXT_SECONDARY = new Color(160, 160, 160);

    // ĐÃ CẬP NHẬT: Nhận vào MainFrame mainFrame từ hàm tạo
    public BudgetPanel(MainFrame mainFrame, BudgetManager budgetManager) {
        this.mainFrame = mainFrame;
        this.budgetManager = budgetManager;
        this.financeService = budgetManager.getFinanceService();

        setLayout(new BorderLayout());
        setBackground(BG_COLOR);
        setBorder(BorderFactory.createEmptyBorder(30, 45, 30, 45));

        JLabel title = new JLabel("NGÂN SÁCH CHI TIÊU THÁNG", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(ACCENT_YELLOW);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 25, 0));
        add(title, BorderLayout.NORTH);

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

        lblBudgetLimit = new JLabel("Hạn mức: 0 đ", SwingConstants.CENTER);
        lblBudgetLimit.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        lblBudgetLimit.setForeground(TEXT_PRIMARY);
        lblBudgetLimit.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(lblBudgetLimit);
        centerPanel.add(Box.createVerticalStrut(12));

        lblSpent = new JLabel("Đã chi: 0 đ", SwingConstants.CENTER);
        lblSpent.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        lblSpent.setForeground(TEXT_SECONDARY);
        lblSpent.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(lblSpent);
        centerPanel.add(Box.createVerticalStrut(12));

        lblRemaining = new JLabel("Còn lại: 0 đ", SwingConstants.CENTER);
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

        btnSetBudget = new JButton("Thay đổi hạn mức ngân sách");
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

        refreshData();
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
        JLabel lblHint = new JLabel("Nhập hạn mức chi tiêu mới (VND):");
        lblHint.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lblHint.setForeground(TEXT_PRIMARY);
        panel.add(lblHint);
        panel.add(txtLimit);

        UIManager.put("OptionPane.background", SURFACE_COLOR);
        UIManager.put("Panel.background", SURFACE_COLOR);
        UIManager.put("OptionPane.messageForeground", TEXT_PRIMARY);

        int result = JOptionPane.showConfirmDialog(this, panel, "Thiết lập ngân sách", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            try {
                double limit = Double.parseDouble(txtLimit.getText().trim());
                int month = LocalDate.now().getMonthValue();
                int year = LocalDate.now().getYear();
                budgetManager.setBudget(month, year, limit);
                refreshData();
                if (mainFrame != null) mainFrame.refreshAllPanels();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Số tiền nhập vào không hợp lệ!", "Lỗi cấu trúc", JOptionPane.ERROR_MESSAGE);
            } catch (InvalidAmountException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi nghiệp vụ", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    public void refreshData() {
        if (budgetManager == null) return;

        String status = budgetManager.checkBudget();
        lblStatus.setText(status);
        if (status.contains("⚠️") || status.contains("Vượt")) {
            lblStatus.setForeground(new Color(244, 67, 54));
        } else if (status.contains("✅") || status.contains("Còn")) {
            lblStatus.setForeground(new Color(76, 175, 80));
        } else {
            lblStatus.setForeground(TEXT_SECONDARY);
        }

        int month = LocalDate.now().getMonthValue();
        int year = LocalDate.now().getYear();
        lblMonthYear.setText("Tháng " + month + " Năm " + year);

        String userId = SessionManager.getCurrentUserId();
        if (userId != null) {
            double spent = financeService.getTotalExpense();
            double limit = 0;
            try {
                Budget budget = DatabaseUtil.getBudget(month, year, userId);
                if (budget != null) {
                    limit = budget.getLimit();
                    lblBudgetLimit.setText(String.format("Hạn mức tháng này: %,.0f đ", limit));
                } else {
                    lblBudgetLimit.setText("Hạn mức tháng này: Chưa thiết lập");
                }
            } catch (Exception e) {
                lblBudgetLimit.setText("Hạn mức tháng này: Chưa thiết lập");
            }
            lblSpent.setText(String.format("Số tiền đã chi tiêu: %,.0f đ", spent));

            if (limit > 0) {
                double remaining = limit - spent;
                lblRemaining.setText(String.format("Số dư còn lại: %,.0f đ", remaining));
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
                lblRemaining.setText("Số dư còn lại: 0 đ");
                progressBar.setValue(0);
                progressBar.setString("0%");
            }
        }
    }

    @Override
    public void update(EventType eventType, Object data) {
        if (eventType == EventType.TRANSACTION_ADDED ||
                eventType == EventType.TRANSACTION_UPDATED ||
                eventType == EventType.TRANSACTION_DELETED ||
                eventType == EventType.BUDGET_CHANGED ||
                eventType == EventType.DATA_LOADED) {
            SwingUtilities.invokeLater(() -> refreshData());
        }
    }
}