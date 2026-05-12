package com.expensemanager.service;

import com.expensemanager.database.DatabaseUtil;
import com.expensemanager.entity.Category;
import com.expensemanager.entity.Transaction;
import com.expensemanager.entity.TransactionType;
import com.expensemanager.exception.DataLoadException;
import com.expensemanager.exception.InvalidAmountException;
import com.expensemanager.util.JsonUtil;
import com.expensemanager.observer.*;  // ✅ THÊM MỚI: Import observer package

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;  // ✅ THÊM MỚI: Cho thread-safe
import java.util.stream.Collectors;

// ✅ THÊM MỚI: Implement Subject interface
public class FinanceService implements Subject {

    private List<Transaction> transactionList;
    private Map<String, Category> categoryMap;
    private static final String JSON_FILE_PATH = "src/main/resources/data/transactions.json";

    // ✅ THÊM MỚI: Danh sách các observer (CopyOnWriteArrayList để tránh ConcurrentModificationException)
    private List<Observer> observers;

    public FinanceService() {
        this.transactionList = new ArrayList<>();
        this.categoryMap = new HashMap<>();
        this.observers = new CopyOnWriteArrayList<>();  // ✅ THÊM MỚI: Khởi tạo observers
        // Việc nạp dữ liệu ban đầu thường được gọi từ MainApp hoặc MainFrame
        // để có thể bắt lỗi và hiển thị Dialog thông báo nếu cần.
    }

    // ========== ✅ THÊM MỚI: CÁC PHƯƠNG THỨC CỦA SUBJECT ==========

