package com.expensemanager.ui;

import com.expensemanager.database.DatabaseUtil;
import com.expensemanager.entity.Category;
import com.expensemanager.entity.Transaction;
import com.expensemanager.entity.TransactionType;
import com.expensemanager.service.SessionManager;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.UUID;

public class AddTransactionDialog extends JDialog {
    private MainFrame mainFrame;
    private TransactionType selectedType = TransactionType.EXPENSE;
    private Category selectedCategory;
    private JTextField txtAmount;
    private JTextArea txtNote;
    private JPanel categoryPanel;
    private JButton btnExpense, btnIncome;
    private JScrollPane categoryScrollPane;

    private int currentPage = 1;
    private final int ITEMS_PER_PAGE = 8;
    private JButton btnPrevPage, btnNextPage;
    private JLabel lblPageIndicator;

    public static Map<String, String> customEmojiMap = new HashMap<>();
    private static final Map<String, String> CATEGORY_EMOJI = new LinkedHashMap<>();
    static {
        // --- 🛍️ DANH MỤC CHI TIÊU (EXPENSE) - 24 KHOẢN ---
        CATEGORY_EMOJI.put("Ăn uống", "🍔");
        CATEGORY_EMOJI.put("Đi chợ", "🛒");
        CATEGORY_EMOJI.put("Ăn vặt", "🍿");
        CATEGORY_EMOJI.put("Trái cây", "🍎");
        CATEGORY_EMOJI.put("Mua sắm", "🛍️");
        CATEGORY_EMOJI.put("Quần áo", "👗");
        CATEGORY_EMOJI.put("Điện tử", "💻");
        CATEGORY_EMOJI.put("Xăng dầu", "⛽");
        CATEGORY_EMOJI.put("Xe cộ", "🏍️");
        CATEGORY_EMOJI.put("Di chuyển", "🚗");
        CATEGORY_EMOJI.put("Điện nước", "⚡");
        CATEGORY_EMOJI.put("Internet", "🌐");
        CATEGORY_EMOJI.put("Thuê nhà", "🏢");
        CATEGORY_EMOJI.put("Điện thoại", "📱");
        CATEGORY_EMOJI.put("Giải trí", "🎮");
        CATEGORY_EMOJI.put("Phim ảnh", "🎬");
        CATEGORY_EMOJI.put("Du lịch", "✈️");
        CATEGORY_EMOJI.put("Học tập", "📚");
        CATEGORY_EMOJI.put("Sức khỏe", "🏥");
        CATEGORY_EMOJI.put("Thuốc men", "💊");
        CATEGORY_EMOJI.put("Làm đẹp", "💄");
        CATEGORY_EMOJI.put("Thể thao", "⚽");
        CATEGORY_EMOJI.put("Thú cưng", "🐱");
        CATEGORY_EMOJI.put("Chi khác", "📌");

        // --- 💰 DANH MỤC THU NHẬP (INCOME) - 8 KHOẢN ---
        CATEGORY_EMOJI.put("Lương", "💰");
        CATEGORY_EMOJI.put("Thưởng", "💵");
        CATEGORY_EMOJI.put("Học bổng", "🎓");
        CATEGORY_EMOJI.put("Được cho", "✉️");
        CATEGORY_EMOJI.put("Làm thêm", "💼");
        CATEGORY_EMOJI.put("Đầu tư", "📈");
        CATEGORY_EMOJI.put("Tiết kiệm", "🐷");
        CATEGORY_EMOJI.put("Thu khác", "🪙");
    }

    private final Color BG_COLOR = new Color(18, 18, 18);
    private final Color SURFACE_COLOR = new Color(30, 30, 30);
    private final Color ACCENT_YELLOW = new Color(255, 193, 7);
    private final Color TEXT_PRIMARY = new Color(240, 240, 240);

