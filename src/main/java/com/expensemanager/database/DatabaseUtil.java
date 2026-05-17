package com.expensemanager.database;

import com.expensemanager.entity.*;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DatabaseUtil {

    // ========== THÊM: CONNECTION POOL VỚI HIKARICP ==========
    private static HikariDataSource dataSource;

    static {
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(DatabaseConfig.DB_URL);
            config.setUsername(DatabaseConfig.DB_USER);
            config.setPassword(DatabaseConfig.DB_PASSWORD);

            // Cấu hình pool
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setConnectionTimeout(30000);
            config.setIdleTimeout(600000);
            config.setMaxLifetime(1800000);

            // Cấu hình hiệu năng PreparedStatement cache
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("useServerPrepStmts", "true");
            config.addDataSourceProperty("rewriteBatchedStatements", "true");

            dataSource = new HikariDataSource(config);
        } catch (Exception e) {
            System.err.println("Lỗi khởi tạo Connection Pool: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    // ========== SỬA: getConnection() dùng pool thay vì DriverManager ==========
    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();  // Thay vì DriverManager.getConnection()
    }

    // ========== THÊM: Đóng pool khi ứng dụng thoát ==========
    public static void closePool() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            System.out.println("Đã đóng Connection Pool");
        }
    }

    // ========== THÊM: Kiểm tra trạng thái pool (debug) ==========
    public static String getPoolStatus() {
        if (dataSource == null) return "Pool chưa khởi tạo";
        return String.format(
                "Active: %d, Idle: %d, Total: %d",
                dataSource.getHikariPoolMXBean().getActiveConnections(),
                dataSource.getHikariPoolMXBean().getIdleConnections(),
                dataSource.getHikariPoolMXBean().getTotalConnections()
        );
    }

    // ========== THÊM MỚI: Khởi tạo database tables ==========
    public static void initializeDatabase() {
        String[] createTableQueries = {
                // Users table
                "CREATE TABLE IF NOT EXISTS users (" +
                        "id VARCHAR(36) PRIMARY KEY, " +
                        "username VARCHAR(50) UNIQUE NOT NULL, " +
                        "password_hash VARCHAR(255) NOT NULL, " +
                        "nickname VARCHAR(100), " +
                        "avatar VARCHAR(255), " +
                        "email VARCHAR(100), " +
                        "gender VARCHAR(10), " +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                        ")",

                // Categories table
                "CREATE TABLE IF NOT EXISTS categories (" +
                        "id VARCHAR(36) PRIMARY KEY, " +
                        "name VARCHAR(50) NOT NULL, " +
                        "type ENUM('INCOME', 'EXPENSE') NOT NULL" +
                        ")",

                // Transactions table
                "CREATE TABLE IF NOT EXISTS transactions (" +
                        "id VARCHAR(36) PRIMARY KEY, " +
                        "amount DECIMAL(15,2) NOT NULL, " +
                        "type ENUM('INCOME', 'EXPENSE') NOT NULL, " +
                        "category_id VARCHAR(36) NOT NULL, " +
                        "date_time DATETIME NOT NULL, " +
                        "note TEXT, " +
                        "user_id VARCHAR(36) NOT NULL, " +
                        "FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT, " +
                        "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE, " +
                        "INDEX idx_user_date (user_id, date_time)" +
                        ")",

                // Budgets table
                "CREATE TABLE IF NOT EXISTS budgets (" +
                        "id VARCHAR(36) PRIMARY KEY, " +
                        "month INT NOT NULL, " +
                        "year INT NOT NULL, " +
                        "budget_limit DECIMAL(15,2) NOT NULL, " +
                        "spent DECIMAL(15,2) DEFAULT 0, " +
                        "user_id VARCHAR(36) NOT NULL, " +
                        "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE, " +
                        "UNIQUE KEY unique_user_month_year (user_id, month, year)" +
                        ")"
        };

        // Insert default categories
        String insertCategories = "INSERT IGNORE INTO categories (id, name, type) VALUES " +
                "('cat1', 'Lương', 'INCOME'), " +
                "('cat2', 'Thưởng', 'INCOME'), " +
                "('cat3', 'Đầu tư', 'INCOME'), " +
                "('cat4', 'Ăn uống', 'EXPENSE'), " +
                "('cat5', 'Di chuyển', 'EXPENSE'), " +
                "('cat6', 'Giải trí', 'EXPENSE'), " +
                "('cat7', 'Học tập', 'EXPENSE'), " +
                "('cat8', 'Hóa đơn', 'EXPENSE')";

        try (Connection conn = getConnection()) {
            // Create tables
            for (String query : createTableQueries) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(query);
                    System.out.println("Created table successfully");
                }
            }

            // Insert default categories
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(insertCategories);
                System.out.println("Default categories inserted");
            }

            System.out.println("Database initialization completed!");

        } catch (SQLException e) {
            System.err.println("Failed to initialize database: " + e.getMessage());
            e.printStackTrace();
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
            e.printStackTrace();
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
            e.printStackTrace();
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
            e.printStackTrace();
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
            e.printStackTrace();
        }
    }

    public static List<Transaction> getAllTransactions(String userId) {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT t.*, c.name as category_name, c.type as category_type " +
                "FROM transactions t JOIN categories c ON t.category_id = c.id " +
                "WHERE t.user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
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

                    Transaction transaction = new Transaction(id, amount, type, category, note, dateTime);
                    list.add(transaction);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ========== THÊM: getTransactionsWithPagination (phân trang) ==========
    public static List<Transaction> getTransactionsWithPagination(String userId, int offset, int limit) {
        List<Transaction> list = new ArrayList<>();
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

                    Transaction transaction = new Transaction(id, amount, type, category, note, dateTime);
                    list.add(transaction);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ========== THÊM: đếm tổng số transaction (cho phân trang) ==========
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
            e.printStackTrace();
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
            e.printStackTrace();
        }
    }

    public static void deleteTransaction(String id) {
        String sql = "DELETE FROM transactions WHERE id=?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ========== BUDGETS ==========
    public static void insertBudget(Budget budget, String userId) {
        String sql = "INSERT INTO budgets (id, month, year, budget_limit, spent, user_id) VALUES (?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE budget_limit = ?, spent = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, budget.getId());
            stmt.setInt(2, budget.getMonth());
            stmt.setInt(3, budget.getYear());
            stmt.setDouble(4, budget.getLimit());
            stmt.setDouble(5, budget.getSpent());
            stmt.setString(6, userId);
            stmt.setDouble(7, budget.getLimit());
            stmt.setDouble(8, budget.getSpent());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateBudget(Budget budget) {
        String sql = "UPDATE budgets SET budget_limit=?, spent=? WHERE id=?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, budget.getLimit());
            stmt.setDouble(2, budget.getSpent());
            stmt.setString(3, budget.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Budget getBudget(int month, int year, String userId) {
        String sql = "SELECT * FROM budgets WHERE month=? AND year=? AND user_id=?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, month);
            stmt.setInt(2, year);
            stmt.setString(3, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String id = rs.getString("id");
                    double limit = rs.getDouble("budget_limit");
                    double spent = rs.getDouble("spent");
                    Budget budget = new Budget(id, month, year, limit);
                    budget.setSpent(spent);
                    return budget;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
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
            e.printStackTrace();
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
            e.printStackTrace();
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
            e.printStackTrace();
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
            e.printStackTrace();
        }
    }

    public static void deleteBudgetsByUser(String userId) {
        String sql = "DELETE FROM budgets WHERE user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteUser(String userId) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}