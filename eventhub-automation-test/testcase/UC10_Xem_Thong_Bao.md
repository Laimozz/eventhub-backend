# UC10: Xem Thông Báo

## 1. Thông Tin Use Case

- **Mã Use Case:** UC10
- **Tên Use Case:** Xem thông báo (Notifications)
- **Tác nhân chính (Actor):** Customer, Staff, Organizer
- **Mô tả nghiệp vụ:** Cung cấp tính năng nhận và xem các thông báo về trạng thái đặt vé, cập nhật sự kiện, thông báo thanh toán và nhắc nhở thời gian diễn ra sự kiện.
- **Tiền điều kiện (Pre-conditions):** Người dùng đã đăng nhập vào hệ thống EventHub.
- **Hậu điều kiện (Post-conditions):** Người dùng xem được danh sách thông báo hệ thống, thông báo được đánh dấu là đã đọc khi tương tác.

## 2. Danh Sách Test Cases Tổng Hợp

| STT | Mã Test Case | Tên / Mục Tiêu Test Case | Phân Loại | Mức Độ Ưu Tiên |
|:---:|:---|:---|:---|:---:|
| 1 | **TC_UC10_01** | Hiển thị danh sách thông báo khi click vào biểu tượng chuông thông báo | Functional (Happy Path) | **High** |
| 2 | **TC_UC10_02** | Phân biệt giao diện trực quan giữa thông báo Chưa đọc và Đã đọc | UI Testing | **Medium** |
| 3 | **TC_UC10_03** | Nhấn vào thông báo chuyển hướng đến màn hình chi tiết tương ứng và đổi trạng thái Đã đọc | Functional (Integration) | **High** |
| 4 | **TC_UC10_04** | Hiển thị trạng thái khi người dùng không có thông báo nào | Boundary / UI | **Low** |
| 5 | **TC_UC10_05** | Đánh dấu tất cả thông báo là đã đọc (Mark all as read) | Functional | **Medium** |
| 6 | **TC_UC10_06** | Cập nhật số đếm thông báo chưa đọc (Badge count) | UI / Real-time | **Medium** |

---

## 3. Thiết Kế Chi Tiết Từng Test Case

