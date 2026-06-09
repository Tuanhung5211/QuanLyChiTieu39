package com.expensemanager.ui;

import com.expensemanager.database.DatabaseUtil;
import com.expensemanager.entity.*;
import com.expensemanager.service.RecurringTransactionService;
import com.expensemanager.util.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import java.util.UUID;

public class AddRecurringTransactionDialog extends JDialog {

    private MainFrame mainFrame;
    private RecurringTransactionService recurringTransactionService;
    private TransactionType selectedType = TransactionType.EXPENSE;
    private Category selectedCategory;
    private RecurringTransaction.RecurrenceType selectedRecurrenceType = RecurringTransaction.RecurrenceType.MONTHLY;

    private JTextField txtAmount;
    private JTextArea txtNote;
    private JPanel categoryPanel;
    private JButton btnExpense, btnIncome;
    private JComboBox<String> cmbRecurrenceType;
    private JSpinner spnCustomDays;
    private JSpinner spnStartDate, spnEndDate;
    private JCheckBox chkEndDate;
    private boolean isVietnamese = true;

    // Cache toàn bộ danh mục sau lần tải đầu tiên
    private List<Category> allCategories = new ArrayList<>();

    public AddRecurringTransactionDialog(MainFrame parent, RecurringTransactionService recurringTransactionService) {
        super(parent, parent != null && parent.isVietnamese() ? "Thêm giao dịch lặp lại" : "Add Recurring Transaction", true);
        this.mainFrame = parent;
        this.recurringTransactionService = recurringTransactionService;
        if (parent != null) {
            this.isVietnamese = parent.isVietnamese();
        }

        setSize(500, 700);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        initComponents();
        applyTheme();

        // Tải danh mục bất đồng bộ
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
                allCategories = DatabaseUtil.getAllCategories();
                if (allCategories == null) allCategories = new ArrayList<>();
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    refreshCategoryGrid();
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

    private void initComponents() {
        // Header (loại giao dịch)
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

        // Center - Scrollable form
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        centerPanel.setOpaque(false);

        // Số tiền
        centerPanel.add(createLabel(isVietnamese ? "Số tiền (VND)" : "Amount (VND)"));
        txtAmount = new JTextField();
        styleTextField(txtAmount);
        txtAmount.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        centerPanel.add(txtAmount);

        centerPanel.add(Box.createVerticalStrut(12));

        // Chọn danh mục
        centerPanel.add(createLabel(isVietnamese ? "Chọn danh mục" : "Select Category"));
        centerPanel.add(Box.createVerticalStrut(4));
        categoryPanel = new JPanel(new GridLayout(0, 4, 8, 8));
        categoryPanel.setOpaque(false);
        JScrollPane catScroll = new JScrollPane(categoryPanel);
        catScroll.setBorder(null);
        catScroll.setOpaque(false);
        catScroll.getViewport().setOpaque(false);
        catScroll.setPreferredSize(new Dimension(460, 120));
        catScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        centerPanel.add(catScroll);
        centerPanel.add(Box.createVerticalStrut(12));

        // Loại lặp lại
        centerPanel.add(createLabel(isVietnamese ? "Loại lặp lại" : "Recurrence Type"));
        String[] recurrenceTypes = isVietnamese ?
                new String[]{"Hàng ngày", "Hàng tuần", "Hàng tháng", "Hàng năm", "Tùy chỉnh"} :
                new String[]{"Daily", "Weekly", "Monthly", "Yearly", "Custom"};
        cmbRecurrenceType = new JComboBox<>(recurrenceTypes);
        cmbRecurrenceType.setSelectedIndex(2); // Mặc định hàng tháng
        cmbRecurrenceType.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        cmbRecurrenceType.addActionListener(e -> updateRecurrenceUI());
        centerPanel.add(cmbRecurrenceType);
        centerPanel.add(Box.createVerticalStrut(8));

        // Khoảng cách ngày (chỉ hiện khi chọn Tùy chỉnh)
        centerPanel.add(createLabel(isVietnamese ? "Khoảng cách (ngày)" : "Interval (days)"));
        spnCustomDays = new JSpinner(new SpinnerNumberModel(1, 1, 365, 1));
        spnCustomDays.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        spnCustomDays.setVisible(false);
        centerPanel.add(spnCustomDays);
        centerPanel.add(Box.createVerticalStrut(12));

        // Ngày bắt đầu
        centerPanel.add(createLabel(isVietnamese ? "Ngày bắt đầu" : "Start Date"));
        spnStartDate = new JSpinner(new SpinnerDateModel(new java.util.Date(), null, null, java.util.Calendar.DAY_OF_MONTH));
        spnStartDate.setEditor(new JSpinner.DateEditor(spnStartDate, "dd/MM/yyyy"));
        spnStartDate.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        centerPanel.add(spnStartDate);
        centerPanel.add(Box.createVerticalStrut(8));

        // Ngày kết thúc
        chkEndDate = new JCheckBox(isVietnamese ? "Đặt ngày kết thúc" : "Set End Date");
        chkEndDate.setOpaque(false);
        chkEndDate.addActionListener(e -> spnEndDate.setEnabled(chkEndDate.isSelected()));
        centerPanel.add(chkEndDate);

        spnEndDate = new JSpinner(new SpinnerDateModel(new java.util.Date(), null, null, java.util.Calendar.DAY_OF_MONTH));
        spnEndDate.setEditor(new JSpinner.DateEditor(spnEndDate, "dd/MM/yyyy"));
        spnEndDate.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        spnEndDate.setEnabled(false);
        centerPanel.add(spnEndDate);
        centerPanel.add(Box.createVerticalStrut(12));

        // Ghi chú
        centerPanel.add(createLabel(isVietnamese ? "Ghi chú" : "Transaction Note"));
        txtNote = new JTextArea(2, 20);
        txtNote.setLineWrap(true);
        txtNote.setWrapStyleWord(true);
        txtNote.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtNote.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        JScrollPane scrollNote = new JScrollPane(txtNote);
        scrollNote.setBorder(BorderFactory.createLineBorder(ThemeManager.getColor("border"), 1));
        scrollNote.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        centerPanel.add(scrollNote);

        centerPanel.add(Box.createVerticalGlue());

        scrollPane.setViewportView(centerPanel);
        add(scrollPane, BorderLayout.CENTER);

        // Footer (nút)
        JPanel footer = new JPanel(new GridLayout(1, 2, 10, 0));
        footer.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        footer.setOpaque(false);

        JButton btnCancel = new JButton(isVietnamese ? "HỦY BỎ" : "CANCEL");
        btnCancel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnCancel.setFocusPainted(false);
        btnCancel.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));
        btnCancel.addActionListener(e -> dispose());

