package com.expensemanager.database;

import com.expensemanager.entity.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DatabaseUtil {

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
        // 🌟 TỐI ƯU: Đưa ResultSet rs vào try-with-resources để tự động giải phóng bộ nhớ
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
    // 🌟 ĐÃ SỬA LỖI CHÍ MẠNG: Thêm 'ON DUPLICATE KEY UPDATE' chống crash ứng dụng khi đổi hạn mức ngân sách
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

            // Tham số cấu hình cho phần UPDATE nếu trùng ID khóa chính
            stmt.setDouble(7, budget.getLimit());
            stmt.setDouble(8, budget.getSpent());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 🌟 TỐI ƯU: Cho phép cập nhật cả hạn mức lẫn số tiền đã tiêu một cách toàn diện
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

                    // 🌟 TỐI ƯU: Gọi gọn gàng Constructor 6 tham số đã khai báo và nạp avatar trực tiếp
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