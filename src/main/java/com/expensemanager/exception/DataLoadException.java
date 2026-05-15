package com.expensemanager.exception;

public class DataLoadException extends Exception {

    public DataLoadException() {
        super("Lỗi khi tải dữ liệu");
    }

    public DataLoadException(String message) {
        super(message);
    }

    public DataLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}