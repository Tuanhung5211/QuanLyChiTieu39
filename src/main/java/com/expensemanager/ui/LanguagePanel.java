package com.expensemanager.ui;

import com.expensemanager.service.SessionManager;

import javax.swing.*;
import java.awt.*;

public class LanguagePanel extends JPanel {
    private JComboBox<String> cmbLanguage;
    private MainFrame mainFrame;

    public LanguagePanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Ngôn ngữ:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0;
        cmbLanguage = new JComboBox<>(new String[]{"Tiếng Việt", "English"});
        String savedLang = SessionManager.getLanguage();
        cmbLanguage.setSelectedItem("English".equals(savedLang) ? "English" : "Tiếng Việt");
        panel.add(cmbLanguage, gbc);

        gbc.gridx = 1; gbc.gridy = 1;
        JButton btnApply = new JButton("Áp dụng");
        btnApply.addActionListener(e -> {
            String selected = (String) cmbLanguage.getSelectedItem();
            if ("English".equals(selected)) {
                SessionManager.setLanguage("en");
                JOptionPane.showMessageDialog(this, "Language set to English. Restart to take full effect.");
            } else {
                SessionManager.setLanguage("vi");
                JOptionPane.showMessageDialog(this, "Đã đổi sang Tiếng Việt. Khởi động lại để thấy toàn bộ.");
            }
        });
        panel.add(btnApply, gbc);

        add(panel, BorderLayout.NORTH);
    }
}