### TC_UC10_01: Hiển thị danh sách thông báo khi click vào biểu tượng chuông thông báo

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC10_01` |
| **Tên Test Case** | Hiển thị danh sách thông báo khi click vào biểu tượng chuông thông báo |
| **Use Case liên quan** | UC10 - Xem thông báo (Notifications) |
| **Phân loại kiểm thử** | Functional (Happy Path) |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Tài khoản có thông báo mới (ví dụ: Thanh toán thành công, Nhắc nhở sự kiện). |
| **Dữ liệu đầu vào (Test Data)** | N/A |
| **Các bước thực hiện** | 1. Tại giao diện chính, click vào biểu tượng chuông 'Xem thông báo' trên header. |
| **Kết quả mong đợi** | Hiển thị dropdown thông báo chứa danh sách các thông báo với tiêu đề, thời gian (ví dụ '23/09/2026 10h38') và nội dung tóm tắt (ví dụ: 'Booking #001 đã thanh toán thành công...'). |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC10_02: Phân biệt giao diện trực quan giữa thông báo Chưa đọc và Đã đọc

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC10_02` |
| **Tên Test Case** | Phân biệt giao diện trực quan giữa thông báo Chưa đọc và Đã đọc |
| **Use Case liên quan** | UC10 - Xem thông báo (Notifications) |
| **Phân loại kiểm thử** | UI Testing |
| **Mức độ ưu tiên** | **Medium** |
| **Tiền điều kiện** | Người dùng có 1 thông báo chưa đọc và 1 thông báo đã đọc. |
| **Dữ liệu đầu vào (Test Data)** | N/A |
| **Các bước thực hiện** | 1. Mở danh sách thông báo.<br/>2. So sánh màu nền của các thông báo. |
| **Kết quả mong đợi** | Thông báo chưa đọc có nền màu xanh nhạt (hoặc có chấm xanh biểu thị chưa đọc). Thông báo đã đọc có nền màu trắng thông thường. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC10_03: Nhấn vào thông báo chuyển hướng đến màn hình chi tiết tương ứng và đổi trạng thái Đã đọc

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC10_03` |
| **Tên Test Case** | Nhấn vào thông báo chuyển hướng đến màn hình chi tiết tương ứng và đổi trạng thái Đã đọc |
| **Use Case liên quan** | UC10 - Xem thông báo (Notifications) |
| **Phân loại kiểm thử** | Functional (Integration) |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Có thông báo chưa đọc: 'Booking #001 đã thanh toán thành công'. |
| **Dữ liệu đầu vào (Test Data)** | N/A |
| **Các bước thực hiện** | 1. Mở dropdown thông báo.<br/>2. Nhấn vào thông báo thanh toán booking #001. |
| **Kết quả mong đợi** | 1. Hệ thống tự động cập nhật trạng thái thông báo thành 'Đã đọc' (nền đổi sang màu trắng, số lượng badge thông báo trên chuông giảm 1).<br/>2. Điều hướng chính xác tới trang Chi tiết booking #001. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC10_04: Hiển thị trạng thái khi người dùng không có thông báo nào

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC10_04` |
| **Tên Test Case** | Hiển thị trạng thái khi người dùng không có thông báo nào |
| **Use Case liên quan** | UC10 - Xem thông báo (Notifications) |
| **Phân loại kiểm thử** | Boundary / UI |
| **Mức độ ưu tiên** | **Low** |
| **Tiền điều kiện** | Tài khoản chưa nhận thông báo nào. |
| **Dữ liệu đầu vào (Test Data)** | Tài khoản mới |
| **Các bước thực hiện** | 1. Click vào biểu tượng chuông thông báo. |
| **Kết quả mong đợi** | Dropdown hiển thị thông báo: 'Bạn không có thông báo nào'. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC10_05: Đánh dấu tất cả thông báo là đã đọc (Mark all as read)

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC10_05` |
| **Tên Test Case** | Đánh dấu tất cả thông báo là đã đọc (Mark all as read) |
| **Use Case liên quan** | UC10 - Xem thông báo (Notifications) |
| **Phân loại kiểm thử** | Functional |
| **Mức độ ưu tiên** | **Medium** |
| **Tiền điều kiện** | Có nhiều thông báo chưa đọc trong danh sách. |
| **Dữ liệu đầu vào (Test Data)** | N/A |
| **Các bước thực hiện** | 1. Mở danh sách thông báo.<br/>2. Nhấn nút 'Đánh dấu tất cả là đã đọc'. |
| **Kết quả mong đợi** | Toàn bộ thông báo chuyển sang trạng thái đã đọc, số badge trên biểu tượng chuông biến mất. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC10_06: Cập nhật số đếm thông báo chưa đọc (Badge count)

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC10_06` |
| **Tên Test Case** | Cập nhật số đếm thông báo chưa đọc (Badge count) |
| **Use Case liên quan** | UC10 - Xem thông báo (Notifications) |
| **Phân loại kiểm thử** | UI / Real-time |
| **Mức độ ưu tiên** | **Medium** |
| **Tiền điều kiện** | Người dùng đang thao tác trên hệ thống. |
| **Dữ liệu đầu vào (Test Data)** | Hệ thống phát sinh 1 thông báo mới |
| **Các bước thực hiện** | 1. Kích hoạt phát sinh thông báo mới (ví dụ hoàn thành thanh toán).<br/>2. Quan sát icon chuông thông báo trên header. |
| **Kết quả mong đợi** | Số badge màu đỏ trên icon chuông tăng thêm 1 tương ứng với số thông báo chưa đọc. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

