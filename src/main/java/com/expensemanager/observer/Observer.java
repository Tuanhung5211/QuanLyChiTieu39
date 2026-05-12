package com.expensemanager.observer;

public interface Observer {
    void update(EventType eventType, Object data);
}