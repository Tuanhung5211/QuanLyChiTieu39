package com.expensemanager.ui;

import com.expensemanager.database.DatabaseUtil;
import com.expensemanager.entity.*;
import com.expensemanager.service.RecurringTransactionService;
import com.expensemanager.service.ThemeManager;
import com.expensemanager.util.CategoryTranslator;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import java.util.UUID;

public class AddRecurringTransactionDialog extends JDialog {

    private MainFrame mainFrame;
    private RecurringTransactionService recurringTransactionService;
    private Category selectedCategory;
    private RecurringTransaction.RecurrenceType selectedRecurrenceType = RecurringTransaction.RecurrenceType.MONTHLY;

    private JTextField txtAmount;
    private JTextArea txtNote;
    private JPanel categoryPanel;
    private JComboBox<String> cmbRecurrenceType;
    private boolean isVietnamese = true;

    private int currentPage = 1;
    private final int ITEMS_PER_PAGE = 8;
    private JButton btnPrevPage, btnNextPage;
    private JLabel lblPageIndicator;
    private JScrollPane categoryScrollPane;

    public static Map<String, String> customEmojiMap = new HashMap<>();
    private List<Category> allCategories = new ArrayList<>();

    public AddRecurringTransactionDialog(MainFrame parent, RecurringTransactionService recurringTransactionService) {
        super(parent, parent != null && parent.isVietnamese() ? "Thêm giao dịch định kì" : "Add Scheduled Transaction", true);
        this.mainFrame = parent;
        this.recurringTransactionService = recurringTransactionService;
        if (parent != null) this.isVietnamese = parent.isVietnamese();

        setSize(460, 600);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        getContentPane().setBackground(ThemeManager.getColor("bg"));

        initComponents();
        applyTheme();
        loadCategoriesAsync();
        ThemeManager.applyThemeRecursively(this);
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
                    refreshCategoryGrid();
                    SwingUtilities.invokeLater(() -> {
                        categoryPanel.revalidate();
                        categoryPanel.repaint();
                        categoryScrollPane.revalidate();
                        categoryScrollPane.repaint();
                        revalidate();
                        repaint();
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(AddRecurringTransactionDialog.this,
                            isVietnamese ? "Không thể tải danh mục!" : "Cannot load categories!",
                            isVietnamese ? "Lỗi" : "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void checkAndSeedCategories(List<Category> currentList) {
        if (currentList == null) currentList = new ArrayList<>();
        Map<String, String> emojiMap = com.expensemanager.util.EmojiUtil.CATEGORY_EMOJI;
        for (Map.Entry<String, String> entry : emojiMap.entrySet()) {
            String name = entry.getKey();
            boolean existed = false;
            for (Category existing : currentList) {
                if (existing.getName().trim().equalsIgnoreCase(name.trim())) {
                    existed = true;
                    break;
                }
            }
            if (!existed) {
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

    private void initComponents() {
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 5, 20));
        centerPanel.setOpaque(false);

        // ---- Số tiền ----
        centerPanel.add(createLabel(isVietnamese ? "Số tiền (VND)" : "Amount (VND)"));
        txtAmount = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        txtAmount.setOpaque(false);
        styleTextField(txtAmount);
        txtAmount.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        txtAmount.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(txtAmount);
        centerPanel.add(Box.createVerticalStrut(10));

        // ---- Danh mục ----
        centerPanel.add(createLabel(isVietnamese ? "Chọn danh mục" : "Select Category"));
        centerPanel.add(Box.createVerticalStrut(4));
        categoryPanel = new JPanel(new GridLayout(0, 4, 8, 8));
        categoryScrollPane = new JScrollPane(categoryPanel);
        categoryScrollPane.setBorder(null);
        categoryScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        categoryScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        categoryScrollPane.setAlignmentX(Component.CENTER_ALIGNMENT);
        categoryScrollPane.setPreferredSize(new Dimension(400, 160));
        centerPanel.add(categoryScrollPane);

        // Phân trang
        JPanel paginationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        paginationPanel.setOpaque(false);
        btnPrevPage = createArrowButton("<");
        btnNextPage = createArrowButton(">");
        lblPageIndicator = new JLabel(isVietnamese ? "Trang 1 / 1" : "Page 1 / 1");
        lblPageIndicator.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblPageIndicator.setForeground(ThemeManager.getColor("textPrimary"));
        btnPrevPage.addActionListener(e -> { if (currentPage > 1) { currentPage--; refreshCategoryGrid(); } });
        btnNextPage.addActionListener(e -> { currentPage++; refreshCategoryGrid(); });
        paginationPanel.add(btnPrevPage);
        paginationPanel.add(lblPageIndicator);
        paginationPanel.add(btnNextPage);
        centerPanel.add(paginationPanel);
        centerPanel.add(Box.createVerticalStrut(10));

        // ---- Chu kì ----
        JPanel recurRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        recurRow.setOpaque(false);
        recurRow.add(new JLabel(isVietnamese ? "Chu kì:" : "Recurrence:"));
        String[] types = isVietnamese ?
                new String[]{"Hàng ngày", "Hàng tuần", "Hàng tháng", "Hàng năm"} :
                new String[]{"Daily", "Weekly", "Monthly", "Yearly"};
        cmbRecurrenceType = new JComboBox<>(types);
        cmbRecurrenceType.setSelectedIndex(2);
        cmbRecurrenceType.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cmbRecurrenceType.addActionListener(e -> {
            selectedRecurrenceType = RecurringTransaction.RecurrenceType.values()[cmbRecurrenceType.getSelectedIndex()];
        });
        recurRow.add(cmbRecurrenceType);
        centerPanel.add(recurRow);
        centerPanel.add(Box.createVerticalStrut(10));

        // ---- Ghi chú ----
        centerPanel.add(createLabel(isVietnamese ? "Ghi chú" : "Transaction Note"));
        centerPanel.add(Box.createVerticalStrut(4));
        txtNote = new JTextArea(3, 20);
        txtNote.setLineWrap(true);
        txtNote.setWrapStyleWord(true);
        txtNote.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        txtNote.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        JScrollPane scrollNote = new JScrollPane(txtNote);
        scrollNote.setBorder(new javax.swing.border.LineBorder(ThemeManager.getColor("border"), 1, true));
        scrollNote.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        scrollNote.setMaximumSize(new Dimension(Integer.MAX_VALUE, 75));
        scrollNote.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(scrollNote);

        add(centerPanel, BorderLayout.CENTER);

        // Footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createEmptyBorder(5, 20, 15, 20));

        JButton btnCancel = new JButton(isVietnamese ? "HỦY BỎ" : "CANCEL");
        btnCancel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCancel.setFocusPainted(false);
        btnCancel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btnCancel.addActionListener(e -> dispose());

        JButton btnSave = new JButton(isVietnamese ? "LƯU GIAO DỊCH" : "SAVE TRANSACTION");
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSave.setFocusPainted(false);
        btnSave.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btnSave.addActionListener(e -> saveRecurringTransaction());

        footer.add(btnCancel);
        footer.add(btnSave);
        add(footer, BorderLayout.SOUTH);
    }

    private void refreshCategoryGrid() {
        categoryPanel.removeAll();
        List<Category> filteredList = new ArrayList<>();
        for (Category c : allCategories) {
            if (c.getType() == TransactionType.EXPENSE) filteredList.add(c);
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
            categoryPanel.add(new JPanel() {{ setOpaque(false); }});
        }

        int rows = displayedCount <= 4 ? 1 : 2;
        int calculatedHeight = rows * 74 + (rows - 1) * 8 + 6;
        categoryScrollPane.setPreferredSize(new Dimension(400, calculatedHeight));

        categoryPanel.revalidate();
        categoryPanel.repaint();
        categoryScrollPane.revalidate();
        categoryScrollPane.repaint();
    }

    private JPanel createCategoryItem(Category c) {
        JPanel item = new JPanel(new BorderLayout(0, 4));
        item.setOpaque(false);
        item.setPreferredSize(new Dimension(85, 74));
        item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        String emoji = customEmojiMap.containsKey(c.getName())
                ? customEmojiMap.get(c.getName())
                : com.expensemanager.util.EmojiUtil.CATEGORY_EMOJI.getOrDefault(c.getName(), "\uD83D\uDCCD");

        JLabel lblIcon = new JLabel(emoji, SwingConstants.CENTER);
        lblIcon.setFont(com.expensemanager.util.EmojiUtil.getEmojiFont(22));
        lblIcon.setOpaque(true);
        lblIcon.setPreferredSize(new Dimension(48, 48));
        lblIcon.setBorder(BorderFactory.createLineBorder(ThemeManager.getColor("border"), 1, true));

        if (selectedCategory != null && selectedCategory.getId().equals(c.getId())) {
            lblIcon.setBackground(ThemeManager.getColor("accent"));
            lblIcon.setForeground(ThemeManager.getContrastColor(ThemeManager.getColor("accent")));
        } else {
            lblIcon.setBackground(ThemeManager.getColor("input"));
            lblIcon.setForeground(ThemeManager.getColor("textPrimary"));
        }

        // Dịch tên danh mục
        String displayName = CategoryTranslator.translate(c.getName(), isVietnamese);
        JLabel lblName = new JLabel(displayName, SwingConstants.CENTER);
        lblName.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblName.setForeground(ThemeManager.getColor("textPrimary"));

        item.add(lblIcon, BorderLayout.CENTER);
        item.add(lblName, BorderLayout.SOUTH);
        item.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                selectedCategory = c;
                refreshCategoryGrid();
            }
        });
        return item;
    }

    private void saveRecurringTransaction() {
        if (selectedCategory == null) {
            JOptionPane.showMessageDialog(this,
                    isVietnamese ? "Vui lòng chọn danh mục!" : "Please select a category!",
                    isVietnamese ? "Cảnh báo" : "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String amountStr = txtAmount.getText().trim();
        if (amountStr.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    isVietnamese ? "Vui lòng nhập số tiền!" : "Please enter amount!",
                    isVietnamese ? "Cảnh báo" : "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            double amount = Double.parseDouble(amountStr);
            if (amount <= 0) throw new NumberFormatException();

            String id = UUID.randomUUID().toString().substring(0, 10);
            RecurringTransaction rt = new RecurringTransaction();
            rt.setId(id);
            rt.setAmount(amount);
            rt.setType(TransactionType.EXPENSE);
            rt.setCategory(selectedCategory);
            rt.setNote(txtNote.getText().trim());
            rt.setRecurrenceType(selectedRecurrenceType);
            rt.setCustomIntervalDays(0);
            rt.setStartDate(LocalDate.now());
            rt.setEndDate(null);
            rt.setActive(true);

            recurringTransactionService.addRecurringTransaction(rt);

            JOptionPane.showMessageDialog(this,
                    isVietnamese ? "Giao dịch định kì đã được lưu!" : "Scheduled transaction saved!",
                    isVietnamese ? "Thông báo" : "Info", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    isVietnamese ? "Vui lòng nhập số tiền hợp lệ!" : "Please enter valid amount!",
                    isVietnamese ? "Lỗi" : "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(ThemeManager.getColor("textSecondary"));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
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

    private void styleTextField(JTextField tf) {
        tf.setBackground(ThemeManager.getColor("input"));
        tf.setForeground(ThemeManager.getColor("textPrimary"));
        tf.setCaretColor(ThemeManager.getColor("accent"));
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        tf.setBorder(BorderFactory.createCompoundBorder(
                new javax.swing.border.LineBorder(ThemeManager.getColor("border"), 1, true),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        tf.setHorizontalAlignment(JTextField.CENTER);
    }

    private void applyTheme() {
        getContentPane().setBackground(ThemeManager.getColor("bg"));
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
        if (cmbRecurrenceType != null) {
            cmbRecurrenceType.setBackground(ThemeManager.getColor("input"));
            cmbRecurrenceType.setForeground(ThemeManager.getColor("textPrimary"));
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
    }
}