package com.expensemanager.ui;

import com.expensemanager.entity.Transaction;
import com.expensemanager.entity.TransactionType;
import com.expensemanager.service.FinanceService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.RowFilter;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class HistoryPanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private FinanceService financeService;
    private JTextField txtSearch;
    private JComboBox<String> cmbFilter;
    private TableRowSorter<DefaultTableModel> sorter;

    public HistoryPanel(FinanceService financeService) {
        this.financeService = financeService;
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
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));

        // Sorter để hỗ trợ lọc
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // Panel tìm kiếm và lọc
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        filterPanel.setBackground(Color.WHITE);

        filterPanel.add(new JLabel("Tìm kiếm:"));
        txtSearch = new JTextField(15);
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilters(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { applyFilters(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { applyFilters(); }
        });
        filterPanel.add(txtSearch);

        filterPanel.add(new JLabel("Lọc:"));
        cmbFilter = new JComboBox<>(new String[]{"Tất cả", "Thu nhập", "Chi tiêu"});
        cmbFilter.addActionListener(e -> applyFilters());
        filterPanel.add(cmbFilter);

        JButton btnRefresh = new JButton("Làm mới");
        btnRefresh.addActionListener(e -> refreshData());
        filterPanel.add(btnRefresh);

        add(filterPanel, BorderLayout.SOUTH);

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
        // Áp dụng lại bộ lọc sau khi làm mới
        applyFilters();
    }

    private void applyFilters() {
        String searchText = txtSearch.getText().trim().toLowerCase();
        String filterType = (String) cmbFilter.getSelectedItem();

        List<RowFilter<Object, Object>> filters = new ArrayList<>();

        // Lọc theo loại giao dịch
        if ("Thu nhập".equals(filterType)) {
            filters.add(RowFilter.regexFilter("Thu", 2)); // cột Loại (index 2)
        } else if ("Chi tiêu".equals(filterType)) {
            filters.add(RowFilter.regexFilter("Chi", 2));
        }

        // Lọc theo từ khóa (tìm trong tất cả các cột)
        if (!searchText.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + searchText));
        }

        sorter.setRowFilter(filters.isEmpty() ? null : RowFilter.andFilter(filters));
    }
}