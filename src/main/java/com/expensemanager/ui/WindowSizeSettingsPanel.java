package com.expensemanager.ui;

import javax.swing.*;
import java.awt.*;

public class WindowSizeSettingsPanel extends JPanel {
    private MainFrame mainFrame;
    private boolean isVietnamese;

    private JLabel lblSizeTitle, lblSizeHint;
    private JComboBox<String> comboWindowSize;
    private JButton btnSaveSize;

    private final Color SURFACE_COLOR = new Color(30, 30, 30);
    private final Color INPUT_BG = new Color(40, 40, 40);
    private final Color TEXT_PRIMARY = new Color(240, 240, 240);
    private final Color ACCENT_YELLOW = new Color(255, 193, 7);

    public WindowSizeSettingsPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.isVietnamese = mainFrame != null && mainFrame.isVietnamese();

        setLayout(new BorderLayout());
        setBackground(SURFACE_COLOR);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(45, 45, 45), 1, true),
                BorderFactory.createEmptyBorder(20, 25, 25, 25)
        ));

        lblSizeTitle = new JLabel();
        lblSizeTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblSizeTitle.setForeground(ACCENT_YELLOW);
        lblSizeTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        add(lblSizeTitle, BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        lblSizeHint = new JLabel();
        lblSizeHint.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lblSizeHint.setForeground(TEXT_PRIMARY);
        content.add(lblSizeHint);
        content.add(Box.createVerticalStrut(20));

        comboWindowSize = new JComboBox<>();
        comboWindowSize.setBackground(INPUT_BG); comboWindowSize.setForeground(TEXT_PRIMARY); comboWindowSize.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        comboWindowSize.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(comboWindowSize);
        content.add(Box.createVerticalStrut(25));

        btnSaveSize = new JButton();
        btnSaveSize.setBackground(ACCENT_YELLOW); btnSaveSize.setForeground(new Color(18, 18, 18));
        btnSaveSize.setFont(new Font("Segoe UI", Font.BOLD, 15)); btnSaveSize.setFocusPainted(false);
        btnSaveSize.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnSaveSize.addActionListener(e -> {
            int index = comboWindowSize.getSelectedIndex();
            int w = 1200, h = 750;
            if (index == 1) { w = 1400; h = 800; }
            else if (index == 2) { w = 1600; h = 950; }
            if (mainFrame != null) mainFrame.changeWindowSize(w, h);
            JOptionPane.showMessageDialog(this, isVietnamese ? "Kích thước ứng dụng đã được thay đổi!" : "Application size changed!", isVietnamese ? "Thành công" : "Success", JOptionPane.INFORMATION_MESSAGE);
        });
        content.add(btnSaveSize);
        add(content, BorderLayout.CENTER);

        updateResponsiveLayout(isVietnamese, 560);
    }

    public void updateResponsiveLayout(boolean isVN, int fluidWidth) {
        this.isVietnamese = isVN;
        int currentSizeIndex = comboWindowSize.getSelectedIndex();

        setPreferredSize(new Dimension(fluidWidth, 260));
        setMaximumSize(new Dimension(fluidWidth, 260));
        comboWindowSize.setPreferredSize(new Dimension(fluidWidth - 48, 40));
        comboWindowSize.setMaximumSize(new Dimension(fluidWidth - 48, 40));
        btnSaveSize.setPreferredSize(new Dimension(160, 40));
        btnSaveSize.setMaximumSize(new Dimension(160, 40));

        if (isVN) {
            lblSizeTitle.setText("Độ phân giải ứng dụng");
            lblSizeHint.setText("Chọn độ phân giải cố định cho cửa sổ ứng dụng (Hệ thống khóa kéo bằng chuột):");
            btnSaveSize.setText("Lưu");
            comboWindowSize.setModel(new DefaultComboBoxModel<>(new String[]{"1200 x 750 (Mặc định)", "1400 x 800", "1600 x 950"}));
        } else {
            lblSizeTitle.setText("Application Resolution");
            lblSizeHint.setText("Select a fixed resolution for the application window (Mouse resize locked):");
            btnSaveSize.setText("Save");
            comboWindowSize.setModel(new DefaultComboBoxModel<>(new String[]{"1200 x 750 (Default)", "1400 x 800", "1600 x 950"}));
        }
        if (currentSizeIndex >= 0 && currentSizeIndex < comboWindowSize.getItemCount()) comboWindowSize.setSelectedIndex(currentSizeIndex);
    }
}