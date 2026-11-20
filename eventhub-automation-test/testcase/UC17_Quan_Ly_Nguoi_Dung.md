# UC17: Quản Lý Người Dùng

## 1. Thông Tin Use Case

- **Mã Use Case:** UC17
- **Tên Use Case:** Quản lý người dùng (User Management)
- **Tác nhân chính (Actor):** Admin (Quản trị viên)
- **Mô tả nghiệp vụ:** Cung cấp cho Admin công cụ tra cứu, tìm kiếm, lọc theo vai trò, thêm mới tài khoản người dùng và quản lý trạng thái kích hoạt (Khóa / Mở khóa tài khoản).
- **Tiền điều kiện (Pre-conditions):** Quản trị viên đã đăng nhập vào hệ thống EventHub với tài khoản quyền Admin.
- **Hậu điều kiện (Post-conditions):** Danh sách người dùng được cập nhật mới (Tạo tài khoản thành công, Khóa tài khoản vi phạm hoặc Mở khóa tài khoản).

## 2. Danh Sách Test Cases Tổng Hợp

| STT | Mã Test Case | Tên / Mục Tiêu Test Case | Phân Loại | Mức Độ Ưu Tiên |
|:---:|:---|:---|:---|:---:|
| 1 | **TC_UC17_01** | Xem danh sách và tìm kiếm, lọc người dùng thành công | Functional (Happy Path) | **High** |
| 2 | **TC_UC17_02** | Tạo tài khoản người dùng mới thành công với đầy đủ thông tin hợp lệ | Functional (Happy Path) | **High** |
| 3 | **TC_UC17_03** | Tạo tài khoản thất bại khi Email đã tồn tại trong hệ thống (Duplicate Email) | Validation (Negative) | **High** |
| 4 | **TC_UC17_04** | Tạo tài khoản thất bại khi để trống các trường thông tin bắt buộc | Validation (Negative) | **Medium** |
| 5 | **TC_UC17_05** | Khóa tài khoản người dùng đang hoạt động (Lock / Ban User) | Functional | **High** |
| 6 | **TC_UC17_06** | Mở khóa tài khoản người dùng đang bị khóa (Unlock User) | Functional | **High** |
| 7 | **TC_UC17_07** | Bảo mật: Admin không thể tự khóa chính tài khoản của mình | Security / Business Rule | **High** |
| 8 | **TC_UC17_08** | Tạo tài khoản cho các vai trò khác nhau (Organizer, Staff) | Functional | **Medium** |
| 9 | **TC_UC17_09** | Phân trang danh sách người dùng khi số lượng lớn | UI / Functional | **Low** |

---

## 3. Thiết Kế Chi Tiết Từng Test Case