    public AddTransactionDialog(MainFrame parent) {
        super(parent, "Thêm giao dịch mới", true);
        this.mainFrame = parent;
        setSize(460, 580);
        setLocationRelativeTo(parent);
        getContentPane().setBackground(BG_COLOR);
        setLayout(new BorderLayout());

        // --- HEADER TABS ---
        JPanel header = new JPanel(new GridLayout(1, 2, 10, 0));
        header.setBackground(BG_COLOR);
        header.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        btnExpense = createTypeButton("CHI TIÊU", true);
        btnIncome = createTypeButton("THU NHẬP", false);

        btnExpense.addActionListener(e -> switchType(TransactionType.EXPENSE));
        btnIncome.addActionListener(e -> switchType(TransactionType.INCOME));

        header.add(btnExpense);
        header.add(btnIncome);
        add(header, BorderLayout.NORTH);

        // --- CENTER FORM ---
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(BG_COLOR);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 5, 20));

        // 1. Khung số tiền
        centerPanel.add(createLabel("Số tiền (VND)"));
        txtAmount = new JTextField();
        styleTextField(txtAmount, "0");
        txtAmount.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        centerPanel.add(txtAmount);

        centerPanel.add(Box.createVerticalStrut(12));
        centerPanel.add(createLabel("Chọn danh mục"));
        centerPanel.add(Box.createVerticalStrut(4));

        // ĐỔI: Sử dụng GridLayout(0, 4) để tự động co giãn số hàng theo linh hồn số lượng cấu phần
        categoryPanel = new JPanel(new GridLayout(0, 4, 12, 12));
        categoryPanel.setBackground(BG_COLOR);

        categoryScrollPane = new JScrollPane(categoryPanel);
        categoryScrollPane.setBorder(null);
        categoryScrollPane.getViewport().setBackground(BG_COLOR);
        categoryScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        categoryScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        centerPanel.add(categoryScrollPane);

        // Thanh điều hướng phân trang
        JPanel paginationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 4));
        paginationPanel.setOpaque(false);

        btnPrevPage = createArrowButton("◀");
        btnNextPage = createArrowButton("▶");
        lblPageIndicator = new JLabel("Trang 1 / 1");
        lblPageIndicator.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblPageIndicator.setForeground(TEXT_PRIMARY);

        btnPrevPage.addActionListener(e -> { if (currentPage > 1) { currentPage--; refreshCategories(); } });
        btnNextPage.addActionListener(e -> { currentPage++; refreshCategories(); });

        paginationPanel.add(btnPrevPage);
        paginationPanel.add(lblPageIndicator);
        paginationPanel.add(btnNextPage);
        centerPanel.add(paginationPanel);

        centerPanel.add(Box.createVerticalStrut(10));
        centerPanel.add(createLabel("Ghi chú"));
        centerPanel.add(Box.createVerticalStrut(4));

        // 3. Khung ghi chú (Chuẩn 2 dòng)
        txtNote = new JTextArea(2, 20);
        txtNote.setLineWrap(true);
        txtNote.setWrapStyleWord(true);
        txtNote.setBackground(SURFACE_COLOR);
        txtNote.setForeground(TEXT_PRIMARY);
        txtNote.setCaretColor(ACCENT_YELLOW);
        txtNote.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        txtNote.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));

        JScrollPane scrollNote = new JScrollPane(txtNote);
        scrollNote.setBorder(BorderFactory.createLineBorder(new Color(50, 50, 50), 1));
        scrollNote.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));
        scrollNote.setMinimumSize(new Dimension(10, 58));
        scrollNote.setPreferredSize(new Dimension(10, 58));
        centerPanel.add(scrollNote);

        // Đệm lò xo dồn ép khoảng trống xuống đáy
        centerPanel.add(Box.createVerticalGlue());

        add(centerPanel, BorderLayout.CENTER);

        // --- FOOTER BUTTONS ---
        JPanel footer = new JPanel(new GridLayout(1, 2, 10, 0));
        footer.setBackground(BG_COLOR);
        footer.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        JButton btnCancel = new JButton("HỦY BỎ");
        btnCancel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnCancel.setBackground(SURFACE_COLOR);
        btnCancel.setForeground(TEXT_PRIMARY);
        btnCancel.setFocusPainted(false);
        btnCancel.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));
        btnCancel.addActionListener(e -> dispose());

        JButton btnSave = new JButton("LƯU GIAO DỊCH");
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnSave.setBackground(ACCENT_YELLOW);
        btnSave.setForeground(BG_COLOR);
        btnSave.setFocusPainted(false);
        btnSave.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));
        btnSave.addActionListener(e -> saveTransaction());

        footer.add(btnCancel);
        footer.add(btnSave);
        add(footer, BorderLayout.SOUTH);

        refreshCategories();
    }

    private void switchType(TransactionType type) {
        this.selectedType = type;
        this.currentPage = 1;
        btnExpense.setBackground(type == TransactionType.EXPENSE ? new Color(244, 67, 54) : SURFACE_COLOR);
        btnIncome.setBackground(type == TransactionType.INCOME ? new Color(76, 175, 80) : SURFACE_COLOR);
        btnExpense.setForeground(Color.WHITE);
        btnIncome.setForeground(Color.WHITE);
        refreshCategories();
    }

    private void checkAndSeedCategories(List<Category> currentList) {
        if (currentList == null) currentList = new ArrayList<>();

        for (Map.Entry<String, String> entry : CATEGORY_EMOJI.entrySet()) {
            String name = entry.getKey();
            boolean isExisted = false;

            for (Category existingCat : currentList) {
                if (existingCat.getName().trim().equalsIgnoreCase(name.trim())) {
                    isExisted = true;
                    break;
                }
            }

            if (!isExisted) {
                TransactionType type = (name.equals("Lương") || name.equals("Thưởng") ||
                        name.equals("Học bổng") || name.equals("Được cho") ||
                        name.equals("Làm thêm") || name.equals("Đầu tư") ||
                        name.equals("Tiết kiệm") || name.equals("Thu khác"))
                        ? TransactionType.INCOME : TransactionType.EXPENSE;

                String generatedId = UUID.randomUUID().toString().substring(0, 8);
                Category newCat = new Category(generatedId, name, type);
                DatabaseUtil.insertCategory(newCat);
                currentList.add(newCat);
            }
        }
    }

    private void refreshCategories() {
        categoryPanel.removeAll();

        List<Category> allList = new ArrayList<>();
        try {
            allList = DatabaseUtil.getAllCategories();
        } catch (Exception e) {
            e.printStackTrace();
        }

        checkAndSeedCategories(allList);

        List<Category> filteredList = new ArrayList<>();
        for (Category c : allList) {
            if (c.getType() == selectedType) {
                filteredList.add(c);
            }
        }

        int totalItems = filteredList.size();
        int totalPages = (int) Math.ceil((double) totalItems / ITEMS_PER_PAGE);
        if (totalPages == 0) totalPages = 1;

        if (currentPage > totalPages) currentPage = totalPages;
        if (currentPage < 1) currentPage = 1;

        lblPageIndicator.setText("Trang " + currentPage + " / " + totalPages);
        btnPrevPage.setEnabled(currentPage > 1);
        btnNextPage.setEnabled(currentPage < totalPages);

        int startIndex = (currentPage - 1) * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, totalItems);

        int displayedCount = 0;
        for (int i = startIndex; i < endIndex; i++) {
            categoryPanel.add(createCategoryItem(filteredList.get(i)));
            displayedCount++;
        }

        // 🌟 CẢI TIẾN: Tính toán số lượng ô dựa trên số item hiển thị thực tế của trang hiện tại
        int itemsOnPage = endIndex - startIndex;
        int targetCount = (itemsOnPage <= 4) ? 4 : 8; // Nếu <= 4 mục thì ép lưới chỉ lấy 4 ô (1 hàng)
        int rows = (int) Math.ceil((double) targetCount / 4);

        // Điền ô trống tàng hình giữ khối vuông
        for (int i = displayedCount; i < targetCount; i++) {
            JPanel placeholder = new JPanel();
            placeholder.setOpaque(false);
            categoryPanel.add(placeholder);
        }

        // ĐỘNG: Cập nhật chiều cao của scrollpane vừa khít theo số hàng thực tế (1 hàng: ~88px, 2 hàng: ~175px)
        int calculatedHeight = rows * 78 + (rows - 1) * 12 + 6;
        categoryScrollPane.setPreferredSize(new Dimension(400, calculatedHeight));
        categoryScrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, calculatedHeight));
        categoryScrollPane.setMinimumSize(new Dimension(10, calculatedHeight));

        categoryPanel.revalidate();
        categoryPanel.repaint();
    }

    private JPanel createCategoryItem(Category c) {
        JPanel item = new JPanel(new BorderLayout(0, 4));
        item.setBackground(BG_COLOR);
        item.setPreferredSize(new Dimension(85, 78));
        item.setMinimumSize(new Dimension(85, 78));
        item.setMaximumSize(new Dimension(85, 78));
        item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        String emoji = customEmojiMap.containsKey(c.getName()) ? customEmojiMap.get(c.getName()) : CATEGORY_EMOJI.getOrDefault(c.getName(), "📌");

        JLabel lblIcon = new JLabel(emoji, SwingConstants.CENTER);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
        lblIcon.setOpaque(true);
        lblIcon.setBackground(SURFACE_COLOR);
        lblIcon.setPreferredSize(new Dimension(48, 48));
        lblIcon.setBorder(BorderFactory.createLineBorder(new Color(50, 50, 50), 1, true));

        JLabel lblName = new JLabel(c.getName(), SwingConstants.CENTER);
        lblName.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblName.setForeground(TEXT_PRIMARY);

        item.add(lblIcon, BorderLayout.CENTER);
        item.add(lblName, BorderLayout.SOUTH);

        item.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                selectedCategory = c;
                for (Component comp : categoryPanel.getComponents()) {
                    if (comp instanceof JPanel && comp.getMinimumSize().width == 85) {
                        ((JPanel) comp).getComponent(0).setBackground(SURFACE_COLOR);
                    }
                }
                lblIcon.setBackground(ACCENT_YELLOW);
            }
        });

        return item;
    }

    private void saveTransaction() {
        try {
            String userId = SessionManager.getCurrentUserId();
            if (userId == null) {
                JOptionPane.showMessageDialog(this, "Bạn chưa đăng nhập!");
                return;
            }
            double amount = Double.parseDouble(txtAmount.getText().trim());
            if (amount <= 0) {
                JOptionPane.showMessageDialog(this, "Số tiền phải lớn hơn 0!");
                return;
            }
            if (selectedCategory == null) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một danh mục!");
                return;
            }
            String note = txtNote.getText().trim();
            String id = UUID.randomUUID().toString().substring(0, 8);
            Transaction t = new Transaction(id, amount, selectedType, selectedCategory, note);

            if (mainFrame != null && mainFrame.getFinanceService() != null) {
                mainFrame.getFinanceService().addTransaction(t);
            } else {
                DatabaseUtil.insertTransaction(t, userId);
            }

            JOptionPane.showMessageDialog(this, "Thêm thành công!");
            dispose();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Số tiền nhập vào không hợp lệ!");
        }
    }

    public static void addCustomEmoji(String categoryName, String emoji) {
        customEmojiMap.put(categoryName, emoji);
    }

    private JButton createTypeButton(String text, boolean active) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setFocusPainted(false);
        b.setBackground(active ? (text.contains("CHI") ? new Color(244, 67, 54) : new Color(76, 175, 80)) : SURFACE_COLOR);
        b.setForeground(Color.WHITE);
        b.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        return b;
    }

    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(new Color(150, 150, 150));
        l.setFont(new Font("Segoe UI", Font.BOLD, 13));
        return l;
    }

    private JButton createArrowButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(TEXT_PRIMARY);
        btn.setBackground(SURFACE_COLOR);
        btn.setBorder(BorderFactory.createLineBorder(new Color(55, 55, 55), 1));
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(32, 26));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void styleTextField(JTextField tf, String placeholder) {
        tf.setBackground(SURFACE_COLOR);
        tf.setForeground(TEXT_PRIMARY);
        tf.setCaretColor(ACCENT_YELLOW);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(50, 50, 50)),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
    }
}