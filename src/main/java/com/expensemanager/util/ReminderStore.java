package com.expensemanager.util;

import com.expensemanager.entity.Reminder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ReminderStore {
    private static final String REMINDERS_DIR = "reminders/";
    private static final Gson gson = GsonUtil.getGson();

    public static void saveReminders(String userId, List<Reminder> reminders) {
        ensureDir();
        try (Writer writer = new FileWriter(REMINDERS_DIR + userId + ".json")) {
            gson.toJson(reminders, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<Reminder> loadReminders(String userId) {
        ensureDir();
        File file = new File(REMINDERS_DIR + userId + ".json");
        if (!file.exists()) return new ArrayList<>();
        try (Reader reader = new FileReader(file)) {
            Type type = new TypeToken<ArrayList<Reminder>>(){}.getType();
            List<Reminder> list = gson.fromJson(reader, type);
            return list != null ? list : new ArrayList<>();
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    private static void ensureDir() {
        File dir = new File(REMINDERS_DIR);
        if (!dir.exists()) dir.mkdirs();
    }
}