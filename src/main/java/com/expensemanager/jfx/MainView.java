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
    private final Stage stage;
    private boolean isVN;
    private BorderPane root;
    private StackPane center;
    private FinanceService finance;
    private BudgetManager budget;
    private StatisticsService stats;

    private DashboardView dashboard;
    private ExpensesView expenses;
    private StatisticsView statistics;
    private BudgetView budgetView;
    private SettingsView settings;
    private CalendarView calendar;
    private ReportView report;

    public MainView(Stage stage, boolean isVN) {
        this.stage = stage;
        this.isVN = isVN;
        finance = new FinanceService();
        budget = new BudgetManager(finance);
        stats = new StatisticsService(finance);
        finance.syncFromDatabase();
        buildUI();
        showDashboard();
    }

    private void buildUI() {
        root = new BorderPane();
        root.setLeft(sidebar());
        center = new StackPane();
        center.setPadding(new Insets(24));
        center.setStyle("-fx-background-color: #0A0B0F;");
        root.setCenter(center);
    }

    private VBox sidebar() {
        VBox side = new VBox(8);
        side.getStyleClass().add("sidebar");
        side.setPadding(new Insets(24, 16, 24, 16));
        side.setMinWidth(260);
        side.setAlignment(Pos.TOP_CENTER);

        User user = DatabaseUtil.getUserByUsername(SessionManager.getCurrentUsername());
        HBox userBox = new HBox(12);
        userBox.setAlignment(Pos.CENTER_LEFT);
        Label avatar = new Label(user != null && user.getAvatar() != null ? user.getAvatar() : "👤");
        avatar.setStyle("-fx-font-size: 42px; -fx-background-color: #22D3EE20; -fx-padding: 10; -fx-background-radius: 50%;");
        VBox info = new VBox(2);
        Label name = new Label(user != null ? user.getNickname() : "User");
        name.setStyle("-fx-font-weight: bold; -fx-text-fill: white; -fx-font-size: 16px;");
        Label email = new Label(user != null ? user.getEmail() : "");
        email.setStyle("-fx-text-fill: #64748B;");
        info.getChildren().addAll(name, email);
        userBox.getChildren().addAll(avatar, info);

        Button dashBtn = sideBtn("🏠 " + (isVN ? "Trang chính" : "Dashboard"), this::showDashboard);
        Button expBtn = sideBtn("💰 " + (isVN ? "Giao dịch" : "Expenses"), this::showExpenses);
        Button statBtn = sideBtn("📊 " + (isVN ? "Thống kê" : "Statistics"), this::showStatistics);
        Button budBtn = sideBtn("🎯 " + (isVN ? "Ngân sách" : "Budget"), this::showBudget);
        Button setBtn = sideBtn("⚙️ " + (isVN ? "Cài đặt" : "Settings"), this::showSettings);
        Button calBtn = sideBtn("📅 " + (isVN ? "Lịch chi tiêu" : "Calendar"), this::showCalendar);
        Button repBtn = sideBtn("📈 " + (isVN ? "Báo cáo" : "Report"), this::showReport);
        Button logoutBtn = sideBtn("🚪 " + (isVN ? "Đăng xuất" : "Logout"), this::logout);

        side.getChildren().addAll(userBox, new Separator(),
                dashBtn, expBtn, statBtn, budBtn, setBtn, calBtn, repBtn,
                new Region(), logoutBtn);
        VBox.setVgrow(new Region(), Priority.ALWAYS);
        return side;
    }

    private Button sideBtn(String text, Runnable action) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.getStyleClass().add("sidebar-button");
        btn.setOnAction(e -> action.run());
        return btn;
    }

    private void showDashboard() {
        if (dashboard == null) dashboard = new DashboardView(finance, budget, stats, isVN);
        center.getChildren().setAll(dashboard.getRoot());
    }
    private void showExpenses() {
        if (expenses == null) expenses = new ExpensesView(finance, isVN);
        center.getChildren().setAll(expenses.getRoot());
    }
    private void showStatistics() {
        if (statistics == null) statistics = new StatisticsView(stats, budget, isVN);
        center.getChildren().setAll(statistics.getRoot());
    }
    private void showBudget() {
        if (budgetView == null) budgetView = new BudgetView(budget, finance, isVN);
        center.getChildren().setAll(budgetView.getRoot());
    }
    private void showSettings() {
        if (settings == null) settings = new SettingsView(this, isVN);
        center.getChildren().setAll(settings.getRoot());
    }
    private void showCalendar() {
        if (calendar == null) calendar = new CalendarView(finance, isVN);
        center.getChildren().setAll(calendar.getRoot());
    }
    private void showReport() {
        // ✅ Gọi constructor 3 tham số (FinanceService, BudgetManager, boolean)
        if (report == null) report = new ReportView(finance, budget, isVN);
        center.getChildren().setAll(report.getRoot());
    }
    private void logout() {
        SessionManager.logout();
        new LoginView(stage, isVN).show();
    }

    public void updateGlobalLanguage(boolean vn) {
        isVN = vn;
        root.setLeft(sidebar());
        if (dashboard != null) dashboard.updateLanguage(vn);
        if (expenses != null) expenses.updateLanguage(vn);
        if (statistics != null) statistics.updateLanguageText(vn);
        if (budgetView != null) budgetView.updateLanguageText(vn);
        if (settings != null) settings.updateLanguage(vn);
        if (calendar != null) calendar.updateLanguage(vn);
        if (report != null) report.updateLanguage(vn);
    }

    public FinanceService getFinanceService() { return finance; }
    public Parent getRoot() { return root; }
}