package com.expensemanager.ui;

import com.expensemanager.database.DatabaseUtil;
import com.expensemanager.entity.User;
import com.expensemanager.observer.EventType;
import com.expensemanager.observer.Observer;
import com.expensemanager.service.BudgetManager;
import com.expensemanager.service.FinanceService;
import com.expensemanager.service.SessionManager;
import com.expensemanager.service.StatisticsService;
import com.expensemanager.util.ConfigLocalStorage;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

public class MainFrame extends JFrame implements Observer {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private DashboardPanel dashboardPanel;
    private StatisticsPanel statisticsPanel;
    private BudgetPanel budgetPanel;
    private SettingsPanel settingsPanel;

    private JButton btnDashboard, btnStatistics, btnBudget, btnSettings;
    private FinanceService financeService;
    private StatisticsService statsService;
    private BudgetManager budgetManager;
    private boolean isVietnamese = true;

    // Sidebar components
    private JLabel lblAvatar, lblNickname;
    private JLabel lblIdLabel, lblIdValue;
    private JLabel lblEmailLabel, lblEmailValue;
    private JLabel lblGenderLabel, lblGenderValue;
    private JButton btnLogout;

    // === DARK THEME COLORS ===
    private final Color SIDEBAR_BG = new Color(30, 30, 30);
    private final Color AVATAR_BG = new Color(45, 45, 45);
    private final Color TEXT_PRIMARY = new Color(240, 240, 240);
    private final Color TEXT_SECONDARY = new Color(180, 180, 180);
    private final Color ACCENT_YELLOW = new Color(255, 193, 7);
    private final Color DANGER_RED = new Color(244, 67, 54);
    private final Color LOGOUT_BG_NORMAL = new Color(50, 50, 50);
    private final Color LOGOUT_BG_HOVER = DANGER_RED;
    private final Color NAV_BG = new Color(40, 40, 40);
    private final Color NAV_BTN_BG = new Color(40, 40, 40);
    private final Color NAV_BTN_HOVER = new Color(60, 60, 60);
    private final Color NAV_BTN_FG = Color.LIGHT_GRAY;
    private final Color NAV_BTN_FG_HOVER = Color.WHITE;

    public boolean isVietnamese() { return isVietnamese; }

    public MainFrame() {
        this.isVietnamese = ConfigLocalStorage.loadLanguage();
        Dimension savedSize = ConfigLocalStorage.loadWindowSize();

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
        setSize(savedSize.width, savedSize.height);
        setResizable(false);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(createSidebar(), BorderLayout.WEST);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setBackground(SIDEBAR_BG); // đồng bộ

        dashboardPanel = new DashboardPanel(this, financeService, budgetManager);
        if (statsService != null && budgetManager != null) {
            statisticsPanel = new StatisticsPanel(statsService, budgetManager);
            budgetPanel = new BudgetPanel(this, budgetManager);
        } else {
            statisticsPanel = null;
            budgetPanel = null;
        }
        settingsPanel = new SettingsPanel(this);

        mainPanel.add(dashboardPanel, "dashboard");
        if (statisticsPanel != null) mainPanel.add(statisticsPanel, "statistics");
        if (budgetPanel != null) mainPanel.add(budgetPanel, "budget");
        mainPanel.add(settingsPanel, "settings");

        add(mainPanel, BorderLayout.CENTER);
        add(createNavBar(), BorderLayout.NORTH);

        setupKeyboardShortcuts();

        if (financeService != null) {
            financeService.attach(dashboardPanel);
            if (statisticsPanel != null) financeService.attach(statisticsPanel);
            if (budgetPanel != null) financeService.attach(budgetPanel);
            financeService.attach(this);
        }

        updateGlobalLanguage(this.isVietnamese);
        setVisible(true);
        refreshSidebarData();
    }

