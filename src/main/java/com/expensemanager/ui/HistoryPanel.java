package com.expensemanager.ui;

import com.expensemanager.entity.Transaction;
import com.expensemanager.entity.TransactionType;
import com.expensemanager.service.FinanceService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class HistoryPanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private FinanceService financeService;

    // Constructor nhận FinanceService
    public HistoryPanel(FinanceService financeService) {
        this.financeService = financeService;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JLabel title = new JLabel("LỊCH SỬ GIAO DỊCH", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(title, BorderLayout.NORTH);

        String[] columns = {"ID", "Số tiền", "Loại", "Danh mục", "Ngày giờ", "Ghi chú"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        JButton btnRefresh = new JButton("Làm mới");
        btnRefresh.addActionListener(e -> refreshData());
        add(btnRefresh, BorderLayout.SOUTH);

        refreshData();
    }

    public void refreshData() {
        tableModel.setRowCount(0);
        if (financeService == null) return;

        for (Transaction t : financeService.getAllTransactions()) {
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
}