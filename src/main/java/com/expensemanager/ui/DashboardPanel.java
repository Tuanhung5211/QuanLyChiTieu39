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

    private int currentMonthOffset = 0;

    private JLabel lblMonthYear, lblIncome, lblExpense, lblBalance;
    private JLabel lblIncomeTitle, lblExpenseTitle, lblBalanceTitle;
    private JPanel transactionListPanel;
    private JScrollPane scrollPane;
    private JTextField txtSearch;
    private JComboBox<String> cmbFilter;
    private JButton btnAdd;

    // Nút bấm quản lý trạng thái khóa/mở khóa biên thời gian
    private JButton btnPrevMonth, btnNextMonth;
    private boolean isVietnamese = true;

    private final Color BG_COLOR = new Color(30, 30, 30);
    private final Color SURFACE_COLOR = new Color(40, 40, 40);
    private final Color INPUT_BG = new Color(45, 45, 45);
    private final Color ACCENT_YELLOW = new Color(255, 193, 7);
    private final Color TEXT_PRIMARY = new Color(240, 240, 240);
    private final Color TEXT_SECONDARY = new Color(180, 180, 180);
    private final Color ADD_BTN_BG = new Color(76, 175, 80);
    private final Color ADD_BTN_HOVER = new Color(56, 142, 60);
    private final Color ADD_BTN_FG = Color.WHITE;

    public DashboardPanel(MainFrame mainFrame, FinanceService financeService, BudgetManager budgetManager) {
        this.mainFrame = mainFrame;
        this.financeService = financeService;
        this.budgetManager = budgetManager;
        if (mainFrame != null) this.isVietnamese = mainFrame.isVietnamese();

        setLayout(new BorderLayout());
        setBackground(BG_COLOR);

        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setOpaque(false);
        topContainer.add(createHeader());

        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        filterBar.setBackground(BG_COLOR);
        filterBar.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        JLabel lblSearchIcon = new JLabel("🔍");
        lblSearchIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        filterBar.add(lblSearchIcon);

        txtSearch = new JTextField(20);
        txtSearch.setBackground(INPUT_BG);
        txtSearch.setForeground(TEXT_PRIMARY);
        txtSearch.setCaretColor(ACCENT_YELLOW);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtSearch.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(60, 60, 60)), BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                refreshData();
            }

            public void removeUpdate(DocumentEvent e) {
                refreshData();
            }

            public void changedUpdate(DocumentEvent e) {
                refreshData();
            }
        });
        filterBar.add(txtSearch);

        cmbFilter = new JComboBox<>(isVietnamese ? new String[]{"Tất cả", "Thu nhập", "Chi tiêu"} : new String[]{"All", "Income", "Expense"});
        cmbFilter.setBackground(INPUT_BG);
        cmbFilter.setForeground(TEXT_PRIMARY);
        cmbFilter.setFont(new Font("Segoe UI", Font.PLAIN, 14)); // 🌟 ĐÃ SỬA: Sửa thành Font.PLAIN để hết lỗi biên dịch
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
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setBackground(BG_COLOR);
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
        scrollPane.getVerticalScrollBar().setUnitIncrement(32);
        add(scrollPane, BorderLayout.CENTER);

        btnAdd = new JButton(isVietnamese ? "+ Thêm giao dịch mới" : "+ Add Transaction");
        styleButton(btnAdd, ADD_BTN_BG, ADD_BTN_FG, 16, true);
        btnAdd.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btnAdd.setBackground(ADD_BTN_HOVER);
            }

            public void mouseExited(MouseEvent e) {
                btnAdd.setBackground(ADD_BTN_BG);
            }
        });
        btnAdd.addActionListener(e -> new AddTransactionDialog(mainFrame).setVisible(true));

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setBackground(BG_COLOR);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 15, 0));
        bottomPanel.add(btnAdd);
        add(bottomPanel, BorderLayout.SOUTH);

        refreshData();
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

    private JButton createInnerArrowButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(TEXT_PRIMARY);
        btn.setBackground(INPUT_BG);
        btn.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60), 1));
        btn.setPreferredSize(new Dimension(32, 28));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        return btn;
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(SURFACE_COLOR);
        header.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JPanel timeNavWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        timeNavWrapper.setOpaque(false);

        btnPrevMonth = createInnerArrowButton("<");
        btnNextMonth = createInnerArrowButton(">");

        lblMonthYear = new JLabel(getCurrentMonthYear());
        lblMonthYear.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblMonthYear.setForeground(Color.WHITE);

        btnPrevMonth.addActionListener(e -> {
            currentMonthOffset--;
            refreshData();
        });
        btnNextMonth.addActionListener(e -> {
            currentMonthOffset++;
            refreshData();
        });

        timeNavWrapper.add(btnPrevMonth);
        timeNavWrapper.add(lblMonthYear);
        timeNavWrapper.add(btnNextMonth);
        header.add(timeNavWrapper, BorderLayout.WEST);

        JPanel summaryPanel = new JPanel(new GridLayout(1, 3, 25, 0));
        summaryPanel.setBackground(SURFACE_COLOR);

        lblIncomeTitle = new JLabel();
        lblExpenseTitle = new JLabel();
        lblBalanceTitle = new JLabel();

        lblIncome = createSummaryLabel(summaryPanel, lblIncomeTitle, isVietnamese ? "Tổng thu nhập" : "Total Income", "0 đ", new Color(76, 175, 80));
        lblExpense = createSummaryLabel(summaryPanel, lblExpenseTitle, isVietnamese ? "Tổng chi tiêu" : "Total Expense", "0 đ", new Color(244, 67, 54));
        lblBalance = createSummaryLabel(summaryPanel, lblBalanceTitle, isVietnamese ? "Số dư hiện tại" : "Balance", "0 đ", Color.WHITE);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(SURFACE_COLOR);
        rightPanel.add(summaryPanel, BorderLayout.EAST);
        header.add(rightPanel, BorderLayout.CENTER);
        return header;
    }

    private JLabel createSummaryLabel(JPanel parent, JLabel lblTitle, String title, String value, Color valueColor) {
        JPanel panel = new JPanel(new GridLayout(2, 1, 0, 2));
        panel.setOpaque(false);
        lblTitle.setText(title);
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTitle.setForeground(TEXT_SECONDARY);
        JLabel lblValue = new JLabel(value, SwingConstants.CENTER);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblValue.setForeground(valueColor);
        panel.add(lblTitle);
        panel.add(lblValue);
        parent.add(panel);
        return lblValue;
    }

    public void refreshData() {
        if (financeService == null) return;

        LocalDate targetDate = LocalDate.now().plusMonths(currentMonthOffset);
        int targetMonth = targetDate.getMonthValue();
        int targetYear = targetDate.getYear();

        // 1. LOGIC KHÓA NÚT BẤM GIỚI HẠN THỜI GIAN THEO BIÊN DATA
        LocalDate earliestTxDate = financeService.getEarliestTransactionDate();
        LocalDate latestTxDate = financeService.getLatestTransactionDate();
        LocalDate realTimeNow = LocalDate.now();

        if (earliestTxDate == null || latestTxDate == null) {
            if (btnPrevMonth != null) btnPrevMonth.setEnabled(false);
            if (btnNextMonth != null) btnNextMonth.setEnabled(false);
        } else {
            LocalDate previousMonth = targetDate.minusMonths(1);
            boolean canGoBack = !previousMonth.isBefore(earliestTxDate.withDayOfMonth(1));
            if (btnPrevMonth != null) btnPrevMonth.setEnabled(canGoBack);

            LocalDate nextMonth = targetDate.plusMonths(1);
            boolean isNextInFuture = nextMonth.isAfter(realTimeNow.withDayOfMonth(realTimeNow.lengthOfMonth()));
            boolean isNextPastDataBounds = nextMonth.isAfter(latestTxDate.withDayOfMonth(latestTxDate.lengthOfMonth()));

            if (btnNextMonth != null) {
                btnNextMonth.setEnabled(!isNextInFuture && !isNextPastDataBounds);
            }
        }

        // 2. TIẾN HÀNH LỌC DỮ LIỆU ĐỂ HIỂN THỊ RA GIAO DIỆN NHƯ CŨ
        String searchText = txtSearch != null ? txtSearch.getText().trim().toLowerCase() : "";
        String filterType = cmbFilter != null ? (String) cmbFilter.getSelectedItem() : "Tất cả";
        final String currentFilter = (filterType.contains("All") || filterType.contains("Tất cả")) ? "Tất cả" :
                (filterType.contains("Income") || filterType.contains("Thu nhập")) ? "Thu nhập" : "Chi tiêu";

        List<Transaction> transactions = financeService.getAllTransactions().stream()
                .filter(t -> {
                    if (t == null) return false;
                    if (t.getDateTime().getMonthValue() != targetMonth || t.getDateTime().getYear() != targetYear)
                        return false;

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

        double totalIncome = financeService.getAllTransactions().stream()
                .filter(t -> t != null && t.getType() == TransactionType.INCOME
                        && t.getDateTime().getMonthValue() == targetMonth
                        && t.getDateTime().getYear() == targetYear)
                .mapToDouble(Transaction::getAmount).sum();

        double totalExpense = financeService.getAllTransactions().stream()
                .filter(t -> t != null && t.getType() == TransactionType.EXPENSE
                        && t.getDateTime().getMonthValue() == targetMonth
                        && t.getDateTime().getYear() == targetYear)
                .mapToDouble(Transaction::getAmount).sum();

        lblIncome.setText(String.format("%,.0f đ", totalIncome));
        lblExpense.setText(String.format("%,.0f đ", totalExpense));
        lblBalance.setText(String.format("%,.0f đ", totalIncome - totalExpense));
        lblMonthYear.setText(getCurrentMonthYear());

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
                dateGroup.setBackground(BG_COLOR);
                dateGroup.setBorder(BorderFactory.createEmptyBorder(12, 0, 5, 0));
                JLabel lblDate = new JLabel(transactionDate);
                lblDate.setFont(new Font("Segoe UI", Font.BOLD, 14));
                lblDate.setForeground(TEXT_SECONDARY);
                lblDate.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 0));
                lblDate.setAlignmentX(Component.LEFT_ALIGNMENT);
                dateGroup.add(lblDate);

                JSeparator sep = new JSeparator();
                sep.setForeground(new Color(255, 255, 255, 160));
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
        row.setBackground(BG_COLOR);
        row.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        Category cat = t.getCategory();
        String emoji = (cat != null) ? EmojiUtil.CATEGORY_EMOJI.getOrDefault(cat.getName(), "📌") : "📌";

        JLabel lblIcon = new JLabel(emoji);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        lblIcon.setForeground(Color.WHITE);
        lblIcon.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 15));

        String description = (t.getNote() != null && !t.getNote().trim().isEmpty()) ? t.getNote().trim() : t.getDateTime().format(dateTimeFormatter);
        JLabel lblDescription = new JLabel(description);
        lblDescription.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblDescription.setForeground(Color.WHITE);

        String amountStr = String.format("%s%,.0f VND", t.getType() == TransactionType.INCOME ? "+" : "-", t.getAmount());
        JLabel lblAmount = new JLabel(amountStr);
        lblAmount.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblAmount.setForeground(t.getType() == TransactionType.INCOME ? new Color(76, 175, 80) : new Color(244, 67, 54));

        row.add(lblIcon, BorderLayout.WEST);
        row.add(lblDescription, BorderLayout.CENTER);
        row.add(lblAmount, BorderLayout.EAST);
        row.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                new TransactionDetailDialog(mainFrame, t).setVisible(true);
            }
        });
        return row;
    }

    private String getCurrentMonthYear() {
        java.time.LocalDate date = java.time.LocalDate.now().plusMonths(currentMonthOffset);
        if (isVietnamese) return "Tháng " + date.getMonthValue() + "/" + date.getYear();
        else return date.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH));
    }

    @Override
    public void update(EventType eventType, Object data) {
        if (eventType == EventType.TRANSACTION_ADDED || eventType == EventType.TRANSACTION_UPDATED ||
                eventType == EventType.TRANSACTION_DELETED || eventType == EventType.DATA_LOADED)
            SwingUtilities.invokeLater(() -> refreshData());
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
        if (lblMonthYear != null) lblMonthYear.setText(getCurrentMonthYear());
        refreshData();
    }
}