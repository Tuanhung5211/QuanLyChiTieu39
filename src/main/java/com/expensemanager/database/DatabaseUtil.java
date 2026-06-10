package com.expensemanager.database;

import com.expensemanager.entity.*;
import com.expensemanager.service.UserService;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DatabaseUtil {

    // ✅ Dùng pool từ DatabaseConfig
    public static Connection getConnection() throws SQLException {
        return DatabaseConfig.getConnection();
    }

    static {
        try {
            ensureUserTableColumns();
            createDefaultAdminIfNotExists();
            ensureRecurringTransactionsTableExists();
        } catch (Exception e) {
            System.err.println("Khởi tạo database không thành công: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void ensureUserTableColumns() {
        try (Connection conn = getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            boolean hasPremium = false, hasAdmin = false;

            try (ResultSet rs = meta.getColumns(null, null, "users", "premium_expiry_date")) {
                if (rs.next()) hasPremium = true;
            }
            try (ResultSet rs = meta.getColumns(null, null, "users", "is_admin")) {
                if (rs.next()) hasAdmin = true;
            }

            if (!hasPremium) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("ALTER TABLE users ADD COLUMN premium_expiry_date DATE DEFAULT NULL");
                }
            }
            if (!hasAdmin) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("ALTER TABLE users ADD COLUMN is_admin BOOLEAN DEFAULT FALSE");
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật cấu trúc bảng users: " + e.getMessage());
        }
    }

    // ========== CATEGORIES ==========
    public static void insertCategory(Category category) {
        String sql = "INSERT INTO categories (id, name, type) VALUES (?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, category.getId());
            stmt.setString(2, category.getName());
            stmt.setString(3, category.getType().name());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi thêm danh mục: " + category.getName(), e);
        }
    }

    public static List<Category> getAllCategories() {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT * FROM categories";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String id = rs.getString("id");
                String name = rs.getString("name");
                TransactionType type = TransactionType.valueOf(rs.getString("type"));
                list.add(new Category(id, name, type));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi nạp danh sách danh mục từ Database", e);
        }
        return list;
    }

    public static void deleteCategory(String id) {
        String sql = "DELETE FROM categories WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi xóa danh mục ID: " + id, e);
        }
    }

    // ========== TRANSACTIONS ==========
    public static void insertTransaction(Transaction transaction, String userId) {
        String sql = "INSERT INTO transactions (id, amount, type, category_id, date_time, note, user_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, transaction.getId());
            stmt.setDouble(2, transaction.getAmount());
            stmt.setString(3, transaction.getType().name());
            stmt.setString(4, transaction.getCategory().getId());
            stmt.setTimestamp(5, Timestamp.valueOf(transaction.getDateTime()));
            stmt.setString(6, transaction.getNote());
            stmt.setString(7, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi tạo mới bản ghi giao dịch", e);
        }
    }

    private static List<Transaction> mapTransactions(ResultSet rs) throws SQLException {
        List<Transaction> list = new ArrayList<>();
        while (rs.next()) {
            String id = rs.getString("id");
            double amount = rs.getDouble("amount");
            TransactionType type = TransactionType.valueOf(rs.getString("type"));
            String categoryId = rs.getString("category_id");
            String categoryName = rs.getString("category_name");
            TransactionType categoryType = TransactionType.valueOf(rs.getString("category_type"));
            Category category = new Category(categoryId, categoryName, categoryType);
            LocalDateTime dateTime = rs.getTimestamp("date_time").toLocalDateTime();
            String note = rs.getString("note");

            Transaction transaction = (type == TransactionType.INCOME)
                    ? new IncomeTransaction(id, amount, category, note)
                    : new ExpenseTransaction(id, amount, category, note);
            transaction.setDateTime(dateTime);
            list.add(transaction);
        }
        return list;
    }

    public static List<Transaction> getAllTransactions(String userId) {
        String sql = "SELECT t.*, c.name as category_name, c.type as category_type " +
                "FROM transactions t JOIN categories c ON t.category_id = c.id " +
                "WHERE t.user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                return mapTransactions(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi tải lịch sử giao dịch toàn bộ của User: " + userId, e);
        }
    }

    public static List<Transaction> getTransactionsWithPagination(String userId, int offset, int limit) {
        String sql = "SELECT t.*, c.name as category_name, c.type as category_type " +
                "FROM transactions t JOIN categories c ON t.category_id = c.id " +
                "WHERE t.user_id = ? ORDER BY t.date_time DESC LIMIT ? OFFSET ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            stmt.setInt(2, limit);
            stmt.setInt(3, offset);
            try (ResultSet rs = stmt.executeQuery()) {
                return mapTransactions(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi phân trang lịch sử giao dịch của User: " + userId, e);
        }
    }

    public static int getTransactionCount(String userId) {
        String sql = "SELECT COUNT(*) FROM transactions WHERE user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi đếm tổng số bản ghi giao dịch", e);
        }
        return 0;
    }

    public static void updateTransaction(Transaction transaction) {
        String sql = "UPDATE transactions SET amount=?, type=?, category_id=?, note=? WHERE id=?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, transaction.getAmount());
            stmt.setString(2, transaction.getType().name());
            stmt.setString(3, transaction.getCategory().getId());
            stmt.setString(4, transaction.getNote());
            stmt.setString(5, transaction.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi cập nhật giao dịch ID: " + transaction.getId(), e);
        }
    }

    public static void deleteTransaction(String id) {
        String sql = "DELETE FROM transactions WHERE id=?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi xóa giao dịch ID: " + id, e);
        }
    }

    // ========== BUDGETS ==========
    // Tối ưu: dùng range query thay vì MONTH/YEAR để tận dụng index
    public static Budget getBudget(int month, int year, String userId) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.plusMonths(1);
        String query = "SELECT * FROM budgets WHERE user_id = ? AND start_date >= ? AND start_date < ? LIMIT 1";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, userId);
            pstmt.setDate(2, java.sql.Date.valueOf(start));
            pstmt.setDate(3, java.sql.Date.valueOf(end));
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Budget b = new Budget();
                b.setId(rs.getString("id"));
                b.setLimit(rs.getDouble("budget_limit"));
                b.setSpent(rs.getDouble("spent"));
                java.sql.Date startDateSql = rs.getDate("start_date");
                if (startDateSql != null) b.setStartDate(startDateSql.toLocalDate());

                java.sql.Date endDateSql = rs.getDate("end_date");
                if (endDateSql != null) b.setEndDate(endDateSql.toLocalDate());
                b.setThreshold(rs.getInt("threshold"));
                b.setUserId(userId);
                return b;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<Budget> getAllBudgets(String userId) {
        List<Budget> list = new ArrayList<>();
        String query = "SELECT b.*, c.name AS cat_name, c.type AS cat_type FROM budgets b LEFT JOIN categories c ON b.category_id = c.id WHERE b.user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Budget b = new Budget();
                b.setId(rs.getString("id"));
                b.setLimit(rs.getDouble("budget_limit"));
                b.setSpent(rs.getDouble("spent"));
                java.sql.Date startDateSql = rs.getDate("start_date");
                if (startDateSql != null) {
                    b.setStartDate(startDateSql.toLocalDate());
                }
                java.sql.Date endDateSql = rs.getDate("end_date");
                if (endDateSql != null) {
                    b.setEndDate(endDateSql.toLocalDate());
                }
                b.setThreshold(rs.getInt("threshold"));
                b.setUserId(userId);

                String catId = rs.getString("category_id");
                if (catId != null) {
                    Category cat = new Category();
                    cat.setId(catId);
                    cat.setName(rs.getString("cat_name"));
                    String typeStr = rs.getString("cat_type");
                    if(typeStr != null) {
                        cat.setType(TransactionType.valueOf(typeStr));
                    }
                    b.setCategory(cat);
                }
                list.add(b);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static void insertBudget(Budget b) {
        String query = "INSERT INTO budgets (id, budget_limit, spent, start_date, end_date, threshold, category_id, user_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, b.getId());
            pstmt.setDouble(2, b.getLimit());
            pstmt.setDouble(3, b.getSpent());
            pstmt.setDate(4, java.sql.Date.valueOf(b.getStartDate()));
            pstmt.setDate(5, java.sql.Date.valueOf(b.getEndDate()));
            pstmt.setInt(6, b.getThreshold());
            if (b.getCategory() != null && b.getCategory().getId() != null) {
                pstmt.setString(7, b.getCategory().getId());
            } else {
                pstmt.setNull(7, java.sql.Types.VARCHAR);
            }
            pstmt.setString(8, b.getUserId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateBudget(Budget b) {
        String query = "UPDATE budgets SET budget_limit = ?, spent = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setDouble(1, b.getLimit());
            pstmt.setDouble(2, b.getSpent());
            pstmt.setString(3, b.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteBudget(String id) {
        String query = "DELETE FROM budgets WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ========== USERS ==========
    public static void insertUser(User user) {
        String sql = "INSERT INTO users (id, username, password_hash, nickname, avatar, email, gender, premium_expiry_date, is_admin) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getId());
            stmt.setString(2, user.getUsername());
            stmt.setString(3, user.getPasswordHash());
            stmt.setString(4, user.getNickname());
            stmt.setString(5, user.getAvatar());
            stmt.setString(6, user.getEmail());
            stmt.setString(7, user.getGender());
            stmt.setDate(8, user.getPremiumExpiryDate() != null ? Date.valueOf(user.getPremiumExpiryDate()) : null);
            stmt.setBoolean(9, user.isAdmin());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi lưu thông tin tài khoản đăng ký mới", e);
        }
    }

    public static User getUserByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String id = rs.getString("id");
                    String passwordHash = rs.getString("password_hash");
                    String nickname = rs.getString("nickname");
                    String avatar = rs.getString("avatar");
                    String email = rs.getString("email");
                    String gender = rs.getString("gender");
                    Date premiumDate = rs.getDate("premium_expiry_date");
                    boolean isAdmin = rs.getBoolean("is_admin");

                    User user = new User(id, username, passwordHash, nickname, email, gender);
                    user.setAvatar(avatar);
                    user.setPremiumExpiryDate(premiumDate != null ? premiumDate.toLocalDate() : null);
                    user.setAdmin(isAdmin);
                    return user;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi tìm kiếm hồ sơ tài khoản: " + username, e);
        }
        return null;
    }

    public static void updateUser(User user) {
        String sql = "UPDATE users SET nickname=?, avatar=?, email=?, gender=? WHERE id=?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getNickname());
            stmt.setString(2, user.getAvatar());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getGender());
            stmt.setString(5, user.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi thực hiện cập nhật thông tin hồ sơ User", e);
        }
    }

    public static void updateUserPassword(String userId, String newPasswordHash) {
        String sql = "UPDATE users SET password_hash=? WHERE id=?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newPasswordHash);
            stmt.setString(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi cập nhật mật khẩu", e);
        }
    }

    public static void deleteTransactionsByUser(String userId) {
        String sql = "DELETE FROM transactions WHERE user_id = ?";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi xóa sạch lịch sử giao dịch", e);
        }
    }

    public static void deleteBudgetsByUser(String userId) {
        String sql = "DELETE FROM budgets WHERE user_id = ?";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi xóa lịch sử ngân sách", e);
        }
    }

    // ✅ THÊM: Xóa giao dịch lặp lại khi xóa user
    public static void deleteRecurringTransactionsByUser(String userId) {
        String sql = "DELETE FROM recurring_transactions WHERE user_id = ?";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi xóa giao dịch lặp lại của user", e);
        }
    }

    public static void deleteUser(String userId) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi xóa tài khoản", e);
        }
    }

    public static User getUserByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String id = rs.getString("id");
                String username = rs.getString("username");
                String passwordHash = rs.getString("password_hash");
                String nickname = rs.getString("nickname");
                String gender = rs.getString("gender");
                String avatar = rs.getString("avatar");
                User user = new User(id, username, passwordHash, nickname, email, gender);
                user.setAvatar(avatar);
                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean updatePasswordByEmail(String email, String newPasswordHash) {
        String sql = "UPDATE users SET password_hash = ? WHERE email = ?";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newPasswordHash);
            stmt.setString(2, email);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ========== RECURRING TRANSACTIONS ==========
    public static void insertRecurringTransaction(RecurringTransaction rt) {
        String sql = "INSERT INTO recurring_transactions " +
                "(id, user_id, amount, type, category_id, note, recurrence_type, custom_interval_days, " +
                "start_date, end_date, created_at, is_active, last_generated_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, rt.getId());
            stmt.setString(2, rt.getUserId());
            stmt.setDouble(3, rt.getAmount());
            stmt.setString(4, rt.getType().name());
            stmt.setString(5, rt.getCategory() != null ? rt.getCategory().getId() : null);
            stmt.setString(6, rt.getNote());
            stmt.setString(7, rt.getRecurrenceType().name());
            stmt.setInt(8, rt.getCustomIntervalDays());
            stmt.setDate(9, java.sql.Date.valueOf(rt.getStartDate()));
            stmt.setDate(10, rt.getEndDate() != null ? java.sql.Date.valueOf(rt.getEndDate()) : null);
            stmt.setTimestamp(11, java.sql.Timestamp.valueOf(rt.getCreatedAt()));
            stmt.setBoolean(12, rt.isActive());
            stmt.setDate(13, rt.getLastGeneratedDate() != null ? java.sql.Date.valueOf(rt.getLastGeneratedDate()) : null);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi thêm giao dịch lặp lại", e);
        }
    }

    public static List<RecurringTransaction> getRecurringTransactions(String userId) {
        List<RecurringTransaction> list = new ArrayList<>();
        String sql = "SELECT rt.*, c.name as category_name, c.type as category_type " +
                "FROM recurring_transactions rt " +
                "LEFT JOIN categories c ON rt.category_id = c.id " +
                "WHERE rt.user_id = ? ORDER BY rt.start_date DESC";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    RecurringTransaction rt = new RecurringTransaction();
                    rt.setId(rs.getString("id"));
                    rt.setUserId(rs.getString("user_id"));
                    rt.setAmount(rs.getDouble("amount"));
                    rt.setType(TransactionType.valueOf(rs.getString("type")));

                    String catId = rs.getString("category_id");
                    if (catId != null && rs.getString("category_name") != null) {
                        Category cat = new Category(catId, rs.getString("category_name"),
                                TransactionType.valueOf(rs.getString("category_type")));
                        rt.setCategory(cat);
                    }

                    rt.setNote(rs.getString("note"));
                    rt.setRecurrenceType(RecurringTransaction.RecurrenceType.valueOf(rs.getString("recurrence_type")));
                    rt.setCustomIntervalDays(rs.getInt("custom_interval_days"));

                    java.sql.Date startDateSql = rs.getDate("start_date");
                    if (startDateSql != null) rt.setStartDate(startDateSql.toLocalDate());

                    java.sql.Date endDateSql = rs.getDate("end_date");
                    if (endDateSql != null) rt.setEndDate(endDateSql.toLocalDate());

                    java.sql.Timestamp createdAtSql = rs.getTimestamp("created_at");
                    if (createdAtSql != null) rt.setCreatedAt(createdAtSql.toLocalDateTime());

                    rt.setActive(rs.getBoolean("is_active"));

                    java.sql.Date lastGenSql = rs.getDate("last_generated_date");
                    if (lastGenSql != null) rt.setLastGeneratedDate(lastGenSql.toLocalDate());

                    list.add(rt);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi tải giao dịch lặp lại của User", e);
        }
        return list;
    }

    private static void ensureRecurringTransactionsTableExists() {
        String createSql = "CREATE TABLE IF NOT EXISTS recurring_transactions (" +
                "id VARCHAR(50) PRIMARY KEY, " +
                "user_id VARCHAR(20) NOT NULL, " +
                "amount DECIMAL(15,2) NOT NULL, " +
                "type ENUM('INCOME','EXPENSE') NOT NULL, " +
                "category_id VARCHAR(10), " +
                "note TEXT, " +
                "recurrence_type ENUM('DAILY','WEEKLY','MONTHLY','YEARLY','CUSTOM') NOT NULL, " +
                "custom_interval_days INT DEFAULT 0, " +
                "start_date DATE NOT NULL, " +
                "end_date DATE, " +
                "created_at DATETIME NOT NULL, " +
                "is_active BOOLEAN DEFAULT TRUE, " +
                "last_generated_date DATE, " +
                "INDEX idx_user_id (user_id), " +
                "INDEX idx_is_active (is_active), " +
                "INDEX idx_recurrence_type (recurrence_type) " +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(createSql);
        } catch (SQLException e) {
            throw new RuntimeException("Không thể tạo bảng recurring_transactions tự động", e);
        }
    }

    public static RecurringTransaction getRecurringTransactionById(String id) {
        String sql = "SELECT rt.*, c.name as category_name, c.type as category_type " +
                "FROM recurring_transactions rt " +
                "LEFT JOIN categories c ON rt.category_id = c.id " +
                "WHERE rt.id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    RecurringTransaction rt = new RecurringTransaction();
                    rt.setId(rs.getString("id"));
                    rt.setUserId(rs.getString("user_id"));
                    rt.setAmount(rs.getDouble("amount"));
                    rt.setType(TransactionType.valueOf(rs.getString("type")));

                    String catId = rs.getString("category_id");
                    if (catId != null && rs.getString("category_name") != null) {
                        Category cat = new Category(catId, rs.getString("category_name"),
                                TransactionType.valueOf(rs.getString("category_type")));
                        rt.setCategory(cat);
                    }

                    rt.setNote(rs.getString("note"));
                    rt.setRecurrenceType(RecurringTransaction.RecurrenceType.valueOf(rs.getString("recurrence_type")));
                    rt.setCustomIntervalDays(rs.getInt("custom_interval_days"));

                    java.sql.Date startDateSql = rs.getDate("start_date");
                    if (startDateSql != null) rt.setStartDate(startDateSql.toLocalDate());

                    java.sql.Date endDateSql = rs.getDate("end_date");
                    if (endDateSql != null) rt.setEndDate(endDateSql.toLocalDate());

                    java.sql.Timestamp createdAtSql = rs.getTimestamp("created_at");
                    if (createdAtSql != null) rt.setCreatedAt(createdAtSql.toLocalDateTime());

                    rt.setActive(rs.getBoolean("is_active"));

                    java.sql.Date lastGenSql = rs.getDate("last_generated_date");
                    if (lastGenSql != null) rt.setLastGeneratedDate(lastGenSql.toLocalDate());

                    return rt;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi tải giao dịch lặp lại ID: " + id, e);
        }
        return null;
    }

    public static void updateRecurringTransaction(RecurringTransaction rt) {
        String sql = "UPDATE recurring_transactions SET amount=?, note=?, recurrence_type=?, " +
                "custom_interval_days=?, end_date=?, is_active=?, last_generated_date=? WHERE id=?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, rt.getAmount());
            stmt.setString(2, rt.getNote());
            stmt.setString(3, rt.getRecurrenceType().name());
            stmt.setInt(4, rt.getCustomIntervalDays());
            stmt.setDate(5, rt.getEndDate() != null ? java.sql.Date.valueOf(rt.getEndDate()) : null);
            stmt.setBoolean(6, rt.isActive());
            stmt.setDate(7, rt.getLastGeneratedDate() != null ? java.sql.Date.valueOf(rt.getLastGeneratedDate()) : null);
            stmt.setString(8, rt.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi cập nhật giao dịch lặp lại", e);
        }
    }

    public static void deleteRecurringTransaction(String id) {
        String sql = "DELETE FROM recurring_transactions WHERE id=?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi xóa giao dịch lặp lại", e);
        }
    }

    public static List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY username";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String id = rs.getString("id");
                String username = rs.getString("username");
                String passwordHash = rs.getString("password_hash");
                String nickname = rs.getString("nickname");
                String avatar = rs.getString("avatar");
                String email = rs.getString("email");
                String gender = rs.getString("gender");
                Date premiumDate = rs.getDate("premium_expiry_date");
                boolean isAdmin = rs.getBoolean("is_admin");

                User user = new User(id, username, passwordHash, nickname, email, gender);
                user.setAvatar(avatar);
                user.setPremiumExpiryDate(premiumDate != null ? premiumDate.toLocalDate() : null);
                user.setAdmin(isAdmin);
                list.add(user);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tải danh sách người dùng", e);
        }
        return list;
    }

    public static void updateUserPremium(String userId, LocalDate expiryDate) {
        String sql = "UPDATE users SET premium_expiry_date = ? WHERE id = ?";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, expiryDate != null ? Date.valueOf(expiryDate) : null);
            stmt.setString(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi cập nhật Premium", e);
        }
    }

    public static void updateUserAdmin(String userId, boolean isAdmin) {
        String sql = "UPDATE users SET is_admin = ? WHERE id = ?";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, isAdmin);
            stmt.setString(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi cập nhật quyền admin", e);
        }
    }

    public static void createDefaultAdminIfNotExists() {
        User admin = getUserByUsername("admin");
        if (admin == null) {
            String id = "admin_" + System.currentTimeMillis();
            String hashedPass = UserService.hashPassword("admin123");
            User defaultAdmin = new User(id, "admin", hashedPass, "Administrator", "admin@example.com", "Other");
            defaultAdmin.setAdmin(true);
            defaultAdmin.setPremiumExpiryDate(null);
            insertUser(defaultAdmin);
            System.out.println("✅ Đã tạo tài khoản admin: admin / admin123");
        } else if (!admin.isAdmin()) {
            updateUserAdmin(admin.getId(), true);
            System.out.println("✅ Đã cấp quyền admin cho user: admin");
        }
    }

    // ========== TIỆN ÍCH KHOẢNG THỜI GIAN ==========
    public static List<Transaction> getTransactionsByDateRange(String userId, LocalDate startDate, LocalDate endDate) {
        List<Transaction> list = new ArrayList<>();

        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            LocalDate temp = startDate;
            startDate = endDate;
            endDate = temp;
        }

        String sql = "SELECT t.*, c.name as category_name, c.type as category_type " +
                "FROM transactions t JOIN categories c ON t.category_id = c.id " +
                "WHERE t.user_id = ? AND DATE(t.date_time) >= ? AND DATE(t.date_time) <= ? " +
                "ORDER BY t.date_time DESC";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId);
            pstmt.setString(2, startDate.toString());
            pstmt.setString(3, endDate.toString());

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String id = rs.getString("id");
                    double amount = rs.getDouble("amount");
                    String note = rs.getString("note");
                    LocalDateTime dateTime = rs.getTimestamp("date_time").toLocalDateTime();

                    String catId = rs.getString("category_id");
                    String catName = rs.getString("category_name");
                    String typeStr = rs.getString("category_type");
                    TransactionType type = TransactionType.valueOf(typeStr);
                    Category category = new Category(catId, catName, type);

                    // Sử dụng constructor 6 tham số để tránh gọi setDateTime thừa
                    Transaction t = new Transaction(id, amount, type, category, note, dateTime);
                    list.add(t);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tải giao dịch theo khoảng ngày: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }
}