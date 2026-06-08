package com.expensemanager.jfx;

import com.expensemanager.entity.Budget;
import com.expensemanager.entity.Category;
import com.expensemanager.entity.Transaction;
import com.expensemanager.entity.TransactionType;
import com.expensemanager.observer.EventType;
import com.expensemanager.observer.Observer;
import com.expensemanager.service.BudgetManager;
import com.expensemanager.service.FinanceService;
import com.expensemanager.service.SessionManager;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReportView implements Observer {
    private final FinanceService financeService;
    private final BudgetManager budgetManager;
    private boolean isVietnamese;
    private VBox root;
    private DatePicker startDatePicker, endDatePicker;
    private Label lblTotalIncome, lblTotalExpense, lblBalance;
    private Label lblBudgetLimit, lblBudgetSpent, lblBudgetRemaining, lblBudgetPercent;
    private ProgressBar budgetProgress;
    private PieChart pieChart;
    private ListView<String> categoryListView;
    private Button btnRefresh;

    // ✅ Constructor CHỈ nhận 3 tham số (bỏ StatisticsService)
    public ReportView(FinanceService financeService, BudgetManager budgetManager, boolean isVietnamese) {
        this.financeService = financeService;
        this.budgetManager = budgetManager;
        this.isVietnamese = isVietnamese;
        financeService.attach(this);
        buildUI();
        setDefaultDateRange();
        refreshReport();
    }

    private void buildUI() {
        root = new VBox(20);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.TOP_CENTER);

        Label title = new Label(isVietnamese ? "📊 BÁO CÁO & PHÂN TÍCH" : "📊 REPORTS & ANALYTICS");
        title.getStyleClass().add("label-title");

        HBox dateRangeBox = new HBox(15);
        dateRangeBox.setAlignment(Pos.CENTER_LEFT);
        Label lblStart = new Label(isVietnamese ? "Từ ngày:" : "Start:");
        startDatePicker = new DatePicker();
        startDatePicker.setPromptText("dd/MM/yyyy");
        Label lblEnd = new Label(isVietnamese ? "Đến ngày:" : "End:");
        endDatePicker = new DatePicker();
        endDatePicker.setPromptText("dd/MM/yyyy");
        btnRefresh = new Button(isVietnamese ? "Xem báo cáo" : "View report");
        btnRefresh.setOnAction(e -> refreshReport());
        dateRangeBox.getChildren().addAll(lblStart, startDatePicker, lblEnd, endDatePicker, btnRefresh);

        GridPane statsCard = createStatsCard();
        GridPane budgetCard = createBudgetCard();
        HBox chartAndList = new HBox(20);
        pieChart = new PieChart();
        pieChart.setLabelsVisible(true);
        pieChart.setPrefSize(400, 400);
        categoryListView = new ListView<>();
        categoryListView.setPrefWidth(300);
        categoryListView.setPrefHeight(350);
        chartAndList.getChildren().addAll(pieChart, categoryListView);

        root.getChildren().addAll(title, dateRangeBox, statsCard, budgetCard, chartAndList);
    }

    private GridPane createStatsCard() {
        GridPane grid = new GridPane();
        grid.setHgap(30);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));
        grid.getStyleClass().add("card");

        lblTotalIncome = new Label("0 VND");
        lblTotalExpense = new Label("0 VND");
        lblBalance = new Label("0 VND");
        lblTotalIncome.setStyle("-fx-font-size: 20px; -fx-text-fill: #10B981; -fx-font-weight: bold;");
        lblTotalExpense.setStyle("-fx-font-size: 20px; -fx-text-fill: #F87171; -fx-font-weight: bold;");
        lblBalance.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        grid.add(new Label(isVietnamese ? "TỔNG THU" : "TOTAL INCOME"), 0, 0);
        grid.add(lblTotalIncome, 0, 1);
        grid.add(new Label(isVietnamese ? "TỔNG CHI" : "TOTAL EXPENSE"), 1, 0);
        grid.add(lblTotalExpense, 1, 1);
        grid.add(new Label(isVietnamese ? "SỐ DƯ" : "BALANCE"), 2, 0);
        grid.add(lblBalance, 2, 1);
        return grid;
    }

    private GridPane createBudgetCard() {
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.getStyleClass().add("card");

        lblBudgetLimit = new Label();
        lblBudgetSpent = new Label();
        lblBudgetRemaining = new Label();
        lblBudgetPercent = new Label();
        budgetProgress = new ProgressBar(0);
        budgetProgress.setPrefWidth(300);

        grid.add(new Label(isVietnamese ? "NGÂN SÁCH THÁNG" : "MONTHLY BUDGET"), 0, 0, 2, 1);
        grid.add(new Label(isVietnamese ? "Hạn mức:" : "Limit:"), 0, 1);
        grid.add(lblBudgetLimit, 1, 1);
        grid.add(new Label(isVietnamese ? "Đã chi:" : "Spent:"), 0, 2);
        grid.add(lblBudgetSpent, 1, 2);
        grid.add(new Label(isVietnamese ? "Còn lại:" : "Remaining:"), 0, 3);
        grid.add(lblBudgetRemaining, 1, 3);
        grid.add(budgetProgress, 0, 4, 2, 1);
        grid.add(lblBudgetPercent, 0, 5, 2, 1);
        return grid;
    }

    private void setDefaultDateRange() {
        LocalDate now = LocalDate.now();
        startDatePicker.setValue(now.withDayOfMonth(1));
        endDatePicker.setValue(now);
    }

    private void refreshReport() {
        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();
        if (start == null || end == null) return;

        List<Transaction> all = financeService.getAllTransactions();
        if (all == null) all = List.of();

        List<Transaction> filtered = all.stream()
                .filter(t -> !t.getDateTime().toLocalDate().isBefore(start))
                .filter(t -> !t.getDateTime().toLocalDate().isAfter(end))
                .collect(Collectors.toList());

        double totalIncome = filtered.stream().filter(t -> t.getType() == TransactionType.INCOME).mapToDouble(Transaction::getAmount).sum();
        double totalExpense = filtered.stream().filter(t -> t.getType() == TransactionType.EXPENSE).mapToDouble(Transaction::getAmount).sum();
        double balance = totalIncome - totalExpense;

        lblTotalIncome.setText(String.format("%,.0f VND", totalIncome));
        lblTotalExpense.setText(String.format("%,.0f VND", totalExpense));
        lblBalance.setText(String.format("%,.0f VND", balance));

        // Ngân sách tháng của ngày bắt đầu
        LocalDate monthStart = start.withDayOfMonth(1);
        LocalDate monthEnd = start.withDayOfMonth(start.lengthOfMonth());
        String userId = SessionManager.getCurrentUserId();
        Budget budget = com.expensemanager.database.DatabaseUtil.getBudget(monthStart.getMonthValue(), monthStart.getYear(), userId);
        if (budget != null && budget.getLimit() > 0) {
            double spentThisMonth = all.stream()
                    .filter(t -> t.getType() == TransactionType.EXPENSE)
                    .filter(t -> !t.getDateTime().toLocalDate().isBefore(monthStart))
                    .filter(t -> !t.getDateTime().toLocalDate().isAfter(monthEnd))
                    .mapToDouble(Transaction::getAmount).sum();
            budget.setSpent(spentThisMonth);
            lblBudgetLimit.setText(String.format("%,.0f VND", budget.getLimit()));
            lblBudgetSpent.setText(String.format("%,.0f VND", budget.getSpent()));
            double remain = budget.getRemaining();
            lblBudgetRemaining.setText(String.format("%,.0f VND", Math.max(remain, 0)));
            double percent = budget.getSpent() / budget.getLimit();
            budgetProgress.setProgress(Math.min(percent, 1.0));
            lblBudgetPercent.setText(String.format("%.1f%% %s", percent * 100, isVietnamese ? "đã sử dụng" : "used"));
        } else {
            lblBudgetLimit.setText(isVietnamese ? "Chưa thiết lập" : "Not set");
            lblBudgetSpent.setText("0 VND");
            lblBudgetRemaining.setText("0 VND");
            budgetProgress.setProgress(0);
            lblBudgetPercent.setText("");
        }

        // Biểu đồ tròn chi tiêu
        Map<Category, Double> catExpense = filtered.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .collect(Collectors.groupingBy(Transaction::getCategory, Collectors.summingDouble(Transaction::getAmount)));

        pieChart.getData().clear();
        categoryListView.getItems().clear();
        if (totalExpense > 0) {
            for (Map.Entry<Category, Double> e : catExpense.entrySet()) {
                double percent = (e.getValue() / totalExpense) * 100;
                pieChart.getData().add(new PieChart.Data(e.getKey().getName() + " " + String.format("%,.0f", e.getValue()), e.getValue()));
                categoryListView.getItems().add(String.format("%s: %,.0f VND (%.2f%%)", e.getKey().getName(), e.getValue(), percent));
            }
        } else {
            categoryListView.getItems().add(isVietnamese ? "Không có chi tiêu" : "No expenses");
        }
    }

    @Override
    public void update(EventType eventType, Object data) {
        if (eventType == EventType.TRANSACTION_ADDED || eventType == EventType.TRANSACTION_UPDATED ||
                eventType == EventType.TRANSACTION_DELETED || eventType == EventType.DATA_LOADED) {
            Platform.runLater(this::refreshReport);
        }
    }

    public void updateLanguage(boolean isVN) {
        this.isVietnamese = isVN;
        root.getChildren().clear();
        buildUI();
        setDefaultDateRange();
        refreshReport();
    }

    public Node getRoot() { return root; }
}