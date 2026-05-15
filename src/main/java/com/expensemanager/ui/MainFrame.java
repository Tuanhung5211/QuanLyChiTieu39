package com.expensemanager.ui;

import com.expensemanager.database.DatabaseUtil;
import com.expensemanager.entity.User;
import com.expensemanager.service.BudgetManager;
import com.expensemanager.service.FinanceService;
import com.expensemanager.service.SessionManager;
import com.expensemanager.service.StatisticsService;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private JPanel sidebar;
    private DashboardPanel dashboardPanel;
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

        // Trả lại nguyên vẹn khối if-else ban đầu
        if (financeService != null) {
            statsService = new StatisticsService(financeService);
            budgetManager = new BudgetManager(financeService);
        } else {
            statsService = null;
            budgetManager = null;
        }

        setTitle("Money Tracker Desktop");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(18, 18, 18));
        setLayout(new BorderLayout());

        sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setBackground(new Color(18, 18, 18));

        // Khởi tạo các Panel
        dashboardPanel = new DashboardPanel(this, financeService, budgetManager);
        if (statsService != null && budgetManager != null) {
            statisticsPanel = new StatisticsPanel(statsService, budgetManager);
        } else {
            statisticsPanel = null;
        }
        settingsPanel = new SettingsPanel(this);

        // Thêm vào Main Panel
        mainPanel.add(dashboardPanel, "dashboard");
        if (statisticsPanel != null) {
            mainPanel.add(statisticsPanel, "statistics");
        }
        mainPanel.add(settingsPanel, "settings");

        add(mainPanel, BorderLayout.CENTER);
        setVisible(true);
    }

    private JPanel createSidebar() {
        JPanel p = new JPanel();
        p.setPreferredSize(new Dimension(280, 800));
        p.setBackground(new Color(25, 25, 25));
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(45, 45, 45)));

        // Lấy Nickname động từ Session & DB
        String currentUsername = SessionManager.getCurrentUsername();
        String displayNick = "Ẩn danh";
        if (currentUsername != null) {
            User currentUser = DatabaseUtil.getUserByUsername(currentUsername);
            if (currentUser != null && currentUser.getNickname() != null && !currentUser.getNickname().trim().isEmpty()) {
                displayNick = currentUser.getNickname();
            } else {
                displayNick = currentUsername;
            }
        }

        // --- PROFILE SECTION ---
        JPanel profileBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 30));
        profileBox.setBackground(new Color(25, 25, 25));
        profileBox.setMaximumSize(new Dimension(280, 120));
        profileBox.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(45, 45, 45)));

        JLabel lblAvatar = new JLabel("👤", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(55, 60, 65));
                g2.fillOval(0, 0, getWidth(), getHeight());
                super.paintComponent(g);
            }
        };
        lblAvatar.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 26));
        lblAvatar.setForeground(Color.LIGHT_GRAY);
        lblAvatar.setPreferredSize(new Dimension(55, 55));

        JLabel lblUser = new JLabel(displayNick);
        lblUser.setForeground(Color.WHITE);
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 22));

        profileBox.add(lblAvatar);
        profileBox.add(lblUser);
        p.add(profileBox);
        p.add(Box.createRigidArea(new Dimension(0, 15)));

        // Các nút chức năng
        p.add(createSideBtn("Tổng quan", "dashboard", "🏠"));
        p.add(createSideBtn("Biểu đồ", "statistics", "📊"));
        p.add(createSideBtn("Ngân sách", "budget", "💰"));
        p.add(createSideBtn("Cài đặt", "settings", "⚙️"));

        return p;
    }

    private JButton createSideBtn(String text, String cardName, String icon) {
        JButton btn = new JButton(icon + "   " + text);
        btn.setMaximumSize(new Dimension(280, 55));
        btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 15));
        btn.setForeground(new Color(200, 200, 200));
        btn.setBackground(new Color(25, 25, 25));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(35, 35, 35)),
                BorderFactory.createEmptyBorder(10, 25, 10, 10)
        ));

        btn.addActionListener(e -> {
            if ("budget".equals(cardName)) {
                if (budgetManager != null) {
                    new BudgetPanel(this, budgetManager).setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(this, "Ngân sách chưa sẵn sàng.");
                }
            } else {
                refreshAllPanels();
                cardLayout.show(mainPanel, cardName);
            }
        });

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(new Color(40, 40, 40));
                btn.setForeground(new Color(255, 193, 7));
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(new Color(25, 25, 25));
                btn.setForeground(new Color(200, 200, 200));
            }
        });
        return btn;
    }

    // Bung toàn bộ các khối try-catch ra đầy đủ như cũ
    public void refreshAllPanels() {
        if (financeService != null) {
            financeService.syncFromDatabase();
        }

        try {
            if (dashboardPanel != null) {
                dashboardPanel.refreshData();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (statisticsPanel != null) {
                statisticsPanel.refreshData();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (settingsPanel != null) {
                settingsPanel.refreshData();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Các hàm Getter
    public FinanceService getFinanceService() {
        return financeService;
    }

    public BudgetManager getBudgetManager() {
        return budgetManager;
    }
}