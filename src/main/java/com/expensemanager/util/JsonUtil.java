package com.expensemanager.util;

import com.expensemanager.entity.Transaction;
import com.expensemanager.exception.DataLoadException;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class JsonUtil {
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();

    public static void saveToJson(List<Transaction> transactions, String filePath) throws DataLoadException {
        try (Writer writer = new FileWriter(filePath)) {
            gson.toJson(transactions, writer);
        } catch (IOException e) {
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
            List<Transaction> list = gson.fromJson(reader, listType);
            return list != null ? list : new ArrayList<>();
        } catch (JsonSyntaxException | IOException e) {
            // File bị hỏng hoặc không đúng định dạng → reset thành mảng rỗng
            System.err.println("File JSON bị lỗi, sẽ tạo file mới: " + e.getMessage());
            try {
                // Ghi đè file bằng mảng rỗng
                try (Writer writer = new FileWriter(filePath)) {
                    gson.toJson(new ArrayList<Transaction>(), writer);
                }
            } catch (IOException ex) {
                throw new DataLoadException("Không thể khôi phục file JSON: " + filePath, ex);
            }
            return new ArrayList<>();
        }
    }

    // Adapter cho LocalDateTime
    private static class LocalDateTimeAdapter implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {
        @Override
        public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.toString());
        }

        @Override
        public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            return LocalDateTime.parse(json.getAsString());
        }
    }
}