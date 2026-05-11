package com.expensemanager.util;

import com.expensemanager.entity.Transaction;
import com.expensemanager.exception.DataLoadException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class JsonUtil {
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()  // In JSON đẹp, dễ đọc
            .create();

    /**
     * Lưu danh sách giao dịch vào file JSON.
     * @param transactions danh sách giao dịch cần lưu
     * @param filePath đường dẫn file (vd: "transactions.json")
     */
    public static void saveToJson(List<Transaction> transactions, String filePath)
            throws DataLoadException {
        try (Writer writer = new FileWriter(filePath)) {
            gson.toJson(transactions, writer);
        } catch (IOException e) {
            throw new DataLoadException("Lỗi khi ghi file JSON: " + filePath, e);
        }
    }

    /**
     * Tải danh sách giao dịch từ file JSON.
     * @param filePath đường dẫn file
     * @return danh sách giao dịch, hoặc danh sách rỗng nếu file không tồn tại
     */
    public static List<Transaction> loadFromJson(String filePath)
            throws DataLoadException {
        File file = new File(filePath);
        if (!file.exists()) {
            return new ArrayList<>(); // Trả về list rỗng nếu file chưa có
        }
        try (Reader reader = new FileReader(filePath)) {
            Type listType = new TypeToken<ArrayList<Transaction>>(){}.getType();
            return gson.fromJson(reader, listType);
        } catch (IOException e) {
            throw new DataLoadException("Lỗi khi đọc file JSON: " + filePath, e);
        }
    }
}