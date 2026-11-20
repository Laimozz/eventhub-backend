# UC09: Xem Vé Điện Tử

## 1. Thông Tin Use Case

- **Mã Use Case:** UC09
- **Tên Use Case:** Xem vé điện tử (E-ticket)
- **Tác nhân chính (Actor):** Customer, Staff
- **Mô tả nghiệp vụ:** Cho phép người dùng xem danh sách vé điện tử mình sở hữu, phân biệt vé chưa sử dụng và đã sử dụng, mở chi tiết vé kèm QR code để soát vé.
- **Tiền điều kiện (Pre-conditions):** Người dùng đã đăng nhập và đã có vé của các sự kiện đã thanh toán.
- **Hậu điều kiện (Post-conditions):** Hệ thống hiển thị mã vé điện tử kèm mã QR Code hợp lệ để phục vụ check-in tại sự kiện.

## 2. Danh Sách Test Cases Tổng Hợp

| STT | Mã Test Case | Tên / Mục Tiêu Test Case | Phân Loại | Mức Độ Ưu Tiên |
|:---:|:---|:---|:---|:---:|
| 1 | **TC_UC09_01** | Xem danh sách vé điện tử với các trạng thái Chưa sử dụng và Đã sử dụng | Functional (Happy Path) | **High** |
| 2 | **TC_UC09_02** | Mở chi tiết vé điện tử chưa sử dụng hiển thị đầy đủ QR Code | Functional (Happy Path) | **High** |
| 3 | **TC_UC09_03** | Kiểm tra nút 'Xem vé điện tử' bị vô hiệu hóa với vé Đã sử dụng | UI / Business Rule | **Medium** |
| 4 | **TC_UC09_04** | Hiển thị thông báo trống khi người dùng chưa có vé nào | Boundary / UI | **Low** |
| 5 | **TC_UC09_05** | Quét thử mã QR bằng thiết bị kiểm tra xem định dạng dữ liệu mã hóa | Integration / Verification | **High** |
| 6 | **TC_UC09_06** | Xem vé điện tử trực tiếp từ trang Chi tiết booking | Navigation | **Medium** |

---

## 3. Thiết Kế Chi Tiết Từng Test Case

