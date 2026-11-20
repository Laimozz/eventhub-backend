# UC03: Đăng Xuất

## 1. Thông Tin Use Case

- **Mã Use Case:** UC03
- **Tên Use Case:** Đăng xuất tài khoản (Logout)
- **Tác nhân chính (Actor):** Customer, Organizer, Staff, Admin
- **Mô tả nghiệp vụ:** Cho phép người dùng chủ động kết thúc phiên làm việc hiện tại một cách an toàn.
- **Tiền điều kiện (Pre-conditions):** Người dùng đã đăng nhập vào hệ thống.
- **Hậu điều kiện (Post-conditions):** Phiên làm việc của người dùng bị hủy hoàn toàn, token/cookie bị thu hồi (revoke/clear). Người dùng trở về trạng thái Guest, nút 'Tài khoản' chuyển lại thành 'Đăng ký / Đăng nhập'.

## 2. Danh Sách Test Cases Tổng Hợp

| STT | Mã Test Case | Tên / Mục Tiêu Test Case | Phân Loại | Mức Độ Ưu Tiên |
|:---:|:---|:---|:---|:---:|
| 1 | **TC_UC03_01** | Đăng xuất thành công khi người dùng xác nhận Đăng xuất | Functional (Happy Path) | **High** |
| 2 | **TC_UC03_02** | Hủy bỏ đăng xuất khi người dùng chọn 'Hủy' trong hộp thoại xác nhận | Functional (Alternative Flow) | **Medium** |
| 3 | **TC_UC03_03** | Kiểm tra bảo mật không thể truy cập lại trang riêng tư sau khi đăng xuất bằng nút Back của trình duyệt | Security | **High** |
| 4 | **TC_UC03_04** | Đăng xuất trên một tab đồng bộ trạng thái đăng xuất trên các tab khác (Multi-tab synchronization) | Security / Session | **Medium** |
| 5 | **TC_UC03_05** | Kiểm tra giao diện Menu Dropdown khi click vào nút Tài khoản | UI Testing | **Low** |

---

## 3. Thiết Kế Chi Tiết Từng Test Case

### TC_UC03_01: Đăng xuất thành công khi người dùng xác nhận Đăng xuất

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC03_01` |
| **Tên Test Case** | Đăng xuất thành công khi người dùng xác nhận Đăng xuất |
| **Use Case liên quan** | UC03 - Đăng xuất tài khoản (Logout) |
| **Phân loại kiểm thử** | Functional (Happy Path) |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Người dùng đã đăng nhập, đang ở bất kỳ trang nào. |
| **Dữ liệu đầu vào (Test Data)** | N/A |
| **Các bước thực hiện** | 1. Nhấn vào nút 'Tài khoản' trên thanh điều hướng header.<br/>2. Từ menu dropdown hiển thị, chọn 'Đăng xuất'.<br/>3. Hộp thoại xác nhận hiển thị 'Bạn có chắc chắn muốn đăng xuất?'. Nhấn nút 'Xác nhận'. |
| **Kết quả mong đợi** | Hệ thống thực hiện API logout, xóa Access Token trong bộ nhớ và xóa Refresh Token cookie. Header chuyển về nút 'Đăng ký / Đăng nhập'. Điều hướng về trang chủ hoặc trang đăng nhập. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC03_02: Hủy bỏ đăng xuất khi người dùng chọn 'Hủy' trong hộp thoại xác nhận

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC03_02` |
| **Tên Test Case** | Hủy bỏ đăng xuất khi người dùng chọn 'Hủy' trong hộp thoại xác nhận |
| **Use Case liên quan** | UC03 - Đăng xuất tài khoản (Logout) |
| **Phân loại kiểm thử** | Functional (Alternative Flow) |
| **Mức độ ưu tiên** | **Medium** |
| **Tiền điều kiện** | Người dùng đã mở dropdown và nhấn 'Đăng xuất'. |
| **Dữ liệu đầu vào (Test Data)** | N/A |
| **Các bước thực hiện** | 1. Nhấn 'Đăng xuất' từ dropdown tài khoản.<br/>2. Tại popup xác nhận 'Bạn có chắc chắn muốn đăng xuất?', nhấn nút 'Hủy'. |
| **Kết quả mong đợi** | Hộp thoại đóng lại. Phiên làm việc của người dùng vẫn được duy trì, trạng thái đăng nhập không thay đổi. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC03_03: Kiểm tra bảo mật không thể truy cập lại trang riêng tư sau khi đăng xuất bằng nút Back của trình duyệt

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC03_03` |
| **Tên Test Case** | Kiểm tra bảo mật không thể truy cập lại trang riêng tư sau khi đăng xuất bằng nút Back của trình duyệt |
| **Use Case liên quan** | UC03 - Đăng xuất tài khoản (Logout) |
| **Phân loại kiểm thử** | Security |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Người dùng đăng nhập, truy cập vào trang cá nhân (/profile hoặc /my-bookings), sau đó thực hiện Đăng xuất thành công. |
| **Dữ liệu đầu vào (Test Data)** | N/A |
| **Các bước thực hiện** | 1. Sau khi đăng xuất thành công và bị chuyển về trang chủ/login.<br/>2. Nhấn nút 'Back' trên trình duyệt để quay lại trang cá nhân trước đó. |
| **Kết quả mong đợi** | Hệ thống phát hiện token đã bị thu hồi/xóa, không cho phép xem dữ liệu riêng tư và tự động chuyển hướng về trang Đăng nhập. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC03_04: Đăng xuất trên một tab đồng bộ trạng thái đăng xuất trên các tab khác (Multi-tab synchronization)

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC03_04` |
| **Tên Test Case** | Đăng xuất trên một tab đồng bộ trạng thái đăng xuất trên các tab khác (Multi-tab synchronization) |
| **Use Case liên quan** | UC03 - Đăng xuất tài khoản (Logout) |
| **Phân loại kiểm thử** | Security / Session |
| **Mức độ ưu tiên** | **Medium** |
| **Tiền điều kiện** | Người dùng mở đồng thời 2 tab trình duyệt trên cùng phiên đăng nhập. |
| **Dữ liệu đầu vào (Test Data)** | N/A |
| **Các bước thực hiện** | 1. Mở Tab A và Tab B cùng đăng nhập.<br/>2. Tại Tab A, thực hiện Đăng xuất thành công.<br/>3. Chuyển sang Tab B và thực hiện thao tác (ví dụ click xem booking hoặc F5). |
| **Kết quả mong đợi** | Tab B nhận diện phiên đăng nhập đã kết thúc, cập nhật header về trạng thái chưa đăng nhập hoặc điều hướng về Login khi thực hiện request cần auth. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC03_05: Kiểm tra giao diện Menu Dropdown khi click vào nút Tài khoản

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC03_05` |
| **Tên Test Case** | Kiểm tra giao diện Menu Dropdown khi click vào nút Tài khoản |
| **Use Case liên quan** | UC03 - Đăng xuất tài khoản (Logout) |
| **Phân loại kiểm thử** | UI Testing |
| **Mức độ ưu tiên** | **Low** |
| **Tiền điều kiện** | Người dùng đã đăng nhập thành công. |
| **Dữ liệu đầu vào (Test Data)** | N/A |
| **Các bước thực hiện** | 1. Click vào avatar/tên người dùng trên header. |
| **Kết quả mong đợi** | Dropdown hiển thị đầy đủ các mục: 'Xem thông tin cá nhân', 'Booking của tôi', 'Vé của tôi', 'Đăng xuất' với định dạng rõ ràng, icon trực quan. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

