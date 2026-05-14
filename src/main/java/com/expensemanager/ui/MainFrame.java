package com.expensemanager.ui;

import com.expensemanager.service.BudgetManager;
import com.expensemanager.service.FinanceService;
import com.expensemanager.service.StatisticsService;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private JPanel sidebar;
    private DashboardPanel dashboardPanel;
    private HistoryPanel historyPanel;
    private StatisticsPanel statisticsPanel;
    private SettingsPanel settingsPanel;

    private FinanceService financeService;
    private StatisticsService statsService;
    private BudgetManager budgetManager;

    public MainFrame() {
        try {
            financeService = new FinanceService();
            financeService.syncFromDatabase();
        } catch (Exception e) {
            e.printStackTrace();
            financeService = null;
        }
        if (financeService != null) {
            statsService = new StatisticsService(financeService);
            budgetManager = new BudgetManager(financeService);
        } else {
            statsService = null;
            budgetManager = null;
        }

        setTitle("Money Tracker Desktop");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 750);
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(18, 18, 18));
        setLayout(new BorderLayout());

        // Sidebar bên trái
        sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);

        // Main Panel dùng CardLayout
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setBackground(new Color(18, 18, 18));

        dashboardPanel = new DashboardPanel(this, financeService, budgetManager);
        historyPanel = new HistoryPanel(financeService);
        if (statsService != null && budgetManager != null) {
            statisticsPanel = new StatisticsPanel(statsService, budgetManager);
        } else {
            statisticsPanel = null;
        }
        settingsPanel = new SettingsPanel(this);

        mainPanel.add(dashboardPanel, "dashboard");
        mainPanel.add(historyPanel, "history");
        if (statisticsPanel != null) mainPanel.add(statisticsPanel, "statistics");
        mainPanel.add(settingsPanel, "settings");

        add(mainPanel, BorderLayout.CENTER);

        setVisible(true);
    }

    private JPanel createSidebar() {
        JPanel p = new JPanel();
        p.setPreferredSize(new Dimension(220, 800));
        p.setBackground(new Color(25, 25, 25));
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(45, 45, 45)));

        // Profile Avatar (Mô phỏng ảnh 10)
        JLabel lblAvatar = new JLabel("H", SwingConstants.CENTER);
        lblAvatar.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblAvatar.setForeground(Color.WHITE);
        lblAvatar.setOpaque(true);
        lblAvatar.setBackground(new Color(138, 107, 95));
        lblAvatar.setPreferredSize(new Dimension(60, 60));
        lblAvatar.setMaximumSize(new Dimension(60, 60));
        lblAvatar.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblAvatar.setBorder(BorderFactory.createLineBorder(new Color(25, 25, 25), 2, true));

        JLabel lblUser = new JLabel("Hưng", SwingConstants.CENTER);
        lblUser.setForeground(Color.WHITE);
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblUser.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblUser.setBorder(BorderFactory.createEmptyBorder(10, 0, 30, 0));

        p.add(Box.createRigidArea(new Dimension(0, 30)));
        p.add(lblAvatar);
        p.add(lblUser);

        p.add(createSideBtn("Tổng quan", "dashboard", "🏠"));
        p.add(createSideBtn("Biểu đồ", "statistics", "📊"));
        p.add(createSideBtn("Lịch sử", "history", "📅"));
        p.add(createSideBtn("Ngân sách", "budget", "💰"));
        p.add(createSideBtn("Cài đặt", "settings", "⚙️"));

        return p;
    }

    private JButton createSideBtn(String text, String cardName, String icon) {
        JButton btn = new JButton(icon + "   " + text);
        btn.setMaximumSize(new Dimension(220, 50));
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        btn.setForeground(new Color(200, 200, 200));
        btn.setBackground(new Color(25, 25, 25));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 10));
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.addActionListener(e -> {
            if ("budget".equals(cardName)) {
                if (budgetManager != null) {
                    new BudgetDialog(this, budgetManager).setVisible(true);
                    refreshAllPanels();
                } else JOptionPane.showMessageDialog(this, "Ngân sách chưa sẵn sàng.");
            } else {
                refreshAllPanels();
                cardLayout.show(mainPanel, cardName);
            }
        });

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(new Color(40, 40, 40));
                btn.setForeground(new Color(255, 193, 7)); // Vàng Money Tracker
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(new Color(25, 25, 25));
                btn.setForeground(new Color(200, 200, 200));
            }
        });
        return btn;
    }

    public void refreshAllPanels() {
        if (financeService != null) financeService.syncFromDatabase();
        try { dashboardPanel.refreshData(); } catch (Exception e) { e.printStackTrace(); }
        try { historyPanel.refreshData(); } catch (Exception e) { e.printStackTrace(); }
        if (statisticsPanel != null) try { statisticsPanel.refreshData(); } catch (Exception e) { e.printStackTrace(); }
        try { settingsPanel.refreshData(); } catch (Exception e) { e.printStackTrace(); }
    }

    public FinanceService getFinanceService() { return financeService; }
    public BudgetManager getBudgetManager() { return budgetManager; }
}