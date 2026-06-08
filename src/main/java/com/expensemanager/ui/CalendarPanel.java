package com.expensemanager.ui;

import com.expensemanager.database.DatabaseUtil;
import com.expensemanager.entity.Budget;
import com.expensemanager.entity.Transaction;
import com.expensemanager.entity.TransactionType;
import com.expensemanager.observer.EventType;
import com.expensemanager.observer.Observer;
import com.expensemanager.service.BudgetManager;
import com.expensemanager.service.FinanceService;
import com.expensemanager.service.SessionManager;
import com.expensemanager.util.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CalendarPanel extends JPanel implements Observer {

    private MainFrame mainFrame;
    private FinanceService financeService;
    private BudgetManager budgetManager;
    private boolean isVietnamese;

    private JLabel lblMonthYear;
    private JPanel daysPanel;
    private JButton btnPrevMonth, btnNextMonth;
    private LocalDate currentDate;
    private Map<LocalDate, Double> dailyExpenseMap;

    public CalendarPanel(MainFrame mainFrame, FinanceService financeService, BudgetManager budgetManager) {
        this.mainFrame = mainFrame;
        this.financeService = financeService;
        this.budgetManager = budgetManager;
        this.isVietnamese = mainFrame != null && mainFrame.isVietnamese();
        this.currentDate = LocalDate.now();
        this.dailyExpenseMap = new HashMap<>();

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        initComponents();
        refreshCalendar();
        applyTheme();
    }

    private void initComponents() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        btnPrevMonth = createNavButton("<");
        btnNextMonth = createNavButton(">");
        lblMonthYear = new JLabel("", SwingConstants.CENTER);
        lblMonthYear.setFont(new Font("Segoe UI", Font.BOLD, 20));

        btnPrevMonth.addActionListener(e -> { currentDate = currentDate.minusMonths(1); refreshCalendar(); });
        btnNextMonth.addActionListener(e -> { currentDate = currentDate.plusMonths(1); refreshCalendar(); });

        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        navPanel.setOpaque(false);
        navPanel.add(btnPrevMonth);
        navPanel.add(lblMonthYear);
        navPanel.add(btnNextMonth);
        header.add(navPanel, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);

        daysPanel = new JPanel(new GridLayout(0, 7, 8, 8));
        add(daysPanel, BorderLayout.CENTER);
    }

    private void refreshCalendar() {
        daysPanel.removeAll();
        dailyExpenseMap.clear();

        YearMonth yearMonth = YearMonth.from(currentDate);
        LocalDate firstOfMonth = yearMonth.atDay(1);
        int daysInMonth = yearMonth.lengthOfMonth();
        int startOffset = firstOfMonth.getDayOfWeek().getValue() % 7;

        String[] weekDays = isVietnamese ?
                new String[]{"T2", "T3", "T4", "T5", "T6", "T7", "CN"} :
                new String[]{"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        for (String d : weekDays) {
            JLabel lbl = new JLabel(d, SwingConstants.CENTER);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
            lbl.setForeground(ThemeManager.getColor("textSecondary"));
            daysPanel.add(lbl);
        }

        List<Transaction> allTx = financeService.getAllTransactions();
        double totalExpenseMonth = 0;
        for (Transaction t : allTx) {
            if (t.getType() == TransactionType.EXPENSE && t.getDateTime().getYear() == currentDate.getYear()
                    && t.getDateTime().getMonthValue() == currentDate.getMonthValue()) {
                LocalDate d = t.getDateTime().toLocalDate();
                dailyExpenseMap.put(d, dailyExpenseMap.getOrDefault(d, 0.0) + t.getAmount());
                totalExpenseMonth += t.getAmount();
            }
        }

        double dailyBudget = 0;
        String userId = SessionManager.getCurrentUserId();
        if (userId != null) {
            Budget budget = DatabaseUtil.getBudget(currentDate.getMonthValue(), currentDate.getYear(), userId);
            if (budget != null && budget.getLimit() > 0) {
                dailyBudget = budget.getLimit() / daysInMonth;
            }
        }

        for (int i = 0; i < startOffset; i++) {
            daysPanel.add(createEmptyDayCell());
        }

        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = LocalDate.of(currentDate.getYear(), currentDate.getMonth(), day);
            double expense = dailyExpenseMap.getOrDefault(date, 0.0);
            boolean isOver = dailyBudget > 0 && expense > dailyBudget;
            JPanel dayCell = createDayCell(day, expense, isOver);
            daysPanel.add(dayCell);
        }

        int totalCells = startOffset + daysInMonth;
        int remaining = (7 - (totalCells % 7)) % 7;
        for (int i = 0; i < remaining; i++) {
            daysPanel.add(createEmptyDayCell());
        }

        String monthStr = isVietnamese ?
                "Tháng " + currentDate.getMonthValue() + "/" + currentDate.getYear() :
                currentDate.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + currentDate.getYear();
        lblMonthYear.setText(monthStr);

        daysPanel.revalidate();
        daysPanel.repaint();
    }

    private JPanel createDayCell(int day, double expense, boolean isOver) {
        JPanel cell = new JPanel(new BorderLayout());
        cell.setBorder(BorderFactory.createLineBorder(ThemeManager.getColor("border"), 1));
        cell.setPreferredSize(new Dimension(80, 70));
        cell.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel lblDay = new JLabel(String.valueOf(day), SwingConstants.LEFT);
        lblDay.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblDay.setBorder(BorderFactory.createEmptyBorder(5,5,0,0));

        JLabel lblAmount = new JLabel(String.format("%,.0f", expense), SwingConstants.RIGHT);
        lblAmount.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblAmount.setBorder(BorderFactory.createEmptyBorder(0,0,5,5));

        cell.add(lblDay, BorderLayout.NORTH);
        cell.add(lblAmount, BorderLayout.SOUTH);

        cell.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                showTransactionsForDay(day);
            }
        });

        // Màu sẽ được set trong applyTheme() thông qua refreshCalendar
        return cell;
    }

    private JPanel createEmptyDayCell() {
        JPanel empty = new JPanel();
        empty.setBorder(BorderFactory.createLineBorder(ThemeManager.getColor("border"), 1));
        empty.setPreferredSize(new Dimension(80, 70));
        return empty;
    }

    private void showTransactionsForDay(int day) {
        LocalDate date = LocalDate.of(currentDate.getYear(), currentDate.getMonth(), day);
        List<Transaction> dayTx = financeService.getAllTransactions().stream()
                .filter(t -> t.getDateTime().toLocalDate().equals(date))
                .toList();
        if (dayTx.isEmpty()) {
            JOptionPane.showMessageDialog(this, isVietnamese ? "Không có giao dịch trong ngày này" : "No transactions on this day");
            return;
        }
        StringBuilder sb = new StringBuilder(isVietnamese ? "Giao dịch ngày " + day + "/" + currentDate.getMonthValue() + ":\n" : "Transactions on " + date + ":\n");
        for (Transaction t : dayTx) {
            sb.append(String.format("%s: %,.0f VND - %s\n", t.getType() == TransactionType.INCOME ? "+" : "-", t.getAmount(), t.getNote()));
        }
        JTextArea ta = new JTextArea(sb.toString());
        ta.setEditable(false);
        JScrollPane sp = new JScrollPane(ta);
        sp.setPreferredSize(new Dimension(400, 300));
        JOptionPane.showMessageDialog(this, sp, isVietnamese ? "Chi tiết ngày" : "Day Details", JOptionPane.INFORMATION_MESSAGE);
    }

    private JButton createNavButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    @Override
    public void update(EventType eventType, Object data) {
        if (eventType == EventType.TRANSACTION_ADDED || eventType == EventType.TRANSACTION_UPDATED ||
                eventType == EventType.TRANSACTION_DELETED || eventType == EventType.BUDGET_CHANGED ||
                eventType == EventType.DATA_LOADED) {
            SwingUtilities.invokeLater(this::refreshCalendar);
        }
    }

    public void applyTheme() {
        setBackground(ThemeManager.getColor("bg"));
        if (daysPanel != null) daysPanel.setBackground(ThemeManager.getColor("bg"));
        if (lblMonthYear != null) lblMonthYear.setForeground(ThemeManager.getColor("accent"));
        if (btnPrevMonth != null) {
            btnPrevMonth.setBackground(ThemeManager.getColor("input"));
            btnPrevMonth.setForeground(ThemeManager.getColor("textPrimary"));
        }
        if (btnNextMonth != null) {
            btnNextMonth.setBackground(ThemeManager.getColor("input"));
            btnNextMonth.setForeground(ThemeManager.getColor("textPrimary"));
        }
        refreshCalendar(); // cập nhật màu từng ô
    }

    // Khi refreshCalendar được gọi từ applyTheme, ta cần set màu cho các ô dựa trên theme
    // Sửa lại phương thức refreshCalendar để set màu theme, hoặc duyệt lại components sau refresh.
    // Tôi sẽ thêm phần set màu động ngay trong createDayCell bằng cách dùng ThemeManager.
    // Để đơn giản, tôi sửa createDayCell và createEmptyDayCell để dùng màu theme.
    private Color getDayCellBackground(boolean isOver) {
        if (isOver) {
            return ThemeManager.getColor("danger"); // vượt chi
        }
        return ThemeManager.getColor("surface");
    }

    private Color getDayAmountColor(boolean isOver) {
        return isOver ? ThemeManager.getColor("textPrimary") : ThemeManager.getColor("textPrimary");
    }

    // Do đã khai báo createDayCell ở trên, ta sửa lại phần set màu trong đó:
    // cell.setBackground(getDayCellBackground(isOver));
    // lblDay.setForeground(ThemeManager.getColor("textPrimary"));
    // lblAmount.setForeground(...);
    // Và createEmptyDayCell: empty.setBackground(ThemeManager.getColor("surface"));
}