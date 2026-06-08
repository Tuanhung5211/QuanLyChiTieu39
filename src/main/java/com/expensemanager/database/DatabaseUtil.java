package com.expensemanager.database;

import com.expensemanager.entity.*;
import java.sql.*;
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
        String query = "SELECT * FROM budgets WHERE user_id = ? AND MONTH(start_date) = ? AND YEAR(start_date) = ? LIMIT 1";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, userId);
            pstmt.setInt(2, month);
            pstmt.setInt(3, year);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Budget b = new Budget();
                b.setId(rs.getString("id"));
                b.setLimit(rs.getDouble("budget_limit"));
                b.setSpent(rs.getDouble("spent"));
                b.setStartDate(rs.getDate("start_date").toLocalDate());
                b.setEndDate(rs.getDate("end_date").toLocalDate());
                b.setThreshold(rs.getInt("threshold"));
                b.setUserId(userId);
                return b;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static java.util.List<Budget> getAllBudgets(String userId) {
        java.util.List<Budget> list = new java.util.ArrayList<>();
        String query = "SELECT b.*, c.name AS cat_name FROM budgets b LEFT JOIN categories c ON b.category_id = c.id WHERE b.user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Budget b = new Budget();
                b.setId(rs.getString("id"));
                b.setLimit(rs.getDouble("budget_limit"));
                b.setSpent(rs.getDouble("spent"));
                b.setStartDate(rs.getDate("start_date").toLocalDate());
                b.setEndDate(rs.getDate("end_date").toLocalDate());
                b.setThreshold(rs.getInt("threshold"));
                b.setUserId(userId);

                String catId = rs.getString("category_id");
                if (catId != null) {
                    Category cat = new Category();
                    cat.setId(catId);
                    cat.setName(rs.getString("cat_name"));
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
}