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
import com.expensemanager.util.InputValidator;
import com.expensemanager.util.ThemeManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class BudgetPanel extends JPanel implements Observer {

    private MainFrame mainFrame;
    private BudgetManager budgetManager;
    private FinanceService financeService;
    private boolean isVietnamese = true;

    private JComboBox<String> cmbScope;
    private JComboBox<Category> cmbCategory;
    private JComboBox<String> cmbPeriod;          // <-- NOW DECLARED
    private JTextField txtAmount;
    private JSpinner spinReminderThreshold;
    private JButton btnSaveBudget;
    private JLabel lblFormTitle, lblListTitle;
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
        applyTheme();
        setBorder(new EmptyBorder(25, 30, 25, 30));

        initComponents();
        refreshData();
    }

    private void initComponents() {
        JPanel splitContainer = new JPanel(new BorderLayout(30, 0));
        splitContainer.setOpaque(false);

        // --- Left column ---
        JPanel leftColumn = new JPanel(new BorderLayout(0, 15));
        leftColumn.setOpaque(false);
        leftColumn.setPreferredSize(new Dimension(360, 0));

        lblFormTitle = new JLabel(isVietnamese ? "THIẾT LẬP HẠN MỨC" : "BUDGET CONFIG");
        lblFormTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        leftColumn.add(lblFormTitle, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
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
        leftColumn.add(formPanel, BorderLayout.CENTER);
        splitContainer.add(leftColumn, BorderLayout.WEST);

        // --- Right column ---
        JPanel rightColumn = new JPanel(new BorderLayout(0, 15));
        rightColumn.setOpaque(false);

        lblListTitle = new JLabel(isVietnamese ? "TIẾN TRÌNH HẠN MỨC HIỆN TẠI" : "CURRENT BUDGET PROGRESS");
        lblListTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        rightColumn.add(lblListTitle, BorderLayout.NORTH);

        budgetListContainer = new JPanel();
        budgetListContainer.setLayout(new BoxLayout(budgetListContainer, BoxLayout.Y_AXIS));
        budgetListContainer.setOpaque(false);

        scrollPane = new JScrollPane(budgetListContainer);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        rightColumn.add(scrollPane, BorderLayout.CENTER);

        splitContainer.add(rightColumn, BorderLayout.CENTER);
        add(splitContainer, BorderLayout.CENTER);
    }

    private void addFormFields(JPanel panel, GridBagConstraints gbc) {
        // Scope
        panel.add(new JLabel(isVietnamese ? "Phạm vi áp dụng:" : "Scope:"), gbc);
        cmbScope = new JComboBox<>(isVietnamese ?
                new String[]{"Tổng thể", "Theo danh mục"} :
                new String[]{"Overall", "By Category"});
        styleComboBox(cmbScope);
        panel.add(cmbScope, gbc);

        // Category (only enabled when scope is "By Category")
        panel.add(new JLabel(isVietnamese ? "Danh mục:" : "Category:"), gbc);
        cmbCategory = new JComboBox<>();
        if (financeService != null) {
            financeService.getAllCategories().stream()
                    .filter(c -> c.getType() == TransactionType.EXPENSE)
                    .forEach(cmbCategory::addItem);
        }
        cmbCategory.setEnabled(false);
        styleComboBox(cmbCategory);
        panel.add(cmbCategory, gbc);
        cmbScope.addActionListener(e ->
                cmbCategory.setEnabled(cmbScope.getSelectedIndex() == 1));

        // Period (Daily/Monthly/Yearly)
        panel.add(new JLabel(isVietnamese ? "Kỳ hạn:" : "Period:"), gbc);
        cmbPeriod = new JComboBox<>(isVietnamese ?
                new String[]{"Theo Ngày", "Theo Tháng", "Theo Năm"} :
                new String[]{"Daily", "Monthly", "Yearly"});
        styleComboBox(cmbPeriod);
        panel.add(cmbPeriod, gbc);

        // Amount
        panel.add(new JLabel(isVietnamese ? "Số tiền (VND):" : "Amount (VND):"), gbc);
        txtAmount = new JTextField();
        styleTextField(txtAmount);
        panel.add(txtAmount, gbc);

        // Reminder threshold
        panel.add(new JLabel(isVietnamese ? "Ngưỡng nhắc nhở (%):" : "Reminder Threshold (%):"), gbc);
        spinReminderThreshold = new JSpinner(new SpinnerNumberModel(80, 10, 100, 5));
        panel.add(spinReminderThreshold, gbc);

        // Save button
        btnSaveBudget = new JButton(isVietnamese ? "Kích hoạt Ngân sách" : "Activate Budget");
        btnSaveBudget.addActionListener(e -> saveBudgetAction());
        panel.add(btnSaveBudget, gbc);
    }

    private void saveBudgetAction() {
        try {
            double amount = InputValidator.validateAmount(txtAmount.getText(), isVietnamese);
            Category selectedCat = (cmbScope.getSelectedIndex() == 1) ?
                    (Category) cmbCategory.getSelectedItem() : null;
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
                JOptionPane.showMessageDialog(this,
                        isVietnamese ? "Đã lưu và triển khai mục tiêu hạn mức!" : "Budget deployed successfully!");
                txtAmount.setText("");
                refreshData();
                if (mainFrame != null) mainFrame.refreshAllPanels();
            }
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    isVietnamese ? "Lỗi nhập liệu" : "Input Error", JOptionPane.WARNING_MESSAGE);
        } catch (InvalidAmountException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    isVietnamese ? "Lỗi số tiền" : "Amount Error", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void refreshData() {
        if (budgetListContainer == null || budgetManager == null) return;
        budgetListContainer.removeAll();

        List<Budget> activeBudgets = budgetManager.getAllBudgets();
        if (activeBudgets == null || activeBudgets.isEmpty()) {
            JPanel emptyPanel = new JPanel(new GridBagLayout());
            emptyPanel.setOpaque(false);
            JLabel lblEmpty = new JLabel(isVietnamese ?
                    "Chưa có hạn mức chi tiêu nào được thiết lập." :
                    "No budget limits configured yet.");
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
    }

    private JPanel createBudgetCard(Budget b) {
        // Resolve colors from ThemeManager for dynamic theming
        Color surfaceColor = ThemeManager.getColor("surface");
        Color textPrimary = ThemeManager.getColor("textPrimary");
        Color textSecondary = ThemeManager.getColor("textSecondary");
        Color dangerColor = ThemeManager.getColor("danger");
        Color successColor = ThemeManager.getColor("success");
        Color borderColor = ThemeManager.getColor("border");

        JPanel card = new JPanel(new BorderLayout(18, 0));
        card.setBackground(surfaceColor);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor, 1, true),
                BorderFactory.createEmptyBorder(14, 18, 14, 18)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 115));

        BudgetProgressCircle progressCircle = new BudgetProgressCircle(b.getLimit(), b.getSpent());
        card.add(progressCircle, BorderLayout.WEST);

        JPanel infoPanel = new JPanel(new GridLayout(3, 1, 0, 4));
        infoPanel.setOpaque(false);

        String title = b.getCategory() == null ?
                (isVietnamese ? "🎯 Ngân sách Tổng Thể Hệ Thống" : "🎯 Overall System Budget") :
                ("📌 Hạn mức mục: " + b.getCategory().getName());
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(textPrimary);

        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String duration = String.format("(%s ➔ %s)",
                b.getStartDate().format(df), b.getEndDate().format(df));
        JLabel lblDuration = new JLabel(duration);
        lblDuration.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDuration.setForeground(textSecondary);

        String details = String.format(
                isVietnamese ? "Đã tiêu dùng: %,.0f đ / Giới hạn: %,.0f đ" : "Spent: %,.0f / Limit: %,.0f VND",
                b.getSpent(), b.getLimit());
        JLabel lblDetails = new JLabel(details);
        lblDetails.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblDetails.setForeground(b.getSpent() > b.getLimit() ? dangerColor : successColor);

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
                    isVietnamese ? "Bạn có chắc muốn gỡ bỏ hạn mức này không?" :
                            "Are you sure you want to remove this budget limit?",
                    isVietnamese ? "Xác nhận xóa" : "Confirm Delete",
                    JOptionPane.YES_NO_OPTION);
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

            // Track
            g2.setStroke(new BasicStroke(7, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(ThemeManager.getColor("progressTrack"));
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

    private void styleFormLabel(JLabel label) {
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(ThemeManager.getColor("textSecondary"));
    }

    private void styleTextField(JTextField tf) {
        tf.setBackground(ThemeManager.getColor("inputBg"));
        tf.setForeground(ThemeManager.getColor("textPrimary"));
        tf.setCaretColor(ThemeManager.getColor("accent"));
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.getColor("border")),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
    }

    private void styleComboBox(JComboBox<?> cb) {
        cb.setBackground(ThemeManager.getColor("inputBg"));
        cb.setForeground(ThemeManager.getColor("textPrimary"));
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    }

    public void updateLanguageText(boolean isVN) {
        this.isVietnamese = isVN;
        if (lblFormTitle != null) lblFormTitle.setText(isVN ? "THIẾT LẬP HẠN MỨC" : "BUDGET CONFIG");
        if (lblListTitle != null) lblListTitle.setText(isVN ? "TIẾN TRÌNH HẠN MỨC HIỆN TẠI" : "CURRENT BUDGET PROGRESS");
        if (btnSaveBudget != null) btnSaveBudget.setText(isVN ? "Kích hoạt Ngân sách" : "Activate Budget");

        if (cmbScope != null) {
            int idx = cmbScope.getSelectedIndex();
            cmbScope.setModel(new DefaultComboBoxModel<>(isVN ?
                    new String[]{"Tổng thể", "Theo danh mục"} :
                    new String[]{"Overall", "By Category"}));
            cmbScope.setSelectedIndex(idx);
        }
        if (cmbPeriod != null) {
            int idx = cmbPeriod.getSelectedIndex();
            cmbPeriod.setModel(new DefaultComboBoxModel<>(isVN ?
                    new String[]{"Theo Ngày", "Theo Tháng", "Theo Năm"} :
                    new String[]{"Daily", "Monthly", "Yearly"}));
            cmbPeriod.setSelectedIndex(idx);
        }
        refreshData();
    }

    public void applyTheme() {
        setBackground(ThemeManager.getColor("bg"));
        if (scrollPane != null) scrollPane.getViewport().setBackground(ThemeManager.getColor("bg"));
        if (budgetListContainer != null) budgetListContainer.setBackground(ThemeManager.getColor("bg"));
        if (lblFormTitle != null) lblFormTitle.setForeground(ThemeManager.getColor("accent"));
        if (lblListTitle != null) lblListTitle.setForeground(ThemeManager.getColor("textPrimary"));
        if (btnSaveBudget != null) {
            btnSaveBudget.setBackground(ThemeManager.getColor("accent"));
            btnSaveBudget.setForeground(ThemeManager.getColor("bg"));
        }
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