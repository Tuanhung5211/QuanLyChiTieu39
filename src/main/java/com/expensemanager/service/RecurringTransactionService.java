package com.expensemanager.service;

import com.expensemanager.database.DatabaseUtil;
import com.expensemanager.entity.*;
import com.expensemanager.observer.EventType;
import com.expensemanager.observer.Subject;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Dịch vụ quản lý giao dịch lặp lại (Recurring Transactions).
 * Cung cấp các chức năng tạo, chỉnh sửa, xóa giao dịch lặp lại
 * và tự động sinh ra các giao dịch thực tế từ các mẫu lặp lại.
 */
public class RecurringTransactionService extends Subject {

    private FinanceService financeService;
    private List<RecurringTransaction> recurringTransactions;

    public RecurringTransactionService(FinanceService financeService) {
        this.financeService = financeService;
        this.recurringTransactions = new ArrayList<>();
    }

    /**
     * Tải tất cả các giao dịch lặp lại của người dùng hiện tại
     */
    public void loadRecurringTransactions() {
        String userId = SessionManager.getCurrentUserId();
        if (userId == null) return;

        try {
            recurringTransactions = DatabaseUtil.getRecurringTransactions(userId);
            notifyObservers(EventType.DATA_LOADED, null);
        } catch (Exception e) {
            // In addition to message, print stack trace so developer can see root cause (SQL, conn, etc.)
            System.err.println("Lỗi tải giao dịch lặp lại: " + e.getMessage());
            e.printStackTrace();
            recurringTransactions = new ArrayList<>();
        }
    }

