package com.expensemanager.ui;

import com.expensemanager.database.DatabaseUtil;
import com.expensemanager.entity.Category;
import com.expensemanager.entity.Transaction;
import com.expensemanager.entity.TransactionType;
import com.expensemanager.service.SessionManager;
import com.expensemanager.util.InputValidator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;
import java.util.UUID;

public class AddTransactionDialog extends JDialog {

    // =====================================================================
    // 1. KHAI BÁO BIẾN GIAO DIỆN VÀ LOGIC
    // =====================================================================
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
    private boolean isVietnamese = true;

    public static Map<String, String> customEmojiMap = new HashMap<>();
    private static final Map<String, String> CATEGORY_EMOJI = new LinkedHashMap<>();

    static {
        CATEGORY_EMOJI.put("Ăn uống", "\uD83C\uDF54");
        CATEGORY_EMOJI.put("Đi chợ", "\uD83D\uDED2");
        CATEGORY_EMOJI.put("Ăn vặt", "\uD83C\uDF7F");
        CATEGORY_EMOJI.put("Trái cây", "\uD83C\uDF4E");
        CATEGORY_EMOJI.put("Mua sắm", "\uD83D\uDECD");
        CATEGORY_EMOJI.put("Quần áo", "\uD83D\uDC57");
        CATEGORY_EMOJI.put("Điện tử", "\uD83D\uDCBB");
        CATEGORY_EMOJI.put("Xăng dầu", "\u26FD");
        CATEGORY_EMOJI.put("Xe cộ", "\uD83C\uDFCD");
        CATEGORY_EMOJI.put("Di chuyển", "\uD83D\uDE97");
        CATEGORY_EMOJI.put("Điện nước", "\u26A1");
        CATEGORY_EMOJI.put("Internet", "\uD83C\uDF10");
        CATEGORY_EMOJI.put("Thuê nhà", "\uD83C\uDFE2");
        CATEGORY_EMOJI.put("Điện thoại", "\uD83D\uDCF1");
        CATEGORY_EMOJI.put("Giải trí", "\uD83C\uDFAE");
        CATEGORY_EMOJI.put("Phim ảnh", "\uD83C\uDFAC");
        CATEGORY_EMOJI.put("Du lịch", "\u2708");
        CATEGORY_EMOJI.put("Học tập", "\uD83D\uDCDA");
        CATEGORY_EMOJI.put("Sức khỏe", "\uD83C\uDFE5");
        CATEGORY_EMOJI.put("Thuốc men", "\uD83D\uDC8A");
        CATEGORY_EMOJI.put("Làm đẹp", "\uD83D\uDC84");
        CATEGORY_EMOJI.put("Thể thao", "\u26BD");
        CATEGORY_EMOJI.put("Thú cưng", "\uD83D\uDC31");
        CATEGORY_EMOJI.put("Chi khác", "\uD83D\uDCCD");

        CATEGORY_EMOJI.put("Lương", "\uD83D\uDCB0");
        CATEGORY_EMOJI.put("Thưởng", "\uD83D\uDCB5");
        CATEGORY_EMOJI.put("Học bổng", "\uD83C\uDF93");
        CATEGORY_EMOJI.put("Được cho", "\u2709\uFE0F");
        CATEGORY_EMOJI.put("Làm thêm", "\uD83D\uDCBC");
        CATEGORY_EMOJI.put("Đầu tư", "\uD83D\uDCC8");
        CATEGORY_EMOJI.put("Tiết kiệm", "\uD83D\uDC37");
        CATEGORY_EMOJI.put("Thu khác", "\uD83E\uDE99");
    }

    private final Color BG_COLOR = new Color(18, 18, 18);
    private final Color SURFACE_COLOR = new Color(30, 30, 30);
    private final Color ACCENT_YELLOW = new Color(255, 193, 7);
    private final Color TEXT_PRIMARY = new Color(240, 240, 240);

