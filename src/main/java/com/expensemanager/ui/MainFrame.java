package com.expensemanager.ui;

import com.expensemanager.database.DatabaseUtil;
import com.expensemanager.entity.User;
import com.expensemanager.observer.EventType;
import com.expensemanager.observer.Observer;
import com.expensemanager.service.*;
import com.expensemanager.util.ConfigLocalStorage;
import com.expensemanager.util.PremiumManager;
import com.expensemanager.util.ThemeManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MainFrame extends JFrame implements Observer {

    private CardLayout cardLayout;
    private JPanel mainPanel;

    private DashboardPanel dashboardPanel;
    private StatisticsPanel statisticsPanel;
    private BudgetPanel budgetPanel;
    private RecurringTransactionManagerPanel recurringPanel;
    private SettingsPanel settingsPanel;

    // Đã loại bỏ nút Nhắc nhở (btnReminder) trên thanh điều hướng Taskbar
    private JButton btnDashboard, btnStatistics, btnBudget, btnRecurring, btnSettings;
    private JButton activeBtn;

    private FinanceService financeService;
    private StatisticsService statsService;
    private BudgetManager budgetManager;

    private boolean isVietnamese;

    // Sidebar components
    private JPanel sidebarPanel;
    private JPanel navPanel;
    private JLabel lblAvatar, lblNickname;
    private JLabel lblIdLabel, lblIdValue;
    private JLabel lblEmailLabel, lblEmailValue;
    private JLabel lblGenderLabel, lblGenderValue;
    private JButton btnLogout;

    // Ad banner
    private AdBanner adBanner;
    private boolean isPremium;

    public MainFrame() {
        this.isVietnamese = ConfigLocalStorage.loadLanguage();
        Dimension savedSize = ConfigLocalStorage.loadWindowSize();

        initServices();
        initFrameSettings(savedSize);
        initComponents();

        if (financeService != null) {
            financeService.attach(dashboardPanel);
            if (statisticsPanel != null) financeService.attach(statisticsPanel);
            if (budgetPanel != null) financeService.attach(budgetPanel);
            financeService.attach(this);
        }

        // Lắng nghe sự kiện thay đổi giao diện (Theme)
        ThemeManager.addThemeListener(this::applyThemeToAll);
        applyThemeToAll();

        updateGlobalLanguage(this.isVietnamese);
        selectTab(btnDashboard, "dashboard");
        refreshSidebarData();

        setVisible(true);
    }

    private void initServices() {
        try {
            financeService = new FinanceService();
            financeService.syncFromDatabase();
        } catch (Exception e) {
            System.err.println("Lỗi khởi tạo FinanceService: " + e.getMessage());
            financeService = null;
        }
        if (financeService != null) {
            statsService = new StatisticsService(financeService);
            ReminderService reminderService = new ReminderService(financeService);
            budgetManager = new BudgetManager(financeService, reminderService);
            reminderService.setBudgetManager(budgetManager);
        } else {
            statsService = null;
            budgetManager = null;
        }
    }

    private void initFrameSettings(Dimension size) {
        setTitle("Money Tracker Desktop");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(size.width, size.height);
        setResizable(false);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
    }

    private void initComponents() {
        sidebarPanel = createSidebar();
        add(sidebarPanel, BorderLayout.WEST);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        dashboardPanel = new DashboardPanel(this, financeService, budgetManager);

        if (statsService != null && budgetManager != null) {
            statisticsPanel = new StatisticsPanel(statsService, budgetManager);
            budgetPanel = new BudgetPanel(this, budgetManager);
        }

        // Khởi tạo Recurring Transaction Panel
        if (financeService != null) {
            recurringPanel = new RecurringTransactionManagerPanel(
                    financeService.getRecurringTransactionService(), this);
        }

        settingsPanel = new SettingsPanel(this);

        mainPanel.add(dashboardPanel, "dashboard");
        if (statisticsPanel != null) mainPanel.add(statisticsPanel, "statistics");
        if (budgetPanel != null) mainPanel.add(budgetPanel, "budget");
        if (recurringPanel != null) mainPanel.add(recurringPanel, "recurring");
        mainPanel.add(settingsPanel, "settings");

        add(mainPanel, BorderLayout.CENTER);

        navPanel = createNavBar();
        add(navPanel, BorderLayout.NORTH);

        // Kiểm tra tài khoản premium để ẩn/hiển thị biểu ngữ quảng cáo
        isPremium = PremiumManager.isPremium(SessionManager.getCurrentUserId());
        if (!isPremium) {
            adBanner = new AdBanner(isVietnamese, this::showPremiumDialog);
            add(adBanner, BorderLayout.SOUTH);
        }
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(240, 0));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, ThemeManager.getColor("border")));

        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setOpaque(false);
        topContainer.setBorder(new EmptyBorder(30, 20, 20, 20));

        JPanel avatarRow = new JPanel(new GridBagLayout());
        avatarRow.setOpaque(false);
        avatarRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblAvatar = new JLabel("A", SwingConstants.CENTER);
        lblAvatar.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblAvatar.setForeground(ThemeManager.getColor("accent"));
        lblAvatar.setOpaque(true);
        lblAvatar.setBackground(ThemeManager.getColor("input"));
        lblAvatar.setPreferredSize(new Dimension(55, 55));
        lblAvatar.setBorder(BorderFactory.createLineBorder(ThemeManager.getColor("border"), 1, true));

        lblNickname = new JLabel("User");
        lblNickname.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblNickname.setForeground(ThemeManager.getColor("textPrimary"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        avatarRow.add(lblAvatar, gbc);

        gbc.gridx = 1;
        gbc.insets = new Insets(0, 15, 0, 0);
        avatarRow.add(lblNickname, gbc);

        avatarRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, avatarRow.getPreferredSize().height));
        topContainer.add(avatarRow);

        topContainer.add(Box.createVerticalStrut(15));

        lblIdLabel = new JLabel("ID:");
        lblIdValue = new JLabel("---");
        lblEmailLabel = new JLabel("Email:");
        lblEmailValue = new JLabel("---");
        lblGenderLabel = new JLabel("Giới tính:");
        lblGenderValue = new JLabel("---");

        JPanel infoPanel = new JPanel(new GridLayout(3, 1, 0, 6));
        infoPanel.setOpaque(false);
        infoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoPanel.add(createProfileRow(lblIdLabel, lblIdValue));
        infoPanel.add(createProfileRow(lblEmailLabel, lblEmailValue));
        infoPanel.add(createProfileRow(lblGenderLabel, lblGenderValue));

        infoPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, infoPanel.getPreferredSize().height));
        topContainer.add(infoPanel);
        topContainer.add(Box.createVerticalGlue());
        sidebar.add(topContainer, BorderLayout.CENTER);

        JPanel bottomContainer = new JPanel(new BorderLayout());
        bottomContainer.setOpaque(false);
        bottomContainer.setBorder(new EmptyBorder(15, 15, 20, 15));

        btnLogout = new JButton("Đăng xuất");
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogout.setBackground(ThemeManager.getColor("input"));
        btnLogout.setForeground(ThemeManager.getColor("textPrimary"));
        btnLogout.setFocusPainted(false);
        btnLogout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogout.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));

        btnLogout.addActionListener(e -> logout());
        btnLogout.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnLogout.setBackground(ThemeManager.getColor("danger"));
                btnLogout.setForeground(Color.WHITE);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btnLogout.setBackground(ThemeManager.getColor("input"));
                btnLogout.setForeground(ThemeManager.getColor("textPrimary"));
            }
        });
        bottomContainer.add(btnLogout, BorderLayout.CENTER);
        sidebar.add(bottomContainer, BorderLayout.SOUTH);

        return sidebar;
    }

    private JPanel createProfileRow(JLabel lblLabel, JLabel lblValue) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        lblLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblLabel.setForeground(ThemeManager.getColor("textSecondary"));
        lblLabel.setPreferredSize(new Dimension(75, 22));

        lblValue.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblValue.setForeground(ThemeManager.getColor("textPrimary"));
        row.add(lblLabel, BorderLayout.WEST);
        row.add(lblValue, BorderLayout.CENTER);
        return row;
    }

    private JPanel createNavBar() {
        // Tăng khoảng giãn cách Layout Flow từ 30 lên 40 vì số lượng nút bấm đã giảm xuống
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 12));
        navPanel.setBackground(ThemeManager.getColor("surface"));
        navPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeManager.getColor("border")));

        btnDashboard = createNavButton(isVietnamese ? "Tổng quan" : "Overview");
        btnStatistics = createNavButton(isVietnamese ? "Thống kê" : "Statistics");
        btnBudget = createNavButton(isVietnamese ? "Ngân sách" : "Budget");
        btnRecurring = createNavButton(isVietnamese ? "Lặp lại" : "Recurring");
        btnSettings = createNavButton(isVietnamese ? "Cài đặt" : "Settings");

        btnDashboard.addActionListener(e -> {
            selectTab(btnDashboard, "dashboard");
            dashboardPanel.refreshData();
        });
        btnStatistics.addActionListener(e -> {
            if (statisticsPanel != null) {
                selectTab(btnStatistics, "statistics");
                statisticsPanel.refreshData();
            }
        });
        btnBudget.addActionListener(e -> {
            if (budgetPanel != null) {
                selectTab(btnBudget, "budget");
                budgetPanel.refreshData();
            }
        });
        btnRecurring.addActionListener(e -> {
            if (recurringPanel != null) {
                selectTab(btnRecurring, "recurring");
            }
        });
        btnSettings.addActionListener(e -> {
            selectTab(btnSettings, "settings");
            settingsPanel.refreshData();
        });

        navPanel.add(btnDashboard);
        navPanel.add(btnStatistics);
        navPanel.add(btnBudget);
        navPanel.add(btnRecurring);
        navPanel.add(btnSettings);

        return navPanel;
    }

    private JButton createNavButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setForeground(ThemeManager.getColor("textSecondary"));
        btn.setBackground(ThemeManager.getColor("surface"));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 22, 10, 22));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent evt) {
                if (btn != activeBtn) btn.setForeground(ThemeManager.getColor("textPrimary"));
            }
            @Override
            public void mouseExited(MouseEvent evt) {
                if (btn != activeBtn) btn.setForeground(ThemeManager.getColor("textSecondary"));
            }
        });
        return btn;
    }

    private void refreshSidebarData() {
        String username = SessionManager.getCurrentUsername();
        if (username == null) return;
        User user = DatabaseUtil.getUserByUsername(username);
        if (user == null) return;

        String nickname = user.getNickname();
        if (nickname != null && nickname.length() > 14) nickname = nickname.substring(0, 12) + "...";
        lblNickname.setText(nickname != null ? nickname : "User");

        lblIdValue.setText(user.getId() != null ? user.getId() : "N/A");

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

        if (user.getNickname() != null && !user.getNickname().isEmpty()) {
            lblAvatar.setText(user.getNickname().substring(0, 1).toUpperCase());
        }
    }

    private void selectTab(JButton targetBtn, String cardName) {
        activeBtn = targetBtn;
        cardLayout.show(mainPanel, cardName);

        JButton[] navButtons = {btnDashboard, btnStatistics, btnBudget, btnRecurring, btnSettings};
        for (JButton btn : navButtons) {
            if (btn != null) {
                if (btn == activeBtn) {
                    btn.setForeground(ThemeManager.getColor("accent"));
                    btn.setBackground(ThemeManager.getColor("surface"));
                } else {
                    btn.setForeground(ThemeManager.getColor("textSecondary"));
                    btn.setBackground(ThemeManager.getColor("surface"));
                }
            }
        }
    }

    public void updateGlobalLanguage(boolean isVN) {
        this.isVietnamese = isVN;
        SessionManager.setLanguage(isVN ? "vi" : "en");
        ConfigLocalStorage.saveConfig(this.isVietnamese, this.getWidth(), this.getHeight());

        if (btnDashboard != null) btnDashboard.setText(isVN ? "Tổng quan" : "Overview");
        if (btnStatistics != null) btnStatistics.setText(isVN ? "Thống kê" : "Statistics");
        if (btnBudget != null) btnBudget.setText(isVN ? "Ngân sách" : "Budget");
        if (btnRecurring != null) btnRecurring.setText(isVN ? "Lặp lại" : "Recurring");
        if (btnSettings != null) btnSettings.setText(isVN ? "Cài đặt" : "Settings");

        if (lblIdLabel != null) {
            lblIdLabel.setText("ID:");
            lblEmailLabel.setText("Email:");
            lblGenderLabel.setText(isVN ? "Giới tính:" : "Gender:");
            btnLogout.setText(isVN ? "Đăng xuất" : "Logout");
            refreshSidebarData();
        }

        if (adBanner != null) adBanner.updateLanguage(isVN);
        if (settingsPanel != null) settingsPanel.updateLanguageAndResponsive(isVN, this.getWidth());

        if (dashboardPanel != null) {
            dashboardPanel.updateLanguageText(isVN);
            dashboardPanel.refreshData();
        }
        if (statisticsPanel != null) {
            statisticsPanel.updateLanguageText(isVN);
            statisticsPanel.refreshData();
        }
        if (budgetPanel != null) {
            budgetPanel.updateLanguageText(isVN);
            budgetPanel.refreshData();
        }
        if (recurringPanel != null) {
            recurringPanel.updateLanguageText();
        }

        this.revalidate();
        this.repaint();
    }

    // PHƯƠNG THỨC THAY ĐỔI KÍCH THƯỚC CỬA SỔ TỪ SETTINGS
    public void changeWindowSize(int width, int height) {
        setResizable(true);
        setSize(width, height);
        setLocationRelativeTo(null);
        setResizable(false);
        ConfigLocalStorage.saveConfig(this.isVietnamese, width, height);
        if (settingsPanel != null) settingsPanel.updateLanguageAndResponsive(this.isVietnamese, width);
        this.revalidate();
        this.repaint();
    }

    // PHƯƠNG THỨC LÀM MỚI DỮ LIỆU ĐỒNG BỘ TOÀN BỘ CÁC PANEL
    public void refreshAllPanels() {
        if (financeService != null) {
            financeService.syncFromDatabase();
        }
        if (dashboardPanel != null) dashboardPanel.refreshData();
        if (statisticsPanel != null) statisticsPanel.refreshData();
        if (budgetPanel != null) budgetPanel.refreshData();
        if (recurringPanel != null) recurringPanel.updateLanguageText();
    }

    public void refreshAllPanelsThemes() {
        applyThemeToAll();
    }

    private void logout() {
        SessionManager.logout();
        dispose();
        new LoginFrame().setVisible(true);
    }

    private void showPremiumDialog() {
        int confirm = JOptionPane.showConfirmDialog(this,
                isVietnamese ? "Đăng ký Premium với giá 30,000đ/tháng?\nSau khi đăng ký, bạn có thể tùy chỉnh giao diện và không còn quảng cáo." : "Upgrade to Premium for 30,000 VND/month?\nYou can customize theme and remove ads.",
                isVietnamese ? "Xác nhận" : "Confirm", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            PremiumManager.activatePremium(SessionManager.getCurrentUserId(), 30);
            isPremium = true;
            if (adBanner != null) {
                remove(adBanner);
                adBanner = null;
                revalidate();
                repaint();
            }
            JOptionPane.showMessageDialog(this, isVietnamese ? "Cảm ơn bạn đã nâng cấp!" : "Thank you for upgrading!");
        }
    }

    private void applyThemeToAll() {
        getContentPane().setBackground(ThemeManager.getColor("bg"));

        if (sidebarPanel != null) {
            sidebarPanel.setBackground(ThemeManager.getColor("surface"));
            sidebarPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, ThemeManager.getColor("border")));
        }

        if (navPanel != null) {
            navPanel.setBackground(ThemeManager.getColor("surface"));
            navPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeManager.getColor("border")));
            for (Component comp : navPanel.getComponents()) {
                if (comp instanceof JButton) {
                    JButton btn = (JButton) comp;
                    if (btn == activeBtn) {
                        btn.setForeground(ThemeManager.getColor("accent"));
                    } else {
                        btn.setForeground(ThemeManager.getColor("textSecondary"));
                    }
                    btn.setBackground(ThemeManager.getColor("surface"));
                }
            }
        }

        if (adBanner != null) adBanner.applyTheme();
        if (dashboardPanel != null) dashboardPanel.applyTheme();
        if (statisticsPanel != null) statisticsPanel.applyTheme();
        if (budgetPanel != null) budgetPanel.applyTheme();
        if (recurringPanel != null) recurringPanel.updateLanguageText();
        if (settingsPanel != null) settingsPanel.applyTheme();

        refreshSidebarData();
        this.revalidate();
        this.repaint();
    }

    public boolean isVietnamese() {
        return this.isVietnamese;
    }

    public FinanceService getFinanceService() {
        return financeService;
    }

    public BudgetManager getBudgetManager() {
        return budgetManager;
    }

    @Override
    public void update(EventType eventType, Object data) {
        if (eventType == EventType.TRANSACTION_ADDED || eventType == EventType.DATA_LOADED) {
            SwingUtilities.invokeLater(this::refreshSidebarData);
        }
    }
}