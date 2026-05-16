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
        setSize(350, 200);
        setLocationRelativeTo(parent);
        getContentPane().setBackground(new Color(30, 30, 30));
        setLayout(new BorderLayout());

        JPanel p = new JPanel(new GridLayout(2, 1, 10, 10));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(20, 30, 10, 30));
        JLabel lbl = new JLabel("Hạn mức (VND):");
        lbl.setForeground(Color.WHITE);
        txtLimit = new JTextField();
        txtLimit.setBackground(new Color(45, 45, 45));
        txtLimit.setForeground(Color.WHITE);
        txtLimit.setCaretColor(Color.WHITE);

        p.add(lbl); p.add(txtLimit);
        add(p, BorderLayout.CENTER);

        JPanel bp = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        bp.setOpaque(false);
        JButton btnSave = new JButton("Lưu");
        JButton btnCancel = new JButton("Hủy");

        btnSave.addActionListener(e -> {
            try {
                double limit = Double.parseDouble(txtLimit.getText().trim());
                int month = java.time.LocalDate.now().getMonthValue();
                int year = java.time.LocalDate.now().getYear();
                budgetManager.setBudget(month, year, limit);
                JOptionPane.showMessageDialog(this, "Đã thiết lập ngân sách!");
                dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Số tiền không hợp lệ!");
            }
        });

        btnCancel.addActionListener(e -> dispose());

        bp.add(btnSave); bp.add(btnCancel);
        add(bp, BorderLayout.SOUTH);
    }
}