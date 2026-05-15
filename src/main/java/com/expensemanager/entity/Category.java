package com.expensemanager.entity;

public class Category {
    private String id;
    private String name;
    private TransactionType type;

    public Category(String id, String name, TransactionType type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public TransactionType getType() { return type; }
    public void setName(String name) { this.name = name; }

    @Override
    public String toString() {
        return this.name;
    }
}