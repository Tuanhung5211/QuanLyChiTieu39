package com.expensemanager.ui;

import com.expensemanager.entity.Category;
import com.expensemanager.entity.Transaction;
import com.expensemanager.entity.TransactionType;
import com.expensemanager.service.BudgetManager;
import com.expensemanager.service.FinanceService;

import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class DashboardPanel extends JPanel {
    private MainFrame mainFrame;
    private FinanceService financeService;
    private BudgetManager budgetManager;

    // Header
    private JLabel lblMonthYear;
    private JLabel lblIncome;
    private JLabel lblExpense;
    private JLabel lblBalance;

    // Danh sách giao dịch
    private JPanel transactionListPanel;
    private JScrollPane scrollPane;

    // Map emoji cho danh mục
    private static final Map<String, String> CATEGORY_EMOJI = new HashMap<>();
    static {
        CATEGORY_EMOJI.put("Mua sắm", "🛍️");
        CATEGORY_EMOJI.put("Ăn uống", "🍔");
        CATEGORY_EMOJI.put("Điện thoại", "📱");
        CATEGORY_EMOJI.put("Giải trí", "🎮");
        CATEGORY_EMOJI.put("Giáo dục", "📚");
        CATEGORY_EMOJI.put("Làm đẹp", "💄");
        CATEGORY_EMOJI.put("Thể thao", "⚽");
        CATEGORY_EMOJI.put("Xã hội", "👥");
        CATEGORY_EMOJI.put("Di chuyển", "🚗");
        CATEGORY_EMOJI.put("Quần áo", "👗");
        CATEGORY_EMOJI.put("Xe cộ", "🏍️");
        CATEGORY_EMOJI.put("Điện tử", "💻");
        CATEGORY_EMOJI.put("Du lịch", "✈️");
        CATEGORY_EMOJI.put("Sức khỏe", "🏥");
        CATEGORY_EMOJI.put("Sửa chữa", "🔧");
        CATEGORY_EMOJI.put("Nhà cửa", "🏠");
        CATEGORY_EMOJI.put("Quà tặng", "🎁");
        CATEGORY_EMOJI.put("Từ thiện", "💖");
        CATEGORY_EMOJI.put("Ăn vặt", "🍿");
        CATEGORY_EMOJI.put("Trái cây", "🍎");
        CATEGORY_EMOJI.put("Lương", "💰");
        CATEGORY_EMOJI.put("Học bổng", "🎓");
        CATEGORY_EMOJI.put("Tiền được cho", "💵");
    }

    public DashboardPanel(MainFrame mainFrame, FinanceService financeService, BudgetManager budgetManager) {
        this.mainFrame = mainFrame;
        this.financeService = financeService;
        this.budgetManager = budgetManager;

        setLayout(new BorderLayout());
        setBackground(new Color(30, 30, 30));

        // Header
        JPanel headerPanel = createHeader();
        add(headerPanel, BorderLayout.NORTH);

        // Danh sách giao dịch
        transactionListPanel = new JPanel();
        transactionListPanel.setLayout(new BoxLayout(transactionListPanel, BoxLayout.Y_AXIS));
        transactionListPanel.setBackground(new Color(30, 30, 30));
        transactionListPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        scrollPane = new JScrollPane(transactionListPanel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(new Color(30, 30, 30));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, BorderLayout.CENTER);

        // Nút thêm giao dịch
        JButton btnAdd = new JButton("＋ Thêm giao dịch");
        btnAdd.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setBackground(new Color(0, 153, 76));
        btnAdd.setFocusPainted(false);
        btnAdd.setBorder(BorderFactory.createEmptyBorder(12, 30, 12, 30));
        btnAdd.addActionListener(e -> {
            new AddTransactionDialog(mainFrame).setVisible(true);
            refreshData();
        });
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setBackground(new Color(30, 30, 30));
        bottomPanel.add(btnAdd);
        add(bottomPanel, BorderLayout.SOUTH);

        refreshData();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(40, 40, 40));
        header.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        lblMonthYear = new JLabel(getCurrentMonthYear());
        lblMonthYear.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblMonthYear.setForeground(Color.WHITE);

        JPanel summaryPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        summaryPanel.setBackground(new Color(40, 40, 40));
        summaryPanel.setOpaque(false);

        lblIncome = createSummaryLabel("Thu nhập", "0 VND", new Color(0, 200, 0));
        lblExpense = createSummaryLabel("Chi tiêu", "0 VND", new Color(255, 80, 80));
        lblBalance = createSummaryLabel("Số dư", "0 VND", Color.WHITE);

        summaryPanel.add(lblIncome);
        summaryPanel.add(lblExpense);
        summaryPanel.add(lblBalance);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(new Color(40, 40, 40));
        rightPanel.setOpaque(false);
        rightPanel.add(summaryPanel, BorderLayout.EAST);

        header.add(lblMonthYear, BorderLayout.WEST);
        header.add(rightPanel, BorderLayout.CENTER);

        return header;
    }

    private JLabel createSummaryLabel(String title, String value, Color valueColor) {
        JPanel panel = new JPanel(new GridLayout(2, 1));
        panel.setOpaque(false);

        JLabel lblTitle = new JLabel(title, SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblTitle.setForeground(Color.LIGHT_GRAY);

        JLabel lblValue = new JLabel(value, SwingConstants.CENTER);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblValue.setForeground(valueColor);

        panel.add(lblTitle);
        panel.add(lblValue);

        JLabel wrapper = new JLabel();
        wrapper.setLayout(new BorderLayout());
        wrapper.add(panel, BorderLayout.CENTER);
        return lblValue;
    }

    public void refreshData() {
        if (financeService == null) return;
        financeService.syncFromDatabase();

        List<Transaction> transactions = financeService.getAllTransactions();
        transactions.sort((a, b) -> b.getDateTime().compareTo(a.getDateTime()));

        double totalIncome = transactions.stream()
                .filter(t -> t.getType() == TransactionType.INCOME)
                .mapToDouble(Transaction::getAmount).sum();
        double totalExpense = transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .mapToDouble(Transaction::getAmount).sum();
        double balance = totalIncome - totalExpense;

        lblIncome.setText(String.format("%,.0f VND", totalIncome));
        lblExpense.setText(String.format("%,.0f VND", totalExpense));
        lblBalance.setText(String.format("%,.0f VND", balance));
        lblMonthYear.setText(getCurrentMonthYear());

        transactionListPanel.removeAll();

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        String currentDate = "";
        JPanel dateGroup = null;

        for (Transaction t : transactions) {
            String transactionDate = t.getDateTime().format(dateFormatter);

            if (!transactionDate.equals(currentDate)) {
                currentDate = transactionDate;
                dateGroup = new JPanel();
                dateGroup.setLayout(new BoxLayout(dateGroup, BoxLayout.Y_AXIS));
                dateGroup.setBackground(new Color(30, 30, 30));
                dateGroup.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));

                JLabel lblDate = new JLabel(transactionDate);
                lblDate.setFont(new Font("Segoe UI", Font.BOLD, 14));
                lblDate.setForeground(new Color(180, 180, 180));
                lblDate.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
                lblDate.setAlignmentX(Component.LEFT_ALIGNMENT);
                dateGroup.add(lblDate);

                JSeparator sep = new JSeparator();
                sep.setForeground(new Color(80, 80, 80));
                sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
                sep.setAlignmentX(Component.LEFT_ALIGNMENT);
                dateGroup.add(sep);

                transactionListPanel.add(dateGroup);
            }

            JPanel row = createTransactionRow(t, dateTimeFormatter);
            row.setAlignmentX(Component.LEFT_ALIGNMENT);
            dateGroup.add(row);

            JSeparator rowSep = new JSeparator();
            rowSep.setForeground(new Color(50, 50, 50));
            rowSep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
            rowSep.setAlignmentX(Component.LEFT_ALIGNMENT);
            dateGroup.add(rowSep);
        }

        transactionListPanel.revalidate();
        transactionListPanel.repaint();
    }

    private JPanel createTransactionRow(Transaction t, DateTimeFormatter dateTimeFormatter) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(new Color(30, 30, 30));
        row.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); // con trỏ tay

        // Icon danh mục
        Category cat = t.getCategory();
        String emoji = (cat != null) ? CATEGORY_EMOJI.getOrDefault(cat.getName(), "📌") : "📌";
        JLabel lblIcon = new JLabel(emoji);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        lblIcon.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));

        // Chỉ hiển thị một dòng: ghi chú (nếu có) hoặc ngày giờ đầy đủ
        String description;
        if (t.getNote() != null && !t.getNote().trim().isEmpty()) {
            description = t.getNote().trim();
        } else {
            description = t.getDateTime().format(dateTimeFormatter);
        }
        JLabel lblDescription = new JLabel(description);
        lblDescription.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblDescription.setForeground(Color.WHITE);

        // Số tiền
        String amountStr = String.format("%s%,.0f VND",
                t.getType() == TransactionType.INCOME ? "+" : "-",
                t.getAmount());
        JLabel lblAmount = new JLabel(amountStr);
        lblAmount.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblAmount.setForeground(t.getType() == TransactionType.INCOME ? new Color(0, 200, 0) : new Color(255, 80, 80));

        row.add(lblIcon, BorderLayout.WEST);
        row.add(lblDescription, BorderLayout.CENTER);
        row.add(lblAmount, BorderLayout.EAST);

        // Thêm sự kiện click
        row.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                new TransactionDetailDialog(mainFrame, t).setVisible(true);
            }
        });

        return row;
    }

    private String getCurrentMonthYear() {
        java.time.LocalDate now = java.time.LocalDate.now();
        return "Tháng " + now.getMonthValue() + "/" + now.getYear();
    }
}