package com.expensemanager.ui;

import com.expensemanager.database.DatabaseUtil;
import com.expensemanager.entity.Category;
import com.expensemanager.entity.Transaction;
import com.expensemanager.entity.TransactionType;
import com.expensemanager.util.EmojiUtil;

import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;

public class TransactionDetailDialog extends JDialog {

    // =====================================================================
    // 1. KHAI BÁO BIẾN GIAO DIỆN VÀ LOGIC
    // =====================================================================
    private MainFrame mainFrame;
    private Transaction transaction;

    private final Color BG_COLOR = new Color(18, 18, 18);
    private final Color SURFACE_COLOR = new Color(30, 30, 30);
    private final Color ACCENT_YELLOW = new Color(255, 193, 7);
    private final Color TEXT_PRIMARY = new Color(240, 240, 240);
    private final Color TEXT_MUTED = new Color(150, 150, 150);

    // =====================================================================
    // 2. CONSTRUCTOR - KHỞI TẠO BỐ CỤC UI
    // =====================================================================
    public TransactionDetailDialog(MainFrame mainFrame, Transaction transaction) {
        super(mainFrame, "Chi tiết giao dịch", true);
        this.mainFrame = mainFrame;
        this.transaction = transaction;

        setSize(420, 420);
        setLocationRelativeTo(mainFrame);
        getContentPane().setBackground(BG_COLOR);
        setLayout(new BorderLayout());

        initComponents();
    }

    private void initComponents() {
        // --- HEADER PANEL ---
        JPanel headerPanel = new JPanel(new BorderLayout(15, 0));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 15, 25));
        headerPanel.setBackground(BG_COLOR);

        Category cat = transaction.getCategory();
        String emoji = (cat != null) ? EmojiUtil.CATEGORY_EMOJI.getOrDefault(cat.getName(), "\uD83D\uDCCD") : "\uD83D\uDCCD";

        JLabel lblIcon = new JLabel(emoji);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 44));
        lblIcon.setForeground(Color.WHITE);
        headerPanel.add(lblIcon, BorderLayout.WEST);

        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        textPanel.setOpaque(false);
        String note = transaction.getNote();
        JLabel lblNote = new JLabel(note != null && !note.isEmpty() ? note : "Không có ghi chú");
        lblNote.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblNote.setForeground(TEXT_PRIMARY);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        JLabel lblDate = new JLabel(transaction.getDateTime().format(dtf));
        lblDate.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblDate.setForeground(TEXT_MUTED);

        textPanel.add(lblNote);
        textPanel.add(lblDate);
        headerPanel.add(textPanel, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

        // --- DETAILS PANEL ---
        JPanel detailPanel = new JPanel(new GridBagLayout());
        detailPanel.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        detailPanel.setBackground(BG_COLOR);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.weightx = 1.0;

        addDetailRow(detailPanel, gbc, "Danh mục:", cat != null ? cat.getName() : "Không có", 0);

        String amountStr = String.format("%,.0f VND", transaction.getAmount());
        addDetailRow(detailPanel, gbc, "Số tiền:", amountStr, 1);
        JLabel amountLabelValue = (JLabel) detailPanel.getComponent(3);
        amountLabelValue.setForeground(transaction.getType() == TransactionType.INCOME ? new Color(76, 175, 80) : new Color(244, 67, 54));
        amountLabelValue.setFont(new Font("Segoe UI", Font.BOLD, 15));

        addDetailRow(detailPanel, gbc, "Loại hành động:", transaction.getType().name(), 2);
        addDetailRow(detailPanel, gbc, "Thời gian lưu:", transaction.getDateTime().format(dtf), 3);
        add(detailPanel, BorderLayout.CENTER);

        // --- FOOTER PANEL BUTTONS ---
        JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 12, 0));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 25, 25, 25));
        buttonPanel.setBackground(BG_COLOR);

        JButton btnEdit = createFlatButton("Sửa", new Color(0, 153, 76), Color.WHITE);
        btnEdit.addActionListener(e -> editTransaction());

        JButton btnDelete = createFlatButton("Xóa", new Color(244, 67, 54), Color.WHITE);
        btnDelete.addActionListener(e -> deleteTransaction());

        JButton btnClose = createFlatButton("Đóng", SURFACE_COLOR, TEXT_PRIMARY);
        btnClose.addActionListener(e -> dispose());

        buttonPanel.add(btnEdit);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnClose);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    // =====================================================================
    // 3. XỬ LÝ LOGIC NGHIỆP VỤ (SỬA / XÓA)
    // =====================================================================
    private void editTransaction() {
        JTextField txtAmount = new JTextField(String.valueOf(transaction.getAmount()));
        JTextField txtNote = new JTextField(transaction.getNote());

        JPanel panel = new JPanel(new GridLayout(4, 1, 5, 5));
        panel.add(new JLabel("Số tiền mới:"));
        panel.add(txtAmount);
        panel.add(new JLabel("Ghi chú mới:"));
        panel.add(txtNote);

        int result = JOptionPane.showConfirmDialog(this, panel, "Sửa giao dịch", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            try {
                double newAmount = Double.parseDouble(txtAmount.getText().trim());
                if (newAmount <= 0) {
                    JOptionPane.showMessageDialog(this, "Số tiền phải lớn hơn 0!");
                    return;
                }
                transaction.setAmount(newAmount);
                transaction.setNote(txtNote.getText().trim());

                if (mainFrame != null && mainFrame.getFinanceService() != null) {
                    mainFrame.getFinanceService().updateTransaction(transaction);
                } else {
                    DatabaseUtil.updateTransaction(transaction);
                }
                JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
                dispose();
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Số tiền không hợp lệ!");
            }
        }
    }

    private void deleteTransaction() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn xóa giao dịch này?", "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            if (mainFrame != null && mainFrame.getFinanceService() != null) {
                mainFrame.getFinanceService().deleteTransaction(transaction.getId());
            } else {
                DatabaseUtil.deleteTransaction(transaction.getId());
            }
            JOptionPane.showMessageDialog(this, "Đã xóa giao dịch!");
            dispose();
        }
    }

    // =====================================================================
    // 4. TIỆN ÍCH GIAO DIỆN CON (UI HELPERS)
    // =====================================================================
    private void addDetailRow(JPanel panel, GridBagConstraints gbc, String label, String value, int row) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblLabel.setForeground(TEXT_MUTED);
        panel.add(lblLabel, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblValue.setForeground(TEXT_PRIMARY);
        panel.add(lblValue, gbc);
    }

    private JButton createFlatButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}