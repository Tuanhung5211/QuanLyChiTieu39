package com.expensemanager.ui;

import com.expensemanager.database.DatabaseUtil;
import com.expensemanager.entity.Transaction;
import com.expensemanager.entity.TransactionType;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class HistoryPanel extends JPanel {
    private JTable table;
    private DefaultTableModel model;
    private JLabel lblCurrentView;
    private int filterMonth = -1, filterYear = -1;
    private MainFrame mainFrame;

    public HistoryPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());
        setBackground(new Color(18, 18, 18));
        setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        JPanel nav = new JPanel(new FlowLayout(FlowLayout.CENTER, 35, 15));
        nav.setOpaque(false);
        JButton btnP = new JButton("<"); 
        JButton btnN = new JButton(">");
        lblCurrentView = new JLabel("Đang xem: Tất cả thời gian");
        lblCurrentView.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblCurrentView.setForeground(Color.WHITE);
        JButton btnA = new JButton("Xem tất cả");
        nav.add(btnP); nav.add(lblCurrentView); nav.add(btnN); nav.add(btnA);
        add(nav, BorderLayout.NORTH);

        String[] cols = {"ID", "Số tiền", "Loại", "Danh mục", "Ngày", "Ghi chú"};
        model = new DefaultTableModel(cols, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        table = new JTable(model);
        table.setBackground(new Color(30, 30, 30));
        table.setForeground(Color.WHITE);
        table.setRowHeight(38);
        table.setGridColor(new Color(55, 55, 55));
        table.getTableHeader().setBackground(new Color(45, 45, 45));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 15));

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(JLabel.CENTER);
        for(int i=0; i<6; i++) table.getColumnModel().getColumn(i).setCellRenderer(center);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bp = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 15));
        bp.setOpaque(false);
        JButton bAdd = new JButton("Thêm mới");
        JButton bEdit = new JButton("Sửa");
        JButton bDel = new JButton("Xóa");
        bp.add(bAdd); bp.add(bEdit); bp.add(bDel);
        add(bp, BorderLayout.SOUTH);

        btnP.addActionListener(e -> changeMonth(-1));
        btnN.addActionListener(e -> changeMonth(1));
        btnA.addActionListener(e -> { filterMonth = -1; refreshData(); });
        bAdd.addActionListener(e -> new AddTransactionDialog(mainFrame).setVisible(true));
        
        bDel.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r != -1) {
                String id = model.getValueAt(r, 0).toString();
                if (JOptionPane.showConfirmDialog(this, "Xóa giao dịch này?", "Xác nhận", 0) == 0) {
                    DatabaseUtil.deleteTransaction(id);
                    mainFrame.refreshAllPanels();
                }
            }
        });

        bEdit.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r != -1) {
                String id = model.getValueAt(r, 0).toString();
                Transaction t = DatabaseUtil.getAllTransactions().stream().filter(i -> i.getId().equals(id)).findFirst().orElse(null);
                if (t != null) new AddTransactionDialog(mainFrame, t).setVisible(true);
            }
        });

        refreshData();
    }

    private void changeMonth(int d) {
        if (filterMonth == -1) { filterMonth = LocalDate.now().getMonthValue(); filterYear = LocalDate.now().getYear(); }
        else { filterMonth += d; if (filterMonth > 12) { filterMonth = 1; filterYear++; } else if (filterMonth < 1) { filterMonth = 12; filterYear--; } }
        refreshData();
    }

    public void refreshData() {
        if (filterMonth == -1) lblCurrentView.setText("Đang xem: Tất cả thời gian");
        else lblCurrentView.setText(String.format("Tháng %02d / %d", filterMonth, filterYear));
        model.setRowCount(0);
        List<Transaction> list = DatabaseUtil.getAllTransactions();
        List<Transaction> filtered = list.stream().filter(t -> {
            if (filterMonth == -1) return true;
            return t.getDateTime().getMonthValue() == filterMonth && t.getDateTime().getYear() == filterYear;
        }).collect(Collectors.toList());
        for (Transaction t : filtered) {
            model.addRow(new Object[]{t.getId(), String.format("%,.0f VND", t.getAmount()), t.getType() == TransactionType.INCOME ? "Thu" : "Chi", t.getCategory().getName(), t.getDateTime().toLocalDate().toString(), t.getNote()});
        }
    }
}