    // =====================================================================
    // 2. CONSTRUCTOR VÀ KHỞI TẠO BỐ CỤC UI
    // =====================================================================
    public AddTransactionDialog(MainFrame parent) {
        super(parent, parent != null && parent.isVietnamese() ? "Thêm giao dịch mới" : "Add New Transaction", true);
        this.mainFrame = parent;
        if (parent != null) {
            this.isVietnamese = parent.isVietnamese();
        }

        setSize(460, 580);
        setLocationRelativeTo(parent);
        getContentPane().setBackground(BG_COLOR);
        setLayout(new BorderLayout());

        initComponents();
        refreshCategories();
    }

    private void initComponents() {
        JPanel header = new JPanel(new GridLayout(1, 2, 10, 0));
        header.setBackground(BG_COLOR);
        header.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        btnExpense = createTypeButton(isVietnamese ? "CHI TIÊU" : "EXPENSE", true);
        btnIncome = createTypeButton(isVietnamese ? "THU NHẬP" : "INCOME", false);

        btnExpense.addActionListener(e -> switchType(TransactionType.EXPENSE));
        btnIncome.addActionListener(e -> switchType(TransactionType.INCOME));

        header.add(btnExpense);
        header.add(btnIncome);
        add(header, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(BG_COLOR);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 5, 20));

        centerPanel.add(createLabel(isVietnamese ? "Số tiền (VND)" : "Amount (VND)"));
        txtAmount = new JTextField();
        styleTextField(txtAmount, "0");
        txtAmount.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        centerPanel.add(txtAmount);

        centerPanel.add(Box.createVerticalStrut(12));
        centerPanel.add(createLabel(isVietnamese ? "Chọn danh mục" : "Select Category"));
        centerPanel.add(Box.createVerticalStrut(4));

        categoryPanel = new JPanel(new GridLayout(0, 4, 12, 12));
        categoryPanel.setBackground(BG_COLOR);

        categoryScrollPane = new JScrollPane(categoryPanel);
        categoryScrollPane.setBorder(null);
        categoryScrollPane.getViewport().setBackground(BG_COLOR);
        categoryScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        categoryScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        categoryScrollPane.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(categoryScrollPane);

