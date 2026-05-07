package com.expensemanager.exception;

/**
 * Ngoại lệ ném ra khi có lỗi đọc/ghi dữ liệu từ file.
 */
public class DataLoadException extends Exception {
    public DataLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}