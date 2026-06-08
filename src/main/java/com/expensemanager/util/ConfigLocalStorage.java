package com.expensemanager.util;

import java.io.*;
import java.awt.Dimension;
import java.util.Properties;

public class ConfigLocalStorage {
    private static final String CONFIG_FILE = "config.properties";

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

    public static boolean loadLanguage() {
        Properties prop = new Properties();
        try (InputStream input = new FileInputStream(CONFIG_FILE)) {
            prop.load(input);
            return "vn".equalsIgnoreCase(prop.getProperty("language", "vn"));
        } catch (IOException ex) {
            return true;
        }
    }

    public static Dimension loadWindowSize() {
        Properties prop = new Properties();
        try (InputStream input = new FileInputStream(CONFIG_FILE)) {
            prop.load(input);
            int w = Integer.parseInt(prop.getProperty("windowWidth", "1200"));
            int h = Integer.parseInt(prop.getProperty("windowHeight", "750"));
            return new Dimension(w, h);
        } catch (IOException ex) {
            return new Dimension(1200, 750);
        }
    }

    public static void saveTheme(boolean isDark) {
        Properties prop = new Properties();
        try (InputStream input = new FileInputStream(CONFIG_FILE)) {
            prop.load(input);
        } catch (IOException ignored) {}
        prop.setProperty("theme", isDark ? "dark" : "light");
        try (OutputStream output = new FileOutputStream(CONFIG_FILE)) {
            prop.store(output, "Money Tracker Local Configurations");
        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    public static boolean loadTheme() {
        Properties prop = new Properties();
        try (InputStream input = new FileInputStream(CONFIG_FILE)) {
            prop.load(input);
            return "dark".equalsIgnoreCase(prop.getProperty("theme", "dark"));
        } catch (IOException ex) {
            return true;
        }
    }

    // Thêm cho theme preset
    public static void saveThemePreset(String preset) {
        Properties prop = new Properties();
        try (InputStream input = new FileInputStream(CONFIG_FILE)) {
            prop.load(input);
        } catch (IOException ignored) {}
        prop.setProperty("themePreset", preset);
        try (OutputStream output = new FileOutputStream(CONFIG_FILE)) {
            prop.store(output, null);
        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    public static String loadThemePreset() {
        Properties prop = new Properties();
        try (InputStream input = new FileInputStream(CONFIG_FILE)) {
            prop.load(input);
            return prop.getProperty("themePreset", "DARK");
        } catch (IOException ex) {
            return "DARK";
        }
    }
}