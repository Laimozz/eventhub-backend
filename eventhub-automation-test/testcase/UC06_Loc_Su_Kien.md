# UC06: Lọc Sự Kiện

## 1. Thông Tin Use Case

- **Mã Use Case:** UC06
- **Tên Use Case:** Lọc sự kiện (Filter Events)
- **Tác nhân chính (Actor):** Guest, Customer, Staff
- **Mô tả nghiệp vụ:** Cung cấp bộ lọc đa tiêu chí giúp người dùng nhanh chóng tìm được sự kiện phù hợp theo thời gian tổ chức, danh mục sự kiện và địa điểm.
- **Tiền điều kiện (Pre-conditions):** Người dùng truy cập vào trang danh sách sự kiện trên EventHub.
- **Hậu điều kiện (Post-conditions):** Danh sách sự kiện được lọc hiển thị chính xác theo các tiêu chí đã chọn (Thời gian, Thể loại, Thành phố tổ chức).

## 2. Danh Sách Test Cases Tổng Hợp

| STT | Mã Test Case | Tên / Mục Tiêu Test Case | Phân Loại | Mức Độ Ưu Tiên |
|:---:|:---|:---|:---|:---:|
| 1 | **TC_UC06_01** | Lọc sự kiện thành công theo Khoảng thời gian và Thành phố tổ chức | Functional (Happy Path) | **High** |
| 2 | **TC_UC06_02** | Lọc sự kiện theo Thể loại (Category) | Functional | **High** |
| 3 | **TC_UC06_03** | Lọc sự kiện theo phím tắt thời gian nhanh (Hôm nay, Tuần này, Tháng này) | Functional | **Medium** |
| 4 | **TC_UC06_04** | Lọc sự kiện thất bại khi nhập Từ ngày lớn hơn Đến ngày | Validation (Negative) | **High** |
| 5 | **TC_UC06_05** | Lọc với tổ hợp tiêu chí không có kết quả phù hợp | Boundary / Negative | **Medium** |
| 6 | **TC_UC06_06** | Kiểm tra tính năng Đặt lại bộ lọc (Reset Filter) | Functional | **Medium** |
| 7 | **TC_UC06_07** | Kết hợp đồng thời Tìm kiếm từ khóa và Lọc sự kiện | Integration | **High** |
| 8 | **TC_UC06_08** | Kiểm tra phân trang danh sách sự kiện sau khi lọc | UI / Functional | **Low** |

---

## 3. Thiết Kế Chi Tiết Từng Test Case