    /**
     * Thêm một giao dịch lặp lại mới
     */
    public void addRecurringTransaction(RecurringTransaction rt) {
        String userId = SessionManager.getCurrentUserId();
        if (userId == null) return;

        try {
            rt.setUserId(userId);
            rt.setCreatedAt(LocalDateTime.now());
            DatabaseUtil.insertRecurringTransaction(rt);
            recurringTransactions.add(rt);
            notifyObservers(EventType.TRANSACTION_ADDED, rt);

            // Check ngay nếu cần tạo giao dịch lần đầu
            checkAndGenerateTransactions();
        } catch (Exception e) {
            System.err.println("Lỗi thêm giao dịch lặp lại: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Cập nhật giao dịch lặp lại
     */
    public void updateRecurringTransaction(RecurringTransaction rt) {
        try {
            DatabaseUtil.updateRecurringTransaction(rt);

            // Cập nhật trong danh sách local
            for (int i = 0; i < recurringTransactions.size(); i++) {
                if (recurringTransactions.get(i).getId().equals(rt.getId())) {
                    recurringTransactions.set(i, rt);
                    break;
                }
            }

            notifyObservers(EventType.TRANSACTION_UPDATED, rt);
        } catch (Exception e) {
            System.err.println("Lỗi cập nhật giao dịch lặp lại: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Xóa giao dịch lặp lại
     */
    public void deleteRecurringTransaction(String id) {
        try {
            DatabaseUtil.deleteRecurringTransaction(id);
            recurringTransactions.removeIf(rt -> rt.getId().equals(id));
            notifyObservers(EventType.TRANSACTION_DELETED, id);
        } catch (Exception e) {
            System.err.println("Lỗi xóa giao dịch lặp lại: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Tắt một giao dịch lặp lại (đặt isActive = false)
     */
    public void deactivateRecurringTransaction(String id) {
        RecurringTransaction rt = findRecurringTransactionById(id);
        if (rt != null) {
            rt.setActive(false);
            updateRecurringTransaction(rt);
        }
    }

    /**
     * Bật một giao dịch lặp lại (đặt isActive = true)
     */
    public void activateRecurringTransaction(String id) {
        RecurringTransaction rt = findRecurringTransactionById(id);
        if (rt != null) {
            rt.setActive(true);
            updateRecurringTransaction(rt);
        }
    }

    /**
     * Tìm giao dịch lặp lại theo ID
     */
    public RecurringTransaction findRecurringTransactionById(String id) {
        return recurringTransactions.stream()
                .filter(rt -> rt.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    /**
     * Lấy danh sách tất cả giao dịch lặp lại
     */
    public List<RecurringTransaction> getAllRecurringTransactions() {
        return new ArrayList<>(recurringTransactions);
    }

    /**
     * Kiểm tra và tự động sinh các giao dịch thực tế từ các mẫu lặp lại
     * Phương thức này nên được gọi định kỳ (hằng ngày hoặc khi ứng dụng khởi động)
     */
    public void checkAndGenerateTransactions() {
        for (RecurringTransaction rt : recurringTransactions) {
            if (rt.shouldGenerateToday()) {
                generateTransactionFromRecurring(rt);
            }
        }
    }

    /**
     * Sinh một giao dịch thực tế từ một mẫu giao dịch lặp lại
     */
    private void generateTransactionFromRecurring(RecurringTransaction rt) {
        try {
            // Tạo giao dịch mới
            String transactionId = UUID.randomUUID().toString().substring(0, 10);
            LocalDateTime dateTime = LocalDateTime.now();

            Transaction transaction;
            if (rt.getType() == TransactionType.INCOME) {
                transaction = new IncomeTransaction(transactionId, rt.getAmount(), rt.getCategory(), rt.getNote());
            } else {
                transaction = new ExpenseTransaction(transactionId, rt.getAmount(), rt.getCategory(), rt.getNote());
            }
            transaction.setDateTime(dateTime);

            // Lưu giao dịch
            String userId = SessionManager.getCurrentUserId();
            DatabaseUtil.insertTransaction(transaction, userId);

            // Cập nhật lastGeneratedDate của recurring transaction
            rt.setLastGeneratedDate(LocalDate.now());
            DatabaseUtil.updateRecurringTransaction(rt);

            // Thông báo FinanceService tải lại dữ liệu
            financeService.syncFromDatabase();

            System.out.println("✓ Sinh giao dịch thực tế từ mẫu lặp lại: " + rt.getId());
        } catch (Exception e) {
            System.err.println("Lỗi sinh giao dịch từ mẫu lặp lại: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Lấy thống kê về giao dịch lặp lại
     */
    public Map<String, Integer> getRecurringTransactionStats() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("TOTAL", recurringTransactions.size());
        stats.put("ACTIVE", (int) recurringTransactions.stream().filter(RecurringTransaction::isActive).count());
        stats.put("INACTIVE", (int) recurringTransactions.stream().filter(rt -> !rt.isActive()).count());

        for (RecurringTransaction.RecurrenceType type : RecurringTransaction.RecurrenceType.values()) {
            long count = recurringTransactions.stream()
                    .filter(rt -> rt.getRecurrenceType() == type && rt.isActive())
                    .count();
            stats.put("TYPE_" + type.name(), (int) count);
        }

        return stats;
    }

    /**
     * Dự tính tổng chi phí hàng tháng từ giao dịch lặp lại
     */
    public double estimateMonthlyExpenses() {
        return recurringTransactions.stream()
                .filter(rt -> rt.isActive() && rt.getType() == TransactionType.EXPENSE)
                .mapToDouble(rt -> estimateMonthlyAmount(rt))
                .sum();
    }

    /**
     * Dự tính tổng thu nhập hàng tháng từ giao dịch lặp lại
     */
    public double estimateMonthlyIncome() {
        return recurringTransactions.stream()
                .filter(rt -> rt.isActive() && rt.getType() == TransactionType.INCOME)
                .mapToDouble(rt -> estimateMonthlyAmount(rt))
                .sum();
    }

    /**
     * Dự tính số lần giao dịch lặp lại sẽ xảy ra trong một tháng
     */
    private double estimateMonthlyAmount(RecurringTransaction rt) {
        switch (rt.getRecurrenceType()) {
            case DAILY:
                return rt.getAmount() * 30;  // ~30 ngày/tháng
            case WEEKLY:
                return rt.getAmount() * 4.3; // ~4.3 tuần/tháng
            case MONTHLY:
                return rt.getAmount();
            case YEARLY:
                return rt.getAmount() / 12;
            case CUSTOM:
                if (rt.getCustomIntervalDays() > 0) {
                    return rt.getAmount() * (30.0 / rt.getCustomIntervalDays());
                }
                return 0;
            default:
                return 0;
        }
    }

    /**
     * Lấy mô tả loại lặp lại bằng tiếng Việt
     */
    public static String getRecurrenceTypeLabel(RecurringTransaction.RecurrenceType type, boolean isVietnamese) {
        if (isVietnamese) {
            switch (type) {
                case DAILY: return "Hàng ngày";
                case WEEKLY: return "Hàng tuần";
                case MONTHLY: return "Hàng tháng";
                case YEARLY: return "Hàng năm";
                case CUSTOM: return "Tùy chỉnh";
                default: return "Không xác định";
            }
        } else {
            switch (type) {
                case DAILY: return "Daily";
                case WEEKLY: return "Weekly";
                case MONTHLY: return "Monthly";
                case YEARLY: return "Yearly";
                case CUSTOM: return "Custom";
                default: return "Unknown";
            }
        }
    }
}

