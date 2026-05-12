package com.expensemanager.ui;

import com.expensemanager.observer.EventType;
import com.expensemanager.observer.Observer;
import com.expensemanager.service.BudgetManager;
import com.expensemanager.service.FinanceService;
import com.expensemanager.service.StatisticsService;

import javax.swing.*;
import java.awt.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainFrame extends JFrame implements Observer {

    private static final Logger LOGGER = Logger.getLogger(MainFrame.class.getName());

    private final CardLayout cardLayout;
    private final JPanel mainPanel;
    private final DashboardPanel dashboardPanel;
    private final HistoryPanel historyPanel;
    private final StatisticsPanel statisticsPanel;

    // Services
    private final FinanceService financeService;
    private final StatisticsService statsService;
    private final BudgetManager budgetManager;

    public MainFrame() {
        // Khoi tao service
        financeService = new FinanceService();
        financeService.attach(this);

        statsService = new StatisticsService(financeService);
        budgetManager = new BudgetManager(financeService);

        setTitle("Quan Ly Chi Tieu");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 650);
        setLocationRelativeTo(null);

        // CardLayout de chuyen doi giua cac man hinh
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Khoi tao cac Panel con
        dashboardPanel = new DashboardPanel(this);
        historyPanel = new HistoryPanel(financeService);  // ✅ DA SUA: truyen financeService
        statisticsPanel = new StatisticsPanel(statsService, budgetManager);

        // Them cac Panel vao mainPanel
        mainPanel.add(dashboardPanel, "dashboard");
        mainPanel.add(historyPanel, "history");
        mainPanel.add(statisticsPanel, "statistics");

        // Tao thanh dieu huong
        JPanel navBar = createNavBar();

        add(navBar, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);

        // Tai du lieu ban dau
        loadInitialData();

        setVisible(true);
    }

    private JPanel createNavBar() {
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));

        JButton btnDashboard = new JButton("Tong quan");
        JButton btnHistory = new JButton("Lich su");
        JButton btnStatistics = new JButton("Thong ke");
        JButton btnBudget = new JButton("Ngan sach");

        btnDashboard.addActionListener(e -> cardLayout.show(mainPanel, "dashboard"));
        btnHistory.addActionListener(e -> cardLayout.show(mainPanel, "history"));
        btnStatistics.addActionListener(e -> {
            statisticsPanel.refreshData();
            cardLayout.show(mainPanel, "statistics");
        });
        btnBudget.addActionListener(e -> {
            new BudgetDialog(this, budgetManager).setVisible(true);
            refreshAllPanels();
        });

        navPanel.add(btnDashboard);
        navPanel.add(btnHistory);
        navPanel.add(btnStatistics);
        navPanel.add(btnBudget);

        return navPanel;
    }

    private void loadInitialData() {
        try {
            financeService.loadInitialData();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Loi tai du lieu", e);
            JOptionPane.showMessageDialog(this,
                    "Loi tai du lieu: " + e.getMessage(),
                    "Loi", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void update(EventType eventType, Object data) {
        SwingUtilities.invokeLater(() -> {
            switch (eventType) {
                case TRANSACTION_ADDED:
                    showNotification("Da them giao dich moi!");
                    break;
                case TRANSACTION_DELETED:
                    showNotification("Da xoa giao dich!");
                    break;
                case TRANSACTION_UPDATED:
                    showNotification("Da cap nhat giao dich!");
                    break;
                case BUDGET_CHANGED:
                    if (data instanceof FinanceService.BudgetAlert) {
                        showBudgetWarning((FinanceService.BudgetAlert) data);
                    }
                    break;
                case DATA_LOADED:
                    showNotification("Da tai du lieu thanh cong!");
                    break;
                default:
                    break;
            }
        });
    }

    private void showNotification(String message) {
        JDialog notification = new JDialog(this);
        notification.setUndecorated(true);
        notification.setAlwaysOnTop(true);

        JLabel label = new JLabel(" " + message + " ");
        label.setFont(new Font("Arial", Font.PLAIN, 14));
        label.setForeground(Color.WHITE);
        label.setBackground(new Color(0, 0, 0, 200));
        label.setOpaque(true);
        label.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        notification.add(label);
        notification.pack();

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = screenSize.width - notification.getWidth() - 20;
        int y = screenSize.height - notification.getHeight() - 50;
        notification.setLocation(x, y);

        Timer timer = new Timer(2000, e -> notification.dispose());
        timer.setRepeats(false);
        timer.start();

        notification.setVisible(true);
    }

    private void showBudgetWarning(FinanceService.BudgetAlert alert) {
        String message = String.format("""
            CANH BAO NGAN SACH!
            
            Da chi: %,.0f VND
            Gioi han: %,.0f VND
            Vuot: %,.0f VND (%.0f%%)
            
            Hay kiem tra lai chi tieu!
            """,
                alert.getCurrentSpent(), alert.getLimit(),
                alert.getExcess(), alert.getPercentage()
        );

        JOptionPane.showMessageDialog(this, message, "Canh bao ngan sach",
                JOptionPane.WARNING_MESSAGE);
    }

    public void refreshAllPanels() {
        dashboardPanel.refreshData();
        historyPanel.refreshData();
        statisticsPanel.refreshData();
    }
}