### TC_UC06_01: Lọc sự kiện thành công theo Khoảng thời gian và Thành phố tổ chức

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC06_01` |
| **Tên Test Case** | Lọc sự kiện thành công theo Khoảng thời gian và Thành phố tổ chức |
| **Use Case liên quan** | UC06 - Lọc sự kiện (Filter Events) |
| **Phân loại kiểm thử** | Functional (Happy Path) |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Hệ thống có các sự kiện diễn ra ngày 26/09/2026 tại Hà Nội. |
| **Dữ liệu đầu vào (Test Data)** | Từ ngày: '26/09/2026'<br/>Đến ngày: '26/09/2026'<br/>Thành phố: 'Hà Nội' |
| **Các bước thực hiện** | 1. Tại thanh tìm kiếm/lọc, nhấn nút 'Lọc'.<br/>2. Dropdown bộ lọc hiển thị.<br/>3. Nhập Từ ngày = '26/09/2026' và Đến ngày = '26/09/2026'.<br/>4. Chọn Thành phố tổ chức = 'Hà Nội'.<br/>5. Nhấn 'Áp dụng bộ lọc'. |
| **Kết quả mong đợi** | Hệ thống lọc và hiển thị chính xác các sự kiện: 'LOALOA SHOW: VIỆT NAM BỪNG SÁNG', 'ART WORKSHOP', 'The Traditional Water Puppet Show' (đều diễn ra 26/09/2026 tại Hà Nội). |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC06_02: Lọc sự kiện theo Thể loại (Category)

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC06_02` |
| **Tên Test Case** | Lọc sự kiện theo Thể loại (Category) |
| **Use Case liên quan** | UC06 - Lọc sự kiện (Filter Events) |
| **Phân loại kiểm thử** | Functional |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Hệ thống có các danh mục: Âm nhạc, Thể thao, Nghệ thuật, Hội thảo, v.v. |
| **Dữ liệu đầu vào (Test Data)** | Thể loại: 'Âm nhạc' |
| **Các bước thực hiện** | 1. Mở bộ lọc sự kiện.<br/>2. Chọn Thể loại = 'Âm nhạc'.<br/>3. Nhấn 'Áp dụng'. |
| **Kết quả mong đợi** | Chỉ các sự kiện thuộc danh mục 'Âm nhạc' được hiển thị trên danh sách. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC06_03: Lọc sự kiện theo phím tắt thời gian nhanh (Hôm nay, Tuần này, Tháng này)

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC06_03` |
| **Tên Test Case** | Lọc sự kiện theo phím tắt thời gian nhanh (Hôm nay, Tuần này, Tháng này) |
| **Use Case liên quan** | UC06 - Lọc sự kiện (Filter Events) |
| **Phân loại kiểm thử** | Functional |
| **Mức độ ưu tiên** | **Medium** |
| **Tiền điều kiện** | Có sự kiện trong tuần hiện tại. |
| **Dữ liệu đầu vào (Test Data)** | Lựa chọn: 'Tuần này' |
| **Các bước thực hiện** | 1. Mở bộ lọc.<br/>2. Chọn radio/button 'Tuần này'.<br/>3. Nhấn áp dụng. |
| **Kết quả mong đợi** | Hệ thống tự động tính toán khoảng thời gian từ đầu tuần đến cuối tuần hiện tại và hiển thị các sự kiện diễn ra trong tuần. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC06_04: Lọc sự kiện thất bại khi nhập Từ ngày lớn hơn Đến ngày

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC06_04` |
| **Tên Test Case** | Lọc sự kiện thất bại khi nhập Từ ngày lớn hơn Đến ngày |
| **Use Case liên quan** | UC06 - Lọc sự kiện (Filter Events) |
| **Phân loại kiểm thử** | Validation (Negative) |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Đang mở bộ lọc thời gian tùy chỉnh. |
| **Dữ liệu đầu vào (Test Data)** | Từ ngày: '30/09/2026'<br/>Đến ngày: '20/09/2026' (Từ ngày > Đến ngày) |
| **Các bước thực hiện** | 1. Mở bộ lọc.<br/>2. Nhập Từ ngày = '30/09/2026'.<br/>3. Nhập Đến ngày = '20/09/2026'.<br/>4. Nhấn áp dụng. |
| **Kết quả mong đợi** | Hệ thống không thực hiện lọc và cảnh báo lỗi: 'Ngày bắt đầu không được lớn hơn ngày kết thúc'. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC06_05: Lọc với tổ hợp tiêu chí không có kết quả phù hợp

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC06_05` |
| **Tên Test Case** | Lọc với tổ hợp tiêu chí không có kết quả phù hợp |
| **Use Case liên quan** | UC06 - Lọc sự kiện (Filter Events) |
| **Phân loại kiểm thử** | Boundary / Negative |
| **Mức độ ưu tiên** | **Medium** |
| **Tiền điều kiện** | Không có sự kiện Thể thao nào tại Huế trong tháng 9/2026. |
| **Dữ liệu đầu vào (Test Data)** | Thể loại: 'Thể thao'<br/>Thành phố: 'Huế'<br/>Thời gian: 'Tháng 9/2026' |
| **Các bước thực hiện** | 1. Chọn các tiêu chí trên bộ lọc.<br/>2. Nhấn áp dụng. |
| **Kết quả mong đợi** | Hệ thống hiển thị danh sách trống cùng thông báo: 'Không có sự kiện nào phù hợp với bộ lọc đã chọn'. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC06_06: Kiểm tra tính năng Đặt lại bộ lọc (Reset Filter)

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC06_06` |
| **Tên Test Case** | Kiểm tra tính năng Đặt lại bộ lọc (Reset Filter) |
| **Use Case liên quan** | UC06 - Lọc sự kiện (Filter Events) |
| **Phân loại kiểm thử** | Functional |
| **Mức độ ưu tiên** | **Medium** |
| **Tiền điều kiện** | Bộ lọc đang được áp dụng nhiều điều kiện. |
| **Dữ liệu đầu vào (Test Data)** | N/A |
| **Các bước thực hiện** | 1. Mở bộ lọc.<br/>2. Nhấn nút 'Đặt lại' (Xóa bộ lọc). |
| **Kết quả mong đợi** | Các trường lọc quay về giá trị mặc định ('Tất cả', không chọn thành phố, không giới hạn ngày). Danh sách sự kiện hiển thị lại toàn bộ như ban đầu. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC06_07: Kết hợp đồng thời Tìm kiếm từ khóa và Lọc sự kiện

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC06_07` |
| **Tên Test Case** | Kết hợp đồng thời Tìm kiếm từ khóa và Lọc sự kiện |
| **Use Case liên quan** | UC06 - Lọc sự kiện (Filter Events) |
| **Phân loại kiểm thử** | Integration |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Có nhiều sự kiện mang tên 'Anh trai say hi' tại Hà Nội và TP.HCM. |
| **Dữ liệu đầu vào (Test Data)** | Từ khóa: 'Anh trai say hi'<br/>Bộ lọc Thành phố: 'Hà Nội' |
| **Các bước thực hiện** | 1. Nhập từ khóa 'Anh trai say hi' vào ô tìm kiếm.<br/>2. Mở bộ lọc và chọn Thành phố = 'Hà Nội'.<br/>3. Nhấn Lọc/Tìm kiếm. |
| **Kết quả mong đợi** | Chỉ hiển thị các sự kiện có tên chứa 'Anh trai say hi' VÀ được tổ chức tại 'Hà Nội'. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC06_08: Kiểm tra phân trang danh sách sự kiện sau khi lọc

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC06_08` |
| **Tên Test Case** | Kiểm tra phân trang danh sách sự kiện sau khi lọc |
| **Use Case liên quan** | UC06 - Lọc sự kiện (Filter Events) |
| **Phân loại kiểm thử** | UI / Functional |
| **Mức độ ưu tiên** | **Low** |
| **Tiền điều kiện** | Kết quả lọc trả về 25 sự kiện, mỗi trang hiển thị tối đa 10 sự kiện. |
| **Dữ liệu đầu vào (Test Data)** | N/A |
| **Các bước thực hiện** | 1. Thực hiện lọc ra danh sách nhiều hơn 10 sự kiện.<br/>2. Nhấn chuyển sang trang 2 ở thanh phân trang dưới cùng. |
| **Kết quả mong đợi** | Trang 2 tải đúng các sự kiện tiếp theo (từ 11 đến 20) mà vẫn giữ nguyên điều kiện lọc đã chọn. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

