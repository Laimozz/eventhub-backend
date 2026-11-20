# UC18: Quản Lý Danh Mục Sự Kiện

## 1. Thông Tin Use Case

- **Mã Use Case:** UC18
- **Tên Use Case:** Quản lý danh mục sự kiện (Category Management)
- **Tác nhân chính (Actor):** Admin (Quản trị viên)
- **Mô tả nghiệp vụ:** Cho phép Quản trị viên quản lý tập danh mục sự kiện của toàn hệ thống (Âm nhạc, Thể thao, Hội thảo, v.v.), giúp chuẩn hóa việc phân loại sự kiện khi Organizer tạo sự kiện.
- **Tiền điều kiện (Pre-conditions):** Quản trị viên đã đăng nhập vào hệ thống EventHub.
- **Hậu điều kiện (Post-conditions):** Danh sách danh mục sự kiện được cập nhật (Thêm mới danh mục, Chỉnh sửa thông tin danh mục hoặc Xóa danh mục).

## 2. Danh Sách Test Cases Tổng Hợp

| STT | Mã Test Case | Tên / Mục Tiêu Test Case | Phân Loại | Mức Độ Ưu Tiên |
|:---:|:---|:---|:---|:---:|
| 1 | **TC_UC18_01** | Xem danh sách các danh mục sự kiện hiện có | Functional (Happy Path) | **High** |
| 2 | **TC_UC18_02** | Thêm mới danh mục sự kiện thành công với dữ liệu hợp lệ | Functional (Happy Path) | **High** |
| 3 | **TC_UC18_03** | Thêm danh mục thất bại khi Tên danh mục đã tồn tại (Duplicate Name) | Validation / Business Rule | **High** |
| 4 | **TC_UC18_04** | Thêm danh mục thất bại khi để trống Tên danh mục | Validation (Negative) | **Medium** |
| 5 | **TC_UC18_05** | Chỉnh sửa thông tin danh mục sự kiện thành công | Functional | **High** |
| 6 | **TC_UC18_06** | Xóa danh mục sự kiện chưa được sự kiện nào sử dụng thành công | Functional | **High** |
| 7 | **TC_UC18_07** | Từ chối xóa danh mục khi đang có ít nhất một sự kiện sử dụng danh mục đó | Business Rule / Constraint | **High** |
| 8 | **TC_UC18_08** | Đồng bộ danh mục mới tạo sang giao diện Bộ lọc và Tạo sự kiện | Integration | **Medium** |

---

## 3. Thiết Kế Chi Tiết Từng Test Case

