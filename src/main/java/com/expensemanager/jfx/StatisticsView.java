package com.expensemanager.jfx;

import com.expensemanager.entity.Transaction;
import com.expensemanager.entity.TransactionType;
import com.expensemanager.observer.EventType;
import com.expensemanager.observer.Observer;
import com.expensemanager.service.BudgetManager;
import com.expensemanager.service.FinanceService;
import com.expensemanager.service.StatisticsService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class StatisticsView implements Observer {
    private final FinanceService financeService;
    private boolean isVietnamese;
    private VBox root;
    private StackPane chartContainer;
    private ComboBox<Integer> monthCombo;
    private ComboBox<Integer> yearCombo;

    public StatisticsView(StatisticsService statsService, BudgetManager budgetManager, boolean isVietnamese) {
        this.financeService = statsService.getFinanceService();
        this.isVietnamese = isVietnamese;
        if (financeService != null) financeService.attach(this);
        buildUI();
        refreshChart();
    }

    private void buildUI() {
        root = new VBox(20);
        root.setPadding(new Insets(20));
        Label title = new Label(isVietnamese ? "📊 PHÂN TÍCH THỐNG KÊ CHI TIÊU" : "📊 EXPENSE STATISTICS");
        VBox card = new VBox(15);
        card.setPadding(new Insets(20));
        HBox filterPanel = new HBox(15);
        filterPanel.setAlignment(Pos.CENTER_LEFT);
        Label filterLabel = new Label(isVietnamese ? "Chọn kỳ:" : "Period:");
        monthCombo = new ComboBox<>();
        for (int i = 1; i <= 12; i++) monthCombo.getItems().add(i);
        monthCombo.setValue(LocalDate.now().getMonthValue());
        yearCombo = new ComboBox<>();
        int currentYear = LocalDate.now().getYear();
        for (int y = currentYear - 5; y <= currentYear + 5; y++) yearCombo.getItems().add(y);
        yearCombo.setValue(currentYear);
        Button refreshBtn = new Button(isVietnamese ? "Xem thống kê" : "View stats");
        refreshBtn.setOnAction(e -> refreshChart());
        monthCombo.valueProperty().addListener((obs, old, val) -> refreshChart());
        yearCombo.valueProperty().addListener((obs, old, val) -> refreshChart());
        filterPanel.getChildren().addAll(filterLabel, monthCombo, yearCombo, refreshBtn);
        chartContainer = new StackPane();
        chartContainer.setMinHeight(400);
        chartContainer.setPadding(new Insets(20));
        card.getChildren().addAll(filterPanel, chartContainer);
        root.getChildren().addAll(title, card);
    }

    private void refreshChart() {
        Integer month = monthCombo.getValue();
        Integer year = yearCombo.getValue();
        if (month == null || year == null) {
            chartContainer.getChildren().setAll(new Label(isVietnamese ? "Vui lòng chọn tháng và năm" : "Please select month and year"));
            return;
        }
        List<Transaction> all = financeService.getAllTransactions();
        if (all == null || all.isEmpty()) {
            chartContainer.getChildren().setAll(new Label(isVietnamese ? "Chưa có giao dịch nào." : "No transactions yet."));
            return;
        }
        List<Transaction> expenses = all.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .filter(t -> t.getDateTime().getYear() == year && t.getDateTime().getMonthValue() == month)
                .collect(Collectors.toList());
        if (expenses.isEmpty()) {
            String monthName = YearMonth.of(year, month).getMonth().getDisplayName(TextStyle.FULL, Locale.forLanguageTag(isVietnamese ? "vi" : "en"));
            chartContainer.getChildren().setAll(new Label(isVietnamese ? "Không có chi tiêu trong tháng " + month + "/" + year : "No expenses in " + monthName + " " + year));
            return;
        }
        Map<String, Double> dataMap = expenses.stream().collect(Collectors.groupingBy(t -> t.getCategory().getName(), Collectors.summingDouble(Transaction::getAmount)));
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        for (Map.Entry<String, Double> entry : dataMap.entrySet()) pieData.add(new PieChart.Data(entry.getKey() + " (" + String.format("%,.0f", entry.getValue()) + " ₫)", entry.getValue()));
        PieChart pieChart = new PieChart(pieData);
        pieChart.setTitle(isVietnamese ? "Chi tiêu tháng " + month + "/" + year : "Expenses for " + YearMonth.of(year, month).getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + year);
        pieChart.setLabelsVisible(true);
        chartContainer.getChildren().setAll(pieChart);
    }

    @Override public void update(EventType eventType, Object data) { if (eventType == EventType.TRANSACTION_ADDED || eventType == EventType.TRANSACTION_UPDATED || eventType == EventType.TRANSACTION_DELETED || eventType == EventType.DATA_LOADED) Platform.runLater(this::refreshChart); }
    public void updateLanguageText(boolean isVN) { this.isVietnamese = isVN; root.getChildren().clear(); buildUI(); refreshChart(); }
    public Node getRoot() { return root; }
}