package com.expensemanager.ui;

import com.expensemanager.database.DatabaseUtil;
import com.expensemanager.entity.Category;
import com.expensemanager.entity.TransactionType;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.UUID;

public class AddCategoryPanel extends JPanel {
    private MainFrame mainFrame;
    private boolean isVietnamese;

    private JPanel categoryCard;
    private JLabel lblCategoryTitle, lblCatNameHint, lblCatIconHint, lblCatTypeHint, lblEmojiPageIndicator;
    private JTextField txtCategoryName;
    private JComboBox<String> comboCategoryType;
    private JPanel emojiGridPanel, emojiPagination, gridWrapper;
    private JButton btnSaveCategory, btnPrevEmojiPage, btnNextEmojiPage;

    private String selectedEmoji = "📌";
    private int currentEmojiPage = 1;
    private final int EMOJI_PER_PAGE = 18;
    private int currentFluidWidth = 560;

    private final String[] EMOJI_LIST = {
            "📌", "🍔", "🛒", "🛍️", "🍿", "🍎", "👗", "💻", "⛽", "🏍️",
            "🚗", "⚡", "🌐", "🏢", "📱", "🎮", "🎬", "✈️", "📚", "🏥",
            "💊", "💄", "⚽", "🐱", "🎁", "💖", "🔧", "🏠", "🛡️", "💰",
            "💵", "🎓", "💼", "📈", "🐷", "🪙"
    };

    private final Color SURFACE_COLOR = new Color(30, 30, 30);
    private final Color INPUT_BG = new Color(40, 40, 40);
    private final Color TEXT_PRIMARY = new Color(240, 240, 240);
    private final Color TEXT_SECONDARY = new Color(150, 150, 150);
    private final Color ACCENT_YELLOW = new Color(255, 193, 7);

    private int getEmojiCellWidth() {
        return (currentFluidWidth - 48 - 64) / 9;
    }

    private int getEmojiCellHeight() {
        int w = getEmojiCellWidth();
        if (w >= 85) return 76;
        if (w >= 70) return 62;
        return 48;
    }

