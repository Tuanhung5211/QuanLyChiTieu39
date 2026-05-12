package com.expensemanager.ui;

import com.expensemanager.database.DatabaseUtil;
import com.expensemanager.entity.Category;
import com.expensemanager.entity.Transaction;
import com.expensemanager.entity.TransactionType;
import com.expensemanager.service.SessionManager;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.UUID;

public class AddTransactionDialog extends JDialog {
    private JTextField txtAmount, txtNote;
    private JComboBox<Category> cmbCategory;
    private JRadioButton rbIncome, rbExpense;
    private MainFrame mainFrame;

    public AddTransactionDialog(MainFrame mainFrame) {
        super(mainFrame, "Thêm giao dịch mới", true);
        this.mainFrame = mainFrame;
        setSize(400, 350);
        setLocationRelativeTo(mainFrame);

        JPanel panel = new JPanel(new GridLayout(6, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        panel.add(new JLabel("Loại giao dịch:"));
        rbIncome = new JRadioButton("Thu nhập");
        rbExpense = new JRadioButton("Chi tiêu", true);
        ButtonGroup group = new ButtonGroup();
        group.add(rbIncome);
        group.add(rbExpense);
        JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        typePanel.add(rbIncome);
        typePanel.add(rbExpense);
        panel.add(typePanel);

        panel.add(new JLabel("Số tiền (VND):"));
        txtAmount = new JTextField();
        panel.add(txtAmount);

        panel.add(new JLabel("Danh mục:"));
        cmbCategory = new JComboBox<>();
        loadCategories();
        panel.add(cmbCategory);

        panel.add(new JLabel("Ghi chú:"));
        txtNote = new JTextField();
        panel.add(txtNote);

        JButton btnSave = new JButton("Lưu");
        btnSave.addActionListener(e -> saveTransaction());
        JButton btnCancel = new JButton("Hủy");
        btnCancel.addActionListener(e -> dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);
        panel.add(new JLabel());
        panel.add(buttonPanel);

        add(panel);
        setVisible(true);
    }

    private void loadCategories() {
        try {
            List<Category> categories = DatabaseUtil.getAllCategories();
            cmbCategory.removeAllItems();
            if (categories != null) {
                for (Category c : categories) {
                    cmbCategory.addItem(c);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Không thể tải danh mục từ database: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveTransaction() {
        try {
            String userId = SessionManager.getCurrentUserId();
            if (userId == null) {
                JOptionPane.showMessageDialog(this, "Bạn chưa đăng nhập!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            double amount = Double.parseDouble(txtAmount.getText().trim());
            if (amount <= 0) {
                JOptionPane.showMessageDialog(this, "Số tiền phải lớn hơn 0!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            TransactionType type = rbIncome.isSelected() ? TransactionType.INCOME : TransactionType.EXPENSE;
            Category category = (Category) cmbCategory.getSelectedItem();
            if (category == null) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn danh mục!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String note = txtNote.getText().trim();
            String id = UUID.randomUUID().toString().substring(0, 8);

            Transaction t = new Transaction(id, amount, type, category, note);
            DatabaseUtil.insertTransaction(t, userId);

            JOptionPane.showMessageDialog(this, "Thêm giao dịch thành công!");
            dispose();

            if (mainFrame != null) {
                try {
                    mainFrame.refreshAllPanels();
                } catch (Exception e) {
                    System.err.println("Lỗi khi làm mới giao diện: " + e.getMessage());
                }
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Số tiền không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}