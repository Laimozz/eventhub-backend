# UC01: Đăng Ký

## 1. Thông Tin Use Case

- **Mã Use Case:** UC01
- **Tên Use Case:** Đăng ký tài khoản (Register)
- **Tác nhân chính (Actor):** Guest (Khách vãng lai)
- **Mô tả nghiệp vụ:** Cho phép khách vãng lai đăng ký tài khoản mới trên hệ thống với vai trò Khách mua vé (Customer) hoặc Ban tổ chức (Organizer).
- **Tiền điều kiện (Pre-conditions):** Người dùng truy cập vào trang chủ hệ thống EventHub và chưa đăng nhập, chưa có tài khoản.
- **Hậu điều kiện (Post-conditions):** Tài khoản người dùng mới được tạo trong cơ sở dữ liệu ở trạng thái hoạt động (ACTIVE). Mật khẩu được mã hóa an toàn (BCrypt). Người dùng có thể đăng nhập bằng tài khoản vừa tạo.

## 2. Danh Sách Test Cases Tổng Hợp

| STT | Mã Test Case | Tên / Mục Tiêu Test Case | Phân Loại | Mức Độ Ưu Tiên |
|:---:|:---|:---|:---|:---:|
| 1 | **TC_UC01_01** | Đăng ký thành công tài khoản Khách mua vé (Customer) với dữ liệu hợp lệ | Functional (Happy Path) | **High** |
| 2 | **TC_UC01_02** | Đăng ký thành công tài khoản Ban tổ chức (Organizer) với dữ liệu hợp lệ | Functional (Happy Path) | **High** |
| 3 | **TC_UC01_03** | Đăng ký thất bại khi nhập sai định dạng Email | Validation (Negative) | **High** |
| 4 | **TC_UC01_04** | Đăng ký thất bại khi mật khẩu ngắn hơn 8 ký tự | Validation / Boundary (Negative) | **High** |
| 5 | **TC_UC01_05** | Đăng ký thất bại khi mật khẩu không đủ độ phức tạp (thiếu ký tự đặc biệt hoặc chữ hoa) | Security / Validation | **Medium** |
| 6 | **TC_UC01_06** | Đăng ký thất bại khi Xác nhận mật khẩu không khớp | Validation (Negative) | **High** |
| 7 | **TC_UC01_07** | Đăng ký thất bại khi Email đã tồn tại trên hệ thống | Business Rule (Negative) | **High** |
| 8 | **TC_UC01_08** | Đăng ký thất bại khi để trống các trường bắt buộc | Validation (Negative) | **Medium** |

---

## 3. Thiết Kế Chi Tiết Từng Test Case

