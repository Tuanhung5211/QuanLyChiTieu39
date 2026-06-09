package com.expensemanager.ui;

import com.expensemanager.entity.RecurringTransaction;
import com.expensemanager.observer.EventType;
import com.expensemanager.observer.Observer;
import com.expensemanager.service.RecurringTransactionService;
import com.expensemanager.service.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class RecurringTransactionManagerPanel extends JPanel implements Observer {

    private RecurringTransactionService recurringTransactionService;
    private MainFrame mainFrame;
    private JPanel contentPanel;
    private boolean isVietnamese = true;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public RecurringTransactionManagerPanel(RecurringTransactionService recurringTransactionService, MainFrame mainFrame) {
        this.recurringTransactionService = recurringTransactionService;
        this.mainFrame = mainFrame;
        if (mainFrame != null) {
            this.isVietnamese = mainFrame.isVietnamese();
            recurringTransactionService.attach(this);
        }

        setLayout(new BorderLayout());
        initComponents();
        refreshUI();
    }

    private void initComponents() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(isVietnamese ? "⌚ Giao dịch lặp lại" : "⌚ Recurring Transactions");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(ThemeManager.getColor("textPrimary"));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JButton btnAdd = new JButton(isVietnamese ? "➕ Thêm lặp lại" : "➕ Add Recurring");
        btnAdd.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnAdd.setFocusPainted(false);
        btnAdd.setBackground(ThemeManager.getColor("accent"));
        btnAdd.setForeground(ThemeManager.getColor("bg"));
        btnAdd.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        btnAdd.addActionListener(e -> {
            AddRecurringTransactionDialog dialog = new AddRecurringTransactionDialog(mainFrame, recurringTransactionService);
            dialog.setVisible(true);
        });
        headerPanel.add(btnAdd, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(10);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void refreshUI() {
        contentPanel.removeAll();

        List<RecurringTransaction> items = recurringTransactionService.getAllRecurringTransactions();

        if (items.isEmpty()) {
            JLabel emptyLabel = new JLabel(isVietnamese ? "Chưa có giao dịch lặp lại nào" : "No recurring transactions yet");
            emptyLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
            emptyLabel.setForeground(ThemeManager.getColor("textSecondary"));
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            contentPanel.add(Box.createVerticalStrut(30));
            contentPanel.add(emptyLabel);
            contentPanel.add(Box.createVerticalGlue());
        } else {
            for (RecurringTransaction rt : items) {
                contentPanel.add(createRecurringTransactionCard(rt));
                contentPanel.add(Box.createVerticalStrut(8));
            }
            contentPanel.add(Box.createVerticalGlue());
        }

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private JPanel createRecurringTransactionCard(RecurringTransaction rt) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout(10, 0));
        card.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));
        Color cardBg = rt.getType() == com.expensemanager.entity.TransactionType.EXPENSE ? ThemeManager.getColor("danger") : ThemeManager.getColor("success");
        card.setBackground(cardBg);
        card.setOpaque(true);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        String typeEmoji = rt.getType().name().equals("INCOME") ? "📥" : "📤";
        String title = (rt.getCategory() != null ? rt.getCategory().getName() : (isVietnamese ? "Không xác định" : "N/A"));

        JLabel lblIcon = new JLabel(typeEmoji, SwingConstants.CENTER);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        lblIcon.setOpaque(true);
        lblIcon.setBackground(new Color(0,0,0,0));
        lblIcon.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 12));
        lblIcon.setForeground(ThemeManager.getColor("bg"));
        infoPanel.add(lblIcon);

        JLabel categoryLabel = new JLabel(title);
        categoryLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        categoryLabel.setForeground(ThemeManager.getColor("bg"));
        infoPanel.add(categoryLabel);

        JLabel amountLabel = new JLabel(String.format("%,.0f VND", rt.getAmount()));
        amountLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        amountLabel.setForeground(ThemeManager.getColor("bg"));
        infoPanel.add(amountLabel);

        String recurTypeLabel = RecurringTransactionService.getRecurrenceTypeLabel(rt.getRecurrenceType(), isVietnamese);
        String dateText = rt.getStartDate().format(dateFormatter);
        if (rt.getEndDate() != null) {
            dateText += " → " + rt.getEndDate().format(dateFormatter);
        } else {
            dateText += " → " + (isVietnamese ? "Không hạn" : "No limit");
        }
        JLabel metaLabel = new JLabel("⌚ " + recurTypeLabel + "   📅 " + dateText);
        metaLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        // SỬA: dùng textPrimary với alpha 200 thay vì new Color(255,255,255,200)
        Color textPrimary = ThemeManager.getColor("textPrimary");
        metaLabel.setForeground(new Color(textPrimary.getRed(), textPrimary.getGreen(), textPrimary.getBlue(), 200));
        infoPanel.add(metaLabel);

        card.add(infoPanel, BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        actionPanel.setOpaque(false);

        JButton btnToggle = new JButton(rt.isActive() ? "✓" : "✗");
        btnToggle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnToggle.setFocusPainted(false);
        btnToggle.setPreferredSize(new Dimension(40, 35));
        btnToggle.setBackground(ThemeManager.getColor("surface"));
        btnToggle.setForeground(ThemeManager.getColor("bg"));
        btnToggle.setBorder(BorderFactory.createLineBorder(ThemeManager.getColor("border"), 1, true));
        btnToggle.setToolTipText(isVietnamese ? (rt.isActive() ? "Tắt" : "Bật") : (rt.isActive() ? "Disable" : "Enable"));
        btnToggle.addActionListener(e -> {
            if (rt.isActive()) {
                recurringTransactionService.deactivateRecurringTransaction(rt.getId());
            } else {
                recurringTransactionService.activateRecurringTransaction(rt.getId());
            }
        });
        actionPanel.add(btnToggle);

        JButton btnDelete = new JButton("🗑️");
        btnDelete.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnDelete.setFocusPainted(false);
        btnDelete.setPreferredSize(new Dimension(40, 35));
        btnDelete.setBackground(ThemeManager.getColor("surface"));
        btnDelete.setForeground(ThemeManager.getColor("bg"));
        btnDelete.setBorder(BorderFactory.createLineBorder(ThemeManager.getColor("border"), 1, true));
        btnDelete.setToolTipText(isVietnamese ? "Xóa" : "Delete");
        btnDelete.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(this,
                    isVietnamese ? "Bạn chắc chắn muốn xóa?" : "Are you sure?",
                    isVietnamese ? "Xác nhận" : "Confirm", JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION) {
                recurringTransactionService.deleteRecurringTransaction(rt.getId());
            }
        });
        actionPanel.add(btnDelete);

        card.add(actionPanel, BorderLayout.EAST);

        return card;
    }

    public void applyTheme() {
        setBackground(ThemeManager.getColor("bg"));

        for (Component comp : getComponents()) {
            if (comp instanceof JPanel) {
                for (Component innerComp : ((JPanel) comp).getComponents()) {
                    if (innerComp instanceof JLabel) {
                        innerComp.setForeground(ThemeManager.getColor("textPrimary"));
                    } else if (innerComp instanceof JButton) {
                        innerComp.setBackground(ThemeManager.getColor("accent"));
                        innerComp.setForeground(ThemeManager.getColor("bg")); // Nút "Thêm lặp lại" giữ trắng để tương phản, có thể thay bg nếu muốn
                    }
                }
            } else if (comp instanceof JScrollPane) {
                comp.setBackground(ThemeManager.getColor("bg"));
                ((JScrollPane) comp).getViewport().setBackground(ThemeManager.getColor("bg"));
            }
        }

        if (contentPanel != null) {
            contentPanel.setBackground(ThemeManager.getColor("bg"));
        }

        refreshUI();
    }

    @Override
    public void update(EventType eventType, Object data) {
        if (eventType == EventType.TRANSACTION_ADDED || eventType == EventType.TRANSACTION_UPDATED ||
                eventType == EventType.TRANSACTION_DELETED || eventType == EventType.DATA_LOADED) {
            refreshUI();
        }
    }

    public void updateLanguageText() {
        isVietnamese = mainFrame != null && mainFrame.isVietnamese();
        refreshUI();
    }
}