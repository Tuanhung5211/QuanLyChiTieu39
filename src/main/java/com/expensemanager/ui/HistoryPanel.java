package com.expensemanager.ui;

import com.expensemanager.database.DatabaseUtil;
import com.expensemanager.entity.Transaction;
import com.expensemanager.entity.TransactionType;
import com.expensemanager.observer.EventType;
import com.expensemanager.observer.Observer;
import com.expensemanager.service.FinanceService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HistoryPanel extends JPanel implements Observer {

    private static final Logger LOGGER = Logger.getLogger(HistoryPanel.class.getName());

    private final JTable table;
    private final DefaultTableModel tableModel;
    private final FinanceService financeService;

    // Constructor nhan 1 tham so FinanceService
    public HistoryPanel(FinanceService financeService) {
        this.financeService = financeService;

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Tieu de
        JLabel title = new JLabel("LICH SU GIAO DICH", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(title, BorderLayout.NORTH);

        // Bang
        String[] columns = {"ID", "So tien", "Loai", "Danh muc", "Ngay gio", "Ghi chu"};
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

        // Nut lam moi
        JButton btnRefresh = new JButton("Lam moi");
        btnRefresh.addActionListener(e -> refreshData());
        add(btnRefresh, BorderLayout.SOUTH);

        // Dang ky observer
        financeService.attach(this);

        // Tai du lieu lan dau
        refreshData();
    }

    @Override
    public void update(EventType eventType, Object data) {
        SwingUtilities.invokeLater(() -> {
            switch (eventType) {
                case TRANSACTION_ADDED:
                    if (data instanceof Transaction) {
                        addTransactionToTable((Transaction) data);
                        int lastRow = tableModel.getRowCount() - 1;
                        table.scrollRectToVisible(table.getCellRect(lastRow, 0, true));
                    }
                    break;

                case TRANSACTION_DELETED:
                    if (data instanceof Transaction) {
                        removeTransactionFromTable((Transaction) data);
                    }
                    break;

                case TRANSACTION_UPDATED:
                case DATA_LOADED:
                    refreshData();
                    break;

                default:
                    break;
            }
        });
    }

    private void addTransactionToTable(Transaction t) {
        if (t == null) return;

        String categoryName = (t.getCategory() != null) ? t.getCategory().getName() : "Khong co";
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

    private void removeTransactionFromTable(Transaction t) {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (tableModel.getValueAt(i, 0).equals(t.getId())) {
                tableModel.removeRow(i);
                break;
            }
        }
    }

    public void refreshData() {
        tableModel.setRowCount(0);

        try {
            List<Transaction> transactions = DatabaseUtil.getAllTransactions();
            if (transactions != null) {
                for (Transaction t : transactions) {
                    if (t != null) {
                        String categoryName = (t.getCategory() != null) ? t.getCategory().getName() : "Khong co";
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
            LOGGER.log(Level.SEVERE, "Loi tai du lieu giao dich", e);
            JOptionPane.showMessageDialog(this,
                    "Loi khi tai du lieu giao dich: " + e.getMessage(),
                    "Loi", JOptionPane.ERROR_MESSAGE);
        }
    }
}