    public AddCategoryPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.isVietnamese = mainFrame != null && mainFrame.isVietnamese();

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);

        categoryCard = new JPanel(new BorderLayout());
        categoryCard.setBackground(SURFACE_COLOR);
        categoryCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(45, 45, 45), 1, true),
                BorderFactory.createEmptyBorder(22, 24, 22, 24)
        ));
        categoryCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel pForm = new JPanel(new GridBagLayout());
        pForm.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(5, 0, 5, 0);

        lblCategoryTitle = new JLabel();
        lblCategoryTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblCategoryTitle.setForeground(ACCENT_YELLOW);
        lblCategoryTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        gbc.gridx = 0; gbc.gridy = 0;
        pForm.add(lblCategoryTitle, gbc);

        lblCatNameHint = createLabel();
        gbc.gridy = 1; pForm.add(lblCatNameHint, gbc);

        txtCategoryName = new JTextField();
        styleTextField(txtCategoryName);
        gbc.gridy = 2; pForm.add(txtCategoryName, gbc);

        lblCatIconHint = createLabel();
        gbc.gridy = 3; pForm.add(lblCatIconHint, gbc);

        emojiGridPanel = new JPanel(new GridLayout(2, 9, 8, 8));
        emojiGridPanel.setBackground(SURFACE_COLOR);
        gbc.gridy = 4; pForm.add(emojiGridPanel, gbc);

        emojiPagination = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        emojiPagination.setOpaque(false);
        btnPrevEmojiPage = createPaginationButton("<");
        btnNextEmojiPage = createPaginationButton(">");
        lblEmojiPageIndicator = new JLabel();
        lblEmojiPageIndicator.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblEmojiPageIndicator.setForeground(TEXT_PRIMARY);
        lblEmojiPageIndicator.setBorder(BorderFactory.createEmptyBorder(0, 18, 0, 18));

        btnPrevEmojiPage.addActionListener(e -> { if (currentEmojiPage > 1) { currentEmojiPage--; refreshEmojiGrid(); } });
        btnNextEmojiPage.addActionListener(e -> { currentEmojiPage++; refreshEmojiGrid(); });

        emojiPagination.add(btnPrevEmojiPage);
        emojiPagination.add(lblEmojiPageIndicator);
        emojiPagination.add(btnNextEmojiPage);
        gbc.gridy = 5; pForm.add(emojiPagination, gbc);

        lblCatTypeHint = createLabel();
        gbc.gridy = 6; pForm.add(lblCatTypeHint, gbc);

        comboCategoryType = new JComboBox<>();
        comboCategoryType.setBackground(INPUT_BG);
        comboCategoryType.setForeground(TEXT_PRIMARY);
        comboCategoryType.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        gbc.gridy = 7; pForm.add(comboCategoryType, gbc);

        btnSaveCategory = new JButton();
        btnSaveCategory.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnSaveCategory.setBackground(ACCENT_YELLOW);
        btnSaveCategory.setForeground(SURFACE_COLOR);
        btnSaveCategory.setFocusPainted(false);
        btnSaveCategory.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSaveCategory.addActionListener(e -> executeAddCategory());

        gbc.gridy = 8;
        gbc.insets = new Insets(15, 0, 0, 0);
        pForm.add(btnSaveCategory, gbc);

        categoryCard.add(pForm, BorderLayout.CENTER);
        add(categoryCard);
    }

    public void updateResponsiveLayout(boolean isVN, int fluidWidth) {
        this.isVietnamese = isVN;
        this.currentFluidWidth = fluidWidth;
        int currentTypeIndex = comboCategoryType.getSelectedIndex();

        int cardH = fluidWidth >= 850 ? 560 : (fluidWidth >= 700 ? 490 : 430);
        int gridH = (getEmojiCellHeight() * 2) + 8;

        setMaximumSize(new Dimension(fluidWidth, Integer.MAX_VALUE));

        if (categoryCard != null) {
            categoryCard.setPreferredSize(new Dimension(fluidWidth, cardH));
            categoryCard.setMaximumSize(new Dimension(fluidWidth, cardH));
            categoryCard.setMinimumSize(new Dimension(fluidWidth, cardH));
        }

        if (emojiGridPanel != null) {
            emojiGridPanel.setPreferredSize(new Dimension(fluidWidth - 48, gridH));
            emojiGridPanel.setMaximumSize(new Dimension(fluidWidth - 48, gridH));
            emojiGridPanel.setMinimumSize(new Dimension(fluidWidth - 48, gridH));
        }

        if (isVN) {
            lblCategoryTitle.setText("Thêm danh mục chi tiêu / thu nhập");
            lblCatNameHint.setText("Tên danh mục mới:");
            lblCatIconHint.setText("Chọn Icon/Emoji đại diện:");
            lblCatTypeHint.setText("Phân loại danh mục:");
            btnSaveCategory.setText("XÁC NHẬN THÊM DANH MỤC");
            comboCategoryType.setModel(new DefaultComboBoxModel<>(new String[]{"Khoản chi tiêu (EXPENSE)", "Khoản thu nhập (INCOME)"}));
        } else {
            lblCategoryTitle.setText("Add Expense / Income Category");
            lblCatNameHint.setText("New Category Name:");
            lblCatIconHint.setText("Select Representative Icon/Emoji:");
            lblCatTypeHint.setText("Category Type:");
            btnSaveCategory.setText("CONFIRM ADD CATEGORY");
            comboCategoryType.setModel(new DefaultComboBoxModel<>(new String[]{"Expense Component (EXPENSE)", "Income Component (INCOME)"}));
        }

        if (currentTypeIndex >= 0 && currentTypeIndex < comboCategoryType.getItemCount()) {
            comboCategoryType.setSelectedIndex(currentTypeIndex);
        }

        refreshEmojiGrid();
    }

    private void refreshEmojiGrid() {
        if (emojiGridPanel == null) return;
        emojiGridPanel.removeAll();
        int totalItems = EMOJI_LIST.length;
        int totalPages = (int) Math.ceil((double) totalItems / EMOJI_PER_PAGE);
        if (currentEmojiPage > totalPages) currentEmojiPage = totalPages;

        lblEmojiPageIndicator.setText((isVietnamese ? "Trang " : "Page ") + currentEmojiPage + " / " + totalPages);
        btnPrevEmojiPage.setEnabled(currentEmojiPage > 1);
        btnNextEmojiPage.setEnabled(currentEmojiPage < totalPages);

        int startIndex = (currentEmojiPage - 1) * EMOJI_PER_PAGE;
        int endIndex = Math.min(startIndex + EMOJI_PER_PAGE, totalItems);
        int displayedCount = 0;

        for (int i = startIndex; i < endIndex; i++) {
            emojiGridPanel.add(createEmojiCellComponent(EMOJI_LIST[i]));
            displayedCount++;
        }

        for (int i = displayedCount; i < EMOJI_PER_PAGE; i++) {
            JPanel placeholder = new JPanel();
            placeholder.setOpaque(false);
            emojiGridPanel.add(placeholder);
        }

        emojiGridPanel.revalidate();
        emojiGridPanel.repaint();
    }

    private JPanel createEmojiCellComponent(String emoji) {
        JPanel cell = new JPanel(new BorderLayout());
        cell.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel lbl = new JLabel(emoji, SwingConstants.CENTER);
        int fontSize = currentFluidWidth >= 850 ? 24 : (currentFluidWidth >= 700 ? 20 : 18);
        lbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, fontSize));

        cell.setPreferredSize(new Dimension(getEmojiCellWidth(), getEmojiCellHeight()));
        cell.add(lbl, BorderLayout.CENTER);

        if (emoji.equals(selectedEmoji)) {
            cell.setBackground(ACCENT_YELLOW);
            lbl.setForeground(SURFACE_COLOR);
            cell.setBorder(BorderFactory.createLineBorder(ACCENT_YELLOW, 1, true));
        } else {
            cell.setBackground(INPUT_BG);
            lbl.setForeground(TEXT_PRIMARY);
            cell.setBorder(BorderFactory.createLineBorder(new Color(55, 55, 55), 1, true));
        }

        cell.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectedEmoji = emoji;
                refreshEmojiGrid();
            }
        });
        return cell;
    }

    private void executeAddCategory() {
        String name = txtCategoryName.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, isVietnamese ? "Vui lòng điền tên danh mục!" : "Please fill in the category name!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String selectedEmoji = this.selectedEmoji;
        TransactionType type = comboCategoryType.getSelectedIndex() == 0 ? TransactionType.EXPENSE : TransactionType.INCOME;
        String generatedId = UUID.randomUUID().toString().substring(0, 8);
        Category newCat = new Category(generatedId, name, type);
        try {
            DatabaseUtil.insertCategory(newCat);
            AddTransactionDialog.addCustomEmoji(name, selectedEmoji);
            JOptionPane.showMessageDialog(this, isVietnamese ? "Đã thêm danh mục '" + name + "' thành công!" : "Category '" + name + "' added successfully!");
            txtCategoryName.setText("");
            this.selectedEmoji = "📌"; this.currentEmojiPage = 1; refreshEmojiGrid();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error DB: " + ex.getMessage(), "System Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JLabel createLabel() {
        JLabel lbl = new JLabel();
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(TEXT_SECONDARY);
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 2, 6, 0));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private void styleTextField(JTextField tf) {
        tf.setBackground(INPUT_BG);
        tf.setForeground(TEXT_PRIMARY);
        tf.setCaretColor(ACCENT_YELLOW);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        tf.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(60, 60, 60)), BorderFactory.createEmptyBorder(10, 15, 10, 15)));
    }

    private JButton createPaginationButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(TEXT_PRIMARY);
        btn.setBackground(INPUT_BG);
        btn.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60), 1));
        btn.setPreferredSize(new Dimension(36, 28));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}