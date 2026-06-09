package com.expensemanager.ui;

import com.expensemanager.database.DatabaseUtil;
import com.expensemanager.entity.Category;
import com.expensemanager.entity.Transaction;
import com.expensemanager.entity.TransactionType;
import com.expensemanager.util.EmojiUtil;
import com.expensemanager.service.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;

public class TransactionDetailDialog extends JDialog {

    private MainFrame mainFrame;
    private Transaction transaction;
    private boolean isVietnamese;

    public TransactionDetailDialog(MainFrame mainFrame, Transaction transaction) {
        super(mainFrame, "Chi tiết giao dịch", true);
        this.mainFrame = mainFrame;
        this.transaction = transaction;
        this.isVietnamese = (mainFrame != null && mainFrame.isVietnamese());

        setSize(420, 420);
        setLocationRelativeTo(mainFrame);
        setLayout(new BorderLayout());

        initComponents();
        applyTheme();

        ThemeManager.applyThemeRecursively(this);
    }

    private void initComponents() {
        JPanel headerPanel = new JPanel(new BorderLayout(15, 0));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 15, 25));

        Category cat = transaction.getCategory();
        String emoji = (cat != null) ? EmojiUtil.CATEGORY_EMOJI.getOrDefault(cat.getName(), "\uD83D\uDCCD") : "\uD83D\uDCCD";

        JLabel lblIcon = new JLabel(emoji);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 44));
        lblIcon.setForeground(ThemeManager.getColor("bg"));
        headerPanel.add(lblIcon, BorderLayout.WEST);

        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        textPanel.setOpaque(false);
        String note = transaction.getNote();
        JLabel lblNote = new JLabel(note != null && !note.isEmpty() ? note : (isVietnamese ? "Không có ghi chú" : "No note"));
        lblNote.setFont(new Font("Segoe UI", Font.BOLD, 18));

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        JLabel lblDate = new JLabel(transaction.getDateTime().format(dtf));
        lblDate.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        textPanel.add(lblNote);
        textPanel.add(lblDate);
        headerPanel.add(textPanel, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

        JPanel detailPanel = new JPanel(new GridBagLayout());
        detailPanel.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.weightx = 1.0;

        addDetailRow(detailPanel, gbc, isVietnamese ? "Danh mục:" : "Category:", cat != null ? cat.getName() : (isVietnamese ? "Không có" : "None"), 0);
        String amountStr = String.format("%,.0f VND", transaction.getAmount());
        addDetailRow(detailPanel, gbc, isVietnamese ? "Số tiền:" : "Amount:", amountStr, 1);
        addDetailRow(detailPanel, gbc, isVietnamese ? "Loại hành động:" : "Type:", transaction.getType().name(), 2);
        addDetailRow(detailPanel, gbc, isVietnamese ? "Thời gian lưu:" : "Recorded at:", transaction.getDateTime().format(dtf), 3);
        add(detailPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 12, 0));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 25, 25, 25));

        JButton btnEdit = createFlatButton(isVietnamese ? "Sửa" : "Edit");
        btnEdit.addActionListener(e -> editTransaction());

        JButton btnDelete = createFlatButton(isVietnamese ? "Xóa" : "Delete");
        btnDelete.addActionListener(e -> deleteTransaction());

        JButton btnClose = createFlatButton(isVietnamese ? "Đóng" : "Close");
        btnClose.addActionListener(e -> dispose());

        buttonPanel.add(btnEdit);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnClose);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    public void applyTheme() {
        getContentPane().setBackground(ThemeManager.getColor("bg"));
        for (Component comp : getContentPane().getComponents()) {
            if (comp instanceof JPanel) {
                JPanel panel = (JPanel) comp;
                panel.setBackground(ThemeManager.getColor("bg"));
                for (Component inner : panel.getComponents()) {
                    if (inner instanceof JLabel) {
                        JLabel lbl = (JLabel) inner;
                        if (lbl.getFont().getSize() >= 18) {
                            lbl.setForeground(ThemeManager.getColor("textPrimary"));
                        } else if (lbl.getFont().getSize() <= 13) {
                            lbl.setForeground(ThemeManager.getColor("textSecondary"));
                        } else {
                            lbl.setForeground(ThemeManager.getColor("textPrimary"));
                        }
                    } else if (inner instanceof JButton) {
                        JButton btn = (JButton) inner;
                        if (btn.getText().equals("Sửa") || btn.getText().equals("Edit")) {
                            btn.setBackground(ThemeManager.getColor("success"));
                            btn.setForeground(ThemeManager.getColor("bg"));
                        } else if (btn.getText().equals("Xóa") || btn.getText().equals("Delete")) {
                            btn.setBackground(ThemeManager.getColor("danger"));
                            btn.setForeground(ThemeManager.getColor("bg"));
                        } else {
                            btn.setBackground(ThemeManager.getColor("surface"));
                            btn.setForeground(ThemeManager.getColor("textPrimary"));
                        }
                    }
                }
            }
        }
        if (getContentPane().getComponentCount() > 2) {
            Component detailComp = getContentPane().getComponent(2);
            if (detailComp instanceof JPanel) {
                for (Component inner : ((JPanel) detailComp).getComponents()) {
                    if (inner instanceof JLabel) {
                        JLabel lbl = (JLabel) inner;
                        if (lbl.getFont().isBold()) {
                            lbl.setForeground(ThemeManager.getColor("textSecondary"));
                        } else {
                            if (lbl.getFont().getSize() > 14) {
                                lbl.setForeground(transaction.getType() == TransactionType.INCOME ? ThemeManager.getColor("success") : ThemeManager.getColor("danger"));
                            } else {
                                lbl.setForeground(ThemeManager.getColor("textPrimary"));
                            }
                        }
                    }
                }
            }
        }
    }

    private void editTransaction() {
        JTextField txtAmount = new JTextField(String.valueOf(transaction.getAmount()));
        JTextField txtNote = new JTextField(transaction.getNote());

        JPanel panel = new JPanel(new GridLayout(4, 1, 5, 5));
        panel.add(new JLabel(isVietnamese ? "Số tiền mới:" : "New amount:"));
        panel.add(txtAmount);
        panel.add(new JLabel(isVietnamese ? "Ghi chú mới:" : "New note:"));
        panel.add(txtNote);

        int result = JOptionPane.showConfirmDialog(this, panel, isVietnamese ? "Sửa giao dịch" : "Edit transaction", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            try {
                double newAmount = Double.parseDouble(txtAmount.getText().trim());
                if (newAmount <= 0) {
                    JOptionPane.showMessageDialog(this, isVietnamese ? "Số tiền phải lớn hơn 0!" : "Amount must be greater than 0!");
                    return;
                }
                transaction.setAmount(newAmount);
                transaction.setNote(txtNote.getText().trim());

                if (mainFrame != null && mainFrame.getFinanceService() != null) {
                    mainFrame.getFinanceService().updateTransaction(transaction);
                } else {
                    DatabaseUtil.updateTransaction(transaction);
                }
                JOptionPane.showMessageDialog(this, isVietnamese ? "Cập nhật thành công!" : "Update successful!");
                dispose();
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, isVietnamese ? "Số tiền không hợp lệ!" : "Invalid amount!");
            }
        }
    }

    private void deleteTransaction() {
        int confirm = JOptionPane.showConfirmDialog(this,
                isVietnamese ? "Bạn có chắc muốn xóa giao dịch này?" : "Are you sure you want to delete this transaction?",
                isVietnamese ? "Xác nhận xóa" : "Confirm delete",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            if (mainFrame != null && mainFrame.getFinanceService() != null) {
                mainFrame.getFinanceService().deleteTransaction(transaction.getId());
            } else {
                DatabaseUtil.deleteTransaction(transaction.getId());
            }
            JOptionPane.showMessageDialog(this, isVietnamese ? "Đã xóa giao dịch!" : "Transaction deleted!");
            dispose();
        }
    }

    private void addDetailRow(JPanel panel, GridBagConstraints gbc, String label, String value, int row) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panel.add(lblLabel, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(lblValue, gbc);
    }

    private JButton createFlatButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}