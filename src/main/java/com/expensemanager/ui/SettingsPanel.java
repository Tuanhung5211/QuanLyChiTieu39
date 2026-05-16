package com.expensemanager.ui;

import com.expensemanager.database.DatabaseUtil;
import com.expensemanager.entity.Category;
import com.expensemanager.entity.TransactionType;
import com.expensemanager.entity.User;
import com.expensemanager.service.SessionManager;
import com.expensemanager.service.UserService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.UUID;

public class SettingsPanel extends JPanel {
    private MainFrame mainFrame;
    private boolean isVietnamese = true;

    // Bộ quản lý Layout chuyển đổi các mục cài đặt nhỏ nội bộ
    private CardLayout subCardLayout;
    private JPanel subContentPanel;

    // Các thành phần giao diện thay đổi văn bản động khi đổi ngôn ngữ
    private JLabel lblMainTitle, lblProfileTitle, lblLanguageTitle, lblCategoryTitle, lblSizeTitle;
    private JLabel lblNickname, lblEmail, lblGender;
    private JLabel lblLangHint, lblCatNameHint, lblCatTypeHint, lblCatIconHint, lblSizeHint;
    private JButton btnLogout, btnAccountTab, btnLanguageTab, btnCategoryTab, btnSizeTab;
    private JButton btnUpdateProfile, btnOpenChangePass, btnSaveCategory, btnDeleteAccount, btnSaveLanguage, btnSaveSize;
    private JRadioButton rbVietnamese, rbEnglish;

    // Các trường nhập liệu mục Thông tin tài khoản
    private JTextField txtNickname;
    private JTextField txtEmail;
    private JComboBox<String> cmbGender;

    // Các trường phục vụ Dialog đổi mật khẩu riêng biệt
    private JPasswordField txtOldPassword;
    private JPasswordField txtNewPassword;
    private JPasswordField txtConfirmPassword;

    // Các trường nhập liệu mục Thêm Danh mục
    private JTextField txtCategoryName;
    private JComboBox<String> comboCategoryType;

    // 🌟 THÀNH PHẦN MỚI: Bộ chọn kích thước độ phân giải ứng dụng cố định
    private JComboBox<String> comboWindowSize;

    // Các cấu phần phục vụ lưới phân trang Emoji 9x2
    private JPanel emojiGridPanel;
    private JButton btnPrevEmojiPage, btnNextEmojiPage;
    private JLabel lblEmojiPageIndicator;
    private String selectedEmoji = "📌";
    private int currentEmojiPage = 1;
    private final int EMOJI_PER_PAGE = 18;

    private final String[] EMOJI_LIST = {
            "📌", "🍔", "🛒", "🛍️", "🍿", "🍎", "👗", "💻", "⛽", "🏍️",
            "🚗", "⚡", "🌐", "🏢", "📱", "🎮", "🎬", "✈️", "📚", "🏥",
            "💊", "💄", "⚽", "🐱", "🎁", "💖", "🔧", "🏠", "🛡️", "💰",
            "💵", "🎓", "💼", "📈", "🐷", "🪙"
    };

    // Hệ màu sắc phẳng Flat Dark Mode đồng bộ app chính
    private final Color BG_COLOR = new Color(18, 18, 18);
    private final Color SURFACE_COLOR = new Color(30, 30, 30);
    private final Color INPUT_BG = new Color(40, 40, 40);
    private final Color TEXT_PRIMARY = new Color(240, 240, 240);
    private final Color TEXT_SECONDARY = new Color(150, 150, 150);
    private final Color ACCENT_YELLOW = new Color(255, 193, 7);
    private final Color DANGER_RED = new Color(244, 67, 54);

    public SettingsPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;

        if (mainFrame != null) {
            this.isVietnamese = mainFrame.isVietnamese();
        }

        setLayout(new BorderLayout());
        setBackground(BG_COLOR);
        setBorder(BorderFactory.createEmptyBorder(25, 35, 25, 35));

        // --- KHU VỰC HEADER TRÊN CÙNG ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        lblMainTitle = new JLabel();
        lblMainTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblMainTitle.setForeground(TEXT_PRIMARY);
        headerPanel.add(lblMainTitle, BorderLayout.WEST);

