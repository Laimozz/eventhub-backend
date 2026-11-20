# UC08: Xem Chi Tiết Booking

## 1. Thông Tin Use Case

- **Mã Use Case:** UC08
- **Tên Use Case:** Xem chi tiết đơn đặt vé (Booking Detail)
- **Tác nhân chính (Actor):** Customer, Staff
- **Mô tả nghiệp vụ:** Cho phép người dùng tra cứu lịch sử các lần đặt vé, trạng thái thanh toán và xem thông tin chi tiết của từng booking.
- **Tiền điều kiện (Pre-conditions):** Người dùng đã đăng nhập vào hệ thống EventHub.
- **Hậu điều kiện (Post-conditions):** Hệ thống hiển thị danh sách lịch sử đặt vé và toàn bộ thông tin chi tiết của đơn booking được chọn.

## 2. Danh Sách Test Cases Tổng Hợp

| STT | Mã Test Case | Tên / Mục Tiêu Test Case | Phân Loại | Mức Độ Ưu Tiên |
|:---:|:---|:---|:---|:---:|
| 1 | **TC_UC08_01** | Xem danh sách lịch sử đặt vé thành công | Functional (Happy Path) | **High** |
| 2 | **TC_UC08_02** | Xem chi tiết một đơn đặt vé Đã thanh toán thành công | Functional (Happy Path) | **High** |
| 3 | **TC_UC08_03** | Xem chi tiết đơn booking có trạng thái Đã hủy hoặc Hết hạn | Functional | **Medium** |
| 4 | **TC_UC08_04** | Hiển thị trạng thái trống khi người dùng chưa từng đặt vé nào | UI / Boundary | **Low** |
| 5 | **TC_UC08_05** | Kiểm tra phân trang lịch sử đặt vé | UI / Functional | **Medium** |
| 6 | **TC_UC08_06** | Nhấn nút 'Xem sự kiện' từ trang chi tiết booking | Navigation | **Low** |
| 7 | **TC_UC08_07** | Bảo mật: Không thể xem chi tiết booking của người dùng khác bằng cách sửa ID trên URL | Security / Authorization (IDOR) | **High** |

---

## 3. Thiết Kế Chi Tiết Từng Test Case

