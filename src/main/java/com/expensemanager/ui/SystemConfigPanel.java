package com.expensemanager.ui;

import javax.swing.*;
import java.awt.*;

public class SystemConfigPanel extends JPanel {

    // =====================================================================
    // 1. KHAI BÁO BIẾN GIAO DIỆN VÀ LOGIC
    // =====================================================================
    private MainFrame mainFrame;
    private boolean isVietnamese;

    private JPanel configCard;
    private JLabel lblLanguageTitle, lblLangHint, lblSizeTitle, lblSizeHint;
    private JRadioButton rbVietnamese, rbEnglish;
    private JComboBox<String> comboWindowSize;
    private JButton btnSaveLanguage, btnSaveSize;
    private JSeparator separator;

    private final Color SURFACE_COLOR = new Color(30, 30, 30);
    private final Color INPUT_BG = new Color(40, 40, 40);
    private final Color TEXT_PRIMARY = new Color(240, 240, 240);
    private final Color ACCENT_YELLOW = new Color(255, 193, 7);

    // =====================================================================
    // 2. CONSTRUCTOR - KHỞI TẠO BỐ CỤC FORM CẤU HÌNH
    // =====================================================================
    public SystemConfigPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.isVietnamese = mainFrame != null && mainFrame.isVietnamese();

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);

        initComponents();
        updateResponsiveLayout(isVietnamese, 560);
    }

    private void initComponents() {
        configCard = new JPanel(new GridBagLayout());
        configCard.setBackground(SURFACE_COLOR);
        configCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(45, 45, 45), 1, true),
                BorderFactory.createEmptyBorder(22, 26, 22, 26)
        ));
        configCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(6, 0, 6, 0);

        // --- PHÂN VÙNG 1: NGÔN NGỮ HIỂN THỊ ---
        lblLanguageTitle = new JLabel();
        lblLanguageTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblLanguageTitle.setForeground(ACCENT_YELLOW);
        gbc.gridx = 0; gbc.gridy = 0;
        configCard.add(lblLanguageTitle, gbc);

        lblLangHint = new JLabel();
        lblLangHint.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lblLangHint.setForeground(TEXT_PRIMARY);
        gbc.gridy = 1; configCard.add(lblLangHint, gbc);

        JPanel rbPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        rbPanel.setOpaque(false);
        rbVietnamese = new JRadioButton("", isVietnamese);
        rbEnglish = new JRadioButton("", !isVietnamese);

        rbVietnamese.setOpaque(false); rbVietnamese.setFont(new Font("Segoe UI", Font.PLAIN, 15)); rbVietnamese.setForeground(TEXT_PRIMARY); rbVietnamese.setFocusPainted(false);
        rbEnglish.setOpaque(false); rbEnglish.setFont(new Font("Segoe UI", Font.PLAIN, 15)); rbEnglish.setForeground(TEXT_PRIMARY); rbEnglish.setFocusPainted(false);

        ButtonGroup group = new ButtonGroup();
        group.add(rbVietnamese); group.add(rbEnglish);
        rbPanel.add(rbVietnamese); rbPanel.add(Box.createHorizontalStrut(25)); rbPanel.add(rbEnglish);
        gbc.gridy = 2; configCard.add(rbPanel, gbc);

        btnSaveLanguage = new JButton();
        stylePrimaryButton(btnSaveLanguage);
        btnSaveLanguage.addActionListener(e -> saveLanguageConfig());
        gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.WEST;
        configCard.add(btnSaveLanguage, gbc);

        // Vạch phân tách phẳng chia đôi không gian
        separator = new JSeparator();
        separator.setForeground(new Color(55, 55, 55));
        gbc.gridy = 4; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.insets = new Insets(18, 0, 18, 0);
        configCard.add(separator, gbc);
        gbc.insets = new Insets(6, 0, 6, 0);

        // --- PHÂN VÙNG 2: ĐỘ PHÂN GIẢI MÀN HÌNH ---
        lblSizeTitle = new JLabel();
        lblSizeTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblSizeTitle.setForeground(ACCENT_YELLOW);
        gbc.gridy = 5; configCard.add(lblSizeTitle, gbc);

        lblSizeHint = new JLabel();
        lblSizeHint.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lblSizeHint.setForeground(TEXT_PRIMARY);
        gbc.gridy = 6; configCard.add(lblSizeHint, gbc);

