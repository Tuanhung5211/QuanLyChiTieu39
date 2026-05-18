# 💰 Quản Lý Chi Tiêu Mini - Expense Manager

Ứng dụng **quản lý tài chính cá nhân** hiện đại, được xây dựng bằng **Java** với giao diện đồ họa trực quan sử dụng **Swing**, kết hợp cơ sở dữ liệu **MySQL** và lưu trữ **JSON** để đảm bảo dữ liệu an toàn và dễ quản lý.

---

## 📋 Mục Lục
- [Giới Thiệu](#giới-thiệu)
- [Chức Năng](#-chức-năng)
- [Công Nghệ Sử Dụng](#-công-nghệ-sử-dụng)
- [Cấu Trúc Dự Án](#-cấu-trúc-dự-án)
- [Cài Đặt](#-cài-đặt)
- [Hướng Dẫn Sử Dụng](#-hướng-dẫn-sử-dụng)
- [Cấu Trúc Dữ Liệu](#-cấu-trúc-dữ-liệu)
- [Tác Giả](#-tác-giả)

---

## 🎯 Giới Thiệu

**Quản Lý Chi Tiêu Mini** là một ứng dụng Java desktop nhẹ nhàng nhưng đầy đủ chức năng, giúp bạn:
- 📊 Theo dõi thu nhập và chi tiêu hàng ngày
- 📈 Phân tích chi tiêu theo danh mục
- 💼 Quản lý ngân sách hàng tháng
- 💾 Lưu trữ dữ liệu an toàn trên MySQL và JSON

Dự án được thiết kế với kiến trúc **OOP** rõ ràng, dễ mở rộng và bảo trì.

---

## ✨ Chức Năng

### 🔰 Chức Năng Cơ Bản
- ✅ **Quản lý Thu nhập**: Thêm, sửa, xóa các khoản thu (lương, học bổng, tiền thưởng, làm thêm...)
- ✅ **Quản lý Chi tiêu**: Thêm, sửa, xóa các khoản chi với danh mục chi tiết
- ✅ **Lưu vết thời gian thực**: Tự động ghi nhận chính xác ngày giờ phát sinh giao dịch
- ✅ **Tính toán số dư**: Tự động tính tổng kết thu – chi, hiển thị số dư khả dụng
- ✅ **Giao diện đồ họa trực quan**: Dễ thao tác cho người mới bắt đầu

### ⭐ Chức Năng Nâng Cao
- 🔍 **Lọc và tìm kiếm giao dịch**: Theo khoảng thời gian, danh mục, loại (thu/chi) hoặc từ khóa ghi chú
- 📊 **Thống kê chi tiết**: 
  - Biểu đồ tròn/cột tỉ lệ chi tiêu theo danh mục
  - Báo cáo tổng quan hàng tháng
- 💼 **Quản lý ngân sách**: 
  - Thiết lập hạn mức chi tiêu từng tháng
  - Cảnh báo khi vượt quá hạn mức
- 💾 **Lưu trữ bền vững**: 
  - Ghi/đọc dữ liệu file **JSON** để khôi phục khi khởi động lại
  - Kết nối cơ sở dữ liệu **MySQL** qua **JDBC**, hỗ trợ CRUD đầy đủ
- ⚠️ **Xử lý ngoại lệ toàn diện**: Bắt lỗi nhập liệu, lỗi I/O, lỗi kết nối CSDL

---

## 🛠 Công Nghệ Sử Dụng

| Công Nghệ | Phiên Bản | Mô Tả |
|-----------|----------|-------|
| **Java** | 17+ | Ngôn ngữ lập trình chính |
| **Apache Maven** | 3.8+ | Quản lý thư viện và build dự án |
| **Java Swing** | JDK 17 | Xây dựng giao diện người dùng (GUI) |
| **Gson** | 2.10.1 | Thư viện đọc/ghi file JSON |
| **MySQL Connector** | 8.0.33 | Kết nối JDBC đến MySQL |
| **MySQL** | 5.7+ | Cơ sở dữ liệu lưu trữ |

### Kiến Trúc & Design Pattern
- **OOP** (Object-Oriented Programming): Lớp, kế thừa, đa hình
- **Service Pattern**: Phân tách Entity – Service – UI rõ ràng
- **Stream & Lambda**: Xử lý dữ liệu hiệu quả (Java 8+)
- **MVC Pattern**: Tách biệt Model-View-Controller

---

## 📁 Cấu Trúc Dự Án
QuanLyChiTieu39/ ├── pom.xml # Cấu hình Maven, khai báo dependency ├── README.md # Tài liệu hướng dẫn dự án ├── config.properties # Cấu hình ứng dụng (ngôn ngữ, kích thước cửa sổ) ├── database_script.sql # Script SQL tạo database và các bảng ├── transactions.json # File dữ liệu mẫu (JSON) │ └── src/ ├── main/ │ ├── java/ │ │ └── com/expensemanager/ │ │ ├── MainApp.java # Entry point, khởi tạo ứng dụng │ │ │ │ │ ├── entity/ # Các lớp dữ liệu │ │ │ ├── Transaction.java # Class giao dịch cơ bản │ │ │ ├── IncomeTransaction.java # Class thu nhập (kế thừa) │ │ │ ├── ExpenseTransaction.java # Class chi tiêu (kế thừa) │ │ │ ├── Category.java # Danh mục (id, name, type) │ │ │ ├── Budget.java # Ngân sách tháng │ │ │ └── TransactionType.java # Enum: INCOME, EXPENSE │ │ │ │ │ ├── database/ # Kết nối database │ │ │ ├── DatabaseConfig.java # Cấu hình kết nối │ │ │ └── DatabaseUtil.java # Hàm CRUD (insert, update, delete, select) │ │ │ │ │ ├── service/ # Logic nghiệp vụ │ │ │ ├── FinanceService.java # Service quản lý tài chính │ │ │ ├── StatisticsService.java # Service thống kê │ │ │ └── BudgetManager.java # Quản lý ngân sách │ │ │ │ │ ├── exception/ # Custom exceptions │ │ │ ├── InvalidAmountException.java # Lỗi số tiền không hợp lệ │ │ │ └── DataLoadException.java # Lỗi khi đọc dữ liệu │ │ │ │ │ ├── util/ # Các utility │ │ │ └── JsonUtil.java # Đọc/ghi file JSON │ │ │ │ │ └── ui/ # Giao diện người dùng │ │ ├── MainFrame.java # JFrame chính │ │ ├── DashboardPanel.java # Panel tổng quan │ │ ├── HistoryPanel.java # Panel lịch sử giao dịch │ │ ├── AddTransactionDialog.java # Dialog thêm giao dịch │ │ ├── StatisticsPanel.java # Panel thống kê │ │ └── BudgetDialog.java # Dialog quản lý ngân sách │ │ │ └── resources/ │ ├── data/ │ │ └── transactions.json # Dữ liệu mẫu │ └── images/ # Icon, hình ảnh │ └── test/ └── java/ └── com/expensemanager/ ├── FinanceServiceTest.java # Test service ├── DatabaseUtilTest.java # Test database └── JsonUtilTest.java # Test JSON
