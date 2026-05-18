package com.expensemanager.service;

import com.expensemanager.database.DatabaseUtil;
import com.expensemanager.entity.Category;
import com.expensemanager.entity.Transaction;
import com.expensemanager.entity.TransactionType;
import com.expensemanager.exception.DataLoadException;
import com.expensemanager.observer.EventType;
import com.expensemanager.observer.Subject;
import com.expensemanager.util.JsonUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FinanceService extends Subject {
    private List<Transaction> transactionList;
    private Map<String, Category> categoryMap;
    private static final String JSON_FILE_PATH = "transactions.json";

    public FinanceService() {
        transactionList = new ArrayList<>();
        categoryMap = new HashMap<>();
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

        try {
            List<Transaction> saved = JsonUtil.loadFromJson(JSON_FILE_PATH);
            transactionList = saved != null ? saved : new ArrayList<>();
        } catch (DataLoadException e) {
            System.err.println("Không thể tải file JSON: " + e.getMessage());
            transactionList = new ArrayList<>();
        }
    }

    public void syncFromDatabase() {
        String userId = SessionManager.getCurrentUserId();
        if (userId == null) return;
        List<Transaction> dbTransactions = DatabaseUtil.getAllTransactions(userId);
        if (dbTransactions != null) {
            this.transactionList = dbTransactions;
            saveToFile();
            notifyObservers(EventType.DATA_LOADED, null);
        }
    }

    // ====================================================================
    // 🌟 QUẢN LÝ QUY TRÌNH CATEGORIES TRUNG GIAN (ĐÃ KHẮC PHỤC)
    // ====================================================================
    public void addCategory(Category category) {
        try {
            DatabaseUtil.insertCategory(category);
            categoryMap.put(category.getId(), category); // Cập nhật bộ nhớ RAM cache ngay lập tức
            notifyObservers(EventType.CATEGORY_ADDED, category);
        } catch (Exception e) {
            System.err.println("Lỗi nghiệp vụ thêm danh mục: " + e.getMessage());
        }
    }

    public void deleteCategory(String categoryId) {
        try {
            DatabaseUtil.deleteCategory(categoryId);
            categoryMap.remove(categoryId); // Xóa RAM cache
            notifyObservers(EventType.CATEGORY_DELETED, categoryId);
        } catch (Exception e) {
            System.err.println("Lỗi nghiệp vụ xóa danh mục: " + e.getMessage());
        }
    }

    // ====================================================================
    // 🌟 QUẢN LÝ QUY TRÌNH TRANSACTIONS ĐỒNG BỘ
    // ====================================================================
    public void addTransaction(Transaction transaction) {
        String userId = SessionManager.getCurrentUserId();
        if (userId == null) return;
        try {
            DatabaseUtil.insertTransaction(transaction, userId);
            transactionList.add(transaction);
            saveToFile();
            notifyObservers(EventType.TRANSACTION_ADDED, transaction);
        } catch (Exception e) {
            System.err.println("Lỗi khi thêm giao dịch: " + e.getMessage());
        }
    }

    public void updateTransaction(Transaction transaction) {
        try {
            DatabaseUtil.updateTransaction(transaction);

            // 🌟 ĐÃ KHẮC PHỤC BUG: Tìm và đồng bộ hóa bản ghi cập nhật ngay trên RAM danh sách tĩnh
            for (int i = 0; i < transactionList.size(); i++) {
                if (transactionList.get(i).getId().equals(transaction.getId())) {
                    transactionList.set(i, transaction);
                    break;
                }
            }
            saveToFile(); // Ghi đè file JSON cục bộ bảo toàn dữ liệu đồng nhất
            notifyObservers(EventType.TRANSACTION_UPDATED, transaction);
        } catch (Exception e) {
            System.err.println("Lỗi khi cập nhật giao dịch: " + e.getMessage());
        }
    }

    public void deleteTransaction(String transactionId) {
        try {
            DatabaseUtil.deleteTransaction(transactionId);
            transactionList.removeIf(t -> t.getId().equals(transactionId));
            saveToFile();
            notifyObservers(EventType.TRANSACTION_DELETED, transactionId);
        } catch (Exception e) {
            System.err.println("Lỗi khi xóa giao dịch: " + e.getMessage());
        }
    }

    public List<Transaction> getAllTransactions() { return transactionList; }
    public Category getCategoryById(String id) { return categoryMap.get(id); }
    public List<Category> getAllCategories() { return new ArrayList<>(categoryMap.values()); }

    public void refreshCategories() {
        try {
            List<Category> categories = DatabaseUtil.getAllCategories();
            categoryMap.clear();
            if (categories != null) for (Category c : categories) categoryMap.put(c.getId(), c);
        } catch (Exception e) {
            System.err.println("Lỗi làm mới danh mục: " + e.getMessage());
        }
    }

    private void saveToFile() {
        try { JsonUtil.saveToJson(transactionList, JSON_FILE_PATH); }
        catch (DataLoadException e) { System.err.println("Lỗi lưu JSON: " + e.getMessage()); }
    }

    public java.time.LocalDate getEarliestTransactionDate() {
        if (transactionList == null || transactionList.isEmpty()) return null;
        return transactionList.stream().map(t -> t.getDateTime().toLocalDate()).min(java.time.LocalDate::compareTo).orElse(null);
    }

    public java.time.LocalDate getLatestTransactionDate() {
        if (transactionList == null || transactionList.isEmpty()) return null;
        return transactionList.stream().map(t -> t.getDateTime().toLocalDate()).max(java.time.LocalDate::compareTo).orElse(null);
    }
}