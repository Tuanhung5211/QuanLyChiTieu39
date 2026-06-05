package com.expensemanager.jfx;

import com.expensemanager.MainApp;
import com.expensemanager.database.DatabaseUtil;
import com.expensemanager.entity.User;
import com.expensemanager.service.SessionManager;
import com.expensemanager.service.UserService;
import com.expensemanager.util.ConfigLocalStorage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class SettingsView {
    private final MainView mainView;
    private boolean isVietnamese;
    private VBox root;
    private TabPane tabPane;

    public SettingsView(MainView mainView, boolean isVietnamese) {
        this.mainView = mainView;
        this.isVietnamese = isVietnamese;
        buildUI();
    }

    private void buildUI() {
        root = new VBox(20);
        root.setPadding(new Insets(20));
        Label title = new Label(isVietnamese ? "Cài đặt hệ thống" : "Settings");
        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        Tab accountTab = new Tab(isVietnamese ? "Thông tin cá nhân" : "Account");
        accountTab.setContent(createAccountPane());
        Tab systemTab = new Tab(isVietnamese ? "Giao diện & Ngôn ngữ" : "System");
        systemTab.setContent(createSystemPane());
        Tab categoryTab = new Tab(isVietnamese ? "Quản lý danh mục" : "Categories");
        categoryTab.setContent(new CategoryManagerView(mainView, mainView.getFinanceService(), isVietnamese).getRoot());
        tabPane.getTabs().addAll(accountTab, systemTab, categoryTab);
        root.getChildren().addAll(title, tabPane);
    }

    private Node createAccountPane() {
        VBox pane = new VBox(16);
        pane.setPadding(new Insets(20));
        User user = DatabaseUtil.getUserByUsername(SessionManager.getCurrentUsername());
        if (user == null) return new Label("Error loading user");
        GridPane grid = new GridPane();
        grid.setHgap(16); grid.setVgap(16);
        Label lblNick = new Label(isVietnamese ? "Tên hiển thị:" : "Nickname:");
        TextField txtNick = new TextField(user.getNickname());
        Label lblEmail = new Label("Email:");
        TextField txtEmail = new TextField(user.getEmail());
        Label lblGender = new Label(isVietnamese ? "Giới tính:" : "Gender:");
        ComboBox<String> cmbGender = new ComboBox<>();
        cmbGender.getItems().addAll("Male", "Female", "Other");
        cmbGender.setValue(user.getGender());
        grid.add(lblNick,0,0); grid.add(txtNick,1,0);
        grid.add(lblEmail,0,1); grid.add(txtEmail,1,1);
        grid.add(lblGender,0,2); grid.add(cmbGender,1,2);
        Button btnSave = new Button(isVietnamese ? "Lưu thay đổi" : "Save Changes");
        btnSave.setOnAction(e -> {
            user.setNickname(txtNick.getText());
            user.setEmail(txtEmail.getText());
            user.setGender(cmbGender.getValue());
            DatabaseUtil.updateUser(user);
            new Alert(Alert.AlertType.INFORMATION, isVietnamese ? "Đã cập nhật!" : "Updated!").showAndWait();
            mainView.updateGlobalLanguage(isVietnamese);
        });
        Button btnChangePass = new Button(isVietnamese ? "Đổi mật khẩu" : "Change Password");
        btnChangePass.setOnAction(e -> openChangePasswordDialog());
        HBox btnBox = new HBox(16, btnSave, btnChangePass);
        pane.getChildren().addAll(grid, btnBox);
        return pane;
    }

    private void openChangePasswordDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(isVietnamese ? "Đổi mật khẩu" : "Change Password");
        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20));
        PasswordField oldPass = new PasswordField();
        PasswordField newPass = new PasswordField();
        PasswordField confirmPass = new PasswordField();
        grid.add(new Label(isVietnamese ? "Mật khẩu cũ:" : "Old password:"),0,0); grid.add(oldPass,1,0);
        grid.add(new Label(isVietnamese ? "Mật khẩu mới:" : "New password:"),0,1); grid.add(newPass,1,1);
        grid.add(new Label(isVietnamese ? "Xác nhận:" : "Confirm:"),0,2); grid.add(confirmPass,1,2);
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(b -> b);
        dialog.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                if (!newPass.getText().equals(confirmPass.getText())) {
                    new Alert(Alert.AlertType.ERROR, isVietnamese ? "Mật khẩu mới không khớp" : "New passwords do not match").showAndWait();
                    return;
                }
                User user = DatabaseUtil.getUserByUsername(SessionManager.getCurrentUsername());
                if (user != null && user.getPasswordHash().equals(UserService.hashPassword(oldPass.getText()))) {
                    user.setPasswordHash(UserService.hashPassword(newPass.getText()));
                    DatabaseUtil.updateUserPassword(user.getId(), user.getPasswordHash());
                    new Alert(Alert.AlertType.INFORMATION, isVietnamese ? "Đổi mật khẩu thành công! Vui lòng đăng nhập lại." : "Password changed! Please login again.").showAndWait();
                    SessionManager.logout();
                    Stage stage = (Stage) root.getScene().getWindow();
                    stage.close();
                    new LoginView(new Stage(), isVietnamese).show();
                } else {
                    new Alert(Alert.AlertType.ERROR, isVietnamese ? "Mật khẩu cũ không đúng" : "Old password incorrect").showAndWait();
                }
            }
        });
    }

    private Node createSystemPane() {
        VBox pane = new VBox(16);
        pane.setPadding(new Insets(20));
        Label langLabel = new Label(isVietnamese ? "Ngôn ngữ:" : "Language:");
        ToggleGroup langGroup = new ToggleGroup();
        RadioButton rbVN = new RadioButton("Tiếng Việt");
        RadioButton rbEN = new RadioButton("English");
        rbVN.setToggleGroup(langGroup);
        rbEN.setToggleGroup(langGroup);
        rbVN.setSelected(isVietnamese);
        HBox langBox = new HBox(20, rbVN, rbEN);
        Button btnSaveLang = new Button(isVietnamese ? "Lưu ngôn ngữ" : "Save Language");
        btnSaveLang.setOnAction(e -> {
            boolean isVN = rbVN.isSelected();
            ConfigLocalStorage.saveConfig(isVN, (int) root.getScene().getWindow().getWidth(), (int) root.getScene().getWindow().getHeight());
            mainView.updateGlobalLanguage(isVN);
        });

        // Theme toggle
        Label themeLabel = new Label(isVietnamese ? "Giao diện:" : "Theme:");
        Button btnToggleTheme = new Button();
        updateThemeButtonText(btnToggleTheme);
        btnToggleTheme.setOnAction(e -> {
            Scene scene = root.getScene();
            boolean isDark = scene.getStylesheets().stream().anyMatch(s -> s.contains("dark"));
            String newTheme = isDark ? "/css/light-theme.css" : "/css/dark-theme.css";
            scene.getStylesheets().clear();
            scene.getStylesheets().add(getClass().getResource(newTheme).toExternalForm());
            ConfigLocalStorage.saveTheme(!isDark);
            updateThemeButtonText(btnToggleTheme);
        });
        HBox themeBox = new HBox(20, themeLabel, btnToggleTheme);

        pane.getChildren().addAll(langLabel, langBox, btnSaveLang, new Separator(), themeBox);
        return pane;
    }

    private void updateThemeButtonText(Button btn) {
        Scene scene = root.getScene();
        boolean isDark = scene != null && scene.getStylesheets().stream().anyMatch(s -> s.contains("dark"));
        if (isVietnamese) btn.setText(isDark ? "Sáng" : "Tối");
        else btn.setText(isDark ? "Light" : "Dark");
    }

    public void updateLanguage(boolean isVN) { this.isVietnamese = isVN; root.getChildren().clear(); buildUI(); }
    public Node getRoot() { return root; }
}