package com.expensemanager.jfx;

import com.expensemanager.database.DatabaseUtil;
import com.expensemanager.entity.Category;
import com.expensemanager.entity.TransactionType;
import com.expensemanager.service.FinanceService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.List;
import java.util.UUID;

public class CategoryManagerView {
    private final MainView mainView;
    private final FinanceService financeService;
    private boolean isVietnamese;
    private VBox root;
    private ListView<Category> listView;
    private TextField txtName;
    private ComboBox<TransactionType> cmbType;
    private Button btnAdd, btnDelete;

    public CategoryManagerView(MainView mainView, FinanceService financeService, boolean isVietnamese) {
        this.mainView = mainView;
        this.financeService = financeService;
        this.isVietnamese = isVietnamese;
        buildUI();
        refresh();
    }

    private void buildUI() {
        root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.TOP_CENTER);
        Label lblTitle = new Label(isVietnamese ? "Quản lý danh mục" : "Category Manager");
        listView = new ListView<>();
        listView.setPrefHeight(300);
        GridPane form = new GridPane();
        form.setHgap(10); form.setVgap(10); form.setPadding(new Insets(10));
        Label lblName = new Label(isVietnamese ? "Tên danh mục:" : "Name:");
        txtName = new TextField();
        Label lblType = new Label(isVietnamese ? "Loại:" : "Type:");
        cmbType = new ComboBox<>();
        cmbType.getItems().addAll(TransactionType.EXPENSE, TransactionType.INCOME);
        btnAdd = new Button(isVietnamese ? "Thêm" : "Add");
        btnDelete = new Button(isVietnamese ? "Xóa" : "Delete");
        form.add(lblName,0,0); form.add(txtName,1,0);
        form.add(lblType,0,1); form.add(cmbType,1,1);
        form.add(btnAdd,0,2); form.add(btnDelete,1,2);
        HBox content = new HBox(20, listView, form);
        HBox.setHgrow(listView, Priority.ALWAYS);
        root.getChildren().addAll(lblTitle, content);
        btnAdd.setOnAction(e -> addCategory());
        btnDelete.setOnAction(e -> deleteCategory());
    }

    private void addCategory() {
        String name = txtName.getText().trim();
        if (name.isEmpty()) { showAlert(isVietnamese ? "Tên danh mục không được để trống" : "Category name cannot be empty"); return; }
        TransactionType type = cmbType.getValue();
        String id = UUID.randomUUID().toString().substring(0, 8);
        Category cat = new Category(id, name, type);
        DatabaseUtil.insertCategory(cat);
        if (financeService != null) financeService.refreshCategories();
        refresh();
        txtName.clear();
    }

    private void deleteCategory() {
        Category selected = listView.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert(isVietnamese ? "Vui lòng chọn danh mục cần xóa" : "Please select a category to delete"); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, isVietnamese ? "Xóa danh mục " + selected.getName() + "?" : "Delete category " + selected.getName() + "?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(r -> { if (r == ButtonType.YES) { DatabaseUtil.deleteCategory(selected.getId()); if (financeService != null) financeService.refreshCategories(); refresh(); } });
    }

    private void refresh() { listView.setItems(FXCollections.observableArrayList(DatabaseUtil.getAllCategories())); }
    private void showAlert(String msg) { new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK).showAndWait(); }
    public void updateLanguage(boolean isVN) { this.isVietnamese = isVN; root.getChildren().clear(); buildUI(); refresh(); }
    public Node getRoot() { return root; }
}