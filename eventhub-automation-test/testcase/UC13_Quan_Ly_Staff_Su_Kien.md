# UC13: Quản Lý Staff Của Sự Kiện

## 1. Thông Tin Use Case

- **Mã Use Case:** UC13
- **Tên Use Case:** Quản lý Staff của sự kiện (Thêm, Xóa, Cập nhật trạng thái Staff)
- **Tác nhân chính (Actor):** Organizer (Nhà tổ chức)
- **Mô tả nghiệp vụ:** Cho phép Nhà tổ chức phân quyền nhân viên phụ trách soát vé và check-in cho sự kiện cụ thể bằng email.
- **Tiền điều kiện (Pre-conditions):** Nhà tổ chức đã đăng nhập và sự kiện đã được Admin phê duyệt (Đã duyệt).
- **Hậu điều kiện (Post-conditions):** Danh sách nhân viên soát vé (Staff) được phân công cho sự kiện được cập nhật (Thêm mới, Khóa, Mở khóa, Xóa bỏ).

## 2. Danh Sách Test Cases Tổng Hợp

| STT | Mã Test Case | Tên / Mục Tiêu Test Case | Phân Loại | Mức Độ Ưu Tiên |
|:---:|:---|:---|:---|:---:|
| 1 | **TC_UC13_01** | Xem danh sách Staff đã phân công cho sự kiện | Functional (Happy Path) | **High** |
| 2 | **TC_UC13_02** | Thêm Staff mới vào sự kiện thành công bằng Email hợp lệ | Functional (Happy Path) | **High** |
| 3 | **TC_UC13_03** | Thêm Staff thất bại khi Email không tồn tại trong hệ thống | Business Rule (Negative) | **High** |
| 4 | **TC_UC13_04** | Thêm Staff thất bại khi Email không thuộc tài khoản Customer (Exception 5.2) | Business Rule (Negative) | **High** |
| 5 | **TC_UC13_05** | Mở khóa nhân viên đang ở trạng thái Tạm khóa | Functional | **Medium** |
| 6 | **TC_UC13_06** | Khóa quyền hoạt động của một nhân viên đang hoạt động | Functional | **Medium** |
| 7 | **TC_UC13_07** | Xóa nhân viên khỏi sự kiện thành công | Functional | **High** |
| 8 | **TC_UC13_08** | Cảnh báo khi xóa nhân viên đang trong ca check-in của sự kiện | Business Rule / Warning | **Medium** |

---

## 3. Thiết Kế Chi Tiết Từng Test Case

