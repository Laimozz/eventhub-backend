# UC14: Quản Lý Sự Kiện (Hủy Sự Kiện)

## 1. Thông Tin Use Case

- **Mã Use Case:** UC14
- **Tên Use Case:** Quản lý sự kiện - Hủy sự kiện đã công bố (Cancel Event)
- **Tác nhân chính (Actor):** Organizer (Nhà tổ chức)
- **Mô tả nghiệp vụ:** Cho phép Nhà tổ chức hủy bỏ một sự kiện đã được duyệt do lý do bất khả kháng, cung cấp lý do hủy để phục vụ công tác hoàn tiền và thông báo người tham dự.
- **Tiền điều kiện (Pre-conditions):** Nhà tổ chức đã đăng nhập, sự kiện đang ở trạng thái 'Đã duyệt' và chưa kết thúc.
- **Hậu điều kiện (Post-conditions):** Sự kiện chuyển sang trạng thái 'Đã hủy' (CANCELLED), dừng bán toàn bộ vé, kích hoạt tiến trình hoàn tiền cho Admin và gửi thông báo khẩn cấp đến toàn bộ khách hàng đã mua vé.

## 2. Danh Sách Test Cases Tổng Hợp

| STT | Mã Test Case | Tên / Mục Tiêu Test Case | Phân Loại | Mức Độ Ưu Tiên |
|:---:|:---|:---|:---|:---:|
| 1 | **TC_UC14_01** | Hủy sự kiện thành công khi nhập đầy đủ lý do và xác nhận hủy | Functional (Happy Path) | **High** |
| 2 | **TC_UC14_02** | Hủy bỏ thao tác khi nhấn Hủy ở hộp thoại xác nhận | Functional (Alternative Flow) | **Medium** |
| 3 | **TC_UC14_03** | Hủy sự kiện thất bại khi không nhập Lý do hủy | Validation (Negative) | **High** |
| 4 | **TC_UC14_04** | Ẩn nút 'Hủy sự kiện' khi sự kiện đang ở trạng thái Chờ duyệt | Business Rule / UI | **Medium** |
| 5 | **TC_UC14_05** | Không thể hủy sự kiện đã diễn ra trong quá khứ hoặc đã bị hủy trước đó | Business Rule | **High** |
| 6 | **TC_UC14_06** | Lọc danh sách sự kiện của tôi theo trạng thái Đã duyệt | Functional | **Medium** |
| 7 | **TC_UC14_07** | Hiển thị thông báo khi Organizer chưa có sự kiện nào hoặc không có sự kiện khớp bộ lọc | UI / Boundary | **Low** |
| 8 | **TC_UC14_08** | Khách hàng không thể tiếp tục đặt vé cho sự kiện sau khi sự kiện đã bị hủy | Integration / Business Rule | **High** |
| 9 | **TC_UC14_09** | Khách hàng đã mua vé nhận được thông báo hủy sự kiện kèm lý do | Integration | **High** |

---

## 3. Thiết Kế Chi Tiết Từng Test Case

