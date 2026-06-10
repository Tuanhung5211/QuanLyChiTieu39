package com.expensemanager.ui;

import com.expensemanager.entity.RecurringTransaction;
import com.expensemanager.entity.TransactionType;
import com.expensemanager.observer.EventType;
import com.expensemanager.observer.Observer;
import com.expensemanager.service.RecurringTransactionService;
import com.expensemanager.service.ThemeManager;
import com.expensemanager.util.EmojiUtil;

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

    private JLabel titleLabel;
    private JButton btnAdd;

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

        titleLabel = new JLabel(isVietnamese ? "Giao dịch định kì" : "Scheduled Transactions");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(ThemeManager.getColor("textPrimary"));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        btnAdd = new JButton(isVietnamese ? "Thêm định kì" : "Add Scheduled");
        btnAdd.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnAdd.setFocusPainted(false);
        btnAdd.setBackground(ThemeManager.getColor("accent"));
        btnAdd.setForeground(ThemeManager.getContrastColor(ThemeManager.getColor("accent")));
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
            JLabel emptyLabel = new JLabel(isVietnamese ? "Chưa có giao dịch định kì nào" : "No scheduled transactions yet");
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
        // Màu nền dựa trên trạng thái hoạt động
        Color bgColor = rt.isActive()
                ? ThemeManager.getColor("success")   // Xanh lá
                : ThemeManager.getColor("danger");   // Đỏ
        Color fgColor = ThemeManager.getContrastColor(bgColor);

        JPanel card = new JPanel(new BorderLayout(10, 0));
        card.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));
        card.setBackground(bgColor);
        card.setOpaque(true);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        // Icon trạng thái
        String statusEmoji = rt.isActive() ? "✅" : "⏸️";
        JLabel lblIcon = new JLabel(statusEmoji, SwingConstants.CENTER);
        lblIcon.setFont(EmojiUtil.getEmojiFont(20));
        lblIcon.setForeground(fgColor);
        lblIcon.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 12));
        infoPanel.add(lblIcon);

        // Tên danh mục
        String title = (rt.getCategory() != null) ? rt.getCategory().getName() :
                (isVietnamese ? "Không xác định" : "N/A");
        JLabel categoryLabel = new JLabel(title);
        categoryLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        categoryLabel.setForeground(fgColor);
        infoPanel.add(categoryLabel);

        // Số tiền
        JLabel amountLabel = new JLabel(String.format("%,.0f VND", rt.getAmount()));
        amountLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        amountLabel.setForeground(fgColor);
        infoPanel.add(amountLabel);

        // Loại định kì và trạng thái
        String recurTypeLabel = RecurringTransactionService.getRecurrenceTypeLabel(rt.getRecurrenceType(), isVietnamese);
        String statusText = rt.isActive()
                ? (isVietnamese ? "Đang hoạt động" : "Active")
                : (isVietnamese ? "Đã dừng" : "Inactive");
        JLabel metaLabel = new JLabel("⏰ " + recurTypeLabel + "   " + statusText);
        metaLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        metaLabel.setForeground(new Color(fgColor.getRed(), fgColor.getGreen(), fgColor.getBlue(), 200));
        infoPanel.add(metaLabel);

        card.add(infoPanel, BorderLayout.CENTER);

        // Panel chứa nút hành động
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        actionPanel.setOpaque(false);

        // Nút toggle (Bật/Tắt)
        JButton btnToggle = new JButton(rt.isActive() ? "⏸️" : "▶️");
        btnToggle.setFont(EmojiUtil.getEmojiFont(14));
        btnToggle.setFocusPainted(false);
        btnToggle.setPreferredSize(new Dimension(40, 35));
        btnToggle.setBackground(ThemeManager.getColor("surface"));
        btnToggle.setForeground(ThemeManager.getColor("textPrimary"));
        btnToggle.setBorder(BorderFactory.createLineBorder(ThemeManager.getColor("border"), 1, true));
        btnToggle.setToolTipText(isVietnamese ? (rt.isActive() ? "Dừng" : "Kích hoạt") : (rt.isActive() ? "Disable" : "Enable"));
        btnToggle.addActionListener(e -> {
            if (rt.isActive()) {
                recurringTransactionService.deactivateRecurringTransaction(rt.getId());
            } else {
                recurringTransactionService.activateRecurringTransaction(rt.getId());
            }
        });
        actionPanel.add(btnToggle);

        // Nút xóa
        JButton btnDelete = new JButton("🗑️");
        btnDelete.setFont(EmojiUtil.getEmojiFont(14));
        btnDelete.setFocusPainted(false);
        btnDelete.setPreferredSize(new Dimension(40, 35));
        btnDelete.setBackground(ThemeManager.getColor("surface"));
        btnDelete.setForeground(ThemeManager.getColor("textPrimary"));
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
        ThemeManager.applyThemeRecursively(this);
        setBackground(ThemeManager.getColor("bg"));
        if (contentPanel != null) {
            contentPanel.setBackground(ThemeManager.getColor("bg"));
        }
        if (titleLabel != null) {
            titleLabel.setForeground(ThemeManager.getColor("textPrimary"));
        }
        if (btnAdd != null) {
            btnAdd.setBackground(ThemeManager.getColor("accent"));
            btnAdd.setForeground(ThemeManager.getContrastColor(ThemeManager.getColor("accent")));
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
        if (titleLabel != null) titleLabel.setText(isVietnamese ? "Giao dịch định kì" : "Scheduled Transactions");
        if (btnAdd != null) btnAdd.setText(isVietnamese ? "Thêm định kì" : "Add Scheduled");
        refreshUI();
    }
}