### TC_UC01_01: Đăng ký thành công tài khoản Khách mua vé (Customer) với dữ liệu hợp lệ

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC01_01` |
| **Tên Test Case** | Đăng ký thành công tài khoản Khách mua vé (Customer) với dữ liệu hợp lệ |
| **Use Case liên quan** | UC01 - Đăng ký tài khoản (Register) |
| **Phân loại kiểm thử** | Functional (Happy Path) |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Hệ thống hoạt động bình thường, email chưa từng đăng ký trong hệ thống. |
| **Dữ liệu đầu vào (Test Data)** | Loại tài khoản: 'Khách mua vé'<br/>Họ và tên: 'Nguyễn Văn Mới'<br/>Email: 'new.customer.demo@eventhub.local'<br/>Số điện thoại: '0908123456'<br/>Mật khẩu: 'EventHub@123'<br/>Xác nhận mật khẩu: 'EventHub@123' |
| **Các bước thực hiện** | 1. Truy cập vào trang chủ EventHub.<br/>2. Nhấn nút 'Đăng ký' trên header.<br/>3. Chọn loại tài khoản là 'Khách mua vé'.<br/>4. Nhập đầy đủ thông tin: Họ và tên, Email, Số điện thoại, Mật khẩu, Xác nhận mật khẩu theo dữ liệu thử nghiệm.<br/>5. Nhấn nút 'Tiếp tục' (Đăng ký). |
| **Kết quả mong đợi** | Hệ thống thông báo đăng ký tài khoản thành công qua hộp thoại thông báo. Có nút chuyển hướng quay về trang Đăng nhập. Tài khoản được lưu vào CSDL với role CUSTOMER, mật khẩu được băm BCrypt. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC01_02: Đăng ký thành công tài khoản Ban tổ chức (Organizer) với dữ liệu hợp lệ

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC01_02` |
| **Tên Test Case** | Đăng ký thành công tài khoản Ban tổ chức (Organizer) với dữ liệu hợp lệ |
| **Use Case liên quan** | UC01 - Đăng ký tài khoản (Register) |
| **Phân loại kiểm thử** | Functional (Happy Path) |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Email chưa từng đăng ký trong hệ thống. |
| **Dữ liệu đầu vào (Test Data)** | Loại tài khoản: 'Ban tổ chức'<br/>Họ và tên: 'Công Ty Sự Kiện Mới'<br/>Email: 'new.organizer.demo@eventhub.local'<br/>Số điện thoại: '0911123456'<br/>Mật khẩu: 'EventHub@123'<br/>Xác nhận mật khẩu: 'EventHub@123' |
| **Các bước thực hiện** | 1. Truy cập vào trang chủ EventHub.<br/>2. Nhấn nút 'Đăng ký' trên header.<br/>3. Chọn loại tài khoản là 'Ban tổ chức'.<br/>4. Nhập đầy đủ thông tin hợp lệ theo dữ liệu thử nghiệm.<br/>5. Nhấn nút 'Tiếp tục'. |
| **Kết quả mong đợi** | Hệ thống thông báo đăng ký thành công. Tài khoản được khởi tạo với vai trò ORGANIZER trong cơ sở dữ liệu. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC01_03: Đăng ký thất bại khi nhập sai định dạng Email

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC01_03` |
| **Tên Test Case** | Đăng ký thất bại khi nhập sai định dạng Email |
| **Use Case liên quan** | UC01 - Đăng ký tài khoản (Register) |
| **Phân loại kiểm thử** | Validation (Negative) |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Đang ở màn hình Đăng ký. |
| **Dữ liệu đầu vào (Test Data)** | Email không hợp lệ: 'phucna', 'phucna@', 'phucna@gmail', 'phucna.gmail.com' |
| **Các bước thực hiện** | 1. Chọn loại tài khoản 'Khách mua vé'.<br/>2. Nhập các trường Họ tên, SĐT, Mật khẩu hợp lệ.<br/>3. Nhập Email = 'phucna@gmail' (sai cú pháp RFC).<br/>4. Nhấn nút 'Tiếp tục'. |
| **Kết quả mong đợi** | Hệ thống không gửi request đăng ký thành công, hiển thị thông báo lỗi tại trường Email: 'Email không đúng định dạng'. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC01_04: Đăng ký thất bại khi mật khẩu ngắn hơn 8 ký tự

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC01_04` |
| **Tên Test Case** | Đăng ký thất bại khi mật khẩu ngắn hơn 8 ký tự |
| **Use Case liên quan** | UC01 - Đăng ký tài khoản (Register) |
| **Phân loại kiểm thử** | Validation / Boundary (Negative) |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Đang ở màn hình Đăng ký. |
| **Dữ liệu đầu vào (Test Data)** | Mật khẩu: 'Phuc1@' (6 ký tự), 'Phuc12@' (7 ký tự)<br/>Xác nhận mật khẩu: Trùng mật khẩu |
| **Các bước thực hiện** | 1. Nhập Họ tên, Email, SĐT hợp lệ.<br/>2. Nhập Mật khẩu = 'Phuc12@'.<br/>3. Nhập Xác nhận mật khẩu = 'Phuc12@'.<br/>4. Nhấn 'Tiếp tục'. |
| **Kết quả mong đợi** | Hệ thống cảnh báo lỗi: 'Mật khẩu phải chứa ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường, chữ số và ký tự đặc biệt'. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC01_05: Đăng ký thất bại khi mật khẩu không đủ độ phức tạp (thiếu ký tự đặc biệt hoặc chữ hoa)

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC01_05` |
| **Tên Test Case** | Đăng ký thất bại khi mật khẩu không đủ độ phức tạp (thiếu ký tự đặc biệt hoặc chữ hoa) |
| **Use Case liên quan** | UC01 - Đăng ký tài khoản (Register) |
| **Phân loại kiểm thử** | Security / Validation |
| **Mức độ ưu tiên** | **Medium** |
| **Tiền điều kiện** | Đang ở màn hình Đăng ký. |
| **Dữ liệu đầu vào (Test Data)** | Mật khẩu: 'phucna123456' (thiếu chữ hoa và ký tự đặc biệt) |
| **Các bước thực hiện** | 1. Nhập thông tin cơ bản hợp lệ.<br/>2. Nhập Mật khẩu = 'phucna123456'.<br/>3. Nhập Xác nhận mật khẩu = 'phucna123456'.<br/>4. Nhấn 'Tiếp tục'. |
| **Kết quả mong đợi** | Hệ thống từ chối đăng ký và báo lỗi yêu cầu mật khẩu phải có chữ hoa, chữ thường, chữ số và ký tự đặc biệt. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC01_06: Đăng ký thất bại khi Xác nhận mật khẩu không khớp

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC01_06` |
| **Tên Test Case** | Đăng ký thất bại khi Xác nhận mật khẩu không khớp |
| **Use Case liên quan** | UC01 - Đăng ký tài khoản (Register) |
| **Phân loại kiểm thử** | Validation (Negative) |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Đang ở màn hình Đăng ký. |
| **Dữ liệu đầu vào (Test Data)** | Mật khẩu: 'Phucyb3456@'<br/>Xác nhận mật khẩu: 'Phucna3456@' |
| **Các bước thực hiện** | 1. Nhập đầy đủ thông tin hợp lệ.<br/>2. Nhập Mật khẩu = 'Phucyb3456@'.<br/>3. Nhập Xác nhận mật khẩu = 'Phucna3456@' (khác mật khẩu trên).<br/>4. Nhấn 'Tiếp tục'. |
| **Kết quả mong đợi** | Hệ thống hiển thị lỗi tại ô Xác nhận mật khẩu: 'Mật khẩu xác nhận không khớp'. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC01_07: Đăng ký thất bại khi Email đã tồn tại trên hệ thống

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC01_07` |
| **Tên Test Case** | Đăng ký thất bại khi Email đã tồn tại trên hệ thống |
| **Use Case liên quan** | UC01 - Đăng ký tài khoản (Register) |
| **Phân loại kiểm thử** | Business Rule (Negative) |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Tài khoản với email 'seed.customer1@eventhub.local' (hoặc 'vol.customer1@eventhub.test') đã tồn tại trong database. |
| **Dữ liệu đầu vào (Test Data)** | Email: 'seed.customer1@eventhub.local' |
| **Các bước thực hiện** | 1. Điền toàn bộ thông tin đăng ký hợp lệ với Email = 'seed.customer1@eventhub.local'.<br/>2. Nhấn 'Tiếp tục'. |
| **Kết quả mong đợi** | Hệ thống trả về mã lỗi (HTTP 409 Conflict hoặc 400 Bad Request), hiển thị thông báo: 'Email đã tồn tại trong hệ thống. Vui lòng sử dụng email khác hoặc đăng nhập'. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC01_08: Đăng ký thất bại khi để trống các trường bắt buộc

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC01_08` |
| **Tên Test Case** | Đăng ký thất bại khi để trống các trường bắt buộc |
| **Use Case liên quan** | UC01 - Đăng ký tài khoản (Register) |
| **Phân loại kiểm thử** | Validation (Negative) |
| **Mức độ ưu tiên** | **Medium** |
| **Tiền điều kiện** | Đang ở màn hình Đăng ký. |
| **Dữ liệu đầu vào (Test Data)** | Để trống toàn bộ hoặc từng trường: Họ tên, Email, Số điện thoại, Mật khẩu |
| **Các bước thực hiện** | 1. Mở trang Đăng ký.<br/>2. Không nhập thông tin nào, bấm ngay nút 'Tiếp tục'.<br/>3. Thử lại bằng cách để trống lần lượt từng trường. |
| **Kết quả mong đợi** | Hệ thống ngăn không cho submit và đánh dấu đỏ/hiển thị cảnh báo lỗi dưới từng trường trống: 'Vui lòng nhập họ và tên', 'Vui lòng nhập email', v.v. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

