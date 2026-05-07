package com.expensemanager.exception;

/**
 * Ngoại lệ ném ra khi số tiền nhập vào không hợp lệ (<= 0).
 */
public class InvalidAmountException extends Exception {
    public InvalidAmountException(String message) {
        super(message);
    }
}