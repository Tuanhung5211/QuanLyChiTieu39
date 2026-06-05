package com.expensemanager.jfx;

import com.expensemanager.entity.*;
import com.expensemanager.service.FinanceService;
import com.expensemanager.util.InputValidator;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;
import java.util.UUID;

public class AddTransactionDialog {
    private final FinanceService financeService;
    private final boolean isVietnamese;
    private Transaction editTransaction;

    public AddTransactionDialog(FinanceService financeService, boolean isVietnamese) { this(financeService, isVietnamese, null); }
    public AddTransactionDialog(FinanceService financeService, boolean isVietnamese, Transaction editTransaction) {
        this.financeService = financeService;
        this.isVietnamese = isVietnamese;
        this.editTransaction = editTransaction;
    }

    public void showAndWait() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(editTransaction == null ? (isVietnamese ? "Thêm giao dịch" : "Add Transaction") : (isVietnamese ? "Sửa giao dịch" : "Edit Transaction"));
        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(12); grid.setPadding(new Insets(20));
        ComboBox<TransactionType> cmbType = new ComboBox<>();
        cmbType.getItems().addAll(TransactionType.INCOME, TransactionType.EXPENSE);
        cmbType.setConverter(new StringConverter<>() {
            @Override public String toString(TransactionType t) { return t == null ? "" : (t == TransactionType.INCOME ? (isVietnamese ? "Thu" : "Income") : (isVietnamese ? "Chi" : "Expense")); }
            @Override public TransactionType fromString(String s) { return null; }
        });
        TextField txtAmount = new TextField();
        ComboBox<Category> cmbCategory = new ComboBox<>();
        cmbCategory.getItems().addAll(financeService.getAllCategories());
        cmbCategory.setConverter(new StringConverter<>() { @Override public String toString(Category c) { return c == null ? "" : c.getName(); } @Override public Category fromString(String s) { return null; } });
        TextField txtNote = new TextField();
        if (editTransaction != null) { cmbType.setValue(editTransaction.getType()); txtAmount.setText(String.valueOf(editTransaction.getAmount())); cmbCategory.setValue(editTransaction.getCategory()); txtNote.setText(editTransaction.getNote()); }
        grid.add(new Label(isVietnamese ? "Loại:" : "Type:"), 0,0); grid.add(cmbType,1,0);
        grid.add(new Label(isVietnamese ? "Số tiền:" : "Amount:"), 0,1); grid.add(txtAmount,1,1);
        grid.add(new Label(isVietnamese ? "Danh mục:" : "Category:"), 0,2); grid.add(cmbCategory,1,2);
        grid.add(new Label(isVietnamese ? "Ghi chú:" : "Note:"), 0,3); grid.add(txtNote,1,3);
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(btn -> btn == ButtonType.OK ? ButtonType.OK : null);
        dialog.showAndWait().ifPresent(btn -> {
            try {
                double amount = InputValidator.validateAmount(txtAmount.getText(), isVietnamese);
                Category cat = cmbCategory.getValue();
                if (cat == null) throw new IllegalArgumentException(isVietnamese ? "Chọn danh mục" : "Select category");
                if (editTransaction == null) {
                    String id = UUID.randomUUID().toString().substring(0, 8);
                    Transaction tx = (cmbType.getValue() == TransactionType.INCOME) ? new IncomeTransaction(id, amount, cat, txtNote.getText()) : new ExpenseTransaction(id, amount, cat, txtNote.getText());
                    financeService.addTransaction(tx);
                } else {
                    editTransaction.setAmount(amount);
                    editTransaction.setCategory(cat);
                    editTransaction.setNote(txtNote.getText());
                    financeService.updateTransaction(editTransaction);
                }
            } catch (Exception ex) { new Alert(Alert.AlertType.ERROR, ex.getMessage(), ButtonType.OK).showAndWait(); }
        });
    }
}