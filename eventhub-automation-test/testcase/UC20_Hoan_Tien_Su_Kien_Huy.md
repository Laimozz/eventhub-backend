# UC20: Hoàn Tiền Cho Khách Hàng Với Sự Kiện Hủy

## 1. Thông Tin Use Case

- **Mã Use Case:** UC20
- **Tên Use Case:** Hoàn tiền cho khách hàng với sự kiện hủy (Refund for Cancelled Event)
- **Tác nhân chính (Actor):** Admin (Quản trị viên)
- **Mô tả nghiệp vụ:** Cung cấp cho Admin tính năng kiểm tra danh sách các giao dịch thanh toán vé của sự kiện bị hủy và kích hoạt tiến trình hoàn tiền hàng loạt một cách an toàn, minh bạch.
- **Tiền điều kiện (Pre-conditions):** Admin đã đăng nhập, sự kiện đã bị hủy bởi Nhà tổ chức (Trạng thái sự kiện = 'Đã hủy').
- **Hậu điều kiện (Post-conditions):** Toàn bộ các đơn hàng đã thanh toán vé của sự kiện được tạo lệnh hoàn tiền tự động qua cổng thanh toán VNPay. Khách hàng nhận được thông báo tiền vé được hoàn.

## 2. Danh Sách Test Cases Tổng Hợp

| STT | Mã Test Case | Tên / Mục Tiêu Test Case | Phân Loại | Mức Độ Ưu Tiên |
|:---:|:---|:---|:---|:---:|
| 1 | **TC_UC20_01** | Xem danh sách các giao dịch cần hoàn tiền của sự kiện đã hủy | Functional (Happy Path) | **High** |
| 2 | **TC_UC20_02** | Kích hoạt hoàn tiền tự động thành công cho toàn bộ khách hàng | Functional (Happy Path) | **High** |
| 3 | **TC_UC20_03** | Ẩn nút 'Hoàn tiền' khi sự kiện chưa ở trạng thái Đã hủy | Business Rule / UI | **High** |
| 4 | **TC_UC20_04** | Xử lý khi một giao dịch hoàn tiền bị lỗi từ cổng thanh toán (Retry mechanism) | Exception / Reliability | **High** |
| 5 | **TC_UC20_05** | Kiểm tra tính lũy đẳng (Idempotency) - Không hoàn tiền lặp lại cho giao dịch đã hoàn | Security / Financial | **High** |
| 6 | **TC_UC20_06** | Khách hàng nhận được thông báo và email chi tiết sau khi hoàn tiền | Integration | **High** |
| 7 | **TC_UC20_07** | Cập nhật ví doanh thu của Nhà tổ chức sau khi sự kiện bị hủy và hoàn tiền | Financial / Consistency | **High** |

---

## 3. Thiết Kế Chi Tiết Từng Test Case

