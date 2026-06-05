package com.expensemanager.jfx;

import com.expensemanager.entity.Transaction;
import com.expensemanager.entity.TransactionType;
import com.expensemanager.observer.EventType;
import com.expensemanager.observer.Observer;
import com.expensemanager.service.FinanceService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ExpensesView implements Observer {
    private final FinanceService financeService;
    private boolean isVietnamese;
    private VBox root;
    private TableView<Transaction> tableView;
    private TextField searchField;
    private ComboBox<TransactionType> typeFilter;
    private Button btnAdd;
    private int currentPage = 0;
    private final int PAGE_SIZE = 20;
    private Label lblPageInfo;
    private Button btnPrev, btnNext;

    public ExpensesView(FinanceService financeService, boolean isVietnamese) {
        this.financeService = financeService;
        this.isVietnamese = isVietnamese;
        financeService.attach(this);
        buildUI();
        refreshTable();
    }

    private void buildUI() {
        root = new VBox(16);
        root.setPadding(new Insets(20));
        Label title = new Label(isVietnamese ? "Quản lý giao dịch" : "Transaction Manager");
        HBox filterBar = new HBox(12);
        filterBar.setAlignment(Pos.CENTER_LEFT);
        searchField = new TextField();
        searchField.setPromptText(isVietnamese ? "Tìm kiếm..." : "Search...");
        searchField.textProperty().addListener((obs, old, val) -> refreshTable());
        typeFilter = new ComboBox<>();
        typeFilter.getItems().addAll(TransactionType.INCOME, TransactionType.EXPENSE);
        typeFilter.setPromptText(isVietnamese ? "Loại" : "Type");
        typeFilter.valueProperty().addListener((obs, old, val) -> refreshTable());
        btnAdd = new Button(isVietnamese ? "+ Thêm giao dịch" : "+ Add Transaction");
        btnAdd.setOnAction(e -> new AddTransactionDialog(financeService, isVietnamese).showAndWait());
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        filterBar.getChildren().addAll(searchField, typeFilter, spacer, btnAdd);

        tableView = new TableView<>();
        setupTableColumns();
        tableView.setRowFactory(tv -> {
            TableRow<Transaction> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                    new TransactionDetailDialog(financeService, row.getItem(), isVietnamese).showAndWait();
                }
            });
            return row;
        });

        HBox pagination = new HBox(10);
        pagination.setAlignment(Pos.CENTER);
        btnPrev = new Button("<");
        btnNext = new Button(">");
        lblPageInfo = new Label();
        btnPrev.setOnAction(e -> { if (currentPage > 0) { currentPage--; refreshTable(); } });
        btnNext.setOnAction(e -> { currentPage++; refreshTable(); });
        pagination.getChildren().addAll(btnPrev, lblPageInfo, btnNext);
        root.getChildren().addAll(title, filterBar, tableView, pagination);
    }

    private void setupTableColumns() {
        TableColumn<Transaction, String> colDate = new TableColumn<>(isVietnamese ? "Ngày" : "Date");
        colDate.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
        TableColumn<Transaction, String> colType = new TableColumn<>(isVietnamese ? "Loại" : "Type");
        colType.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getType() == TransactionType.INCOME ? "Thu" : "Chi"));
        TableColumn<Transaction, String> colCategory = new TableColumn<>(isVietnamese ? "Danh mục" : "Category");
        colCategory.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getCategory().getName()));
        TableColumn<Transaction, Double> colAmount = new TableColumn<>(isVietnamese ? "Số tiền" : "Amount");
        colAmount.setCellValueFactory(cell -> new javafx.beans.property.SimpleDoubleProperty(cell.getValue().getAmount()).asObject());
        colAmount.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                setText(empty || amount == null ? null : String.format("%,.0f VND", amount));
            }
        });
        TableColumn<Transaction, String> colNote = new TableColumn<>(isVietnamese ? "Ghi chú" : "Note");
        colNote.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getNote()));
        TableColumn<Transaction, Void> colAction = new TableColumn<>(isVietnamese ? "Hành động" : "Action");
        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button(isVietnamese ? "Sửa" : "Edit");
            private final Button deleteBtn = new Button(isVietnamese ? "Xóa" : "Delete");
            {
                editBtn.setOnAction(e -> { Transaction tx = getTableView().getItems().get(getIndex()); new AddTransactionDialog(financeService, isVietnamese, tx).showAndWait(); });
                deleteBtn.setOnAction(e -> { Transaction tx = getTableView().getItems().get(getIndex()); Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, isVietnamese ? "Xóa giao dịch?" : "Delete transaction?", ButtonType.YES, ButtonType.NO); confirm.showAndWait().ifPresent(r -> { if (r == ButtonType.YES) financeService.deleteTransaction(tx.getId()); }); });
            }
            @Override protected void updateItem(Void item, boolean empty) { setGraphic(empty ? null : new HBox(8, editBtn, deleteBtn)); }
        });
        tableView.getColumns().addAll(colDate, colType, colCategory, colAmount, colNote, colAction);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void refreshTable() {
        int total = financeService.getTransactionCount();
        int totalPages = Math.max(1, (int) Math.ceil((double) total / PAGE_SIZE));
        if (currentPage >= totalPages) currentPage = totalPages - 1;
        if (currentPage < 0) currentPage = 0;
        lblPageInfo.setText((isVietnamese ? "Trang " : "Page ") + (currentPage + 1) + "/" + totalPages);
        btnPrev.setDisable(currentPage == 0);
        btnNext.setDisable(currentPage + 1 >= totalPages);
        List<Transaction> all = financeService.getTransactionsWithPagination(currentPage * PAGE_SIZE, PAGE_SIZE);
        ObservableList<Transaction> filtered = FXCollections.observableArrayList(all);
        String search = searchField.getText().toLowerCase();
        TransactionType type = typeFilter.getValue();
        if (search != null && !search.isEmpty()) filtered = filtered.filtered(t -> t.getNote().toLowerCase().contains(search) || t.getCategory().getName().toLowerCase().contains(search));
        if (type != null) filtered = filtered.filtered(t -> t.getType() == type);
        tableView.setItems(filtered);
    }

    @Override public void update(EventType eventType, Object data) { if (eventType == EventType.TRANSACTION_ADDED || eventType == EventType.TRANSACTION_UPDATED || eventType == EventType.TRANSACTION_DELETED) Platform.runLater(this::refreshTable); }
    public void updateLanguage(boolean isVN) { this.isVietnamese = isVN; root.getChildren().clear(); buildUI(); refreshTable(); }
    public Node getRoot() { return root; }
}