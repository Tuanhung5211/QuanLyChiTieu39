package com.expensemanager.util;

import com.expensemanager.entity.Transaction;
import com.expensemanager.exception.DataLoadException;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class JsonUtil {
    // Thêm Adapter để xử lý LocalDateTime từ class của Bạn A
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context) ->
                    new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
            .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>) (json, typeOfT, context) ->
                    LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            .setPrettyPrinting()
            .create();

    public static void saveToJson(List<Transaction> transactions, String filePath) throws DataLoadException {
        try (Writer writer = new FileWriter(filePath)) {
            gson.toJson(transactions, writer);
        } catch (IOException e) {
            // Đảm bảo DataLoadException có constructor (String, Throwable)
            throw new DataLoadException("Lỗi khi ghi file JSON: " + filePath, e);
        }
    }

    public static List<Transaction> loadFromJson(String filePath) throws DataLoadException {
        File file = new File(filePath);
        if (!file.exists()) {
            return new ArrayList<>();
        }
        try (Reader reader = new FileReader(filePath)) {
            Type listType = new TypeToken<ArrayList<Transaction>>(){}.getType();
            List<Transaction> result = gson.fromJson(reader, listType);
            // Tránh trả về null để FinanceService không bị lỗi
            return (result != null) ? result : new ArrayList<>();
        } catch (IOException | JsonSyntaxException e) {
            throw new DataLoadException("Lỗi khi đọc file JSON: " + filePath, e);
        }
    }
}