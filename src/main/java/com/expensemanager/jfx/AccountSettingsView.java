package com.expensemanager.jfx;

import com.expensemanager.database.DatabaseUtil;
import com.expensemanager.entity.User;
import com.expensemanager.service.SessionManager;
import com.expensemanager.service.UserService;
import com.expensemanager.util.InputValidator;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class AccountSettingsView {
    private final MainView mainView;
    private boolean isVietnamese;
    private VBox root;
    private TextField txtNickname, txtEmail;
    private ComboBox<String> cmbGender;
    private Button btnUpdate, btnChangePass, btnDelete;

    public AccountSettingsView(MainView mainView, boolean isVietnamese) {
        this.mainView = mainView;
        this.isVietnamese = isVietnamese;
        buildUI();
        refresh();
    }

    private void buildUI() {
        root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.TOP_CENTER);

        Label lblTitle = new Label(isVietnamese ? "Thông tin cá nhân" : "Personal Info");
        lblTitle.getStyleClass().add("label-title");

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(10));

        Label lblNick = new Label(isVietnamese ? "Tên hiển thị:" : "Nickname:");
        lblNick.getStyleClass().add("label-secondary");
        txtNickname = new TextField();
        txtNickname.getStyleClass().add("text-field");

        Label lblEmail = new Label("Email:");
        lblEmail.getStyleClass().add("label-secondary");
        txtEmail = new TextField();
        txtEmail.getStyleClass().add("text-field");

        Label lblGender = new Label(isVietnamese ? "Giới tính:" : "Gender:");
        lblGender.getStyleClass().add("label-secondary");
        cmbGender = new ComboBox<>();
        cmbGender.getItems().addAll(isVietnamese ? "Nam" : "Male", isVietnamese ? "Nữ" : "Female", isVietnamese ? "Khác" : "Other");
        cmbGender.getStyleClass().add("combo-box");

        grid.add(lblNick, 0, 0);
        grid.add(txtNickname, 1, 0);
        grid.add(lblEmail, 0, 1);
        grid.add(txtEmail, 1, 1);
        grid.add(lblGender, 0, 2);
        grid.add(cmbGender, 1, 2);

        btnUpdate = new Button(isVietnamese ? "Lưu thay đổi" : "Save Changes");
        btnUpdate.getStyleClass().add("button-primary");
        btnUpdate.setOnAction(e -> updateProfile());

        btnChangePass = new Button(isVietnamese ? "Đổi mật khẩu" : "Change Password");
        btnChangePass.getStyleClass().add("button-secondary");
        btnChangePass.setOnAction(e -> openChangePasswordDialog());

        btnDelete = new Button(isVietnamese ? "Xóa tài khoản" : "Delete Account");
        btnDelete.getStyleClass().add("button-secondary");
        btnDelete.setStyle("-fx-text-fill: #F44336;");
        btnDelete.setOnAction(e -> deleteAccount());

        HBox btnBox = new HBox(15, btnUpdate, btnChangePass);
        btnBox.setAlignment(Pos.CENTER);

        root.getChildren().addAll(lblTitle, grid, btnBox, btnDelete);
    }

    private void updateProfile() {
        String nickname = txtNickname.getText();
        String email = txtEmail.getText();
        String gender = cmbGender.getValue();
        try {
            InputValidator.validateNickname(nickname, isVietnamese);
            InputValidator.validateEmail(email, isVietnamese);
            User user = DatabaseUtil.getUserByUsername(SessionManager.getCurrentUsername());
            if (user != null) {
                user.setNickname(nickname);
                user.setEmail(email);
                user.setGender(gender.equals("Nam") ? "Male" : gender.equals("Nữ") ? "Female" : "Other");
                DatabaseUtil.updateUser(user);
                Alert alert = new Alert(Alert.AlertType.INFORMATION, isVietnamese ? "Cập nhật thành công!" : "Profile updated!");
                alert.showAndWait();
            }
        } catch (IllegalArgumentException ex) {
            Alert alert = new Alert(Alert.AlertType.WARNING, ex.getMessage());
            alert.showAndWait();
        }
    }

    private void openChangePasswordDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(isVietnamese ? "Đổi mật khẩu" : "Change Password");
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        PasswordField oldPass = new PasswordField();
        PasswordField newPass = new PasswordField();
        PasswordField confirmPass = new PasswordField();
        grid.add(new Label(isVietnamese ? "Mật khẩu cũ:" : "Old password:"), 0, 0);
        grid.add(oldPass, 1, 0);
        grid.add(new Label(isVietnamese ? "Mật khẩu mới:" : "New password:"), 0, 1);
        grid.add(newPass, 1, 1);
        grid.add(new Label(isVietnamese ? "Xác nhận:" : "Confirm:"), 0, 2);
        grid.add(confirmPass, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(b -> b);
        dialog.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    InputValidator.validatePasswordChange(oldPass.getText(), newPass.getText(), confirmPass.getText(), isVietnamese);
                    User user = UserService.login(SessionManager.getCurrentUsername(), oldPass.getText());
                    if (user == null) {
                        Alert alert = new Alert(Alert.AlertType.WARNING, isVietnamese ? "Mật khẩu cũ không đúng" : "Old password incorrect");
                        alert.showAndWait();
                    } else {
                        user.setPasswordHash(UserService.hashPassword(newPass.getText()));
                        DatabaseUtil.updateUserPassword(user.getId(), user.getPasswordHash());
                        Alert alert = new Alert(Alert.AlertType.INFORMATION, isVietnamese ? "Đổi mật khẩu thành công! Vui lòng đăng nhập lại." : "Password changed! Please login again.");
                        alert.showAndWait();
                        // logout
                        SessionManager.logout();
                        Stage stage = (Stage) root.getScene().getWindow();
                        stage.close();
                        new LoginView(new Stage(), isVietnamese).show();
                    }
                } catch (IllegalArgumentException ex) {
                    Alert alert = new Alert(Alert.AlertType.WARNING, ex.getMessage());
                    alert.showAndWait();
                }
            }
        });
    }

    private void deleteAccount() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, isVietnamese ? "Bạn chắc chắn muốn xóa tài khoản? Hành động không thể hoàn tác." : "Delete account permanently? This cannot be undone.", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.YES) {
                String userId = SessionManager.getCurrentUserId();
                if (userId != null) {
                    DatabaseUtil.deleteTransactionsByUser(userId);
                    DatabaseUtil.deleteBudgetsByUser(userId);
                    DatabaseUtil.deleteUser(userId);
                    SessionManager.logout();
                    Stage stage = (Stage) root.getScene().getWindow();
                    stage.close();
                    new LoginView(new Stage(), isVietnamese).show();
                }
            }
        });
    }

    public void refresh() {
        User user = DatabaseUtil.getUserByUsername(SessionManager.getCurrentUsername());
        if (user != null) {
            txtNickname.setText(user.getNickname());
            txtEmail.setText(user.getEmail());
            String g = user.getGender();
            if (g.equalsIgnoreCase("Male")) cmbGender.setValue(isVietnamese ? "Nam" : "Male");
            else if (g.equalsIgnoreCase("Female")) cmbGender.setValue(isVietnamese ? "Nữ" : "Female");
            else cmbGender.setValue(isVietnamese ? "Khác" : "Other");
        }
    }

    public void updateLanguage(boolean isVN) {
        this.isVietnamese = isVN;
        // Rebuild or update text (simplified: rebuild UI)
        root.getChildren().clear();
        buildUI();
        refresh();
    }

    public Node getRoot() {
        return root;
    }
}