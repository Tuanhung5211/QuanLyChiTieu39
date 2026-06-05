package com.expensemanager.jfx;

import com.expensemanager.util.ConfigLocalStorage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class SystemConfigView {
    private final MainView mainView;
    private boolean isVietnamese;
    private VBox root;
    private ComboBox<String> comboSize;
    private RadioButton rbVietnamese, rbEnglish;

    public SystemConfigView(MainView mainView, boolean isVietnamese) {
        this.mainView = mainView;
        this.isVietnamese = isVietnamese;
        buildUI();
    }

    private void buildUI() {
        root = new VBox(20);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.TOP_CENTER);

        Label lblTitle = new Label(isVietnamese ? "Cấu hình hệ thống" : "System Configuration");
        lblTitle.getStyleClass().add("label-title");

        VBox card = new VBox(15);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(20));
        card.setMaxWidth(500);

        // Language
        Label lblLang = new Label(isVietnamese ? "Ngôn ngữ" : "Language");
        lblLang.getStyleClass().add("label-primary");
        rbVietnamese = new RadioButton("Tiếng Việt");
        rbEnglish = new RadioButton("English");
        ToggleGroup langGroup = new ToggleGroup();
        rbVietnamese.setToggleGroup(langGroup);
        rbEnglish.setToggleGroup(langGroup);
        rbVietnamese.setSelected(isVietnamese);
        HBox langBox = new HBox(20, rbVietnamese, rbEnglish);
        Button btnSaveLang = new Button(isVietnamese ? "Lưu ngôn ngữ" : "Save Language");
        btnSaveLang.getStyleClass().add("button-primary");
        btnSaveLang.setOnAction(e -> saveLanguage());

        // Window size
        Label lblSize = new Label(isVietnamese ? "Kích thước cửa sổ" : "Window Size");
        lblSize.getStyleClass().add("label-primary");
        comboSize = new ComboBox<>();
        comboSize.getItems().addAll("1200x750", "1400x800", "1600x950");
        comboSize.setValue("1200x750");
        Button btnSaveSize = new Button(isVietnamese ? "Áp dụng kích thước" : "Apply Size");
        btnSaveSize.getStyleClass().add("button-primary");
        btnSaveSize.setOnAction(e -> saveWindowSize());

        card.getChildren().addAll(lblLang, langBox, btnSaveLang, new Separator(), lblSize, comboSize, btnSaveSize);
        root.getChildren().addAll(lblTitle, card);
    }

    private void saveLanguage() {
        boolean isVN = rbVietnamese.isSelected();
        ConfigLocalStorage.saveConfig(isVN, (int) root.getScene().getWindow().getWidth(), (int) root.getScene().getWindow().getHeight());
        if (mainView != null) {
            mainView.updateGlobalLanguage(isVN);
        }
        Alert alert = new Alert(Alert.AlertType.INFORMATION, isVN ? "Đã đổi ngôn ngữ!" : "Language changed!");
        alert.showAndWait();
    }

    private void saveWindowSize() {
        String val = comboSize.getValue();
        int w = 1200, h = 750;
        if (val.equals("1400x800")) { w = 1400; h = 800; }
        else if (val.equals("1600x950")) { w = 1600; h = 950; }
        Stage stage = (Stage) root.getScene().getWindow();
        stage.setWidth(w);
        stage.setHeight(h);
        stage.centerOnScreen();
        ConfigLocalStorage.saveConfig(isVietnamese, w, h);
    }

    public void updateLanguage(boolean isVN) {
        this.isVietnamese = isVN;
        root.getChildren().clear();
        buildUI();
    }

    public Node getRoot() {
        return root;
    }
}