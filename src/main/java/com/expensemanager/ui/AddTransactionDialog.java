package com.expensemanager.ui;

import com.expensemanager.database.DatabaseUtil;
import com.expensemanager.entity.Category;
import com.expensemanager.entity.Transaction;
import com.expensemanager.entity.TransactionType;
import com.expensemanager.service.SessionManager;
import com.expensemanager.util.EmojiUtil;
import com.expensemanager.util.InputValidator;
import com.expensemanager.service.ThemeManager;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseEvent;
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
    private boolean isVietnamese = true;

    public static Map<String, String> customEmojiMap = new HashMap<>();

    private List<Category> allCategories = new ArrayList<>();

    public AddTransactionDialog(MainFrame parent) {
        super(parent, parent != null && parent.isVietnamese() ? "Thêm giao dịch mới" : "Add New Transaction", true);
        this.mainFrame = parent;
        if (parent != null) {
            this.isVietnamese = parent.isVietnamese();
        }

        setSize(460, 580);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        initComponents();
        applyTheme();

        loadCategoriesAsync();
    }

    private void loadCategoriesAsync() {
        categoryPanel.removeAll();
        JLabel loadingLabel = new JLabel(isVietnamese ? "Đang tải danh mục..." : "Loading categories...", SwingConstants.CENTER);
        loadingLabel.setForeground(ThemeManager.getColor("textSecondary"));
        categoryPanel.add(loadingLabel);
        categoryPanel.revalidate();
        categoryPanel.repaint();

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                List<Category> cats = DatabaseUtil.getAllCategories();
                checkAndSeedCategories(cats);
                allCategories = cats;
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    refreshCategories();
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(AddTransactionDialog.this,
                            isVietnamese ? "Không thể tải danh mục!" : "Cannot load categories!",
                            isVietnamese ? "Lỗi" : "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void initComponents() {
        JPanel header = new JPanel(new GridLayout(1, 2, 10, 0));
        header.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        header.setOpaque(false);

        btnExpense = createTypeButton(isVietnamese ? "CHI TIÊU" : "EXPENSE", true);
        btnIncome = createTypeButton(isVietnamese ? "THU NHẬP" : "INCOME", false);

        btnExpense.addActionListener(e -> switchType(TransactionType.EXPENSE));
        btnIncome.addActionListener(e -> switchType(TransactionType.INCOME));

        header.add(btnExpense);
        header.add(btnIncome);
        add(header, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 5, 20));
        centerPanel.setOpaque(false);

        centerPanel.add(createLabel(isVietnamese ? "Số tiền (VND)" : "Amount (VND)"));
        txtAmount = new JTextField();
        styleTextField(txtAmount, "0");
        txtAmount.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        txtAmount.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(txtAmount);

        centerPanel.add(Box.createVerticalStrut(12));
        centerPanel.add(createLabel(isVietnamese ? "Chọn danh mục" : "Select Category"));
        centerPanel.add(Box.createVerticalStrut(4));

        categoryPanel = new JPanel(new GridLayout(0, 4, 12, 12));
        categoryScrollPane = new JScrollPane(categoryPanel);
        categoryScrollPane.setBorder(null);
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
        lblPageIndicator.setForeground(ThemeManager.getColor("textPrimary"));

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
        txtNote.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        txtNote.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        txtNote.getDocument().addDocumentListener(new DocumentListener() {
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
            public void insertUpdate(DocumentEvent e) { check(); }
            public void removeUpdate(DocumentEvent e) { check(); }
            public void changedUpdate(DocumentEvent e) { check(); }
        });

        JScrollPane scrollNote = new JScrollPane(txtNote);
        scrollNote.setBorder(BorderFactory.createLineBorder(ThemeManager.getColor("border"), 1));
        scrollNote.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));
        scrollNote.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(scrollNote);

        centerPanel.add(Box.createVerticalGlue());
        add(centerPanel, BorderLayout.CENTER);

        JPanel footer = new JPanel(new GridLayout(1, 2, 10, 0));
        footer.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        footer.setOpaque(false);

        JButton btnCancel = new JButton(isVietnamese ? "HỦY BỎ" : "CANCEL");
        btnCancel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnCancel.setFocusPainted(false);
        btnCancel.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));
        btnCancel.addActionListener(e -> dispose());

        JButton btnSave = new JButton(isVietnamese ? "LƯU GIAO DỊCH" : "SAVE TRANSACTION");
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnSave.setFocusPainted(false);
        btnSave.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));
        btnSave.addActionListener(e -> saveTransaction());

        footer.add(btnCancel);
        footer.add(btnSave);
        add(footer, BorderLayout.SOUTH);
    }

    public void applyTheme() {
        getContentPane().setBackground(ThemeManager.getColor("bg"));
        if (btnExpense != null) {
            btnExpense.setBackground(selectedType == TransactionType.EXPENSE ? ThemeManager.getColor("danger") : ThemeManager.getColor("surface"));
            btnExpense.setForeground(ThemeManager.getColor("bg"));
        }
        if (btnIncome != null) {
            btnIncome.setBackground(selectedType == TransactionType.INCOME ? ThemeManager.getColor("success") : ThemeManager.getColor("surface"));
            btnIncome.setForeground(ThemeManager.getColor("bg"));
        }
        if (txtAmount != null) {
            txtAmount.setBackground(ThemeManager.getColor("input"));
            txtAmount.setForeground(ThemeManager.getColor("textPrimary"));
            txtAmount.setCaretColor(ThemeManager.getColor("accent"));
        }
        if (txtNote != null) {
            txtNote.setBackground(ThemeManager.getColor("input"));
            txtNote.setForeground(ThemeManager.getColor("textPrimary"));
            txtNote.setCaretColor(ThemeManager.getColor("accent"));
        }
        if (categoryPanel != null) categoryPanel.setBackground(ThemeManager.getColor("bg"));
        if (categoryScrollPane != null) categoryScrollPane.getViewport().setBackground(ThemeManager.getColor("bg"));
        if (btnPrevPage != null) {
            btnPrevPage.setBackground(ThemeManager.getColor("surface"));
            btnPrevPage.setForeground(ThemeManager.getColor("textPrimary"));
        }
        if (btnNextPage != null) {
            btnNextPage.setBackground(ThemeManager.getColor("surface"));
            btnNextPage.setForeground(ThemeManager.getColor("textPrimary"));
        }
        if (lblPageIndicator != null) lblPageIndicator.setForeground(ThemeManager.getColor("textPrimary"));
        for (Component comp : getContentPane().getComponents()) {
            if (comp instanceof JPanel) {
                JPanel panel = (JPanel) comp;
                if (panel.getComponentCount() == 2 && panel.getComponent(0) instanceof JButton && panel.getComponent(1) instanceof JButton) {
                    for (Component btnComp : panel.getComponents()) {
                        if (btnComp instanceof JButton) {
                            JButton btn = (JButton) btnComp;
                            if (btn.getText().contains("HỦY") || btn.getText().contains("CANCEL")) {
                                btn.setBackground(ThemeManager.getColor("surface"));
                                btn.setForeground(ThemeManager.getColor("textPrimary"));
                            } else {
                                btn.setBackground(ThemeManager.getColor("accent"));
                                btn.setForeground(ThemeManager.getColor("textPrimary"));
                            }
                        }
                    }
                }
            }
        }
        if (!allCategories.isEmpty()) {
            refreshCategories();
        }
    }

    private void switchType(TransactionType type) {
        this.selectedType = type;
        this.currentPage = 1;
        this.selectedCategory = null;
        applyTheme();
        refreshCategories();
    }

    private void refreshCategories() {
        categoryPanel.removeAll();
        List<Category> filteredList = new ArrayList<>();
        for (Category c : allCategories) {
            if (c.getType() == selectedType) filteredList.add(c);
        }

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

        for (int i = startIndex; i < endIndex; i++) {
            categoryPanel.add(createCategoryItem(filteredList.get(i)));
        }
        int displayedCount = endIndex - startIndex;
        for (int i = displayedCount; i < ITEMS_PER_PAGE; i++) {
            JPanel placeholder = new JPanel(); placeholder.setOpaque(false); categoryPanel.add(placeholder);
        }

        int rows = displayedCount <= 4 ? 1 : 2;
        int calculatedHeight = rows * 78 + (rows - 1) * 12 + 6;
        categoryScrollPane.setPreferredSize(new Dimension(400, calculatedHeight));
        categoryScrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, calculatedHeight));

        categoryPanel.revalidate(); categoryPanel.repaint();
    }

    private void checkAndSeedCategories(List<Category> currentList) {
        if (currentList == null) currentList = new ArrayList<>();
        for (Map.Entry<String, String> entry : EmojiUtil.CATEGORY_EMOJI.entrySet()) {
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

    private JPanel createCategoryItem(Category c) {
        JPanel item = new JPanel(new BorderLayout(0, 4));
        item.setOpaque(false);
        item.setPreferredSize(new Dimension(85, 78));
        item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        String emoji = customEmojiMap.containsKey(c.getName())
                ? customEmojiMap.get(c.getName())
                : EmojiUtil.CATEGORY_EMOJI.getOrDefault(c.getName(), "\uD83D\uDCCD");

        JLabel lblIcon = new JLabel(emoji, SwingConstants.CENTER);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
        lblIcon.setOpaque(true);
        lblIcon.setPreferredSize(new Dimension(48, 48));
        lblIcon.setBorder(BorderFactory.createLineBorder(ThemeManager.getColor("border"), 1, true));

        if (selectedCategory != null && selectedCategory.getId().equals(c.getId())) {
            lblIcon.setBackground(ThemeManager.getColor("accent"));
            lblIcon.setForeground(ThemeManager.getColor("bg"));
        } else {
            lblIcon.setBackground(ThemeManager.getColor("input"));
            lblIcon.setForeground(ThemeManager.getColor("bg"));
        }

        JLabel lblName = new JLabel(c.getName(), SwingConstants.CENTER);
        lblName.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblName.setForeground(ThemeManager.getColor("textPrimary"));

        item.add(lblIcon, BorderLayout.CENTER);
        item.add(lblName, BorderLayout.SOUTH);

        item.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                selectedCategory = c;
                refreshCategories();
            }
        });
        return item;
    }

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

    public static void addCustomEmoji(String categoryName, String emoji) {
        customEmojiMap.put(categoryName, emoji);
    }

    private JButton createTypeButton(String text, boolean active) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setFocusPainted(false);
        b.setForeground(ThemeManager.getColor("bg"));
        b.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        return b;
    }

    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setForeground(ThemeManager.getColor("textSecondary"));
        l.setFont(new Font("Segoe UI", Font.BOLD, 13));
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        return l;
    }

    private JButton createArrowButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBorder(BorderFactory.createLineBorder(ThemeManager.getColor("border"), 1));
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(36, 28));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void styleTextField(JTextField tf, String placeholder) {
        tf.setBackground(ThemeManager.getColor("input"));
        tf.setForeground(ThemeManager.getColor("textPrimary"));
        tf.setCaretColor(ThemeManager.getColor("accent"));
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.getColor("border")),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        tf.setHorizontalAlignment(JTextField.CENTER);
        tf.setAlignmentX(Component.CENTER_ALIGNMENT);
    }
}