package com.expensemanager.ui;

import com.expensemanager.entity.Category;
import com.expensemanager.entity.Transaction;
import com.expensemanager.entity.TransactionType;
import com.expensemanager.observer.EventType;
import com.expensemanager.observer.Observer;
import com.expensemanager.service.BudgetManager;
import com.expensemanager.service.FinanceService;
import com.expensemanager.util.EmojiUtil;
import com.expensemanager.service.ThemeManager;

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

    // =====================================================================
    // 1. KHAI BÁO BIẾN GIAO DIỆN VÀ LOGIC
    // =====================================================================
    private MainFrame mainFrame;
    private FinanceService financeService;
    private BudgetManager budgetManager;

    private int currentMonthOffset = 0;
    private boolean isVietnamese = true;

    private JLabel lblMonthYear, lblIncome, lblExpense, lblBalance;
    private JLabel lblIncomeTitle, lblExpenseTitle, lblBalanceTitle;
    private JPanel transactionListPanel;
    private JScrollPane scrollPane;
    private JTextField txtSearch;
    private JComboBox<String> cmbFilter;
    private JButton btnAdd, btnPrevMonth, btnNextMonth;

    // =====================================================================
    // 2. CONSTRUCTOR - KHỞI TẠO BỐ CỤC
    // =====================================================================
    public DashboardPanel(MainFrame mainFrame, FinanceService financeService, BudgetManager budgetManager) {
        this.mainFrame = mainFrame;
        this.financeService = financeService;
        this.budgetManager = budgetManager;
        if (mainFrame != null) this.isVietnamese = mainFrame.isVietnamese();

        setLayout(new BorderLayout());

        initComponents();
        applyTheme(); // Gọi hàm Theme ngay khi khởi tạo
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
        styleButton(btnAdd, ThemeManager.getColor("success"), ThemeManager.getColor("bg"), 16, true);
        btnAdd.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btnAdd.setBackground(ThemeManager.getColor("success").darker()); }
            public void mouseExited(MouseEvent e) { btnAdd.setBackground(ThemeManager.getColor("success")); }
        });
        btnAdd.addActionListener(e -> new AddTransactionDialog(mainFrame).setVisible(true));

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 15, 0));
        bottomPanel.add(btnAdd);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    // =====================================================================
    // 3. XÂY DỰNG GIAO DIỆN THÀNH PHẦN (UI COMPONENTS)
    // =====================================================================
    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setName("headerPanel");
        header.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JPanel timeNavWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        timeNavWrapper.setOpaque(false);

        btnPrevMonth = createInnerArrowButton("<");
        btnNextMonth = createInnerArrowButton(">");

        lblMonthYear = new JLabel(getCurrentMonthYear());
        lblMonthYear.setFont(new Font("Segoe UI", Font.BOLD, 22));

        btnPrevMonth.addActionListener(e -> { currentMonthOffset--; refreshData(); });
        btnNextMonth.addActionListener(e -> { currentMonthOffset++; refreshData(); });

        timeNavWrapper.add(btnPrevMonth);
        timeNavWrapper.add(lblMonthYear);
        timeNavWrapper.add(btnNextMonth);
        header.add(timeNavWrapper, BorderLayout.WEST);

        JPanel summaryPanel = new JPanel(new GridLayout(1, 3, 25, 0));
        summaryPanel.setOpaque(false);

        lblIncomeTitle = new JLabel(); lblExpenseTitle = new JLabel(); lblBalanceTitle = new JLabel();

        lblIncome = createSummaryLabel(summaryPanel, lblIncomeTitle, isVietnamese ? "Tổng thu nhập" : "Total Income", "0 đ", ThemeManager.getColor("success"));
        lblExpense = createSummaryLabel(summaryPanel, lblExpenseTitle, isVietnamese ? "Tổng chi tiêu" : "Total Expense", "0 đ", ThemeManager.getColor("danger"));
        lblBalance = createSummaryLabel(summaryPanel, lblBalanceTitle, isVietnamese ? "Số dư hiện tại" : "Balance", "0 đ", ThemeManager.getColor("textPrimary"));

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setOpaque(false);
        rightPanel.add(summaryPanel, BorderLayout.EAST);
        header.add(rightPanel, BorderLayout.CENTER);
        return header;
    }

    private JPanel createFilterBar() {
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        filterBar.setOpaque(false);
        filterBar.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        JLabel lblSearchIcon = new JLabel("🔍");
        lblSearchIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        filterBar.add(lblSearchIcon);

        txtSearch = new JTextField(20);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { refreshData(); }
            public void removeUpdate(DocumentEvent e) { refreshData(); }
            public void changedUpdate(DocumentEvent e) { refreshData(); }
        });
        filterBar.add(txtSearch);

        cmbFilter = new JComboBox<>(isVietnamese ? new String[]{"Tất cả", "Thu nhập", "Chi tiêu"} : new String[]{"All", "Income", "Expense"});
        cmbFilter.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cmbFilter.addActionListener(e -> refreshData());
        filterBar.add(cmbFilter);

        return filterBar;
    }

    private JPanel createTransactionRow(Transaction t, DateTimeFormatter dateTimeFormatter) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        Category cat = t.getCategory();
        String emoji = (cat != null) ? EmojiUtil.CATEGORY_EMOJI.getOrDefault(cat.getName(), "📌") : "📌";

        JLabel lblIcon = new JLabel(emoji);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        lblIcon.setForeground(ThemeManager.getColor("textPrimary"));
        lblIcon.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 15));

        String description = (t.getNote() != null && !t.getNote().trim().isEmpty()) ? t.getNote().trim() : t.getDateTime().format(dateTimeFormatter);
        JLabel lblDescription = new JLabel(description);
        lblDescription.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblDescription.setForeground(ThemeManager.getColor("textPrimary"));

        String amountStr = String.format("%s%,.0f VND", t.getType() == TransactionType.INCOME ? "+" : "-", t.getAmount());
        JLabel lblAmount = new JLabel(amountStr);
        lblAmount.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblAmount.setForeground(t.getType() == TransactionType.INCOME ? ThemeManager.getColor("success") : ThemeManager.getColor("danger"));

        row.add(lblIcon, BorderLayout.WEST);
        row.add(lblDescription, BorderLayout.CENTER);
        row.add(lblAmount, BorderLayout.EAST);
        row.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { new TransactionDetailDialog(mainFrame, t).setVisible(true); }
            public void mouseEntered(MouseEvent e) { row.setBackground(ThemeManager.getColor("surface")); row.setOpaque(true); row.repaint(); }
            public void mouseExited(MouseEvent e) { row.setOpaque(false); row.repaint(); }
        });
        return row;
    }

    // =====================================================================
    // 4. XỬ LÝ DỮ LIỆU & LOGIC TÍNH TOÁN
    // =====================================================================
    public void refreshData() {
        if (financeService == null) return;

        LocalDate targetDate = LocalDate.now().plusMonths(currentMonthOffset);
        int targetMonth = targetDate.getMonthValue();
        int targetYear = targetDate.getYear();

        updateNavigationButtonsStatus(targetDate);

        String searchText = txtSearch != null ? txtSearch.getText().trim().toLowerCase() : "";
        String filterType = cmbFilter != null ? (String) cmbFilter.getSelectedItem() : "Tất cả";
        final String currentFilter = (filterType.contains("All") || filterType.contains("Tất cả")) ? "Tất cả" :
                (filterType.contains("Income") || filterType.contains("Thu nhập")) ? "Thu nhập" : "Chi tiêu";

        List<Transaction> transactions = financeService.getAllTransactions().stream()
                .filter(t -> {
                    if (t == null) return false;
                    if (t.getDateTime().getMonthValue() != targetMonth || t.getDateTime().getYear() != targetYear) return false;
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

        updateSummaryMetrics(targetMonth, targetYear);
        renderTransactionList(transactions);
    }

    private void updateNavigationButtonsStatus(LocalDate targetDate) {
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
            if (btnNextMonth != null) btnNextMonth.setEnabled(!isNextInFuture && !isNextPastDataBounds);
        }
    }

    private void updateSummaryMetrics(int targetMonth, int targetYear) {
        double totalIncome = financeService.getAllTransactions().stream()
                .filter(t -> t != null && t.getType() == TransactionType.INCOME && t.getDateTime().getMonthValue() == targetMonth && t.getDateTime().getYear() == targetYear)
                .mapToDouble(Transaction::getAmount).sum();

        double totalExpense = financeService.getAllTransactions().stream()
                .filter(t -> t != null && t.getType() == TransactionType.EXPENSE && t.getDateTime().getMonthValue() == targetMonth && t.getDateTime().getYear() == targetYear)
                .mapToDouble(Transaction::getAmount).sum();

        lblIncome.setText(String.format("%,.0f đ", totalIncome));
        lblExpense.setText(String.format("%,.0f đ", totalExpense));
        lblBalance.setText(String.format("%,.0f đ", totalIncome - totalExpense));
        lblMonthYear.setText(getCurrentMonthYear());
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
                dateGroup.setOpaque(false);
                dateGroup.setBorder(BorderFactory.createEmptyBorder(12, 0, 5, 0));

                JLabel lblDate = new JLabel(transactionDate);
                lblDate.setFont(new Font("Segoe UI", Font.BOLD, 14));
                lblDate.setForeground(ThemeManager.getColor("textSecondary"));
                lblDate.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 0));
                lblDate.setAlignmentX(Component.LEFT_ALIGNMENT);
                dateGroup.add(lblDate);

                JSeparator sep = new JSeparator();
                sep.setForeground(ThemeManager.getColor("border"));
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

    // =====================================================================
    // 5. CÁC HÀM TIỆN ÍCH, GIAO DIỆN VÀ OBSERVER PATTERN
    // =====================================================================
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
        btn.setPreferredSize(new Dimension(32, 28));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        return btn;
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

    private String getCurrentMonthYear() {
        java.time.LocalDate date = java.time.LocalDate.now().plusMonths(currentMonthOffset);
        if (isVietnamese) return "Tháng " + date.getMonthValue() + "/" + date.getYear();
        else return date.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH));
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

    @Override
    public void update(EventType eventType, Object data) {
        if (eventType == EventType.TRANSACTION_ADDED || eventType == EventType.TRANSACTION_UPDATED ||
                eventType == EventType.TRANSACTION_DELETED || eventType == EventType.DATA_LOADED) {
            SwingUtilities.invokeLater(this::refreshData);
        }
    }

    // =====================================================================
    // 6. ÁP DỤNG THEME MANAGER
    // =====================================================================
    public void applyTheme() {
        ThemeManager.applyThemeRecursively(this);

        // Áp dụng màu cho các component đặc thù không tự quét được
        for (Component comp : getComponents()) {
            if (comp instanceof JPanel && "headerPanel".equals(comp.getName())) {
                comp.setBackground(ThemeManager.getColor("surface"));
                ((JPanel) comp).setOpaque(true); // 👉 ĐÃ SỬA LỖI Ở ĐÂY: Thêm ép kiểu (JPanel)
            }
        }

        if (lblIncome != null) lblIncome.setForeground(ThemeManager.getColor("success"));
        if (lblExpense != null) lblExpense.setForeground(ThemeManager.getColor("danger"));
        if (lblBalance != null) lblBalance.setForeground(ThemeManager.getColor("textPrimary"));

        if (lblIncomeTitle != null) lblIncomeTitle.setForeground(ThemeManager.getColor("textSecondary"));
        if (lblExpenseTitle != null) lblExpenseTitle.setForeground(ThemeManager.getColor("textSecondary"));
        if (lblBalanceTitle != null) lblBalanceTitle.setForeground(ThemeManager.getColor("textSecondary"));
        if (lblMonthYear != null) lblMonthYear.setForeground(ThemeManager.getColor("textPrimary"));

        if (btnPrevMonth != null) {
            btnPrevMonth.setBackground(ThemeManager.getColor("input"));
            btnPrevMonth.setForeground(ThemeManager.getColor("textPrimary"));
            btnPrevMonth.setBorder(BorderFactory.createLineBorder(ThemeManager.getColor("border"), 1));
        }
        if (btnNextMonth != null) {
            btnNextMonth.setBackground(ThemeManager.getColor("input"));
            btnNextMonth.setForeground(ThemeManager.getColor("textPrimary"));
            btnNextMonth.setBorder(BorderFactory.createLineBorder(ThemeManager.getColor("border"), 1));
        }

        if (txtSearch != null) {
            txtSearch.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(ThemeManager.getColor("border")),
                    BorderFactory.createEmptyBorder(6, 10, 6, 10))
            );
        }

        if (btnAdd != null) {
            btnAdd.setBackground(ThemeManager.getColor("success"));
            btnAdd.setForeground(ThemeManager.getColor("bg"));
        }

        refreshData();
    }
    public void updateCalendarLanguage(boolean isVN) {
        // Bỏ trống vì giao diện Dashboard cũ không sử dụng CalendarPanel
    }
}