### TC_UC09_01: Xem danh sách vé điện tử với các trạng thái Chưa sử dụng và Đã sử dụng

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC09_01` |
| **Tên Test Case** | Xem danh sách vé điện tử với các trạng thái Chưa sử dụng và Đã sử dụng |
| **Use Case liên quan** | UC09 - Xem vé điện tử (E-ticket) |
| **Phân loại kiểm thử** | Functional (Happy Path) |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Người dùng đã mua vé cho sự kiện 'ANH TRAI SAY HI - CONCEPT 4'. |
| **Dữ liệu đầu vào (Test Data)** | N/A |
| **Các bước thực hiện** | 1. Nhấn nút 'Tài khoản' trên header.<br/>2. Chọn 'Vé của tôi'.<br/>3. Quan sát danh sách vé. |
| **Kết quả mong đợi** | Hiển thị danh sách vé: Thông tin sự kiện, ngày giờ, địa điểm, loại vé (A1), và trạng thái 'Chưa sử dụng' hoặc 'Đã sử dụng'. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC09_02: Mở chi tiết vé điện tử chưa sử dụng hiển thị đầy đủ QR Code

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC09_02` |
| **Tên Test Case** | Mở chi tiết vé điện tử chưa sử dụng hiển thị đầy đủ QR Code |
| **Use Case liên quan** | UC09 - Xem vé điện tử (E-ticket) |
| **Phân loại kiểm thử** | Functional (Happy Path) |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Có vé ở trạng thái 'Chưa sử dụng'. |
| **Dữ liệu đầu vào (Test Data)** | Vé sự kiện 'ANH TRAI SAY HI - CONCEPT 4', loại vé A1, ghế X43 |
| **Các bước thực hiện** | 1. Tại vé thứ nhất có trạng thái 'Chưa sử dụng', nhấn nút 'Xem vé điện tử'.<br/>2. Quan sát popup/màn hình Chi tiết vé. |
| **Kết quả mong đợi** | Hiển thị rõ nét hình ảnh Mã QR Code. Bên dưới có đầy đủ thông tin vé: Loại vé A1, Vị trí ghế X43, Tên sự kiện 'ANH TRAI SAY HI - CONCEPT 4', Ngày 26/09/2026, Giờ 20:00 - 23:00, Địa điểm SVĐ Mỹ Đình. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC09_03: Kiểm tra nút 'Xem vé điện tử' bị vô hiệu hóa với vé Đã sử dụng

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC09_03` |
| **Tên Test Case** | Kiểm tra nút 'Xem vé điện tử' bị vô hiệu hóa với vé Đã sử dụng |
| **Use Case liên quan** | UC09 - Xem vé điện tử (E-ticket) |
| **Phân loại kiểm thử** | UI / Business Rule |
| **Mức độ ưu tiên** | **Medium** |
| **Tiền điều kiện** | Có vé trong danh sách đã được nhân viên check-in (Trạng thái: Đã sử dụng). |
| **Dữ liệu đầu vào (Test Data)** | Vé trạng thái 'Đã sử dụng' |
| **Các bước thực hiện** | 1. Tìm đến vé có trạng thái 'Đã sử dụng'.<br/>2. Kiểm tra trạng thái và tương tác với nút 'Xem vé điện tử'. |
| **Kết quả mong đợi** | Nút 'Xem vé điện tử' bị disabled (vô hiệu hóa, làm mờ) hoặc hiển thị nhãn mộc 'ĐÃ SỬ DỤNG' để ngăn chặn sử dụng lại. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC09_04: Hiển thị thông báo trống khi người dùng chưa có vé nào

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC09_04` |
| **Tên Test Case** | Hiển thị thông báo trống khi người dùng chưa có vé nào |
| **Use Case liên quan** | UC09 - Xem vé điện tử (E-ticket) |
| **Phân loại kiểm thử** | Boundary / UI |
| **Mức độ ưu tiên** | **Low** |
| **Tiền điều kiện** | Người dùng chưa từng mua vé. |
| **Dữ liệu đầu vào (Test Data)** | Tài khoản mới |
| **Các bước thực hiện** | 1. Vào menu 'Vé của tôi'. |
| **Kết quả mong đợi** | Hệ thống hiển thị thông báo: 'Bạn chưa có vé điện tử nào' kèm nút kêu gọi đặt vé. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC09_05: Quét thử mã QR bằng thiết bị kiểm tra xem định dạng dữ liệu mã hóa

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC09_05` |
| **Tên Test Case** | Quét thử mã QR bằng thiết bị kiểm tra xem định dạng dữ liệu mã hóa |
| **Use Case liên quan** | UC09 - Xem vé điện tử (E-ticket) |
| **Phân loại kiểm thử** | Integration / Verification |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Chi tiết vé điện tử đang hiển thị mã QR. |
| **Dữ liệu đầu vào (Test Data)** | Mã QR trên vé |
| **Các bước thực hiện** | 1. Sử dụng ứng dụng quét QR quét mã hiển thị trên màn hình vé.<br/>2. Kiểm tra chuỗi dữ liệu giải mã. |
| **Kết quả mong đợi** | Chuỗi mã hóa chứa mã vé duy nhất (Unique Ticket Code / JWT Signed Code) khớp với mã vé trong cơ sở dữ liệu. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC09_06: Xem vé điện tử trực tiếp từ trang Chi tiết booking

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC09_06` |
| **Tên Test Case** | Xem vé điện tử trực tiếp từ trang Chi tiết booking |
| **Use Case liên quan** | UC09 - Xem vé điện tử (E-ticket) |
| **Phân loại kiểm thử** | Navigation |
| **Mức độ ưu tiên** | **Medium** |
| **Tiền điều kiện** | Đang ở trang Chi tiết booking #001. |
| **Dữ liệu đầu vào (Test Data)** | N/A |
| **Các bước thực hiện** | 1. Tại bảng thông tin vé trong chi tiết booking, nhấn nút 'Xem vé điện tử' ở một dòng vé bất kỳ. |
| **Kết quả mong đợi** | Hiển thị chính xác popup vé điện tử của mã vé đó với đầy đủ mã QR. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

