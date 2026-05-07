package com.expensemanager.entity;

public class ExpenseTransaction extends Transaction {
    public ExpenseTransaction(String id, double amount, Category category, String note) {
        super(id, amount, TransactionType.EXPENSE, category, note);
    }

    @Override
    public String toString() {
        return "[CHI] " + super.toString();
    }
}