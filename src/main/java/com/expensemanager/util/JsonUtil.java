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
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    // Sửa thành 2 tham số: danh sách và đường dẫn file
    public static void saveToJson(List<Transaction> transactions, String filePath) throws DataLoadException {
        try (Writer writer = new FileWriter(filePath)) {
            gson.toJson(transactions, writer);
        } catch (IOException e) {
            throw new DataLoadException("Không thể ghi file JSON: " + e.getMessage());
        }
    }

    // Sửa thành 1 tham số: đường dẫn file
    public static List<Transaction> loadFromJson(String filePath) throws DataLoadException {
        File file = new File(filePath);
        if (!file.exists()) return new ArrayList<>();

        try (Reader reader = new FileReader(filePath)) {
            Type listType = new TypeToken<ArrayList<Transaction>>() {}.getType();
            List<Transaction> data = gson.fromJson(reader, listType);
            return (data != null) ? data : new ArrayList<>();
        } catch (IOException | com.google.gson.JsonSyntaxException e) {
            throw new DataLoadException("Lỗi đọc file JSON: " + e.getMessage());
        }
    }
}