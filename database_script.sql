-- Xóa database cũ nếu tồn tại (cẩn thận: mất hết dữ liệu cũ, nhưng đang ở giai đoạn phát triển)
DROP DATABASE IF EXISTS expense_manager;

-- Tạo database mới
CREATE DATABASE expense_manager;
USE expense_manager;

-- Bảng users
CREATE TABLE users (
                       id VARCHAR(36) PRIMARY KEY,
                       username VARCHAR(50) UNIQUE NOT NULL,
                       password_hash VARCHAR(255) NOT NULL,
                       nickname VARCHAR(100),
                       avatar VARCHAR(255),
                       email VARCHAR(100),
                       gender VARCHAR(10),
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Bảng categories
CREATE TABLE categories (
                            id VARCHAR(10) PRIMARY KEY,
                            name VARCHAR(100) NOT NULL,
                            type ENUM('INCOME', 'EXPENSE') NOT NULL
);

-- Bảng transactions (có cột user_id)
CREATE TABLE transactions (
                              id VARCHAR(10) PRIMARY KEY,
                              amount DECIMAL(15,2) NOT NULL,
                              type ENUM('INCOME', 'EXPENSE') NOT NULL,
                              category_id VARCHAR(10),
                              date_time DATETIME NOT NULL,
                              note TEXT,
                              user_id VARCHAR(36) NOT NULL,
                              FOREIGN KEY (category_id) REFERENCES categories(id),
                              FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Bảng budgets (có cột user_id)
CREATE TABLE budgets (
                         id VARCHAR(10) PRIMARY KEY,
                         month INT NOT NULL,
                         year INT NOT NULL,
                         budget_limit DECIMAL(15,2) NOT NULL,
                         spent DECIMAL(15,2) DEFAULT 0,
                         user_id VARCHAR(36) NOT NULL,
                         FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                         UNIQUE KEY unique_user_month_year (user_id, month, year)
);

-- Chèn dữ liệu mẫu cho categories
INSERT IGNORE INTO categories (id, name, type) VALUES
('cat1', 'Lương', 'INCOME'),
('cat2', 'Thưởng', 'INCOME'),
('cat3', 'Đầu tư', 'INCOME'),
('cat4', 'Ăn uống', 'EXPENSE'),
('cat5', 'Di chuyển', 'EXPENSE'),
('cat6', 'Giải trí', 'EXPENSE'),
('cat7', 'Học tập', 'EXPENSE'),
('cat8', 'Hóa đơn', 'EXPENSE');