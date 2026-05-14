package com.expensemanager.ui;

import com.expensemanager.database.DatabaseUtil;
import com.expensemanager.entity.Category;
import com.expensemanager.entity.Transaction;
import com.expensemanager.service.SessionManager;

import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class TransactionDetailDialog extends JDialog {

    private static final Map<String, String> CATEGORY_EMOJI = new HashMap<>();
    static {
        CATEGORY_EMOJI.put("Mua sắm", "🛍️");
        CATEGORY_EMOJI.put("Ăn uống", "🍔");
        CATEGORY_EMOJI.put("Điện thoại", "📱");
        CATEGORY_EMOJI.put("Giải trí", "🎮");
        CATEGORY_EMOJI.put("Giáo dục", "📚");
        CATEGORY_EMOJI.put("Làm đẹp", "💄");
        CATEGORY_EMOJI.put("Thể thao", "⚽");
        CATEGORY_EMOJI.put("Xã hội", "👥");
        CATEGORY_EMOJI.put("Di chuyển", "🚗");
        CATEGORY_EMOJI.put("Quần áo", "👗");
        CATEGORY_EMOJI.put("Xe cộ", "🏍️");
        CATEGORY_EMOJI.put("Điện tử", "💻");
        CATEGORY_EMOJI.put("Du lịch", "✈️");
        CATEGORY_EMOJI.put("Sức khỏe", "🏥");
        CATEGORY_EMOJI.put("Sửa chữa", "🔧");
        CATEGORY_EMOJI.put("Nhà cửa", "🏠");
        CATEGORY_EMOJI.put("Quà tặng", "🎁");
        CATEGORY_EMOJI.put("Từ thiện", "💖");
        CATEGORY_EMOJI.put("Ăn vặt", "🍿");
        CATEGORY_EMOJI.put("Trái cây", "🍎");
        CATEGORY_EMOJI.put("Lương", "💰");
        CATEGORY_EMOJI.put("Học bổng", "🎓");
        CATEGORY_EMOJI.put("Tiền được cho", "💵");
    }

    private MainFrame mainFrame;
    private Transaction transaction;

    public TransactionDetailDialog(MainFrame mainFrame, Transaction transaction) {
        super(mainFrame, "Chi tiết giao dịch", true);
        this.mainFrame = mainFrame;
        this.transaction = transaction;
        setSize(400, 350);
        setLocationRelativeTo(mainFrame);
        setLayout(new BorderLayout());

        // Panel tiêu đề: icon + ghi chú + ngày
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        headerPanel.setBackground(Color.WHITE);

        Category cat = transaction.getCategory();
        String emoji = (cat != null) ? CATEGORY_EMOJI.getOrDefault(cat.getName(), "📌") : "📌";
        JLabel lblIcon = new JLabel(emoji);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
        headerPanel.add(lblIcon, BorderLayout.WEST);

        JPanel textPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        textPanel.setOpaque(false);

        String note = transaction.getNote();
        JLabel lblNote = new JLabel(note != null && !note.isEmpty() ? note : "Không có ghi chú");
        lblNote.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblNote.setForeground(Color.BLACK);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        JLabel lblDate = new JLabel(transaction.getDateTime().format(dtf));
        lblDate.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblDate.setForeground(Color.GRAY);

        textPanel.add(lblNote);
        textPanel.add(lblDate);

        headerPanel.add(textPanel, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

        // Panel thông tin chi tiết
        JPanel detailPanel = new JPanel(new GridBagLayout());
        detailPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        detailPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        addDetailRow(detailPanel, gbc, "Danh mục:", cat != null ? cat.getName() : "Không có", 0);
        addDetailRow(detailPanel, gbc, "Số tiền:", String.format("%,.0f VND", transaction.getAmount()), 1);
        addDetailRow(detailPanel, gbc, "Loại:", transaction.getType().name(), 2);
        addDetailRow(detailPanel, gbc, "Ngày giờ:", transaction.getDateTime().format(dtf), 3);

        add(detailPanel, BorderLayout.CENTER);

        // Panel nút Sửa, Xóa, Đóng
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(Color.WHITE);

        JButton btnEdit = new JButton("Sửa");
        btnEdit.setBackground(new Color(0, 153, 76));
        btnEdit.setForeground(Color.WHITE);
        btnEdit.addActionListener(e -> editTransaction());

        JButton btnDelete = new JButton("Xóa");
        btnDelete.setBackground(Color.RED);
        btnDelete.setForeground(Color.WHITE);
        btnDelete.addActionListener(e -> deleteTransaction());

        JButton btnClose = new JButton("Đóng");
        btnClose.addActionListener(e -> dispose());

        buttonPanel.add(btnEdit);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnClose);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void addDetailRow(JPanel panel, GridBagConstraints gbc, String label, String value, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        panel.add(lblLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = row;
        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        panel.add(lblValue, gbc);
    }

    private void editTransaction() {
        JTextField txtAmount = new JTextField(String.valueOf(transaction.getAmount()));
        JTextField txtNote = new JTextField(transaction.getNote());

        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(new JLabel("Số tiền:"));
        panel.add(txtAmount);
        panel.add(new JLabel("Ghi chú:"));
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
                DatabaseUtil.updateTransaction(transaction);
                JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
                dispose();
                mainFrame.refreshAllPanels();
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Số tiền không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteTransaction() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn xóa giao dịch này?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            DatabaseUtil.deleteTransaction(transaction.getId());
            JOptionPane.showMessageDialog(this, "Đã xóa giao dịch!");
            dispose();
            mainFrame.refreshAllPanels();
        }
    }
}