### TC_UC08_01: Xem danh sách lịch sử đặt vé thành công

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC08_01` |
| **Tên Test Case** | Xem danh sách lịch sử đặt vé thành công |
| **Use Case liên quan** | UC08 - Xem chi tiết đơn đặt vé (Booking Detail) |
| **Phân loại kiểm thử** | Functional (Happy Path) |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Người dùng có ít nhất 2 booking trong tài khoản. |
| **Dữ liệu đầu vào (Test Data)** | N/A |
| **Các bước thực hiện** | 1. Nhấp vào nút 'Tài khoản' trên thanh điều hướng.<br/>2. Chọn 'Booking của tôi'.<br/>3. Quan sát danh sách hiển thị. |
| **Kết quả mong đợi** | Hệ thống hiển thị danh sách các booking gồm: Mã booking (ví dụ #001, #002), Trạng thái (Đã thanh toán / Đã hủy), Ảnh sự kiện, Tên sự kiện, Thời gian diễn ra, Thành phố, Số lượng vé, Tổng tiền và nút 'Xem chi tiết'. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC08_02: Xem chi tiết một đơn đặt vé Đã thanh toán thành công

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC08_02` |
| **Tên Test Case** | Xem chi tiết một đơn đặt vé Đã thanh toán thành công |
| **Use Case liên quan** | UC08 - Xem chi tiết đơn đặt vé (Booking Detail) |
| **Phân loại kiểm thử** | Functional (Happy Path) |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Tài khoản có booking #001 với trạng thái 'Đã thanh toán'. |
| **Dữ liệu đầu vào (Test Data)** | Mã booking: '#001' |
| **Các bước thực hiện** | 1. Tại danh sách booking, nhấn nút 'Xem chi tiết' tại booking #001.<br/>2. Quan sát trang Chi tiết đặt vé. |
| **Kết quả mong đợi** | Hiển thị đầy đủ: Trạng thái 'Đã thanh toán', Mã booking #001, Thời gian đặt lúc 20/09/2026 10:35, Thông tin sự kiện (Tên, ngày, giờ, địa điểm, nút Xem sự kiện), Bảng thông tin vé (Loại vé, số lượng, đơn giá, thành tiền, nút 'Xem vé điện tử' cho từng vé), Tổng tiền 1.000.000đ, Thông tin thanh toán (Phương thức VNPAY, thời gian, trạng thái Thành công). |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC08_03: Xem chi tiết đơn booking có trạng thái Đã hủy hoặc Hết hạn

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC08_03` |
| **Tên Test Case** | Xem chi tiết đơn booking có trạng thái Đã hủy hoặc Hết hạn |
| **Use Case liên quan** | UC08 - Xem chi tiết đơn đặt vé (Booking Detail) |
| **Phân loại kiểm thử** | Functional |
| **Mức độ ưu tiên** | **Medium** |
| **Tiền điều kiện** | Tài khoản có booking bị hủy do hết thời gian thanh toán. |
| **Dữ liệu đầu vào (Test Data)** | Booking trạng thái 'ĐÃ HỦY' |
| **Các bước thực hiện** | 1. Tìm booking có trạng thái 'Đã hủy'.<br/>2. Nhấn 'Xem chi tiết'. |
| **Kết quả mong đợi** | Màn hình chi tiết hiển thị trạng thái 'Đã hủy', các nút 'Xem vé điện tử' bị vô hiệu hóa hoặc ẩn, hiển thị lý do hủy (hết hạn giữ chỗ/người dùng hủy). |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC08_04: Hiển thị trạng thái trống khi người dùng chưa từng đặt vé nào

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC08_04` |
| **Tên Test Case** | Hiển thị trạng thái trống khi người dùng chưa từng đặt vé nào |
| **Use Case liên quan** | UC08 - Xem chi tiết đơn đặt vé (Booking Detail) |
| **Phân loại kiểm thử** | UI / Boundary |
| **Mức độ ưu tiên** | **Low** |
| **Tiền điều kiện** | Tài khoản người dùng mới tạo, chưa thực hiện giao dịch mua vé nào. |
| **Dữ liệu đầu vào (Test Data)** | Tài khoản mới |
| **Các bước thực hiện** | 1. Đăng nhập tài khoản mới.<br/>2. Vào menu 'Booking của tôi'. |
| **Kết quả mong đợi** | Hệ thống hiển thị giao diện trống: 'Bạn chưa có đơn đặt vé nào' kèm nút 'Khám phá sự kiện ngay' chuyển hướng ra trang chủ. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC08_05: Kiểm tra phân trang lịch sử đặt vé

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC08_05` |
| **Tên Test Case** | Kiểm tra phân trang lịch sử đặt vé |
| **Use Case liên quan** | UC08 - Xem chi tiết đơn đặt vé (Booking Detail) |
| **Phân loại kiểm thử** | UI / Functional |
| **Mức độ ưu tiên** | **Medium** |
| **Tiền điều kiện** | Tài khoản có nhiều hơn 5 đơn đặt vé. |
| **Dữ liệu đầu vào (Test Data)** | N/A |
| **Các bước thực hiện** | 1. Cuộn xuống cuối trang danh sách booking.<br/>2. Nhấn vào các số trang 1, 2, ... hoặc mũi tên chuyển trang. |
| **Kết quả mong đợi** | Dữ liệu được phân trang mượt mà, chuyển trang đúng và hiển thị các đơn booking tương ứng. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC08_06: Nhấn nút 'Xem sự kiện' từ trang chi tiết booking

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC08_06` |
| **Tên Test Case** | Nhấn nút 'Xem sự kiện' từ trang chi tiết booking |
| **Use Case liên quan** | UC08 - Xem chi tiết đơn đặt vé (Booking Detail) |
| **Phân loại kiểm thử** | Navigation |
| **Mức độ ưu tiên** | **Low** |
| **Tiền điều kiện** | Đang xem chi tiết booking #001. |
| **Dữ liệu đầu vào (Test Data)** | N/A |
| **Các bước thực hiện** | 1. Tại mục thông tin sự kiện của trang chi tiết booking, nhấn nút 'Xem sự kiện'. |
| **Kết quả mong đợi** | Chuyển hướng chính xác đến trang công khai chi tiết của sự kiện đó. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC08_07: Bảo mật: Không thể xem chi tiết booking của người dùng khác bằng cách sửa ID trên URL

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC08_07` |
| **Tên Test Case** | Bảo mật: Không thể xem chi tiết booking của người dùng khác bằng cách sửa ID trên URL |
| **Use Case liên quan** | UC08 - Xem chi tiết đơn đặt vé (Booking Detail) |
| **Phân loại kiểm thử** | Security / Authorization (IDOR) |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | User A có booking ID 100, User B đăng nhập và thử truy cập URL `/bookings/100`. |
| **Dữ liệu đầu vào (Test Data)** | URL: '/bookings/100' |
| **Các bước thực hiện** | 1. Đăng nhập bằng tài khoản User B.<br/>2. Gõ trực tiếp trên thanh địa chỉ trình duyệt đường dẫn xem booking của User A. |
| **Kết quả mong đợi** | Hệ thống kiểm tra quyền sở hữu (HTTP 403 Forbidden hoặc 404 Not Found), từ chối hiển thị và thông báo: 'Bạn không có quyền truy cập đơn hàng này'. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

