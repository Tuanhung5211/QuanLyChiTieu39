package com.expensemanager.jfx;

import com.expensemanager.database.DatabaseUtil;
import com.expensemanager.entity.User;
import com.expensemanager.service.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class MainView {
    private final Stage primaryStage;
    private boolean isVietnamese;
    private BorderPane root;
    private VBox sidebar;
    private StackPane contentArea;
    private DashboardView dashboardView;
    private ExpensesView expensesView;
    private StatisticsView statisticsView;
    private BudgetView budgetView;
    private SettingsView settingsView;
    private FinanceService financeService;
    private BudgetManager budgetManager;
    private StatisticsService statisticsService;
    private Button btnDashboard, btnExpenses, btnStatistics, btnBudget, btnSettings, btnLogout;

    public MainView(Stage primaryStage, boolean isVietnamese) {
        this.primaryStage = primaryStage;
        this.isVietnamese = isVietnamese;
        initServices();
        buildUI();
        showDashboard();
    }

    private void initServices() {
        financeService = new FinanceService();
        budgetManager = new BudgetManager(financeService);
        statisticsService = new StatisticsService(financeService);
        financeService.syncFromDatabase();
    }

    private void buildUI() {
        root = new BorderPane();
        sidebar = new VBox(8);
        sidebar.setPadding(new Insets(24, 16, 24, 16));
        sidebar.setMinWidth(240);
        sidebar.setStyle("-fx-background-color: #f8fafc;");

        User currentUser = DatabaseUtil.getUserByUsername(SessionManager.getCurrentUsername());
        VBox userBox = new VBox(6);
        userBox.setAlignment(Pos.CENTER_LEFT);
        Label lblUserName = new Label(currentUser != null ? currentUser.getNickname() : "User");
        Label lblUserEmail = new Label(currentUser != null ? currentUser.getEmail() : "");
        userBox.getChildren().addAll(lblUserName, lblUserEmail);

        btnDashboard = createSidebarButton(isVietnamese ? "🏠 Trang chính" : "🏠 Dashboard", "dashboard");
        btnExpenses = createSidebarButton(isVietnamese ? "💰 Giao dịch" : "💰 Expenses", "expenses");
        btnStatistics = createSidebarButton(isVietnamese ? "📊 Thống kê" : "📊 Statistics", "statistics");
        btnBudget = createSidebarButton(isVietnamese ? "🎯 Ngân sách" : "🎯 Budget", "budget");
        btnSettings = createSidebarButton(isVietnamese ? "⚙️ Cài đặt" : "⚙️ Settings", "settings");
        btnLogout = createSidebarButton(isVietnamese ? "🚪 Đăng xuất" : "🚪 Logout", "logout");

        VBox.setVgrow(new Region(), Priority.ALWAYS);
        sidebar.getChildren().addAll(userBox, new Separator(), btnDashboard, btnExpenses,
                btnStatistics, btnBudget, btnSettings, new Region(), btnLogout);
        contentArea = new StackPane();
        contentArea.setPadding(new Insets(24));
        root.setLeft(sidebar);
        root.setCenter(contentArea);
    }

    private Button createSidebarButton(String text, String id) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setId(id);
        btn.setOnAction(e -> {
            switch (id) {
                case "dashboard" -> showDashboard();
                case "expenses" -> showExpenses();
                case "statistics" -> showStatistics();
                case "budget" -> showBudget();
                case "settings" -> showSettings();
                case "logout" -> logout();
            }
        });
        return btn;
    }

    private void showDashboard() {
        if (dashboardView == null) dashboardView = new DashboardView(financeService, budgetManager, statisticsService, isVietnamese);
        contentArea.getChildren().setAll(dashboardView.getRoot());
    }
    private void showExpenses() {
        if (expensesView == null) expensesView = new ExpensesView(financeService, isVietnamese);
        contentArea.getChildren().setAll(expensesView.getRoot());
    }
    private void showStatistics() {
        if (statisticsView == null) statisticsView = new StatisticsView(statisticsService, budgetManager, isVietnamese);
        contentArea.getChildren().setAll(statisticsView.getRoot());
    }
    private void showBudget() {
        if (budgetView == null) budgetView = new BudgetView(budgetManager, financeService, isVietnamese);
        contentArea.getChildren().setAll(budgetView.getRoot());
    }
    private void showSettings() {
        if (settingsView == null) settingsView = new SettingsView(this, isVietnamese);
        contentArea.getChildren().setAll(settingsView.getRoot());
    }
    private void logout() {
        SessionManager.logout();
        new LoginView(primaryStage, isVietnamese).show();
    }

    public void updateGlobalLanguage(boolean isVN) {
        this.isVietnamese = isVN;
        btnDashboard.setText(isVN ? "🏠 Trang chính" : "🏠 Dashboard");
        btnExpenses.setText(isVN ? "💰 Giao dịch" : "💰 Expenses");
        btnStatistics.setText(isVN ? "📊 Thống kê" : "📊 Statistics");
        btnBudget.setText(isVN ? "🎯 Ngân sách" : "🎯 Budget");
        btnSettings.setText(isVN ? "⚙️ Cài đặt" : "⚙️ Settings");
        btnLogout.setText(isVN ? "🚪 Đăng xuất" : "🚪 Logout");
        if (dashboardView != null) dashboardView.updateLanguage(isVN);
        if (expensesView != null) expensesView.updateLanguage(isVN);
        if (statisticsView != null) statisticsView.updateLanguageText(isVN);
        if (budgetView != null) budgetView.updateLanguageText(isVN);
        if (settingsView != null) settingsView.updateLanguage(isVN);
    }

    public FinanceService getFinanceService() { return financeService; }
    public Parent getRoot() { return root; }
}