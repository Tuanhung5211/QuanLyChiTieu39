package com.expensemanager.ui;

import javax.swing.*;
import java.awt.*;

public class AddTransactionDialog extends JDialog {
    private JTextField txtAmount, txtNote;
    private JComboBox<String> cbType;
    private JButton btnSave, btnCancel;

    public AddTransactionDialog(JFrame parent) {
        super(parent, "Thêm Giao Dịch Mới", true);
        setSize(350, 250);
        setLocationRelativeTo(parent);
        setLayout(new GridLayout(4, 2, 10, 10));

        add(new JLabel("Loại giao dịch:"));
        cbType = new JComboBox<>(new String[]{"Chi tiêu (Expense)", "Thu nhập (Income)"});
        add(cbType);

        add(new JLabel("Số tiền (VNĐ):"));
        txtAmount = new JTextField();
        add(txtAmount);

        add(new JLabel("Ghi chú:"));
        txtNote = new JTextField();
        add(txtNote);

        btnSave = new JButton("Lưu");
        btnCancel = new JButton("Hủy");

        add(btnSave);
        add(btnCancel);

        // ĐÂY LÀ PHẦN BẮT LỖI TRY-CATCH YÊU CẦU
        btnSave.addActionListener(e -> {
            try {
                // Parse dữ liệu từ String sang double
                String amountStr = txtAmount.getText().trim();
                if (amountStr.isEmpty()) {
                    throw new NumberFormatException();
                }
                
                double amount = Double.parseDouble(amountStr);
                
                if (amount <= 0) {
                    JOptionPane.showMessageDialog(this, "Số tiền phải lớn hơn 0!", "Lỗi logic", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                String note = txtNote.getText();
                String type = cbType.getSelectedIndex() == 0 ? "EXPENSE" : "INCOME";

                // CHỖ NÀY GỌI SERVICE CỦA BẠN C / A
                // financeService.addTransaction(new Transaction(amount, type, note));

                JOptionPane.showMessageDialog(this, "Thêm thành công!");
                dispose(); // Đóng dialog

            } catch (NumberFormatException ex) {
                // Bắt lỗi khi người dùng nhập chữ vào ô số tiền
                JOptionPane.showMessageDialog(this, "Lỗi nhập liệu: Số tiền phải là số hợp lệ (VD: 50000)!", "Lỗi định dạng", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnCancel.addActionListener(e -> dispose());
    }
}