# UC15: Quản Lý Yêu Cầu Rút Tiền

## 1. Thông Tin Use Case

- **Mã Use Case:** UC15
- **Tên Use Case:** Quản lý yêu cầu rút tiền - Tạo yêu cầu rút tiền (Withdrawal Request)
- **Tác nhân chính (Actor):** Organizer (Nhà tổ chức)
- **Mô tả nghiệp vụ:** Cho phép Nhà tổ chức theo dõi số dư doanh thu từ các sự kiện và tạo lệnh rút tiền về tài khoản ngân hàng.
- **Tiền điều kiện (Pre-conditions):** Nhà tổ chức đã đăng nhập và Số dư khả dụng trong ví lớn hơn 0.
- **Hậu điều kiện (Post-conditions):** Yêu cầu rút tiền được tạo với trạng thái 'Chờ duyệt' (PENDING). Số tiền rút được trừ khỏi Số dư khả dụng và cộng tạm thời vào Số dư đang chờ rút. Gửi thông báo đến Admin.

## 2. Danh Sách Test Cases Tổng Hợp

| STT | Mã Test Case | Tên / Mục Tiêu Test Case | Phân Loại | Mức Độ Ưu Tiên |
|:---:|:---|:---|:---|:---:|
| 1 | **TC_UC15_01** | Xem thông tin ví doanh thu và số dư chính xác | Functional (Happy Path) | **High** |
| 2 | **TC_UC15_02** | Tạo yêu cầu rút tiền thành công với số tiền hợp lệ | Functional (Happy Path) | **High** |
| 3 | **TC_UC15_03** | Tạo yêu cầu rút tiền thất bại khi số tiền nhập lớn hơn Số dư khả dụng | Validation / Business Rule | **High** |
| 4 | **TC_UC15_04** | Tạo yêu cầu rút tiền thất bại khi số tiền nhập nhỏ hơn hoặc bằng 0 | Validation (Negative) | **High** |
| 5 | **TC_UC15_05** | Tạo yêu cầu thất bại khi để trống Tên ngân hàng hoặc Số tài khoản | Validation (Negative) | **Medium** |
| 6 | **TC_UC15_06** | Hủy bỏ yêu cầu rút tiền ở bước xác nhận (Preview) | Functional (Alternative Flow) | **Medium** |
| 7 | **TC_UC15_07** | Kiểm tra nút 'Rút tiền' khi Nhà tổ chức chưa có doanh thu (Số dư = 0) | UI / Boundary | **Low** |
| 8 | **TC_UC15_08** | Xem lịch sử các yêu cầu rút tiền của Nhà tổ chức | Functional | **Medium** |

---

## 3. Thiết Kế Chi Tiết Từng Test Case

