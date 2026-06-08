package com.expensemanager.observer;

public enum EventType {
    TRANSACTION_ADDED,
    TRANSACTION_UPDATED,
    TRANSACTION_DELETED,
    BUDGET_CHANGED,
    CATEGORY_ADDED,
    CATEGORY_DELETED,
    DATA_LOADED,
    DATA_CLEARED
}