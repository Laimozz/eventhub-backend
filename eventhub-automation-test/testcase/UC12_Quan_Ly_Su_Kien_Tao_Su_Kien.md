# UC12: Quản Lý Sự Kiện (Tạo Sự Kiện)

## 1. Thông Tin Use Case

- **Mã Use Case:** UC12
- **Tên Use Case:** Quản lý sự kiện - Tạo sự kiện mới (Create Event)
- **Tác nhân chính (Actor):** Organizer (Nhà tổ chức)
- **Mô tả nghiệp vụ:** Cung cấp quy trình từng bước (Multi-step Wizard) cho phép Nhà tổ chức nhập thông tin cơ bản của sự kiện, danh mục, thông tin khách mời/diễn giả và cấu hình các loại vé mở bán.
- **Tiền điều kiện (Pre-conditions):** Nhà tổ chức đã đăng nhập vào hệ thống EventHub bằng tài khoản Organizer.
- **Hậu điều kiện (Post-conditions):** Sự kiện mới được tạo thành công trong cơ sở dữ liệu ở trạng thái 'Chờ duyệt' (PENDING_APPROVAL) và gửi yêu cầu phê duyệt đến Quản trị viên (Admin).

## 2. Danh Sách Test Cases Tổng Hợp

| STT | Mã Test Case | Tên / Mục Tiêu Test Case | Phân Loại | Mức Độ Ưu Tiên |
|:---:|:---|:---|:---|:---:|
| 1 | **TC_UC12_01** | Tạo sự kiện mới thành công qua 3 bước với đầy đủ thông tin hợp lệ | Functional (Happy Path) | **High** |
| 2 | **TC_UC12_02** | Tạo sự kiện thất bại khi Thời gian bắt đầu lớn hơn hoặc bằng Thời gian kết thúc | Validation / Logic (Negative) | **High** |
| 3 | **TC_UC12_03** | Tạo sự kiện thất bại khi chọn Thời gian sự kiện trong quá khứ | Validation (Negative) | **High** |
| 4 | **TC_UC12_04** | Chặn chuyển bước khi để trống các trường bắt buộc ở Bước 1 | Validation (Negative) | **Medium** |
| 5 | **TC_UC12_05** | Kiểm tra tải lên hình ảnh hợp lệ và không hợp lệ ở Bước 1 | Functional / Upload | **Medium** |
| 6 | **TC_UC12_06** | Thêm nhiều khách mời / diễn giả thành công ở Bước 2 | Functional | **Medium** |
| 7 | **TC_UC12_07** | Tạo sự kiện thất bại khi Thời gian kết thúc bán vé diễn ra sau khi sự kiện kết thúc | Business Rule (Negative) | **High** |
| 8 | **TC_UC12_08** | Tạo sự kiện thất bại khi Thời gian kết thúc bán vé nhỏ hơn Thời gian bắt đầu bán vé | Validation / Logic | **High** |
| 9 | **TC_UC12_09** | Tạo sự kiện thất bại khi chưa thêm bất kỳ loại vé nào ở Bước 3 | Business Rule (Negative) | **High** |
| 10 | **TC_UC12_10** | Cảnh báo khi Tổng số lượng vé vượt quá Sức chứa của sự kiện | Business Rule / Boundary | **Medium** |
| 11 | **TC_UC12_11** | Kiểm tra nút quay lại bước trước (Back) bảo toàn dữ liệu đã nhập | UI / State Preservation | **Low** |
| 12 | **TC_UC12_12** | Gửi thông báo đến Admin khi Organizer tạo sự kiện mới | Integration | **Medium** |

---

## 3. Thiết Kế Chi Tiết Từng Test Case

