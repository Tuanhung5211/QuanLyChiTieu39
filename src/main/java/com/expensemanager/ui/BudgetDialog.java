package com.expensemanager.ui;

import com.expensemanager.service.BudgetManager;
import javax.swing.*;
import java.awt.*;

public class BudgetDialog extends JDialog {
    private JTextField txtLimit;
    private BudgetManager budgetManager;

    public BudgetDialog(JFrame parent, BudgetManager budgetManager) {
        super(parent, "Thiết lập ngân sách tháng", true);
        this.budgetManager = budgetManager;
        setSize(300, 150);
        setLocationRelativeTo(parent);
        setLayout(new GridLayout(3, 2, 10, 10));

        add(new JLabel("Hạn mức (VND):"));
        txtLimit = new JTextField();
        add(txtLimit);

        JButton btnSave = new JButton("Lưu");
        JButton btnCancel = new JButton("Hủy");
        add(btnSave);
        add(btnCancel);

        btnSave.addActionListener(e -> {
            try {
                double limit = Double.parseDouble(txtLimit.getText().trim());
                int month = java.time.LocalDate.now().getMonthValue();
                int year = java.time.LocalDate.now().getYear();
                budgetManager.setBudget(month, year, limit);
                JOptionPane.showMessageDialog(this, "Đã thiết lập ngân sách!");
                dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Số tiền không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
        btnCancel.addActionListener(e -> dispose());
    }
}