package com.expensemanager.observer;

public interface Subject {
    void attach(Observer observer);      // Đăng ký theo dõi
    void detach(Observer observer);      // Hủy đăng ký
    void notifyObservers(EventType eventType, Object data); // Thông báo
}