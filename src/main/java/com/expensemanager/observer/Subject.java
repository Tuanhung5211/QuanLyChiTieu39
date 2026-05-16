package com.expensemanager.observer;

import java.util.ArrayList;
import java.util.List;

public abstract class Subject {
    private List<Observer> observers = new ArrayList<>();

    public void attach(Observer observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void detach(Observer observer) {
        observers.remove(observer);
    }

    protected void notifyObservers(EventType eventType, Object data) {
        for (Observer observer : observers) {
            observer.update(eventType, data);
        }
    }
}