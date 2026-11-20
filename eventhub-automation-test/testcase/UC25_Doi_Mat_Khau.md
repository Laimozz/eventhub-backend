# UC25: Đổi Mật Khẩu

## 1. Thông Tin Use Case

- **Mã Use Case:** UC25
- **Tên Use Case:** Đổi mật khẩu (Change Password)
- **Tác nhân chính (Actor):** Customer, Organizer, Staff, Admin (Người dùng đã đăng nhập)
- **Mô tả nghiệp vụ:** Cho phép người dùng đã xác thực thay đổi mật khẩu đăng nhập của tài khoản để nâng cao tính bảo mật. Hệ thống yêu cầu xác thực mật khẩu hiện tại, kiểm tra quy tắc độ mạnh mật khẩu mới, kiểm tra trùng khớp với mật khẩu cũ và lịch sử mật khẩu đã sử dụng (chính sách không trùng mật khẩu trong 45 ngày gần nhất), sau đó cập nhật mật khẩu mới (mã hóa BCrypt).
- **Tiền điều kiện (Pre-conditions):** Người dùng đã đăng nhập thành công vào hệ thống EventHub và có tài khoản đang ở trạng thái hoạt động (ACTIVE).
- **Hậu điều kiện (Post-conditions):** Mật khẩu mới được băm và lưu vào cơ sở dữ liệu. Mật khẩu cũ không còn hiệu lực. Người dùng có thể đăng nhập bằng mật khẩu mới và bị từ chối khi dùng mật khẩu cũ.

## 2. Danh Sách Test Cases Tổng Hợp

| STT | Mã Test Case | Tên / Mục Tiêu Test Case | Phân Loại | Mức Độ Ưu Tiên |
|:---:|:---|:---|:---|:---:|
| 1 | **TC25_01** | Đổi mật khẩu hợp lệ | Functional (Happy Path) | **High** |
| 2 | **TC25_02** | Nhập sai mật khẩu hiện tại | Security / Validation (Negative) | **High** |
| 3 | **TC25_03** | Để trống các trường bắt buộc | Validation (Negative) | **Medium** |
| 4 | **TC25_04** | Mật khẩu mới và xác nhận không khớp | Validation (Negative) | **High** |
| 5 | **TC25_05** | Mật khẩu mới không đáp ứng yêu cầu | Security / Validation (Negative) | **High** |
| 6 | **TC25_06** | Mật khẩu mới trùng mật khẩu hiện tại | Business Rule (Negative) | **High** |
| 7 | **TC25_07** | Mật khẩu mới trùng mật khẩu đã sử dụng trong 45 ngày gần nhất | Business Rule / Security (Negative) | **High** |
| 8 | **TC25_08** | Mật khẩu mới trùng mật khẩu đã sử dụng cách đây hơn 45 ngày | Business Rule / Security (Positive) | **Medium** |
| 9 | **TC25_09** | Đăng nhập bằng mật khẩu mới sau khi đổi | Integration / Functional (Positive) | **High** |
| 10 | **TC25_10** | Đăng nhập bằng mật khẩu cũ sau khi đổi | Security / Integration (Negative) | **High** |

---

## 3. Thiết Kế Chi Tiết Từng Test Case