### TC_UC14_01: Hủy sự kiện thành công khi nhập đầy đủ lý do và xác nhận hủy

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC14_01` |
| **Tên Test Case** | Hủy sự kiện thành công khi nhập đầy đủ lý do và xác nhận hủy |
| **Use Case liên quan** | UC14 - Quản lý sự kiện - Hủy sự kiện đã công bố (Cancel Event) |
| **Phân loại kiểm thử** | Functional (Happy Path) |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Sự kiện 'Đêm nhạc Acoustic Thu' ở trạng thái Đã duyệt, đã bán được 65 vé. |
| **Dữ liệu đầu vào (Test Data)** | Lý do hủy: 'Sự kiện xảy ra sự cố kỹ thuật về địa điểm tổ chức không thể khắc phục' |
| **Các bước thực hiện** | 1. Vào menu 'Sự kiện của tôi', chọn lọc theo trạng thái 'Đã duyệt'.<br/>2. Chọn xem chi tiết sự kiện 'Đêm nhạc Acoustic Thu'.<br/>3. Nhấn nút 'Hủy sự kiện'.<br/>4. Hệ thống hiển thị cảnh báo hoàn tiền toàn bộ vé đã bán kèm ô nhập lý do. Nhập lý do theo dữ liệu thử nghiệm.<br/>5. Nhấn 'Xác nhận hủy'.<br/>6. Hộp thoại xác nhận lần cuối hiển thị: 'Bạn có chắc chắn muốn hủy sự kiện này?'. Nhấn 'Xác nhận'. |
| **Kết quả mong đợi** | Hệ thống cập nhật trạng thái sự kiện thành 'Đã hủy'. Lưu lại lý do hủy. Tạo danh sách các giao dịch cần hoàn tiền cho Admin. Gửi thông báo đến Admin và gửi thông báo/email đến 65 khách hàng đã mua vé. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC14_02: Hủy bỏ thao tác khi nhấn Hủy ở hộp thoại xác nhận

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC14_02` |
| **Tên Test Case** | Hủy bỏ thao tác khi nhấn Hủy ở hộp thoại xác nhận |
| **Use Case liên quan** | UC14 - Quản lý sự kiện - Hủy sự kiện đã công bố (Cancel Event) |
| **Phân loại kiểm thử** | Functional (Alternative Flow) |
| **Mức độ ưu tiên** | **Medium** |
| **Tiền điều kiện** | Đang mở hộp thoại xác nhận lần cuối 'Bạn có chắc chắn muốn hủy sự kiện này?'. |
| **Dữ liệu đầu vào (Test Data)** | N/A |
| **Các bước thực hiện** | 1. Nhập lý do hủy và ấn 'Xác nhận hủy'.<br/>2. Tại popup xác nhận lần cuối, nhấn nút 'Hủy bỏ' hoặc nút đóng (x). |
| **Kết quả mong đợi** | Hộp thoại đóng lại. Sự kiện vẫn giữ nguyên trạng thái 'Đã duyệt', không có thông báo hủy nào được phát đi. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC14_03: Hủy sự kiện thất bại khi không nhập Lý do hủy

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC14_03` |
| **Tên Test Case** | Hủy sự kiện thất bại khi không nhập Lý do hủy |
| **Use Case liên quan** | UC14 - Quản lý sự kiện - Hủy sự kiện đã công bố (Cancel Event) |
| **Phân loại kiểm thử** | Validation (Negative) |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Đang ở popup nhập lý do hủy sự kiện. |
| **Dữ liệu đầu vào (Test Data)** | Lý do hủy: '' (để trống hoặc toàn dấu cách) |
| **Các bước thực hiện** | 1. Mở form hủy sự kiện.<br/>2. Để trống ô lý do hủy.<br/>3. Nhấn 'Xác nhận hủy'. |
| **Kết quả mong đợi** | Hệ thống chặn submit và báo lỗi: 'Vui lòng nhập lý do hủy sự kiện để thông báo cho khách hàng'. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC14_04: Ẩn nút 'Hủy sự kiện' khi sự kiện đang ở trạng thái Chờ duyệt

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC14_04` |
| **Tên Test Case** | Ẩn nút 'Hủy sự kiện' khi sự kiện đang ở trạng thái Chờ duyệt |
| **Use Case liên quan** | UC14 - Quản lý sự kiện - Hủy sự kiện đã công bố (Cancel Event) |
| **Phân loại kiểm thử** | Business Rule / UI |
| **Mức độ ưu tiên** | **Medium** |
| **Tiền điều kiện** | Sự kiện có trạng thái 'Chờ duyệt' (PENDING_APPROVAL). |
| **Dữ liệu đầu vào (Test Data)** | N/A |
| **Các bước thực hiện** | 1. Mở chi tiết sự kiện đang chờ duyệt. |
| **Kết quả mong đợi** | Giao diện chỉ hiển thị nút 'Sửa', ẩn các nút 'Hủy sự kiện', 'Quản lý Staff' và 'Xem thống kê' do sự kiện chưa được công bố mở bán. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC14_05: Không thể hủy sự kiện đã diễn ra trong quá khứ hoặc đã bị hủy trước đó

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC14_05` |
| **Tên Test Case** | Không thể hủy sự kiện đã diễn ra trong quá khứ hoặc đã bị hủy trước đó |
| **Use Case liên quan** | UC14 - Quản lý sự kiện - Hủy sự kiện đã công bố (Cancel Event) |
| **Phân loại kiểm thử** | Business Rule |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Sự kiện đã kết thúc thời gian diễn ra hoặc đã ở trạng thái CANCELLED. |
| **Dữ liệu đầu vào (Test Data)** | N/A |
| **Các bước thực hiện** | 1. Mở chi tiết sự kiện đã kết thúc. |
| **Kết quả mong đợi** | Nút 'Hủy sự kiện' bị ẩn hoặc disabled. Nếu cố tình gọi API hủy trực tiếp bằng tool, server trả về mã lỗi 400 Bad Request: 'Không thể hủy sự kiện đã kết thúc hoặc đã hủy'. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC14_06: Lọc danh sách sự kiện của tôi theo trạng thái Đã duyệt

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC14_06` |
| **Tên Test Case** | Lọc danh sách sự kiện của tôi theo trạng thái Đã duyệt |
| **Use Case liên quan** | UC14 - Quản lý sự kiện - Hủy sự kiện đã công bố (Cancel Event) |
| **Phân loại kiểm thử** | Functional |
| **Mức độ ưu tiên** | **Medium** |
| **Tiền điều kiện** | Organizer có các sự kiện ở nhiều trạng thái: Chờ duyệt, Đã duyệt, Đã hủy. |
| **Dữ liệu đầu vào (Test Data)** | Bộ lọc: 'Đã duyệt' |
| **Các bước thực hiện** | 1. Vào danh sách 'Sự kiện của tôi'.<br/>2. Chọn lọc theo trạng thái 'Đã duyệt'. |
| **Kết quả mong đợi** | Danh sách chỉ hiển thị các sự kiện có trạng thái Đã duyệt ('Hội thảo Công nghệ AI', 'Workshop Nhiếp ảnh', 'Đêm nhạc Acoustic Thu'). |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC14_07: Hiển thị thông báo khi Organizer chưa có sự kiện nào hoặc không có sự kiện khớp bộ lọc

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC14_07` |
| **Tên Test Case** | Hiển thị thông báo khi Organizer chưa có sự kiện nào hoặc không có sự kiện khớp bộ lọc |
| **Use Case liên quan** | UC14 - Quản lý sự kiện - Hủy sự kiện đã công bố (Cancel Event) |
| **Phân loại kiểm thử** | UI / Boundary |
| **Mức độ ưu tiên** | **Low** |
| **Tiền điều kiện** | Lọc theo trạng thái 'Từ chối' nhưng không có sự kiện nào bị từ chối. |
| **Dữ liệu đầu vào (Test Data)** | N/A |
| **Các bước thực hiện** | 1. Chọn lọc theo trạng thái không có dữ liệu. |
| **Kết quả mong đợi** | Hệ thống hiển thị bảng trống kèm thông điệp: 'Không tìm thấy sự kiện nào khớp với bộ lọc'. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC14_08: Khách hàng không thể tiếp tục đặt vé cho sự kiện sau khi sự kiện đã bị hủy

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC14_08` |
| **Tên Test Case** | Khách hàng không thể tiếp tục đặt vé cho sự kiện sau khi sự kiện đã bị hủy |
| **Use Case liên quan** | UC14 - Quản lý sự kiện - Hủy sự kiện đã công bố (Cancel Event) |
| **Phân loại kiểm thử** | Integration / Business Rule |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Sự kiện vừa chuyển trạng thái sang 'Đã hủy'. |
| **Dữ liệu đầu vào (Test Data)** | N/A |
| **Các bước thực hiện** | 1. Truy cập vào trang chi tiết công khai của sự kiện đó với vai trò Customer. |
| **Kết quả mong đợi** | Nút 'Mua vé ngay' bị ẩn hoặc thay thế bằng thông báo lớn màu đỏ: 'SỰ KIỆN NÀY ĐÃ BỊ HỦY BỞI BAN TỔ CHỨC'. Không thể tạo đơn booking mới. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC14_09: Khách hàng đã mua vé nhận được thông báo hủy sự kiện kèm lý do

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC14_09` |
| **Tên Test Case** | Khách hàng đã mua vé nhận được thông báo hủy sự kiện kèm lý do |
| **Use Case liên quan** | UC14 - Quản lý sự kiện - Hủy sự kiện đã công bố (Cancel Event) |
| **Phân loại kiểm thử** | Integration |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Customer đã mua vé sự kiện 'Đêm nhạc Acoustic Thu'. |
| **Dữ liệu đầu vào (Test Data)** | N/A |
| **Các bước thực hiện** | 1. Đăng nhập tài khoản Customer đã mua vé.<br/>2. Kiểm tra chuông thông báo và email. |
| **Kết quả mong đợi** | Khách hàng nhận được thông báo: 'Sự kiện Đêm nhạc Acoustic Thu đã bị hủy vì lý do: Sự kiện xảy ra sự cố kỹ thuật... Tiền vé sẽ được hoàn lại tự động trong thời gian sớm nhất'. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