        JButton btnSave = new JButton(isVietnamese ? "LƯU GIAO DỊCH LẶP LẠI" : "SAVE RECURRING");
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnSave.setFocusPainted(false);
        btnSave.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));
        btnSave.addActionListener(e -> saveRecurringTransaction());

        footer.add(btnCancel);
        footer.add(btnSave);
        add(footer, BorderLayout.SOUTH);
    }

    private void refreshCategoryGrid() {
        categoryPanel.removeAll();
        if (allCategories.isEmpty()) {
            categoryPanel.add(new JLabel(isVietnamese ? "Không có danh mục" : "No categories", SwingConstants.CENTER));
        } else {
            for (Category cat : allCategories) {
                if (cat.getType() == selectedType) {
                    JButton btnCategory = new JButton("[" + cat.getName() + "]");
                    btnCategory.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                    btnCategory.setFocusPainted(false);
                    btnCategory.setPreferredSize(new Dimension(80, 50));
                    btnCategory.addActionListener(e -> {
                        selectedCategory = cat;
                        refreshCategoryGrid(); // highlight
                    });
                    if (selectedCategory != null && selectedCategory.getId().equals(cat.getId())) {
                        btnCategory.setBackground(ThemeManager.getColor("accent"));
                        btnCategory.setForeground(Color.WHITE);
                    } else {
                        btnCategory.setBackground(ThemeManager.getColor("inputBg"));
                        btnCategory.setForeground(ThemeManager.getColor("textPrimary"));
                    }
                    categoryPanel.add(btnCategory);
                }
            }
        }
        categoryPanel.revalidate();
        categoryPanel.repaint();
    }

    private void switchType(TransactionType type) {
        selectedType = type;
        selectedCategory = null;
        updateTypeButtons();
        refreshCategoryGrid();
    }

    private void updateTypeButtons() {
        if (selectedType == TransactionType.EXPENSE) {
            btnExpense.setBackground(ThemeManager.getColor("accent"));
            btnExpense.setForeground(Color.WHITE);
            btnIncome.setBackground(ThemeManager.getColor("cardBg"));
            btnIncome.setForeground(ThemeManager.getColor("textPrimary"));
        } else {
            btnIncome.setBackground(ThemeManager.getColor("accent"));
            btnIncome.setForeground(Color.WHITE);
            btnExpense.setBackground(ThemeManager.getColor("cardBg"));
            btnExpense.setForeground(ThemeManager.getColor("textPrimary"));
        }
    }

    private void updateRecurrenceUI() {
        int idx = cmbRecurrenceType.getSelectedIndex();
        RecurringTransaction.RecurrenceType[] types = RecurringTransaction.RecurrenceType.values();
        selectedRecurrenceType = types[idx];
        spnCustomDays.setVisible(selectedRecurrenceType == RecurringTransaction.RecurrenceType.CUSTOM);
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

            LocalDate startDate = ((java.util.Date) spnStartDate.getValue()).toInstant()
                    .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            LocalDate endDate = null;

            if (chkEndDate.isSelected()) {
                endDate = ((java.util.Date) spnEndDate.getValue()).toInstant()
                        .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                if (endDate.isBefore(startDate)) {
                    JOptionPane.showMessageDialog(this,
                            isVietnamese ? "Ngày kết thúc phải sau ngày bắt đầu!" : "End date must be after start date!",
                            isVietnamese ? "Cảnh báo" : "Warning", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }

            String id = UUID.randomUUID().toString().substring(0, 10);
            RecurringTransaction rt = new RecurringTransaction();
            rt.setId(id);
            rt.setAmount(amount);
            rt.setType(selectedType);
            rt.setCategory(selectedCategory);
            rt.setNote(txtNote.getText().trim());
            rt.setRecurrenceType(selectedRecurrenceType);
            rt.setCustomIntervalDays(selectedRecurrenceType == RecurringTransaction.RecurrenceType.CUSTOM ?
                    (int) spnCustomDays.getValue() : 0);
            rt.setStartDate(startDate);
            rt.setEndDate(endDate);
            rt.setActive(true);

            recurringTransactionService.addRecurringTransaction(rt);

            JOptionPane.showMessageDialog(this,
                    isVietnamese ? "Giao dịch lặp lại đã được lưu!" : "Recurring transaction saved!",
                    isVietnamese ? "Thông báo" : "Info", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    isVietnamese ? "Vui lòng nhập số tiền hợp lệ!" : "Please enter valid amount!",
                    isVietnamese ? "Lỗi" : "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(ThemeManager.getColor("textPrimary"));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JButton createTypeButton(String text, boolean isExpense) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        btn.setOpaque(true);
        if (isExpense) {
            btn.setBackground(ThemeManager.getColor("accent"));
            btn.setForeground(Color.WHITE);
        } else {
            btn.setBackground(ThemeManager.getColor("cardBg"));
            btn.setForeground(ThemeManager.getColor("textPrimary"));
        }
        return btn;
    }

    private void styleTextField(JTextField txt) {
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        txt.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        txt.setBackground(ThemeManager.getColor("inputBg"));
        txt.setForeground(ThemeManager.getColor("textPrimary"));
        txt.setCaretColor(ThemeManager.getColor("textPrimary"));
    }

    private void applyTheme() {
        setBackground(ThemeManager.getColor("bg"));
        setForeground(ThemeManager.getColor("textPrimary"));
        for (Component comp : getContentPane().getComponents()) {
            applyThemeRecursive(comp);
        }
    }

    private void applyThemeRecursive(Component comp) {
        comp.setBackground(ThemeManager.getColor("bg"));
        comp.setForeground(ThemeManager.getColor("textPrimary"));
        if (comp instanceof Container) {
            for (Component child : ((Container) comp).getComponents()) {
                applyThemeRecursive(child);
            }
        }
    }
}