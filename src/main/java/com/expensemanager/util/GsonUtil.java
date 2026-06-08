package com.expensemanager.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class GsonUtil {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_TIME;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(LocalDate.class, new TypeAdapter<LocalDate>() {
                @Override public void write(JsonWriter out, LocalDate value) throws IOException {
                    out.value(value != null ? value.format(DATE_FORMATTER) : null);
                }
                @Override public LocalDate read(JsonReader in) throws IOException {
                    String s = in.nextString();
                    return s != null ? LocalDate.parse(s, DATE_FORMATTER) : null;
                }
            })
            .registerTypeAdapter(LocalTime.class, new TypeAdapter<LocalTime>() {
                @Override public void write(JsonWriter out, LocalTime value) throws IOException {
                    out.value(value != null ? value.format(TIME_FORMATTER) : null);
                }
                @Override public LocalTime read(JsonReader in) throws IOException {
                    String s = in.nextString();
                    return s != null ? LocalTime.parse(s, TIME_FORMATTER) : null;
                }
            })
            .registerTypeAdapter(LocalDateTime.class, new TypeAdapter<LocalDateTime>() {
                @Override public void write(JsonWriter out, LocalDateTime value) throws IOException {
                    out.value(value != null ? value.format(DATE_TIME_FORMATTER) : null);
                }
                @Override public LocalDateTime read(JsonReader in) throws IOException {
                    String s = in.nextString();
                    return s != null ? LocalDateTime.parse(s, DATE_TIME_FORMATTER) : null;
                }
            })
            .create();

    public static Gson getGson() {
        return GSON;
    }
}