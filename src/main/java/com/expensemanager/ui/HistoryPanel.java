package com.expensemanager.ui;

import com.expensemanager.database.DatabaseUtil;
import com.expensemanager.entity.Transaction;
import com.expensemanager.entity.TransactionType;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class HistoryPanel extends JPanel {

    private JTable table;
    private DefaultTableModel tableModel;

    public HistoryPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Tiêu đề
        JLabel title = new JLabel("LỊCH SỬ GIAO DỊCH", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(title, BorderLayout.NORTH);

        // Bảng
        String[] columns = {"ID", "Số tiền", "Loại", "Danh mục", "Ngày giờ", "Ghi chú"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Không cho sửa trực tiếp trên bảng
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // Nút làm mới
        JButton btnRefresh = new JButton("Làm mới");
        btnRefresh.addActionListener(e -> refreshData());
        add(btnRefresh, BorderLayout.SOUTH);

        // Tải dữ liệu lần đầu
        refreshData();
    }

    public void refreshData() {
        // Xóa dữ liệu cũ
        tableModel.setRowCount(0);

        try {
            List<Transaction> transactions = DatabaseUtil.getAllTransactions();
            if (transactions != null) {
                for (Transaction t : transactions) {
                    if (t != null) {
                        String categoryName = (t.getCategory() != null) ? t.getCategory().getName() : "Không có";
                        String typeStr = (t.getType() == TransactionType.INCOME) ? "Thu" : "Chi";
                        Object[] row = {
                                t.getId(),
                                String.format("%,.0f VND", t.getAmount()),
                                typeStr,
                                categoryName,
                                t.getDateTime().toString().replace("T", " "),
                                t.getNote() != null ? t.getNote() : ""
                        };
                        tableModel.addRow(row);
                    }
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi khi tải dữ liệu giao dịch: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}