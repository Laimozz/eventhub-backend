# UC23: Quản Lý Sự Kiện - Chỉnh Sửa Sự Kiện [BỔ SUNG GIAO DIỆN]

> [!NOTE]
> **Use Case bổ sung**: Phục vụ kiểm thử tính năng của nút "Sửa" sự kiện (được đề cập trong UC14 của tài liệu nghiệp vụ khi sự kiện đang ở trạng thái Chờ duyệt hoặc Bị từ chối).

## 1. Thông Tin Use Case
- **Mã Use Case:** UC23
- **Tên Use Case:** Chỉnh sửa thông tin sự kiện (Edit Event)
- **Tác nhân chính (Actor):** Organizer
- **Mô tả nghiệp vụ:** Cho phép Nhà tổ chức sự kiện cập nhật các thông tin cơ bản (mô tả, địa điểm, banner, giá vé) cho sự kiện trước thời điểm diễn ra.
- **Tiền điều kiện:** Nhà tổ chức đã đăng nhập và sở hữu sự kiện cần chỉnh sửa.
- **Hậu điều kiện:** Thông tin sự kiện được cập nhật thành công trong cơ sở dữ liệu.

## 2. Danh Sách Test Cases

| STT | Mã Test Case | Tên / Mục Tiêu Test Case | Phân Loại | Mức Độ Ưu Tiên |
|:---:|:---|:---|:---|:---:|
| 1 | **TC_UC23_01** | Truy cập trang chỉnh sửa sự kiện và nạp thông tin cũ thành công | Functional (Happy Path) | **High** |
| 2 | **TC_UC23_02** | Cập nhật thông tin mô tả và địa chỉ sự kiện thành công | Functional (Happy Path) | **High** |
| 3 | **TC_UC23_03** | Chỉnh sửa thất bại khi xóa trắng tên sự kiện bắt buộc | Validation (Negative) | **Medium** |

---

## 3. Thiết Kế Chi Tiết Từng Test Case

### TC_UC23_01: Truy cập trang chỉnh sửa sự kiện và nạp thông tin cũ thành công
- **Tiền điều kiện:** Đã đăng nhập tài khoản Organizer, sự kiện ở trạng thái có thể chỉnh sửa (`canEdit=true`).
- **Các bước thực hiện:**
  1. Mở trang `/organizer/events`.
  2. Bấm vào biểu tượng chỉnh sửa (Pencil icon) của sự kiện hoặc truy cập `/organizer/events/:id/edit/details`.
- **Kết quả mong đợi:** Điều hướng đến trang chỉnh sửa sự kiện, nạp đầy đủ thông tin cũ của sự kiện vào các ô nhập.

### TC_UC23_02: Cập nhật thông tin mô tả và địa chỉ sự kiện thành công
- **Tiền điều kiện:** Màn hình chỉnh sửa sự kiện đang mở.
- **Các bước thực hiện:**
  1. Thay đổi thông tin địa chỉ tại ô `address`.
  2. Nhấn nút "Tiếp tục" hoặc "Lưu".
- **Kết quả mong đợi:** Thông tin được lưu thành công, không xuất hiện thông báo lỗi.

### TC_UC23_03: Chỉnh sửa thất bại khi xóa trắng tên sự kiện bắt buộc
- **Tiền điều kiện:** Màn hình chỉnh sửa sự kiện đang mở.
- **Các bước thực hiện:**
  1. Xóa sạch dữ liệu trong ô "Tên sự kiện" (`name`).
  2. Nhấn nút "Tiếp tục".
- **Kết quả mong đợi:** Hệ thống chặn lại ở Bước 1 và cảnh báo lỗi trường tên không được để trống.

