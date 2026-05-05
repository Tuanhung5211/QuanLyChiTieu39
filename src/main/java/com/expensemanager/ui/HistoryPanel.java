package com.expensemanager.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Vector;

public class HistoryPanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;

    public HistoryPanel() {
        setLayout(new BorderLayout());

        // Sử dụng Vector theo đúng yêu cầu
        Vector<String> columnNames = new Vector<>();
        columnNames.add("ID");
        columnNames.add("Ngày");
        columnNames.add("Loại");
        columnNames.add("Danh mục");
        columnNames.add("Số tiền");
        columnNames.add("Ghi chú");

        // Dữ liệu mẫu (Vector lồng Vector)
        Vector<Vector<Object>> data = new Vector<>();
        
        tableModel = new DefaultTableModel(data, columnNames);
        table = new JTable(tableModel);
        
        JScrollPane scrollPane = new JScrollPane(table);
        add(new JLabel("LỊCH SỬ GIAO DỊCH", SwingConstants.CENTER), BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    // Hàm gọi Service lấy list giao dịch đổ vào bảng
    public void refreshTable() {
        // tableModel.setRowCount(0); // Xóa data cũ
        // List<Transaction> list = financeService.getAll();
        // for(Transaction t : list) {
        //     Vector<Object> row = new Vector<>();
        //     row.add(t.getId()); ...
        //     tableModel.addRow(row);
        // }
    }
}