### TC_UC13_01: Xem danh sách Staff đã phân công cho sự kiện

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC13_01` |
| **Tên Test Case** | Xem danh sách Staff đã phân công cho sự kiện |
| **Use Case liên quan** | UC13 - Quản lý Staff của sự kiện (Thêm, Xóa, Cập nhật trạng thái Staff) |
| **Phân loại kiểm thử** | Functional (Happy Path) |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Sự kiện đã được duyệt và đã có 2 nhân viên được phân công. |
| **Dữ liệu đầu vào (Test Data)** | N/A |
| **Các bước thực hiện** | 1. Mở màn hình chi tiết sự kiện đã duyệt.<br/>2. Nhấn nút 'Quản lý Staff'.<br/>3. Quan sát danh sách nhân viên. |
| **Kết quả mong đợi** | Hiển thị danh sách Staff gồm các cột: Tên, Email, Trạng thái ('Đang hoạt động' / 'Tạm khóa') và các nút thao tác tương ứng (Khóa / Mở, Xóa) cùng nút 'Thêm Staff'. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC13_02: Thêm Staff mới vào sự kiện thành công bằng Email hợp lệ

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC13_02` |
| **Tên Test Case** | Thêm Staff mới vào sự kiện thành công bằng Email hợp lệ |
| **Use Case liên quan** | UC13 - Quản lý Staff của sự kiện (Thêm, Xóa, Cập nhật trạng thái Staff) |
| **Phân loại kiểm thử** | Functional (Happy Path) |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Tài khoản 'seed.customer2@eventhub.local' tồn tại trong hệ thống với vai trò CUSTOMER. |
| **Dữ liệu đầu vào (Test Data)** | Email Staff: 'seed.customer2@eventhub.local' |
| **Các bước thực hiện** | 1. Tại màn hình Quản lý Staff, nhấn nút 'Thêm Staff'.<br/>2. Nhập Email = 'seed.customer2@eventhub.local'.<br/>3. Nhấn nút 'Thêm'. |
| **Kết quả mong đợi** | Hệ thống thông báo: 'Thêm staff thành công'. Nhân viên với email trên xuất hiện trong danh sách ở trạng thái 'Đang hoạt động'. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC13_03: Thêm Staff thất bại khi Email không tồn tại trong hệ thống

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC13_03` |
| **Tên Test Case** | Thêm Staff thất bại khi Email không tồn tại trong hệ thống |
| **Use Case liên quan** | UC13 - Quản lý Staff của sự kiện (Thêm, Xóa, Cập nhật trạng thái Staff) |
| **Phân loại kiểm thử** | Business Rule (Negative) |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Email 'notfound_user_99999@eventhub.test' chưa đăng ký tài khoản. |
| **Dữ liệu đầu vào (Test Data)** | Email: 'notfound_user_99999@eventhub.test' |
| **Các bước thực hiện** | 1. Nhấn 'Thêm Staff'.<br/>2. Nhập Email = 'notfound_user_99999@eventhub.test'.<br/>3. Nhấn 'Thêm'. |
| **Kết quả mong đợi** | Hệ thống từ chối và báo lỗi: 'Email nhập vào không tồn tại trong hệ thống. Vui lòng kiểm tra lại'. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC13_04: Thêm Staff thất bại khi Email không thuộc tài khoản Customer (Exception 5.2)

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC13_04` |
| **Tên Test Case** | Thêm Staff thất bại khi Email không thuộc tài khoản Customer |
| **Use Case liên quan** | UC13 - Quản lý Staff của sự kiện (Thêm, Xóa, Cập nhật trạng thái Staff) |
| **Phân loại kiểm thử** | Business Rule (Negative) |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Email 'seed.admin@eventhub.local' thuộc tài khoản vai trò ADMIN hoặc ORGANIZER. |
| **Dữ liệu đầu vào (Test Data)** | Email: 'seed.admin@eventhub.local' |
| **Các bước thực hiện** | 1. Nhập email của tài khoản không phải Customer (ví dụ 'seed.admin@eventhub.local').<br/>2. Nhấn 'Thêm'. |
| **Kết quả mong đợi** | Hệ thống từ chối và báo lỗi: 'Email nhập vào không thuộc tài khoản Customer'. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC13_05: Mở khóa nhân viên đang ở trạng thái Tạm khóa

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC13_05` |
| **Tên Test Case** | Mở khóa nhân viên đang ở trạng thái Tạm khóa |
| **Use Case liên quan** | UC13 - Quản lý Staff của sự kiện (Thêm, Xóa, Cập nhật trạng thái Staff) |
| **Phân loại kiểm thử** | Functional |
| **Mức độ ưu tiên** | **Medium** |
| **Tiền điều kiện** | Nhân viên 'Lê Thị C' đang có trạng thái 'Tạm khóa'. |
| **Dữ liệu đầu vào (Test Data)** | Nhân viên: 'Lê Thị C' |
| **Các bước thực hiện** | 1. Tại dòng nhân viên Lê Thị C, nhấn nút 'Mở'.<br/>2. Quan sát trạng thái. |
| **Kết quả mong đợi** | Hệ thống cập nhật ngay trạng thái của Lê Thị C thành 'Đang hoạt động', nút đổi thành 'Khóa'. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC13_06: Khóa quyền hoạt động của một nhân viên đang hoạt động

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC13_06` |
| **Tên Test Case** | Khóa quyền hoạt động của một nhân viên đang hoạt động |
| **Use Case liên quan** | UC13 - Quản lý Staff của sự kiện (Thêm, Xóa, Cập nhật trạng thái Staff) |
| **Phân loại kiểm thử** | Functional |
| **Mức độ ưu tiên** | **Medium** |
| **Tiền điều kiện** | Nhân viên 'Trần Văn B' đang có trạng thái 'Đang hoạt động'. |
| **Dữ liệu đầu vào (Test Data)** | Nhân viên: 'Trần Văn B' |
| **Các bước thực hiện** | 1. Nhấn nút 'Khóa' tại dòng nhân viên Trần Văn B. |
| **Kết quả mong đợi** | Hệ thống cập nhật trạng thái nhân viên thành 'Tạm khóa'. Nhân viên này không thể đăng nhập app soát vé để check-in cho sự kiện này nữa. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC13_07: Xóa nhân viên khỏi sự kiện thành công

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC13_07` |
| **Tên Test Case** | Xóa nhân viên khỏi sự kiện thành công |
| **Use Case liên quan** | UC13 - Quản lý Staff của sự kiện (Thêm, Xóa, Cập nhật trạng thái Staff) |
| **Phân loại kiểm thử** | Functional |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Nhân viên 'Trần Văn B' không trong ca check-in trực tiếp. |
| **Dữ liệu đầu vào (Test Data)** | Nhân viên: 'Trần Văn B' |
| **Các bước thực hiện** | 1. Nhấn nút 'Xóa' tại dòng nhân viên Trần Văn B.<br/>2. Hộp thoại xác nhận hiển thị, nhấn 'Đồng ý'. |
| **Kết quả mong đợi** | Nhân viên Trần Văn B bị gỡ hoàn toàn khỏi danh sách phân công Staff của sự kiện. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC13_08: Cảnh báo khi xóa nhân viên đang trong ca check-in của sự kiện

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC13_08` |
| **Tên Test Case** | Cảnh báo khi xóa nhân viên đang trong ca check-in của sự kiện |
| **Use Case liên quan** | UC13 - Quản lý Staff của sự kiện (Thêm, Xóa, Cập nhật trạng thái Staff) |
| **Phân loại kiểm thử** | Business Rule / Warning |
| **Mức độ ưu tiên** | **Medium** |
| **Tiền điều kiện** | Sự kiện đang diễn ra và nhân viên đang thực hiện check-in cho khách. |
| **Dữ liệu đầu vào (Test Data)** | Nhân viên đang trong ca làm việc |
| **Các bước thực hiện** | 1. Nhấn nút 'Xóa' nhân viên trong lúc sự kiện đang diễn ra.<br/>2. Quan sát thông báo từ hệ thống. |
| **Kết quả mong đợi** | Hệ thống hiển thị cảnh báo đặc biệt: 'Nhân viên này đang trong ca check-in của sự kiện. Việc xóa có thể gián đoạn hoạt động soát vé. Bạn có chắc chắn muốn xóa?'. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

