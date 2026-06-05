package com.expensemanager.jfx;

import com.expensemanager.database.DatabaseUtil;
import com.expensemanager.entity.Budget;
import com.expensemanager.entity.Transaction;
import com.expensemanager.entity.TransactionType;
import com.expensemanager.exception.InvalidAmountException;
import com.expensemanager.observer.EventType;
import com.expensemanager.observer.Observer;
import com.expensemanager.service.BudgetManager;
import com.expensemanager.service.FinanceService;
import com.expensemanager.service.SessionManager;
import com.expensemanager.util.InputValidator;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.time.LocalDate;
import java.util.Locale;

public class BudgetView implements Observer {
    private final BudgetManager budgetManager;
    private final FinanceService financeService;
    private boolean isVietnamese;
    private VBox root;
    private Label lblMonthYear, lblBudgetLimit, lblSpent, lblRemaining, lblStatus;
    private ProgressBar progressBar;
    private Button btnSetBudget;

    public BudgetView(BudgetManager budgetManager, FinanceService financeService, boolean isVietnamese) {
        this.budgetManager = budgetManager;
        this.financeService = financeService;
        this.isVietnamese = isVietnamese;
        if (financeService != null) financeService.attach(this);
        buildUI();
        refreshData();
    }

    private void buildUI() {
        root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.TOP_CENTER);
        Label lblTitle = new Label(isVietnamese ? "NGÂN SÁCH CHI TIÊU THÁNG" : "MONTHLY EXPENSE BUDGET");
        VBox card = new VBox(15);
        card.setPadding(new Insets(25));
        card.setMaxWidth(600);
        card.setAlignment(Pos.CENTER);
        lblMonthYear = new Label();
        lblBudgetLimit = new Label();
        lblSpent = new Label();
        lblRemaining = new Label();
        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(400);
        lblStatus = new Label();
        btnSetBudget = new Button(isVietnamese ? "Thay đổi hạn mức ngân sách" : "Adjust Budget Limit");
        btnSetBudget.setOnAction(e -> openSetBudgetDialog());
        card.getChildren().addAll(lblMonthYear, lblBudgetLimit, lblSpent, lblRemaining, progressBar, lblStatus, btnSetBudget);
        root.getChildren().addAll(lblTitle, card);
    }

    private void openSetBudgetDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(isVietnamese ? "Thiết lập ngân sách" : "Set Budget");
        dialog.setHeaderText(isVietnamese ? "Nhập hạn mức chi tiêu tháng này (VND)" : "Enter monthly budget limit (VND)");
        ButtonType ok = new ButtonType(isVietnamese ? "Xác nhận" : "OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().setAll(ok, ButtonType.CANCEL);
        dialog.setResultConverter(b -> {
            if (b == ok) {
                try {
                    double limit = InputValidator.validateAmount(dialog.getEditor().getText(), isVietnamese);
                    budgetManager.setBudget(LocalDate.now().getMonthValue(), LocalDate.now().getYear(), limit);
                    refreshData();
                } catch (IllegalArgumentException | InvalidAmountException ex) {
                    new Alert(Alert.AlertType.WARNING, ex.getMessage(), ButtonType.OK).showAndWait();
                }
            }
            return null;
        });
        dialog.showAndWait();
    }

    public void refreshData() {
        int month = LocalDate.now().getMonthValue();
        int year = LocalDate.now().getYear();
        lblMonthYear.setText(isVietnamese ? "Tháng " + month + " Năm " + year : java.time.Month.of(month).getDisplayName(java.time.format.TextStyle.FULL, Locale.ENGLISH) + " " + year);
        String userId = SessionManager.getCurrentUserId();
        double spent = financeService.getAllTransactions().stream()
                .filter(t -> t != null && t.getType() == TransactionType.EXPENSE)
                .filter(t -> t.getDateTime().getMonthValue() == month && t.getDateTime().getYear() == year)
                .mapToDouble(Transaction::getAmount).sum();
        Budget budget = DatabaseUtil.getBudget(month, year, userId);
        double limit = (budget != null) ? budget.getLimit() : 0;
        if (limit > 0) {
            lblBudgetLimit.setText(isVietnamese ? String.format("Hạn mức tháng này: %,.0f đ", limit) : String.format("Monthly Limit: %,.0f VND", limit));
            lblSpent.setText(isVietnamese ? String.format("Số tiền đã chi: %,.0f đ", spent) : String.format("Total Spent: %,.0f VND", spent));
            double remaining = limit - spent;
            lblRemaining.setText(isVietnamese ? String.format("Còn lại: %,.0f đ", remaining) : String.format("Remaining: %,.0f VND", remaining));
            double percent = spent / limit;
            progressBar.setProgress(Math.min(percent, 1.0));
            String status = budgetManager.checkBudget();
            lblStatus.setText(status);
        } else {
            lblBudgetLimit.setText(isVietnamese ? "Hạn mức tháng này: Chưa thiết lập" : "Monthly Limit: Not Set");
            lblSpent.setText(isVietnamese ? String.format("Số tiền đã chi: %,.0f đ", spent) : String.format("Total Spent: %,.0f VND", spent));
            lblRemaining.setText(isVietnamese ? "Còn lại: 0 đ" : "Remaining: 0 VND");
            progressBar.setProgress(0);
            lblStatus.setText(isVietnamese ? "Chưa thiết lập ngân sách." : "No budget set.");
        }
    }

    @Override public void update(EventType eventType, Object data) { if (eventType == EventType.TRANSACTION_ADDED || eventType == EventType.TRANSACTION_UPDATED || eventType == EventType.TRANSACTION_DELETED || eventType == EventType.BUDGET_CHANGED || eventType == EventType.DATA_LOADED) javafx.application.Platform.runLater(this::refreshData); }
    public void updateLanguageText(boolean isVN) { this.isVietnamese = isVN; refreshData(); }
    public Node getRoot() { return root; }
}