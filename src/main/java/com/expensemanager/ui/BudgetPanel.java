package com.expensemanager.ui;

import com.expensemanager.entity.Budget;
import com.expensemanager.entity.Category;
import com.expensemanager.entity.TransactionType;
import com.expensemanager.exception.InvalidAmountException;
import com.expensemanager.observer.EventType;
import com.expensemanager.observer.Observer;
import com.expensemanager.service.BudgetManager;
import com.expensemanager.service.FinanceService;
import com.expensemanager.service.SessionManager;
import com.expensemanager.util.EmojiUtil;
import com.expensemanager.util.InputValidator;
import com.expensemanager.service.ThemeManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class BudgetPanel extends JPanel implements Observer {

    private MainFrame mainFrame;
    private BudgetManager budgetManager;
    private FinanceService financeService;
    private boolean isVietnamese = true;

    // Form components
    private JComboBox<String> cmbScope;
    private JPanel categoryGridPanel;
    private JScrollPane categoryGridScroll;
    private Category selectedCategory;
    private JComboBox<String> cmbPeriod;
    private JTextField txtAmount;
    private JSpinner spinReminderThreshold;
    private JButton btnSaveBudget;
    private JLabel lblFormTitle, lblListTitle;

    private JLabel lblScope, lblCategory, lblPeriod, lblAmount, lblReminder;

    private int categoryPage = 1;
    private final int CATEGORIES_PER_PAGE = 8;
    private JButton btnPrevCatPage, btnNextCatPage;
    private JLabel lblCatPageIndicator;
    private JPanel categoryContainer; // khai báo instance

    private JPanel budgetListContainer;
    private JScrollPane scrollPane;

    public BudgetPanel(MainFrame mainFrame, BudgetManager budgetManager) {
        this.mainFrame = mainFrame;
        this.budgetManager = budgetManager;

        if (mainFrame != null) {
            this.financeService = mainFrame.getFinanceService();
            this.isVietnamese = mainFrame.isVietnamese();
        }

        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(25, 30, 25, 30));

        initComponents();
        applyTheme();
        refreshData();
    }

    private void initComponents() {
        JPanel splitContainer = new JPanel(new BorderLayout(30, 0));
        splitContainer.setOpaque(false);

        // --- LEFT COLUMN ---
        JPanel leftColumn = new JPanel(new BorderLayout(0, 15));
        leftColumn.setOpaque(false);
        leftColumn.setPreferredSize(new Dimension(380, 0)); // tăng nhẹ để chứa lưới

        lblFormTitle = new JLabel(isVietnamese ? "THIẾT LẬP HẠN MỨC" : "BUDGET CONFIG");
        lblFormTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        leftColumn.add(lblFormTitle, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.getColor("border"), 1, true),
                BorderFactory.createEmptyBorder(20, 20, 25, 20)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;

        addFormFields(formPanel, gbc);

        // Nạp danh sách danh mục (sẽ ẩn, nhưng có dữ liệu)
        refreshCategoryGrid();

        leftColumn.add(formPanel, BorderLayout.CENTER);
        splitContainer.add(leftColumn, BorderLayout.WEST);

        // --- RIGHT COLUMN ---
        JPanel rightColumn = new JPanel(new BorderLayout(0, 15));
        rightColumn.setOpaque(false);

        lblListTitle = new JLabel(isVietnamese ? "TIẾN TRÌNH HẠN MỨC HIỆN TẠI" : "CURRENT BUDGET PROGRESS");
        lblListTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        rightColumn.add(lblListTitle, BorderLayout.NORTH);

        budgetListContainer = new JPanel();
        budgetListContainer.setLayout(new BoxLayout(budgetListContainer, BoxLayout.Y_AXIS));

        scrollPane = new JScrollPane(budgetListContainer);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);
        rightColumn.add(scrollPane, BorderLayout.CENTER);

        splitContainer.add(rightColumn, BorderLayout.CENTER);
        add(splitContainer, BorderLayout.CENTER);
    }

    private void addFormFields(JPanel panel, GridBagConstraints gbc) {
        // 1. Scope
        lblScope = new JLabel(isVietnamese ? "Phạm vi áp dụng:" : "Scope:");
        panel.add(lblScope, gbc);

        cmbScope = new JComboBox<>(isVietnamese ? new String[]{"Tổng thể", "Theo danh mục"} : new String[]{"Overall", "By Category"});
        styleComboBox(cmbScope);
        panel.add(cmbScope, gbc);

        // 2. Category label
        lblCategory = new JLabel(isVietnamese ? "Chọn danh mục:" : "Select Category:");
        panel.add(lblCategory, gbc);

        // Container cho lưới
        categoryContainer = new JPanel(new BorderLayout());
        categoryContainer.setOpaque(false);
        // Kích thước tối thiểu để GridBagLayout cấp không gian
        categoryContainer.setMinimumSize(new Dimension(340, 180));
        categoryContainer.setPreferredSize(new Dimension(340, 180));

        categoryGridPanel = new JPanel(new GridLayout(0, 4, 8, 8));
        categoryGridScroll = new JScrollPane(categoryGridPanel);
        categoryGridScroll.setBorder(null);
        categoryGridScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        categoryGridScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        categoryContainer.add(categoryGridScroll, BorderLayout.CENTER);

        JPanel pagination = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        pagination.setOpaque(false);
        btnPrevCatPage = createArrowButton("<");
        btnNextCatPage = createArrowButton(">");
        lblCatPageIndicator = new JLabel(isVietnamese ? "Trang 1 / 1" : "Page 1 / 1");
        lblCatPageIndicator.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblCatPageIndicator.setForeground(ThemeManager.getColor("textPrimary"));
        btnPrevCatPage.addActionListener(e -> { if (categoryPage > 1) { categoryPage--; refreshCategoryGrid(); } });
        btnNextCatPage.addActionListener(e -> { categoryPage++; refreshCategoryGrid(); });
        pagination.add(btnPrevCatPage);
        pagination.add(lblCatPageIndicator);
        pagination.add(btnNextCatPage);
        categoryContainer.add(pagination, BorderLayout.SOUTH);

        panel.add(categoryContainer, gbc);

        categoryContainer.setVisible(false); // ẩn ban đầu

        cmbScope.addActionListener(e -> {
            boolean byCategory = cmbScope.getSelectedIndex() == 1;
            categoryContainer.setVisible(byCategory);
            if (byCategory) {
                categoryPage = 1;
                refreshCategoryGrid();
            } else {
                selectedCategory = null;
            }
            // Cập nhật layout toàn bộ cây giao diện
            panel.revalidate();
            panel.repaint();
            SwingUtilities.getWindowAncestor(panel).revalidate();
            SwingUtilities.getWindowAncestor(panel).repaint();
        });

        // 3. Period
        lblPeriod = new JLabel(isVietnamese ? "Kỳ hạn:" : "Period:");
        panel.add(lblPeriod, gbc);

        cmbPeriod = new JComboBox<>(isVietnamese ? new String[]{"Theo Ngày", "Theo Tháng", "Theo Năm"} : new String[]{"Daily", "Monthly", "Yearly"});
        styleComboBox(cmbPeriod);
        panel.add(cmbPeriod, gbc);

        // 4. Amount
        lblAmount = new JLabel(isVietnamese ? "Số tiền (VND):" : "Amount (VND):");
        panel.add(lblAmount, gbc);

        txtAmount = new JTextField();
        styleTextField(txtAmount);
        panel.add(txtAmount, gbc);

        // 5. Threshold
        lblReminder = new JLabel(isVietnamese ? "Ngưỡng nhắc nhở (%):" : "Reminder Threshold (%):");
        panel.add(lblReminder, gbc);

        spinReminderThreshold = new JSpinner(new SpinnerNumberModel(80, 10, 100, 5));
        spinReminderThreshold.setFont(new Font("Segoe UI", Font.BOLD, 14));
        styleSpinner(spinReminderThreshold);
        panel.add(spinReminderThreshold, gbc);

        // 6. Save button
        gbc.insets = new Insets(20, 0, 5, 0);
        btnSaveBudget = new JButton(isVietnamese ? "Kích hoạt Ngân sách" : "Activate Budget");
        btnSaveBudget.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnSaveBudget.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSaveBudget.setFocusPainted(false);
        btnSaveBudget.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));
        btnSaveBudget.addActionListener(e -> saveBudgetAction());
        panel.add(btnSaveBudget, gbc);
    }

    private void refreshCategoryGrid() {
        categoryGridPanel.removeAll();
        List<Category> categories = new ArrayList<>();
        if (financeService != null) {
            for (Category c : financeService.getAllCategories()) {
                if (c.getType() == TransactionType.EXPENSE) categories.add(c);
            }
        }

        int totalItems = categories.size();
        int totalPages = (int) Math.ceil((double) totalItems / CATEGORIES_PER_PAGE);
        if (totalPages == 0) totalPages = 1;
        if (categoryPage > totalPages) categoryPage = totalPages;
        if (categoryPage < 1) categoryPage = 1;

        lblCatPageIndicator.setText((isVietnamese ? "Trang " : "Page ") + categoryPage + " / " + totalPages);
        btnPrevCatPage.setEnabled(categoryPage > 1);
        btnNextCatPage.setEnabled(categoryPage < totalPages);

        int start = (categoryPage - 1) * CATEGORIES_PER_PAGE;
        int end = Math.min(start + CATEGORIES_PER_PAGE, totalItems);
        for (int i = start; i < end; i++) {
            categoryGridPanel.add(createCategoryItem(categories.get(i)));
        }
        int displayed = end - start;
        for (int i = displayed; i < CATEGORIES_PER_PAGE; i++) {
            categoryGridPanel.add(new JPanel() {{ setOpaque(false); }});
        }

        // Điều chỉnh chiều cao scroll
        int rows = displayed <= 4 ? 1 : 2;
        int height = rows * 74 + (rows - 1) * 8 + 6;
        categoryGridScroll.setPreferredSize(new Dimension(340, height));
        categoryGridPanel.revalidate();
        categoryGridPanel.repaint();
    }

    private JPanel createCategoryItem(Category c) {
        JPanel item = new JPanel(new BorderLayout(0, 4));
        item.setOpaque(false);
        item.setPreferredSize(new Dimension(75, 74));
        item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        String emoji = EmojiUtil.CATEGORY_EMOJI.getOrDefault(c.getName(), "\uD83D\uDCCD");
        JLabel lblIcon = new JLabel(emoji, SwingConstants.CENTER);
        lblIcon.setFont(EmojiUtil.getEmojiFont(22));
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

        JLabel lblName = new JLabel(c.getName(), SwingConstants.CENTER);
        lblName.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblName.setForeground(ThemeManager.getColor("textPrimary"));

        item.add(lblIcon, BorderLayout.CENTER);
        item.add(lblName, BorderLayout.SOUTH);
        item.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectedCategory = c;
                refreshCategoryGrid();
            }
        });
        return item;
    }

    private void saveBudgetAction() {
        try {
            double amount = InputValidator.validateAmount(txtAmount.getText(), isVietnamese);

            boolean byCategory = cmbScope.getSelectedIndex() == 1;
            if (byCategory && selectedCategory == null) {
                JOptionPane.showMessageDialog(this,
                        isVietnamese ? "Vui lòng chọn một danh mục!" : "Please select a category!",
                        isVietnamese ? "Lỗi" : "Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Category selectedCat = byCategory ? selectedCategory : null;
            String period = (String) cmbPeriod.getSelectedItem();
            int threshold = (int) spinReminderThreshold.getValue();

            LocalDate now = LocalDate.now();
            LocalDate start, end;

            if (period.contains("Ngày") || period.contains("Daily")) {
                start = now; end = now;
            } else if (period.contains("Tháng") || period.contains("Monthly")) {
                start = now.withDayOfMonth(1);
                end = now.withDayOfMonth(now.lengthOfMonth());
            } else {
                start = now.withDayOfYear(1);
                end = now.withDayOfYear(now.lengthOfYear());
            }

            Budget budget = new Budget();
            budget.setId("B_" + System.currentTimeMillis());
            budget.setLimit(amount);
            budget.setCategory(selectedCat);
            budget.setStartDate(start);
            budget.setEndDate(end);
            budget.setThreshold(threshold);
            budget.setUserId(SessionManager.getCurrentUserId());

            if (budgetManager != null) {
                budgetManager.addBudget(budget);
                JOptionPane.showMessageDialog(this, isVietnamese ? "Đã lưu và triển khai mục tiêu hạn mức!" : "Budget deployed successfully!");
                txtAmount.setText("");
                selectedCategory = null;
                refreshCategoryGrid();
                refreshData();
                if (mainFrame != null) mainFrame.refreshAllPanels();
            }

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), isVietnamese ? "Lỗi nhập liệu" : "Input Error", JOptionPane.WARNING_MESSAGE);
        } catch (InvalidAmountException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), isVietnamese ? "Lỗi số tiền" : "Amount Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    public void refreshData() {
        if (budgetListContainer == null || budgetManager == null) return;

        budgetListContainer.removeAll();
        JLabel lblLoading = new JLabel(isVietnamese ? "Đang tải dữ liệu ngân sách..." : "Loading budget data...");
        lblLoading.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        lblLoading.setForeground(ThemeManager.getColor("textSecondary"));
        lblLoading.setAlignmentX(Component.CENTER_ALIGNMENT);
        budgetListContainer.add(lblLoading);
        budgetListContainer.revalidate();
        budgetListContainer.repaint();

        SwingWorker<List<Budget>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Budget> doInBackground() throws Exception {
                return budgetManager.getAllBudgets();
            }

            @Override
            protected void done() {
                try {
                    List<Budget> activeBudgets = get();
                    budgetListContainer.removeAll();

                    if (activeBudgets == null || activeBudgets.isEmpty()) {
                        JPanel emptyPanel = new JPanel(new GridBagLayout());
                        emptyPanel.setOpaque(false);
                        JLabel lblEmpty = new JLabel(isVietnamese ? "Chưa có hạn mức chi tiêu nào." : "No budget limits configured yet.");
                        lblEmpty.setFont(new Font("Segoe UI", Font.ITALIC, 14));
                        lblEmpty.setForeground(ThemeManager.getColor("textSecondary"));
                        emptyPanel.add(lblEmpty);
                        budgetListContainer.add(emptyPanel);
                    } else {
                        for (Budget b : activeBudgets) {
                            budgetListContainer.add(createBudgetCard(b));
                            budgetListContainer.add(Box.createVerticalStrut(14));
                        }
                    }

                    budgetListContainer.revalidate();
                    budgetListContainer.repaint();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    private JPanel createBudgetCard(Budget b) {
        JPanel card = new JPanel(new BorderLayout(18, 0));
        card.setBackground(ThemeManager.getColor("surface"));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.getColor("border"), 1, true),
                BorderFactory.createEmptyBorder(14, 18, 14, 18)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 115));

        BudgetProgressCircle progressCircle = new BudgetProgressCircle(b.getLimit(), b.getSpent());
        card.add(progressCircle, BorderLayout.WEST);

        JPanel infoPanel = new JPanel(new GridLayout(3, 1, 0, 4));
        infoPanel.setOpaque(false);

        String title = b.getCategory() == null ?
                (isVietnamese ? "Ngân sách Tổng Thể" : "Overall Budget") :
                (b.getCategory().getName());
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(ThemeManager.getColor("textPrimary"));

        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String duration = String.format("(%s ➔ %s)", b.getStartDate().format(df), b.getEndDate().format(df));
        JLabel lblDuration = new JLabel(duration);
        lblDuration.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDuration.setForeground(ThemeManager.getColor("textSecondary"));

        String details = String.format(isVietnamese ? "Đã tiêu dùng: %,.0f đ / Giới hạn: %,.0f đ" : "Spent: %,.0f / Limit: %,.0f VND", b.getSpent(), b.getLimit());
        JLabel lblDetails = new JLabel(details);
        lblDetails.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblDetails.setForeground(b.getSpent() > b.getLimit() ? ThemeManager.getColor("danger") : ThemeManager.getColor("success"));

        infoPanel.add(lblTitle);
        infoPanel.add(lblDuration);
        infoPanel.add(lblDetails);
        card.add(infoPanel, BorderLayout.CENTER);

        JButton btnDelete = new JButton("❌");
        btnDelete.setFocusPainted(false);
        btnDelete.setContentAreaFilled(false);
        btnDelete.setBorderPainted(false);
        btnDelete.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnDelete.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnDelete.addActionListener(e -> {
            int option = JOptionPane.showConfirmDialog(this,
                    isVietnamese ? "Gỡ bỏ hạn mức này?" : "Remove this budget limit?",
                    isVietnamese ? "Xác nhận xóa" : "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (option == JOptionPane.YES_OPTION) {
                budgetManager.deleteBudget(b.getId());
                refreshData();
                if (mainFrame != null) mainFrame.refreshAllPanels();
            }
        });
        card.add(btnDelete, BorderLayout.EAST);

        return card;
    }

    private class BudgetProgressCircle extends JComponent {
        private final double limit;
        private final double spent;

        public BudgetProgressCircle(double limit, double spent) {
            this.limit = limit;
            this.spent = spent;
            setPreferredSize(new Dimension(85, 85));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int size = Math.min(getWidth(), getHeight()) - 12;
            int x = (getWidth() - size) / 2;
            int y = (getHeight() - size) / 2;

            g2.setStroke(new BasicStroke(7, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(ThemeManager.getColor("border"));
            g2.drawOval(x, y, size, size);

            double pct = limit > 0 ? (spent / limit) : 0;
            int angle = (int) (pct * 360);
            if (angle > 360) angle = 360;

            if (spent > limit) {
                g2.setColor(ThemeManager.getColor("danger"));
            } else if (pct >= 0.8) {
                g2.setColor(ThemeManager.getColor("warning"));
            } else {
                g2.setColor(ThemeManager.getColor("success"));
            }

            g2.drawArc(x, y, size, size, 90, -angle);

            String txt = String.format("%.0f%%", pct * 100);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
            g2.setColor(spent > limit ? ThemeManager.getColor("danger") : ThemeManager.getColor("textPrimary"));
            FontMetrics fm = g2.getFontMetrics();
            int tx = (getWidth() - fm.stringWidth(txt)) / 2;
            int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(txt, tx, ty);

            g2.dispose();
        }
    }

    private void styleTextField(JTextField tf) {
        tf.setBackground(ThemeManager.getColor("input"));
        tf.setForeground(ThemeManager.getColor("textPrimary"));
        tf.setCaretColor(ThemeManager.getColor("accent"));
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.getColor("border")),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
    }

    private void styleComboBox(JComboBox<?> cb) {
        cb.setBackground(ThemeManager.getColor("input"));
        cb.setForeground(ThemeManager.getColor("textPrimary"));
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cb.setOpaque(true);
        cb.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.getColor("border"), 1),
                BorderFactory.createEmptyBorder(2, 6, 2, 6)
        ));
    }

    private void styleSpinner(JSpinner spinner) {
        spinner.setBackground(ThemeManager.getColor("input"));
        spinner.setForeground(ThemeManager.getColor("textPrimary"));
        spinner.setFont(new Font("Segoe UI", Font.BOLD, 14));
        spinner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.getColor("border"), 1),
                BorderFactory.createEmptyBorder(2, 2, 2, 2)
        ));
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JSpinner.DefaultEditor defaultEditor = (JSpinner.DefaultEditor) editor;
            defaultEditor.getTextField().setBackground(ThemeManager.getColor("input"));
            defaultEditor.getTextField().setForeground(ThemeManager.getColor("textPrimary"));
            defaultEditor.getTextField().setCaretColor(ThemeManager.getColor("accent"));
            defaultEditor.getTextField().setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        }
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

    public void applyTheme() {
        setOpaque(true);
        setBackground(ThemeManager.getColor("bg"));

        if (scrollPane != null) {
            scrollPane.setOpaque(true);
            scrollPane.setBackground(ThemeManager.getColor("bg"));
            scrollPane.getViewport().setOpaque(true);
            scrollPane.getViewport().setBackground(ThemeManager.getColor("bg"));
        }

        if (budgetListContainer != null) {
            budgetListContainer.setOpaque(true);
            budgetListContainer.setBackground(ThemeManager.getColor("bg"));
        }

        if (lblFormTitle != null) lblFormTitle.setForeground(ThemeManager.getColor("accent"));
        if (lblListTitle != null) lblListTitle.setForeground(ThemeManager.getColor("textPrimary"));

        if (btnSaveBudget != null) {
            btnSaveBudget.setBackground(ThemeManager.getColor("accent"));
            btnSaveBudget.setForeground(ThemeManager.getContrastColor(ThemeManager.getColor("accent")));
        }

        if (cmbScope != null) styleComboBox(cmbScope);
        if (cmbPeriod != null) styleComboBox(cmbPeriod);
        if (spinReminderThreshold != null) styleSpinner(spinReminderThreshold);

        for (Component c : this.getComponents()) {
            updateLabelsTheme(c);
        }

        refreshCategoryGrid();
        refreshData();
    }

    private void updateLabelsTheme(Component c) {
        if (c instanceof JLabel && c != lblFormTitle && c != lblListTitle) {
            c.setForeground(ThemeManager.getColor("textSecondary"));
        } else if (c instanceof Container) {
            for (Component child : ((Container) c).getComponents()) {
                updateLabelsTheme(child);
            }
        }
    }

    public void updateLanguageText(boolean isVN) {
        this.isVietnamese = isVN;
        if (lblFormTitle != null) lblFormTitle.setText(isVN ? "THIẾT LẬP HẠN MỨC" : "BUDGET CONFIG");
        if (lblListTitle != null) lblListTitle.setText(isVN ? "TIẾN TRÌNH HẠN MỨC HIỆN TẠI" : "CURRENT BUDGET PROGRESS");
        if (btnSaveBudget != null) btnSaveBudget.setText(isVN ? "Kích hoạt Ngân sách" : "Activate Budget");

        if (lblScope != null) lblScope.setText(isVN ? "Phạm vi áp dụng:" : "Scope:");
        if (lblCategory != null) lblCategory.setText(isVN ? "Chọn danh mục:" : "Select Category:");
        if (lblPeriod != null) lblPeriod.setText(isVN ? "Kỳ hạn:" : "Period:");
        if (lblAmount != null) lblAmount.setText(isVN ? "Số tiền (VND):" : "Amount (VND):");
        if (lblReminder != null) lblReminder.setText(isVN ? "Ngưỡng nhắc nhở (%):" : "Reminder Threshold (%):");

        if (cmbScope != null) {
            int sel = cmbScope.getSelectedIndex();
            cmbScope.setModel(new DefaultComboBoxModel<>(isVN ? new String[]{"Tổng thể", "Theo danh mục"} : new String[]{"Overall", "By Category"}));
            if (sel >= 0 && sel < cmbScope.getItemCount()) cmbScope.setSelectedIndex(sel);
        }
        if (cmbPeriod != null) {
            int sel = cmbPeriod.getSelectedIndex();
            cmbPeriod.setModel(new DefaultComboBoxModel<>(isVN ? new String[]{"Theo Ngày", "Theo Tháng", "Theo Năm"} : new String[]{"Daily", "Monthly", "Yearly"}));
            if (sel >= 0) cmbPeriod.setSelectedIndex(sel);
        }

        refreshCategoryGrid();
        refreshData();
    }

    @Override
    public void update(EventType eventType, Object data) {
        if (eventType == EventType.TRANSACTION_ADDED ||
                eventType == EventType.TRANSACTION_UPDATED ||
                eventType == EventType.TRANSACTION_DELETED ||
                eventType == EventType.BUDGET_CHANGED ||
                eventType == EventType.DATA_LOADED) {
            SwingUtilities.invokeLater(this::refreshData);
        }
    }
}