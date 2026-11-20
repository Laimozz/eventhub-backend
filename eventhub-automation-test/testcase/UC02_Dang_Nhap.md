# UC02: Đăng Nhập

## 1. Thông Tin Use Case

- **Mã Use Case:** UC02
- **Tên Use Case:** Đăng nhập tài khoản (Login)
- **Tác nhân chính (Actor):** Customer, Organizer, Staff, Admin
- **Mô tả nghiệp vụ:** Cho phép các tác nhân đăng nhập vào hệ thống bằng Email và Mật khẩu để sử dụng các tính năng theo phân quyền.
- **Tiền điều kiện (Pre-conditions):** Người dùng đã có tài khoản trên hệ thống EventHub.
- **Hậu điều kiện (Post-conditions):** Người dùng được xác thực danh tính thành công, hệ thống lưu token/cookie phiên đăng nhập, điều hướng người dùng tới trang đích phù hợp với vai trò (Role). Giao diện đổi nút 'Đăng nhập / Đăng ký' thành avatar và menu 'Tài khoản'.

## 2. Danh Sách Test Cases Tổng Hợp

| STT | Mã Test Case | Tên / Mục Tiêu Test Case | Phân Loại | Mức Độ Ưu Tiên |
|:---:|:---|:---|:---|:---:|
| 1 | **TC_UC02_01** | Đăng nhập thành công với vai trò Customer bằng thông tin chính xác | Functional (Happy Path) | **High** |
| 2 | **TC_UC02_02** | Đăng nhập thành công với vai trò Organizer và điều hướng đúng giao diện quản lý | Functional (Happy Path) | **High** |
| 3 | **TC_UC02_03** | Đăng nhập thành công với vai trò Admin | Functional (Happy Path) | **High** |
| 4 | **TC_UC02_04** | Đăng nhập thất bại khi nhập sai Mật khẩu | Security / Negative | **High** |
| 5 | **TC_UC02_05** | Đăng nhập thất bại khi Email chưa tồn tại trong hệ thống | Negative | **High** |
| 6 | **TC_UC02_06** | Đăng nhập thất bại khi bỏ trống Email hoặc Mật khẩu | Validation | **Medium** |
| 7 | **TC_UC02_07** | Đăng nhập thất bại với tài khoản đang ở trạng thái Bị khóa (BANNED/LOCKED) | Business Rule / Security | **High** |
| 8 | **TC_UC02_08** | Kiểm tra liên kết chuyển hướng 'Đăng ký' và 'Quên mật khẩu' | UI / Navigation | **Low** |

---

## 3. Thiết Kế Chi Tiết Từng Test Case

