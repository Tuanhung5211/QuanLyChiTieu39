package com.expensemanager.jfx;

import com.expensemanager.entity.Transaction;
import com.expensemanager.entity.TransactionType;
import com.expensemanager.observer.EventType;
import com.expensemanager.observer.Observer;
import com.expensemanager.service.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class DashboardView implements Observer {
    private final FinanceService financeService;
    private final BudgetManager budgetManager;
    private final StatisticsService statsService;
    private boolean isVietnamese;
    private BorderPane root;
    private VBox leftColumn, rightColumn;
    private Label lblBalance, lblLast6Months, lblMonthlyExpense, lblMonthlySavings;
    private BarChart<String, Number> barChart;
    private VBox remindersList;
    private GridPane calendarGrid;

    public DashboardView(FinanceService financeService, BudgetManager budgetManager,
                         StatisticsService statisticsService, boolean isVietnamese) {
        this.financeService = financeService;
        this.budgetManager = budgetManager;
        this.statsService = statisticsService;
        this.isVietnamese = isVietnamese;
        financeService.attach(this);
        buildUI();
        refreshData();
    }

    private void buildUI() {
        root = new BorderPane();
        leftColumn = new VBox(20);
        leftColumn.setPadding(new Insets(20));
        leftColumn.setPrefWidth(650);
        VBox overviewCard = new VBox(12);
        overviewCard.setPadding(new Insets(18));
        overviewCard.setStyle("-fx-background-color: white; -fx-background-radius: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 2);");
        Label overviewTitle = new Label(isVietnamese ? "TỔNG QUAN CHI TIÊU" : "EXPENSES OVERVIEW");
        overviewTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        GridPane overviewGrid = new GridPane();
        overviewGrid.setHgap(20);
        overviewGrid.setVgap(15);
        lblBalance = new Label("0 ₫");
        lblBalance.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
        lblLast6Months = new Label("0 ₫");
        lblLast6Months.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
        lblMonthlyExpense = new Label("0 ₫");
        lblMonthlyExpense.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
        lblMonthlySavings = new Label("0 ₫");
        lblMonthlySavings.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
        overviewGrid.add(createStatBox(isVietnamese ? "Số dư hiện tại" : "Your balance", lblBalance), 0, 0);
        overviewGrid.add(createStatBox(isVietnamese ? "Chi 6 tháng qua" : "Last 6 months", lblLast6Months), 1, 0);
        overviewGrid.add(createStatBox(isVietnamese ? "Chi tiêu TB tháng" : "Monthly expenditure", lblMonthlyExpense), 0, 1);
        overviewGrid.add(createStatBox(isVietnamese ? "Tiết kiệm TB tháng" : "Monthly savings", lblMonthlySavings), 1, 1);
        overviewCard.getChildren().addAll(overviewTitle, overviewGrid);

        VBox chartCard = new VBox(12);
        chartCard.setPadding(new Insets(18));
        chartCard.setStyle("-fx-background-color: white; -fx-background-radius: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 2);");
        Label chartTitle = new Label(isVietnamese ? "CHI TIÊU 6 THÁNG GẦN NHẤT" : "LAST 6 MONTHS SPENDINGS");
        chartTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        barChart = new BarChart<>(xAxis, yAxis);
        barChart.setLegendVisible(false);
        barChart.setPrefHeight(300);
        chartCard.getChildren().addAll(chartTitle, barChart);

        leftColumn.getChildren().addAll(overviewCard, chartCard);

        rightColumn = new VBox(20);
        rightColumn.setPadding(new Insets(20, 20, 20, 0));
        rightColumn.setPrefWidth(400);
        VBox reminderCard = new VBox(12);
        reminderCard.setPadding(new Insets(18));
        reminderCard.setStyle("-fx-background-color: white; -fx-background-radius: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 2);");
        Label reminderTitle = new Label(isVietnamese ? "NHẮC NHỞ SẮP TỚI" : "UPCOMING REMINDERS");
        reminderTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        remindersList = new VBox(10);
        ScrollPane reminderScroll = new ScrollPane(remindersList);
        reminderScroll.setFitToWidth(true);
        reminderScroll.setPrefHeight(250);
        reminderScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        reminderCard.getChildren().addAll(reminderTitle, reminderScroll);

        VBox calendarCard = new VBox(12);
        calendarCard.setPadding(new Insets(18));
        calendarCard.setStyle("-fx-background-color: white; -fx-background-radius: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 2);");
        Label calendarTitle = new Label(isVietnamese ? "LỊCH THANH TOÁN" : "SCHEDULE PAYMENT");
        calendarTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        calendarGrid = new GridPane();
        calendarGrid.setHgap(8);
        calendarGrid.setVgap(8);
        calendarGrid.setAlignment(Pos.CENTER);
        calendarCard.getChildren().addAll(calendarTitle, calendarGrid);

        rightColumn.getChildren().addAll(reminderCard, calendarCard);

        SplitPane splitPane = new SplitPane();
        splitPane.getItems().addAll(leftColumn, rightColumn);
        splitPane.setDividerPositions(0.62);
        splitPane.setStyle("-fx-background-color: transparent;");
        root.setCenter(splitPane);
    }

    private VBox createStatBox(String labelText, Label valueLabel) {
        VBox box = new VBox(5);
        Label label = new Label(labelText);
        label.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748B;");
        box.getChildren().addAll(label, valueLabel);
        return box;
    }

    private void refreshData() {
        List<Transaction> allTrans = financeService.getAllTransactions();
        double balance = allTrans.stream().mapToDouble(t -> t.getType() == TransactionType.INCOME ? t.getAmount() : -t.getAmount()).sum();
        lblBalance.setText(String.format("%,.0f ₫", balance));
        LocalDate now = LocalDate.now();
        double last6MonthsExpense = allTrans.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .filter(t -> t.getDateTime().toLocalDate().isAfter(now.minusMonths(6)))
                .mapToDouble(Transaction::getAmount).sum();
        lblLast6Months.setText(String.format("%,.0f ₫", last6MonthsExpense));
        double avgMonthlyExpense = last6MonthsExpense / 6;
        lblMonthlyExpense.setText(String.format("%,.0f ₫", avgMonthlyExpense));
        double last6MonthsIncome = allTrans.stream()
                .filter(t -> t.getType() == TransactionType.INCOME)
                .filter(t -> t.getDateTime().toLocalDate().isAfter(now.minusMonths(6)))
                .mapToDouble(Transaction::getAmount).sum();
        double avgMonthlyIncome = last6MonthsIncome / 6;
        double savings = avgMonthlyIncome - avgMonthlyExpense;
        lblMonthlySavings.setText(String.format("%,.0f ₫", savings > 0 ? savings : 0));

        barChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (int i = 5; i >= 0; i--) {
            LocalDate monthStart = now.minusMonths(i).withDayOfMonth(1);
            double expense = allTrans.stream()
                    .filter(t -> t.getType() == TransactionType.EXPENSE)
                    .filter(t -> t.getDateTime().toLocalDate().getYear() == monthStart.getYear() &&
                            t.getDateTime().toLocalDate().getMonth() == monthStart.getMonth())
                    .mapToDouble(Transaction::getAmount).sum();
            String label = monthStart.format(DateTimeFormatter.ofPattern("MMM yyyy"));
            series.getData().add(new XYChart.Data<>(label, expense));
        }
        barChart.getData().add(series);

        remindersList.getChildren().clear();
        List<Transaction> upcoming = allTrans.stream()
                .filter(t -> t.getDateTime().toLocalDate().isAfter(now) && t.getDateTime().toLocalDate().isBefore(now.plusDays(30)))
                .sorted((a,b) -> a.getDateTime().compareTo(b.getDateTime()))
                .limit(5).collect(Collectors.toList());
        if (upcoming.isEmpty()) {
            remindersList.getChildren().add(new Label(isVietnamese ? "Không có nhắc nhở nào" : "No upcoming reminders"));
        } else {
            for (Transaction t : upcoming) {
                HBox item = new HBox(10);
                item.setAlignment(Pos.CENTER_LEFT);
                Label name = new Label(t.getCategory().getName());
                name.setStyle("-fx-font-weight: bold;");
                Label date = new Label(t.getDateTime().format(DateTimeFormatter.ofPattern("dd MMM")));
                date.setStyle("-fx-text-fill: #3B82F6;");
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                Label amount = new Label(String.format("%,.0f ₫", t.getAmount()));
                amount.setStyle("-fx-font-weight: bold;");
                item.getChildren().addAll(name, date, spacer, amount);
                remindersList.getChildren().add(item);
            }
        }

        calendarGrid.getChildren().clear();
        int year = now.getYear();
        int month = now.getMonthValue();
        LocalDate firstDay = LocalDate.of(year, month, 1);
        int daysInMonth = firstDay.lengthOfMonth();
        int startOffset = firstDay.getDayOfWeek().getValue() % 7;
        String[] weekDays = isVietnamese ? new String[]{"T2","T3","T4","T5","T6","T7","CN"} : new String[]{"Mon","Tue","Wed","Thu","Fri","Sat","Sun"};
        for (int i = 0; i < 7; i++) {
            Label dow = new Label(weekDays[i]);
            dow.setStyle("-fx-font-weight: bold; -fx-text-fill: #475569;");
            calendarGrid.add(dow, i, 0);
        }
        int row = 1, col = startOffset;
        for (int d = 1; d <= daysInMonth; d++) {
            LocalDate current = LocalDate.of(year, month, d);
            Button dayBtn = new Button(String.valueOf(d));
            dayBtn.setStyle("-fx-background-color: transparent; -fx-padding: 6 0; -fx-pref-width: 40;");
            boolean hasExpense = financeService.getAllTransactions().stream()
                    .anyMatch(t -> t.getType() == TransactionType.EXPENSE && t.getDateTime().toLocalDate().equals(current));
            if (hasExpense) dayBtn.setStyle("-fx-background-color: #EFF6FF; -fx-text-fill: #2563EB; -fx-font-weight: bold; -fx-background-radius: 20;");
            calendarGrid.add(dayBtn, col, row);
            col++;
            if (col == 7) { col = 0; row++; }
        }
    }

    @Override
    public void update(EventType eventType, Object data) {
        if (eventType == EventType.TRANSACTION_ADDED || eventType == EventType.TRANSACTION_UPDATED ||
                eventType == EventType.TRANSACTION_DELETED || eventType == EventType.DATA_LOADED) {
            Platform.runLater(this::refreshData);
        }
    }

    public void updateLanguage(boolean isVN) { this.isVietnamese = isVN; refreshData(); }
    public Node getRoot() { return root; }
}