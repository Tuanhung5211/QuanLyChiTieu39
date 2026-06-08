package com.expensemanager.exception;

public class InvalidAmountException extends Exception {

    public InvalidAmountException() {
        super("Số tiền phải lớn hơn 0");
    }

    public InvalidAmountException(String message) {
        super(message);
    }
} //hàm kiểm tra va đưa ra lỗi nếu số tiền nho hơn 0