### TC_UC15_01: Xem thông tin ví doanh thu và số dư chính xác

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC15_01` |
| **Tên Test Case** | Xem thông tin ví doanh thu và số dư chính xác |
| **Use Case liên quan** | UC15 - Quản lý yêu cầu rút tiền - Tạo yêu cầu rút tiền (Withdrawal Request) |
| **Phân loại kiểm thử** | Functional (Happy Path) |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Nhà tổ chức có doanh thu từ các sự kiện đã bán vé. |
| **Dữ liệu đầu vào (Test Data)** | N/A |
| **Các bước thực hiện** | 1. Vào mục 'Doanh thu & Số dư' trên menu Organizer.<br/>2. Quan sát các thẻ số dư hiển thị. |
| **Kết quả mong đợi** | Hiển thị chính xác 4 chỉ số: Số dư đang chờ xác nhận (15.000.000đ), Số dư khả dụng (42.300.000đ), Số dư đang chờ rút (5.000.000đ), Tổng số tiền đã rút (80.000.000đ). Phía dưới hiển thị bảng doanh thu từng sự kiện và nút 'Rút tiền'. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC15_02: Tạo yêu cầu rút tiền thành công với số tiền hợp lệ

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC15_02` |
| **Tên Test Case** | Tạo yêu cầu rút tiền thành công với số tiền hợp lệ |
| **Use Case liên quan** | UC15 - Quản lý yêu cầu rút tiền - Tạo yêu cầu rút tiền (Withdrawal Request) |
| **Phân loại kiểm thử** | Functional (Happy Path) |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Số dư khả dụng hiện có: 42.300.000đ. |
| **Dữ liệu đầu vào (Test Data)** | Số tiền muốn rút: 30.000.000đ<br/>Tên ngân hàng: 'MBBank'<br/>Số tài khoản: '334433' |
| **Các bước thực hiện** | 1. Nhấn nút 'Rút tiền'.<br/>2. Nhập Số tiền = 30.000.000đ, Tên ngân hàng = 'MBBank', Số tài khoản = '334433'.<br/>3. Nhấn 'Gửi yêu cầu'.<br/>4. Màn hình xác nhận hiển thị thông tin tóm tắt và số dư còn lại sau khi trừ (12.300.000đ). Nhấn 'Xác nhận'. |
| **Kết quả mong đợi** | Hệ thống tạo yêu cầu rút tiền thành công ở trạng thái 'Chờ duyệt'. Cập nhật lại ví: Số dư khả dụng giảm còn 12.300.000đ, Số dư đang chờ rút tăng lên 35.000.000đ. Gửi thông báo yêu cầu rút tiền đến Admin. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC15_03: Tạo yêu cầu rút tiền thất bại khi số tiền nhập lớn hơn Số dư khả dụng

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC15_03` |
| **Tên Test Case** | Tạo yêu cầu rút tiền thất bại khi số tiền nhập lớn hơn Số dư khả dụng |
| **Use Case liên quan** | UC15 - Quản lý yêu cầu rút tiền - Tạo yêu cầu rút tiền (Withdrawal Request) |
| **Phân loại kiểm thử** | Validation / Business Rule |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Số dư khả dụng hiện có là 42.300.000đ. |
| **Dữ liệu đầu vào (Test Data)** | Số tiền muốn rút: 50.000.000đ (> 42.300.000đ) |
| **Các bước thực hiện** | 1. Nhấn 'Rút tiền'.<br/>2. Nhập số tiền = 50.000.000đ.<br/>3. Nhấn 'Gửi yêu cầu'. |
| **Kết quả mong đợi** | Hệ thống báo lỗi: 'Số tiền muốn rút vượt quá số dư khả dụng hiện tại (Tối đa: 42.300.000đ)'. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC15_04: Tạo yêu cầu rút tiền thất bại khi số tiền nhập nhỏ hơn hoặc bằng 0

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC15_04` |
| **Tên Test Case** | Tạo yêu cầu rút tiền thất bại khi số tiền nhập nhỏ hơn hoặc bằng 0 |
| **Use Case liên quan** | UC15 - Quản lý yêu cầu rút tiền - Tạo yêu cầu rút tiền (Withdrawal Request) |
| **Phân loại kiểm thử** | Validation (Negative) |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Đang ở form nhập thông tin rút tiền. |
| **Dữ liệu đầu vào (Test Data)** | Số tiền: 0 hoặc -500.000đ |
| **Các bước thực hiện** | 1. Nhập Số tiền = 0 hoặc số âm.<br/>2. Nhấn 'Gửi yêu cầu'. |
| **Kết quả mong đợi** | Hệ thống báo lỗi: 'Số tiền rút phải lớn hơn 0 và tối thiểu theo quy định (ví dụ 100.000đ)'. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC15_05: Tạo yêu cầu thất bại khi để trống Tên ngân hàng hoặc Số tài khoản

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC15_05` |
| **Tên Test Case** | Tạo yêu cầu thất bại khi để trống Tên ngân hàng hoặc Số tài khoản |
| **Use Case liên quan** | UC15 - Quản lý yêu cầu rút tiền - Tạo yêu cầu rút tiền (Withdrawal Request) |
| **Phân loại kiểm thử** | Validation (Negative) |
| **Mức độ ưu tiên** | **Medium** |
| **Tiền điều kiện** | Đang ở form rút tiền. |
| **Dữ liệu đầu vào (Test Data)** | Số tiền: 1.000.000đ, Tên ngân hàng: '', Số tài khoản: '' |
| **Các bước thực hiện** | 1. Nhập số tiền hợp lệ.<br/>2. Bỏ trống thông tin ngân hàng.<br/>3. Nhấn 'Gửi yêu cầu'. |
| **Kết quả mong đợi** | Hệ thống cảnh báo: 'Vui lòng nhập tên ngân hàng và số tài khoản nhận tiền'. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC15_06: Hủy bỏ yêu cầu rút tiền ở bước xác nhận (Preview)

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC15_06` |
| **Tên Test Case** | Hủy bỏ yêu cầu rút tiền ở bước xác nhận (Preview) |
| **Use Case liên quan** | UC15 - Quản lý yêu cầu rút tiền - Tạo yêu cầu rút tiền (Withdrawal Request) |
| **Phân loại kiểm thử** | Functional (Alternative Flow) |
| **Mức độ ưu tiên** | **Medium** |
| **Tiền điều kiện** | Đang ở màn hình xem trước thông tin xác nhận rút tiền. |
| **Dữ liệu đầu vào (Test Data)** | N/A |
| **Các bước thực hiện** | 1. Điền thông tin rút tiền và bấm 'Gửi yêu cầu'.<br/>2. Tại popup xác nhận, nhấn nút 'Hủy'. |
| **Kết quả mong đợi** | Hệ thống đóng popup xác nhận, quay lại form nhập liệu. Không có yêu cầu rút tiền nào được tạo và số dư tài khoản giữ nguyên. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC15_07: Kiểm tra nút 'Rút tiền' khi Nhà tổ chức chưa có doanh thu (Số dư = 0)

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC15_07` |
| **Tên Test Case** | Kiểm tra nút 'Rút tiền' khi Nhà tổ chức chưa có doanh thu (Số dư = 0) |
| **Use Case liên quan** | UC15 - Quản lý yêu cầu rút tiền - Tạo yêu cầu rút tiền (Withdrawal Request) |
| **Phân loại kiểm thử** | UI / Boundary |
| **Mức độ ưu tiên** | **Low** |
| **Tiền điều kiện** | Tài khoản Organizer mới tạo, Số dư khả dụng = 0đ. |
| **Dữ liệu đầu vào (Test Data)** | Số dư = 0đ |
| **Các bước thực hiện** | 1. Mở trang Doanh thu & Số dư. |
| **Kết quả mong đợi** | Các thẻ số dư đều hiển thị 0đ, danh sách sự kiện trống và nút 'Rút tiền' bị disabled/vô hiệu hóa. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC15_08: Xem lịch sử các yêu cầu rút tiền của Nhà tổ chức

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC15_08` |
| **Tên Test Case** | Xem lịch sử các yêu cầu rút tiền của Nhà tổ chức |
| **Use Case liên quan** | UC15 - Quản lý yêu cầu rút tiền - Tạo yêu cầu rút tiền (Withdrawal Request) |
| **Phân loại kiểm thử** | Functional |
| **Mức độ ưu tiên** | **Medium** |
| **Tiền điều kiện** | Nhà tổ chức đã từng tạo các yêu cầu rút tiền. |
| **Dữ liệu đầu vào (Test Data)** | N/A |
| **Các bước thực hiện** | 1. Chuyển sang tab/mục 'Lịch sử rút tiền'. |
| **Kết quả mong đợi** | Hiển thị danh sách các lệnh rút: Mã yêu cầu, Ngày tạo, Số tiền, Ngân hàng, Số tài khoản, Trạng thái ('Chờ duyệt', 'Đã duyệt', 'Bị từ chối'). |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