### TC_UC18_01: Xem danh sách các danh mục sự kiện hiện có

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC18_01` |
| **Tên Test Case** | Xem danh sách các danh mục sự kiện hiện có |
| **Use Case liên quan** | UC18 - Quản lý danh mục sự kiện (Category Management) |
| **Phân loại kiểm thử** | Functional (Happy Path) |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Hệ thống đã có các danh mục: Âm nhạc, Hội thảo. |
| **Dữ liệu đầu vào (Test Data)** | N/A |
| **Các bước thực hiện** | 1. Admin chọn menu 'Quản lý danh mục sự kiện'.<br/>2. Quan sát bảng danh mục hiển thị. |
| **Kết quả mong đợi** | Bảng hiển thị đầy đủ các cột: Tên danh mục, Mô tả, Cột thao tác (nút Sửa, nút Xóa) và nút 'Thêm danh mục'. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC18_02: Thêm mới danh mục sự kiện thành công với dữ liệu hợp lệ

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC18_02` |
| **Tên Test Case** | Thêm mới danh mục sự kiện thành công với dữ liệu hợp lệ |
| **Use Case liên quan** | UC18 - Quản lý danh mục sự kiện (Category Management) |
| **Phân loại kiểm thử** | Functional (Happy Path) |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Tên danh mục 'Tự do' chưa có trong hệ thống. |
| **Dữ liệu đầu vào (Test Data)** | Tên danh mục: 'Tự do'<br/>Mô tả: 'Đây là những sự kiện mang tính freestyle không phụ thuộc vào cái gì' |
| **Các bước thực hiện** | 1. Nhấn nút 'Thêm danh mục'.<br/>2. Nhập Tên danh mục = 'Tự do'.<br/>3. Nhập Mô tả = 'Đây là những sự kiện mang tính freestyle không phụ thuộc vào cái gì'.<br/>4. Nhấn nút 'Lưu'. |
| **Kết quả mong đợi** | Hệ thống thông báo: 'Thêm danh mục thành công'. Danh mục 'Tự do' xuất hiện trong bảng danh mục và có sẵn trong dropdown chọn danh mục khi Organizer tạo sự kiện. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC18_03: Thêm danh mục thất bại khi Tên danh mục đã tồn tại (Duplicate Name)

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC18_03` |
| **Tên Test Case** | Thêm danh mục thất bại khi Tên danh mục đã tồn tại (Duplicate Name) |
| **Use Case liên quan** | UC18 - Quản lý danh mục sự kiện (Category Management) |
| **Phân loại kiểm thử** | Validation / Business Rule |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Danh mục 'Âm nhạc' đã tồn tại trong CSDL. |
| **Dữ liệu đầu vào (Test Data)** | Tên danh mục: 'Âm nhạc' |
| **Các bước thực hiện** | 1. Nhấn 'Thêm danh mục'.<br/>2. Nhập Tên danh mục = 'Âm nhạc'.<br/>3. Nhấn 'Lưu'. |
| **Kết quả mong đợi** | Hệ thống từ chối lưu và cảnh báo: 'Tên danh mục đã tồn tại trong hệ thống. Vui lòng chọn tên khác'. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC18_04: Thêm danh mục thất bại khi để trống Tên danh mục

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC18_04` |
| **Tên Test Case** | Thêm danh mục thất bại khi để trống Tên danh mục |
| **Use Case liên quan** | UC18 - Quản lý danh mục sự kiện (Category Management) |
| **Phân loại kiểm thử** | Validation (Negative) |
| **Mức độ ưu tiên** | **Medium** |
| **Tiền điều kiện** | Đang mở form Thêm danh mục. |
| **Dữ liệu đầu vào (Test Data)** | Tên danh mục: '' (để trống) |
| **Các bước thực hiện** | 1. Bỏ trống ô Tên danh mục.<br/>2. Nhấn 'Lưu'. |
| **Kết quả mong đợi** | Hệ thống báo lỗi: 'Tên danh mục không được để trống'. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC18_05: Chỉnh sửa thông tin danh mục sự kiện thành công

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC18_05` |
| **Tên Test Case** | Chỉnh sửa thông tin danh mục sự kiện thành công |
| **Use Case liên quan** | UC18 - Quản lý danh mục sự kiện (Category Management) |
| **Phân loại kiểm thử** | Functional |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Có danh mục 'Tự do'. |
| **Dữ liệu đầu vào (Test Data)** | Mô tả mới: 'Cập nhật mô tả các sự kiện tự do nghệ thuật' |
| **Các bước thực hiện** | 1. Tại dòng danh mục 'Tự do', nhấn nút 'Sửa'.<br/>2. Thay đổi phần Mô tả thành nội dung mới.<br/>3. Nhấn 'Lưu'. |
| **Kết quả mong đợi** | Hệ thống cập nhật thông tin và hiển thị mô tả mới trên danh sách. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC18_06: Xóa danh mục sự kiện chưa được sự kiện nào sử dụng thành công

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC18_06` |
| **Tên Test Case** | Xóa danh mục sự kiện chưa được sự kiện nào sử dụng thành công |
| **Use Case liên quan** | UC18 - Quản lý danh mục sự kiện (Category Management) |
| **Phân loại kiểm thử** | Functional |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Danh mục 'Tự do' vừa tạo, chưa có sự kiện nào gán vào danh mục này. |
| **Dữ liệu đầu vào (Test Data)** | Danh mục: 'Tự do' |
| **Các bước thực hiện** | 1. Nhấn nút 'Xóa' tại dòng danh mục 'Tự do'.<br/>2. Hộp thoại xác nhận hiển thị, nhấn 'Xác nhận xóa'. |
| **Kết quả mong đợi** | Hệ thống xóa danh mục khỏi cơ sở dữ liệu và biến mất khỏi danh sách. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC18_07: Từ chối xóa danh mục khi đang có ít nhất một sự kiện sử dụng danh mục đó

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC18_07` |
| **Tên Test Case** | Từ chối xóa danh mục khi đang có ít nhất một sự kiện sử dụng danh mục đó |
| **Use Case liên quan** | UC18 - Quản lý danh mục sự kiện (Category Management) |
| **Phân loại kiểm thử** | Business Rule / Constraint |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Danh mục 'Âm nhạc' đang được sự kiện 'ANH TRAI SAY HI' và 'Đêm nhạc Acoustic Thu' sử dụng. |
| **Dữ liệu đầu vào (Test Data)** | Danh mục: 'Âm nhạc' |
| **Các bước thực hiện** | 1. Nhấn nút 'Xóa' tại danh mục 'Âm nhạc'.<br/>2. Nhấn xác nhận xóa. |
| **Kết quả mong đợi** | Hệ thống từ chối xóa (HTTP 409 Conflict), hiển thị thông báo lỗi: 'Không thể xóa danh mục này do đang có sự kiện thuộc danh mục. Vui lòng xóa hoặc chuyển danh mục của các sự kiện liên quan trước'. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC18_08: Đồng bộ danh mục mới tạo sang giao diện Bộ lọc và Tạo sự kiện

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC18_08` |
| **Tên Test Case** | Đồng bộ danh mục mới tạo sang giao diện Bộ lọc và Tạo sự kiện |
| **Use Case liên quan** | UC18 - Quản lý danh mục sự kiện (Category Management) |
| **Phân loại kiểm thử** | Integration |
| **Mức độ ưu tiên** | **Medium** |
| **Tiền điều kiện** | Admin vừa thêm danh mục 'Thể thao điện tử (Esports)'. |
| **Dữ liệu đầu vào (Test Data)** | Danh mục: 'Thể thao điện tử (Esports)' |
| **Các bước thực hiện** | 1. Đăng nhập tài khoản Organizer, mở form Tạo sự kiện -> Kiểm tra dropdown Loại sự kiện.<br/>2. Mở trang chủ -> Kiểm tra dropdown Bộ lọc thể loại. |
| **Kết quả mong đợi** | Danh mục 'Thể thao điện tử (Esports)' xuất hiện đầy đủ ở cả form Tạo sự kiện và Bộ lọc tìm kiếm. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

