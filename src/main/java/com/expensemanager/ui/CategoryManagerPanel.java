package com.expensemanager.ui;

import com.expensemanager.database.DatabaseUtil;
import com.expensemanager.entity.Category;
import com.expensemanager.entity.TransactionType;
import com.expensemanager.util.EmojiUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.UUID;

public class CategoryManagerPanel extends JPanel {
    private MainFrame mainFrame;
    private boolean isVietnamese;

    private JPanel listCard;
    private JLabel lblListTitle;
    private JList<Category> categoryList;
    private DefaultListModel<Category> listModel;
    private JButton btnDeleteCategory;

    private JPanel addCard;
    private JLabel lblCategoryTitle, lblCatNameHint, lblCatIconHint, lblCatTypeHint, lblEmojiPageIndicator;
    private JTextField txtCategoryName;
    private JComboBox<String> comboCategoryType;
    private JPanel emojiGridPanel, emojiPagination, gridWrapper;
    private JButton btnSaveCategory, btnPrevEmojiPage, btnNextEmojiPage;

    private String selectedEmoji = "\uD83D\uDCCD";
    private int currentEmojiPage = 1;
    private final int EMOJI_PER_PAGE = 18;
    private int currentFluidWidth = 560;

    private final String[] EMOJI_LIST = {
            "\uD83D\uDCCD", "\uD83C\uDF54", "\uD83D\uDED2", "\uD83D\uDECD", "\uD83C\uDF7F", "\uD83C\uDF4E",
            "\uD83D\uDC57", "\uD83D\uDCBB", "\u26FD",       "\uD83C\uDFCD", "\uD83D\uDE97", "\u26A1",
            "\uD83C\uDF10", "\uD83C\uDFE2", "\uD83D\uDCF1", "\uD83C\uDFAE", "\uD83C\uDFAC", "\u2708",
            "\uD83D\uDCDA", "\uD83C\uDFE5", "\uD83D\uDC8A", "\uD83D\uDC84", "\u26BD",       "\uD83D\uDC31",
            "\uD83C\uDF81", "\uD83D\uDC96", "\uD83D\uDD27", "\uD83C\uDFE0", "\uD83D\uDEE1", "\uD83D\uDCB0",
            "\uD83D\uDCB5", "\uD83C\uDF93", "\uD83D\uDCBC", "\uD83D\uDCC8", "\uD83D\uDC37", "\uD83E\uDE99"
    };

    private final Color SURFACE_COLOR = new Color(30, 30, 30);
    private final Color INPUT_BG = new Color(40, 40, 40);
    private final Color TEXT_PRIMARY = new Color(240, 240, 240);
    private final Color TEXT_SECONDARY = new Color(150, 150, 150);
    private final Color ACCENT_YELLOW = new Color(255, 193, 7);
    private final Color DANGER_RED = new Color(244, 67, 54);

    private int getEmojiCellWidth() { return (currentFluidWidth - 48 - 64) / 9; }
    private int getEmojiCellHeight() { int w = getEmojiCellWidth(); return w >= 85 ? 76 : (w >= 70 ? 62 : 48); }

