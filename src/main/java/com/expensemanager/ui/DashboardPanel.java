package com.expensemanager.ui;

import com.expensemanager.entity.Category;
import com.expensemanager.entity.Transaction;
import com.expensemanager.entity.TransactionType;
import com.expensemanager.observer.EventType;
import com.expensemanager.observer.Observer;
import com.expensemanager.service.BudgetManager;
import com.expensemanager.service.FinanceService;
import com.expensemanager.util.EmojiUtil;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class DashboardPanel extends JPanel implements Observer {
    private MainFrame mainFrame;
    private FinanceService financeService;
    private BudgetManager budgetManager;

    private JLabel lblMonthYear, lblIncome, lblExpense, lblBalance;
    private JPanel transactionListPanel;
    private JScrollPane scrollPane;
    private JTextField txtSearch;
    private JComboBox<String> cmbFilter;

    private final Color BG_COLOR = new Color(30, 30, 30);
    private final Color SURFACE_COLOR = new Color(40, 40, 40);
    private final Color INPUT_BG = new Color(45, 45, 45);
    private final Color ACCENT_YELLOW = new Color(255, 193, 7);
    private final Color TEXT_PRIMARY = new Color(240, 240, 240);

    public DashboardPanel(MainFrame mainFrame, FinanceService financeService, BudgetManager budgetManager) {
        this.mainFrame = mainFrame;
        this.financeService = financeService;
        this.budgetManager = budgetManager;

        setLayout(new BorderLayout());
        setBackground(BG_COLOR);

        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setOpaque(false);

        topContainer.add(createHeader());

        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        filterBar.setBackground(BG_COLOR);
        filterBar.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        // Kính lúp mã Unicode chuẩn không dùng kí tự thô
        JLabel lblSearchIcon = new JLabel("\uD83D\uDD0D");
        lblSearchIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        filterBar.add(lblSearchIcon);

        txtSearch = new JTextField(20);
        txtSearch.setBackground(INPUT_BG); txtSearch.setForeground(TEXT_PRIMARY); txtSearch.setCaretColor(ACCENT_YELLOW);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtSearch.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(60, 60, 60)), BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { refreshData(); }
            public void removeUpdate(DocumentEvent e) { refreshData(); }
            public void changedUpdate(DocumentEvent e) { refreshData(); }
        });
        filterBar.add(txtSearch);

        cmbFilter = new JComboBox<>(new String[]{"Tất cả", "Thu nhập", "Chi tiêu"});
        cmbFilter.setBackground(INPUT_BG); cmbFilter.setForeground(TEXT_PRIMARY); cmbFilter.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cmbFilter.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60)));
        cmbFilter.addActionListener(e -> refreshData());
        filterBar.add(cmbFilter);

        topContainer.add(filterBar);
        add(topContainer, BorderLayout.NORTH);

        transactionListPanel = new JPanel();
        transactionListPanel.setLayout(new BoxLayout(transactionListPanel, BoxLayout.Y_AXIS));
        transactionListPanel.setBackground(BG_COLOR);
        transactionListPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        scrollPane = new JScrollPane(transactionListPanel);
        scrollPane.setBorder(null); scrollPane.setOpaque(false);
        scrollPane.getViewport().setBackground(BG_COLOR);

        // 🌟 BỎ THANH CUỘN & TĂNG TỐC ĐỘ CUỘN GẤP ĐÔI KHÔNG TẢI RÁC
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
        scrollPane.getVerticalScrollBar().setUnitIncrement(32);

        add(scrollPane, BorderLayout.CENTER);

        JButton btnAdd = new JButton("+ Thêm giao dịch mới");
        btnAdd.setFont(new Font("Segoe UI", Font.BOLD, 16)); btnAdd.setForeground(BG_COLOR); btnAdd.setBackground(ACCENT_YELLOW);
        btnAdd.setFocusPainted(false); btnAdd.setBorder(BorderFactory.createEmptyBorder(12, 40, 12, 40));
        btnAdd.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnAdd.addActionListener(e -> new AddTransactionDialog(mainFrame).setVisible(true));

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setBackground(BG_COLOR); bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 15, 0));
        bottomPanel.add(btnAdd);
        add(bottomPanel, BorderLayout.SOUTH);

        refreshData();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(SURFACE_COLOR);
        header.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        lblMonthYear = new JLabel(getCurrentMonthYear());
        lblMonthYear.setFont(new Font("Segoe UI", Font.BOLD, 22)); lblMonthYear.setForeground(Color.WHITE);

        JPanel summaryPanel = new JPanel(new GridLayout(1, 3, 25, 0));
        summaryPanel.setBackground(SURFACE_COLOR);

        lblIncome = createSummaryLabel(summaryPanel, "Tổng thu nhập", "0 đ", new Color(76, 175, 80));
        lblExpense = createSummaryLabel(summaryPanel, "Tổng chi tiêu", "0 đ", new Color(244, 67, 54));
        lblBalance = createSummaryLabel(summaryPanel, "Số dư hiện tại", "0 đ", Color.WHITE);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(SURFACE_COLOR); rightPanel.add(summaryPanel, BorderLayout.EAST);

        header.add(lblMonthYear, BorderLayout.WEST);
        header.add(rightPanel, BorderLayout.CENTER);
        return header;
    }

    private JLabel createSummaryLabel(JPanel parent, String title, String value, Color valueColor) {
        JPanel panel = new JPanel(new GridLayout(2, 1, 0, 2)); panel.setOpaque(false);
        JLabel lblTitle = new JLabel(title, SwingConstants.CENTER); lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12)); lblTitle.setForeground(Color.LIGHT_GRAY);
        JLabel lblValue = new JLabel(value, SwingConstants.CENTER); lblValue.setFont(new Font("Segoe UI", Font.BOLD, 18)); lblValue.setForeground(valueColor);
        panel.add(lblTitle); panel.add(lblValue); parent.add(panel);
        return lblValue;
    }

    public void refreshData() {
        if (financeService == null) return;
        String searchText = txtSearch != null ? txtSearch.getText().trim().toLowerCase() : "";
        String filterType = cmbFilter != null ? (String) cmbFilter.getSelectedItem() : "Tất cả";

        List<Transaction> transactions = financeService.getAllTransactions().stream()
                .filter(t -> {
                    if (t == null) return false;
                    if ("Thu nhập".equals(filterType) && t.getType() != TransactionType.INCOME) return false;
                    if ("Chi tiêu".equals(filterType) && t.getType() != TransactionType.EXPENSE) return false;
                    if (!searchText.isEmpty()) {
                        String note = t.getNote() != null ? t.getNote().toLowerCase() : "";
                        String catName = t.getCategory() != null ? t.getCategory().getName().toLowerCase() : "";
                        return note.contains(searchText) || catName.contains(searchText);
                    }
                    return true;
                })
                .sorted((a, b) -> b.getDateTime().compareTo(a.getDateTime()))
                .collect(Collectors.toList());

        double totalIncome = financeService.getAllTransactions().stream().filter(t -> t != null && t.getType() == TransactionType.INCOME).mapToDouble(Transaction::getAmount).sum();
        double totalExpense = financeService.getAllTransactions().stream().filter(t -> t != null && t.getType() == TransactionType.EXPENSE).mapToDouble(Transaction::getAmount).sum();

        lblIncome.setText(String.format("%,.0f đ", totalIncome));
        lblExpense.setText(String.format("%,.0f đ", totalExpense));
        lblBalance.setText(String.format("%,.0f đ", totalIncome - totalExpense));
        lblMonthYear.setText(getCurrentMonthYear());

        transactionListPanel.removeAll();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String currentDate = ""; JPanel dateGroup = null;

        for (Transaction t : transactions) {
            String transactionDate = t.getDateTime().format(dateFormatter);
            if (!transactionDate.equals(currentDate)) {
                currentDate = transactionDate;
                dateGroup = new JPanel(); dateGroup.setLayout(new BoxLayout(dateGroup, BoxLayout.Y_AXIS)); dateGroup.setBackground(BG_COLOR);
                dateGroup.setBorder(BorderFactory.createEmptyBorder(12, 0, 5, 0));

                JLabel lblDate = new JLabel(transactionDate); lblDate.setFont(new Font("Segoe UI", Font.BOLD, 14)); lblDate.setForeground(new Color(180, 180, 180));
                lblDate.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 0)); lblDate.setAlignmentX(Component.LEFT_ALIGNMENT);
                dateGroup.add(lblDate);

                JSeparator sep = new JSeparator(); sep.setForeground(new Color(70, 70, 70)); sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1)); sep.setAlignmentX(Component.LEFT_ALIGNMENT);
                dateGroup.add(sep); transactionListPanel.add(dateGroup);
            }
            JPanel row = createTransactionRow(t, dateTimeFormatter); row.setAlignmentX(Component.LEFT_ALIGNMENT); dateGroup.add(row);
            JSeparator rowSep = new JSeparator(); rowSep.setForeground(new Color(45, 45, 45)); rowSep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1)); rowSep.setAlignmentX(Component.LEFT_ALIGNMENT);
            dateGroup.add(rowSep);
        }
        transactionListPanel.revalidate(); transactionListPanel.repaint();
    }

    private JPanel createTransactionRow(Transaction t, DateTimeFormatter dateTimeFormatter) {
        JPanel row = new JPanel(new BorderLayout()); row.setBackground(BG_COLOR); row.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48)); row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        Category cat = t.getCategory();
        String emoji = (cat != null) ? EmojiUtil.CATEGORY_EMOJI.getOrDefault(cat.getName(), "\uD83D\uDCCD") : "\uD83D\uDCCD";

        // 🌟 TRẢ VỀ NGUYÊN BẢN CHUẨN: Dùng trực tiếp setFont gốc
        JLabel lblIcon = new JLabel(emoji);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        lblIcon.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 15));

        String description = (t.getNote() != null && !t.getNote().trim().isEmpty()) ? t.getNote().trim() : t.getDateTime().format(dateTimeFormatter);
        JLabel lblDescription = new JLabel(description); lblDescription.setFont(new Font("Segoe UI", Font.PLAIN, 14)); lblDescription.setForeground(Color.WHITE);

        String amountStr = String.format("%s%,.0f VND", t.getType() == TransactionType.INCOME ? "+" : "-", t.getAmount());
        JLabel lblAmount = new JLabel(amountStr); lblAmount.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblAmount.setForeground(t.getType() == TransactionType.INCOME ? new Color(76, 175, 80) : new Color(244, 67, 54));

        row.add(lblIcon, BorderLayout.WEST); row.add(lblDescription, BorderLayout.CENTER); row.add(lblAmount, BorderLayout.EAST);
        row.addMouseListener(new java.awt.event.MouseAdapter() { public void mouseClicked(java.awt.event.MouseEvent e) { new TransactionDetailDialog(mainFrame, t).setVisible(true); } });
        return row;
    }

    private String getCurrentMonthYear() { java.time.LocalDate now = java.time.LocalDate.now(); return "Tháng " + now.getMonthValue() + "/" + now.getYear(); }
    @Override public void update(EventType eventType, Object data) { if (eventType == EventType.TRANSACTION_ADDED || eventType == EventType.TRANSACTION_UPDATED || eventType == EventType.TRANSACTION_DELETED || eventType == EventType.DATA_LOADED) { SwingUtilities.invokeLater(() -> refreshData()); } }
}