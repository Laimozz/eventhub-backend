# UC07: Đặt Vé Thanh Toán

## 1. Thông Tin Use Case

- **Mã Use Case:** UC07
- **Tên Use Case:** Đặt vé và thanh toán (Book ticket & Payment)
- **Tác nhân chính (Actor):** Customer, Staff
- **Mô tả nghiệp vụ:** Cho phép khách hàng lựa chọn loại vé, số lượng vé, nhập thông tin liên hệ, thực hiện thanh toán trực tuyến qua cổng thanh toán VNPay và nhận mã booking.
- **Tiền điều kiện (Pre-conditions):** Người dùng đã đăng nhập vào hệ thống và đang ở màn hình chi tiết sự kiện có mở bán vé.
- **Hậu điều kiện (Post-conditions):** Đơn đặt vé (Booking) được tạo thành công với trạng thái ĐÃ THANH TOÁN (PAID), kho vé được trừ tương ứng, tạo mã vé điện tử và gửi thông báo/email xác nhận cho khách hàng.

## 2. Danh Sách Test Cases Tổng Hợp

| STT | Mã Test Case | Tên / Mục Tiêu Test Case | Phân Loại | Mức Độ Ưu Tiên |
|:---:|:---|:---|:---|:---:|
| 1 | **TC_UC07_01** | Đặt vé và thanh toán thành công qua VNPay với dữ liệu hợp lệ | Functional (Happy Path) | **High** |
| 2 | **TC_UC07_02** | Đặt vé thất bại khi không chọn loại vé nào mà bấm Tiếp tục | Validation (Negative) | **High** |
| 3 | **TC_UC07_03** | Chọn vé thất bại khi số lượng vé chọn vượt quá số lượng vé còn lại trong kho | Business Rule / Boundary | **High** |
| 4 | **TC_UC07_04** | Giới hạn số lượng vé tối đa cho một đơn hàng (Max tickets per booking) | Business Rule / Boundary | **Medium** |
| 5 | **TC_UC07_05** | Đặt vé thất bại khi không tick checkbox Đồng ý điều khoản | Validation (Negative) | **Medium** |
| 6 | **TC_UC07_06** | Đặt vé thất bại khi để trống hoặc nhập sai định dạng thông tin người mua | Validation (Negative) | **Medium** |
| 7 | **TC_UC07_07** | Đặt vé thất bại khi chưa chọn phương thức thanh toán | Validation (Negative) | **Medium** |
| 8 | **TC_UC07_08** | Hủy thanh toán tại trang thanh toán VNPay | Functional (Alternative Flow) | **High** |
| 9 | **TC_UC07_09** | Hết thời gian giữ chỗ (Countdown Timeout) trước khi thanh toán xong | Business Rule / Timeout | **High** |
| 10 | **TC_UC07_10** | Kiểm tra tính toàn vẹn dữ liệu khi có nhiều người dùng đồng thời mua số vé cuối cùng (Concurrency / Race Condition) | Concurrency / Non-functional | **High** |
| 11 | **TC_UC07_11** | Gửi Email xác nhận đặt vé thành công kèm mã vé điện tử | Integration | **High** |
| 12 | **TC_UC07_12** | Kiểm tra tính chính xác của Tổng tiền khi chọn kết hợp nhiều loại vé | Functional / Calculation | **Medium** |

---

## 3. Thiết Kế Chi Tiết Từng Test Case

