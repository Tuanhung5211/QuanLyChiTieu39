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

    private JLabel lblMonthYear;
    private JLabel lblIncome;
    private JLabel lblExpense;
    private JLabel lblBalance;
    private JPanel transactionListPanel;
    private JScrollPane scrollPane;

    public static final Map<String, String> CATEGORY_EMOJI = new HashMap<>();
    static {
        CATEGORY_EMOJI.put("Mua sắm", "🛍️"); CATEGORY_EMOJI.put("Ăn uống", "🍔");
        CATEGORY_EMOJI.put("Điện thoại", "📱"); CATEGORY_EMOJI.put("Giải trí", "🎮");
        CATEGORY_EMOJI.put("Giáo dục", "📚"); CATEGORY_EMOJI.put("Làm đẹp", "💄");
        CATEGORY_EMOJI.put("Thể thao", "⚽"); CATEGORY_EMOJI.put("Xã hội", "👥");
        CATEGORY_EMOJI.put("Di chuyển", "🚗"); CATEGORY_EMOJI.put("Quần áo", "👗");
        CATEGORY_EMOJI.put("Xe cộ", "🏍️"); CATEGORY_EMOJI.put("Điện tử", "💻");
        CATEGORY_EMOJI.put("Du lịch", "✈️"); CATEGORY_EMOJI.put("Sức khỏe", "🏥");
        CATEGORY_EMOJI.put("Sửa chữa", "🔧"); CATEGORY_EMOJI.put("Nhà cửa", "🏠");
        CATEGORY_EMOJI.put("Quà tặng", "🎁"); CATEGORY_EMOJI.put("Từ thiện", "💖");
        CATEGORY_EMOJI.put("Ăn vặt", "🍿"); CATEGORY_EMOJI.put("Trái cây", "🍎");
        CATEGORY_EMOJI.put("Lương", "💰"); CATEGORY_EMOJI.put("Học bổng", "🎓");
        CATEGORY_EMOJI.put("Tiền được cho", "💵");
    }

    public DashboardPanel(MainFrame mainFrame, FinanceService financeService, BudgetManager budgetManager) {
        this.mainFrame = mainFrame;
        this.financeService = financeService;
        this.budgetManager = budgetManager;

        setLayout(new BorderLayout());
        setBackground(new Color(18, 18, 18)); // Đen sâu chuẩn App

        add(createHeader(), BorderLayout.NORTH);

        transactionListPanel = new JPanel();
        transactionListPanel.setLayout(new BoxLayout(transactionListPanel, BoxLayout.Y_AXIS));
        transactionListPanel.setBackground(new Color(18, 18, 18));
        transactionListPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        scrollPane = new JScrollPane(transactionListPanel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(new Color(18, 18, 18));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        // Nút thêm bự ở dưới (Giống ảnh 1)
        JButton btnAdd = new JButton("+ Thêm giao dịch");
        btnAdd.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnAdd.setForeground(new Color(18, 18, 18));
        btnAdd.setBackground(new Color(255, 193, 7)); // Vàng Money Tracker
        btnAdd.setFocusPainted(false);
        btnAdd.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        btnAdd.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnAdd.addActionListener(e -> {
            new AddTransactionDialog(mainFrame).setVisible(true);
            refreshData();
        });

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(new Color(18, 18, 18));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));
        bottomPanel.add(btnAdd, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        refreshData();
    }

    private JPanel createHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(18, 18, 18));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        lblMonthYear = new JLabel("Tháng " + java.time.LocalDate.now().getMonthValue() + "/" + java.time.LocalDate.now().getYear());
        lblMonthYear.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblMonthYear.setForeground(Color.WHITE);
        headerPanel.add(lblMonthYear, BorderLayout.NORTH);

        JPanel cardsPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        cardsPanel.setBackground(new Color(18, 18, 18));
        cardsPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        lblIncome = createStatCard("Thu nhập", "0", new Color(76, 175, 80), cardsPanel);
        lblExpense = createStatCard("Chi tiêu", "0", new Color(244, 67, 54), cardsPanel);
        lblBalance = createStatCard("Số dư", "0", Color.WHITE, cardsPanel);

        headerPanel.add(cardsPanel, BorderLayout.CENTER);
        return headerPanel;
    }

    private JLabel createStatCard(String title, String value, Color color, JPanel parent) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(30, 30, 30));
        card.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblTitle.setForeground(new Color(170, 170, 170));

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblValue.setForeground(color);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblValue, BorderLayout.SOUTH);
        parent.add(card);

        return lblValue;
    }

    public void refreshData() {
        if (financeService == null) return;
        financeService.syncFromDatabase();

        List<Transaction> transactions = financeService.getAllTransactions();
        transactions.sort((a, b) -> b.getDateTime().compareTo(a.getDateTime()));

        double totalIncome = transactions.stream().filter(t -> t.getType() == TransactionType.INCOME).mapToDouble(Transaction::getAmount).sum();
        double totalExpense = transactions.stream().filter(t -> t.getType() == TransactionType.EXPENSE).mapToDouble(Transaction::getAmount).sum();
        double balance = totalIncome - totalExpense;

        lblIncome.setText(String.format("%,.0f", totalIncome));
        lblExpense.setText(String.format("%,.0f", totalExpense));
        lblBalance.setText(String.format("%,.0f", balance));

        transactionListPanel.removeAll();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE, dd/MM");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        String currentDate = "";
        for (Transaction t : transactions) {
            String transactionDate = t.getDateTime().format(dateFormatter);

            if (!transactionDate.equals(currentDate)) {
                currentDate = transactionDate;
                JLabel lblDate = new JLabel(transactionDate);
                lblDate.setFont(new Font("Segoe UI", Font.BOLD, 14));
                lblDate.setForeground(new Color(150, 150, 150));
                lblDate.setBorder(BorderFactory.createEmptyBorder(15, 0, 5, 0));
                lblDate.setAlignmentX(Component.LEFT_ALIGNMENT);
                transactionListPanel.add(lblDate);
            }

            JPanel row = createTransactionRow(t, dateTimeFormatter);
            row.setAlignmentX(Component.LEFT_ALIGNMENT);
            transactionListPanel.add(row);
        }

        transactionListPanel.revalidate();
        transactionListPanel.repaint();
    }

    private JPanel createTransactionRow(Transaction t, DateTimeFormatter dateTimeFormatter) {
        JPanel row = new JPanel(new BorderLayout(15, 0));
        row.setBackground(new Color(18, 18, 18));
        row.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        Category cat = t.getCategory();
        String emoji = (cat != null) ? CATEGORY_EMOJI.getOrDefault(cat.getName(), "📌") : "📌";

        JLabel lblIcon = new JLabel(emoji, SwingConstants.CENTER);
        lblIcon.setOpaque(true);
        // Tái tạo màu nền xanh lơ/hồng nhẹ từ app gốc
        lblIcon.setBackground(new Color(40, 50, 60));
        lblIcon.setPreferredSize(new Dimension(45, 45));
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        lblIcon.setBorder(BorderFactory.createLineBorder(new Color(18,18,18), 1, true)); // Trick bo tròn nhẹ

        String description = (t.getNote() != null && !t.getNote().trim().isEmpty()) ? t.getNote().trim() : (cat != null ? cat.getName() : "Khác");
        JLabel lblDescription = new JLabel(description);
        lblDescription.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblDescription.setForeground(Color.WHITE);

        String amountStr = String.format("%s%,.0f", t.getType() == TransactionType.INCOME ? "+" : "-", t.getAmount());
        JLabel lblAmount = new JLabel(amountStr);
        lblAmount.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblAmount.setForeground(t.getType() == TransactionType.INCOME ? new Color(76, 175, 80) : new Color(244, 67, 54));

        row.add(lblIcon, BorderLayout.WEST);
        row.add(lblDescription, BorderLayout.CENTER);
        row.add(lblAmount, BorderLayout.EAST);

        row.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                new TransactionDetailDialog(mainFrame, t).setVisible(true);
            }
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) { row.setBackground(new Color(30, 30, 30)); }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) { row.setBackground(new Color(18, 18, 18)); }
        });

        return row;
    }
}