### TC_UC12_01: Tạo sự kiện mới thành công qua 3 bước với đầy đủ thông tin hợp lệ

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC12_01` |
| **Tên Test Case** | Tạo sự kiện mới thành công qua 3 bước với đầy đủ thông tin hợp lệ |
| **Use Case liên quan** | UC12 - Quản lý sự kiện - Tạo sự kiện mới (Create Event) |
| **Phân loại kiểm thử** | Functional (Happy Path) |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Organizer đã đăng nhập. |
| **Dữ liệu đầu vào (Test Data)** | Bước 1: Thành phố: 'Hà Nội', Địa chỉ: 'SVĐ Mỹ Đình', Sức chứa: 40000, Tên: 'ANH TRAI SAYHI - CONCEPT 5', Thời gian: 26/09/2026 08:00 đến 26/09/2026 12:00, Ảnh bìa, Thumbnail, Sơ đồ chỗ ngồi.<br/>Bước 2: Loại sự kiện: 'Âm nhạc', Khách mời: Tên 'Trấn Thành', Vai trò 'MC', Ảnh, Mô tả.<br/>Bước 3: Vé A1 (100 vé, 300.000đ, bán 01/08/2026 đến 25/09/2026), Vé B1 (200 vé, 400.000đ). |
| **Các bước thực hiện** | 1. Nhấn nút 'Tạo sự kiện'.<br/>2. Tại Bước 1, điền đầy đủ thông tin sự kiện và tải lên 3 hình ảnh (bìa, thumbnail, sơ đồ chỗ ngồi). Nhấn 'NEXT'.<br/>3. Tại Bước 2, chọn Loại sự kiện = 'Âm nhạc', nhập mô tả, thêm thông tin khách mời rồi nhấn 'Thêm +'. Nhấn 'NEXT'.<br/>4. Tại Bước 3, nhập thông tin loại vé A1, B1 gồm số lượng, giá tiền, thời gian mở bán. Nhấn 'Thêm +'.<br/>5. Nhấn 'SUBMIT'. |
| **Kết quả mong đợi** | Hệ thống lưu toàn bộ thông tin sự kiện, tạo bản ghi với trạng thái PENDING_APPROVAL. Hiển thị thông báo: 'Tạo sự kiện thành công! Sự kiện của bạn đang chờ Admin xét duyệt'. Điều hướng về danh sách sự kiện. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC12_02: Tạo sự kiện thất bại khi Thời gian bắt đầu lớn hơn hoặc bằng Thời gian kết thúc

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC12_02` |
| **Tên Test Case** | Tạo sự kiện thất bại khi Thời gian bắt đầu lớn hơn hoặc bằng Thời gian kết thúc |
| **Use Case liên quan** | UC12 - Quản lý sự kiện - Tạo sự kiện mới (Create Event) |
| **Phân loại kiểm thử** | Validation / Logic (Negative) |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Đang ở Bước 1 của form tạo sự kiện. |
| **Dữ liệu đầu vào (Test Data)** | Thời gian bắt đầu: '26/09/2026 12:00'<br/>Thời gian kết thúc: '26/09/2026 08:00' (Bắt đầu sau kết thúc) |
| **Các bước thực hiện** | 1. Nhập thông tin Bước 1.<br/>2. Chọn Thời gian bắt đầu > Thời gian kết thúc.<br/>3. Nhấn 'NEXT'. |
| **Kết quả mong đợi** | Hệ thống ngăn không cho chuyển bước và báo lỗi: 'Thời gian kết thúc sự kiện phải sau thời gian bắt đầu'. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC12_03: Tạo sự kiện thất bại khi chọn Thời gian sự kiện trong quá khứ

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC12_03` |
| **Tên Test Case** | Tạo sự kiện thất bại khi chọn Thời gian sự kiện trong quá khứ |
| **Use Case liên quan** | UC12 - Quản lý sự kiện - Tạo sự kiện mới (Create Event) |
| **Phân loại kiểm thử** | Validation (Negative) |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Đang ở Bước 1. |
| **Dữ liệu đầu vào (Test Data)** | Thời gian bắt đầu: Một ngày trong quá khứ so với thời điểm hiện tại |
| **Các bước thực hiện** | 1. Chọn thời gian bắt đầu sự kiện trong quá khứ.<br/>2. Nhấn 'NEXT'. |
| **Kết quả mong đợi** | Hệ thống báo lỗi: 'Thời gian bắt đầu sự kiện không thể ở trong quá khứ'. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC12_04: Chặn chuyển bước khi để trống các trường bắt buộc ở Bước 1

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC12_04` |
| **Tên Test Case** | Chặn chuyển bước khi để trống các trường bắt buộc ở Bước 1 |
| **Use Case liên quan** | UC12 - Quản lý sự kiện - Tạo sự kiện mới (Create Event) |
| **Phân loại kiểm thử** | Validation (Negative) |
| **Mức độ ưu tiên** | **Medium** |
| **Tiền điều kiện** | Đang ở Bước 1. |
| **Dữ liệu đầu vào (Test Data)** | Để trống Tên sự kiện hoặc Địa chỉ hoặc Thành phố |
| **Các bước thực hiện** | 1. Không nhập Tên sự kiện.<br/>2. Nhấn nút 'NEXT'. |
| **Kết quả mong đợi** | Hệ thống đánh dấu đỏ trường thiếu và hiển thị thông báo yêu cầu nhập đầy đủ. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC12_05: Kiểm tra tải lên hình ảnh hợp lệ và không hợp lệ ở Bước 1

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC12_05` |
| **Tên Test Case** | Kiểm tra tải lên hình ảnh hợp lệ và không hợp lệ ở Bước 1 |
| **Use Case liên quan** | UC12 - Quản lý sự kiện - Tạo sự kiện mới (Create Event) |
| **Phân loại kiểm thử** | Functional / Upload |
| **Mức độ ưu tiên** | **Medium** |
| **Tiền điều kiện** | Đang ở Bước 1. |
| **Dữ liệu đầu vào (Test Data)** | File hợp lệ: event_cover.jpg (2MB)<br/>File không hợp lệ: script.bat, file_qua_lon.png (25MB) |
| **Các bước thực hiện** | 1. Kéo thả file script.bat vào vùng upload ảnh bìa.<br/>2. Quan sát phản hồi hệ thống. |
| **Kết quả mong đợi** | Hệ thống từ chối file không đúng định dạng hình ảnh và báo lỗi yêu cầu file JPG/PNG/WEBP dung lượng dưới 10MB. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC12_06: Thêm nhiều khách mời / diễn giả thành công ở Bước 2

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC12_06` |
| **Tên Test Case** | Thêm nhiều khách mời / diễn giả thành công ở Bước 2 |
| **Use Case liên quan** | UC12 - Quản lý sự kiện - Tạo sự kiện mới (Create Event) |
| **Phân loại kiểm thử** | Functional |
| **Mức độ ưu tiên** | **Medium** |
| **Tiền điều kiện** | Đã qua Bước 2. |
| **Dữ liệu đầu vào (Test Data)** | Khách mời 1: 'Trấn Thành' (MC), Khách mời 2: 'Sơn Tùng M-TP' (Ca sĩ) |
| **Các bước thực hiện** | 1. Nhập thông tin khách mời 1, nhấn 'Thêm +'.<br/>2. Nhập thông tin khách mời 2, nhấn 'Thêm +'.<br/>3. Quan sát danh sách khách mời hiển thị bên dưới. |
| **Kết quả mong đợi** | Cả 2 khách mời hiển thị trong danh sách tạm với đầy đủ tên, vai trò và nút xóa tương ứng nếu muốn xóa bỏ. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC12_07: Tạo sự kiện thất bại khi Thời gian kết thúc bán vé diễn ra sau khi sự kiện kết thúc

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC12_07` |
| **Tên Test Case** | Tạo sự kiện thất bại khi Thời gian kết thúc bán vé diễn ra sau khi sự kiện kết thúc |
| **Use Case liên quan** | UC12 - Quản lý sự kiện - Tạo sự kiện mới (Create Event) |
| **Phân loại kiểm thử** | Business Rule (Negative) |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Sự kiện diễn ra từ 08:00 đến 12:00 ngày 26/09/2026. Đang ở Bước 3. |
| **Dữ liệu đầu vào (Test Data)** | Thời gian kết thúc bán vé: '27/09/2026 10:00' (Sau khi sự kiện kết thúc) |
| **Các bước thực hiện** | 1. Điền thông tin loại vé.<br/>2. Chọn thời gian kết thúc bán vé là ngày 27/09/2026.<br/>3. Nhấn 'SUBMIT'. |
| **Kết quả mong đợi** | Hệ thống báo lỗi nghiệp vụ: 'Thời gian kết thúc bán vé không được diễn ra sau khi sự kiện kết thúc'. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC12_08: Tạo sự kiện thất bại khi Thời gian kết thúc bán vé nhỏ hơn Thời gian bắt đầu bán vé

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC12_08` |
| **Tên Test Case** | Tạo sự kiện thất bại khi Thời gian kết thúc bán vé nhỏ hơn Thời gian bắt đầu bán vé |
| **Use Case liên quan** | UC12 - Quản lý sự kiện - Tạo sự kiện mới (Create Event) |
| **Phân loại kiểm thử** | Validation / Logic |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Đang cấu hình vé ở Bước 3. |
| **Dữ liệu đầu vào (Test Data)** | Bắt đầu bán vé: '15/09/2026', Kết thúc bán vé: '10/09/2026' |
| **Các bước thực hiện** | 1. Nhập thời gian kết thúc bán vé trước thời gian bắt đầu bán vé.<br/>2. Nhấn 'Thêm vé' hoặc 'SUBMIT'. |
| **Kết quả mong đợi** | Hệ thống cảnh báo lỗi: 'Thời gian kết thúc bán vé phải sau thời gian bắt đầu bán vé'. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC12_09: Tạo sự kiện thất bại khi chưa thêm bất kỳ loại vé nào ở Bước 3

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC12_09` |
| **Tên Test Case** | Tạo sự kiện thất bại khi chưa thêm bất kỳ loại vé nào ở Bước 3 |
| **Use Case liên quan** | UC12 - Quản lý sự kiện - Tạo sự kiện mới (Create Event) |
| **Phân loại kiểm thử** | Business Rule (Negative) |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Đang ở Bước 3, danh sách loại vé đang trống. |
| **Dữ liệu đầu vào (Test Data)** | Danh sách loại vé = 0 |
| **Các bước thực hiện** | 1. Không tạo loại vé nào.<br/>2. Nhấn nút 'SUBMIT'. |
| **Kết quả mong đợi** | Hệ thống từ chối lưu và cảnh báo: 'Sự kiện phải có ít nhất 1 loại vé'. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC12_10: Cảnh báo khi Tổng số lượng vé vượt quá Sức chứa của sự kiện

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC12_10` |
| **Tên Test Case** | Cảnh báo khi Tổng số lượng vé vượt quá Sức chứa của sự kiện |
| **Use Case liên quan** | UC12 - Quản lý sự kiện - Tạo sự kiện mới (Create Event) |
| **Phân loại kiểm thử** | Business Rule / Boundary |
| **Mức độ ưu tiên** | **Medium** |
| **Tiền điều kiện** | Sức chứa sự kiện ở Bước 1 là 500 người. |
| **Dữ liệu đầu vào (Test Data)** | Vé VIP: 300 vé, Vé Thường: 300 vé (Tổng = 600 vé > 500) |
| **Các bước thực hiện** | 1. Nhập sức chứa = 500 ở Bước 1.<br/>2. Tại Bước 3, tạo 2 loại vé có tổng số lượng là 600.<br/>3. Nhấn 'SUBMIT'. |
| **Kết quả mong đợi** | Hệ thống cảnh báo lỗi: 'Tổng số lượng vé phát hành (600) không được vượt quá sức chứa sự kiện (500)'. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC12_11: Kiểm tra nút quay lại bước trước (Back) bảo toàn dữ liệu đã nhập

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC12_11` |
| **Tên Test Case** | Kiểm tra nút quay lại bước trước (Back) bảo toàn dữ liệu đã nhập |
| **Use Case liên quan** | UC12 - Quản lý sự kiện - Tạo sự kiện mới (Create Event) |
| **Phân loại kiểm thử** | UI / State Preservation |
| **Mức độ ưu tiên** | **Low** |
| **Tiền điều kiện** | Đang ở Bước 2 sau khi đã hoàn thành Bước 1. |
| **Dữ liệu đầu vào (Test Data)** | N/A |
| **Các bước thực hiện** | 1. Nhấn nút 'Quay lại' (PREVIOUS / BACK) từ Bước 2 về Bước 1.<br/>2. Kiểm tra các ô nhập liệu ở Bước 1. |
| **Kết quả mong đợi** | Toàn bộ dữ liệu đã nhập ở Bước 1 (Tên, địa chỉ, ngày giờ, ảnh đã upload) vẫn được giữ nguyên không bị mất. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC12_12: Gửi thông báo đến Admin khi Organizer tạo sự kiện mới

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC12_12` |
| **Tên Test Case** | Gửi thông báo đến Admin khi Organizer tạo sự kiện mới |
| **Use Case liên quan** | UC12 - Quản lý sự kiện - Tạo sự kiện mới (Create Event) |
| **Phân loại kiểm thử** | Integration |
| **Mức độ ưu tiên** | **Medium** |
| **Tiền điều kiện** | Organizer gửi yêu cầu tạo sự kiện thành công. |
| **Dữ liệu đầu vào (Test Data)** | Sự kiện 'ANH TRAI SAYHI - CONCEPT 5' |
| **Các bước thực hiện** | 1. Hoàn tất Submit sự kiện.<br/>2. Đăng nhập tài khoản Admin và kiểm tra danh sách Sự kiện chờ duyệt. |
| **Kết quả mong đợi** | Tài khoản Admin nhận được thông báo về sự kiện mới cần duyệt và sự kiện xuất hiện ngay trên trang danh sách chờ duyệt. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

