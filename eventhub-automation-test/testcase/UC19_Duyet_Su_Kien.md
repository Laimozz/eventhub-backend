# UC19: Duyệt Sự Kiện

## 1. Thông Tin Use Case

- **Mã Use Case:** UC19
- **Tên Use Case:** Phê duyệt / Từ chối sự kiện (Approve or Reject Event)
- **Tác nhân chính (Actor):** Admin (Quản trị viên)
- **Mô tả nghiệp vụ:** Cung cấp cho Admin công cụ kiểm duyệt thông tin chi tiết sự kiện do Organizer gửi lên, đảm bảo tuân thủ tiêu chuẩn chất lượng và nội dung trước khi phát hành vé ra thị trường.
- **Tiền điều kiện (Pre-conditions):** Quản trị viên đã đăng nhập vào hệ thống EventHub.
- **Hậu điều kiện (Post-conditions):** Sự kiện được chuyển sang trạng thái 'Đã duyệt' (APPROVED - được phép mở bán vé công khai) hoặc 'Bị từ chối' (REJECTED). Gửi thông báo kèm kết quả/lý do cho Organizer.

## 2. Danh Sách Test Cases Tổng Hợp

| STT | Mã Test Case | Tên / Mục Tiêu Test Case | Phân Loại | Mức Độ Ưu Tiên |
|:---:|:---|:---|:---|:---:|
| 1 | **TC_UC19_01** | Xem danh sách và chi tiết hồ sơ sự kiện đang chờ duyệt | Functional (Happy Path) | **High** |
| 2 | **TC_UC19_02** | Duyệt sự kiện thành công khi hồ sơ hợp lệ | Functional (Happy Path) | **High** |
| 3 | **TC_UC19_03** | Hủy bỏ thao tác duyệt khi nhấn Hủy ở hộp thoại xác nhận | Functional (Alternative Flow) | **Medium** |
| 4 | **TC_UC19_04** | Từ chối duyệt sự kiện thành công khi nhập đầy đủ lý do từ chối | Functional (Alternative Flow) | **High** |
| 5 | **TC_UC19_05** | Từ chối sự kiện thất bại khi không nhập Lý do từ chối | Validation (Negative) | **High** |
| 6 | **TC_UC19_06** | Hiển thị danh sách trống khi không có sự kiện nào đang chờ duyệt | UI / Boundary | **Low** |
| 7 | **TC_UC19_07** | Organizer nhận thông báo ngay khi sự kiện được Duyệt hoặc Từ chối | Integration | **High** |
| 8 | **TC_UC19_08** | Kiểm tra bảo mật không cho phép duyệt sự kiện khi không có quyền Admin | Security / Authorization | **High** |

---

## 3. Thiết Kế Chi Tiết Từng Test Case

