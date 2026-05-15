package com.expensemanager.ui;

import com.expensemanager.entity.Category;
import com.expensemanager.entity.Transaction;
import com.expensemanager.entity.TransactionType;
import com.expensemanager.service.BudgetManager;
import com.expensemanager.service.FinanceService;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    // Component tìm kiếm & Lọc mới gộp vào
    private JTextField txtSearch;
    private JComboBox<String> cmbFilter;

    private final Color BG_COLOR = new Color(18, 18, 18);
    private final Color SURFACE_COLOR = new Color(30, 30, 30);
    private final Color SURFACE_HOVER = new Color(45, 45, 45);
    private final Color TEXT_PRIMARY = new Color(240, 240, 240);
    private final Color TEXT_SECONDARY = new Color(150, 150, 150);
    private final Color ACCENT_YELLOW = new Color(255, 193, 7);
    private final Color ACCENT_GREEN = new Color(76, 175, 80);
    private final Color ACCENT_RED = new Color(244, 67, 54);

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
        setBackground(BG_COLOR);

        // Khởi tạo Panel trước
        JPanel topContainer = new JPanel();
        // Sau đó mới set Layout cho nó
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setBackground(BG_COLOR);
        topContainer.add(createHeader());
        topContainer.add(createSearchAndFilterBar()); // Thêm thanh tìm kiếm vào đây

        add(topContainer, BorderLayout.NORTH);

        transactionListPanel = new JPanel();
        transactionListPanel.setLayout(new BoxLayout(transactionListPanel, BoxLayout.Y_AXIS));
        transactionListPanel.setBackground(BG_COLOR);
        transactionListPanel.setBorder(BorderFactory.createEmptyBorder(10, 30, 20, 30));

        scrollPane = new JScrollPane(transactionListPanel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(BG_COLOR);
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);
        applyModernScrollBar(scrollPane);
        add(scrollPane, BorderLayout.CENTER);

        JButton btnAdd = new JButton("+ Thêm giao dịch mới");
        btnAdd.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnAdd.setForeground(BG_COLOR);
        btnAdd.setBackground(ACCENT_YELLOW);
        btnAdd.setFocusPainted(false);
        btnAdd.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        btnAdd.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnAdd.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        btnAdd.addActionListener(e -> {
            new AddTransactionDialog(mainFrame).setVisible(true);
            refreshData();
        });

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(BG_COLOR);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(15, 50, 25, 50));
        bottomPanel.add(btnAdd, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        refreshData();
    }

    private JPanel createHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BG_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 20, 30));

        lblMonthYear = new JLabel("Tháng " + java.time.LocalDate.now().getMonthValue() + " năm " + java.time.LocalDate.now().getYear());
        lblMonthYear.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblMonthYear.setForeground(TEXT_PRIMARY);
        headerPanel.add(lblMonthYear, BorderLayout.NORTH);

        JPanel cardsPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        cardsPanel.setBackground(BG_COLOR);
        cardsPanel.setBorder(BorderFactory.createEmptyBorder(25, 0, 0, 0));

        lblIncome = createStatCard("Tổng thu nhập", "0", ACCENT_GREEN, cardsPanel);
        lblExpense = createStatCard("Tổng chi tiêu", "0", ACCENT_RED, cardsPanel);
        lblBalance = createStatCard("Số dư hiện tại", "0", TEXT_PRIMARY, cardsPanel);

        headerPanel.add(cardsPanel, BorderLayout.CENTER);
        return headerPanel;
    }

    // THANH TÌM KIẾM TÍCH HỢP
    private JPanel createSearchAndFilterBar() {
        JPanel barPanel = new JPanel(new BorderLayout(15, 0));
        barPanel.setBackground(BG_COLOR);
        barPanel.setBorder(BorderFactory.createEmptyBorder(0, 30, 10, 30));

        // Khung search bo góc tàng hình
        RoundedPanel searchBox = new RoundedPanel(20, SURFACE_COLOR);
        searchBox.setLayout(new BorderLayout(10, 0));
        searchBox.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));

        JLabel lblSearchIcon = new JLabel("🔍");
        lblSearchIcon.setForeground(TEXT_SECONDARY);
        lblSearchIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));

        txtSearch = new JTextField();
        txtSearch.setOpaque(false);
        txtSearch.setBorder(null);
        txtSearch.setForeground(TEXT_PRIMARY);
        txtSearch.setCaretColor(ACCENT_YELLOW);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 15));

        // Auto filter khi gõ chữ
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { refreshData(); }
            public void removeUpdate(DocumentEvent e) { refreshData(); }
            public void changedUpdate(DocumentEvent e) { refreshData(); }
        });

        searchBox.add(lblSearchIcon, BorderLayout.WEST);
        searchBox.add(txtSearch, BorderLayout.CENTER);

        // Combo box lọc Thu / Chi
        cmbFilter = new JComboBox<>(new String[]{"Tất cả", "Chỉ Thu Nhập", "Chỉ Chi Tiêu"});
        cmbFilter.setFont(new Font("Segoe UI", Font.BOLD, 13));
        cmbFilter.setBackground(SURFACE_COLOR);
        cmbFilter.setForeground(TEXT_PRIMARY);
        cmbFilter.setFocusable(false);
        cmbFilter.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cmbFilter.addActionListener(e -> refreshData());

        barPanel.add(searchBox, BorderLayout.CENTER);
        barPanel.add(cmbFilter, BorderLayout.EAST);

        return barPanel;
    }

    private JLabel createStatCard(String title, String value, Color valueColor, JPanel parent) {
        RoundedPanel card = new RoundedPanel(20, SURFACE_COLOR);
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lblTitle.setForeground(TEXT_SECONDARY);

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblValue.setForeground(valueColor);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblValue, BorderLayout.SOUTH);
        parent.add(card);

        return lblValue;
    }

    public void refreshData() {
        if (financeService == null) return;
        financeService.syncFromDatabase();

        List<Transaction> allTransactions = financeService.getAllTransactions();

        // Cập nhật thẻ Header dựa trên TẤT CẢ giao dịch
        double totalIncome = allTransactions.stream().filter(t -> t.getType() == TransactionType.INCOME).mapToDouble(Transaction::getAmount).sum();
        double totalExpense = allTransactions.stream().filter(t -> t.getType() == TransactionType.EXPENSE).mapToDouble(Transaction::getAmount).sum();
        double balance = totalIncome - totalExpense;

        lblIncome.setText(String.format("%,.0f ₫", totalIncome));
        lblExpense.setText(String.format("%,.0f ₫", totalExpense));
        lblBalance.setText(String.format("%,.0f ₫", balance));

        // Xử lý Lọc & Tìm kiếm cho List bên dưới
        String keyword = txtSearch != null ? txtSearch.getText().toLowerCase().trim() : "";
        String filterType = cmbFilter != null ? (String) cmbFilter.getSelectedItem() : "Tất cả";

        List<Transaction> filteredList = allTransactions.stream()
                .filter(t -> {
                    // Lọc theo ComboBox
                    if (filterType.equals("Chỉ Thu Nhập") && t.getType() != TransactionType.INCOME) return false;
                    if (filterType.equals("Chỉ Chi Tiêu") && t.getType() != TransactionType.EXPENSE) return false;

                    // Lọc theo Keyword
                    if (!keyword.isEmpty()) {
                        String note = t.getNote() != null ? t.getNote().toLowerCase() : "";
                        String catName = t.getCategory() != null ? t.getCategory().getName().toLowerCase() : "";
                        return note.contains(keyword) || catName.contains(keyword);
                    }
                    return true;
                })
                .sorted((a, b) -> b.getDateTime().compareTo(a.getDateTime())) // Sort mới nhất lên đầu
                .collect(Collectors.toList());

        // Render List
        transactionListPanel.removeAll();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE, dd/MM");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        if (filteredList.isEmpty()) {
            JLabel emptyLbl = new JLabel("Không tìm thấy giao dịch nào.");
            emptyLbl.setForeground(TEXT_SECONDARY);
            emptyLbl.setFont(new Font("Segoe UI", Font.ITALIC, 14));
            emptyLbl.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
            transactionListPanel.add(emptyLbl);
        } else {
            String currentDate = "";
            for (Transaction t : filteredList) {
                String transactionDate = t.getDateTime().format(dateFormatter);

                if (!transactionDate.equals(currentDate)) {
                    currentDate = transactionDate;
                    JLabel lblDate = new JLabel(transactionDate.toUpperCase());
                    lblDate.setFont(new Font("Segoe UI", Font.BOLD, 13));
                    lblDate.setForeground(TEXT_SECONDARY);
                    lblDate.setBorder(BorderFactory.createEmptyBorder(20, 5, 10, 0));
                    lblDate.setAlignmentX(Component.LEFT_ALIGNMENT);
                    transactionListPanel.add(lblDate);
                }

                JPanel row = createTransactionRow(t, dateTimeFormatter);
                row.setAlignmentX(Component.LEFT_ALIGNMENT);
                transactionListPanel.add(row);
                transactionListPanel.add(Box.createRigidArea(new Dimension(0, 8)));
            }
        }

        transactionListPanel.revalidate();
        transactionListPanel.repaint();
    }

    private JPanel createTransactionRow(Transaction t, DateTimeFormatter dateTimeFormatter) {
        RoundedPanel row = new RoundedPanel(15, SURFACE_COLOR);
        row.setLayout(new BorderLayout(20, 0));
        row.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 75));
        row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        Category cat = t.getCategory();
        String emoji = (cat != null) ? CATEGORY_EMOJI.getOrDefault(cat.getName(), "📌") : "📌";

        JLabel lblIcon = new JLabel(emoji, SwingConstants.CENTER);
        lblIcon.setOpaque(true);
        lblIcon.setBackground(new Color(50, 55, 65));
        lblIcon.setPreferredSize(new Dimension(45, 45));
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
        lblIcon.setBorder(BorderFactory.createLineBorder(SURFACE_COLOR, 2, true));

        JPanel centerInfo = new JPanel(new GridLayout(2, 1, 0, 3));
        centerInfo.setOpaque(false);

        String description = (t.getNote() != null && !t.getNote().trim().isEmpty()) ? t.getNote().trim() : (cat != null ? cat.getName() : "Khác");
        JLabel lblDescription = new JLabel(description);
        lblDescription.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblDescription.setForeground(TEXT_PRIMARY);

        JLabel lblTime = new JLabel(t.getDateTime().format(dateTimeFormatter) + " • " + (cat != null ? cat.getName() : ""));
        lblTime.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTime.setForeground(TEXT_SECONDARY);

        centerInfo.add(lblDescription);
        centerInfo.add(lblTime);

        String amountStr = String.format("%s%,.0f ₫", t.getType() == TransactionType.INCOME ? "+" : "-", t.getAmount());
        JLabel lblAmount = new JLabel(amountStr);
        lblAmount.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblAmount.setForeground(t.getType() == TransactionType.INCOME ? ACCENT_GREEN : ACCENT_RED);

        row.add(lblIcon, BorderLayout.WEST);
        row.add(centerInfo, BorderLayout.CENTER);
        row.add(lblAmount, BorderLayout.EAST);

        row.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                new TransactionDetailDialog(mainFrame, t).setVisible(true);
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                row.setBackgroundColor(SURFACE_HOVER);
                row.repaint();
            }
            @Override
            public void mouseExited(MouseEvent e) {
                row.setBackgroundColor(SURFACE_COLOR);
                row.repaint();
            }
        });

        return row;
    }

    private void applyModernScrollBar(JScrollPane scrollPane) {
        scrollPane.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(70, 70, 70);
                this.trackColor = BG_COLOR;
            }
            @Override
            protected JButton createDecreaseButton(int orientation) { return createZeroButton(); }
            @Override
            protected JButton createIncreaseButton(int orientation) { return createZeroButton(); }

            private JButton createZeroButton() {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(0, 0));
                button.setMinimumSize(new Dimension(0, 0));
                button.setMaximumSize(new Dimension(0, 0));
                return button;
            }
        });
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
    }

    class RoundedPanel extends JPanel {
        private int cornerRadius;
        private Color bgColor;

        public RoundedPanel(int radius, Color bgColor) {
            super();
            this.cornerRadius = radius;
            this.bgColor = bgColor;
            setOpaque(false);
        }

        public void setBackgroundColor(Color color) {
            this.bgColor = color;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Dimension arcs = new Dimension(cornerRadius, cornerRadius);
            int width = getWidth();
            int height = getHeight();
            Graphics2D graphics = (Graphics2D) g;
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (bgColor != null) {
                graphics.setColor(bgColor);
            } else {
                graphics.setColor(getBackground());
            }
            graphics.fillRoundRect(0, 0, width - 1, height - 1, arcs.width, arcs.height);
        }
    }
}