    /**
     * Đăng ký một observer để nhận thông báo khi dữ liệu thay đổi
     * @param observer Component muốn theo dõi thay đổi (Dashboard, History, Statistics...)
     */
    @Override
    public void attach(Observer observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
            System.out.println("✅ [FinanceService] Đã đăng ký: " + observer.getClass().getSimpleName());
        }
    }

    /**
     * Hủy đăng ký một observer (khi component bị đóng)
     * @param observer Component không cần nhận thông báo nữa
     */
    @Override
    public void detach(Observer observer) {
        observers.remove(observer);
        System.out.println("❌ [FinanceService] Đã hủy đăng ký: " + observer.getClass().getSimpleName());
    }

    /**
     * Thông báo cho tất cả observer về sự thay đổi
     * @param eventType Loại sự kiện (THÊM, SỬA, XÓA...)
     * @param data Dữ liệu kèm theo (transaction, budget alert...)
     */
    @Override
    public void notifyObservers(EventType eventType, Object data) {
        // Duyệt qua từng observer và gọi phương thức update()
        for (Observer observer : observers) {
            try {
                observer.update(eventType, data);
            } catch (Exception e) {
                // Bắt lỗi từng observer để không ảnh hưởng đến observer khác
                System.err.println("⚠️ Lỗi khi thông báo cho " + observer.getClass().getSimpleName() + ": " + e.getMessage());
            }
        }
    }

    // ========== KẾT THÚC PHẦN THÊM MỚI ==========

    /**
     * Nạp dữ liệu ban đầu từ Database và JSON.
     * @throws DataLoadException nếu có lỗi nghiêm trọng khi đọc dữ liệu.
     */
    public void loadInitialData() throws DataLoadException {
        // 1. Tải danh mục từ database
        try {
            List<Category> categories = DatabaseUtil.getAllCategories();
            categoryMap.clear();
            if (categories != null) {
                for (Category c : categories) {
                    if (c != null) categoryMap.put(c.getId(), c);
                }
            }
        } catch (Exception e) {
            // Log lỗi nhưng không chặn tiến trình nếu đây là lỗi DB không nghiêm trọng
            System.err.println("Cảnh báo: Không thể tải danh mục từ database.");
        }

        // 2. Tải giao dịch từ file JSON
        try {
            List<Transaction> savedTransactions = JsonUtil.loadFromJson(JSON_FILE_PATH);
            this.transactionList = (savedTransactions != null) ? savedTransactions : new ArrayList<>();

            // ✅ THÊM MỚI: Thông báo dữ liệu đã được load xong
            notifyObservers(EventType.DATA_LOADED, null);

        } catch (DataLoadException e) {
            // Ném lại ngoại lệ để UI xử lý (ví dụ: yêu cầu người dùng kiểm tra file)
            throw new DataLoadException("Lỗi nạp lịch sử giao dịch: " + e.getMessage());
        }
    }

    // ========== CÁC PHƯƠNG THỨC THAO TÁC GIAO DỊCH ==========

    /**
     * Thêm giao dịch với kiểm tra logic dữ liệu (Validation).
     */
    public void addTransaction(Transaction transaction) throws InvalidAmountException, DataLoadException {
        // Kiểm tra dữ liệu đầu vào (Xử lý dữ liệu)
        if (transaction == null) {
            throw new IllegalArgumentException("Giao dịch không được để trống.");
        }

        if (transaction.getAmount() <= 0) {
            throw new InvalidAmountException("Số tiền phải lớn hơn 0. Giá trị nhập: " + transaction.getAmount());
        }

        // Thao tác Database (I/O)
        try {
            DatabaseUtil.insertTransaction(transaction);
        } catch (Exception e) {
            // Nếu lỗi DB, ta có thể chọn ghi log và vẫn cho phép lưu vào JSON để tránh mất dữ liệu tạm thời
            System.err.println("Lỗi Database: " + e.getMessage());
        }

        transactionList.add(transaction);

        // Thao tác File (I/O)
        saveToFile();

        // ✅ THÊM MỚI: Thông báo cho tất cả observer biết có giao dịch mới
        notifyObservers(EventType.TRANSACTION_ADDED, transaction);

        // ✅ THÊM MỚI: Kiểm tra ngân sách nếu là chi tiêu
        if (transaction.getType() == TransactionType.EXPENSE) {
            checkBudgetAfterExpense(transaction);
        }
    }

    // ✅ THÊM MỚI: PHƯƠNG THỨC SỬA GIAO DỊCH
    /**
     * Cập nhật thông tin giao dịch
     * @param oldTransaction Giao dịch cũ
     * @param newTransaction Giao dịch mới
     */
    public void updateTransaction(Transaction oldTransaction, Transaction newTransaction)
            throws InvalidAmountException, DataLoadException {

        if (newTransaction.getAmount() <= 0) {
            throw new InvalidAmountException("Số tiền phải lớn hơn 0. Giá trị nhập: " + newTransaction.getAmount());
        }

        int index = transactionList.indexOf(oldTransaction);
        if (index != -1) {
            transactionList.set(index, newTransaction);

            // Cập nhật database
            try {
                DatabaseUtil.updateTransaction(newTransaction);
            } catch (Exception e) {
                System.err.println("Lỗi cập nhật Database: " + e.getMessage());
            }

            saveToFile();

            // ✅ THÊM MỚI: Thông báo giao dịch đã được sửa
            notifyObservers(EventType.TRANSACTION_UPDATED, newTransaction);
        }
    }

    // ✅ THÊM MỚI: PHƯƠNG THỨC XÓA GIAO DỊCH
    /**
     * Xóa một giao dịch
     * @param transaction Giao dịch cần xóa
     */
    public void deleteTransaction(Transaction transaction) throws DataLoadException {
        transactionList.remove(transaction);

        // Xóa khỏi database
        try {
            DatabaseUtil.deleteTransaction(transaction.getId());
        } catch (Exception e) {
            System.err.println("Lỗi xóa Database: " + e.getMessage());
        }

        saveToFile();

        // ✅ THÊM MỚI: Thông báo giao dịch đã bị xóa
        notifyObservers(EventType.TRANSACTION_DELETED, transaction);
    }

    // ✅ THÊM MỚI: KIỂM TRA NGÂN SÁCH
    private void checkBudgetAfterExpense(Transaction expense) {
        // Giả sử bạn có một phương thức để lấy ngân sách hiện tại
        // Bạn có thể tạo thêm class BudgetManager hoặc thêm vào đây tạm thời
        double currentMonthExpense = getCurrentMonthExpense();
        double budgetLimit = getBudgetLimit(); // Bạn cần implement phương thức này

        if (budgetLimit > 0 && currentMonthExpense > budgetLimit) {
            // Tạo đối tượng alert và thông báo
            BudgetAlert alert = new BudgetAlert(budgetLimit, currentMonthExpense);
            notifyObservers(EventType.BUDGET_CHANGED, alert);
        }
    }

    // ✅ THÊM MỚI: Helper methods cho budget
    private double getCurrentMonthExpense() {
        // Lấy tháng hiện tại (yyyy-MM)
        java.time.YearMonth now = java.time.YearMonth.now();
        String currentMonth = now.toString();

        return transactionList.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .filter(t -> {
                    java.time.LocalDateTime dateTime = t.getDateTime();
                    String transactionMonth = dateTime.toString().substring(0, 7);
                    return transactionMonth.equals(currentMonth);
                })
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    private double getBudgetLimit() {
        // Bạn cần implement phương thức này
        // Có thể lấy từ Database hoặc từ một BudgetManager riêng
        // Tạm thời trả về 0 (không giới hạn)
        return 0;
    }

    // ✅ THÊM MỚI: INNER CLASS CHO BUDGET ALERT
    public static class BudgetAlert {
        private double limit;
        private double currentSpent;

        public BudgetAlert(double limit, double currentSpent) {
            this.limit = limit;
            this.currentSpent = currentSpent;
        }

        public double getLimit() { return limit; }
        public double getCurrentSpent() { return currentSpent; }
        public double getExcess() { return currentSpent - limit; }
        public double getPercentage() {
            return (currentSpent / limit) * 100;
        }
    }

    // ========== CÁC PHƯƠNG THỨC TÍNH TOÁN (Stream API) ==========

    public double getTotalIncome() {
        return transactionList.stream()
                .filter(t -> t != null && t.getType() == TransactionType.INCOME)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    public double getTotalExpense() {
        return transactionList.stream()
                .filter(t -> t != null && t.getType() == TransactionType.EXPENSE)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    public double getBalance() {
        // Sử dụng LaTeX cho công thức tính toán đơn giản
        // $$Balance = \sum Income - \sum Expense$$
        return getTotalIncome() - getTotalExpense();
    }

    // ✅ THÊM MỚI: Lấy giao dịch theo ID (hữu ích cho update/delete)
    public Transaction getTransactionById(int id) {
        return transactionList.stream()
                .filter(t -> t.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    // ========== HỖ TRỢ LƯU TRỮ VÀ ĐỒNG BỘ ==========

    /**
     * Lưu danh sách vào file JSON.
     * Bọc ngoại lệ I/O để đảm bảo tính bền vững của dữ liệu.
     */
    private void saveToFile() throws DataLoadException {
        try {
            JsonUtil.saveToJson(transactionList, JSON_FILE_PATH);
        } catch (DataLoadException e) {
            throw new DataLoadException("Không thể sao lưu dữ liệu vào JSON: " + e.getMessage());
        }
    }

    public void syncFromDatabase() throws DataLoadException {
        try {
            List<Transaction> dbTransactions = DatabaseUtil.getAllTransactions();
            if (dbTransactions != null) {
                this.transactionList = new ArrayList<>(dbTransactions);
                saveToFile();

                // ✅ THÊM MỚI: Thông báo đồng bộ thành công
                notifyObservers(EventType.DATA_LOADED, null);
            }
        } catch (Exception e) {
            throw new DataLoadException("Lỗi đồng bộ dữ liệu từ Database: " + e.getMessage());
        }
    }

    // Các getter khác...
    public List<Transaction> getAllTransactions() {
        return new ArrayList<>(transactionList);
    }

    // ✅ THÊM MỚI: Getter cho categoryMap (nếu cần)
    public Map<String, Category> getCategoryMap() {
        return new HashMap<>(categoryMap);
    }
}