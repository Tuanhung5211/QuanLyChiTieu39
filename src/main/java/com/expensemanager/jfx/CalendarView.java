package com.expensemanager.jfx;

import com.expensemanager.entity.Transaction;
import com.expensemanager.entity.TransactionType;
import com.expensemanager.observer.EventType;
import com.expensemanager.observer.Observer;
import com.expensemanager.service.FinanceService;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class CalendarView implements Observer {
    private final FinanceService financeService;
    private boolean isVietnamese;
    private VBox root;
    private GridPane calendarGrid;
    private Label monthYearLabel;
    private YearMonth currentYearMonth;

    public CalendarView(FinanceService financeService, boolean isVietnamese) {
        this.financeService = financeService;
        this.isVietnamese = isVietnamese;
        financeService.attach(this);
        currentYearMonth = YearMonth.now();
        buildUI();
        refreshCalendar();
    }

    private void buildUI() {
        root = new VBox(15);
        root.setPadding(new javafx.geometry.Insets(20));
        root.setAlignment(Pos.TOP_CENTER);

        Label title = new Label(isVietnamese ? "📅 LỊCH CHI TIÊU" : "📅 EXPENSE CALENDAR");
        title.getStyleClass().add("label-title");

        HBox navBar = new HBox(15);
        navBar.setAlignment(Pos.CENTER);
        Button prevBtn = new Button("◀");
        Button nextBtn = new Button("▶");
        monthYearLabel = new Label();
        monthYearLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        prevBtn.setOnAction(e -> { currentYearMonth = currentYearMonth.minusMonths(1); refreshCalendar(); });
        nextBtn.setOnAction(e -> { currentYearMonth = currentYearMonth.plusMonths(1); refreshCalendar(); });
        navBar.getChildren().addAll(prevBtn, monthYearLabel, nextBtn);

        calendarGrid = new GridPane();
        calendarGrid.setHgap(8);
        calendarGrid.setVgap(8);
        calendarGrid.setAlignment(Pos.CENTER);

        root.getChildren().addAll(title, navBar, calendarGrid);
    }

    private void refreshCalendar() {
        calendarGrid.getChildren().clear();
        monthYearLabel.setText(formatMonthYear(currentYearMonth));

        // Tiêu đề các ngày: bắt đầu từ Thứ 2
        String[] weekDays = getWeekdayLabels();
        for (int i = 0; i < 7; i++) {
            Label dow = new Label(weekDays[i]);
            dow.setStyle("-fx-font-weight: bold; -fx-text-fill: #64748B; -fx-padding: 5;");
            calendarGrid.add(dow, i, 0);
        }

        List<Transaction> allTransactions = financeService.getAllTransactions();
        Map<LocalDate, Double> dailyExpense = allTransactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .collect(Collectors.groupingBy(t -> t.getDateTime().toLocalDate(),
                        Collectors.summingDouble(Transaction::getAmount)));

        LocalDate firstDay = currentYearMonth.atDay(1);
        int offset = firstDay.getDayOfWeek().getValue() - 1; // 0=Monday, 6=Sunday
        int daysInMonth = currentYearMonth.lengthOfMonth();
        LocalDate today = LocalDate.now();

        int row = 1, col = offset;
        for (int d = 1; d <= daysInMonth; d++) {
            LocalDate date = currentYearMonth.atDay(d);
            double expense = dailyExpense.getOrDefault(date, 0.0);
            boolean isFuture = date.isAfter(today);

            VBox dayBox = new VBox(4);
            dayBox.setAlignment(Pos.TOP_CENTER);
            dayBox.setPadding(new javafx.geometry.Insets(8, 4, 8, 4));
            dayBox.setPrefWidth(70);
            dayBox.setPrefHeight(70);
            dayBox.getStyleClass().add("calendar-day");

            if (date.equals(today)) {
                dayBox.setStyle("-fx-background-color: #22D3EE30; -fx-background-radius: 12; -fx-border-color: #22D3EE; -fx-border-radius: 12;");
            } else if (isFuture) {
                dayBox.setStyle("-fx-background-color: #2D2D3D; -fx-background-radius: 12; -fx-opacity: 0.5;");
            } else {
                dayBox.setStyle("-fx-background-color: #1F2937; -fx-background-radius: 12;");
            }

            Label dayLabel = new Label(String.valueOf(d));
            dayLabel.setStyle("-fx-font-weight: bold;");
            Label amountLabel = new Label(expense > 0 && !isFuture ? String.format("%,.0f", expense) : "");
            amountLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #F87171;");

            dayBox.getChildren().addAll(dayLabel, amountLabel);

            if (!isFuture) {
                dayBox.setOnMouseClicked(e -> showTransactionsForDate(date));
                dayBox.setCursor(javafx.scene.Cursor.HAND);
            }

            calendarGrid.add(dayBox, col, row);
            col++;
            if (col == 7) { col = 0; row++; }
        }
    }

    private void showTransactionsForDate(LocalDate date) {
        List<Transaction> dayTransactions = financeService.getAllTransactions().stream()
                .filter(t -> t.getDateTime().toLocalDate().equals(date))
                .collect(Collectors.toList());
        if (dayTransactions.isEmpty()) {
            new Alert(Alert.AlertType.INFORMATION,
                    isVietnamese ? "Không có giao dịch trong ngày này." : "No transactions on this day.").showAndWait();
            return;
        }
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(isVietnamese ? "Giao dịch ngày " + date : "Transactions on " + date);
        ListView<Transaction> listView = new ListView<>();
        listView.getItems().addAll(dayTransactions);
        listView.setCellFactory(lv -> new ListCell<Transaction>() {
            @Override
            protected void updateItem(Transaction t, boolean empty) {
                super.updateItem(t, empty);
                if (empty || t == null) setText(null);
                else setText(String.format("[%s] %,.0f VND - %s",
                        t.getType() == TransactionType.INCOME ? (isVietnamese ? "Thu" : "Income") : (isVietnamese ? "Chi" : "Expense"),
                        t.getAmount(), t.getCategory().getName()));
            }
        });
        listView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Transaction selected = listView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    new TransactionDetailDialog(financeService, selected, isVietnamese).showAndWait();
                    dialog.close();
                }
            }
        });
        dialog.getDialogPane().setContent(listView);
        dialog.getDialogPane().setMinWidth(400);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    private String formatMonthYear(YearMonth ym) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy",
                new Locale(isVietnamese ? "vi" : "en"));
        return ym.format(formatter);
    }

    private String[] getWeekdayLabels() {
        if (isVietnamese) return new String[]{"T2", "T3", "T4", "T5", "T6", "T7", "CN"};
        else return new String[]{"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
    }

    @Override
    public void update(EventType eventType, Object data) {
        if (eventType == EventType.TRANSACTION_ADDED || eventType == EventType.TRANSACTION_UPDATED ||
                eventType == EventType.TRANSACTION_DELETED || eventType == EventType.DATA_LOADED) {
            Platform.runLater(this::refreshCalendar);
        }
    }

    public void updateLanguage(boolean isVN) {
        this.isVietnamese = isVN;
        ((Label) root.getChildren().get(0)).setText(isVN ? "📅 LỊCH CHI TIÊU" : "📅 EXPENSE CALENDAR");
        refreshCalendar();
    }

    public Node getRoot() { return root; }
}