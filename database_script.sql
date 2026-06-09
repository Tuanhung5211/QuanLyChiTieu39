-- ============================================
-- DATABASE SCRIPT FOR EXPENSE MANAGER
-- Quản Lý Chi Tiêu - Ứng dụng quản lý tài chính cá nhân
-- Phiên bản: Hỗ trợ Premium & Admin
-- ============================================

-- Tạo database nếu chưa tồn tại
CREATE DATABASE IF NOT EXISTS expense_manager
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

-- Sử dụng database
USE expense_manager;

-- ============================================
-- BẢNG USERS: Thông tin người dùng (ĐÃ THÊM CỘT PREMIUM & ADMIN)
-- ============================================
CREATE TABLE IF NOT EXISTS users (
                                     id VARCHAR(20) PRIMARY KEY COMMENT 'ID duy nhất của người dùng',
    username VARCHAR(50) UNIQUE NOT NULL COMMENT 'Tên đăng nhập (duy nhất)',
    password_hash VARCHAR(255) NOT NULL COMMENT 'Mật khẩu đã mã hóa SHA-256',
    nickname VARCHAR(100) COMMENT 'Tên hiển thị',
    avatar VARCHAR(10) DEFAULT '👤' COMMENT 'Avatar/Emoji đại diện',
    email VARCHAR(50) COMMENT 'Email liên hệ',
    gender VARCHAR(10) COMMENT 'Giới tính (Male/Female/Other)',
    premium_expiry_date DATE DEFAULT NULL COMMENT 'Ngày hết hạn Premium (NULL = không Premium)',
    is_admin BOOLEAN DEFAULT FALSE COMMENT 'Quyền quản trị viên',
    INDEX idx_username (username),
    INDEX idx_premium_expiry (premium_expiry_date)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- BẢNG CATEGORIES: Danh mục giao dịch
-- ============================================
CREATE TABLE IF NOT EXISTS categories (
                                          id VARCHAR(10) PRIMARY KEY COMMENT 'ID danh mục',
    name VARCHAR(100) NOT NULL COMMENT 'Tên danh mục (Ăn uống, Học tập, v.v.)',
    type ENUM('INCOME', 'EXPENSE') NOT NULL COMMENT 'Loại danh mục (Thu nhập/Chi tiêu)',
    INDEX idx_type (type)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- BẢNG TRANSACTIONS: Giao dịch thu/chi
-- ============================================
CREATE TABLE IF NOT EXISTS transactions (
                                            id VARCHAR(10) PRIMARY KEY COMMENT 'ID giao dịch',
    amount DECIMAL(15, 2) NOT NULL COMMENT 'Số tiền giao dịch',
    type ENUM('INCOME', 'EXPENSE') NOT NULL COMMENT 'Loại giao dịch',
    category_id VARCHAR(10) COMMENT 'ID danh mục tham chiếu',
    date_time DATETIME NOT NULL COMMENT 'Thời gian giao dịch',
    note TEXT COMMENT 'Ghi chú thêm',
    user_id VARCHAR(20) COMMENT 'ID người dùng sở hữu',

    -- Foreign Keys
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,

    -- Indexes cho tối ưu truy vấn
    INDEX idx_user_id (user_id),
    INDEX idx_date_time (date_time),
    INDEX idx_type (type)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- BẢNG BUDGETS: Ngân sách hàng tháng
-- ============================================
CREATE TABLE IF NOT EXISTS budgets (
                                       id VARCHAR(50) PRIMARY KEY COMMENT 'ID ngân sách',
    month INT NOT NULL,
    year INT NOT NULL,
    budget_limit DECIMAL(15, 2) NOT NULL,
    spent DECIMAL(15, 2) DEFAULT 0,
    user_id VARCHAR(20),

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uq_user_month_year (user_id, month, year)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- BẢNG RECURRING_TRANSACTIONS: Giao dịch lặp lại
-- ============================================
CREATE TABLE IF NOT EXISTS recurring_transactions (
                                                      id VARCHAR(50) PRIMARY KEY COMMENT 'ID giao dịch lặp lại',
    user_id VARCHAR(20) NOT NULL COMMENT 'ID người dùng sở hữu',
    amount DECIMAL(15, 2) NOT NULL COMMENT 'Số tiền giao dịch',
    type ENUM('INCOME', 'EXPENSE') NOT NULL COMMENT 'Loại giao dịch',
    category_id VARCHAR(10) COMMENT 'ID danh mục tham chiếu',
    note TEXT COMMENT 'Ghi chú',
    recurrence_type ENUM('DAILY', 'WEEKLY', 'MONTHLY', 'YEARLY', 'CUSTOM') NOT NULL COMMENT 'Kiểu lặp lại',
    custom_interval_days INT DEFAULT 0 COMMENT 'Khoảng cách (ngày) nếu CUSTOM',
    start_date DATE NOT NULL COMMENT 'Ngày bắt đầu',
    end_date DATE COMMENT 'Ngày kết thúc (NULL = không hạn chế)',
    created_at DATETIME NOT NULL COMMENT 'Thời gian tạo',
    is_active BOOLEAN DEFAULT TRUE COMMENT 'Trạng thái hoạt động',
    last_generated_date DATE COMMENT 'Ngày tạo giao dịch cuối cùng',

    -- Foreign Keys
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,

    -- Indexes
    INDEX idx_user_id (user_id),
    INDEX idx_is_active (is_active),
    INDEX idx_recurrence_type (recurrence_type)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- THÊM CÁC RÀNG BUỘC KIỂM TRA (NẾU CẦN)
-- ============================================
-- (Có thể thêm trigger để tự động cập nhật spent trong budgets khi thêm/sửa/xóa transaction)

-- ============================================
-- KHỞI TẠO DỮ LIỆU MẪU (TÙY CHỌN)
-- ============================================
-- Tạo tài khoản admin mặc định (mật khẩu: admin123 - đã được hash SHA-256)
-- Mật khẩu "admin123" sau khi hash: 240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9
INSERT IGNORE INTO users (id, username, password_hash, nickname, email, gender, is_admin)
VALUES ('admin_001', 'admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'Administrator', 'admin@example.com', 'Other', TRUE);

-- Chèn một số danh mục mặc định (nếu chưa có)
INSERT IGNORE INTO categories (id, name, type) VALUES
('cat_exp_01', 'Ăn uống', 'EXPENSE'),
('cat_exp_02', 'Di chuyển', 'EXPENSE'),
('cat_exp_03', 'Giải trí', 'EXPENSE'),
('cat_exp_04', 'Hóa đơn', 'EXPENSE'),
('cat_exp_05', 'Mua sắm', 'EXPENSE'),
('cat_inc_01', 'Lương', 'INCOME'),
('cat_inc_02', 'Thưởng', 'INCOME'),
('cat_inc_03', 'Đầu tư', 'INCOME'),
('cat_inc_04', 'Thu khác', 'INCOME');