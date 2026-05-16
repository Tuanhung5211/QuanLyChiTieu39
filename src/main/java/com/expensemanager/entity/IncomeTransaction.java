package com.expensemanager.entity;

public class IncomeTransaction extends Transaction {
    public IncomeTransaction(String id, double amount, Category category, String note) {
        super(id, amount, TransactionType.INCOME, category, note);
    }

    @Override
    public String toString() {
        return "[THU] " + super.toString();
    }
}