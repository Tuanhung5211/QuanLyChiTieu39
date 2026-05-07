package com.expensemanager.ui;

import com.expensemanager.database.DatabaseUtil;
import com.expensemanager.entity.Transaction;

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

        refreshData();
    }

    public void refreshData() {
        // Xóa dữ liệu cũ
        tableModel.setRowCount(0);

        List<Transaction> transactions = DatabaseUtil.getAllTransactions();
        for (Transaction t : transactions) {
            Object[] row = {
                    t.getId(),
                    String.format("%,.0f VND", t.getAmount()),
                    t.getType() == com.expensemanager.entity.TransactionType.INCOME ? "Thu" : "Chi",
                    t.getCategory().getName(),
                    t.getDateTime().toString().replace("T", " "),
                    t.getNote()
            };
            tableModel.addRow(row);
        }
    }
}