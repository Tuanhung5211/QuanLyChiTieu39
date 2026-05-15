# 💰 Quản Lý Chi Tiêu Mini (Expense Manager)

Ứng dụng quản lý tài chính cá nhân đơn giản được xây dựng bằng ngôn ngữ **Java** và thư viện **Swing**. Dự án được phát triển trong khuôn khổ Bài tập lớn môn **Công nghệ Java **.

## ✨ Chức năng chính

### 🔰 Chức năng cơ bản
- **Quản lý Thu nhập**: Thêm, sửa, xóa các khoản thu (lương, học bổng, tiền được cho…).
- **Quản lý Chi tiêu**: Thêm, sửa, xóa các khoản chi, mỗi khoản gắn với một danh mục (ăn uống, học tập, đi lại, giải trí…).
- **Lưu vết thời gian thực**: Tự động ghi nhận chính xác ngày giờ phát sinh giao dịch.
- **Tính toán số dư**: Tự động tổng kết thu – chi, hiển thị số dư khả dụng ngay trên giao diện chính.
- **Giao diện đồ họa trực quan**: Sử dụng Java Swing, dễ thao tác cho người mới bắt đầu.

### ⭐ Chức năng nâng cao (đáp ứng yêu cầu BTL)
- **Lọc và tìm kiếm giao dịch**: Theo khoảng thời gian, danh mục, loại (thu/chi) hoặc từ khóa ghi chú.
- **Thống kê**: Biểu đồ tròn/cột tỉ lệ chi tiêu theo danh mục; báo cáo tổng quan hàng tháng.
- **Quản lý ngân sách**: Thiết lập hạn mức chi tiêu từng tháng, cảnh báo khi vượt quá.
- **Lưu trữ bền vững**: 
  - Ghi/đọc dữ liệu ra file **JSON** để khôi phục khi khởi động lại ứng dụng.
  - Kết nối cơ sở dữ liệu **MySQL** qua **JDBC**, hỗ trợ đầy đủ CRUD.
- **Xử lý ngoại lệ toàn diện**: Bắt lỗi nhập liệu, lỗi I/O, lỗi kết nối CSDL; sử dụng custom exception.

## 🛠 Công nghệ sử dụng

| Công nghệ | Mô tả |
|-----------|-------|
| **Java (JDK 17+)** | Ngôn ngữ lập trình chính |
| **Apache Maven** | Quản lý thư viện và build dự án |
| **Java Swing** | Xây dựng giao diện người dùng (GUI) |
| **Gson** | Thư viện đọc/ghi file JSON |
| **MySQL + JDBC** | Lưu trữ dữ liệu bền vững, thực hiện CRUD |
| **OOP + Service Pattern** | Kiến trúc phân tách Entity – Service – UI rõ ràng |

## 📁 Cấu trúc dự án (tóm tắt)