    private void setupKeyboardShortcuts() {
        JRootPane rootPane = getRootPane();
        InputMap im = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = rootPane.getActionMap();
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK), "newTransaction");
        am.put("newTransaction", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new AddTransactionDialog(MainFrame.this).setVisible(true);
                refreshAllPanels();
            }
        });
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(240, 0));
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setLayout(new BorderLayout());
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(45, 45, 45)));

        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setOpaque(false);
        topContainer.setBorder(new EmptyBorder(30, 20, 20, 20));

        JPanel avatarRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        avatarRow.setOpaque(false);
        avatarRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblAvatar = new JLabel("A", SwingConstants.CENTER);
        lblAvatar.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblAvatar.setForeground(ACCENT_YELLOW);
        lblAvatar.setOpaque(true);
        lblAvatar.setBackground(AVATAR_BG);
        lblAvatar.setPreferredSize(new Dimension(55, 55));
        lblAvatar.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60), 1, true));
        avatarRow.add(lblAvatar);

        lblNickname = new JLabel("User");
        lblNickname.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblNickname.setForeground(TEXT_PRIMARY);
        lblNickname.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
        avatarRow.add(lblNickname);
        topContainer.add(avatarRow);
        topContainer.add(Box.createVerticalStrut(12));

        lblIdLabel = new JLabel("ID:");
        lblIdValue = new JLabel("---");
        lblEmailLabel = new JLabel("Email:");
        lblEmailValue = new JLabel("---");
        lblGenderLabel = new JLabel("Giới tính:");
        lblGenderValue = new JLabel("---");

        JPanel infoPanel = new JPanel(new GridLayout(3, 1, 0, 4));
        infoPanel.setOpaque(false);
        infoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoPanel.add(createProfileRow(lblIdLabel, lblIdValue));
        infoPanel.add(createProfileRow(lblEmailLabel, lblEmailValue));
        infoPanel.add(createProfileRow(lblGenderLabel, lblGenderValue));
        topContainer.add(infoPanel);
        topContainer.add(Box.createVerticalGlue());
        sidebar.add(topContainer, BorderLayout.CENTER);

        JPanel bottomContainer = new JPanel(new BorderLayout());
        bottomContainer.setOpaque(false);
        bottomContainer.setBorder(new EmptyBorder(15, 15, 20, 15));

        btnLogout = new JButton("Đăng xuất");
        styleButton(btnLogout, LOGOUT_BG_NORMAL, TEXT_PRIMARY, 14, true);
        btnLogout.addActionListener(e -> logout());
        btnLogout.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btnLogout.setBackground(LOGOUT_BG_HOVER); btnLogout.setForeground(Color.WHITE); }
            public void mouseExited(MouseEvent e) { btnLogout.setBackground(LOGOUT_BG_NORMAL); btnLogout.setForeground(TEXT_PRIMARY); }
        });
        bottomContainer.add(btnLogout, BorderLayout.CENTER);
        sidebar.add(bottomContainer, BorderLayout.SOUTH);
        return sidebar;
    }

    private JPanel createProfileRow(JLabel lblLabel, JLabel lblValue) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        lblLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblLabel.setForeground(TEXT_SECONDARY);
        lblLabel.setPreferredSize(new Dimension(70, 22));
        lblValue.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblValue.setForeground(TEXT_PRIMARY);
        row.add(lblLabel, BorderLayout.WEST);
        row.add(lblValue, BorderLayout.CENTER);
        return row;
    }

    private void refreshSidebarData() {
        String username = SessionManager.getCurrentUsername();
        if (username != null) {
            User user = DatabaseUtil.getUserByUsername(username);
            if (user != null) {
                lblNickname.setText(user.getNickname());
                lblIdValue.setText(user.getId() != null ? user.getId() : "c4929946");
                String email = user.getEmail();
                if (email != null && email.length() > 18) email = email.substring(0, 16) + "...";
                lblEmailValue.setText(email != null ? email : "---");
                String gender = user.getGender();
                if (isVietnamese) {
                    if ("Male".equalsIgnoreCase(gender) || "Nam".equalsIgnoreCase(gender)) lblGenderValue.setText("Nam");
                    else if ("Female".equalsIgnoreCase(gender) || "Nữ".equalsIgnoreCase(gender)) lblGenderValue.setText("Nữ");
                    else lblGenderValue.setText("Khác");
                } else {
                    if ("Male".equalsIgnoreCase(gender) || "Nam".equalsIgnoreCase(gender)) lblGenderValue.setText("Male");
                    else if ("Female".equalsIgnoreCase(gender) || "Nữ".equalsIgnoreCase(gender)) lblGenderValue.setText("Female");
                    else lblGenderValue.setText("Other");
                }
                if (user.getNickname() != null && !user.getNickname().isEmpty())
                    lblAvatar.setText(user.getNickname().substring(0, 1).toUpperCase());
            }
        }
    }

    private JPanel createNavBar() {
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 12));
        navPanel.setBackground(NAV_BG);
        navPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(60, 60, 60)));

        btnDashboard = createNavButton(isVietnamese ? "Tổng quan" : "Overview");
        btnStatistics = createNavButton(isVietnamese ? "Thống kê" : "Statistics");
        btnBudget = createNavButton(isVietnamese ? "Ngân sách" : "Budget");
        btnSettings = createNavButton(isVietnamese ? "Cài đặt" : "Settings");

        btnDashboard.addActionListener(e -> { dashboardPanel.refreshData(); cardLayout.show(mainPanel, "dashboard"); });
        btnStatistics.addActionListener(e -> { if (statisticsPanel != null) { statisticsPanel.refreshData(); cardLayout.show(mainPanel, "statistics"); } });
        btnBudget.addActionListener(e -> { if (budgetPanel != null) { budgetPanel.refreshData(); cardLayout.show(mainPanel, "budget"); } });
        btnSettings.addActionListener(e -> { settingsPanel.refreshData(); cardLayout.show(mainPanel, "settings"); });

        navPanel.add(btnDashboard); navPanel.add(btnStatistics); navPanel.add(btnBudget); navPanel.add(btnSettings);
        return navPanel;
    }

    private JButton createNavButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setForeground(NAV_BTN_FG);
        btn.setBackground(NAV_BTN_BG);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 22, 10, 22));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setForeground(NAV_BTN_FG_HOVER); btn.setBackground(NAV_BTN_HOVER); }
            public void mouseExited(MouseEvent e) { btn.setForeground(NAV_BTN_FG); btn.setBackground(NAV_BTN_BG); }
        });
        return btn;
    }

    private void styleButton(JButton btn, Color bg, Color fg, int fontSize, boolean bold) {
        btn.setFont(new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, fontSize));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public void updateGlobalLanguage(boolean isVN) {
        this.isVietnamese = isVN;
        ConfigLocalStorage.saveConfig(this.isVietnamese, this.getWidth(), this.getHeight());

        if (btnDashboard != null) btnDashboard.setText(isVN ? "Tổng quan" : "Overview");
        if (btnStatistics != null) btnStatistics.setText(isVN ? "Thống kê" : "Statistics");
        if (btnBudget != null) btnBudget.setText(isVN ? "Ngân sách" : "Budget");
        if (btnSettings != null) btnSettings.setText(isVN ? "Cài đặt" : "Settings");

        if (lblIdLabel != null) {
            lblIdLabel.setText("ID:");
            lblEmailLabel.setText("Email:");
            lblGenderLabel.setText(isVN ? "Giới tính:" : "Gender:");
            btnLogout.setText(isVN ? "Đăng xuất" : "Logout");
            refreshSidebarData();
        }

        if (settingsPanel != null) settingsPanel.updateLanguageAndResponsive(isVN, this.getWidth());
        if (dashboardPanel != null) { dashboardPanel.updateLanguageText(isVN); dashboardPanel.refreshData(); }
        if (statisticsPanel != null) { statisticsPanel.updateLanguageText(isVN); statisticsPanel.refreshData(); }
        if (budgetPanel != null) { budgetPanel.updateLanguageText(isVN); budgetPanel.refreshData(); }

        this.revalidate(); this.repaint();
    }

    public void changeWindowSize(int width, int height) {
        setResizable(true);
        setSize(width, height);
        setLocationRelativeTo(null);
        setResizable(false);
        ConfigLocalStorage.saveConfig(this.isVietnamese, width, height);
        if (settingsPanel != null) settingsPanel.updateLanguageAndResponsive(this.isVietnamese, width);
        this.revalidate(); this.repaint();
    }

    public void refreshAllPanels() {
        if (financeService != null) financeService.syncFromDatabase();
    }

    private void logout() {
        SessionManager.logout();
        dispose();
        new LoginFrame().setVisible(true);
    }

    @Override
    public void update(EventType eventType, Object data) {
        if (eventType == EventType.TRANSACTION_ADDED || eventType == EventType.DATA_LOADED)
            SwingUtilities.invokeLater(() -> refreshSidebarData());
    }

    public FinanceService getFinanceService() { return financeService; }
    public BudgetManager getBudgetManager() { return budgetManager; }
}