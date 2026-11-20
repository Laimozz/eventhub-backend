# UC24: Báo Cáo Thống Kê Tổng Quan Admin [BỔ SUNG GIAO DIỆN]

> [!NOTE]
> **Use Case bổ sung**: Phục vụ kiểm thử bảng điều khiển tổng quan Admin (Dashboard / Statistics) hỗ trợ quan sát vận hành hệ thống.

## 1. Thông Tin Use Case
- **Mã Use Case:** UC24
- **Tên Use Case:** Báo cáo & Thống kê tổng quan hoạt động toàn hệ thống
- **Tác nhân chính (Actor):** Admin
- **Mô tả nghiệp vụ:** Cung cấp cho Quản trị viên số liệu tài chính toàn sàn (doanh thu thành công, tổng hoàn tiền, hoa hồng nền tảng), số lượng sự kiện, số lượng người dùng và biểu đồ xu hướng.
- **Tiền điều kiện:** Quản trị viên đã đăng nhập hệ thống.
- **Hậu điều kiện:** Dữ liệu tài chính và số liệu vận hành hệ thống được hiển thị đầy đủ, chính xác theo thời gian thực.

## 2. Danh Sách Test Cases

| STT | Mã Test Case | Tên / Mục Tiêu Test Case | Phân Loại | Mức Độ Ưu Tiên |
|:---:|:---|:---|:---|:---:|
| 1 | **TC_UC24_01** | Xem bảng chỉ số thống kê tài chính và số lượng sự kiện toàn sàn | Functional (Happy Path) | **High** |
| 2 | **TC_UC24_02** | Điều hướng nhanh từ Dashboard sang các module quản trị (Người dùng, Danh mục, Rút tiền) | Functional / Navigation | **Medium** |

---

## 3. Thiết Kế Chi Tiết Từng Test Case

### TC_UC24_01: Xem bảng chỉ số thống kê tài chính và số lượng sự kiện toàn sàn
- **Tiền điều kiện:** Đã đăng nhập tài khoản Admin.
- **Các bước thực hiện:**
  1. Mở trang `/admin/statistics`.
  2. Quan sát các thẻ thống kê số liệu.
- **Kết quả mong đợi:** Hiển thị tiêu đề "Thống kê tổng quan", hiển thị các thẻ: "Tiền vé thanh toán thành công", "Doanh thu nền tảng", "Tổng số sự kiện".

### TC_UC24_02: Điều hướng nhanh từ Dashboard sang các module quản trị
- **Tiền điều kiện:** Màn hình Dashboard Admin đang mở.
- **Các bước thực hiện:**
  1. Click vào link điều hướng "Quản lý người dùng".
- **Kết quả mong đợi:** Điều hướng thành công đến trang `/admin/users`.