## 📁 Cấu trúc dự án (dự kiến)
```text
ExpenseManager/
│
├── pom.xml                          # [Cả nhóm] File cấu hình Maven, khai báo dependency (Gson, MySQL Connector...)
├── README.md                        # [Cả nhóm] Hướng dẫn cài đặt, mô tả dự án
├── database_script.sql              # [Bạn A] Script tạo database và bảng trong MySQL
│
├── src/
│   ├── main/
│   │   ├── java/com/expensemanager/
│   │   │   │
│   │   │   ├── MainApp.java                    # [Bạn B] Entry point, khởi tạo MainFrame
│   │   │   │
│   │   │   ├── entity/                          # ========== BẠN A PHỤ TRÁCH ==========
│   │   │   │   ├── Transaction.java             #    Class cha: id, amount, date, type, note
│   │   │   │   ├── IncomeTransaction.java       #    Kế thừa Transaction (đáp ứng yêu cầu kế thừa)
│   │   │   │   ├── ExpenseTransaction.java      #    Kế thừa Transaction
│   │   │   │   ├── Category.java                #    Quản lý danh mục (id, name, type)
│   │   │   │   ├── Budget.java                  #    Quản lý ngân sách tháng (month, limit, spent)
│   │   │   │   └── TransactionType.java         #    Enum: INCOME, EXPENSE (thay vì String)
│   │   │   │
│   │   │   ├── database/                        # ========== BẠN A PHỤ TRÁCH ==========
│   │   │   │   ├── DatabaseConfig.java          #    Cấu hình kết nối (URL, user, password)
│   │   │   │   └── DatabaseUtil.java            #    Kết nối JDBC, các hàm CRUD (insert, update, delete, select)
│   │   │   │
│   │   │   ├── service/                         # ========== BẠN C & D PHỤ TRÁCH ==========
│   │   │   │   ├── FinanceService.java          # [C] Logic nghiệp vụ chính (CREDIT cho C đã gợi ý trước)
│   │   │   │   │                                #     - Quản lý HashMap<String, Category>
│   │   │   │   │                                #     - Dùng Stream/Lambda để lọc, tính tổng
│   │   │   │   │                                #     - Phối hợp với D để gọi thống kê
│   │   │   │   ├── StatisticsService.java       # [D] Logic thống kê (theo danh mục, theo tháng, số dư TB)
│   │   │   │   └── BudgetManager.java           # [D] Quản lý ngân sách, kiểm tra vượt chi, cảnh báo
│   │   │   │
│   │   │   ├── exception/                       # ========== BẠN C PHỤ TRÁCH ==========
│   │   │   │   ├── InvalidAmountException.java  #    Custom exception: số tiền âm hoặc bằng 0
│   │   │   │   └── DataLoadException.java       #    Custom exception: lỗi khi đọc file JSON
│   │   │   │
│   │   │   ├── util/                            # ========== BẠN C PHỤ TRÁCH ==========
│   │   │   │   └── JsonUtil.java                #    Đọc/ghi file JSON (dùng Gson)
│   │   │   │                                    #    - saveToJson(List<Transaction>)
│   │   │   │                                    #    - loadFromJson(): List<Transaction>
│   │   │   │
│   │   │   └── ui/                              # ========== BẠN B & D PHỤ TRÁCH ==========
│   │   │       ├── MainFrame.java               # [B] JFrame chính, chứa các panel
│   │   │       ├── DashboardPanel.java          # [B] Màn hình 1: Tổng quan (số dư, thu, chi, nút thêm)
│   │   │       ├── HistoryPanel.java            # [B] Màn hình 2: Lịch sử giao dịch (JTable)
│   │   │       ├── AddTransactionDialog.java    # [D] Màn hình 3: Hộp thoại thêm giao dịch
│   │   │       │                                #     - JTextField: số tiền, ghi chú
│   │   │       │                                #     - JComboBox: chọn danh mục (lấy từ FinanceService)
│   │   │       │                                #     - JDatePicker: chọn ngày (nếu cần)
│   │   │       ├── StatisticsPanel.java         # [D] Màn hình 4: Thống kê (biểu đồ, báo cáo theo tháng)
│   │   │       └── BudgetDialog.java            # [D] Hộp thoại thiết lập ngân sách tháng
│   │   │
│   │   └── resources/
│   │       ├── data/                            # ========== BẠN C PHỤ TRÁCH ==========
│   │       │   └── transactions.json            #    File dữ liệu mẫu (nếu cần)
│   │       └── images/                          # [B, D] Icon, hình ảnh cho giao diện (nếu có)
│   │
│   └── test/
│       └── java/com/expensemanager/
│           ├── FinanceServiceTest.java          # [C] Test logic nghiệp vụ
│           ├── DatabaseUtilTest.java            # [A] Test kết nối và CRUD
│           └── JsonUtilTest.java                # [C] Test đọc/ghi file
│
└── docs/                                        # ========== CẢ NHÓM ==========
    ├── BaoCao.docx
    ├── BaoCao.pdf
    ├── ThuyetTrinh.pptx
    └── HuongDanCaiDat.md                        # Hướng dẫn cài đặt chi tiết
```