### TC_UC19_01: Xem danh sách và chi tiết hồ sơ sự kiện đang chờ duyệt

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC19_01` |
| **Tên Test Case** | Xem danh sách và chi tiết hồ sơ sự kiện đang chờ duyệt |
| **Use Case liên quan** | UC19 - Phê duyệt / Từ chối sự kiện (Approve or Reject Event) |
| **Phân loại kiểm thử** | Functional (Happy Path) |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Có sự kiện 'Đêm nhạc Acoustic Thu' do Organizer 'Trần Thị B' gửi tạo, trạng thái 'Chờ duyệt'. |
| **Dữ liệu đầu vào (Test Data)** | N/A |
| **Các bước thực hiện** | 1. Admin chọn menu 'Sự kiện chờ duyệt'.<br/>2. Quan sát danh sách sự kiện chờ duyệt.<br/>3. Nhấn xem chi tiết sự kiện 'Đêm nhạc Acoustic Thu'. |
| **Kết quả mong đợi** | Hiển thị đầy đủ hồ sơ thẩm định sự kiện: Tên sự kiện, Nhà tổ chức, Thành phố, Địa chỉ chi tiết, Loại sự kiện, Thời gian bắt đầu/kết thúc, Danh sách khách mời, Danh sách các loại vé (Vé VIP 100 vé, giá 1.500.000đ) cùng 2 nút hành động: 'Duyệt' và 'Từ chối'. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC19_02: Duyệt sự kiện thành công khi hồ sơ hợp lệ

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC19_02` |
| **Tên Test Case** | Duyệt sự kiện thành công khi hồ sơ hợp lệ |
| **Use Case liên quan** | UC19 - Phê duyệt / Từ chối sự kiện (Approve or Reject Event) |
| **Phân loại kiểm thử** | Functional (Happy Path) |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Đang xem chi tiết sự kiện 'Đêm nhạc Acoustic Thu' có trạng thái Chờ duyệt. |
| **Dữ liệu đầu vào (Test Data)** | N/A |
| **Các bước thực hiện** | 1. Tại màn chi tiết sự kiện chờ duyệt, nhấn nút 'Duyệt'.<br/>2. Hộp thoại xác nhận hiển thị 'Bạn có chắc chắn muốn duyệt sự kiện này?'. Nhấn 'Xác nhận'. |
| **Kết quả mong đợi** | Hệ thống cập nhật trạng thái sự kiện thành 'Đã duyệt' (APPROVED). Lưu lại Admin duyệt và thời điểm duyệt. Sự kiện xuất hiện trên trang chủ cho khách hàng mua vé. Hệ thống gửi thông báo chúc mừng tới Organizer. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC19_03: Hủy bỏ thao tác duyệt khi nhấn Hủy ở hộp thoại xác nhận

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC19_03` |
| **Tên Test Case** | Hủy bỏ thao tác duyệt khi nhấn Hủy ở hộp thoại xác nhận |
| **Use Case liên quan** | UC19 - Phê duyệt / Từ chối sự kiện (Approve or Reject Event) |
| **Phân loại kiểm thử** | Functional (Alternative Flow) |
| **Mức độ ưu tiên** | **Medium** |
| **Tiền điều kiện** | Đang mở popup xác nhận duyệt sự kiện. |
| **Dữ liệu đầu vào (Test Data)** | N/A |
| **Các bước thực hiện** | 1. Nhấn nút 'Duyệt'.<br/>2. Tại popup xác nhận, nhấn nút 'Hủy bỏ'. |
| **Kết quả mong đợi** | Popup đóng lại. Trạng thái sự kiện vẫn giữ nguyên là 'Chờ duyệt'. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC19_04: Từ chối duyệt sự kiện thành công khi nhập đầy đủ lý do từ chối

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC19_04` |
| **Tên Test Case** | Từ chối duyệt sự kiện thành công khi nhập đầy đủ lý do từ chối |
| **Use Case liên quan** | UC19 - Phê duyệt / Từ chối sự kiện (Approve or Reject Event) |
| **Phân loại kiểm thử** | Functional (Alternative Flow) |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Hồ sơ sự kiện vi phạm quy định hoặc thiếu thông tin. |
| **Dữ liệu đầu vào (Test Data)** | Lý do từ chối: 'Địa chỉ tổ chức không rõ ràng và hình ảnh sơ đồ chỗ ngồi bị mờ, vui lòng cập nhật lại' |
| **Các bước thực hiện** | 1. Nhấn nút 'Từ chối'.<br/>2. Nhập lý do từ chối vào ô text box.<br/>3. Nhấn 'Xác nhận từ chối'. |
| **Kết quả mong đợi** | Sự kiện chuyển sang trạng thái 'Bị từ chối' (REJECTED). Lưu lý do từ chối vào hệ thống. Gửi thông báo kèm lý do chi tiết cho Organizer để họ chỉnh sửa nộp lại. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC19_05: Từ chối sự kiện thất bại khi không nhập Lý do từ chối

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC19_05` |
| **Tên Test Case** | Từ chối sự kiện thất bại khi không nhập Lý do từ chối |
| **Use Case liên quan** | UC19 - Phê duyệt / Từ chối sự kiện (Approve or Reject Event) |
| **Phân loại kiểm thử** | Validation (Negative) |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Đang ở popup Từ chối sự kiện. |
| **Dữ liệu đầu vào (Test Data)** | Lý do từ chối: '' (để trống) |
| **Các bước thực hiện** | 1. Nhấn 'Từ chối'.<br/>2. Để trống ô lý do từ chối.<br/>3. Nhấn 'Xác nhận từ chối'. |
| **Kết quả mong đợi** | Hệ thống chặn không cho gửi và cảnh báo: 'Vui lòng nhập lý do từ chối sự kiện để thông báo cho Nhà tổ chức'. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC19_06: Hiển thị danh sách trống khi không có sự kiện nào đang chờ duyệt

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC19_06` |
| **Tên Test Case** | Hiển thị danh sách trống khi không có sự kiện nào đang chờ duyệt |
| **Use Case liên quan** | UC19 - Phê duyệt / Từ chối sự kiện (Approve or Reject Event) |
| **Phân loại kiểm thử** | UI / Boundary |
| **Mức độ ưu tiên** | **Low** |
| **Tiền điều kiện** | Toàn bộ sự kiện đã được kiểm duyệt xong. |
| **Dữ liệu đầu vào (Test Data)** | N/A |
| **Các bước thực hiện** | 1. Mở menu 'Sự kiện chờ duyệt'. |
| **Kết quả mong đợi** | Hệ thống hiển thị trạng thái trống: 'Hiện không có sự kiện nào đang chờ duyệt'. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC19_07: Organizer nhận thông báo ngay khi sự kiện được Duyệt hoặc Từ chối

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC19_07` |
| **Tên Test Case** | Organizer nhận thông báo ngay khi sự kiện được Duyệt hoặc Từ chối |
| **Use Case liên quan** | UC19 - Phê duyệt / Từ chối sự kiện (Approve or Reject Event) |
| **Phân loại kiểm thử** | Integration |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Admin vừa hoàn tất Duyệt hoặc Từ chối sự kiện. |
| **Dữ liệu đầu vào (Test Data)** | N/A |
| **Các bước thực hiện** | 1. Đăng nhập tài khoản của Organizer tạo sự kiện.<br/>2. Kiểm tra chuông thông báo và danh sách 'Sự kiện của tôi'. |
| **Kết quả mong đợi** | Organizer nhận được thông báo: 'Sự kiện Đêm nhạc Acoustic Thu của bạn đã được duyệt thành công' (hoặc 'bị từ chối vì lý do...'). Trạng thái sự kiện trong mục của tôi hiển thị khớp. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC19_08: Kiểm tra bảo mật không cho phép duyệt sự kiện khi không có quyền Admin

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC19_08` |
| **Tên Test Case** | Kiểm tra bảo mật không cho phép duyệt sự kiện khi không có quyền Admin |
| **Use Case liên quan** | UC19 - Phê duyệt / Từ chối sự kiện (Approve or Reject Event) |
| **Phân loại kiểm thử** | Security / Authorization |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Người dùng đăng nhập với vai trò Customer hoặc Organizer, cố tình gửi POST request đến `/api/admin/events/{id}/approve`. |
| **Dữ liệu đầu vào (Test Data)** | API: `POST /api/admin/events/1/approve` |
| **Các bước thực hiện** | 1. Dùng token của Customer gọi trực tiếp API duyệt sự kiện. |
| **Kết quả mong đợi** | Hệ thống chặn ngay từ tầng Security Filter, trả về mã lỗi HTTP 403 Forbidden: 'Access Denied: Quyền truy cập bị từ chối'. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

