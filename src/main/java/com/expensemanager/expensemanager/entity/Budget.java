package com.expensemanager.entity;

public class Budget {
    private String id;
    private int month;
    private int year;
    private double limit;    // hạn mức
    private double spent;    // đã chi

    public Budget(String id, int month, int year, double limit) {
        this.id = id;
        this.month = month;
        this.year = year;
        this.limit = limit;
        this.spent = 0;
    }

    public String getId() { return id; }
    public int getMonth() { return month; }
    public int getYear() { return year; }
    public double getLimit() { return limit; }
    public double getSpent() { return spent; }

    public void setLimit(double limit) { this.limit = limit; }
    public void setSpent(double spent) { this.spent = spent; }

    public void addSpent(double amount) { this.spent += amount; }

    public boolean isOverBudget() { return spent > limit; }

    public double getRemaining() { return limit - spent; }

    @Override
    public String toString() {
        return String.format("Ngân sách %d/%d: %,.0f / %,.0f VND (Còn: %,.0f)",
                month, year, spent, limit, getRemaining());
    }
}