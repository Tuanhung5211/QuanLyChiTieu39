package com.expensemanager;

import com.expensemanager.jfx.LoginView;
import com.expensemanager.util.ConfigLocalStorage;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        boolean isVietnamese = ConfigLocalStorage.loadLanguage();
        boolean isDark = ConfigLocalStorage.loadTheme();

        LoginView loginView = new LoginView(primaryStage, isVietnamese);
        Scene scene = new Scene(loginView.getRoot(), 500, 700);
        applyTheme(scene, isDark);

        primaryStage.setTitle("Money Tracker");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void applyTheme(Scene scene, boolean isDark) {
        String themeFile = isDark ? "/css/dark-theme.css" : "/css/light-theme.css";
        java.net.URL url = MainApp.class.getResource(themeFile);
        if (url == null) {
            System.err.println("Không tìm thấy file CSS: " + themeFile);
            return;
        }
        scene.getStylesheets().clear();
        scene.getStylesheets().add(url.toExternalForm());
    }

    public static void main(String[] args) {
        launch(args);
    }
}