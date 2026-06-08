package com.expensemanager.ui;

import com.expensemanager.entity.Category;
import com.expensemanager.entity.Transaction;
import com.expensemanager.entity.TransactionType;
import com.expensemanager.observer.EventType;
import com.expensemanager.observer.Observer;
import com.expensemanager.service.BudgetManager;
import com.expensemanager.service.FinanceService;
import com.expensemanager.util.EmojiUtil;
import com.expensemanager.util.ThemeManager;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class DashboardPanel extends JPanel implements Observer {

    private MainFrame mainFrame;
    private FinanceService financeService;
    private BudgetManager budgetManager;

    private boolean isVietnamese = true;

    private JLabel lblIncome, lblExpense, lblBalance;
    private JLabel lblIncomeTitle, lblExpenseTitle, lblBalanceTitle;
    private JPanel transactionListPanel;
    private JScrollPane scrollPane;
    private JTextField txtSearch;
    private JComboBox<String> cmbFilter;
    private JButton btnAdd;
    private JTextField txtStartDate, txtEndDate;
    private JButton btnApplyFilter;

    private CalendarPanel calendarPanel;
    private JButton btnCalendarPopup;

    private LocalDate startDate;
    private LocalDate endDate;

    public DashboardPanel(MainFrame mainFrame, FinanceService financeService, BudgetManager budgetManager) {
        this.mainFrame = mainFrame;
        this.financeService = financeService;
        this.budgetManager = budgetManager;
        if (mainFrame != null) this.isVietnamese = mainFrame.isVietnamese();

        this.calendarPanel = new CalendarPanel(mainFrame, financeService, budgetManager);
        if (financeService != null) {
            financeService.attach(this.calendarPanel);
        }

        setLayout(new BorderLayout());

        LocalDate now = LocalDate.now();
        startDate = now.withDayOfMonth(1);
        endDate = now.withDayOfMonth(now.lengthOfMonth());

        initComponents();
        refreshData();
        applyTheme();
    }

    private void initComponents() {
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setOpaque(false);
        topContainer.add(createHeader());
        topContainer.add(createFilterBar());
        add(topContainer, BorderLayout.NORTH);

        transactionListPanel = new JPanel();
        transactionListPanel.setLayout(new BoxLayout(transactionListPanel, BoxLayout.Y_AXIS));
        transactionListPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        scrollPane = new JScrollPane(transactionListPanel);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
        scrollPane.getVerticalScrollBar().setUnitIncrement(32);
        add(scrollPane, BorderLayout.CENTER);

        btnAdd = new JButton(isVietnamese ? "+ Thêm giao dịch mới" : "+ Add Transaction");
        styleButton(btnAdd, ThemeManager.getColor("success"), Color.WHITE, 16, true);
        btnAdd.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btnAdd.setBackground(ThemeManager.getColor("success").darker()); }
            public void mouseExited(MouseEvent e) { btnAdd.setBackground(ThemeManager.getColor("success")); }
        });
        btnAdd.addActionListener(e -> new AddTransactionDialog(mainFrame).setVisible(true));

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 15, 0));
        bottomPanel.add(btnAdd);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JPanel summaryPanel = new JPanel(new GridLayout(1, 3, 25, 0));

        lblIncomeTitle = new JLabel();
        lblExpenseTitle = new JLabel();
        lblBalanceTitle = new JLabel();

        lblIncome = createSummaryLabel(summaryPanel, lblIncomeTitle,
                isVietnamese ? "Tổng thu nhập" : "Total Income", "0 đ", ThemeManager.getColor("success"));
        lblExpense = createSummaryLabel(summaryPanel, lblExpenseTitle,
                isVietnamese ? "Tổng chi tiêu" : "Total Expense", "0 đ", ThemeManager.getColor("danger"));
        lblBalance = createSummaryLabel(summaryPanel, lblBalanceTitle,
                isVietnamese ? "Số dư hiện tại" : "Balance", "0 đ", ThemeManager.getColor("textPrimary"));

        header.add(summaryPanel, BorderLayout.CENTER);
        return header;
    }

    private JPanel createFilterBar() {
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        filterBar.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        JLabel lblSearchIcon = new JLabel("🔍");
        lblSearchIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        filterBar.add(lblSearchIcon);

        txtSearch = new JTextField(20);
        styleTextField(txtSearch);
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { refreshData(); }
            public void removeUpdate(DocumentEvent e) { refreshData(); }
            public void changedUpdate(DocumentEvent e) { refreshData(); }
        });
        filterBar.add(txtSearch);

        cmbFilter = new JComboBox<>(isVietnamese ? new String[]{"Tất cả", "Thu nhập", "Chi tiêu"} : new String[]{"All", "Income", "Expense"});
        styleComboBox(cmbFilter);
        cmbFilter.addActionListener(e -> refreshData());
        filterBar.add(cmbFilter);

        JLabel lblFrom = new JLabel(isVietnamese ? "Từ:" : "From:");
        lblFrom.setForeground(ThemeManager.getColor("textSecondary"));
        txtStartDate = new JTextField(10);
        txtStartDate.setText(startDate.toString());
        styleTextField(txtStartDate);

        JLabel lblTo = new JLabel(isVietnamese ? "Đến:" : "To:");
        txtEndDate = new JTextField(10);
        txtEndDate.setText(endDate.toString());
        styleTextField(txtEndDate);

        btnApplyFilter = new JButton(isVietnamese ? "Áp dụng" : "Apply");
        btnApplyFilter.setBackground(ThemeManager.getColor("accent"));
        btnApplyFilter.setForeground(ThemeManager.getColor("bg"));
        btnApplyFilter.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnApplyFilter.setFocusPainted(false);
        btnApplyFilter.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnApplyFilter.addActionListener(e -> applyDateFilter());

        filterBar.add(lblFrom);
        filterBar.add(txtStartDate);
        filterBar.add(lblTo);
        filterBar.add(txtEndDate);
        filterBar.add(btnApplyFilter);

        btnCalendarPopup = new JButton("📅");
        btnCalendarPopup.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        btnCalendarPopup.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        btnCalendarPopup.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCalendarPopup.setFocusPainted(false);
        btnCalendarPopup.addActionListener(e -> showCalendarDialog());
        filterBar.add(btnCalendarPopup);

        return filterBar;
    }

    private void showCalendarDialog() {
        JDialog dialog = new JDialog(mainFrame, isVietnamese ? "Lịch giao dịch" : "Transaction Calendar", true);
        dialog.setSize(850, 650);
        dialog.setLocationRelativeTo(this);
        dialog.add(this.calendarPanel);
        dialog.setVisible(true);
    }

    private void applyDateFilter() {
        try {
            startDate = LocalDate.parse(txtStartDate.getText().trim());
            endDate = LocalDate.parse(txtEndDate.getText().trim());
            if (startDate.isAfter(endDate)) {
                JOptionPane.showMessageDialog(this,
                        isVietnamese ? "Ngày bắt đầu phải trước ngày kết thúc!" : "Start date must be before end date!");
                return;
            }
            refreshData();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    isVietnamese ? "Định dạng ngày không hợp lệ (YYYY-MM-DD)" : "Invalid date format (YYYY-MM-DD)");
        }
    }

    public void refreshData() {
        if (financeService == null) return;
        String searchText = txtSearch != null ? txtSearch.getText().trim().toLowerCase() : "";
        String filterType = cmbFilter != null ? (String) cmbFilter.getSelectedItem() : "Tất cả";

        final String currentFilter = (filterType.contains("All") || filterType.contains("Tất cả")) ? "Tất cả" :
                (filterType.contains("Income") || filterType.contains("Thu nhập")) ? "Thu nhập" : "Chi tiêu";

        List<Transaction> transactions = financeService.getAllTransactions().stream()
                .filter(t -> {
                    if (t == null) return false;
                    LocalDate txDate = t.getDateTime().toLocalDate();
                    if (txDate.isBefore(startDate) || txDate.isAfter(endDate)) return false;

                    if ("Thu nhập".equals(currentFilter) && t.getType() != TransactionType.INCOME) return false;
                    if ("Chi tiêu".equals(currentFilter) && t.getType() != TransactionType.EXPENSE) return false;

                    if (!searchText.isEmpty()) {
                        String note = t.getNote() != null ? t.getNote().toLowerCase() : "";
                        String catName = t.getCategory() != null ? t.getCategory().getName().toLowerCase() : "";
                        return note.contains(searchText) || catName.contains(searchText);
                    }
                    return true;
                })
                .sorted((a, b) -> b.getDateTime().compareTo(a.getDateTime()))
                .collect(Collectors.toList());

        updateSummaryMetrics(transactions);
        renderTransactionList(transactions);
    }

    private void updateSummaryMetrics(List<Transaction> transactions) {
        double totalIncome = transactions.stream()
                .filter(t -> t.getType() == TransactionType.INCOME)
                .mapToDouble(Transaction::getAmount).sum();

        double totalExpense = transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .mapToDouble(Transaction::getAmount).sum();

        lblIncome.setText(String.format("%,.0f đ", totalIncome));
        lblExpense.setText(String.format("%,.0f đ", totalExpense));
        lblBalance.setText(String.format("%,.0f đ", totalIncome - totalExpense));
    }

    private void renderTransactionList(List<Transaction> transactions) {
        transactionListPanel.removeAll();
        Locale currentLocale = isVietnamese ? new Locale("vi", "VN") : Locale.ENGLISH;
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy", currentLocale);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", currentLocale);

        String currentDate = "";
        JPanel dateGroup = null;

        for (Transaction t : transactions) {
            String transactionDate = t.getDateTime().format(dateFormatter);
            if (!transactionDate.equals(currentDate)) {
                currentDate = transactionDate;
                dateGroup = new JPanel();
                dateGroup.setLayout(new BoxLayout(dateGroup, BoxLayout.Y_AXIS));
                dateGroup.setBorder(BorderFactory.createEmptyBorder(12, 0, 5, 0));

                JLabel lblDate = new JLabel(transactionDate);
                lblDate.setFont(new Font("Segoe UI", Font.BOLD, 14));
                lblDate.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 0));
                lblDate.setAlignmentX(Component.LEFT_ALIGNMENT);
                dateGroup.add(lblDate);

                JSeparator sep = new JSeparator();
                sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
                sep.setAlignmentX(Component.LEFT_ALIGNMENT);
                dateGroup.add(sep);

                transactionListPanel.add(dateGroup);
            }
            JPanel row = createTransactionRow(t, dateTimeFormatter);
            row.setAlignmentX(Component.LEFT_ALIGNMENT);
            dateGroup.add(row);
        }
        transactionListPanel.revalidate();
        transactionListPanel.repaint();
    }

    private JPanel createTransactionRow(Transaction t, DateTimeFormatter dateTimeFormatter) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        Category cat = t.getCategory();
        String emoji = (cat != null) ? EmojiUtil.CATEGORY_EMOJI.getOrDefault(cat.getName(), "📌") : "📌";

        JLabel lblIcon = new JLabel(emoji);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        lblIcon.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 15));

        String description = (t.getNote() != null && !t.getNote().trim().isEmpty()) ? t.getNote().trim() : t.getDateTime().format(dateTimeFormatter);
        JLabel lblDescription = new JLabel(description);
        lblDescription.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        String amountStr = String.format("%s%,.0f VND", t.getType() == TransactionType.INCOME ? "+" : "-", t.getAmount());
        JLabel lblAmount = new JLabel(amountStr);
        lblAmount.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblAmount.setForeground(t.getType() == TransactionType.INCOME ? ThemeManager.getColor("success") : ThemeManager.getColor("danger"));

        row.add(lblIcon, BorderLayout.WEST);
        row.add(lblDescription, BorderLayout.CENTER);
        row.add(lblAmount, BorderLayout.EAST);

        row.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { new TransactionDetailDialog(mainFrame, t).setVisible(true); }
        });

        return row;
    }

    private JLabel createSummaryLabel(JPanel parent, JLabel lblTitle, String title, String value, Color valueColor) {
        JPanel panel = new JPanel(new GridLayout(2, 1, 0, 2));
        panel.setOpaque(false);

        lblTitle.setText(title);
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JLabel lblValue = new JLabel(value, SwingConstants.CENTER);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblValue.setForeground(valueColor);

        panel.add(lblTitle);
        panel.add(lblValue);
        parent.add(panel);

        return lblValue;
    }

    private void styleTextField(JTextField tf) {
        tf.setBackground(ThemeManager.getColor("input"));
        tf.setForeground(ThemeManager.getColor("textPrimary"));
        tf.setCaretColor(ThemeManager.getColor("accent"));
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.getColor("border")),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
    }

    private void styleComboBox(JComboBox<String> cb) {
        cb.setBackground(ThemeManager.getColor("input"));
        cb.setForeground(ThemeManager.getColor("textPrimary"));
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cb.setBorder(BorderFactory.createLineBorder(ThemeManager.getColor("border")));
    }

    private void styleButton(JButton btn, Color bg, Color fg, int fontSize, boolean bold) {
        btn.setFont(new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, fontSize));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public void updateLanguageText(boolean isVN) {
        this.isVietnamese = isVN;
        if (btnAdd != null) btnAdd.setText(isVN ? "+ Thêm giao dịch mới" : "+ Add Transaction");

        if (cmbFilter != null) {
            int idx = cmbFilter.getSelectedIndex();
            cmbFilter.setModel(new DefaultComboBoxModel<>(isVN ? new String[]{"Tất cả", "Thu nhập", "Chi tiêu"} : new String[]{"All", "Income", "Expense"}));
            if (idx >= 0) cmbFilter.setSelectedIndex(idx);
        }

        if (lblIncomeTitle != null) lblIncomeTitle.setText(isVN ? "Tổng thu nhập" : "Total Income");
        if (lblExpenseTitle != null) lblExpenseTitle.setText(isVN ? "Tổng chi tiêu" : "Total Expense");
        if (lblBalanceTitle != null) lblBalanceTitle.setText(isVN ? "Số dư hiện tại" : "Balance");
        if (btnApplyFilter != null) btnApplyFilter.setText(isVN ? "Áp dụng" : "Apply");

        if (calendarPanel != null) {
            calendarPanel.update(EventType.DATA_LOADED, null);
        }
        refreshData();
    }

    @Override
    public void update(EventType eventType, Object data) {
        if (eventType == EventType.TRANSACTION_ADDED || eventType == EventType.TRANSACTION_UPDATED ||
                eventType == EventType.TRANSACTION_DELETED || eventType == EventType.DATA_LOADED) {
            SwingUtilities.invokeLater(this::refreshData);
        }
    }

    public void applyTheme() {
        setBackground(ThemeManager.getColor("bg"));
        if (transactionListPanel != null) transactionListPanel.setBackground(ThemeManager.getColor("bg"));
        if (scrollPane != null) scrollPane.getViewport().setBackground(ThemeManager.getColor("bg"));

        if (lblIncome != null) lblIncome.setForeground(ThemeManager.getColor("success"));
        if (lblExpense != null) lblExpense.setForeground(ThemeManager.getColor("danger"));
        if (lblBalance != null) lblBalance.setForeground(ThemeManager.getColor("textPrimary"));

        if (lblIncomeTitle != null) lblIncomeTitle.setForeground(ThemeManager.getColor("textSecondary"));
        if (lblExpenseTitle != null) lblExpenseTitle.setForeground(ThemeManager.getColor("textSecondary"));
        if (lblBalanceTitle != null) lblBalanceTitle.setForeground(ThemeManager.getColor("textSecondary"));

        if (txtSearch != null) {
            txtSearch.setBackground(ThemeManager.getColor("input"));
            txtSearch.setForeground(ThemeManager.getColor("textPrimary"));
            txtSearch.setCaretColor(ThemeManager.getColor("accent"));
        }
        if (cmbFilter != null) {
            cmbFilter.setBackground(ThemeManager.getColor("input"));
            cmbFilter.setForeground(ThemeManager.getColor("textPrimary"));
        }
        if (txtStartDate != null) {
            txtStartDate.setBackground(ThemeManager.getColor("input"));
            txtStartDate.setForeground(ThemeManager.getColor("textPrimary"));
        }
        if (txtEndDate != null) {
            txtEndDate.setBackground(ThemeManager.getColor("input"));
            txtEndDate.setForeground(ThemeManager.getColor("textPrimary"));
        }
        if (btnApplyFilter != null) {
            btnApplyFilter.setBackground(ThemeManager.getColor("accent"));
            btnApplyFilter.setForeground(ThemeManager.getColor("bg"));
        }
        if (btnAdd != null) {
            btnAdd.setBackground(ThemeManager.getColor("success"));
            btnAdd.setForeground(Color.WHITE);
        }
        if (calendarPanel != null) calendarPanel.applyTheme();
        if (btnCalendarPopup != null) {
            btnCalendarPopup.setBackground(ThemeManager.getColor("input"));
            btnCalendarPopup.setForeground(ThemeManager.getColor("textPrimary"));
        }

        refreshData();
    }
}