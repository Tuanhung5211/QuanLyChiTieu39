package com.expensemanager.ui;

import javax.swing.*;
import java.awt.*;

public class SettingsPanel extends JPanel {
    private MainFrame mainFrame;
    private JTabbedPane tabbedPane;
    private ProfilePanel profilePanel;
    private CategorySettingsPanel categoryPanel;
    private LanguagePanel languagePanel;

    public SettingsPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JLabel title = new JLabel("CÀI ĐẶT", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setBorder(BorderFactory.createEmptyBorder(15, 0, 10, 0));
        add(title, BorderLayout.NORTH);

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        profilePanel = new ProfilePanel();
        categoryPanel = new CategorySettingsPanel(mainFrame);
        languagePanel = new LanguagePanel(mainFrame);

        tabbedPane.addTab("Profile", profilePanel);
        tabbedPane.addTab("Danh mục", categoryPanel);
        tabbedPane.addTab("Ngôn ngữ", languagePanel);

        add(tabbedPane, BorderLayout.CENTER);
    }

    public void refreshData() {
        profilePanel.loadUserData();
        categoryPanel.refreshCategories();
    }
}