        btnLogout = new JButton();
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogout.setBackground(SURFACE_COLOR);
        btnLogout.setForeground(DANGER_RED);
        btnLogout.setFocusPainted(false);
        btnLogout.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.addActionListener(e -> logout());
        headerPanel.add(btnLogout, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // --- KHU VỰC THÂN TRANG (SIDEBAR TRÁI - NỘI DUNG PHẢI) ---
        JPanel bodyContainer = new JPanel(new BorderLayout(25, 0));
        bodyContainer.setOpaque(false);
        bodyContainer.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        // 1. Sidebar điều hướng phụ bên trái (BỔ SUNG MỤC KÍCH THƯỚC)
        JPanel sidebarPanel = new JPanel();
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBackground(SURFACE_COLOR);
        sidebarPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(45, 45, 45), 1, true),
                BorderFactory.createEmptyBorder(15, 12, 15, 12)
        ));
        sidebarPanel.setPreferredSize(new Dimension(220, 0));

        btnAccountTab = createSubNavButton("", true);
        btnLanguageTab = createSubNavButton("", false);
        btnSizeTab = createSubNavButton("", false); // 🌟 Thêm Tab kích thước
        btnCategoryTab = createSubNavButton("", false);

        btnAccountTab.addActionListener(e -> switchSubTab("account", btnAccountTab));
        btnLanguageTab.addActionListener(e -> switchSubTab("language", btnLanguageTab));
        btnSizeTab.addActionListener(e -> switchSubTab("size", btnSizeTab)); // 🌟 Event chuyển Tab
        btnCategoryTab.addActionListener(e -> switchSubTab("category", btnCategoryTab));

        sidebarPanel.add(btnAccountTab);
        sidebarPanel.add(Box.createVerticalStrut(10));
        sidebarPanel.add(btnLanguageTab);
        sidebarPanel.add(Box.createVerticalStrut(10));
        sidebarPanel.add(btnSizeTab); // 🌟 Nạp vào giao diện sidebar
        sidebarPanel.add(Box.createVerticalStrut(10));
        sidebarPanel.add(btnCategoryTab);
        sidebarPanel.add(Box.createVerticalGlue());
        bodyContainer.add(sidebarPanel, BorderLayout.WEST);

        // 2. Khung nội dung CardLayout lật trang bên phải
        subCardLayout = new CardLayout();
        subContentPanel = new JPanel(subCardLayout);
        subContentPanel.setOpaque(false);

        cmbGender = new JComboBox<>();
        comboCategoryType = new JComboBox<>();
        comboWindowSize = new JComboBox<>(); // 🌟 Khởi tạo Combobox kích thước

        subContentPanel.add(createResponsiveWrapper(createAccountSubPanel()), "account");
        subContentPanel.add(createResponsiveWrapper(createLanguageSubPanel()), "language");
        subContentPanel.add(createResponsiveWrapper(createSizeSubPanel()), "size"); // 🌟 Nạp Panel Kích thước
        subContentPanel.add(createResponsiveWrapper(createCategorySubPanel()), "category");

        JScrollPane scrollPane = new JScrollPane(subContentPanel);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        bodyContainer.add(scrollPane, BorderLayout.CENTER);

        add(bodyContainer, BorderLayout.CENTER);

        updateLanguageText();
        refreshData();
    }

    private JPanel createResponsiveWrapper(JPanel targetPanel) {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 1.0; gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        wrapper.add(targetPanel, gbc);

        gbc.gridy = 1; gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        JPanel filler = new JPanel(); filler.setOpaque(false);
        wrapper.add(filler, gbc);
        return wrapper;
    }

    private JPanel createAccountSubPanel() {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setOpaque(false);
        container.setMaximumSize(new Dimension(580, Integer.MAX_VALUE));

        JPanel profileCard = new JPanel(new BorderLayout(0, 15));
        profileCard.setBackground(SURFACE_COLOR);
        profileCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(45, 45, 45), 1, true),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        lblProfileTitle = new JLabel();
        lblProfileTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblProfileTitle.setForeground(ACCENT_YELLOW);
        profileCard.add(lblProfileTitle, BorderLayout.NORTH);

        JPanel pForm = new JPanel(new GridBagLayout());
        pForm.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.25;
        lblNickname = new JLabel(); lblNickname.setFont(new Font("Segoe UI", Font.PLAIN, 15)); lblNickname.setForeground(TEXT_SECONDARY);
        pForm.add(lblNickname, gbc);
        gbc.gridx = 1; gbc.weightx = 0.75;
        txtNickname = new JTextField(); styleTextField(txtNickname);
        pForm.add(txtNickname, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.25;
        lblEmail = new JLabel(); lblEmail.setFont(new Font("Segoe UI", Font.PLAIN, 15)); lblEmail.setForeground(TEXT_SECONDARY);
        pForm.add(lblEmail, gbc);
        gbc.gridx = 1; gbc.weightx = 0.75;
        txtEmail = new JTextField(); styleTextField(txtEmail);
        pForm.add(txtEmail, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.25;
        lblGender = new JLabel(); lblGender.setFont(new Font("Segoe UI", Font.PLAIN, 15)); lblGender.setForeground(TEXT_SECONDARY);
        pForm.add(lblGender, gbc);
        gbc.gridx = 1; gbc.weightx = 0.75;
        cmbGender.setBackground(INPUT_BG); cmbGender.setForeground(TEXT_PRIMARY); cmbGender.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        pForm.add(cmbGender, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; gbc.insets = new Insets(15, 8, 5, 8);
        btnUpdateProfile = new JButton(); stylePrimaryButton(btnUpdateProfile);
        btnUpdateProfile.addActionListener(e -> updateProfile());
        pForm.add(btnUpdateProfile, gbc);

        gbc.gridy = 4;
        btnOpenChangePass = new JButton();
        btnOpenChangePass.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnOpenChangePass.setBackground(INPUT_BG);
        btnOpenChangePass.setForeground(TEXT_PRIMARY);
        btnOpenChangePass.setFocusPainted(false);
        btnOpenChangePass.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnOpenChangePass.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(65, 65, 65), 1, true),
                BorderFactory.createEmptyBorder(10, 0, 10, 0)
        ));
        btnOpenChangePass.addActionListener(e -> openChangePasswordDialog());
        pForm.add(btnOpenChangePass, gbc);

        profileCard.add(pForm, BorderLayout.CENTER);
        container.add(profileCard);
        container.add(Box.createVerticalStrut(20));

        btnDeleteAccount = new JButton();
        btnDeleteAccount.setBackground(SURFACE_COLOR); btnDeleteAccount.setForeground(DANGER_RED); btnDeleteAccount.setFont(new Font("Segoe UI", Font.BOLD, 15)); btnDeleteAccount.setFocusPainted(false); btnDeleteAccount.setCursor(new Cursor(Cursor.HAND_CURSOR)); btnDeleteAccount.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        btnDeleteAccount.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(DANGER_RED, 1, true), BorderFactory.createEmptyBorder(10, 0, 10, 0)));
        btnDeleteAccount.addActionListener(e -> deleteAccount());
        btnDeleteAccount.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btnDeleteAccount.setBackground(DANGER_RED); btnDeleteAccount.setForeground(Color.WHITE); }
            @Override public void mouseExited(MouseEvent e) { btnDeleteAccount.setBackground(SURFACE_COLOR); btnDeleteAccount.setForeground(DANGER_RED); }
        });
        container.add(btnDeleteAccount);

        return container;
    }

    private JPanel createLanguageSubPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(SURFACE_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(45, 45, 45), 1, true),
                BorderFactory.createEmptyBorder(20, 25, 25, 25)
        ));
        panel.setMaximumSize(new Dimension(580, 300));

        lblLanguageTitle = new JLabel();
        lblLanguageTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblLanguageTitle.setForeground(ACCENT_YELLOW);
        lblLanguageTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        panel.add(lblLanguageTitle, BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        lblLangHint = new JLabel();
        lblLangHint.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lblLangHint.setForeground(TEXT_PRIMARY);
        content.add(lblLangHint);
        content.add(Box.createVerticalStrut(20));

        rbVietnamese = new JRadioButton("", isVietnamese);
        rbEnglish = new JRadioButton("", !isVietnamese);

        rbVietnamese.setOpaque(false); rbVietnamese.setFont(new Font("Segoe UI", Font.PLAIN, 15)); rbVietnamese.setForeground(TEXT_PRIMARY); rbVietnamese.setFocusPainted(false);
        rbEnglish.setOpaque(false); rbEnglish.setFont(new Font("Segoe UI", Font.PLAIN, 15)); rbEnglish.setForeground(TEXT_PRIMARY); rbEnglish.setFocusPainted(false);

        ButtonGroup group = new ButtonGroup();
        group.add(rbVietnamese);
        group.add(rbEnglish);

        content.add(rbVietnamese);
        content.add(Box.createVerticalStrut(12));
        content.add(rbEnglish);
        content.add(Box.createVerticalStrut(25));

        btnSaveLanguage = new JButton();
        stylePrimaryButton(btnSaveLanguage);
        btnSaveLanguage.setPreferredSize(new Dimension(160, 40));
        btnSaveLanguage.setMaximumSize(new Dimension(160, 40));
        btnSaveLanguage.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnSaveLanguage.addActionListener(e -> {
            isVietnamese = rbVietnamese.isSelected();
            if (mainFrame != null) mainFrame.updateGlobalLanguage(isVietnamese);
            updateLanguageText();
            String successMsg = isVietnamese ? "Cài đặt ngôn ngữ đã được áp dụng thành công!" : "Language settings applied successfully!";
            String successTitle = isVietnamese ? "Thành công" : "Success";
            JOptionPane.showMessageDialog(this, successMsg, successTitle, JOptionPane.INFORMATION_MESSAGE);
        });
        content.add(btnSaveLanguage);

        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    // --- 🌟 THÀNH PHẦN MỚI: TẠO KHỐI THIẾT LẬP KÍCH THƯỚC CỐ ĐỊNH 🌟 ---
    private JPanel createSizeSubPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(SURFACE_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(45, 45, 45), 1, true),
                BorderFactory.createEmptyBorder(20, 25, 25, 25)
        ));
        panel.setMaximumSize(new Dimension(580, 300));

        lblSizeTitle = new JLabel();
        lblSizeTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblSizeTitle.setForeground(ACCENT_YELLOW);
        lblSizeTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        panel.add(lblSizeTitle, BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        lblSizeHint = new JLabel();
        lblSizeHint.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lblSizeHint.setForeground(TEXT_PRIMARY);
        content.add(lblSizeHint);
        content.add(Box.createVerticalStrut(20));

        // Thiết lập bộ chọn độ phân giải
        comboWindowSize.setBackground(INPUT_BG);
        comboWindowSize.setForeground(TEXT_PRIMARY);
        comboWindowSize.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        comboWindowSize.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        comboWindowSize.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(comboWindowSize);
        content.add(Box.createVerticalStrut(25));

        btnSaveSize = new JButton();
        stylePrimaryButton(btnSaveSize);
        btnSaveSize.setPreferredSize(new Dimension(160, 40));
        btnSaveSize.setMaximumSize(new Dimension(160, 40));
        btnSaveSize.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Xử lý sự kiện lưu kích thước cửa sổ cố định mới
        btnSaveSize.addActionListener(e -> {
            int index = comboWindowSize.getSelectedIndex();
            int w = 1200, h = 750;
            if (index == 1) { w = 1400; h = 850; }
            else if (index == 2) { w = 1600; h = 950; }

            if (mainFrame != null) {
                mainFrame.changeWindowSize(w, h);
            }

            String successMsg = isVietnamese ? "Kích thước ứng dụng đã được thay đổi cố định!" : "Application size changed successfully!";
            String successTitle = isVietnamese ? "Thành công" : "Success";
            JOptionPane.showMessageDialog(this, successMsg, successTitle, JOptionPane.INFORMATION_MESSAGE);
        });
        content.add(btnSaveSize);

        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createCategorySubPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(SURFACE_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(45, 45, 45), 1, true),
                BorderFactory.createEmptyBorder(20, 25, 25, 25)
        ));
        panel.setMaximumSize(new Dimension(580, Integer.MAX_VALUE));

        lblCategoryTitle = new JLabel();
        lblCategoryTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblCategoryTitle.setForeground(ACCENT_YELLOW);
        lblCategoryTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        panel.add(lblCategoryTitle, BorderLayout.NORTH);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);

        lblCatNameHint = new JLabel();
        lblCatNameHint.setForeground(TEXT_SECONDARY);
        lblCatNameHint.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblCatNameHint.setBorder(BorderFactory.createEmptyBorder(0, 2, 6, 0));
        lblCatNameHint.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(lblCatNameHint);

        txtCategoryName = new JTextField();
        styleTextField(txtCategoryName);
        txtCategoryName.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        txtCategoryName.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(txtCategoryName);
        form.add(Box.createVerticalStrut(15));

        lblCatIconHint = new JLabel();
        lblCatIconHint.setForeground(TEXT_SECONDARY);
        lblCatIconHint.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblCatIconHint.setBorder(BorderFactory.createEmptyBorder(0, 2, 6, 0));
        lblCatIconHint.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(lblCatIconHint);

        JPanel emojiContainer = new JPanel();
        emojiContainer.setLayout(new BoxLayout(emojiContainer, BoxLayout.Y_AXIS));
        emojiContainer.setOpaque(false);
        emojiContainer.setAlignmentX(Component.LEFT_ALIGNMENT);

        emojiGridPanel = new JPanel(new GridLayout(2, 9, 6, 6));
        emojiGridPanel.setBackground(SURFACE_COLOR);
        emojiGridPanel.setPreferredSize(new Dimension(480, 94));
        emojiGridPanel.setMaximumSize(new Dimension(480, 94));
        emojiGridPanel.setMinimumSize(new Dimension(480, 94));

        JPanel gridWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        gridWrapper.setOpaque(false);
        gridWrapper.add(emojiGridPanel);
        emojiContainer.add(gridWrapper);

        emojiContainer.add(Box.createVerticalStrut(10));

        JPanel emojiPagination = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        emojiPagination.setOpaque(false);
        emojiPagination.setPreferredSize(new Dimension(480, 30));
        emojiPagination.setMaximumSize(new Dimension(480, 30));
        emojiPagination.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnPrevEmojiPage = createPaginationButton("◀");
        btnNextEmojiPage = createPaginationButton("▶");
        lblEmojiPageIndicator = new JLabel("Trang 1 / 1");
        lblEmojiPageIndicator.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblEmojiPageIndicator.setForeground(TEXT_PRIMARY);
        lblEmojiPageIndicator.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));

        btnPrevEmojiPage.addActionListener(e -> { if (currentEmojiPage > 1) { currentEmojiPage--; refreshEmojiGrid(); } });
        btnNextEmojiPage.addActionListener(e -> { currentEmojiPage++; refreshEmojiGrid(); });

        emojiPagination.add(btnPrevEmojiPage);
        emojiPagination.add(lblEmojiPageIndicator);
        emojiPagination.add(btnNextEmojiPage);
        emojiContainer.add(emojiPagination);

        form.add(emojiContainer);
        form.add(Box.createVerticalStrut(15));

        lblCatTypeHint = new JLabel();
        lblCatTypeHint.setForeground(TEXT_SECONDARY);
        lblCatTypeHint.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblCatTypeHint.setBorder(BorderFactory.createEmptyBorder(0, 2, 6, 0));
        lblCatTypeHint.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(lblCatTypeHint);

        comboCategoryType.setBackground(INPUT_BG);
        comboCategoryType.setForeground(TEXT_PRIMARY);
        comboCategoryType.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        comboCategoryType.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        comboCategoryType.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(comboCategoryType);
        form.add(Box.createVerticalStrut(25));

        btnSaveCategory = new JButton();
        btnSaveCategory.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnSaveCategory.setBackground(ACCENT_YELLOW);
        btnSaveCategory.setForeground(BG_COLOR);
        btnSaveCategory.setFocusPainted(false);
        btnSaveCategory.setBorder(BorderFactory.createEmptyBorder(12, 30, 12, 30));
        btnSaveCategory.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSaveCategory.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnSaveCategory.addActionListener(e -> executeAddCategory());
        form.add(btnSaveCategory);

        panel.add(form, BorderLayout.CENTER);
        return panel;
    }

    private void refreshEmojiGrid() {
        if (emojiGridPanel == null) return;
        emojiGridPanel.removeAll();

        int totalItems = EMOJI_LIST.length;
        int totalPages = (int) Math.ceil((double) totalItems / EMOJI_PER_PAGE);

        if (currentEmojiPage > totalPages) currentEmojiPage = totalPages;
        if (currentEmojiPage < 1) currentEmojiPage = 1;

        lblEmojiPageIndicator.setText((isVietnamese ? "Trang " : "Page ") + currentEmojiPage + " / " + totalPages);
        btnPrevEmojiPage.setEnabled(currentEmojiPage > 1);
        btnNextEmojiPage.setEnabled(currentEmojiPage < totalPages);

        int startIndex = (currentEmojiPage - 1) * EMOJI_PER_PAGE;
        int endIndex = Math.min(startIndex + EMOJI_PER_PAGE, totalItems);

        int displayedCount = 0;
        for (int i = startIndex; i < endIndex; i++) {
            String emoji = EMOJI_LIST[i];
            emojiGridPanel.add(createEmojiCellComponent(emoji));
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
        cell.setPreferredSize(new Dimension(48, 44));
        cell.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel lbl = new JLabel(emoji, SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        cell.add(lbl, BorderLayout.CENTER);

        if (emoji.equals(selectedEmoji)) {
            cell.setBackground(ACCENT_YELLOW);
            lbl.setForeground(BG_COLOR);
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

    // --- BỘ LẬP BẢN DỊCH NGÔN NGỮ ĐỘNG TOÀN DIỆN (ĐÃ BỔ SUNG KHỐI SIZE THỨ 4) ---
    public void updateLanguageText() {
        int currentGenderIndex = cmbGender.getSelectedIndex();
        int currentTypeIndex = comboCategoryType.getSelectedIndex();
        int currentSizeIndex = comboWindowSize.getSelectedIndex(); // Lưu index kích thước hiện tại

        if (isVietnamese) {
            lblMainTitle.setText("Cài đặt hệ thống");
            btnLogout.setText("Đăng xuất");
            btnAccountTab.setText("Thông tin tài khoản");
            btnLanguageTab.setText("Ngôn ngữ");
            btnSizeTab.setText("Kích thước cửa sổ"); // 🌟 Dịch nghĩa Tab
            btnCategoryTab.setText("Thêm Danh mục");

            lblProfileTitle.setText("Thông tin cá nhân");
            lblNickname.setText("Tên hiển thị:");
            lblEmail.setText("Email:");
            lblGender.setText("Giới tính:");
            btnUpdateProfile.setText("Lưu thay đổi");
            btnOpenChangePass.setText("Đổi mật khẩu bảo mật");
            btnDeleteAccount.setText("Xóa tài khoản vĩnh viễn");

            lblLanguageTitle.setText("Ngôn ngữ hiển thị");
            lblLangHint.setText("Chọn ngôn ngữ mặc định của hệ thống ứng dụng:");

            if (rbVietnamese != null) rbVietnamese.setText("Tiếng Việt (Vietnamese)");
            if (rbEnglish != null) rbEnglish.setText("Tiếng Anh (English)");
            if (btnSaveLanguage != null) btnSaveLanguage.setText("Lưu");

            // 🌟 Cập nhật Text mục Kích thước cửa sổ
            lblSizeTitle.setText("Độ phân giải ứng dụng");
            lblSizeHint.setText("Chọn độ phân giải cố định cho cửa sổ ứng dụng (Hệ thống khóa kéo bằng chuột):");
            if (btnSaveSize != null) btnSaveSize.setText("Lưu");
            comboWindowSize.setModel(new DefaultComboBoxModel<>(new String[]{
                    "1200 x 750 (Mặc định)", "1400 x 850", "1600 x 950"
            }));

            lblCategoryTitle.setText("Thêm danh mục chi tiêu / thu nhập");
            lblCatNameHint.setText("Tên danh mục mới:");
            lblCatIconHint.setText("Chọn Icon/Emoji đại diện:");
            lblCatTypeHint.setText("Phân loại danh mục:");
            btnSaveCategory.setText("XÁC NHẬN THÊM DANH MỤC");

            cmbGender.setModel(new DefaultComboBoxModel<>(new String[]{"Nam", "Nữ", "Khác"}));
            comboCategoryType.setModel(new DefaultComboBoxModel<>(new String[]{
                    "Khoản chi tiêu (EXPENSE)", "Khoản thu nhập (INCOME)"
            }));
        } else {
            lblMainTitle.setText("System Settings");
            btnLogout.setText("Logout");
            btnAccountTab.setText("Account Settings");
            btnLanguageTab.setText("Language");
            btnSizeTab.setText("Window Size"); // 🌟 Dịch nghĩa Tab
            btnCategoryTab.setText("Add Category");

            lblProfileTitle.setText("Personal Profile");
            lblNickname.setText("Display Name:");
            lblEmail.setText("Email:");
            lblGender.setText("Gender:");
            btnUpdateProfile.setText("Save Changes");
            btnOpenChangePass.setText("Change Password");
            btnDeleteAccount.setText("Delete Account Permanently");

            lblLanguageTitle.setText("Display Language");
            lblLangHint.setText("Select the default language for the application system:");

            if (rbVietnamese != null) rbVietnamese.setText("Vietnamese (Tiếng Việt)");
            if (rbEnglish != null) rbEnglish.setText("English (Tiếng Anh)");
            if (btnSaveLanguage != null) btnSaveLanguage.setText("Save");

            // 🌟 Cập nhật Text mục Kích thước cửa sổ bằng tiếng Anh
            lblSizeTitle.setText("Application Resolution");
            lblSizeHint.setText("Select a fixed resolution for the application window (Mouse resize locked):");
            if (btnSaveSize != null) btnSaveSize.setText("Save");
            comboWindowSize.setModel(new DefaultComboBoxModel<>(new String[]{
                    "1200 x 750 (Default)", "1400 x 850", "1600 x 950"
            }));

            lblCategoryTitle.setText("Add Expense / Income Category");
            lblCatNameHint.setText("New Category Name:");
            lblCatIconHint.setText("Select Representative Icon/Emoji:");
            lblCatTypeHint.setText("Category Type:");
            btnSaveCategory.setText("CONFIRM ADD CATEGORY");

            cmbGender.setModel(new DefaultComboBoxModel<>(new String[]{"Male", "Female", "Other"}));
            comboCategoryType.setModel(new DefaultComboBoxModel<>(new String[]{
                    "Expense Component (EXPENSE)", "Income Component (INCOME)"
            }));
        }

        if (currentGenderIndex >= 0 && currentGenderIndex < cmbGender.getItemCount()) {
            cmbGender.setSelectedIndex(currentGenderIndex);
        }
        if (currentTypeIndex >= 0 && currentTypeIndex < comboCategoryType.getItemCount()) {
            comboCategoryType.setSelectedIndex(currentTypeIndex);
        }
        if (currentSizeIndex >= 0 && currentSizeIndex < comboWindowSize.getItemCount()) {
            comboWindowSize.setSelectedIndex(currentSizeIndex);
        }

        if (rbVietnamese != null) rbVietnamese.setSelected(isVietnamese);
        if (rbEnglish != null) rbEnglish.setSelected(!isVietnamese);

        refreshEmojiGrid();

        this.revalidate();
        this.repaint();
    }

    private void openChangePasswordDialog() {
        JDialog passDialog = new JDialog(mainFrame, isVietnamese ? "Thay đổi mật khẩu" : "Change Password", true);
        passDialog.setSize(400, 320);
        passDialog.setLocationRelativeTo(this);
        passDialog.getContentPane().setBackground(BG_COLOR);
        passDialog.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 15));
        formPanel.setBackground(BG_COLOR);
        formPanel.setBorder(BorderFactory.createEmptyBorder(25, 20, 15, 20));

        JLabel lOld = createLabel(isVietnamese ? "Mật khẩu cũ:" : "Old Password:");
        txtOldPassword = new JPasswordField(); styleTextField(txtOldPassword);

        JLabel lNew = createLabel(isVietnamese ? "Mật khẩu mới:" : "New Password:");
        txtNewPassword = new JPasswordField(); styleTextField(txtNewPassword);

        JLabel lConf = createLabel(isVietnamese ? "Xác nhận MK:" : "Confirm Pass:");
        txtConfirmPassword = new JPasswordField(); styleTextField(txtConfirmPassword);

        formPanel.add(lOld); formPanel.add(txtOldPassword);
        formPanel.add(lNew); formPanel.add(txtNewPassword);
        formPanel.add(lConf); formPanel.add(txtConfirmPassword);
        passDialog.add(formPanel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        btnPanel.setBackground(BG_COLOR);

        JButton btnCancel = new JButton(isVietnamese ? "HỦY BỎ" : "CANCEL");
        btnCancel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCancel.setBackground(SURFACE_COLOR); btnCancel.setForeground(TEXT_PRIMARY);
        btnCancel.setFocusPainted(false); btnCancel.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        btnCancel.addActionListener(e -> passDialog.dispose());

        JButton btnConfirm = new JButton(isVietnamese ? "XÁC NHẬN" : "CONFIRM");
        btnConfirm.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnConfirm.setBackground(ACCENT_YELLOW); btnConfirm.setForeground(BG_COLOR);
        btnConfirm.setFocusPainted(false); btnConfirm.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));

        btnConfirm.addActionListener(e -> {
            String oldPass = new String(txtOldPassword.getPassword());
            String newPass = new String(txtNewPassword.getPassword());
            String confirmPass = new String(txtConfirmPassword.getPassword());

            if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
                JOptionPane.showMessageDialog(passDialog, isVietnamese ? "Vui lòng nhập đầy đủ!" : "Please fill all fields!", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!newPass.equals(confirmPass)) {
                JOptionPane.showMessageDialog(passDialog, isVietnamese ? "Mật khẩu xác nhận không khớp!" : "Confirm password does not match!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String username = SessionManager.getCurrentUsername();
            User user = UserService.login(username, oldPass);

            if (user == null) {
                JOptionPane.showMessageDialog(passDialog, isVietnamese ? "Mật khẩu cũ không đúng!" : "Incorrect old password!", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                user.setPasswordHash(UserService.hashPassword(newPass));
                DatabaseUtil.updateUser(user);
                JOptionPane.showMessageDialog(passDialog, isVietnamese ? "Đổi mật khẩu thành công! Hãy đăng nhập lại." : "Password changed! Please re-login.");
                passDialog.dispose();
                logout();
            }
        });

        btnPanel.add(btnCancel);
        btnPanel.add(btnConfirm);
        passDialog.add(btnPanel, BorderLayout.SOUTH);
        passDialog.setVisible(true);
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

            this.selectedEmoji = "📌";
            this.currentEmojiPage = 1;
            refreshEmojiGrid();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error DB: " + ex.getMessage(), "System Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void switchSubTab(String targetCard, JButton activeBtn) {
        subCardLayout.show(subContentPanel, targetCard);
        btnAccountTab.setBackground(INPUT_BG); btnAccountTab.setForeground(TEXT_SECONDARY);
        btnLanguageTab.setBackground(INPUT_BG); btnLanguageTab.setForeground(TEXT_SECONDARY);
        btnSizeTab.setBackground(INPUT_BG); btnSizeTab.setForeground(TEXT_SECONDARY); // 🌟 Reset nút size
        btnCategoryTab.setBackground(INPUT_BG); btnCategoryTab.setForeground(TEXT_SECONDARY);
        activeBtn.setBackground(ACCENT_YELLOW); activeBtn.setForeground(BG_COLOR);
    }

    private JButton createSubNavButton(String text, boolean isActive) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        if (isActive) {
            btn.setBackground(ACCENT_YELLOW); btn.setForeground(BG_COLOR);
        } else {
            btn.setBackground(INPUT_BG); btn.setForeground(TEXT_SECONDARY);
        }
        return btn;
    }

    private JButton createPaginationButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(TEXT_PRIMARY);
        btn.setBackground(INPUT_BG);
        btn.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60), 1));
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(32, 26));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public void refreshData() {
        String username = SessionManager.getCurrentUsername();
        if (username != null) {
            User user = DatabaseUtil.getUserByUsername(username);
            if (user != null) {
                txtNickname.setText(user.getNickname());
                txtEmail.setText(user.getEmail());

                String g = user.getGender();
                if ("Male".equalsIgnoreCase(g) || "Nam".equalsIgnoreCase(g)) cmbGender.setSelectedIndex(0);
                else if ("Female".equalsIgnoreCase(g) || "Nữ".equalsIgnoreCase(g)) cmbGender.setSelectedIndex(1);
                else cmbGender.setSelectedIndex(2);
            }
        }
    }

    private void updateProfile() {
        String nickname = txtNickname.getText().trim();
        String email = txtEmail.getText().trim();

        String gender = "Other";
        if (cmbGender.getSelectedIndex() == 0) gender = "Male";
        else if (cmbGender.getSelectedIndex() == 1) gender = "Female";

        String username = SessionManager.getCurrentUsername();
        if (username == null) return;

        User user = DatabaseUtil.getUserByUsername(username);
        if (user != null) {
            user.setNickname(nickname);
            user.setEmail(email);
            user.setGender(gender);
            DatabaseUtil.updateUser(user);
            JOptionPane.showMessageDialog(this, isVietnamese ? "Cập nhật thông tin thành công!" : "Profile updated successfully!");
            mainFrame.refreshAllPanels();
        }
    }

    private void deleteAccount() {
        int confirm = JOptionPane.showConfirmDialog(this,
                isVietnamese ? "Bạn có chắc muốn xóa tài khoản? Toàn bộ giao dịch và ngân sách sẽ bị mất và không thể hoàn tác!" : "Are you sure you want to delete your account? All transaction and budget data will be permanently lost!",
                "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            String userId = SessionManager.getCurrentUserId();
            if (userId != null) {
                DatabaseUtil.deleteTransactionsByUser(userId);
                DatabaseUtil.deleteBudgetsByUser(userId);
                DatabaseUtil.deleteUser(userId);
                logout();
            }
        }
    }

    private void logout() {
        SessionManager.logout();
        mainFrame.dispose();
        new LoginFrame().setVisible(true);
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lbl.setForeground(TEXT_SECONDARY);
        return lbl;
    }

    private void styleTextField(JTextField tf) {
        tf.setBackground(INPUT_BG); tf.setForeground(TEXT_PRIMARY); tf.setCaretColor(ACCENT_YELLOW);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        tf.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(60, 60, 60)), BorderFactory.createEmptyBorder(10, 15, 10, 15)));
    }

    private void stylePrimaryButton(JButton btn) {
        btn.setBackground(ACCENT_YELLOW); btn.setForeground(BG_COLOR); btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setFocusPainted(false); btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
    }
}