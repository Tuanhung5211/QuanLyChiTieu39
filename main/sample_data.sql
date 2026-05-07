USE expense_manager;

-- Danh mục mẫu
INSERT INTO categories (id, name, type) VALUES
                                            ('CAT001', 'Ăn uống', 'EXPENSE'),
                                            ('CAT002', 'Học tập', 'EXPENSE'),
                                            ('CAT003', 'Lương', 'INCOME'),
                                            ('CAT004', 'Đi lại', 'EXPENSE');

-- Ngân sách tháng 5/2026
INSERT INTO budgets (id, month, year, budget_limit, spent) VALUES
    ('BUD001', 5, 2026, 5000000, 0);

-- Giao dịch mẫu
INSERT INTO transactions (id, amount, type, category_id, date_time, note) VALUES
                                                                              ('TX001', 1500000, 'INCOME', 'CAT003', '2026-05-05 10:00:00', 'Lương tháng 5'),
                                                                              ('TX002', 50000, 'EXPENSE', 'CAT001', '2026-05-05 12:30:00', 'Cơm trưa'),
                                                                              ('TX003', 200000, 'EXPENSE', 'CAT002', '2026-05-06 15:00:00', 'Mua sách');