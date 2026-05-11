package com.expensemanager.ui;

import com.expensemanager.service.*;
import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private DashboardPanel dashboardPanel;
    private HistoryPanel historyPanel;
    private StatisticsPanel statisticsPanel;
    private FinanceService financeService;
    private StatisticsService statsService;
    private BudgetManager budgetManager;

    public MainFrame() {
        financeService = new FinanceService();
        statsService = new StatisticsService(financeService);
        budgetManager = new BudgetManager(financeService);

        setTitle("Quản Lý Chi Tiêu - Dark Mode");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1250, 850);
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(18, 18, 18));
        setLayout(new BorderLayout());

        JPanel leftSidebar = new JPanel();
        leftSidebar.setLayout(new BoxLayout(leftSidebar, BoxLayout.Y_AXIS));
        leftSidebar.setBackground(new Color(25, 25, 25));
        leftSidebar.setPreferredSize(new Dimension(280, 0));
        leftSidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(50, 50, 50)));

        JPanel profileHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 25));
        profileHeader.setOpaque(false);
        profileHeader.setMaximumSize(new Dimension(280, 100));
        profileHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel avatar = new JLabel("T") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(100, 100, 100));
                g2.fillOval(0, 0, 60, 60);
                super.paintComponent(g);
            }
        };
        avatar.setPreferredSize(new Dimension(60, 60));
        avatar.setFont(new Font("Segoe UI", Font.BOLD, 25));
        avatar.setForeground(Color.WHITE);
        avatar.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel profileText = new JPanel(new GridLayout(2, 1));
        profileText.setOpaque(false);
        JLabel lblName = new JLabel("TEST");
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblName.setForeground(Color.WHITE);
        JLabel lblID = new JLabel("ID: 20260401"); 
        lblID.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblID.setForeground(new Color(160, 160, 160));
        profileText.add(lblName); profileText.add(lblID);
        profileHeader.add(avatar); profileHeader.add(profileText);
        leftSidebar.add(profileHeader);

        leftSidebar.add(Box.createVerticalStrut(20));
        leftSidebar.add(createSidebarBtn("Thành viên Premium", new Color(255, 165, 0)));
        leftSidebar.add(createSidebarBtn("Giới thiệu bạn bè", Color.WHITE));
        leftSidebar.add(createSidebarBtn("Chặn quảng cáo", Color.WHITE));
        leftSidebar.add(createSidebarBtn("Khu vườn", Color.WHITE));
        leftSidebar.add(createSidebarBtn("Cài đặt", Color.WHITE));
        leftSidebar.add(createSidebarBtn("Ứng dụng khác", Color.WHITE));
        leftSidebar.add(Box.createVerticalGlue());

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(18, 18, 18));
        topBar.setPreferredSize(new Dimension(0, 75));
        topBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(50, 50, 50)));

        JPanel navButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 20));
        navButtons.setOpaque(false);
        JButton b1 = navBtn("Tổng quan"); JButton b2 = navBtn("Lịch sử"); 
        JButton b3 = navBtn("Thống kê"); JButton b4 = navBtn("Ngân sách");
        navButtons.add(b1); navButtons.add(b2); navButtons.add(b3); navButtons.add(b4);
        topBar.add(navButtons, BorderLayout.EAST);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setOpaque(false);
        dashboardPanel = new DashboardPanel(this);
        historyPanel = new HistoryPanel(this);
        statisticsPanel = new StatisticsPanel(statsService, budgetManager);
        mainPanel.add(dashboardPanel, "dashboard");
        mainPanel.add(historyPanel, "history");
        mainPanel.add(statisticsPanel, "statistics");

        b1.addActionListener(e -> cardLayout.show(mainPanel, "dashboard"));
        b2.addActionListener(e -> { historyPanel.refreshData(); cardLayout.show(mainPanel, "history"); });
        b3.addActionListener(e -> { statisticsPanel.refreshData(); cardLayout.show(mainPanel, "statistics"); });
        b4.addActionListener(e -> new BudgetDialog(this, budgetManager).setVisible(true));

        JPanel rightSide = new JPanel(new BorderLayout());
        rightSide.setOpaque(false);
        rightSide.add(topBar, BorderLayout.NORTH);
        rightSide.add(mainPanel, BorderLayout.CENTER);
        add(leftSidebar, BorderLayout.WEST);
        add(rightSide, BorderLayout.CENTER);
        setVisible(true);
    }

    private JButton createSidebarBtn(String t, Color c) {
        JButton b = new JButton(t); b.setMaximumSize(new Dimension(280, 45));
        b.setFont(new Font("Segoe UI", Font.PLAIN, 15)); b.setForeground(c);
        b.setContentAreaFilled(false); b.setBorderPainted(false); b.setFocusPainted(false);
        b.setHorizontalAlignment(SwingConstants.LEFT); 
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setMargin(new Insets(0, 20, 0, 0));
        return b;
    }

    private JButton navBtn(String t) {
        JButton b = new JButton(t); b.setFont(new Font("Segoe UI", Font.BOLD, 15));
        b.setForeground(new Color(210, 210, 210)); b.setContentAreaFilled(false);
        b.setBorderPainted(false); b.setFocusPainted(false);
        return b;
    }

    public void refreshAllPanels() {
        dashboardPanel.refreshData(); historyPanel.refreshData(); statisticsPanel.refreshData();
    }
}