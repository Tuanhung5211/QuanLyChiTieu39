CREATE DATABASE IF NOT EXISTS expense_manager;
USE expense_manager;

CREATE TABLE IF NOT EXISTS categories (
                                          id VARCHAR(10) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type ENUM('INCOME', 'EXPENSE') NOT NULL
    );

CREATE TABLE IF NOT EXISTS budgets (
                                       id VARCHAR(10) PRIMARY KEY,
    month INT NOT NULL,
    year INT NOT NULL,
    budget_limit DECIMAL(15, 2) NOT NULL,
    spent DECIMAL(15, 2) DEFAULT 0
    );

CREATE TABLE IF NOT EXISTS transactions (
                                            id VARCHAR(10) PRIMARY KEY,
    amount DECIMAL(15, 2) NOT NULL,
    type ENUM('INCOME', 'EXPENSE') NOT NULL,
    category_id VARCHAR(10),
    date_time DATETIME NOT NULL,
    note TEXT,
    FOREIGN KEY (category_id) REFERENCES categories(id)
    );