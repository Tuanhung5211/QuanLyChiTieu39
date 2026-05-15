package com.expensemanager.ui;

import com.expensemanager.database.DatabaseUtil;
import com.expensemanager.entity.Transaction;
import com.expensemanager.entity.TransactionType;

import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;

public class TransactionDetailDialog extends JDialog {
    private MainFrame mainFrame;
    private Transaction transaction;

    private final Color BG_COLOR = new Color(18, 18, 18);
    private final Color SURFACE_COLOR = new Color(30, 30, 30);
    private final Color ACCENT_YELLOW = new Color(255, 193, 7);

    public TransactionDetailDialog(MainFrame parent, Transaction t) {
        super(parent, "Chi tiết giao dịch", true);
        this.mainFrame = parent;
        this.transaction = t;

        setSize(400, 480);
        setLocationRelativeTo(parent);
        getContentPane().setBackground(BG_COLOR);
        setLayout(new BorderLayout());

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(BG_COLOR);
        content.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // Lấy Emoji (Có kiểm tra CustomEmojiMap từ AddTransactionDialog)
        String categoryName = t.getCategory().getName();
        String defaultEmoji = DashboardPanel.CATEGORY_EMOJI.getOrDefault(categoryName, "📌");
        String emoji = AddTransactionDialog.customEmojiMap.getOrDefault(categoryName, defaultEmoji);

        JLabel lblIcon = new JLabel(emoji, SwingConstants.CENTER);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 55));
        lblIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Số tiền
        JLabel lblAmount = new JLabel(String.format("%,.0f ₫", t.getAmount()));
        lblAmount.setFont(new Font("Segoe UI", Font.BOLD, 36));
        lblAmount.setForeground(t.getType() == TransactionType.INCOME ? new Color(76, 175, 80) : new Color(244, 67, 54));
        lblAmount.setAlignmentX(Component.CENTER_ALIGNMENT);

        content.add(lblIcon);
        content.add(Box.createVerticalStrut(15));
        content.add(lblAmount);
        content.add(Box.createVerticalStrut(35));

        // Thông tin chi tiết
        content.add(createDetailRow("Danh mục", categoryName));
        content.add(createDetailRow("Thời gian", t.getDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
        content.add(createDetailRow("Ghi chú", t.getNote().isEmpty() ? "---" : t.getNote()));

        add(content, BorderLayout.CENTER);

        // --- FOOTER: KHÔI PHỤC NÚT SỬA VÀ XÓA ---
        JPanel footer = new JPanel(new GridLayout(1, 2, 10, 0));
        footer.setBackground(BG_COLOR);
        footer.setBorder(BorderFactory.createEmptyBorder(10, 40, 30, 40));

        JButton btnEdit = new JButton("SỬA");
        btnEdit.setBackground(SURFACE_COLOR);
        btnEdit.setForeground(Color.WHITE);
        btnEdit.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnEdit.setFocusPainted(false);
        btnEdit.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));
        btnEdit.addActionListener(e -> editTransaction());

        JButton btnDelete = new JButton("XÓA");
        btnDelete.setBackground(new Color(244, 67, 54));
        btnDelete.setForeground(Color.WHITE);
        btnDelete.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnDelete.setFocusPainted(false);
        btnDelete.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));
        btnDelete.addActionListener(e -> deleteTransaction());

        footer.add(btnEdit);
        footer.add(btnDelete);

        add(footer, BorderLayout.SOUTH);
    }

    private JPanel createDetailRow(String label, String value) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG_COLOR);
        p.setMaximumSize(new Dimension(400, 40));
        p.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(40, 40, 40)));

        JLabel l = new JLabel(label);
        l.setForeground(new Color(150, 150, 150));
        l.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JLabel v = new JLabel(value);
        v.setForeground(Color.WHITE);
        v.setFont(new Font("Segoe UI", Font.BOLD, 14));

        p.add(l, BorderLayout.WEST);
        p.add(v, BorderLayout.EAST);
        return p;
    }

    // --- KHÔI PHỤC LOGIC EDIT ---
    private void editTransaction() {
        JPanel panel = new JPanel(new GridLayout(4, 1, 5, 5));

        JTextField txtAmount = new JTextField(String.valueOf(transaction.getAmount()));
        JTextField txtNote = new JTextField(transaction.getNote());

        panel.add(new JLabel("Số tiền mới:"));
        panel.add(txtAmount);
        panel.add(new JLabel("Ghi chú mới:"));
        panel.add(txtNote);

        int result = JOptionPane.showConfirmDialog(this, panel, "Sửa giao dịch", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                double newAmount = Double.parseDouble(txtAmount.getText().trim());
                if (newAmount <= 0) {
                    JOptionPane.showMessageDialog(this, "Số tiền phải lớn hơn 0!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                transaction.setAmount(newAmount);
                transaction.setNote(txtNote.getText().trim());
                DatabaseUtil.updateTransaction(transaction); // Cập nhật vào DB

                JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
                dispose();
                mainFrame.refreshAllPanels();
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Số tiền không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // --- KHÔI PHỤC LOGIC DELETE ---
    private void deleteTransaction() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn xóa giao dịch này?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            DatabaseUtil.deleteTransaction(transaction.getId()); // Xóa khỏi DB
            JOptionPane.showMessageDialog(this, "Đã xóa giao dịch!");
            dispose();
            mainFrame.refreshAllPanels();
        }
    }
}