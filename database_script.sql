-- ============================================
-- DATABASE SCRIPT FOR EXPENSE MANAGER
-- Quản Lý Chi Tiêu - Ứng dụng quản lý tài chính cá nhân
-- ============================================

-- Tạo database nếu chưa tồn tại
CREATE DATABASE IF NOT EXISTS expense_manager
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

-- Sử dụng database
USE expense_manager;

-- ============================================
-- BẢNG USERS: Thông tin người dùng
-- ============================================
CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(20) PRIMARY KEY COMMENT 'ID duy nhất của người dùng',
    username VARCHAR(50) UNIQUE NOT NULL COMMENT 'Tên đăng nhập (duy nhất)',
    password_hash VARCHAR(255) NOT NULL COMMENT 'Mật khẩu đã mã hóa SHA-256',
    nickname VARCHAR(100) COMMENT 'Tên hiển thị',
    avatar VARCHAR(10) DEFAULT '👤' COMMENT 'Avatar/Emoji đại diện',
    email VARCHAR(50) COMMENT 'Email liên hệ',
    gender VARCHAR(10) COMMENT 'Giới tính (Male/Female/Other)',
    INDEX idx_username (username)
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
                                       id VARCHAR(50) PRIMARY KEY COMMENT 'ID ngân sách', -- Nâng kích thước từ 10 lên 50
    month INT NOT NULL,
    year INT NOT NULL,
    budget_limit DECIMAL(15, 2) NOT NULL,
    spent DECIMAL(15, 2) DEFAULT 0,
    user_id VARCHAR(20),

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uq_user_month_year (user_id, month, year) -- Đảm bảo tính duy nhất theo cặp tài khoản + thời gian
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;