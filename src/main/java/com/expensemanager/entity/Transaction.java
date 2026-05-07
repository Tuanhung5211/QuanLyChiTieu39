package com.expensemanager.entity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Transaction {
    protected String id;
    protected double amount;
    protected TransactionType type;
    protected Category category;
    protected LocalDateTime dateTime;
    protected String note;

    public Transaction(String id, double amount, TransactionType type, Category category, String note) {
        this.id = id;
        this.amount = amount;
        this.type = type;
        this.category = category;
        this.dateTime = LocalDateTime.now();
        this.note = note;
    }

    public String getId() { return id; }
    public double getAmount() { return amount; }
    public TransactionType getType() { return type; }
    public Category getCategory() { return category; }
    public LocalDateTime getDateTime() { return dateTime; }
    public String getNote() { return note; }

    public void setAmount(double amount) { this.amount = amount; }
    public void setNote(String note) { this.note = note; }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return String.format("[%s] %s: %,.0f VND - %s (%s)",
                dateTime.format(formatter), type, amount, note, category.getName());
    }
}