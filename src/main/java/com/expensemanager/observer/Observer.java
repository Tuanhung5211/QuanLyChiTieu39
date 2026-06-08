package com.expensemanager.observer;

public interface Observer {
    void update(EventType eventType, Object data);
}
//định nghĩa hành vi cập nhật khi có sự kiện xảy ra