    public CategoryManagerPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.isVietnamese = mainFrame != null && mainFrame.isVietnamese();

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);

        listCard = new JPanel(new BorderLayout(0, 10));
        listCard.setBackground(SURFACE_COLOR);
        listCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(45, 45, 45), 1, true),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        listCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblListTitle = new JLabel();
        lblListTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblListTitle.setForeground(ACCENT_YELLOW);
        listCard.add(lblListTitle, BorderLayout.NORTH);

        listModel = new DefaultListModel<>();
        categoryList = new JList<>(listModel);
        categoryList.setBackground(INPUT_BG);
        categoryList.setForeground(TEXT_PRIMARY);
        categoryList.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        categoryList.setSelectionBackground(ACCENT_YELLOW);
        categoryList.setSelectionForeground(SURFACE_COLOR);
        categoryList.setCellRenderer(new CategoryCellRenderer());

        JScrollPane scrollPane = new JScrollPane(categoryList);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(55, 55, 55)));
        listCard.add(scrollPane, BorderLayout.CENTER);

        btnDeleteCategory = new JButton();
        btnDeleteCategory.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnDeleteCategory.setBackground(INPUT_BG); btnDeleteCategory.setForeground(DANGER_RED);
        btnDeleteCategory.setFocusPainted(false); btnDeleteCategory.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnDeleteCategory.setBorder(BorderFactory.createLineBorder(DANGER_RED, 1, true));
        btnDeleteCategory.addActionListener(e -> deleteCategory());
        listCard.add(btnDeleteCategory, BorderLayout.SOUTH);

        add(listCard);
        add(Box.createVerticalStrut(20));

        addCard = new JPanel(new BorderLayout());
        addCard.setBackground(SURFACE_COLOR);
        addCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(45, 45, 45), 1, true),
                BorderFactory.createEmptyBorder(18, 20, 18, 20)
        ));
        addCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel pForm = new JPanel(new GridBagLayout());
        pForm.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(4, 0, 4, 0);

        lblCategoryTitle = new JLabel();
        lblCategoryTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblCategoryTitle.setForeground(ACCENT_YELLOW);
        lblCategoryTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        gbc.gridx = 0; gbc.gridy = 0; pForm.add(lblCategoryTitle, gbc);

        lblCatNameHint = createLabel(); gbc.gridy = 1; pForm.add(lblCatNameHint, gbc);
        txtCategoryName = new JTextField(); styleTextField(txtCategoryName); gbc.gridy = 2; pForm.add(txtCategoryName, gbc);

        lblCatIconHint = createLabel(); gbc.gridy = 3; pForm.add(lblCatIconHint, gbc);
        emojiGridPanel = new JPanel(new GridLayout(2, 9, 8, 8)); emojiGridPanel.setBackground(SURFACE_COLOR);
        gridWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)); gridWrapper.setOpaque(false); gridWrapper.setAlignmentX(Component.LEFT_ALIGNMENT); gridWrapper.add(emojiGridPanel);
        gbc.gridy = 4; pForm.add(gridWrapper, gbc);

        emojiPagination = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0)); emojiPagination.setOpaque(false); emojiPagination.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnPrevEmojiPage = createPaginationButton("<"); btnNextEmojiPage = createPaginationButton(">");
        lblEmojiPageIndicator = new JLabel(); lblEmojiPageIndicator.setFont(new Font("Segoe UI", Font.BOLD, 13)); lblEmojiPageIndicator.setForeground(TEXT_PRIMARY); lblEmojiPageIndicator.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
        btnPrevEmojiPage.addActionListener(e -> { if (currentEmojiPage > 1) { currentEmojiPage--; refreshEmojiGrid(); } });
        btnNextEmojiPage.addActionListener(e -> { currentEmojiPage++; refreshEmojiGrid(); });
        emojiPagination.add(btnPrevEmojiPage); emojiPagination.add(lblEmojiPageIndicator); emojiPagination.add(btnNextEmojiPage);
        gbc.gridy = 5; pForm.add(emojiPagination, gbc);

        lblCatTypeHint = createLabel(); gbc.gridy = 6; pForm.add(lblCatTypeHint, gbc);
        comboCategoryType = new JComboBox<>(); comboCategoryType.setBackground(INPUT_BG); comboCategoryType.setForeground(TEXT_PRIMARY); comboCategoryType.setFont(new Font("Segoe UI", Font.PLAIN, 14)); comboCategoryType.setAlignmentX(Component.LEFT_ALIGNMENT);
        gbc.gridy = 7; pForm.add(comboCategoryType, gbc);

        btnSaveCategory = new JButton(); btnSaveCategory.setFont(new Font("Segoe UI", Font.BOLD, 15)); btnSaveCategory.setBackground(ACCENT_YELLOW); btnSaveCategory.setForeground(SURFACE_COLOR); btnSaveCategory.setFocusPainted(false); btnSaveCategory.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); btnSaveCategory.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnSaveCategory.addActionListener(e -> executeAddCategory());
        gbc.gridy = 8; gbc.insets = new Insets(12, 0, 0, 0); pForm.add(btnSaveCategory, gbc);

        addCard.add(pForm, BorderLayout.CENTER);
        add(addCard);

        updateResponsiveLayout(isVietnamese, 560);
        refreshCategories();
    }

    public void updateResponsiveLayout(boolean isVN, int fluidWidth) {
        this.isVietnamese = isVN;
        this.currentFluidWidth = fluidWidth;
        int currentTypeIndex = comboCategoryType.getSelectedIndex();
        int gridH = (getEmojiCellHeight() * 2) + 12;

        setMaximumSize(new Dimension(fluidWidth, Integer.MAX_VALUE));

        if (listCard != null) {
            listCard.setPreferredSize(new Dimension(fluidWidth, 210));
            listCard.setMaximumSize(new Dimension(fluidWidth, 210));
            listCard.setMinimumSize(new Dimension(fluidWidth, 210));
        }
        if (btnDeleteCategory != null) {
            btnDeleteCategory.setPreferredSize(new Dimension(fluidWidth, 36));
            btnDeleteCategory.setMaximumSize(new Dimension(fluidWidth, 36));
        }
        if (addCard != null) {
            addCard.setPreferredSize(new Dimension(fluidWidth, 540));
            addCard.setMaximumSize(new Dimension(fluidWidth, 540));
            addCard.setMinimumSize(new Dimension(fluidWidth, 540));
        }
        if (txtCategoryName != null) {
            txtCategoryName.setPreferredSize(new Dimension(fluidWidth - 48, 40));
            txtCategoryName.setMaximumSize(new Dimension(fluidWidth - 48, 40));
        }
        if (emojiGridPanel != null) {
            emojiGridPanel.setPreferredSize(new Dimension(fluidWidth - 48, gridH));
            emojiGridPanel.setMaximumSize(new Dimension(fluidWidth - 48, gridH));
            emojiGridPanel.setMinimumSize(new Dimension(fluidWidth - 48, gridH));
        }
        if (gridWrapper != null) {
            gridWrapper.setPreferredSize(new Dimension(fluidWidth - 48, gridH));
            gridWrapper.setMaximumSize(new Dimension(fluidWidth - 48, gridH));
        }
        if (emojiPagination != null) {
            emojiPagination.setPreferredSize(new Dimension(fluidWidth - 48, 32));
            emojiPagination.setMaximumSize(new Dimension(fluidWidth - 48, 32));
        }
        if (comboCategoryType != null) {
            comboCategoryType.setPreferredSize(new Dimension(fluidWidth - 48, 40));
            comboCategoryType.setMaximumSize(new Dimension(fluidWidth - 48, 40));
        }
        if (btnSaveCategory != null) {
            btnSaveCategory.setPreferredSize(new Dimension(fluidWidth - 48, 42));
            btnSaveCategory.setMaximumSize(new Dimension(fluidWidth - 48, 42));
        }

        if (isVN) {
            lblListTitle.setText("Danh sách các danh mục đang khả dụng");
            btnDeleteCategory.setText("XÓA DANH MỤC ĐANG CHỌN");
            lblCategoryTitle.setText("Thêm danh mục chi tiêu / thu nhập mới");
            lblCatNameHint.setText("Tên danh mục mới:"); lblCatIconHint.setText("Chọn Icon/Emoji đại diện:"); lblCatTypeHint.setText("Phân loại danh mục:"); btnSaveCategory.setText("XÁC NHẬN THÊM DANH MỤC");
            comboCategoryType.setModel(new DefaultComboBoxModel<>(new String[]{"Khoản chi tiêu (EXPENSE)", "Khoản thu nhập (INCOME)"}));
        } else {
            // 🌟 ĐÃ SỬA: Chuyển đổi ComboBox sang tiếng Anh chính xác
            lblListTitle.setText("Available System Categories List");
            btnDeleteCategory.setText("DELETE SELECTED CATEGORY");
            lblCategoryTitle.setText("Add New Expense / Income Category");
            lblCatNameHint.setText("New Category Name:"); lblCatIconHint.setText("Select Representative Icon/Emoji:"); lblCatTypeHint.setText("Category Type:"); btnSaveCategory.setText("CONFIRM ADD CATEGORY");
            comboCategoryType.setModel(new DefaultComboBoxModel<>(new String[]{"Expense (CHI TIÊU)", "Income (THU NHẬP)"}));
        }
        if (currentTypeIndex >= 0 && currentTypeIndex < comboCategoryType.getItemCount()) comboCategoryType.setSelectedIndex(currentTypeIndex);

        // Ép danh sách hiển thị cập nhật ngay ngôn ngữ mới
        if (categoryList != null) {
            categoryList.repaint();
        }
        refreshEmojiGrid();
    }

    public void refreshCategories() {
        listModel.clear();
        List<Category> categories = DatabaseUtil.getAllCategories();
        for (Category c : categories) {
            if (c != null) listModel.addElement(c);
        }
    }

    private void deleteCategory() {
        Category selected = categoryList.getSelectedValue();
        if (selected != null) {
            int confirm = JOptionPane.showConfirmDialog(this, (isVietnamese ? "Xóa danh mục \"" : "Delete category \"") + selected.getName() + "\"?", (isVietnamese ? "Xác nhận" : "Confirm"), JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                DatabaseUtil.deleteCategory(selected.getId());
                refreshCategories();
            }
        } else {
            JOptionPane.showMessageDialog(this, isVietnamese ? "Vui lòng chọn một danh mục để xóa." : "Please select a category to delete.");
        }
    }

    private void refreshEmojiGrid() {
        if (emojiGridPanel == null) return;
        emojiGridPanel.removeAll();
        int totalItems = EMOJI_LIST.length;
        int totalPages = (int) Math.ceil((double) totalItems / EMOJI_PER_PAGE);
        if (currentEmojiPage > totalPages) currentEmojiPage = totalPages;
        lblEmojiPageIndicator.setText((isVietnamese ? "Trang " : "Page ") + currentEmojiPage + " / " + totalPages);
        btnPrevEmojiPage.setEnabled(currentEmojiPage > 1); btnNextEmojiPage.setEnabled(currentEmojiPage < totalPages);

        int startIndex = (currentEmojiPage - 1) * EMOJI_PER_PAGE;
        int endIndex = Math.min(startIndex + EMOJI_PER_PAGE, totalItems);
        int displayedCount = 0;
        for (int i = startIndex; i < endIndex; i++) {
            emojiGridPanel.add(createEmojiCellComponent(EMOJI_LIST[i])); displayedCount++;
        }
        for (int i = displayedCount; i < EMOJI_PER_PAGE; i++) {
            JPanel placeholder = new JPanel(); placeholder.setOpaque(false); emojiGridPanel.add(placeholder);
        }
        emojiGridPanel.revalidate(); emojiGridPanel.repaint();
    }

    private JPanel createEmojiCellComponent(String emoji) {
        JPanel cell = new JPanel(new BorderLayout());
        cell.setPreferredSize(new Dimension(getEmojiCellWidth(), getEmojiCellHeight()));
        cell.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel lbl = new JLabel(emoji, SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        cell.add(lbl, BorderLayout.CENTER);

        if (emoji.equals(selectedEmoji)) {
            cell.setBackground(ACCENT_YELLOW);
            cell.setBorder(BorderFactory.createLineBorder(ACCENT_YELLOW, 1, true));
        } else {
            cell.setBackground(INPUT_BG);
            cell.setBorder(BorderFactory.createLineBorder(new Color(55, 55, 55), 1, true));
        }
        cell.addMouseListener(new MouseAdapter() { @Override public void mouseClicked(MouseEvent e) { selectedEmoji = emoji; refreshEmojiGrid(); } });
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
            this.selectedEmoji = "\uD83D\uDCCD"; this.currentEmojiPage = 1;
            refreshCategories();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error DB: " + ex.getMessage(), "System Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JLabel createLabel() { JLabel lbl = new JLabel(); lbl.setFont(new Font("Segoe UI", Font.BOLD, 14)); lbl.setForeground(TEXT_SECONDARY); lbl.setBorder(BorderFactory.createEmptyBorder(0, 2, 6, 0)); lbl.setAlignmentX(Component.LEFT_ALIGNMENT); return lbl; }
    private void styleTextField(JTextField tf) { tf.setBackground(INPUT_BG); tf.setForeground(TEXT_PRIMARY); tf.setCaretColor(ACCENT_YELLOW); tf.setFont(new Font("Segoe UI", Font.PLAIN, 15)); tf.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(60, 60, 60)), BorderFactory.createEmptyBorder(10, 15, 10, 15))); }
    private JButton createPaginationButton(String text) { JButton btn = new JButton(text); btn.setFont(new Font("Segoe UI", Font.BOLD, 14)); btn.setForeground(TEXT_PRIMARY); btn.setBackground(INPUT_BG); btn.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60), 1)); btn.setPreferredSize(new Dimension(36, 28)); btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); return btn; }

    private class CategoryCellRenderer extends JPanel implements ListCellRenderer<Category> {
        private final JLabel lblEmoji = new JLabel();
        private final JLabel lblText = new JLabel();

        public CategoryCellRenderer() {
            setLayout(new FlowLayout(FlowLayout.LEFT, 10, 4));
            setOpaque(true);
            lblEmoji.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 15));
            lblText.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            add(lblEmoji);
            add(lblText);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Category> list, Category value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value != null) {
                String emoji = EmojiUtil.CATEGORY_EMOJI.getOrDefault(value.getName(), "\uD83D\uDCCD");
                lblEmoji.setText(emoji);

                String typeStr = (value.getType() == TransactionType.INCOME) ?
                        (isVietnamese ? "Thu" : "Income") : (isVietnamese ? "Chi" : "Expense");
                lblText.setText(value.getName() + " (" + typeStr + ")");
            }

            if (isSelected) {
                setBackground(list.getSelectionBackground());
                lblEmoji.setForeground(list.getSelectionForeground());
                lblText.setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                lblEmoji.setForeground(list.getForeground());
                lblText.setForeground(list.getForeground());
            }
            return this;
        }
    }
}