### TC_UC07_01: Đặt vé và thanh toán thành công qua VNPay với dữ liệu hợp lệ

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC07_01` |
| **Tên Test Case** | Đặt vé và thanh toán thành công qua VNPay với dữ liệu hợp lệ |
| **Use Case liên quan** | UC07 - Đặt vé và thanh toán (Book ticket & Payment) |
| **Phân loại kiểm thử** | Functional (Happy Path) |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Đã đăng nhập tài khoản Customer, sự kiện 'ANH TRAI SAY HI - CONCEPT 5' còn vé A1 (100 vé) và B1 (200 vé). |
| **Dữ liệu đầu vào (Test Data)** | Loại vé: 2 vé Tiêu chuẩn (350.000đ/vé)<br/>Tổng tiền: 700.000đ<br/>Họ và tên: 'Nguyễn Gia Bảo'<br/>SĐT: '0908000009'<br/>Email: 'seed.customer1@eventhub.local'<br/>Phương thức thanh toán: 'VNPAY' |
| **Các bước thực hiện** | 1. Tại trang chi tiết sự kiện, nhấn 'Mua vé ngay'.<br/>2. Tại giao diện chọn vé, click nút '+' 2 lần ở vé A1 và 1 lần ở vé B1. Nhấn 'Tiếp tục'.<br/>3. Kiểm tra thông tin tóm tắt và đồng hồ đếm ngược giữ chỗ (15 phút). Điền thông tin người mua: Họ tên, SĐT, Email.<br/>4. Chọn phương thức thanh toán 'VNPAY', tick vào checkbox 'Đồng ý điều khoản đặt mua'. Nhấn 'Tiếp tục'.<br/>5. Hệ thống chuyển hướng sang cổng thanh toán VNPay hiển thị mã QR và số tiền 1.000.000đ.<br/>6. Thực hiện quét mã QR / giả lập thanh toán thành công trên VNPay sandbox. |
| **Kết quả mong đợi** | VNPay callback về hệ thống EventHub thành công. Chuyển hướng đến màn hình kết quả Booking (#001), trạng thái 'Đã thanh toán', hiển thị đầy đủ thông tin: Mã booking, ngày đặt, sự kiện, loại vé (2 vé A1, 1 vé B1), tổng tiền 1.000.000đ. Kho vé A1 còn 98, vé B1 còn 199. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC07_02: Đặt vé thất bại khi không chọn loại vé nào mà bấm Tiếp tục

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC07_02` |
| **Tên Test Case** | Đặt vé thất bại khi không chọn loại vé nào mà bấm Tiếp tục |
| **Use Case liên quan** | UC07 - Đặt vé và thanh toán (Book ticket & Payment) |
| **Phân loại kiểm thử** | Validation (Negative) |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Đang ở màn hình Chọn vé. |
| **Dữ liệu đầu vào (Test Data)** | Số lượng tất cả các loại vé = 0 |
| **Các bước thực hiện** | 1. Mở màn hình chọn vé.<br/>2. Không bấm dấu '+' ở bất kỳ loại vé nào (Tổng số vé = 0).<br/>3. Nhấn 'Tiếp tục'. |
| **Kết quả mong đợi** | Hệ thống chặn không cho sang bước tiếp theo, nút 'Tiếp tục' bị vô hiệu hóa hoặc hiển thị cảnh báo: 'Vui lòng chọn ít nhất 1 vé để tiếp tục'. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC07_03: Chọn vé thất bại khi số lượng vé chọn vượt quá số lượng vé còn lại trong kho

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC07_03` |
| **Tên Test Case** | Chọn vé thất bại khi số lượng vé chọn vượt quá số lượng vé còn lại trong kho |
| **Use Case liên quan** | UC07 - Đặt vé và thanh toán (Book ticket & Payment) |
| **Phân loại kiểm thử** | Business Rule / Boundary |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Loại vé VIP chỉ còn lại 2 vé trong kho. |
| **Dữ liệu đầu vào (Test Data)** | Click chọn 3 vé VIP |
| **Các bước thực hiện** | 1. Tại loại vé VIP (số lượng còn lại: 2), nhấn nút '+' 3 lần. |
| **Kết quả mong đợi** | Nút '+' bị disabled khi đạt số lượng tối đa 2 vé còn lại trong kho. Không thể tăng số lượng lên 3. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC07_04: Giới hạn số lượng vé tối đa cho một đơn hàng (Max tickets per booking)

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC07_04` |
| **Tên Test Case** | Giới hạn số lượng vé tối đa cho một đơn hàng (Max tickets per booking) |
| **Use Case liên quan** | UC07 - Đặt vé và thanh toán (Book ticket & Payment) |
| **Phân loại kiểm thử** | Business Rule / Boundary |
| **Mức độ ưu tiên** | **Medium** |
| **Tiền điều kiện** | Quy định hệ thống mỗi tài khoản chỉ được mua tối đa 10 vé/lần. |
| **Dữ liệu đầu vào (Test Data)** | Cố gắng chọn tổng 11 vé |
| **Các bước thực hiện** | 1. Chọn 10 vé loại thường.<br/>2. Cố gắng nhấn '+' thêm 1 vé nữa. |
| **Kết quả mong đợi** | Hệ thống hiển thị cảnh báo: 'Mỗi đơn hàng chỉ được mua tối đa 10 vé'. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC07_05: Đặt vé thất bại khi không tick checkbox Đồng ý điều khoản

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC07_05` |
| **Tên Test Case** | Đặt vé thất bại khi không tick checkbox Đồng ý điều khoản |
| **Use Case liên quan** | UC07 - Đặt vé và thanh toán (Book ticket & Payment) |
| **Phân loại kiểm thử** | Validation (Negative) |
| **Mức độ ưu tiên** | **Medium** |
| **Tiền điều kiện** | Đang ở màn hình Xác nhận thông tin đặt vé. |
| **Dữ liệu đầu vào (Test Data)** | Đã điền thông tin cá nhân đầy đủ, phương thức VNPAY, nhưng chưa tick 'Đồng ý điều khoản' |
| **Các bước thực hiện** | 1. Điền Họ tên, SĐT, Email hợp lệ.<br/>2. Chọn thanh toán VNPAY.<br/>3. Bỏ qua ô checkbox điều khoản.<br/>4. Nhấn 'Tiếp tục'. |
| **Kết quả mong đợi** | Hệ thống không chuyển sang cổng thanh toán, hiển thị cảnh báo: 'Bạn cần đồng ý với các điều khoản của EventHub để tiếp tục'. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC07_06: Đặt vé thất bại khi để trống hoặc nhập sai định dạng thông tin người mua

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC07_06` |
| **Tên Test Case** | Đặt vé thất bại khi để trống hoặc nhập sai định dạng thông tin người mua |
| **Use Case liên quan** | UC07 - Đặt vé và thanh toán (Book ticket & Payment) |
| **Phân loại kiểm thử** | Validation (Negative) |
| **Mức độ ưu tiên** | **Medium** |
| **Tiền điều kiện** | Đang ở màn hình Xác nhận đặt vé. |
| **Dữ liệu đầu vào (Test Data)** | Email: 'sai_email', SĐT: 'abc1234' |
| **Các bước thực hiện** | 1. Nhập thông tin người mua sai định dạng.<br/>2. Nhấn 'Tiếp tục'. |
| **Kết quả mong đợi** | Hệ thống hiển thị lỗi validation dưới từng ô nhập sai tương ứng. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC07_07: Đặt vé thất bại khi chưa chọn phương thức thanh toán

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC07_07` |
| **Tên Test Case** | Đặt vé thất bại khi chưa chọn phương thức thanh toán |
| **Use Case liên quan** | UC07 - Đặt vé và thanh toán (Book ticket & Payment) |
| **Phân loại kiểm thử** | Validation (Negative) |
| **Mức độ ưu tiên** | **Medium** |
| **Tiền điều kiện** | Đang ở màn hình Xác nhận đặt vé. |
| **Dữ liệu đầu vào (Test Data)** | Không chọn radio button phương thức thanh toán nào |
| **Các bước thực hiện** | 1. Điền thông tin người mua, tick điều khoản.<br/>2. Không chọn phương thức thanh toán.<br/>3. Nhấn 'Tiếp tục'. |
| **Kết quả mong đợi** | Hệ thống thông báo: 'Vui lòng chọn phương thức thanh toán'. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC07_08: Hủy thanh toán tại trang thanh toán VNPay

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC07_08` |
| **Tên Test Case** | Hủy thanh toán tại trang thanh toán VNPay |
| **Use Case liên quan** | UC07 - Đặt vé và thanh toán (Book ticket & Payment) |
| **Phân loại kiểm thử** | Functional (Alternative Flow) |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Đang ở trang thanh toán VNPay. |
| **Dữ liệu đầu vào (Test Data)** | N/A |
| **Các bước thực hiện** | 1. Tại trang VNPay, nhấn nút 'Hủy thanh toán' hoặc 'Quay về website'. |
| **Kết quả mong đợi** | Hệ thống nhận kết quả hủy giao dịch từ VNPay, điều hướng về màn hình chi tiết booking với trạng thái 'Đã hủy / Chưa thanh toán'. Vé tạm giữ được hoàn trả lại kho. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC07_09: Hết thời gian giữ chỗ (Countdown Timeout) trước khi thanh toán xong

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC07_09` |
| **Tên Test Case** | Hết thời gian giữ chỗ (Countdown Timeout) trước khi thanh toán xong |
| **Use Case liên quan** | UC07 - Đặt vé và thanh toán (Book ticket & Payment) |
| **Phân loại kiểm thử** | Business Rule / Timeout |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Khách hàng đang ở màn hình thanh toán, đồng hồ đếm ngược giữ chỗ 15 phút đếm về 00:00. |
| **Dữ liệu đầu vào (Test Data)** | Thời gian chờ > 15 phút |
| **Các bước thực hiện** | 1. Chọn vé và vào màn hình thanh toán.<br/>2. Chờ cho đến khi đồng hồ giữ chỗ đếm hết giờ (00:00).<br/>3. Cố gắng thực hiện thanh toán. |
| **Kết quả mong đợi** | Hệ thống tự động hủy phiên giữ vé, hiển thị thông báo: 'Đã hết thời gian giữ chỗ. Vé của bạn đã được hoàn lại vào hệ thống. Vui lòng thực hiện đặt vé lại'. Toàn bộ số lượng vé được hoàn trả vào kho khả dụng. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC07_10: Kiểm tra tính toàn vẹn dữ liệu khi có nhiều người dùng đồng thời mua số vé cuối cùng (Concurrency / Race Condition)

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC07_10` |
| **Tên Test Case** | Kiểm tra tính toàn vẹn dữ liệu khi có nhiều người dùng đồng thời mua số vé cuối cùng (Concurrency / Race Condition) |
| **Use Case liên quan** | UC07 - Đặt vé và thanh toán (Book ticket & Payment) |
| **Phân loại kiểm thử** | Concurrency / Non-functional |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Kho vé A1 chỉ còn đúng 1 vé cuối cùng. Hai người dùng User A và User B cùng chọn vé này và ấn Mua cùng thời điểm. |
| **Dữ liệu đầu vào (Test Data)** | Số lượng vé còn lại: 1 |
| **Các bước thực hiện** | 1. User A và User B cùng mở trang mua vé.<br/>2. Cả 2 cùng chọn 1 vé A1 và nhấn Tiếp tục giữ chỗ đồng thời. |
| **Kết quả mong đợi** | Chỉ 1 người dùng giữ chỗ thành công (User A). Người dùng còn lại (User B) nhận thông báo: 'Loại vé này vừa hết, vui lòng chọn loại vé khác'. Không xảy ra hiện tượng overselling (kho vé không bị âm). |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC07_11: Gửi Email xác nhận đặt vé thành công kèm mã vé điện tử

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC07_11` |
| **Tên Test Case** | Gửi Email xác nhận đặt vé thành công kèm mã vé điện tử |
| **Use Case liên quan** | UC07 - Đặt vé và thanh toán (Book ticket & Payment) |
| **Phân loại kiểm thử** | Integration |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Thanh toán thành công đơn hàng. |
| **Dữ liệu đầu vào (Test Data)** | Email người mua: 'seed.customer1@eventhub.local' |
| **Các bước thực hiện** | 1. Hoàn tất thanh toán đơn hàng.<br/>2. Kiểm tra hộp thư đến của email 'seed.customer1@eventhub.local'. |
| **Kết quả mong đợi** | Khách hàng nhận được email tự động từ EventHub chứa thông tin xác nhận: Mã booking, Tên sự kiện, Thời gian, Địa điểm, Danh sách vé và mã QR code để check-in. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC07_12: Kiểm tra tính chính xác của Tổng tiền khi chọn kết hợp nhiều loại vé

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC07_12` |
| **Tên Test Case** | Kiểm tra tính chính xác của Tổng tiền khi chọn kết hợp nhiều loại vé |
| **Use Case liên quan** | UC07 - Đặt vé và thanh toán (Book ticket & Payment) |
| **Phân loại kiểm thử** | Functional / Calculation |
| **Mức độ ưu tiên** | **Medium** |
| **Tiền điều kiện** | Vé A1: 300.000đ, Vé B1: 400.000đ, Vé C1: 1.000.000đ. |
| **Dữ liệu đầu vào (Test Data)** | Chọn: 3 vé A1 (900.000đ) + 2 vé B1 (800.000đ) + 1 vé C1 (1.000.000đ) |
| **Các bước thực hiện** | 1. Tăng số lượng vé A1 lên 3, B1 lên 2, C1 lên 1.<br/>2. Quan sát ô 'Tổng số lượng vé' và 'Tổng tiền'. |
| **Kết quả mong đợi** | Tổng số vé hiển thị chính xác là 6 vé. Tổng tiền tính toán đúng = 900.000 + 800.000 + 1.000.000 = 2.700.000đ. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