### TC_UC02_01: Đăng nhập thành công với vai trò Customer bằng thông tin chính xác

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC02_01` |
| **Tên Test Case** | Đăng nhập thành công với vai trò Customer bằng thông tin chính xác |
| **Use Case liên quan** | UC02 - Đăng nhập tài khoản (Login) |
| **Phân loại kiểm thử** | Functional (Happy Path) |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Tài khoản Customer 'seed.customer1@eventhub.local' (hoặc 'vol.customer1@eventhub.test') đã kích hoạt trong CSDL seed data. |
| **Dữ liệu đầu vào (Test Data)** | Email: 'seed.customer1@eventhub.local'<br/>Mật khẩu: 'EventHub@123' |
| **Các bước thực hiện** | 1. Tại trang chủ, click nút 'Đăng nhập'.<br/>2. Nhập Email = 'seed.customer1@eventhub.local'.<br/>3. Nhập Mật khẩu = 'EventHub@123'.<br/>4. Nhấn nút 'Đăng nhập'. |
| **Kết quả mong đợi** | Hệ thống thông báo đăng nhập thành công. Điều hướng về trang chủ. Header hiển thị Avatar và nút 'Tài khoản'. Nhận được Access Token và Refresh Token hợp lệ. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC02_02: Đăng nhập thành công với vai trò Organizer và điều hướng đúng giao diện quản lý

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC02_02` |
| **Tên Test Case** | Đăng nhập thành công với vai trò Organizer và điều hướng đúng giao diện quản lý |
| **Use Case liên quan** | UC02 - Đăng nhập tài khoản (Login) |
| **Phân loại kiểm thử** | Functional (Happy Path) |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Tài khoản Organizer 'vol.organizer1@eventhub.test' (hoặc 'seed.organizer1@eventhub.local') tồn tại và đang hoạt động trong seed data. |
| **Dữ liệu đầu vào (Test Data)** | Email: 'vol.organizer1@eventhub.test'<br/>Mật khẩu: 'EventHub@123' |
| **Các bước thực hiện** | 1. Nhấn nút 'Đăng nhập'.<br/>2. Nhập thông tin đăng nhập của Organizer.<br/>3. Nhấn 'Đăng nhập'. |
| **Kết quả mong đợi** | Đăng nhập thành công, hệ thống chuyển hướng Organizer vào trang Quản lý sự kiện / Dashboard của Ban tổ chức. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC02_03: Đăng nhập thành công với vai trò Admin

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC02_03` |
| **Tên Test Case** | Đăng nhập thành công với vai trò Admin |
| **Use Case liên quan** | UC02 - Đăng nhập tài khoản (Login) |
| **Phân loại kiểm thử** | Functional (Happy Path) |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Tài khoản Admin 'seed.admin@eventhub.local' đã được tạo trong CSDL seed data. |
| **Dữ liệu đầu vào (Test Data)** | Email: 'seed.admin@eventhub.local'<br/>Mật khẩu: 'EventHub@123' |
| **Các bước thực hiện** | 1. Mở trang Đăng nhập.<br/>2. Nhập Email và Mật khẩu của Admin.<br/>3. Nhấn 'Đăng nhập'. |
| **Kết quả mong đợi** | Đăng nhập thành công, điều hướng vào trang Quản trị Admin (/admin). |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC02_04: Đăng nhập thất bại khi nhập sai Mật khẩu

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC02_04` |
| **Tên Test Case** | Đăng nhập thất bại khi nhập sai Mật khẩu |
| **Use Case liên quan** | UC02 - Đăng nhập tài khoản (Login) |
| **Phân loại kiểm thử** | Security / Negative |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Tài khoản 'seed.customer1@eventhub.local' tồn tại trong CSDL. |
| **Dữ liệu đầu vào (Test Data)** | Email: 'seed.customer1@eventhub.local'<br/>Mật khẩu sai: 'SaiMatKhau123@' |
| **Các bước thực hiện** | 1. Nhập Email = 'seed.customer1@eventhub.local'.<br/>2. Nhập Mật khẩu sai = 'SaiMatKhau123@'.<br/>3. Nhấn 'Đăng nhập'. |
| **Kết quả mong đợi** | Đăng nhập không thành công, hệ thống hiển thị thông báo lỗi: 'Email hoặc mật khẩu không chính xác'. Không tiết lộ chi tiết sai mật khẩu hay sai email. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC02_05: Đăng nhập thất bại khi Email chưa tồn tại trong hệ thống

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC02_05` |
| **Tên Test Case** | Đăng nhập thất bại khi Email chưa tồn tại trong hệ thống |
| **Use Case liên quan** | UC02 - Đăng nhập tài khoản (Login) |
| **Phân loại kiểm thử** | Negative |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Email 'notfound_user_99999@eventhub.test' chưa từng đăng ký. |
| **Dữ liệu đầu vào (Test Data)** | Email: 'notfound_user_99999@eventhub.test'<br/>Mật khẩu: 'Pass123456@' |
| **Các bước thực hiện** | 1. Nhập Email = 'notfound_user_99999@eventhub.test'.<br/>2. Nhập Mật khẩu = 'Pass123456@'.<br/>3. Nhấn 'Đăng nhập'. |
| **Kết quả mong đợi** | Đăng nhập thất bại, hệ thống báo lỗi: 'Email hoặc mật khẩu không chính xác'. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC02_06: Đăng nhập thất bại khi bỏ trống Email hoặc Mật khẩu

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC02_06` |
| **Tên Test Case** | Đăng nhập thất bại khi bỏ trống Email hoặc Mật khẩu |
| **Use Case liên quan** | UC02 - Đăng nhập tài khoản (Login) |
| **Phân loại kiểm thử** | Validation |
| **Mức độ ưu tiên** | **Medium** |
| **Tiền điều kiện** | Đang ở màn hình Đăng nhập. |
| **Dữ liệu đầu vào (Test Data)** | Trường hợp 1: Trống cả hai<br/>Trường hợp 2: Trống Email<br/>Trường hợp 3: Trống Mật khẩu |
| **Các bước thực hiện** | 1. Mở form Đăng nhập.<br/>2. Nhấn nút 'Đăng nhập' mà không điền thông tin.<br/>3. Điền Email, bỏ trống Mật khẩu rồi bấm 'Đăng nhập'. |
| **Kết quả mong đợi** | Hệ thống chặn submit, hiển thị thông báo lỗi 'Vui lòng nhập email' hoặc 'Vui lòng nhập mật khẩu'. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC02_07: Đăng nhập thất bại với tài khoản đang ở trạng thái Bị khóa (BANNED/LOCKED)

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC02_07` |
| **Tên Test Case** | Đăng nhập thất bại với tài khoản đang ở trạng thái Bị khóa (BANNED/LOCKED) |
| **Use Case liên quan** | UC02 - Đăng nhập tài khoản (Login) |
| **Phân loại kiểm thử** | Business Rule / Security |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Tài khoản 'vol.customer36@eventhub.test' có status = 'LOCKED' trong seeddata-large.sql. |
| **Dữ liệu đầu vào (Test Data)** | Email: 'vol.customer36@eventhub.test'<br/>Mật khẩu: 'EventHub@123' |
| **Các bước thực hiện** | 1. Nhập thông tin của tài khoản bị khóa.<br/>2. Nhấn nút 'Đăng nhập'. |
| **Kết quả mong đợi** | Hệ thống từ chối đăng nhập (HTTP 403 Forbidden), hiển thị thông báo: 'Tài khoản của bạn đã bị khóa. Vui lòng liên hệ quản trị viên để biết thêm chi tiết'. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC02_08: Kiểm tra liên kết chuyển hướng 'Đăng ký' và 'Quên mật khẩu'

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC02_08` |
| **Tên Test Case** | Kiểm tra liên kết chuyển hướng 'Đăng ký' và 'Quên mật khẩu' |
| **Use Case liên quan** | UC02 - Đăng nhập tài khoản (Login) |
| **Phân loại kiểm thử** | UI / Navigation |
| **Mức độ ưu tiên** | **Low** |
| **Tiền điều kiện** | Đang ở màn hình Đăng nhập. |
| **Dữ liệu đầu vào (Test Data)** | N/A |
| **Các bước thực hiện** | 1. Nhấn vào liên kết 'Đăng ký'.<br/>2. Quay lại trang đăng nhập, nhấn vào liên kết 'Quên mật khẩu'. |
| **Kết quả mong đợi** | 1. Chuyển hướng chính xác sang trang Đăng ký (/register).<br/>2. Chuyển hướng chính xác sang trang Quên mật khẩu (/forgot-password). |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

