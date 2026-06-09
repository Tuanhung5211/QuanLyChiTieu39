package com.expensemanager.database;

import com.expensemanager.entity.*;
import java.sql.*;
import java.sql.SQLSyntaxErrorException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DatabaseUtil {

    // Lấy kết nối trực tiếp thuần DriverManager theo yêu cầu
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                DatabaseConfig.DB_URL,
                DatabaseConfig.DB_USER,
                DatabaseConfig.DB_PASSWORD
        );
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

    // Hàm phụ trợ ánh xạ dữ liệu ResultSet
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
                "WHERE t.user_id = ? " +
                "ORDER BY t.date_time DESC " +
                "LIMIT ? OFFSET ?";
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

    // ==========================================================
    // CÁC HÀM QUẢN LÝ NGÂN SÁCH (ĐÃ ĐỒNG BỘ CẤU TRÚC MỚI & XÓA HÀM THỪA)
    // ==========================================================

    public static Budget getBudget(int month, int year, String userId) {
        // The budgets table uses month/year columns (see database_script.sql).
        String query = "SELECT * FROM budgets WHERE user_id = ? AND month = ? AND year = ? LIMIT 1";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, userId);
            pstmt.setInt(2, month);
            pstmt.setInt(3, year);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Budget b = new Budget();
                b.setId(rs.getString("id"));
                b.setMonth(rs.getInt("month"));
                b.setYear(rs.getInt("year"));
                b.setLimit(rs.getDouble("budget_limit"));
                b.setSpent(rs.getDouble("spent"));
                b.setUserId(userId);
                // derive start/end dates from month/year for compatibility with UI logic
                java.time.LocalDate start = java.time.LocalDate.of(b.getYear(), b.getMonth(), 1);
                java.time.LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
                b.setStartDate(start);
                b.setEndDate(end);
                return b;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static java.util.List<Budget> getAllBudgets(String userId) {
        java.util.List<Budget> list = new java.util.ArrayList<>();
        // budgets table currently stores month/year, budget_limit, spent and user_id
        String query = "SELECT * FROM budgets WHERE user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Budget b = new Budget();
                b.setId(rs.getString("id"));
                b.setMonth(rs.getInt("month"));
                b.setYear(rs.getInt("year"));
                b.setLimit(rs.getDouble("budget_limit"));
                b.setSpent(rs.getDouble("spent"));
                b.setUserId(userId);

                // Derive start/end dates from month/year so existing code that uses them continues to work
                java.time.LocalDate start = java.time.LocalDate.of(b.getYear(), b.getMonth(), 1);
                java.time.LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
                b.setStartDate(start);
                b.setEndDate(end);

                list.add(b);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static void insertBudget(Budget b) {
        // Ensure month/year exist; derive from startDate if necessary
        int month = b.getMonth();
        int year = b.getYear();
        if ((month == 0 || year == 0) && b.getStartDate() != null) {
            month = b.getStartDate().getMonthValue();
            year = b.getStartDate().getYear();
            b.setMonth(month);
            b.setYear(year);
        }

        String query = "INSERT INTO budgets (id, month, year, budget_limit, spent, user_id) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, b.getId());
            pstmt.setInt(2, month);
            pstmt.setInt(3, year);
            pstmt.setDouble(4, b.getLimit());
            pstmt.setDouble(5, b.getSpent());
            pstmt.setString(6, b.getUserId());
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
        String sql = "INSERT INTO users (id, username, password_hash, nickname, avatar, email, gender) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getId());
            stmt.setString(2, user.getUsername());
            stmt.setString(3, user.getPasswordHash());
            stmt.setString(4, user.getNickname());
            stmt.setString(5, user.getAvatar());
            stmt.setString(6, user.getEmail());
            stmt.setString(7, user.getGender());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi lưu thông tin tài khoản đăng ký mới", e);
        }
    }

    public static User getUserByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String id = rs.getString("id");
                    String passwordHash = rs.getString("password_hash");
                    String nickname = rs.getString("nickname");
                    String avatar = rs.getString("avatar");
                    String email = rs.getString("email");
                    String gender = rs.getString("gender");

                    User user = new User(id, username, passwordHash, nickname, email, gender);
                    user.setAvatar(avatar);
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

    // ========== XÓA DỮ LIỆU THEO USER ==========
    public static void deleteTransactionsByUser(String userId) {
        String sql = "DELETE FROM transactions WHERE user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi xóa sạch lịch sử giao dịch của hội viên: " + userId, e);
        }
    }

    public static void deleteBudgetsByUser(String userId) {
        String sql = "DELETE FROM budgets WHERE user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi xóa lịch sử ngân sách chi tiêu của hội viên: " + userId, e);
        }
    }

    public static void deleteUser(String userId) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi xóa vĩnh viễn tài khoản người dùng khỏi hệ thống", e);
        }
    }

    // Tìm user theo email
    public static User getUserByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
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
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
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

    public static java.util.List<RecurringTransaction> getRecurringTransactions(String userId) {
        java.util.List<RecurringTransaction> list = new java.util.ArrayList<>();
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
                    if (startDateSql != null) {
                        rt.setStartDate(startDateSql.toLocalDate());
                    }

                    java.sql.Date endDateSql = rs.getDate("end_date");
                    if (endDateSql != null) {
                        rt.setEndDate(endDateSql.toLocalDate());
                    }

                    java.sql.Timestamp createdAtSql = rs.getTimestamp("created_at");
                    if (createdAtSql != null) {
                        rt.setCreatedAt(createdAtSql.toLocalDateTime());
                    }

                    rt.setActive(rs.getBoolean("is_active"));

                    java.sql.Date lastGenSql = rs.getDate("last_generated_date");
                    if (lastGenSql != null) {
                        rt.setLastGeneratedDate(lastGenSql.toLocalDate());
                    }

                    list.add(rt);
                }
            }
        } catch (SQLSyntaxErrorException syntaxEx) {
            // Likely the recurring_transactions table does not exist. Try to create it and retry once.
            try {
                ensureRecurringTransactionsTableExists();
                // retry the query once
                try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
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
                            if (startDateSql != null) {
                                rt.setStartDate(startDateSql.toLocalDate());
                            }

                            java.sql.Date endDateSql = rs.getDate("end_date");
                            if (endDateSql != null) {
                                rt.setEndDate(endDateSql.toLocalDate());
                            }

                            java.sql.Timestamp createdAtSql = rs.getTimestamp("created_at");
                            if (createdAtSql != null) {
                                rt.setCreatedAt(createdAtSql.toLocalDateTime());
                            }

                            rt.setActive(rs.getBoolean("is_active"));

                            java.sql.Date lastGenSql = rs.getDate("last_generated_date");
                            if (lastGenSql != null) {
                                rt.setLastGeneratedDate(lastGenSql.toLocalDate());
                            }

                            list.add(rt);
                        }
                    }
                }
            } catch (SQLException | RuntimeException retryEx) {
                throw new RuntimeException("Lỗi khi tải giao dịch lặp lại của User: " + userId, retryEx);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi tải giao dịch lặp lại của User: " + userId, e);
        }
        return list;
    }

    /**
     * Ensure the recurring_transactions table exists in the database. If not, create it.
     */
    private static void ensureRecurringTransactionsTableExists() {
        // Create table without foreign key constraints to avoid charset/collation/engine incompatibilities
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
                    if (startDateSql != null) {
                        rt.setStartDate(startDateSql.toLocalDate());
                    }

                    java.sql.Date endDateSql = rs.getDate("end_date");
                    if (endDateSql != null) {
                        rt.setEndDate(endDateSql.toLocalDate());
                    }

                    java.sql.Timestamp createdAtSql = rs.getTimestamp("created_at");
                    if (createdAtSql != null) {
                        rt.setCreatedAt(createdAtSql.toLocalDateTime());
                    }

                    rt.setActive(rs.getBoolean("is_active"));

                    java.sql.Date lastGenSql = rs.getDate("last_generated_date");
                    if (lastGenSql != null) {
                        rt.setLastGeneratedDate(lastGenSql.toLocalDate());
                    }

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
            throw new RuntimeException("Lỗi khi cập nhật giao dịch lặp lại ID: " + rt.getId(), e);
        }
    }

    public static void deleteRecurringTransaction(String id) {
        String sql = "DELETE FROM recurring_transactions WHERE id=?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi xóa giao dịch lặp lại ID: " + id, e);
        }
    }
}