        JPanel paginationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 4));
        paginationPanel.setOpaque(false);
        paginationPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        btnPrevPage = createArrowButton("<");
        btnNextPage = createArrowButton(">");
        lblPageIndicator = new JLabel(isVietnamese ? "Trang 1 / 1" : "Page 1 / 1");
        lblPageIndicator.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblPageIndicator.setForeground(TEXT_PRIMARY);

        btnPrevPage.addActionListener(e -> { if (currentPage > 1) { currentPage--; refreshCategories(); } });
        btnNextPage.addActionListener(e -> { currentPage++; refreshCategories(); });

        paginationPanel.add(btnPrevPage);
        paginationPanel.add(lblPageIndicator);
        paginationPanel.add(btnNextPage);
        centerPanel.add(paginationPanel);

        centerPanel.add(Box.createVerticalStrut(10));
        centerPanel.add(createLabel(isVietnamese ? "Ghi chú" : "Transaction Note"));
        centerPanel.add(Box.createVerticalStrut(4));

        txtNote = new JTextArea(2, 20);
        txtNote.setLineWrap(true);
        txtNote.setWrapStyleWord(true);
        txtNote.setBackground(SURFACE_COLOR);
        txtNote.setForeground(TEXT_PRIMARY);
        txtNote.setCaretColor(ACCENT_YELLOW);
        txtNote.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        txtNote.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));

        txtNote.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void check() {
                if (txtNote.getText().length() > 200) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(AddTransactionDialog.this,
                                isVietnamese ? "Ghi chú không được vượt quá 200 ký tự!" : "Note cannot exceed 200 characters!",
                                isVietnamese ? "Cảnh báo" : "Warning", JOptionPane.WARNING_MESSAGE);
                        txtNote.setText(txtNote.getText().substring(0, 200));
                    });
                }
            }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { check(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { check(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { check(); }
        });

        JScrollPane scrollNote = new JScrollPane(txtNote);
        scrollNote.setBorder(BorderFactory.createLineBorder(new Color(50, 50, 50), 1));
        scrollNote.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));
        scrollNote.setMinimumSize(new Dimension(10, 58));
        scrollNote.setPreferredSize(new Dimension(10, 58));
        scrollNote.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(scrollNote);

        centerPanel.add(Box.createVerticalGlue());
        add(centerPanel, BorderLayout.CENTER);

        JPanel footer = new JPanel(new GridLayout(1, 2, 10, 0));
        footer.setBackground(BG_COLOR);
        footer.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        JButton btnCancel = new JButton(isVietnamese ? "HỦY BỎ" : "CANCEL");
        btnCancel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnCancel.setBackground(SURFACE_COLOR);
        btnCancel.setForeground(TEXT_PRIMARY);
        btnCancel.setFocusPainted(false);
        btnCancel.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));
        btnCancel.addActionListener(e -> dispose());

        JButton btnSave = new JButton(isVietnamese ? "LƯU GIAO DỊCH" : "SAVE TRANSACTION");
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnSave.setBackground(ACCENT_YELLOW);
        btnSave.setForeground(BG_COLOR);
        btnSave.setFocusPainted(false);
        btnSave.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));
        btnSave.addActionListener(e -> saveTransaction());

        footer.add(btnCancel);
        footer.add(btnSave);
        add(footer, BorderLayout.SOUTH);
    }

    // =====================================================================
    // 3. QUẢN LÝ LỌC & PHÂN TRANG DANH MỤC
    // =====================================================================
    private void switchType(TransactionType type) {
        this.selectedType = type;
        this.currentPage = 1;
        this.selectedCategory = null;
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
        try { allList = DatabaseUtil.getAllCategories(); } catch (Exception e) { e.printStackTrace(); }

        checkAndSeedCategories(allList);
        List<Category> filteredList = new ArrayList<>();
        for (Category c : allList) { if (c.getType() == selectedType) filteredList.add(c); }

        int totalItems = filteredList.size();
        int totalPages = (int) Math.ceil((double) totalItems / ITEMS_PER_PAGE);
        if (totalPages == 0) totalPages = 1;

        if (currentPage > totalPages) currentPage = totalPages;
        if (currentPage < 1) currentPage = 1;

        lblPageIndicator.setText((isVietnamese ? "Trang " : "Page ") + currentPage + " / " + totalPages);
        btnPrevPage.setEnabled(currentPage > 1);
        btnNextPage.setEnabled(currentPage < totalPages);

        int startIndex = (currentPage - 1) * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, totalItems);

        int displayedCount = 0;
        for (int i = startIndex; i < endIndex; i++) {
            categoryPanel.add(createCategoryItem(filteredList.get(i)));
            displayedCount++;
        }

        int itemsOnPage = endIndex - startIndex;
        int targetCount = (itemsOnPage <= 4) ? 4 : 8;
        int rows = (int) Math.ceil((double) targetCount / 4);

        for (int i = displayedCount; i < targetCount; i++) {
            JPanel placeholder = new JPanel(); placeholder.setOpaque(false); categoryPanel.add(placeholder);
        }

        int calculatedHeight = rows * 78 + (rows - 1) * 12 + 6;
        categoryScrollPane.setPreferredSize(new Dimension(400, calculatedHeight));
        categoryScrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, calculatedHeight));
        categoryScrollPane.setMinimumSize(new Dimension(10, calculatedHeight));

        categoryPanel.revalidate(); categoryPanel.repaint();
    }

    private JPanel createCategoryItem(Category c) {
        JPanel item = new JPanel(new BorderLayout(0, 4));
        item.setBackground(BG_COLOR);
        item.setPreferredSize(new Dimension(85, 78));
        item.setMinimumSize(new Dimension(85, 78));
        item.setMaximumSize(new Dimension(85, 78));
        item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        String emoji = customEmojiMap.containsKey(c.getName()) ? customEmojiMap.get(c.getName()) : CATEGORY_EMOJI.getOrDefault(c.getName(), "\uD83D\uDCCD");

        JLabel lblIcon = new JLabel(emoji, SwingConstants.CENTER);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
        lblIcon.setOpaque(true);

        if (selectedCategory != null && selectedCategory.getId().equals(c.getId())) {
            lblIcon.setBackground(ACCENT_YELLOW);
            lblIcon.setForeground(BG_COLOR);
        } else {
            lblIcon.setBackground(SURFACE_COLOR);
            lblIcon.setForeground(Color.WHITE);
        }

        lblIcon.setPreferredSize(new Dimension(48, 48));
        lblIcon.setBorder(BorderFactory.createLineBorder(new Color(50, 50, 50), 1, true));

        JLabel lblName = new JLabel(c.getName(), SwingConstants.CENTER);
        lblName.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblName.setForeground(TEXT_PRIMARY);

        item.add(lblIcon, BorderLayout.CENTER);
        item.add(lblName, BorderLayout.SOUTH);

        item.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                selectedCategory = c;
                for (Component comp : categoryPanel.getComponents()) {
                    if (comp instanceof JPanel && comp.getMinimumSize().width == 85) {
                        JPanel p = (JPanel) comp;
                        if (p.getComponentCount() > 0 && p.getComponent(0) instanceof JLabel) {
                            JLabel icon = (JLabel) p.getComponent(0);
                            icon.setBackground(SURFACE_COLOR);
                            icon.setForeground(Color.WHITE);
                        }
                    }
                }
                lblIcon.setBackground(ACCENT_YELLOW);
                lblIcon.setForeground(BG_COLOR);
            }
        });
        return item;
    }

    // =====================================================================
    // 4. LƯU GIAO DỊCH
    // =====================================================================
    private void saveTransaction() {
        try {
            String userId = SessionManager.getCurrentUserId();
            if (userId == null) {
                JOptionPane.showMessageDialog(this, isVietnamese ? "Bạn chưa đăng nhập hệ thống!" : "You are not logged in!");
                return;
            }

            double amount = InputValidator.validateAmount(txtAmount.getText(), isVietnamese);

            if (selectedCategory == null) {
                JOptionPane.showMessageDialog(this, isVietnamese ? "Vui lòng chọn một danh mục giao dịch!" : "Please select a category!",
                        isVietnamese ? "Cảnh báo" : "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String note = InputValidator.validateTransactionNote(txtNote.getText(), isVietnamese);
            String id = UUID.randomUUID().toString().substring(0, 8);
            Transaction t = new Transaction(id, amount, selectedType, selectedCategory, note);

            if (mainFrame != null && mainFrame.getFinanceService() != null) {
                mainFrame.getFinanceService().addTransaction(t);
            } else {
                DatabaseUtil.insertTransaction(t, userId);
            }

            JOptionPane.showMessageDialog(this, isVietnamese ? "Thêm giao dịch thành công!" : "Transaction saved successfully!",
                    isVietnamese ? "Thành công" : "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), isVietnamese ? "Lỗi nhập liệu" : "Validation Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    // =====================================================================
    // 5. TIỆN ÍCH GIAO DIỆN (UI STYLES)
    // =====================================================================
    public static void addCustomEmoji(String categoryName, String emoji) {
        customEmojiMap.put(categoryName, emoji);
    }

    private JButton createTypeButton(String text, boolean active) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setFocusPainted(false);
        b.setBackground(active ? (text.contains("CHI") || text.contains("EXP") ? new Color(244, 67, 54) : new Color(76, 175, 80)) : SURFACE_COLOR);
        b.setForeground(Color.WHITE);
        b.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        return b;
    }

    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setForeground(new Color(150, 150, 150));
        l.setFont(new Font("Segoe UI", Font.BOLD, 13));
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        return l;
    }

    private JButton createArrowButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(TEXT_PRIMARY);
        btn.setBackground(SURFACE_COLOR);
        btn.setBorder(BorderFactory.createLineBorder(new Color(55, 55, 55), 1));
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(36, 28));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void styleTextField(JTextField tf, String placeholder) {
        tf.setBackground(SURFACE_COLOR);
        tf.setForeground(TEXT_PRIMARY);
        tf.setCaretColor(ACCENT_YELLOW);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        tf.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(50, 50, 50)), BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        tf.setHorizontalAlignment(JTextField.CENTER);
        tf.setAlignmentX(Component.CENTER_ALIGNMENT);
    }
}