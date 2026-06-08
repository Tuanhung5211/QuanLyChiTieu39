package com.expensemanager.ui;

import com.expensemanager.util.ThemeManager;

import javax.swing.*;
import java.awt.*;

public class SystemConfigPanel extends JPanel {

    private MainFrame mainFrame;
    private boolean isVietnamese;

    private JPanel configCard;
    private JLabel lblLanguageTitle, lblLangHint, lblSizeTitle, lblSizeHint;
    private JRadioButton rbVietnamese, rbEnglish;
    private JComboBox<String> comboWindowSize;
    private JButton btnSaveLanguage, btnSaveSize;
    private JSeparator separator;

    public SystemConfigPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.isVietnamese = mainFrame != null && mainFrame.isVietnamese();

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);

        initComponents();
        syncComboWithCurrentSize();
        updateResponsiveLayout(isVietnamese, 560);
        applyTheme();
    }

    private void initComponents() {
        configCard = new JPanel(new GridBagLayout());
        configCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.getColor("border"), 1, true),
                BorderFactory.createEmptyBorder(22, 26, 22, 26)
        ));
        configCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(6, 0, 6, 0);

        lblLanguageTitle = new JLabel();
        lblLanguageTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        gbc.gridx = 0; gbc.gridy = 0;
        configCard.add(lblLanguageTitle, gbc);

        lblLangHint = new JLabel();
        lblLangHint.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        gbc.gridy = 1; configCard.add(lblLangHint, gbc);

        JPanel rbPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        rbPanel.setOpaque(false);
        rbVietnamese = new JRadioButton("", isVietnamese);
        rbEnglish = new JRadioButton("", !isVietnamese);
        rbVietnamese.setOpaque(false); rbVietnamese.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        rbEnglish.setOpaque(false); rbEnglish.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        ButtonGroup group = new ButtonGroup();
        group.add(rbVietnamese); group.add(rbEnglish);
        rbPanel.add(rbVietnamese); rbPanel.add(Box.createHorizontalStrut(25)); rbPanel.add(rbEnglish);
        gbc.gridy = 2; configCard.add(rbPanel, gbc);

        btnSaveLanguage = new JButton();
        stylePrimaryButton(btnSaveLanguage);
        btnSaveLanguage.addActionListener(e -> saveLanguageConfig());
        gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.WEST;
        configCard.add(btnSaveLanguage, gbc);

        separator = new JSeparator();
        gbc.gridy = 4; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.insets = new Insets(18, 0, 18, 0);
        configCard.add(separator, gbc);
        gbc.insets = new Insets(6, 0, 6, 0);

        lblSizeTitle = new JLabel();
        lblSizeTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        gbc.gridy = 5; configCard.add(lblSizeTitle, gbc);

        lblSizeHint = new JLabel();
        lblSizeHint.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        gbc.gridy = 6; configCard.add(lblSizeHint, gbc);

        comboWindowSize = new JComboBox<>();
        comboWindowSize.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        gbc.gridy = 7; configCard.add(comboWindowSize, gbc);

        btnSaveSize = new JButton();
        stylePrimaryButton(btnSaveSize);
        btnSaveSize.addActionListener(e -> saveResolutionConfig());
        gbc.gridy = 8; gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.WEST;
        configCard.add(btnSaveSize, gbc);

        add(configCard);
    }

    public void applyTheme() {
        setBackground(ThemeManager.getColor("bg"));
        if (configCard != null) {
            configCard.setBackground(ThemeManager.getColor("surface"));
            configCard.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(ThemeManager.getColor("border"), 1, true),
                    ((javax.swing.border.CompoundBorder)configCard.getBorder()).getInsideBorder()
            ));
        }
        if (lblLanguageTitle != null) lblLanguageTitle.setForeground(ThemeManager.getColor("accent"));
        if (lblLangHint != null) lblLangHint.setForeground(ThemeManager.getColor("textPrimary"));
        if (rbVietnamese != null) rbVietnamese.setForeground(ThemeManager.getColor("textPrimary"));
        if (rbEnglish != null) rbEnglish.setForeground(ThemeManager.getColor("textPrimary"));
        if (btnSaveLanguage != null) {
            btnSaveLanguage.setBackground(ThemeManager.getColor("accent"));
            btnSaveLanguage.setForeground(ThemeManager.getColor("bg"));
        }
        if (lblSizeTitle != null) lblSizeTitle.setForeground(ThemeManager.getColor("accent"));
        if (lblSizeHint != null) lblSizeHint.setForeground(ThemeManager.getColor("textPrimary"));
        if (comboWindowSize != null) {
            comboWindowSize.setBackground(ThemeManager.getColor("input"));
            comboWindowSize.setForeground(ThemeManager.getColor("textPrimary"));
        }
        if (btnSaveSize != null) {
            btnSaveSize.setBackground(ThemeManager.getColor("accent"));
            btnSaveSize.setForeground(ThemeManager.getColor("bg"));
        }
        if (separator != null) separator.setForeground(ThemeManager.getColor("border"));
    }

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

    private void stylePrimaryButton(JButton btn) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
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

    private void syncComboWithCurrentSize() {
        if (mainFrame == null) return;
        int currentWidth = mainFrame.getWidth();
        int currentHeight = mainFrame.getHeight();
        int index = 0;
        if (currentWidth == 1400 && currentHeight == 800) index = 1;
        else if (currentWidth == 1600 && currentHeight == 950) index = 2;
        if (comboWindowSize != null && comboWindowSize.getItemCount() > index) {
            comboWindowSize.setSelectedIndex(index);
        }
    }
}