### TC25_01: Đổi mật khẩu hợp lệ

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC25_01` |
| **Tên Test Case** | Đổi mật khẩu hợp lệ |
| **Use Case liên quan** | UC25 - Đổi mật khẩu (Change Password) |
| **Phân loại kiểm thử** | Functional (Happy Path) |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Người dùng đã đăng nhập thành công vào hệ thống. Mật khẩu hiện tại đang hoạt động là `EventHub@123`. |
| **Dữ liệu đầu vào (Test Data)** | Mật khẩu hiện tại: `EventHub@123`<br/>Mật khẩu mới: `NewPass@2026!`<br/>Xác nhận mật khẩu mới: `NewPass@2026!` |
| **Các bước thực hiện** | 1. Tại menu người dùng, chọn mục 'Thông tin cá nhân' hoặc 'Đổi mật khẩu'.<br/>2. Nhập Mật khẩu hiện tại = `EventHub@123`.<br/>3. Nhập Mật khẩu mới = `NewPass@2026!`.<br/>4. Nhập Xác nhận mật khẩu mới = `NewPass@2026!`.<br/>5. Nhấn nút 'Đổi mật khẩu' (hoặc 'Xác nhận'). |
| **Kết quả mong đợi** | Đổi mật khẩu thành công. Hệ thống hiển thị thông báo xác nhận thành công. Mật khẩu mới được băm bằng BCrypt và cập nhật vào cơ sở dữ liệu. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC25_02: Nhập sai mật khẩu hiện tại

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC25_02` |
| **Tên Test Case** | Nhập sai mật khẩu hiện tại |
| **Use Case liên quan** | UC25 - Đổi mật khẩu (Change Password) |
| **Phân loại kiểm thử** | Security / Validation (Negative) |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Người dùng đã đăng nhập vào hệ thống. Mật khẩu hiện tại đúng là `EventHub@123`. |
| **Dữ liệu đầu vào (Test Data)** | Mật khẩu hiện tại: `WrongPassword@999`<br/>Mật khẩu mới: `NewPass@2026!`<br/>Xác nhận mật khẩu: `NewPass@2026!` |
| **Các bước thực hiện** | 1. Mở màn hình/modal Đổi mật khẩu.<br/>2. Nhập Mật khẩu hiện tại = `WrongPassword@999` (sai mật khẩu).<br/>3. Nhập Mật khẩu mới = `NewPass@2026!`.<br/>4. Nhập Xác nhận mật khẩu = `NewPass@2026!`.<br/>5. Nhấn nút 'Đổi mật khẩu'. |
| **Kết quả mong đợi** | Hiển thị thông báo lỗi. Hệ thống từ chối đổi mật khẩu, thông báo: 'Mật khẩu hiện tại không chính xác'. Mật khẩu trong CSDL không bị thay đổi. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC25_03: Để trống các trường bắt buộc

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC25_03` |
| **Tên Test Case** | Để trống các trường bắt buộc |
| **Use Case liên quan** | UC25 - Đổi mật khẩu (Change Password) |
| **Phân loại kiểm thử** | Validation (Negative) |
| **Mức độ ưu tiên** | **Medium** |
| **Tiền điều kiện** | Đang ở màn hình/form Đổi mật khẩu. |
| **Dữ liệu đầu vào (Test Data)** | Để trống lần lượt hoặc toàn bộ các trường: Mật khẩu hiện tại, Mật khẩu mới, Xác nhận mật khẩu mới. |
| **Các bước thực hiện** | 1. Để trống toàn bộ 3 trường và nhấn nút 'Đổi mật khẩu'.<br/>2. Thử lần lượt: điền Mật khẩu hiện tại nhưng để trống Mật khẩu mới và Xác nhận mật khẩu.<br/>3. Điền Mật khẩu hiện tại và Mật khẩu mới nhưng để trống Xác nhận mật khẩu. |
| **Kết quả mong đợi** | Yêu cầu nhập đầy đủ thông tin. Hệ thống ngăn chặn gửi form và hiển thị cảnh báo lỗi tương ứng tại từng trường bị bỏ trống (ví dụ: 'Mật khẩu hiện tại không được để trống', 'Mật khẩu mới không được để trống', 'Vui lòng xác nhận mật khẩu mới'). |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC25_04: Mật khẩu mới và xác nhận không khớp

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC25_04` |
| **Tên Test Case** | Mật khẩu mới và xác nhận không khớp |
| **Use Case liên quan** | UC25 - Đổi mật khẩu (Change Password) |
| **Phân loại kiểm thử** | Validation (Negative) |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Đang ở form Đổi mật khẩu. |
| **Dữ liệu đầu vào (Test Data)** | Mật khẩu hiện tại: `EventHub@123`<br/>Mật khẩu mới: `NewPass@2026!`<br/>Xác nhận mật khẩu: `NewPass@2026Different!` |
| **Các bước thực hiện** | 1. Nhập Mật khẩu hiện tại đúng = `EventHub@123`.<br/>2. Nhập Mật khẩu mới = `NewPass@2026!`.<br/>3. Nhập Xác nhận mật khẩu = `NewPass@2026Different!`.<br/>4. Nhấn nút 'Đổi mật khẩu'. |
| **Kết quả mong đợi** | Hiển thị thông báo không khớp. Hệ thống báo lỗi: 'Xác nhận mật khẩu mới không trùng khớp' (hoặc 'Mật khẩu xác nhận không khớp') và không cho phép submit. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC25_05: Mật khẩu mới không đáp ứng yêu cầu

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC25_05` |
| **Tên Test Case** | Mật khẩu mới không đáp ứng yêu cầu |
| **Use Case liên quan** | UC25 - Đổi mật khẩu (Change Password) |
| **Phân loại kiểm thử** | Security / Validation (Negative) |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Đang ở form Đổi mật khẩu. Mật khẩu hiện tại hợp lệ. |
| **Dữ liệu đầu vào (Test Data)** | Các mẫu mật khẩu không hợp lệ:<br/>- Ngắn hơn 8 ký tự: `Pass1@`<br/>- Thiếu chữ hoa: `newpass@123`<br/>- Thiếu chữ thường: `NEWPASS@123`<br/>- Thiếu số: `NewPass@!#`<br/>- Thiếu ký tự đặc biệt: `NewPass123`<br/>- Dài hơn 72 ký tự |
| **Các bước thực hiện** | 1. Nhập Mật khẩu hiện tại đúng.<br/>2. Nhập Mật khẩu mới lần lượt với các mẫu dữ liệu không hợp lệ ở trên.<br/>3. Nhập Xác nhận mật khẩu trùng với mật khẩu mới.<br/>4. Nhấn nút 'Đổi mật khẩu'. |
| **Kết quả mong đợi** | Từ chối đổi mật khẩu. Hệ thống hiển thị thông báo lỗi: 'Mật khẩu mới phải từ 8 đến 72 ký tự, bao gồm ít nhất 1 chữ hoa, 1 chữ thường, 1 chữ số và 1 ký tự đặc biệt'. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC25_06: Mật khẩu mới trùng mật khẩu hiện tại

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC25_06` |
| **Tên Test Case** | Mật khẩu mới trùng mật khẩu hiện tại |
| **Use Case liên quan** | UC25 - Đổi mật khẩu (Change Password) |
| **Phân loại kiểm thử** | Business Rule (Negative) |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Đang ở form Đổi mật khẩu. Mật khẩu đang sử dụng là `EventHub@123`. |
| **Dữ liệu đầu vào (Test Data)** | Mật khẩu hiện tại: `EventHub@123`<br/>Mật khẩu mới: `EventHub@123`<br/>Xác nhận mật khẩu: `EventHub@123` |
| **Các bước thực hiện** | 1. Nhập Mật khẩu hiện tại = `EventHub@123`.<br/>2. Nhập Mật khẩu mới = `EventHub@123`.<br/>3. Nhập Xác nhận mật khẩu = `EventHub@123`.<br/>4. Nhấn nút 'Đổi mật khẩu'. |
| **Kết quả mong đợi** | Từ chối đổi mật khẩu. Hệ thống hiển thị thông báo lỗi: 'Mật khẩu mới không được trùng với mật khẩu hiện tại'. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC25_07: Mật khẩu mới trùng mật khẩu đã sử dụng trong 45 ngày gần nhất

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC25_07` |
| **Tên Test Case** | Mật khẩu mới trùng mật khẩu đã sử dụng trong 45 ngày gần nhất |
| **Use Case liên quan** | UC25 - Đổi mật khẩu (Change Password) |
| **Phân loại kiểm thử** | Business Rule / Security (Negative) |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Tài khoản có lịch sử thay đổi mật khẩu; trong đó mật khẩu cũ `OldPass@2026!` đã được sử dụng cách đây 20 ngày ($\le 45$ ngày). Mật khẩu hiện tại là `CurrentPass@2026!`. |
| **Dữ liệu đầu vào (Test Data)** | Mật khẩu hiện tại: `CurrentPass@2026!`<br/>Mật khẩu mới: `OldPass@2026!` (mật khẩu đã dùng 20 ngày trước)<br/>Xác nhận mật khẩu: `OldPass@2026!` |
| **Các bước thực hiện** | 1. Nhập Mật khẩu hiện tại = `CurrentPass@2026!`.<br/>2. Nhập Mật khẩu mới = `OldPass@2026!`.<br/>3. Nhập Xác nhận mật khẩu = `OldPass@2026!`.<br/>4. Nhấn nút 'Đổi mật khẩu'. |
| **Kết quả mong đợi** | Từ chối đổi mật khẩu. Hệ thống kiểm tra lịch sử mật khẩu, phát hiện trùng trong phạm vi 45 ngày gần nhất và hiển thị thông báo: 'Mật khẩu mới không được trùng với mật khẩu đã sử dụng trong vòng 45 ngày gần nhất'. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC25_08: Mật khẩu mới trùng mật khẩu đã sử dụng cách đây hơn 45 ngày

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC25_08` |
| **Tên Test Case** | Mật khẩu mới trùng mật khẩu đã sử dụng cách đây hơn 45 ngày |
| **Use Case liên quan** | UC25 - Đổi mật khẩu (Change Password) |
| **Phân loại kiểm thử** | Business Rule / Security (Positive) |
| **Mức độ ưu tiên** | **Medium** |
| **Tiền điều kiện** | Tài khoản có lịch sử thay đổi mật khẩu; trong đó mật khẩu cũ `AncientPass@2025!` đã được sử dụng cách đây 60 ngày ($> 45$ ngày). Mật khẩu hiện tại là `CurrentPass@2026!`. |
| **Dữ liệu đầu vào (Test Data)** | Mật khẩu hiện tại: `CurrentPass@2026!`<br/>Mật khẩu mới: `AncientPass@2025!`<br/>Xác nhận mật khẩu: `AncientPass@2025!` |
| **Các bước thực hiện** | 1. Nhập Mật khẩu hiện tại = `CurrentPass@2026!`.<br/>2. Nhập Mật khẩu mới = `AncientPass@2025!`.<br/>3. Nhập Xác nhận mật khẩu = `AncientPass@2025!`.<br/>4. Nhấn nút 'Đổi mật khẩu'. |
| **Kết quả mong đợi** | Cho phép đổi mật khẩu nếu các điều kiện khác hợp lệ. Hệ thống chấp nhận mật khẩu đã hết hạn kiểm tra lịch sử 45 ngày, cập nhật mật khẩu mới thành công và hiển thị thông báo xác nhận. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC25_09: Đăng nhập bằng mật khẩu mới sau khi đổi

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC25_09` |
| **Tên Test Case** | Đăng nhập bằng mật khẩu mới sau khi đổi |
| **Use Case liên quan** | UC25 - Đổi mật khẩu (Change Password) |
| **Phân loại kiểm thử** | Integration / Functional (Positive) |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Tài khoản vừa thực hiện đổi mật khẩu thành công sang mật khẩu mới `NewPass@2026!`. Người dùng đang ở màn hình Đăng nhập (hoặc đã đăng xuất). |
| **Dữ liệu đầu vào (Test Data)** | Email: `demo20.customer1@example.test`<br/>Mật khẩu: `NewPass@2026!` |
| **Các bước thực hiện** | 1. Truy cập vào trang Đăng nhập EventHub.<br/>2. Nhập Email = `demo20.customer1@example.test`.<br/>3. Nhập Mật khẩu = `NewPass@2026!` (mật khẩu mới vừa đổi).<br/>4. Nhấn nút 'Đăng nhập'. |
| **Kết quả mong đợi** | Đăng nhập thành công. Hệ thống xác thực thông tin chính xác, cấp token phiên đăng nhập và chuyển hướng người dùng vào trang chủ/dashboard của hệ thống. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC25_10: Đăng nhập bằng mật khẩu cũ sau khi đổi

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC25_10` |
| **Tên Test Case** | Đăng nhập bằng mật khẩu cũ sau khi đổi |
| **Use Case liên quan** | UC25 - Đổi mật khẩu (Change Password) |
| **Phân loại kiểm thử** | Security / Integration (Negative) |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Tài khoản đã đổi mật khẩu thành công từ `EventHub@123` sang `NewPass@2026!`. Đang ở màn hình Đăng nhập. |
| **Dữ liệu đầu vào (Test Data)** | Email: `demo20.customer1@example.test`<br/>Mật khẩu: `EventHub@123` (mật khẩu cũ trước khi đổi) |
| **Các bước thực hiện** | 1. Truy cập vào trang Đăng nhập EventHub.<br/>2. Nhập Email = `demo20.customer1@example.test`.<br/>3. Nhập Mật khẩu = `EventHub@123`.<br/>4. Nhấn nút 'Đăng nhập'. |
| **Kết quả mong đợi** | Từ chối đăng nhập. Hệ thống phát hiện mật khẩu cũ không còn hiệu lực, hiển thị thông báo lỗi: 'Email hoặc mật khẩu không chính xác' (HTTP 401 Unauthorized) và không cấp quyền truy cập. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

