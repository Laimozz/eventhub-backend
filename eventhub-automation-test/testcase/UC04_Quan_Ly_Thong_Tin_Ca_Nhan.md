# UC04: Quản Lý Thông Tin Cá Nhân

## 1. Thông Tin Use Case

- **Mã Use Case:** UC04
- **Tên Use Case:** Quản lý thông tin cá nhân (Profile Management)
- **Tác nhân chính (Actor):** Customer, Organizer, Staff, Admin
- **Mô tả nghiệp vụ:** Cho phép người dùng xem thông tin cá nhân hiện tại và cập nhật các trường thông tin (Họ tên, SĐT, Ngày sinh, Giới tính, Ảnh đại diện, Đổi mật khẩu).
- **Tiền điều kiện (Pre-conditions):** Người dùng đã đăng nhập thành công vào hệ thống.
- **Hậu điều kiện (Post-conditions):** Thông tin hồ sơ người dùng trong cơ sở dữ liệu được cập nhật mới. Người dùng xem được thông tin cá nhân đã cập nhật.

## 2. Danh Sách Test Cases Tổng Hợp

| STT | Mã Test Case | Tên / Mục Tiêu Test Case | Phân Loại | Mức Độ Ưu Tiên |
|:---:|:---|:---|:---|:---:|
| 1 | **TC_UC04_01** | Xem thông tin cá nhân chính xác từ cơ sở dữ liệu | Functional (Happy Path) | **High** |
| 2 | **TC_UC04_02** | Cập nhật thành công Số điện thoại và Ngày sinh với dữ liệu hợp lệ | Functional (Happy Path) | **High** |
| 3 | **TC_UC04_03** | Cập nhật thông tin cá nhân thất bại khi để trống Họ và tên | Validation (Negative) | **High** |
| 4 | **TC_UC04_04** | Cập nhật thông tin cá nhân thất bại khi Số điện thoại sai định dạng | Validation (Negative) | **Medium** |
| 5 | **TC_UC04_05** | Cập nhật thông tin thất bại khi Ngày sinh lớn hơn ngày hiện tại | Validation / Logic | **Medium** |
| 6 | **TC_UC04_06** | Tải lên ảnh đại diện (Avatar) mới thành công | Functional (File Upload) | **Medium** |
| 7 | **TC_UC04_07** | Tải lên ảnh đại diện thất bại khi chọn file không phải định dạng ảnh hoặc vượt quá dung lượng | Security / Validation | **Medium** |
| 8 | **TC_UC04_08** | Kiểm tra trường Email và Vai trò ở chế độ chỉ đọc (Read-only) | UI / Security | **Low** |

---

## 3. Thiết Kế Chi Tiết Từng Test Case

