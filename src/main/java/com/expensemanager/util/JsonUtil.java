package com.expensemanager.util;

import com.expensemanager.entity.*;
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
            .registerTypeAdapter(Transaction.class, new TransactionAdapter()) // 🌟 KHẮC PHỤC: Khôi phục đúng lớp con kế thừa
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
            System.err.println("File JSON bị lỗi, tiến hành reset file rỗng: " + e.getMessage());
            try (Writer writer = new FileWriter(filePath)) {
                gson.toJson(new ArrayList<Transaction>(), writer);
            } catch (IOException ex) {
                throw new DataLoadException("Không thể khôi phục file JSON: " + filePath, ex);
            }
            return new ArrayList<>();
        }
    }

    private static class LocalDateTimeAdapter implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {
        @Override
        public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.toString());
        }
        @Override
        public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return LocalDateTime.parse(json.getAsString());
        }
    }

    // 🌟 ĐÃ THÊM: Bộ Deserialize đa hình, tạo đúng thực thể Income/Expense subclass khi đọc file
    private static class TransactionAdapter implements JsonDeserializer<Transaction> {
        @Override
        public Transaction deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            TransactionType type = TransactionType.valueOf(jsonObject.get("type").getAsString());

            String id = jsonObject.get("id").getAsString();
            double amount = jsonObject.get("amount").getAsDouble();
            Category category = context.deserialize(jsonObject.get("category"), Category.class);
            String note = jsonObject.has("note") && !jsonObject.get("note").isJsonNull() ? jsonObject.get("note").getAsString() : "";
            LocalDateTime dateTime = context.deserialize(jsonObject.get("dateTime"), LocalDateTime.class);

            Transaction tx = (type == TransactionType.INCOME)
                    ? new IncomeTransaction(id, amount, category, note)
                    : new ExpenseTransaction(id, amount, category, note);
            tx.setDateTime(dateTime);
            return tx;
        }
    }
}