### TC_UC20_01: Xem danh sách các giao dịch cần hoàn tiền của sự kiện đã hủy

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC20_01` |
| **Tên Test Case** | Xem danh sách các giao dịch cần hoàn tiền của sự kiện đã hủy |
| **Use Case liên quan** | UC20 - Hoàn tiền cho khách hàng với sự kiện hủy (Refund for Cancelled Event) |
| **Phân loại kiểm thử** | Functional (Happy Path) |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Sự kiện 'Đêm nhạc Acoustic Thu' đã bị hủy, có 2 giao dịch thanh toán thành công: Nguyễn Văn D (3.000.000đ) và Phạm Thị E (1.500.000đ). |
| **Dữ liệu đầu vào (Test Data)** | N/A |
| **Các bước thực hiện** | 1. Admin mở chi tiết sự kiện đã hủy 'Đêm nhạc Acoustic Thu'.<br/>2. Quan sát bảng danh sách các giao dịch cần hoàn tiền. |
| **Kết quả mong đợi** | Bảng hiển thị đầy đủ danh sách giao dịch cần hoàn: Tên khách hàng (Nguyễn Văn D, Phạm Thị E), Số tiền đã thanh toán (3.000.000đ, 1.500.000đ), Mã đơn hàng, Phương thức thanh toán (VNPAY) và nút 'Hoàn tiền'. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC20_02: Kích hoạt hoàn tiền tự động thành công cho toàn bộ khách hàng

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC20_02` |
| **Tên Test Case** | Kích hoạt hoàn tiền tự động thành công cho toàn bộ khách hàng |
| **Use Case liên quan** | UC20 - Hoàn tiền cho khách hàng với sự kiện hủy (Refund for Cancelled Event) |
| **Phân loại kiểm thử** | Functional (Happy Path) |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Đang xem danh sách hoàn tiền của sự kiện đã hủy. |
| **Dữ liệu đầu vào (Test Data)** | N/A |
| **Các bước thực hiện** | 1. Nhấn nút 'Hoàn tiền'.<br/>2. Hộp thoại xác nhận hiển thị: 'Xác nhận hoàn tiền cho toàn bộ khách hàng của sự kiện này?'.<br/>3. Nhấn 'Xác nhận'. |
| **Kết quả mong đợi** | Hệ thống tự động tạo yêu cầu hoàn tiền (Refund request) gửi tới cổng thanh toán VNPay cho từng giao dịch. Trạng thái các giao dịch chuyển sang 'Đã hoàn tiền'. Khách hàng nhận được thông báo kết quả hoàn tiền. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC20_03: Ẩn nút 'Hoàn tiền' khi sự kiện chưa ở trạng thái Đã hủy

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC20_03` |
| **Tên Test Case** | Ẩn nút 'Hoàn tiền' khi sự kiện chưa ở trạng thái Đã hủy |
| **Use Case liên quan** | UC20 - Hoàn tiền cho khách hàng với sự kiện hủy (Refund for Cancelled Event) |
| **Phân loại kiểm thử** | Business Rule / UI |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Sự kiện đang ở trạng thái 'Đã duyệt' hoặc 'Đang diễn ra'. |
| **Dữ liệu đầu vào (Test Data)** | N/A |
| **Các bước thực hiện** | 1. Mở chi tiết sự kiện chưa bị hủy. |
| **Kết quả mong đợi** | Nút 'Hoàn tiền' không hiển thị trên giao diện, ngăn chặn thao tác hoàn tiền nhầm lẫn cho sự kiện đang hoạt động bình thường. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC20_04: Xử lý khi một giao dịch hoàn tiền bị lỗi từ cổng thanh toán (Retry mechanism)

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC20_04` |
| **Tên Test Case** | Xử lý khi một giao dịch hoàn tiền bị lỗi từ cổng thanh toán (Retry mechanism) |
| **Use Case liên quan** | UC20 - Hoàn tiền cho khách hàng với sự kiện hủy (Refund for Cancelled Event) |
| **Phân loại kiểm thử** | Exception / Reliability |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Cổng thanh toán VNPay trả về lỗi tạm thời với giao dịch của Phạm Thị E. |
| **Dữ liệu đầu vào (Test Data)** | Lỗi mạng hoặc lỗi timeout từ cổng thanh toán |
| **Các bước thực hiện** | 1. Kích hoạt hoàn tiền hàng loạt.<br/>2. Quan sát trạng thái xử lý của từng dòng giao dịch. |
| **Kết quả mong đợi** | Hệ thống ghi nhận log lý do lỗi, giữ trạng thái 'Đang xử lý / Chờ thử lại' cho giao dịch lỗi, cho phép Admin nhấn 'Thử lại hoàn tiền' (Retry) riêng cho giao dịch đó mà không ảnh hưởng tới các giao dịch khác đã thành công. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC20_05: Kiểm tra tính lũy đẳng (Idempotency) - Không hoàn tiền lặp lại cho giao dịch đã hoàn

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC20_05` |
| **Tên Test Case** | Kiểm tra tính lũy đẳng (Idempotency) - Không hoàn tiền lặp lại cho giao dịch đã hoàn |
| **Use Case liên quan** | UC20 - Hoàn tiền cho khách hàng với sự kiện hủy (Refund for Cancelled Event) |
| **Phân loại kiểm thử** | Security / Financial |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Giao dịch của Nguyễn Văn D đã hoàn tiền thành công (Trạng thái: 'Đã hoàn tiền'). |
| **Dữ liệu đầu vào (Test Data)** | N/A |
| **Các bước thực hiện** | 1. Cố tình nhấn nút Hoàn tiền hoặc gọi lại API hoàn tiền cho giao dịch đã hoàn. |
| **Kết quả mong đợi** | Hệ thống từ chối thực hiện lại, thông báo: 'Giao dịch này đã được hoàn tiền trước đó', đảm bảo không bị trừ tiền hoặc hoàn thừa lần thứ hai. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC20_06: Khách hàng nhận được thông báo và email chi tiết sau khi hoàn tiền

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC20_06` |
| **Tên Test Case** | Khách hàng nhận được thông báo và email chi tiết sau khi hoàn tiền |
| **Use Case liên quan** | UC20 - Hoàn tiền cho khách hàng với sự kiện hủy (Refund for Cancelled Event) |
| **Phân loại kiểm thử** | Integration |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Lệnh hoàn tiền được thực thi thành công. |
| **Dữ liệu đầu vào (Test Data)** | Email khách hàng: 'seed.customer3@eventhub.local' |
| **Các bước thực hiện** | 1. Kiểm tra hộp thư và thông báo của khách hàng 'seed.customer3@eventhub.local'. |
| **Kết quả mong đợi** | Khách hàng nhận được email/thông báo hoàn tiền cho đơn đặt vé sự kiện Art & Light - Không gian sắc màu (bị hủy). |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC20_07: Cập nhật ví doanh thu của Nhà tổ chức sau khi sự kiện bị hủy và hoàn tiền

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC20_07` |
| **Tên Test Case** | Cập nhật ví doanh thu của Nhà tổ chức sau khi sự kiện bị hủy và hoàn tiền |
| **Use Case liên quan** | UC20 - Hoàn tiền cho khách hàng với sự kiện hủy (Refund for Cancelled Event) |
| **Phân loại kiểm thử** | Financial / Consistency |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Sự kiện bị hủy có doanh thu tạm tính nằm trong ví của Organizer. |
| **Dữ liệu đầu vào (Test Data)** | N/A |
| **Các bước thực hiện** | 1. Kiểm tra ví doanh thu của Organizer sau khi hoàn tiền cho khách. |
| **Kết quả mong đợi** | Số dư đang chờ xác nhận hoặc doanh thu tạm tính của sự kiện bị trừ tương ứng, tránh việc Organizer rút nhầm tiền của sự kiện đã bị hủy. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

