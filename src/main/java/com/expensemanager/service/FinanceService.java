package com.expensemanager.service;

import com.expensemanager.database.DatabaseUtil;
import com.expensemanager.entity.Category;
import com.expensemanager.entity.Transaction;
import com.expensemanager.exception.DataLoadException;
import com.expensemanager.observer.EventType;
import com.expensemanager.observer.Subject;
import com.expensemanager.util.JsonUtil;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class FinanceService extends Subject {

    private final List<Transaction> transactionList = Collections.synchronizedList(new ArrayList<>());
    private final Map<String, Category> categoryMap = new HashMap<>();
    private final RecurringTransactionService recurringTransactionService;
    private static final String JSON_FILE_PATH = "transactions.json";

    public FinanceService() {
        this.recurringTransactionService = new RecurringTransactionService(this);
        loadInitialData();
    }

    private void loadInitialData() {
        try {
            List<Category> categories = DatabaseUtil.getAllCategories();
            if (categories != null) {
                for (Category c : categories) {
                    if (c != null) categoryMap.put(c.getId(), c);
                }
            }
        } catch (Exception e) {
            System.err.println("Không thể tải danh mục: " + e.getMessage());
        }

        transactionList.clear();

        try {
            recurringTransactionService.loadRecurringTransactions();
            recurringTransactionService.checkAndGenerateTransactions();
        } catch (Exception e) {
            System.err.println("Không thể tải giao dịch lặp lại: " + e.getMessage());
        }
    }

    public void syncFromDatabase() {
        String userId = SessionManager.getCurrentUserId();
        if (userId == null) return;

        List<Transaction> dbTransactions = DatabaseUtil.getAllTransactions(userId);
        if (dbTransactions != null) {
            synchronized (transactionList) {
                transactionList.clear();
                transactionList.addAll(dbTransactions);
            }
            try {
                JsonUtil.saveToJson(new ArrayList<>(transactionList), JSON_FILE_PATH);
            } catch (DataLoadException e) {
                System.err.println("Lỗi lưu JSON backup: " + e.getMessage());
            }
            notifyObservers(EventType.DATA_LOADED, null);
        }

        recurringTransactionService.loadRecurringTransactions();
    }

    // ========== CATEGORIES ==========
    public void addCategory(Category category) {
        try {
            DatabaseUtil.insertCategory(category);
            categoryMap.put(category.getId(), category);
            notifyObservers(EventType.CATEGORY_ADDED, category);
        } catch (Exception e) {
            System.err.println("Lỗi nghiệp vụ thêm danh mục: " + e.getMessage());
        }
    }

    public void deleteCategory(String categoryId) {
        try {
            DatabaseUtil.deleteCategory(categoryId);
            categoryMap.remove(categoryId);
            notifyObservers(EventType.CATEGORY_DELETED, categoryId);
        } catch (Exception e) {
            System.err.println("Lỗi nghiệp vụ xóa danh mục: " + e.getMessage());
        }
    }

    public void refreshCategories() {
        try {
            List<Category> categories = DatabaseUtil.getAllCategories();
            categoryMap.clear();
            if (categories != null) {
                for (Category c : categories) categoryMap.put(c.getId(), c);
            }
        } catch (Exception e) {
            System.err.println("Lỗi làm mới danh mục: " + e.getMessage());
        }
    }

    // ========== TRANSACTIONS ==========
    public void addTransaction(Transaction transaction) {
        String userId = SessionManager.getCurrentUserId();
        if (userId == null) return;

        try {
            DatabaseUtil.insertTransaction(transaction, userId);
            synchronized (transactionList) {
                transactionList.add(transaction);
            }
            saveToFile();
            notifyObservers(EventType.TRANSACTION_ADDED, transaction);
        } catch (Exception e) {
            System.err.println("Lỗi khi thêm giao dịch: " + e.getMessage());
        }
    }

    public void updateTransaction(Transaction transaction) {
        try {
            DatabaseUtil.updateTransaction(transaction);
            synchronized (transactionList) {
                for (int i = 0; i < transactionList.size(); i++) {
                    if (transactionList.get(i).getId().equals(transaction.getId())) {
                        transactionList.set(i, transaction);
                        break;
                    }
                }
            }
            saveToFile();
            notifyObservers(EventType.TRANSACTION_UPDATED, transaction);
        } catch (Exception e) {
            System.err.println("Lỗi khi cập nhật giao dịch: " + e.getMessage());
        }
    }

    public void deleteTransaction(String transactionId) {
        try {
            DatabaseUtil.deleteTransaction(transactionId);
            synchronized (transactionList) {
                transactionList.removeIf(t -> t.getId().equals(transactionId));
            }
            saveToFile();
            notifyObservers(EventType.TRANSACTION_DELETED, transactionId);
        } catch (Exception e) {
            System.err.println("Lỗi khi xóa giao dịch: " + e.getMessage());
        }
    }

    private void saveToFile() {
        try {
            List<Transaction> snapshot;
            synchronized (transactionList) {
                snapshot = new ArrayList<>(transactionList);
            }
            JsonUtil.saveToJson(snapshot, JSON_FILE_PATH);
        } catch (DataLoadException e) {
            System.err.println("Lỗi lưu JSON: " + e.getMessage());
        }
    }

    public List<Transaction> getAllTransactions() {
        synchronized (transactionList) {
            return new ArrayList<>(transactionList);
        }
    }

    public Category getCategoryById(String id) { return categoryMap.get(id); }

    public List<Category> getAllCategories() {
        return new ArrayList<>(categoryMap.values());
    }

    public LocalDate getEarliestTransactionDate() {
        List<Transaction> snapshot = getAllTransactions();
        if (snapshot.isEmpty()) return null;
        return snapshot.stream()
                .map(t -> t.getDateTime().toLocalDate())
                .min(LocalDate::compareTo)
                .orElse(null);
    }

    public LocalDate getLatestTransactionDate() {
        List<Transaction> snapshot = getAllTransactions();
        if (snapshot.isEmpty()) return null;
        return snapshot.stream()
                .map(t -> t.getDateTime().toLocalDate())
                .max(LocalDate::compareTo)
                .orElse(null);
    }

    public List<Transaction> getTransactionsWithPagination(int offset, int limit) {
        String userId = SessionManager.getCurrentUserId();
        if (userId == null) return new ArrayList<>();
        return DatabaseUtil.getTransactionsWithPagination(userId, offset, limit);
    }

    public int getTransactionCount() {
        String userId = SessionManager.getCurrentUserId();
        if (userId == null) return 0;
        return DatabaseUtil.getTransactionCount(userId);
    }

    public RecurringTransactionService getRecurringTransactionService() {
        return recurringTransactionService;
    }

    public void loadRecurringTransactions() {
        recurringTransactionService.loadRecurringTransactions();
    }
}