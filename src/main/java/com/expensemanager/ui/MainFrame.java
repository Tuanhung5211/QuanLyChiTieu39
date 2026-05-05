package com.expensemanager.ui;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private CardLayout cardLayout;
    private JPanel centerPanel;
    
    // Các màn hình
    private DashboardPanel dashboardPanel;
    private HistoryPanel historyPanel;
    private StatisticsPanel statisticsPanel;

    // Giả sử có một instance của Service (bạn C làm) được truyền vào đây
    // private FinanceService financeService;

    public MainFrame() {
        // this.financeService = new FinanceService(); // Khởi tạo service
        
        setTitle("Quản Lý Chi Tiêu - Expense Manager");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        initUI();
    }

    private void initUI() {
        // 1. Khởi tạo các panel màn hình
        dashboardPanel = new DashboardPanel();
        historyPanel = new HistoryPanel();
        statisticsPanel = new StatisticsPanel();

        // 2. Setup Center Panel dùng CardLayout để chuyển màn hình
        cardLayout = new CardLayout();
        centerPanel = new JPanel(cardLayout);
        
        centerPanel.add(dashboardPanel, "Dashboard");
        centerPanel.add(historyPanel, "History");
        centerPanel.add(statisticsPanel, "Statistics");

        // 3. Setup Menu bên trái (Navigation)
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBackground(Color.DARK_GRAY);
        menuPanel.setPreferredSize(new Dimension(150, 0));

        JButton btnDashboard = createMenuButton("Tổng quan");
        JButton btnHistory = createMenuButton("Lịch sử");
        JButton btnStats = createMenuButton("Thống kê");
        JButton btnAdd = createMenuButton("+ Thêm GD");

        // Xử lý sự kiện chuyển màn hình
        btnDashboard.addActionListener(e -> cardLayout.show(centerPanel, "Dashboard"));
        btnHistory.addActionListener(e -> cardLayout.show(centerPanel, "History"));
        btnStats.addActionListener(e -> cardLayout.show(centerPanel, "Statistics"));
        
        // Mở Dialog thêm giao dịch
        btnAdd.addActionListener(e -> {
            AddTransactionDialog dialog = new AddTransactionDialog(this);
            dialog.setVisible(true);
            // Sau khi thêm xong, có thể gọi hàm cập nhật dữ liệu ở đây
            // dashboardPanel.refreshData();
            // historyPanel.refreshTable();
        });

        menuPanel.add(Box.createVerticalStrut(20));
        menuPanel.add(btnDashboard);
        menuPanel.add(btnHistory);
        menuPanel.add(btnStats);
        menuPanel.add(Box.createVerticalGlue());
        menuPanel.add(btnAdd);
        menuPanel.add(Box.createVerticalStrut(20));

        // Thêm vào Frame chính
        add(menuPanel, BorderLayout.WEST);
        add(centerPanel, BorderLayout.CENTER);
    }

    private JButton createMenuButton(String text) {
        JButton btn = new JButton(text);
        btn.setMaximumSize(new Dimension(130, 40));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        return btn;
    }
}