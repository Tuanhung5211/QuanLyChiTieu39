# Quản Lý Chi Tiêu Mini (Expense Manager)
Ứng dụng quản lý tài chính cá nhân đơn giản được xây dựng bằng ngôn ngữ Java và thư viện Swing. Đây là dự án nền tảng để thực hành tư duy Lập trình hướng đối tượng (OOP) và quản lý dự án với Maven.

## Chức năng chính
Quản lý Thu nhập: Thêm các khoản thu từ nhiều nguồn khác nhau (lương, học bổng, tiền được cho...).

Quản lý Chi tiêu: Ghi chép các khoản chi, mỗi khoản được gắn với một danh mục cụ thể (ăn uống, học tập, đi lại, giải trí...).

Lưu vết thời gian thực: Tự động ghi lại chính xác thời điểm (ngày, giờ) phát sinh giao dịch.

Tính toán số dư: Tự động tổng kết tổng thu, tổng chi và hiển thị số dư khả dụng ngay trên giao diện chính.

Giao diện trực quan: Tương tác trực tiếp thông qua cửa sổ ứng dụng (Java Swing), dễ sử dụng cho người mới bắt đầu.

## 🛠 Công nghệ sử dụng
Ngôn ngữ: Java (JDK 17+)

Quản lý dự án: Apache Maven

Giao diện (UI): Java Swing

Kiến trúc: Phân tách rõ ràng giữa Entity (dữ liệu) và Service (logic xử lý), giúp dễ bảo trì và mở rộng.

## 📁 Cấu trúc dự án (dự kiến)
```text
ExpenseManager/
├── src/
│   ├── main/
│   │   ├── java/com/expensemanager/
│   │   │   ├── entity/          # Chứa các lớp dữ liệu
│   │   │   │   ├── Transaction.java   (id, số tiền, loại, danh mục, ngày giờ, ghi chú)
│   │   │   │   └── Category.java      (id, tên danh mục, loại thu/chi)
│   │   │   ├── service/         # Chứa logic xử lý nghiệp vụ
│   │   │   │   └── FinanceService.java (quản lý danh sách giao dịch, tính số dư)
│   │   │   ├── ui/              # Giao diện người dùng (Swing)
│   │   │   │   ├── MainFrame.java     (cửa sổ chính)
│   │   │   │   └── AddDialog.java     (hộp thoại thêm giao dịch)
│   │   │   └── MainApp.java     # Điểm khởi chạy chương trình
│   │   └── resources/           # (tùy chọn) file cấu hình, icons...
│   └── test/java/...            # (mở rộng) Kiểm thử đơn vị
└── pom.xml                      # File cấu hình Maven
```
