package com.expensemanager.ui;

import com.expensemanager.database.DatabaseUtil;
import com.expensemanager.entity.Category;
import com.expensemanager.entity.TransactionType;
import com.expensemanager.util.EmojiUtil;
import com.expensemanager.util.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.UUID;

public class CategoryManagerPanel extends JPanel {

    // =====================================================================
    // 1. KHAI BÁO BIẾN GIAO DIỆN VÀ LOGIC
    // =====================================================================
    private MainFrame mainFrame;
    private boolean isVietnamese;

    private JPanel listCard;
    private JLabel lblListTitle, lblListPageIndicator;
    private JPanel listGridPanel, listGridWrapper, listPaginationPanel;
    private JButton btnPrevListPage, btnNextListPage;
    private JButton btnDeleteCategory;
    private Category selectedCategoryForDelete;

    private JButton btnListExpense, btnListIncome;
    private TransactionType currentListType = TransactionType.EXPENSE;
    private int currentListPage = 1;
    private final int LIST_PER_PAGE = 18;

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
    
    // Preferred emoji fonts to try (platform dependent)
    private static final String[] EMOJI_FALLBACK_FONTS = new String[]{
            "Segoe UI Emoji", "Noto Color Emoji", "Apple Color Emoji", "EmojiOne Color", "Segoe UI Symbol", "Symbola"
    };

