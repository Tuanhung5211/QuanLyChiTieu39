package com.expensemanager.util;

import java.io.*;
import java.awt.Dimension;
import java.util.Properties;

public class ConfigLocalStorage {
    private static final String CONFIG_FILE = "config.properties";

    // Hàm lưu trạng thái cấu hình xuống file vật lý
    public static void saveConfig(boolean isVietnamese, int width, int height) {
        Properties prop = new Properties();
        prop.setProperty("language", isVietnamese ? "vn" : "en");
        prop.setProperty("windowWidth", String.valueOf(width));
        prop.setProperty("windowHeight", String.valueOf(height));
        try (OutputStream output = new FileOutputStream(CONFIG_FILE)) {
            prop.store(output, "Money Tracker Local Configurations");
        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    // Hàm nạp ngôn ngữ đã lưu
    public static boolean loadLanguage() {
        Properties prop = new Properties();
        try (InputStream input = new FileInputStream(CONFIG_FILE)) {
            prop.load(input);
            return "vn".equalsIgnoreCase(prop.getProperty("language", "vn"));
        } catch (IOException ex) {
            return true; // Mặc định là Tiếng Việt nếu chưa có file
        }
    }

    // Hàm nạp kích thước cửa sổ đã lưu
    public static Dimension loadWindowSize() {
        Properties prop = new Properties();
        try (InputStream input = new FileInputStream(CONFIG_FILE)) {
            prop.load(input);
            int w = Integer.parseInt(prop.getProperty("windowWidth", "1200"));
            int h = Integer.parseInt(prop.getProperty("windowHeight", "750"));
            return new Dimension(w, h);
        } catch (IOException ex) {
            return new Dimension(1200, 750); // Mặc định nếu chưa cài đặt
        }
    }
}