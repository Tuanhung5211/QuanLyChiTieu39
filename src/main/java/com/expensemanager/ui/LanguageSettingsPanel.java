package com.expensemanager.ui;

import javax.swing.*;
import java.awt.*;

public class LanguageSettingsPanel extends JPanel {
    private MainFrame mainFrame;
    private boolean isVietnamese;

    private JLabel lblLanguageTitle, lblLangHint;
    private JRadioButton rbVietnamese, rbEnglish;
    private JButton btnSaveLanguage;

    private final Color SURFACE_COLOR = new Color(30, 30, 30);
    private final Color TEXT_PRIMARY = new Color(240, 240, 240);
    private final Color ACCENT_YELLOW = new Color(255, 193, 7);

    public LanguageSettingsPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.isVietnamese = mainFrame != null && mainFrame.isVietnamese();

        setLayout(new BorderLayout());
        setBackground(SURFACE_COLOR);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(45, 45, 45), 1, true),
                BorderFactory.createEmptyBorder(20, 25, 25, 25)
        ));

        lblLanguageTitle = new JLabel();
        lblLanguageTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblLanguageTitle.setForeground(ACCENT_YELLOW);
        lblLanguageTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        add(lblLanguageTitle, BorderLayout.NORTH);

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
        group.add(rbVietnamese); group.add(rbEnglish);
        content.add(rbVietnamese); content.add(Box.createVerticalStrut(12)); content.add(rbEnglish);
        content.add(Box.createVerticalStrut(25));

        btnSaveLanguage = new JButton();
        btnSaveLanguage.setBackground(ACCENT_YELLOW); btnSaveLanguage.setForeground(new Color(18, 18, 18));
        btnSaveLanguage.setFont(new Font("Segoe UI", Font.BOLD, 15)); btnSaveLanguage.setFocusPainted(false);
        btnSaveLanguage.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSaveLanguage.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnSaveLanguage.addActionListener(e -> {
            isVietnamese = rbVietnamese.isSelected();
            if (mainFrame != null) mainFrame.updateGlobalLanguage(isVietnamese);
            String successMsg = isVietnamese ? "Cài đặt ngôn ngữ đã được áp dụng thành công!" : "Language settings applied successfully!";
            JOptionPane.showMessageDialog(this, successMsg, isVietnamese ? "Thành công" : "Success", JOptionPane.INFORMATION_MESSAGE);
        });
        content.add(btnSaveLanguage);
        add(content, BorderLayout.CENTER);

        updateResponsiveLayout(isVietnamese, 560);
    }

    public void updateResponsiveLayout(boolean isVN, int fluidWidth) {
        this.isVietnamese = isVN;

        setPreferredSize(new Dimension(fluidWidth, 260));
        setMaximumSize(new Dimension(fluidWidth, 260));

        if (isVN) {
            lblLanguageTitle.setText("Ngôn ngữ hiển thị");
            lblLangHint.setText("Chọn ngôn ngữ mặc định của hệ thống ứng dụng:");
            rbVietnamese.setText("Tiếng Việt (Vietnamese)"); rbEnglish.setText("Tiếng Anh (English)");
            btnSaveLanguage.setText("Lưu");
        } else {
            lblLanguageTitle.setText("Display Language");
            lblLangHint.setText("Select the default language for the application system:");
            rbVietnamese.setText("Vietnamese (Tiếng Việt)"); rbEnglish.setText("English (Tiếng Anh)");
            btnSaveLanguage.setText("Save");
        }
        rbVietnamese.setSelected(isVN);
        rbEnglish.setSelected(!isVN);
    }
}