### TC_UC17_01: Xem danh sách và tìm kiếm, lọc người dùng thành công

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC17_01` |
| **Tên Test Case** | Xem danh sách và tìm kiếm, lọc người dùng thành công |
| **Use Case liên quan** | UC17 - Quản lý người dùng (User Management) |
| **Phân loại kiểm thử** | Functional (Happy Path) |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Hệ thống có nhiều người dùng với các vai trò Customer, Organizer, Staff. |
| **Dữ liệu đầu vào (Test Data)** | Tìm kiếm: 'Nguyễn Văn A'<br/>Bộ lọc vai trò: 'Customer' |
| **Các bước thực hiện** | 1. Admin chọn menu 'Quản lý người dùng'.<br/>2. Nhập từ khóa 'Nguyễn Văn A' vào ô tìm kiếm.<br/>3. Chọn bộ lọc vai trò = 'Customer'.<br/>4. Quan sát danh sách hiển thị. |
| **Kết quả mong đợi** | Hệ thống hiển thị chính xác các tài khoản có tên chứa 'Nguyễn Văn A' và có vai trò Customer kèm thông tin: Họ tên, Email, Vai trò, Trạng thái ('Đang hoạt động'). |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC17_02: Tạo tài khoản người dùng mới thành công với đầy đủ thông tin hợp lệ

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC17_02` |
| **Tên Test Case** | Tạo tài khoản người dùng mới thành công với đầy đủ thông tin hợp lệ |
| **Use Case liên quan** | UC17 - Quản lý người dùng (User Management) |
| **Phân loại kiểm thử** | Functional (Happy Path) |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Email 'new_admin_created@eventhub.test' chưa có trên hệ thống. |
| **Dữ liệu đầu vào (Test Data)** | Họ tên: 'Người Dùng Test Admin'<br/>Email: 'new_admin_created@eventhub.test'<br/>SĐT: '0988776655'<br/>Mật khẩu: 'SecurePass123@'<br/>Vai trò: 'Customer' |
| **Các bước thực hiện** | 1. Nhấn nút 'Thêm người dùng'.<br/>2. Nhập đầy đủ thông tin theo dữ liệu thử nghiệm.<br/>3. Nhấn nút 'Tạo tài khoản'. |
| **Kết quả mong đợi** | Hệ thống thông báo tạo tài khoản thành công. Tài khoản mới xuất hiện trong danh sách người dùng với trạng thái 'Đang hoạt động'. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC17_03: Tạo tài khoản thất bại khi Email đã tồn tại trong hệ thống (Duplicate Email)

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC17_03` |
| **Tên Test Case** | Tạo tài khoản thất bại khi Email đã tồn tại trong hệ thống (Duplicate Email) |
| **Use Case liên quan** | UC17 - Quản lý người dùng (User Management) |
| **Phân loại kiểm thử** | Validation (Negative) |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Tài khoản 'seed.customer1@eventhub.local' đã tồn tại trong seed data. |
| **Dữ liệu đầu vào (Test Data)** | Email: 'seed.customer1@eventhub.local' |
| **Các bước thực hiện** | 1. Nhấn 'Thêm người dùng'.<br/>2. Nhập email trùng lặp 'seed.customer1@eventhub.local' cùng các thông tin khác.<br/>3. Nhấn 'Tạo tài khoản'. |
| **Kết quả mong đợi** | Hệ thống từ chối tạo và hiển thị thông báo lỗi: 'Email đã tồn tại trong hệ thống. Vui lòng sử dụng email khác'. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC17_04: Tạo tài khoản thất bại khi để trống các trường thông tin bắt buộc

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC17_04` |
| **Tên Test Case** | Tạo tài khoản thất bại khi để trống các trường thông tin bắt buộc |
| **Use Case liên quan** | UC17 - Quản lý người dùng (User Management) |
| **Phân loại kiểm thử** | Validation (Negative) |
| **Mức độ ưu tiên** | **Medium** |
| **Tiền điều kiện** | Đang mở form Thêm người dùng. |
| **Dữ liệu đầu vào (Test Data)** | Để trống Họ tên, Email, Mật khẩu hoặc chưa chọn Vai trò |
| **Các bước thực hiện** | 1. Mở form thêm người dùng.<br/>2. Nhấn nút 'Tạo tài khoản' khi chưa nhập đủ thông tin. |
| **Kết quả mong đợi** | Hệ thống đánh dấu các trường bắt buộc và thông báo: 'Vui lòng điền đầy đủ các thông tin bắt buộc'. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC17_05: Khóa tài khoản người dùng đang hoạt động (Lock / Ban User)

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC17_05` |
| **Tên Test Case** | Khóa tài khoản người dùng đang hoạt động (Lock / Ban User) |
| **Use Case liên quan** | UC17 - Quản lý người dùng (User Management) |
| **Phân loại kiểm thử** | Functional |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Người dùng 'vol.customer2@eventhub.test' (Khách Hàng Test Số 2) đang có trạng thái 'Đang hoạt động' (ACTIVE). |
| **Dữ liệu đầu vào (Test Data)** | Người dùng: 'vol.customer2@eventhub.test' |
| **Các bước thực hiện** | 1. Tại dòng tài khoản của 'vol.customer2@eventhub.test', chọn đổi trạng thái sang 'Bị khóa' (hoặc bấm nút Khóa tài khoản).<br/>2. Xác nhận hành động khóa. |
| **Kết quả mong đợi** | Trạng thái của tài khoản đổi thành 'Bị khóa' (LOCKED). Người dùng sẽ không thể đăng nhập vào hệ thống được nữa. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC17_06: Mở khóa tài khoản người dùng đang bị khóa (Unlock User)

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC17_06` |
| **Tên Test Case** | Mở khóa tài khoản người dùng đang bị khóa (Unlock User) |
| **Use Case liên quan** | UC17 - Quản lý người dùng (User Management) |
| **Phân loại kiểm thử** | Functional |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Người dùng 'vol.customer36@eventhub.test' (Khách Hàng Test Số 36) đang có trạng thái 'Bị khóa' (LOCKED trong seeddata-large.sql). |
| **Dữ liệu đầu vào (Test Data)** | Người dùng: 'vol.customer36@eventhub.test' |
| **Các bước thực hiện** | 1. Tại dòng tài khoản 'vol.customer36@eventhub.test', click chuyển trạng thái 'Bị khóa' sang 'Đang hoạt động'.<br/>2. Xác nhận hành động mở khóa. |
| **Kết quả mong đợi** | Hệ thống cập nhật trạng thái tài khoản thành 'Đang hoạt động' (ACTIVE). Người dùng có thể đăng nhập lại bình thường. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC17_07: Bảo mật: Admin không thể tự khóa chính tài khoản của mình

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC17_07` |
| **Tên Test Case** | Bảo mật: Admin không thể tự khóa chính tài khoản của mình |
| **Use Case liên quan** | UC17 - Quản lý người dùng (User Management) |
| **Phân loại kiểm thử** | Security / Business Rule |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Admin đang đăng nhập bằng tài khoản 'seed.admin@eventhub.local'. |
| **Dữ liệu đầu vào (Test Data)** | Tài khoản hiện tại của Admin: 'seed.admin@eventhub.local' |
| **Các bước thực hiện** | 1. Tìm tài khoản 'seed.admin@eventhub.local' trong danh sách người dùng.<br/>2. Thử nhấn nút khóa trên chính tài khoản của mình. |
| **Kết quả mong đợi** | Nút khóa trên tài khoản của chính mình bị ẩn/disabled hoặc hệ thống trả về thông báo lỗi: 'Bạn không thể tự khóa tài khoản quản trị của chính mình'. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC17_08: Tạo tài khoản cho các vai trò khác nhau (Organizer, Staff)

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC17_08` |
| **Tên Test Case** | Tạo tài khoản cho các vai trò khác nhau (Organizer, Staff) |
| **Use Case liên quan** | UC17 - Quản lý người dùng (User Management) |
| **Phân loại kiểm thử** | Functional |
| **Mức độ ưu tiên** | **Medium** |
| **Tiền điều kiện** | Đang ở form Thêm người dùng. |
| **Dữ liệu đầu vào (Test Data)** | Chọn vai trò = 'Organizer' hoặc 'Staff' |
| **Các bước thực hiện** | 1. Nhập thông tin tài khoản.<br/>2. Chọn Vai trò = 'Organizer'.<br/>3. Nhấn 'Tạo tài khoản'. |
| **Kết quả mong đợi** | Tài khoản được tạo thành công với quyền tương ứng, đăng nhập vào đúng phân hệ chức năng của Organizer/Staff. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC17_09: Phân trang danh sách người dùng khi số lượng lớn

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC17_09` |
| **Tên Test Case** | Phân trang danh sách người dùng khi số lượng lớn |
| **Use Case liên quan** | UC17 - Quản lý người dùng (User Management) |
| **Phân loại kiểm thử** | UI / Functional |
| **Mức độ ưu tiên** | **Low** |
| **Tiền điều kiện** | Hệ thống có nhiều hơn 20 người dùng. |
| **Dữ liệu đầu vào (Test Data)** | N/A |
| **Các bước thực hiện** | 1. Cuộn xuống cuối trang danh sách người dùng.<br/>2. Nhấn vào trang tiếp theo. |
| **Kết quả mong đợi** | Chuyển trang chính xác, hiển thị đầy đủ danh sách người dùng của trang đó. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