### TC_UC04_01: Xem thông tin cá nhân chính xác từ cơ sở dữ liệu

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC04_01` |
| **Tên Test Case** | Xem thông tin cá nhân chính xác từ cơ sở dữ liệu |
| **Use Case liên quan** | UC04 - Quản lý thông tin cá nhân (Profile Management) |
| **Phân loại kiểm thử** | Functional (Happy Path) |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Người dùng đã đăng nhập với tài khoản Customer seed data ('seed.customer1@eventhub.local' hoặc 'vol.customer1@eventhub.test'). |
| **Dữ liệu đầu vào (Test Data)** | Họ và tên: 'Nguyễn Gia Bảo'<br/>Email: 'seed.customer1@eventhub.local'<br/>SĐT: '0908000009'<br/>Giới tính: 'MALE'<br/>Vai trò: 'CUSTOMER' |
| **Các bước thực hiện** | 1. Tại trang chủ, nhấn vào nút 'Tài khoản'.<br/>2. Chọn 'Xem thông tin cá nhân'.<br/>3. Quan sát các thông tin hiển thị trên màn hình. |
| **Kết quả mong đợi** | Màn hình thông tin cá nhân hiển thị chính xác Avatar, Họ tên, Email, Số điện thoại, Ngày sinh, Giới tính, Vai trò và nút 'Chỉnh sửa thông tin cá nhân'. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC04_02: Cập nhật thành công Số điện thoại và Ngày sinh với dữ liệu hợp lệ

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC04_02` |
| **Tên Test Case** | Cập nhật thành công Số điện thoại và Ngày sinh với dữ liệu hợp lệ |
| **Use Case liên quan** | UC04 - Quản lý thông tin cá nhân (Profile Management) |
| **Phân loại kiểm thử** | Functional (Happy Path) |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Đang ở màn hình Chỉnh sửa thông tin cá nhân. |
| **Dữ liệu đầu vào (Test Data)** | Số điện thoại mới: '0363896031'<br/>Ngày sinh mới: '30/03/2004' |
| **Các bước thực hiện** | 1. Tại màn hình thông tin cá nhân, nhấn 'Chỉnh sửa thông tin cá nhân'.<br/>2. Đổi Số điện thoại thành '0363896031'.<br/>3. Nhấn nút 'Xác nhận'. |
| **Kết quả mong đợi** | Hệ thống thông báo 'Cập nhật thông tin cá nhân thành công'. Màn hình quay về trang thông tin cá nhân với số điện thoại mới đã được cập nhật thành '0363896031'. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC04_03: Cập nhật thông tin cá nhân thất bại khi để trống Họ và tên

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC04_03` |
| **Tên Test Case** | Cập nhật thông tin cá nhân thất bại khi để trống Họ và tên |
| **Use Case liên quan** | UC04 - Quản lý thông tin cá nhân (Profile Management) |
| **Phân loại kiểm thử** | Validation (Negative) |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Đang ở form Chỉnh sửa thông tin cá nhân. |
| **Dữ liệu đầu vào (Test Data)** | Họ và tên: '' (để trống) |
| **Các bước thực hiện** | 1. Xóa trắng trường Họ và tên.<br/>2. Nhấn nút 'Xác nhận'. |
| **Kết quả mong đợi** | Hệ thống ngăn chặn lưu, hiển thị thông báo lỗi 'Họ và tên không được để trống'. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC04_04: Cập nhật thông tin cá nhân thất bại khi Số điện thoại sai định dạng

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC04_04` |
| **Tên Test Case** | Cập nhật thông tin cá nhân thất bại khi Số điện thoại sai định dạng |
| **Use Case liên quan** | UC04 - Quản lý thông tin cá nhân (Profile Management) |
| **Phân loại kiểm thử** | Validation (Negative) |
| **Mức độ ưu tiên** | **Medium** |
| **Tiền điều kiện** | Đang ở form Chỉnh sửa thông tin cá nhân. |
| **Dữ liệu đầu vào (Test Data)** | Số điện thoại không hợp lệ: '0378abc', '12345', '037877033100' (> 11 số) |
| **Các bước thực hiện** | 1. Nhập Số điện thoại = '0378abc'.<br/>2. Nhấn nút 'Xác nhận'. |
| **Kết quả mong đợi** | Hệ thống báo lỗi định dạng: 'Số điện thoại không hợp lệ (yêu cầu 10 số bắt đầu bằng đầu số Việt Nam)'. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC04_05: Cập nhật thông tin thất bại khi Ngày sinh lớn hơn ngày hiện tại

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC04_05` |
| **Tên Test Case** | Cập nhật thông tin thất bại khi Ngày sinh lớn hơn ngày hiện tại |
| **Use Case liên quan** | UC04 - Quản lý thông tin cá nhân (Profile Management) |
| **Phân loại kiểm thử** | Validation / Logic |
| **Mức độ ưu tiên** | **Medium** |
| **Tiền điều kiện** | Đang ở form Chỉnh sửa thông tin cá nhân. |
| **Dữ liệu đầu vào (Test Data)** | Ngày sinh trong tương lai: '01/01/2030' |
| **Các bước thực hiện** | 1. Chọn Ngày sinh là một ngày trong tương lai.<br/>2. Nhấn nút 'Xác nhận'. |
| **Kết quả mong đợi** | Hệ thống cảnh báo lỗi: 'Ngày sinh không thể lớn hơn ngày hiện tại'. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC04_06: Tải lên ảnh đại diện (Avatar) mới thành công

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC04_06` |
| **Tên Test Case** | Tải lên ảnh đại diện (Avatar) mới thành công |
| **Use Case liên quan** | UC04 - Quản lý thông tin cá nhân (Profile Management) |
| **Phân loại kiểm thử** | Functional (File Upload) |
| **Mức độ ưu tiên** | **Medium** |
| **Tiền điều kiện** | Chuẩn bị file ảnh avatar.jpg có kích thước 1MB hợp lệ. |
| **Dữ liệu đầu vào (Test Data)** | File ảnh avatar.jpg (kích thước < 5MB) |
| **Các bước thực hiện** | 1. Nhấn nút 'Chọn tệp từ máy tính' tại mục Ảnh đại diện.<br/>2. Chọn file avatar.jpg.<br/>3. Nhấn 'Xác nhận'. |
| **Kết quả mong đợi** | Hệ thống tải ảnh lên thành công, cập nhật URL avatar mới và hiển thị ảnh đại diện mới trên hồ sơ cũng như header. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC04_07: Tải lên ảnh đại diện thất bại khi chọn file không phải định dạng ảnh hoặc vượt quá dung lượng

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC04_07` |
| **Tên Test Case** | Tải lên ảnh đại diện thất bại khi chọn file không phải định dạng ảnh hoặc vượt quá dung lượng |
| **Use Case liên quan** | UC04 - Quản lý thông tin cá nhân (Profile Management) |
| **Phân loại kiểm thử** | Security / Validation |
| **Mức độ ưu tiên** | **Medium** |
| **Tiền điều kiện** | Chuẩn bị file tài liệu document.pdf hoặc ảnh nặng > 10MB. |
| **Dữ liệu đầu vào (Test Data)** | File: document.pdf hoặc large_image.png (> 10MB) |
| **Các bước thực hiện** | 1. Chọn tệp document.pdf để upload.<br/>2. Nhấn 'Xác nhận'. |
| **Kết quả mong đợi** | Hệ thống từ chối file và thông báo lỗi: 'Chỉ chấp nhận các tệp ảnh định dạng JPG, JPEG, PNG có dung lượng dưới 5MB'. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC04_08: Kiểm tra trường Email và Vai trò ở chế độ chỉ đọc (Read-only)

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC04_08` |
| **Tên Test Case** | Kiểm tra trường Email và Vai trò ở chế độ chỉ đọc (Read-only) |
| **Use Case liên quan** | UC04 - Quản lý thông tin cá nhân (Profile Management) |
| **Phân loại kiểm thử** | UI / Security |
| **Mức độ ưu tiên** | **Low** |
| **Tiền điều kiện** | Đang ở form Chỉnh sửa thông tin cá nhân. |
| **Dữ liệu đầu vào (Test Data)** | N/A |
| **Các bước thực hiện** | 1. Thử click và gõ thay đổi thông tin tại ô Email và ô Vai trò. |
| **Kết quả mong đợi** | Trường Email và Vai trò bị disabled/readonly, người dùng không thể tự ý sửa đổi email hoặc nâng quyền vai trò trên giao diện này. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

