# UC22: Báo Cáo Thống Kê Ban Tổ Chức [BỔ SUNG GIAO DIỆN]

> [!NOTE]
> **Use Case bổ sung**: Phục vụ kiểm thử tính năng của nút "Xem thống kê" trong màn hình chi tiết sự kiện (được đề cập trong UC14 của tài liệu nghiệp vụ).

## 1. Thông Tin Use Case
- **Mã Use Case:** UC22
- **Tên Use Case:** Báo cáo & Thống kê doanh thu, vé bán của Nhà tổ chức sự kiện
- **Tác nhân chính (Actor):** Organizer
- **Mô tả nghiệp vụ:** Cung cấp số liệu tổng quan và chi tiết về tiến độ bán vé, doanh thu thực tế, tỷ lệ check-in và biểu đồ doanh số theo từng mốc thời gian hoặc theo từng sự kiện cụ thể.
- **Tiền điều kiện:** Nhà tổ chức đã đăng nhập vào hệ thống EventHub.
- **Hậu điều kiện:** Dữ liệu báo cáo và biểu đồ được nạp đầy đủ, hỗ trợ xuất báo cáo file CSV.

## 2. Danh Sách Test Cases

| STT | Mã Test Case | Tên / Mục Tiêu Test Case | Phân Loại | Mức Độ Ưu Tiên |
|:---:|:---|:---|:---|:---:|
| 1 | **TC_UC22_01** | Xem tổng quan báo cáo thống kê doanh thu, vé bán và tỷ lệ check-in | Functional (Happy Path) | **High** |
| 2 | **TC_UC22_02** | Lọc dữ liệu báo cáo thống kê theo sự kiện cụ thể từ danh sách | Functional / Filter | **High** |
| 3 | **TC_UC22_03** | Chuyển đổi các mốc thời gian lọc (7 ngày, 30 ngày, Tất cả) | Functional / Filter | **Medium** |
| 4 | **TC_UC22_04** | Kiểm tra hoạt động của nút Xuất file CSV và nút Làm mới dữ liệu | Functional / Export | **Medium** |

---

## 3. Thiết Kế Chi Tiết Từng Test Case

### TC_UC22_01: Xem tổng quan báo cáo thống kê doanh thu, vé bán và tỷ lệ check-in
- **Tiền điều kiện:** Đã đăng nhập tài khoản Organizer.
- **Các bước thực hiện:**
  1. Truy cập trang `/organizer/reports`.
  2. Quan sát tiêu đề và các khối chỉ số KPI.
- **Kết quả mong đợi:** Hiển thị tiêu đề "Báo cáo & Thống kê", hiển thị các thẻ tổng doanh thu, vé đã bán và tỷ lệ check-in.

### TC_UC22_02: Lọc dữ liệu báo cáo thống kê theo sự kiện cụ thể từ danh sách
- **Tiền điều kiện:** Đã đăng nhập tài khoản Organizer.
- **Các bước thực hiện:**
  1. Mở trang `/organizer/reports`.
  2. Chọn một sự kiện từ dropdown "Chọn sự kiện lọc".
- **Kết quả mong đợi:** Dữ liệu thống kê cập nhật tương ứng với sự kiện đã chọn, không bị lỗi dữ liệu hay crash trang.

### TC_UC22_03: Chuyển đổi các mốc thời gian lọc (7 ngày, 30 ngày, Tất cả)
- **Tiền điều kiện:** Đã đăng nhập tài khoản Organizer.
- **Các bước thực hiện:**
  1. Click nút "7 ngày".
  2. Click nút "Tất cả".
- **Kết quả mong đợi:** Các biểu đồ và số liệu KPI thay đổi tương ứng theo khoảng thời gian đã chọn.

### TC_UC22_04: Kiểm tra hoạt động của nút Xuất file CSV và nút Làm mới dữ liệu
- **Tiền điều kiện:** Đã đăng nhập tài khoản Organizer.
- **Các bước thực hiện:**
  1. Kiểm tra trạng thái nút "Xuất file CSV".
  2. Click nút "Làm mới".
- **Kết quả mong đợi:** Nút CSV ở trạng thái sẵn sàng, bấm Làm mới gọi lại API và cập nhật dữ liệu mượt mà.

