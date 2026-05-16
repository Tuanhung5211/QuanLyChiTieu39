package com.expensemanager.exception;

public class InvalidAmountException extends Exception {

    public InvalidAmountException() {
        super("Số tiền phải lớn hơn 0");
    }

    public InvalidAmountException(String message) {
        super(message);
    }
}