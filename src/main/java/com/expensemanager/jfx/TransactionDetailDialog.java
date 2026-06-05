package com.expensemanager.jfx;

import com.expensemanager.entity.Transaction;
import com.expensemanager.entity.TransactionType;
import com.expensemanager.service.FinanceService;
import com.expensemanager.util.EmojiUtil;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class TransactionDetailDialog {
    private final FinanceService financeService;
    private final Transaction transaction;
    private final boolean isVietnamese;

    public TransactionDetailDialog(FinanceService financeService, Transaction transaction, boolean isVietnamese) {
        this.financeService = financeService;
        this.transaction = transaction;
        this.isVietnamese = isVietnamese;
    }

    public void showAndWait() {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(isVietnamese ? "Chi tiết giao dịch" : "Transaction Details");
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        String emoji = EmojiUtil.CATEGORY_EMOJI.getOrDefault(transaction.getCategory().getName(), "📌");
        Label lblIcon = new Label(emoji);
        lblIcon.setStyle("-fx-font-size: 40px;");
        Label lblNote = new Label(transaction.getNote().isEmpty() ? (isVietnamese ? "Không có ghi chú" : "No note") : transaction.getNote());
        lblNote.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        Label lblDate = new Label(transaction.getDateTime().toString());
        GridPane grid = new GridPane();
        grid.setHgap(15); grid.setVgap(10);
        grid.addRow(0, new Label(isVietnamese ? "Danh mục:" : "Category:"), new Label(transaction.getCategory().getName()));
        grid.addRow(1, new Label(isVietnamese ? "Số tiền:" : "Amount:"), new Label(String.format("%,.0f VND", transaction.getAmount())));
        grid.addRow(2, new Label(isVietnamese ? "Loại:" : "Type:"), new Label(transaction.getType() == TransactionType.INCOME ? (isVietnamese ? "Thu" : "Income") : (isVietnamese ? "Chi" : "Expense")));
        HBox buttons = new HBox(10);
        Button btnEdit = new Button(isVietnamese ? "Sửa" : "Edit");
        Button btnDelete = new Button(isVietnamese ? "Xóa" : "Delete");
        Button btnClose = new Button(isVietnamese ? "Đóng" : "Close");
        btnEdit.setOnAction(e -> { new AddTransactionDialog(financeService, isVietnamese, transaction).showAndWait(); stage.close(); });
        btnDelete.setOnAction(e -> { Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, isVietnamese ? "Xóa giao dịch này?" : "Delete this transaction?", ButtonType.YES, ButtonType.NO); confirm.showAndWait().ifPresent(res -> { if (res == ButtonType.YES) { financeService.deleteTransaction(transaction.getId()); stage.close(); } }); });
        btnClose.setOnAction(e -> stage.close());
        buttons.getChildren().addAll(btnEdit, btnDelete, btnClose);
        root.getChildren().addAll(lblIcon, lblNote, lblDate, grid, buttons);
        Scene scene = new Scene(root, 450, 400);
        stage.setScene(scene);
        stage.showAndWait();
    }
}