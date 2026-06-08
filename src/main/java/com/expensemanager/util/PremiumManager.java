package com.expensemanager.util;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.io.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class PremiumManager {
    private static final String PREMIUM_FILE = "premium.json";
    private static Map<String, PremiumInfo> premiumUsers = new HashMap<>();
    private static Gson gson = GsonUtil.getGson();

    static {
        load();
    }

    private static void load() {
        File f = new File(PREMIUM_FILE);
        if (!f.exists()) {
            premiumUsers = new HashMap<>();
            return;
        }
        try (Reader r = new FileReader(f)) {
            PremiumInfo[] arr = gson.fromJson(r, PremiumInfo[].class);
            premiumUsers.clear();
            for (PremiumInfo info : arr) {
                premiumUsers.put(info.userId, info);
            }
        } catch (IOException | JsonSyntaxException e) {
            // File bị hỏng hoặc rỗng → reset lại file
            System.err.println("File premium.json bị lỗi, sẽ tạo lại: " + e.getMessage());
            premiumUsers = new HashMap<>();
            // Xóa file cũ để tránh lỗi lần sau
            if (f.exists()) f.delete();
            save(); // Tạo file mới rỗng
        }
    }

    private static void save() {
        try (Writer w = new FileWriter(PREMIUM_FILE)) {
            gson.toJson(premiumUsers.values().toArray(), w);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isPremium(String userId) {
        if (userId == null) return false;
        PremiumInfo info = premiumUsers.get(userId);
        if (info == null) return false;
        if (info.expiryDate != null && info.expiryDate.isBefore(LocalDate.now())) {
            premiumUsers.remove(userId);
            save();
            return false;
        }
        return true;
    }

    public static void activatePremium(String userId, int days) {
        PremiumInfo info = premiumUsers.get(userId);
        if (info == null) {
            info = new PremiumInfo();
            info.userId = userId;
        }
        info.expiryDate = LocalDate.now().plusDays(days);
        premiumUsers.put(userId, info);
        save();
    }

    static class PremiumInfo {
        String userId;
        LocalDate expiryDate;
    }
}