    // =====================================================================
    // 2. CONSTRUCTOR - KHỞI TẠO BỐ CỤC CHÍNH
    // =====================================================================
    public CategoryManagerPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.isVietnamese = mainFrame != null && mainFrame.isVietnamese();

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);

        initComponentsListCard();
        add(Box.createVerticalStrut(20));
        initComponentsAddCard();

        updateResponsiveLayout(isVietnamese, 560);
    }

    // =====================================================================
    // 3. XÂY DỰNG GIAO DIỆN THÀNH PHẦN (UI COMPONENTS)
    // =====================================================================
    private void initComponentsListCard() {
        listCard = new JPanel(new BorderLayout(0, 10));
        listCard.setBackground(SURFACE_COLOR);
        listCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(45, 45, 45), 1, true),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        listCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel listHeaderPanel = new JPanel(new BorderLayout());
        listHeaderPanel.setOpaque(false);

        lblListTitle = new JLabel();
        lblListTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblListTitle.setForeground(ACCENT_YELLOW);
        listHeaderPanel.add(lblListTitle, BorderLayout.WEST);

        JPanel listTypeTabsRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        listTypeTabsRow.setOpaque(false);

        btnListExpense = createListTypeTabButton("Khoản chi", true);
        btnListIncome = createListTypeTabButton("Khoản thu", false);

        btnListExpense.addActionListener(e -> {
            currentListType = TransactionType.EXPENSE;
            currentListPage = 1;
            selectedCategoryForDelete = null;
            selectListTypeTab(btnListExpense);
            refreshCategories();
        });

        btnListIncome.addActionListener(e -> {
            currentListType = TransactionType.INCOME;
            currentListPage = 1;
            selectedCategoryForDelete = null;
            selectListTypeTab(btnListIncome);
            refreshCategories();
        });

        listTypeTabsRow.add(btnListExpense);
        listTypeTabsRow.add(btnListIncome);
        listHeaderPanel.add(listTypeTabsRow, BorderLayout.EAST);
        listCard.add(listHeaderPanel, BorderLayout.NORTH);

        listGridPanel = new JPanel(new GridLayout(2, 9, 8, 8));
        listGridPanel.setBackground(SURFACE_COLOR);

        listGridWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        listGridWrapper.setOpaque(false);
        listGridWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        listGridWrapper.add(listGridPanel);

        listPaginationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        listPaginationPanel.setOpaque(false);
        listPaginationPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnPrevListPage = createPaginationButton("<");
        btnNextListPage = createPaginationButton(">");
        lblListPageIndicator = new JLabel();
        lblListPageIndicator.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblListPageIndicator.setForeground(TEXT_PRIMARY);
        lblListPageIndicator.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));

        btnPrevListPage.addActionListener(e -> { if (currentListPage > 1) { currentListPage--; refreshCategories(); } });
        btnNextListPage.addActionListener(e -> { currentListPage++; refreshCategories(); });

        listPaginationPanel.add(btnPrevListPage);
        listPaginationPanel.add(lblListPageIndicator);
        listPaginationPanel.add(btnNextListPage);

        JPanel listCenterContainer = new JPanel();
        listCenterContainer.setLayout(new BoxLayout(listCenterContainer, BoxLayout.Y_AXIS));
        listCenterContainer.setOpaque(false);
        listCenterContainer.add(listGridWrapper);
        listCenterContainer.add(Box.createVerticalStrut(8));
        listCenterContainer.add(listPaginationPanel);
        listCard.add(listCenterContainer, BorderLayout.CENTER);

        btnDeleteCategory = new JButton();
        btnDeleteCategory.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnDeleteCategory.setBackground(INPUT_BG); btnDeleteCategory.setForeground(DANGER_RED);
        btnDeleteCategory.setFocusPainted(false); btnDeleteCategory.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnDeleteCategory.setBorder(BorderFactory.createLineBorder(DANGER_RED, 1, true));
        btnDeleteCategory.addActionListener(e -> deleteCategory());
        listCard.add(btnDeleteCategory, BorderLayout.SOUTH);

        add(listCard);
    }

    private void initComponentsAddCard() {
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

        emojiGridPanel = new JPanel(new GridLayout(2, 9, 8, 8));
        emojiGridPanel.setBackground(SURFACE_COLOR);

        gridWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        gridWrapper.setOpaque(false);
        gridWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        gridWrapper.add(emojiGridPanel);
        gbc.gridy = 4; pForm.add(gridWrapper, gbc);

        emojiPagination = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        emojiPagination.setOpaque(false);
        emojiPagination.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnPrevEmojiPage = createPaginationButton("<");
        btnNextEmojiPage = createPaginationButton(">");
        lblEmojiPageIndicator = new JLabel();
        lblEmojiPageIndicator.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblEmojiPageIndicator.setForeground(TEXT_PRIMARY);
        lblEmojiPageIndicator.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));

        btnPrevEmojiPage.addActionListener(e -> { if (currentEmojiPage > 1) { currentEmojiPage--; refreshEmojiGrid(); } });
        btnNextEmojiPage.addActionListener(e -> { currentEmojiPage++; refreshEmojiGrid(); });

        emojiPagination.add(btnPrevEmojiPage);
        emojiPagination.add(lblEmojiPageIndicator);
        emojiPagination.add(btnNextEmojiPage);
        gbc.gridy = 5; pForm.add(emojiPagination, gbc);

        lblCatTypeHint = createLabel(); gbc.gridy = 6; pForm.add(lblCatTypeHint, gbc);

        comboCategoryType = new JComboBox<>();
        comboCategoryType.setBackground(INPUT_BG);
        comboCategoryType.setForeground(TEXT_PRIMARY);
        comboCategoryType.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        comboCategoryType.setAlignmentX(Component.LEFT_ALIGNMENT);
        gbc.gridy = 7; pForm.add(comboCategoryType, gbc);

        btnSaveCategory = new JButton();
        btnSaveCategory.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnSaveCategory.setBackground(ACCENT_YELLOW);
        btnSaveCategory.setForeground(SURFACE_COLOR);
        btnSaveCategory.setFocusPainted(false);
        btnSaveCategory.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSaveCategory.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnSaveCategory.addActionListener(e -> executeAddCategory());

        gbc.gridy = 8; gbc.insets = new Insets(12, 0, 0, 0); pForm.add(btnSaveCategory, gbc);

        addCard.add(pForm, BorderLayout.CENTER);
        add(addCard);
    }

    // =====================================================================
    // 4. XỬ LÝ LOGIC NGHIỆP VỤ & ĐỒNG BỘ UI
    // =====================================================================
    public void refreshCategories() {
        if (listGridPanel == null) return;
        listGridPanel.removeAll();

        List<Category> categories = DatabaseUtil.getAllCategories();
        List<Category> filteredCategories = new java.util.ArrayList<>();
        for (Category c : categories) {
            if (c != null && c.getType() == currentListType) {
                filteredCategories.add(c);
            }
        }

        int totalItems = filteredCategories.size();
        int totalPages = (int) Math.ceil((double) totalItems / LIST_PER_PAGE);
        if (totalPages == 0) totalPages = 1;

        if (currentListPage > totalPages) currentListPage = totalPages;
        if (currentListPage < 1) currentListPage = 1;

        lblListPageIndicator.setText((isVietnamese ? "Trang " : "Page ") + currentListPage + " / " + totalPages);
        btnPrevListPage.setEnabled(currentListPage > 1);
        btnNextListPage.setEnabled(currentListPage < totalPages);

        int startIndex = (currentListPage - 1) * LIST_PER_PAGE;
        int endIndex = Math.min(startIndex + LIST_PER_PAGE, totalItems);

        int displayedCount = 0;
        for (int i = startIndex; i < endIndex; i++) {
            listGridPanel.add(createCategoryListCellComponent(filteredCategories.get(i)));
            displayedCount++;
        }

        for (int i = displayedCount; i < LIST_PER_PAGE; i++) {
            JPanel placeholder = new JPanel(); placeholder.setOpaque(false); listGridPanel.add(placeholder);
        }

        listGridPanel.revalidate();
        listGridPanel.repaint();
    }

    private JPanel createCategoryListCellComponent(Category c) {
        JPanel cell = new JPanel(new BorderLayout(0, 4));
        cell.setOpaque(false);
        cell.setPreferredSize(new Dimension(getEmojiCellWidth(), getEmojiCellHeight()));
        cell.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        String emoji = EmojiUtil.CATEGORY_EMOJI.getOrDefault(c.getName(), "\uD83D\uDCCD");

        // Try to load a PNG icon resource for this category first (resources/icons/categories/<key>.png)
        int iconSize = 36;
        ImageIcon fileIcon = loadCategoryIcon(c.getName(), iconSize);
        JLabel lblIcon = new JLabel((fileIcon != null) ? fileIcon : null, SwingConstants.CENTER);
        if (fileIcon != null) {
            lblIcon.setOpaque(true);
        } else {
            // Fallback: emoji glyph or first letter
            Font ef = emojiFont(20);
            String iconText = canDisplayText(emoji, ef) ? emoji : ((c.getName() != null && c.getName().length() > 0) ? c.getName().substring(0,1).toUpperCase() : "?");
            lblIcon.setText(iconText);
            lblIcon.setFont(ef);
            lblIcon.setOpaque(true);
        }

        if (selectedCategoryForDelete != null && selectedCategoryForDelete.getId().equals(c.getId())) {
            lblIcon.setBackground(ThemeManager.getColor("accent"));
            lblIcon.setBorder(BorderFactory.createLineBorder(ThemeManager.getColor("accent"), 1, true));
            lblIcon.setForeground(ThemeManager.getColor("bg"));
        } else {
            lblIcon.setBackground(ThemeManager.getColor("input"));
            lblIcon.setBorder(BorderFactory.createLineBorder(ThemeManager.getColor("border"), 1, true));
            lblIcon.setForeground(ThemeManager.getColor("textPrimary"));
        }

        String displayName = c.getName();
        if (displayName != null && displayName.length() > 8) {
            displayName = displayName.substring(0, 6) + "...";
        }

        JLabel lblName = new JLabel(displayName, SwingConstants.CENTER);
        lblName.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblName.setForeground(ThemeManager.getColor("textPrimary"));

        cell.add(lblIcon, BorderLayout.CENTER);
        cell.add(lblName, BorderLayout.SOUTH);

        cell.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectedCategoryForDelete = c;
                refreshCategories();
            }
        });
        return cell;
    }

    private ImageIcon loadCategoryIcon(String categoryName, int size) {
        if (categoryName == null) return null;
        try {
            String key = sanitizeKey(categoryName);
            String path = "/icons/categories/" + key + ".png";
            java.net.URL res = getClass().getResource(path);
            if (res == null) return null;
            ImageIcon ico = new ImageIcon(res);
            Image img = ico.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
            return new ImageIcon(img);
        } catch (Throwable t) {
            return null;
        }
    }

    private String sanitizeKey(String name) {
        if (name == null) return "";
        String s = name.trim().toLowerCase();
        s = s.replaceAll("[^a-z0-9]+", "_");
        if (s.startsWith("_")) s = s.substring(1);
        if (s.endsWith("_")) s = s.substring(0, s.length()-1);
        if (s.isEmpty()) s = "unknown";
        return s;
    }

    private void deleteCategory() {
        if (selectedCategoryForDelete != null) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    (isVietnamese ? "Xóa danh mục \"" : "Delete category \"") + selectedCategoryForDelete.getName() + "\"?",
                    (isVietnamese ? "Xác nhận" : "Confirm"), JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                DatabaseUtil.deleteCategory(selectedCategoryForDelete.getId());
                selectedCategoryForDelete = null;
                refreshCategories();
                if (mainFrame != null) mainFrame.refreshAllPanels();
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
        cell.setOpaque(true);
        cell.setPreferredSize(new Dimension(getEmojiCellWidth(), getEmojiCellHeight()));
        cell.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Fallback to simple marker if emoji cannot be rendered on this system
        String display = canDisplayText(emoji, emojiFont(20)) ? emoji : "?";
        JLabel lbl = new JLabel(display, SwingConstants.CENTER);
        lbl.setFont(emojiFont(20));
        lbl.setOpaque(false);
        cell.add(lbl, BorderLayout.CENTER);

        if (emoji.equals(selectedEmoji)) {
            cell.setBackground(ThemeManager.getColor("accent"));
            cell.setBorder(BorderFactory.createLineBorder(ThemeManager.getColor("accent"), 1, true));
            lbl.setForeground(ThemeManager.getColor("bg"));
        } else {
            cell.setBackground(ThemeManager.getColor("input"));
            cell.setBorder(BorderFactory.createLineBorder(ThemeManager.getColor("border"), 1, true));
            lbl.setForeground(ThemeManager.getColor("textPrimary"));
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

    private Font emojiFont(int size) {
        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            String[] available = ge.getAvailableFontFamilyNames();
            for (String candidate : EMOJI_FALLBACK_FONTS) {
                for (String fam : available) {
                    if (fam.equalsIgnoreCase(candidate)) {
                        return new Font(fam, Font.PLAIN, size);
                    }
                }
            }
        } catch (Exception ignored) {}
        // fallback to default font
        return new Font(Font.SANS_SERIF, Font.PLAIN, size);
    }

    private boolean canDisplayText(String text, Font font) {
        if (text == null || text.isEmpty()) return false;
        try {
            for (int i = 0; i < text.length(); ) {
                int cp = text.codePointAt(i);
                if (!font.canDisplay(cp)) return false;
                i += Character.charCount(cp);
            }
            return true;
        } catch (Throwable t) {
            return false;
        }
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
            if (mainFrame != null) mainFrame.refreshAllPanels();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error DB: " + ex.getMessage(), "System Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // =====================================================================
    // 5. CÁC HÀM TIỆN ÍCH, RESPONSIVE, STYLE CHUNG
    // =====================================================================
    private int getEmojiCellWidth() { return (currentFluidWidth - 48 - 64) / 9; }
    private int getEmojiCellHeight() { int w = getEmojiCellWidth(); return w >= 85 ? 76 : (w >= 70 ? 62 : 48); }

    public void updateResponsiveLayout(boolean isVN, int fluidWidth) {
        this.isVietnamese = isVN;
        this.currentFluidWidth = fluidWidth;
        int currentTypeIndex = comboCategoryType.getSelectedIndex();
        int gridH = (getEmojiCellHeight() * 2) + 12;

        setMaximumSize(new Dimension(fluidWidth, Integer.MAX_VALUE));

        int totalListCardH = 42 + gridH + 8 + 32 + 10 + 36 + 15;
        if (listCard != null) {
            listCard.setPreferredSize(new Dimension(fluidWidth, totalListCardH));
            listCard.setMaximumSize(new Dimension(fluidWidth, totalListCardH));
            listCard.setMinimumSize(new Dimension(fluidWidth, totalListCardH));
        }
        if (listGridPanel != null) {
            listGridPanel.setPreferredSize(new Dimension(fluidWidth - 48, gridH));
            listGridPanel.setMaximumSize(new Dimension(fluidWidth - 48, gridH));
            listGridPanel.setMinimumSize(new Dimension(fluidWidth - 48, gridH));
        }
        if (listGridWrapper != null) {
            listGridWrapper.setPreferredSize(new Dimension(fluidWidth - 48, gridH));
            listGridWrapper.setMaximumSize(new Dimension(fluidWidth - 48, gridH));
        }
        if (listPaginationPanel != null) {
            listPaginationPanel.setPreferredSize(new Dimension(fluidWidth - 48, 32));
            listPaginationPanel.setMaximumSize(new Dimension(fluidWidth - 48, 32));
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
            if (btnListExpense != null) btnListExpense.setText("Khoản chi");
            if (btnListIncome != null) btnListIncome.setText("Khoản thu");
            btnDeleteCategory.setText("XÓA DANH MỤC ĐANG CHỌN");
            lblCategoryTitle.setText("Thêm danh mục chi tiêu / thu nhập mới");
            lblCatNameHint.setText("Tên danh mục mới:"); lblCatIconHint.setText("Chọn Icon/Emoji đại diện:"); lblCatTypeHint.setText("Phân loại danh mục:"); btnSaveCategory.setText("XÁC NHẬN THÊM DANH MỤC");
            comboCategoryType.setModel(new DefaultComboBoxModel<>(new String[]{"Khoản chi tiêu (EXPENSE)", "Khoản thu nhập (INCOME)"}));
        } else {
            lblListTitle.setText("Available System Categories List");
            if (btnListExpense != null) btnListExpense.setText("Expenses");
            if (btnListIncome != null) btnListIncome.setText("Incomes");
            btnDeleteCategory.setText("DELETE SELECTED CATEGORY");
            lblCategoryTitle.setText("Add New Expense / Income Category");
            lblCatNameHint.setText("New Category Name:"); lblCatIconHint.setText("Select Representative Icon/Emoji:"); lblCatTypeHint.setText("Category Type:"); btnSaveCategory.setText("CONFIRM ADD CATEGORY");
            comboCategoryType.setModel(new DefaultComboBoxModel<>(new String[]{"Expense (CHI TIÊU)", "Income (THU NHẬP)"}));
        }

        if (btnListExpense != null && btnListIncome != null) {
            btnListExpense.setBackground(currentListType == TransactionType.EXPENSE ? ThemeManager.getColor("accent") : ThemeManager.getColor("input"));
            btnListExpense.setForeground(currentListType == TransactionType.EXPENSE ? ThemeManager.getColor("bg") : ThemeManager.getColor("textPrimary"));
            btnListIncome.setBackground(currentListType == TransactionType.INCOME ? ThemeManager.getColor("accent") : ThemeManager.getColor("input"));
            btnListIncome.setForeground(currentListType == TransactionType.INCOME ? ThemeManager.getColor("bg") : ThemeManager.getColor("textPrimary"));
        }

        if (currentTypeIndex >= 0 && currentTypeIndex < comboCategoryType.getItemCount()) comboCategoryType.setSelectedIndex(currentTypeIndex);

        refreshCategories();
        refreshEmojiGrid();
    }

    private void selectListTypeTab(JButton target) {
        btnListExpense.setBackground(ThemeManager.getColor("input"));
        btnListExpense.setForeground(ThemeManager.getColor("textPrimary"));
        btnListIncome.setBackground(ThemeManager.getColor("input"));
        btnListIncome.setForeground(ThemeManager.getColor("textPrimary"));
        target.setBackground(ThemeManager.getColor("accent"));
        target.setForeground(ThemeManager.getColor("bg"));
    }

    private JButton createListTypeTabButton(String text, boolean active) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBackground(active ? ThemeManager.getColor("accent") : ThemeManager.getColor("input"));
        btn.setForeground(active ? ThemeManager.getColor("bg") : ThemeManager.getColor("textPrimary"));
        btn.setBorder(BorderFactory.createEmptyBorder(5, 14, 5, 14));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public void applyTheme() {
        setBackground(ThemeManager.getColor("bg"));
        if (listCard != null) {
            listCard.setBackground(ThemeManager.getColor("surface"));
            listCard.setBorder(BorderFactory.createLineBorder(ThemeManager.getColor("border"), 1, true));
        }
        if (lblListTitle != null) lblListTitle.setForeground(ThemeManager.getColor("accent"));
        if (listGridPanel != null) {
            listGridPanel.setBackground(ThemeManager.getColor("surface"));
        }
        if (listGridWrapper != null) {
            listGridWrapper.setBackground(ThemeManager.getColor("surface"));
        }
        if (listPaginationPanel != null) {
            listPaginationPanel.setBackground(ThemeManager.getColor("surface"));
        }
        if (addCard != null) {
            addCard.setBackground(ThemeManager.getColor("surface"));
            addCard.setBorder(BorderFactory.createLineBorder(ThemeManager.getColor("border"), 1, true));
        }
        if (lblCategoryTitle != null) lblCategoryTitle.setForeground(ThemeManager.getColor("accent"));
        if (lblCatNameHint != null) lblCatNameHint.setForeground(ThemeManager.getColor("textSecondary"));
        if (lblCatIconHint != null) lblCatIconHint.setForeground(ThemeManager.getColor("textSecondary"));
        if (lblCatTypeHint != null) lblCatTypeHint.setForeground(ThemeManager.getColor("textSecondary"));
        if (txtCategoryName != null) {
            txtCategoryName.setBackground(ThemeManager.getColor("input"));
            txtCategoryName.setForeground(ThemeManager.getColor("textPrimary"));
        }
        if (emojiGridPanel != null) {
            emojiGridPanel.setBackground(ThemeManager.getColor("surface"));
        }
        if (gridWrapper != null) {
            gridWrapper.setBackground(ThemeManager.getColor("surface"));
        }
        if (emojiPagination != null) {
            emojiPagination.setBackground(ThemeManager.getColor("surface"));
        }
        if (comboCategoryType != null) {
            comboCategoryType.setBackground(ThemeManager.getColor("input"));
            comboCategoryType.setForeground(ThemeManager.getColor("textPrimary"));
        }
        if (btnSaveCategory != null) {
            btnSaveCategory.setBackground(ThemeManager.getColor("accent"));
            btnSaveCategory.setForeground(ThemeManager.getColor("bg"));
        }
        if (btnDeleteCategory != null) {
            btnDeleteCategory.setBackground(ThemeManager.getColor("input"));
            btnDeleteCategory.setForeground(ThemeManager.getColor("danger"));
        }
        refreshCategories();
        refreshEmojiGrid();
    }

    private JLabel createLabel() {
        JLabel lbl = new JLabel();
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(ThemeManager.getColor("textSecondary"));
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 2, 6, 0));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }
    private void styleTextField(JTextField tf) {
        tf.setBackground(ThemeManager.getColor("input"));
        tf.setForeground(ThemeManager.getColor("textPrimary"));
        tf.setCaretColor(ThemeManager.getColor("accent"));
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.getColor("border")),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
    }
    private JButton createPaginationButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(ThemeManager.getColor("textPrimary"));
        btn.setBackground(ThemeManager.getColor("input"));
        btn.setBorder(BorderFactory.createLineBorder(ThemeManager.getColor("border"), 1));
        btn.setPreferredSize(new Dimension(36, 28));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}