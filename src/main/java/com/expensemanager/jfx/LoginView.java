package com.expensemanager.jfx;

import com.expensemanager.MainApp;
import com.expensemanager.entity.User;
import com.expensemanager.service.UserService;
import com.expensemanager.util.ConfigLocalStorage;
import com.expensemanager.util.InputValidator;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class LoginView {
    private final Stage primaryStage;
    private boolean isVietnamese;
    private VBox root;
    private StackPane cardContainer;
    private VBox loginCard;
    private VBox registerCard;
    private TextField txtLoginUsername;
    private PasswordField txtLoginPassword;
    private TextField txtRegUsername, txtRegNickname, txtRegEmail;
    private PasswordField txtRegPassword, txtRegConfirmPassword;
    private ComboBox<String> comboGender;

    public LoginView(Stage primaryStage, boolean isVietnamese) {
        this.primaryStage = primaryStage;
        this.isVietnamese = isVietnamese;
        buildUI();
    }

    private void buildUI() {
        root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));
        cardContainer = new StackPane();
        loginCard = createLoginCard();
        registerCard = createRegisterCard();
        cardContainer.getChildren().addAll(loginCard, registerCard);
        registerCard.setVisible(false);
        Hyperlink switchToRegister = new Hyperlink(isVietnamese ? "Chưa có tài khoản? Đăng ký" : "Don't have an account? Sign up");
        Hyperlink switchToLogin = new Hyperlink(isVietnamese ? "Quay lại đăng nhập" : "Back to login");
        switchToRegister.setOnAction(e -> {
            loginCard.setVisible(false);
            registerCard.setVisible(true);
        });
        switchToLogin.setOnAction(e -> {
            registerCard.setVisible(false);
            loginCard.setVisible(true);
        });
        registerCard.getChildren().add(switchToLogin);
        root.getChildren().addAll(cardContainer, switchToRegister);
    }

    private VBox createLoginCard() {
        VBox card = new VBox(15);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(30));
        card.setMaxWidth(400);
        Label lblTitle = new Label(isVietnamese ? "ĐĂNG NHẬP" : "LOGIN");
        txtLoginUsername = new TextField();
        txtLoginUsername.setPromptText(isVietnamese ? "Tên đăng nhập" : "Username");
        txtLoginPassword = new PasswordField();
        txtLoginPassword.setPromptText(isVietnamese ? "Mật khẩu" : "Password");
        Hyperlink forgotLink = new Hyperlink(isVietnamese ? "Quên mật khẩu?" : "Forgot password?");
        forgotLink.setOnAction(e -> showForgotPasswordDialog());
        Button btnLogin = new Button(isVietnamese ? "ĐĂNG NHẬP" : "LOGIN");
        btnLogin.setOnAction(e -> login());
        card.getChildren().addAll(lblTitle, txtLoginUsername, txtLoginPassword, forgotLink, btnLogin);
        return card;
    }

    private VBox createRegisterCard() {
        VBox card = new VBox(15);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(30));
        card.setMaxWidth(400);
        Label lblTitle = new Label(isVietnamese ? "ĐĂNG KÝ" : "SIGN UP");
        txtRegUsername = new TextField();
        txtRegUsername.setPromptText(isVietnamese ? "Tên đăng nhập" : "Username");
        txtRegNickname = new TextField();
        txtRegNickname.setPromptText(isVietnamese ? "Tên hiển thị" : "Nickname");
        txtRegEmail = new TextField();
        txtRegEmail.setPromptText("Email");
        txtRegPassword = new PasswordField();
        txtRegPassword.setPromptText(isVietnamese ? "Mật khẩu" : "Password");
        txtRegConfirmPassword = new PasswordField();
        txtRegConfirmPassword.setPromptText(isVietnamese ? "Xác nhận mật khẩu" : "Confirm password");
        comboGender = new ComboBox<>();
        comboGender.getItems().addAll(isVietnamese ? "Nam" : "Male", isVietnamese ? "Nữ" : "Female", isVietnamese ? "Khác" : "Other");
        comboGender.setValue(isVietnamese ? "Nam" : "Male");
        Button btnRegister = new Button(isVietnamese ? "ĐĂNG KÝ" : "REGISTER");
        btnRegister.setOnAction(e -> register());
        card.getChildren().addAll(lblTitle, txtRegUsername, txtRegNickname, txtRegEmail,
                txtRegPassword, txtRegConfirmPassword, comboGender, btnRegister);
        return card;
    }

    private void login() {
        String username = txtLoginUsername.getText();
        String password = txtLoginPassword.getText();
        try {
            InputValidator.validateLogin(username, password, isVietnamese);
            User user = UserService.login(username, password);
            if (user != null) {
                MainView mainView = new MainView(primaryStage, isVietnamese);
                Scene scene = new Scene(mainView.getRoot(), 1200, 750);
                boolean isDark = ConfigLocalStorage.loadTheme();
                MainApp.applyTheme(scene, isDark);
                primaryStage.setScene(scene);
                primaryStage.setResizable(true);
                primaryStage.setTitle("Money Tracker");
            } else {
                showAlert(isVietnamese ? "Sai tên đăng nhập hoặc mật khẩu" : "Invalid username or password");
            }
        } catch (IllegalArgumentException ex) {
            showAlert(ex.getMessage());
        }
    }

    private void register() {
        String username = txtRegUsername.getText();
        String password = txtRegPassword.getText();
        String confirm = txtRegConfirmPassword.getText();
        String email = txtRegEmail.getText();
        String nickname = txtRegNickname.getText();
        String gender = comboGender.getValue();
        try {
            InputValidator.validateRegister(username, password, confirm, email, nickname, isVietnamese);
            String genderEn = gender;
            if (isVietnamese) {
                if (gender.equals("Nam")) genderEn = "Male";
                else if (gender.equals("Nữ")) genderEn = "Female";
                else genderEn = "Other";
            }
            User user = UserService.register(username, password, nickname, email, genderEn);
            if (user != null) {
                showAlert(isVietnamese ? "Đăng ký thành công! Vui lòng đăng nhập." : "Registration successful! Please login.");
                registerCard.setVisible(false);
                loginCard.setVisible(true);
                txtLoginUsername.setText(username);
                txtLoginPassword.clear();
            }
        } catch (IllegalArgumentException ex) {
            showAlert(ex.getMessage());
        }
    }

    private void showForgotPasswordDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(isVietnamese ? "Khôi phục mật khẩu" : "Forgot Password");
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        TextField txtEmail = new TextField();
        txtEmail.setPromptText("Email");
        grid.add(new Label(isVietnamese ? "Nhập email đã đăng ký:" : "Enter your registered email:"), 0, 0);
        grid.add(txtEmail, 1, 0);
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(btn -> btn == ButtonType.OK ? ButtonType.OK : null);
        dialog.showAndWait().ifPresent(btn -> {
            String email = txtEmail.getText().trim();
            if (email.isEmpty()) {
                showAlert(isVietnamese ? "Email không được để trống!" : "Email cannot be empty!");
                return;
            }
            boolean sent = UserService.resetPasswordByEmail(email);
            if (sent) {
                showAlert(isVietnamese ? "Mật khẩu mới đã được gửi vào email của bạn!" : "New password sent to your email!");
            } else {
                showAlert(isVietnamese ? "Email không tồn tại hoặc lỗi gửi mail!" : "Email not found or mail error!");
            }
        });
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.showAndWait();
    }

    public VBox getRoot() {
        return root;
    }

    // PHƯƠNG THỨC show() DÙNG ĐỂ HIỂN THỊ LOGIN VIEW KHI ĐĂNG XUẤT
    public void show() {
        Scene scene = new Scene(root, 500, 700);
        boolean isDark = ConfigLocalStorage.loadTheme();
        MainApp.applyTheme(scene, isDark);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.setTitle("Money Tracker - Login");
        primaryStage.show();
    }
}