//        comboWindowSize = new JComboBox<>();
//        comboWindowSize.setBackground(INPUT_BG); comboWindowSize.setForeground(TEXT_PRIMARY); comboWindowSize.setFont(new Font("Segoe UI", Font.PLAIN, 15));
//        gbc.gridy = 7; configCard.add(comboWindowSize, gbc);
        //Cập nhật đổi màu
        comboWindowSize = new JComboBox<>();
        comboWindowSize.setBackground(INPUT_BG);
        comboWindowSize.setForeground(TEXT_PRIMARY);
        comboWindowSize.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        comboWindowSize.setUI(new javax.swing.plaf.basic.BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton btn = super.createArrowButton();
                btn.setBackground(new Color(100, 100, 100));  // màu nền sáng
                btn.setForeground(Color.WHITE);
                btn.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
                return btn;
            }
        });
        gbc.gridy = 7; configCard.add(comboWindowSize, gbc);

        btnSaveSize = new JButton();
        stylePrimaryButton(btnSaveSize);
        btnSaveSize.addActionListener(e -> saveResolutionConfig());
        gbc.gridy = 8; gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.WEST;
        configCard.add(btnSaveSize, gbc);

        add(configCard);
    }

    // =====================================================================
    // 3. XỬ LÝ LOGIC NGHIỆP VỤ LƯU CẤU HÌNH
    // =====================================================================
    private void saveLanguageConfig() {
        isVietnamese = rbVietnamese.isSelected();
        if (mainFrame != null) {
            mainFrame.updateGlobalLanguage(isVietnamese);
        }
        String successMsg = isVietnamese ? "Cài đặt ngôn ngữ đã được áp dụng thành công!" : "Language settings applied successfully!";
        JOptionPane.showMessageDialog(this, successMsg, isVietnamese ? "Thành công" : "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void saveResolutionConfig() {
        int index = comboWindowSize.getSelectedIndex();
        int w = 1200, h = 750;
        if (index == 1) { w = 1400; h = 800; }
        else if (index == 2) { w = 1600; h = 950; }

        if (mainFrame != null) {
            mainFrame.changeWindowSize(w, h);
        }
        JOptionPane.showMessageDialog(this,
                isVietnamese ? "Kích thước ứng dụng đã được thay đổi!" : "Application size changed!",
                isVietnamese ? "Thành công" : "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    // =====================================================================
    // 4. TIỆN ÍCH GIAO DIỆN VÀ RESPONSIVE MÀN HÌNH
    // =====================================================================
    private void stylePrimaryButton(JButton btn) {
        btn.setBackground(ACCENT_YELLOW); btn.setForeground(SURFACE_COLOR); btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false); btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 22, 8, 22));
    }

    public void updateResponsiveLayout(boolean isVN, int fluidWidth) {
        this.isVietnamese = isVN;
        int currentSizeIndex = comboWindowSize.getSelectedIndex();

        setMaximumSize(new Dimension(fluidWidth, Integer.MAX_VALUE));
        if (configCard != null) {
            configCard.setPreferredSize(new Dimension(fluidWidth, 440));
            configCard.setMaximumSize(new Dimension(fluidWidth, 440));
            configCard.setMinimumSize(new Dimension(fluidWidth, 440));
        }
        if (comboWindowSize != null) {
            comboWindowSize.setPreferredSize(new Dimension(fluidWidth - 52, 40));
            comboWindowSize.setMaximumSize(new Dimension(fluidWidth - 52, 40));
        }

        if (isVN) {
            lblLanguageTitle.setText("Ngôn ngữ hiển thị");
            lblLangHint.setText("Chọn ngôn ngữ mặc định của hệ thống ứng dụng:");
            rbVietnamese.setText("Tiếng Việt (Vietnamese)"); rbEnglish.setText("Tiếng Anh (English)");
            btnSaveLanguage.setText("Lưu cấu hình dịch");

            lblSizeTitle.setText("Độ phân giải hiển thị");
            lblSizeHint.setText("Chọn độ phân giải cố định cho cửa sổ (Hệ thống tự động khóa chuột kéo giãn):");
            btnSaveSize.setText("Áp dụng kích thước");
            comboWindowSize.setModel(new DefaultComboBoxModel<>(new String[]{"1200 x 750 (Mặc định)", "1400 x 800", "1600 x 950"}));
        } else {
            lblLanguageTitle.setText("Display Language");
            lblLangHint.setText("Select the default language for the application system:");
            rbVietnamese.setText("Vietnamese (Tiếng Việt)"); rbEnglish.setText("English (Tiếng Anh)");
            btnSaveLanguage.setText("Save Translation");

            lblSizeTitle.setText("Application Resolution");
            lblSizeHint.setText("Select a fixed resolution for the window (Mouse resize is permanently locked):");
            btnSaveSize.setText("Apply Screen Size");
            comboWindowSize.setModel(new DefaultComboBoxModel<>(new String[]{"1200 x 750 (Default)", "1400 x 800", "1600 x 950"}));
        }

        if (currentSizeIndex >= 0 && currentSizeIndex < comboWindowSize.getItemCount()) {
            comboWindowSize.setSelectedIndex(currentSizeIndex);
        }
    }
}