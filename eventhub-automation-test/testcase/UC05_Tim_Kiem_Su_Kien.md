# UC05: Tìm Kiếm Sự Kiện

## 1. Thông Tin Use Case

- **Mã Use Case:** UC05
- **Tên Use Case:** Tìm kiếm sự kiện (Search Events)
- **Tác nhân chính (Actor):** Customer, Staff, Guest
- **Mô tả nghiệp vụ:** Cho phép người dùng nhập từ khóa tìm kiếm để tra cứu các sự kiện theo tên sự kiện, nghệ sĩ biểu diễn hoặc địa điểm.
- **Tiền điều kiện (Pre-conditions):** Người dùng truy cập vào trang web EventHub (trang chủ hoặc trang sự kiện).
- **Hậu điều kiện (Post-conditions):** Hệ thống hiển thị danh sách các sự kiện phù hợp với từ khóa tìm kiếm. Người dùng có thể click xem chi tiết từng sự kiện.

## 2. Danh Sách Test Cases Tổng Hợp

| STT | Mã Test Case | Tên / Mục Tiêu Test Case | Phân Loại | Mức Độ Ưu Tiên |
|:---:|:---|:---|:---|:---:|
| 1 | **TC_UC05_01** | Tìm kiếm sự kiện thành công với từ khóa chính xác | Functional (Happy Path) | **High** |
| 2 | **TC_UC05_02** | Tìm kiếm không phân biệt chữ hoa, chữ thường (Case-insensitive) | Functional | **Medium** |
| 3 | **TC_UC05_03** | Tìm kiếm hỗ trợ tiếng Việt có dấu và không dấu | Functional / Localization | **Medium** |
| 4 | **TC_UC05_04** | Tìm kiếm với từ khóa không tồn tại trả về danh sách trống | Functional (Negative) | **High** |
| 5 | **TC_UC05_05** | Tìm kiếm khi để ô tìm kiếm trống | Boundary | **Low** |
| 6 | **TC_UC05_06** | Tìm kiếm chứa ký tự đặc biệt an toàn (SQL Injection / XSS Prevention) | Security | **High** |
| 7 | **TC_UC05_07** | Nhấn vào kết quả sự kiện chuyển hướng đến trang Chi tiết sự kiện | Navigation / Integration | **High** |

---

## 3. Thiết Kế Chi Tiết Từng Test Case

### TC_UC05_01: Tìm kiếm sự kiện thành công với từ khóa chính xác

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC05_01` |
| **Tên Test Case** | Tìm kiếm sự kiện thành công với từ khóa chính xác |
| **Use Case liên quan** | UC05 - Tìm kiếm sự kiện (Search Events) |
| **Phân loại kiểm thử** | Functional (Happy Path) |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Hệ thống có các sự kiện mang tên 'ANH TRAI SAYHI - CONCEPT 4' và 'ANH TRAI SAYHI - CONCEPT 5'. |
| **Dữ liệu đầu vào (Test Data)** | Từ khóa: 'Anh trai say hi' |
| **Các bước thực hiện** | 1. Tại trang chủ, nhập từ khóa 'Anh trai say hi' vào ô tìm kiếm.<br/>2. Nhấn phím Enter hoặc click biểu tượng 'Tìm kiếm'. |
| **Kết quả mong đợi** | Hệ thống trả về danh sách các sự kiện chứa từ khóa, hiển thị: Ảnh sự kiện, Tên sự kiện ('ANH TRAI SAYHI - CONCEPT 4', 'ANH TRAI SAYHI - CONCEPT 5'), Thời gian, Giá vé khởi điểm, Thành phố (Hà Nội). |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC05_02: Tìm kiếm không phân biệt chữ hoa, chữ thường (Case-insensitive)

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC05_02` |
| **Tên Test Case** | Tìm kiếm không phân biệt chữ hoa, chữ thường (Case-insensitive) |
| **Use Case liên quan** | UC05 - Tìm kiếm sự kiện (Search Events) |
| **Phân loại kiểm thử** | Functional |
| **Mức độ ưu tiên** | **Medium** |
| **Tiền điều kiện** | Hệ thống có sự kiện 'ANH TRAI SAYHI'. |
| **Dữ liệu đầu vào (Test Data)** | Thử các từ khóa: 'anh trai say hi', 'ANH TRAI SAY HI', 'AnH tRaI sAy Hi' |
| **Các bước thực hiện** | 1. Lần lượt nhập các chuỗi hoa thường khác nhau vào ô tìm kiếm.<br/>2. Thực hiện tìm kiếm và so sánh kết quả. |
| **Kết quả mong đợi** | Tất cả các lần tìm kiếm đều trả về cùng một danh sách kết quả như nhau. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC05_03: Tìm kiếm hỗ trợ tiếng Việt có dấu và không dấu

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC05_03` |
| **Tên Test Case** | Tìm kiếm hỗ trợ tiếng Việt có dấu và không dấu |
| **Use Case liên quan** | UC05 - Tìm kiếm sự kiện (Search Events) |
| **Phân loại kiểm thử** | Functional / Localization |
| **Mức độ ưu tiên** | **Medium** |
| **Tiền điều kiện** | Có sự kiện 'Múa rối nước'. |
| **Dữ liệu đầu vào (Test Data)** | Từ khóa có dấu: 'Múa rối nước'<br/>Từ khóa không dấu: 'Mua roi nuoc' |
| **Các bước thực hiện** | 1. Nhập 'Mua roi nuoc' vào ô tìm kiếm.<br/>2. Nhấn tìm kiếm. |
| **Kết quả mong đợi** | Hệ thống vẫn nhận diện và hiển thị sự kiện 'The “Traditional Water Puppet Show” - Múa rối nước'. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC05_04: Tìm kiếm với từ khóa không tồn tại trả về danh sách trống

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC05_04` |
| **Tên Test Case** | Tìm kiếm với từ khóa không tồn tại trả về danh sách trống |
| **Use Case liên quan** | UC05 - Tìm kiếm sự kiện (Search Events) |
| **Phân loại kiểm thử** | Functional (Negative) |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Hệ thống không có sự kiện nào tên 'Sự kiện xyz 9999'. |
| **Dữ liệu đầu vào (Test Data)** | Từ khóa: 'Sự kiện xyz 9999' |
| **Các bước thực hiện** | 1. Nhập 'Sự kiện xyz 9999' vào thanh tìm kiếm.<br/>2. Nhấn tìm kiếm. |
| **Kết quả mong đợi** | Hệ thống hiển thị trạng thái trống (Empty state) với thông báo: 'Không tìm thấy sự kiện nào phù hợp với từ khóa của bạn' kèm gợi ý thử từ khóa khác. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC05_05: Tìm kiếm khi để ô tìm kiếm trống

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC05_05` |
| **Tên Test Case** | Tìm kiếm khi để ô tìm kiếm trống |
| **Use Case liên quan** | UC05 - Tìm kiếm sự kiện (Search Events) |
| **Phân loại kiểm thử** | Boundary |
| **Mức độ ưu tiên** | **Low** |
| **Tiền điều kiện** | Hệ thống có nhiều sự kiện đang hoạt động. |
| **Dữ liệu đầu vào (Test Data)** | Từ khóa: '' (chuỗi rỗng hoặc chỉ có dấu cách) |
| **Các bước thực hiện** | 1. Để trống ô tìm kiếm.<br/>2. Nhấn Enter hoặc nút Tìm kiếm. |
| **Kết quả mong đợi** | Hệ thống tải lại toàn bộ danh sách sự kiện mặc định hoặc không thực hiện lọc gì thêm. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC05_06: Tìm kiếm chứa ký tự đặc biệt an toàn (SQL Injection / XSS Prevention)

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC05_06` |
| **Tên Test Case** | Tìm kiếm chứa ký tự đặc biệt an toàn (SQL Injection / XSS Prevention) |
| **Use Case liên quan** | UC05 - Tìm kiếm sự kiện (Search Events) |
| **Phân loại kiểm thử** | Security |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Hệ thống bảo mật. |
| **Dữ liệu đầu vào (Test Data)** | Chuỗi: '' OR '1'='1', '<script>alert(1)</script>', '%_' |
| **Các bước thực hiện** | 1. Nhập chuỗi độc hại vào ô tìm kiếm.<br/>2. Nhấn tìm kiếm. |
| **Kết quả mong đợi** | Hệ thống escape ký tự an toàn, không xảy ra lỗi 500, không bị tấn công SQL Injection hay thực thi mã JS XSS. Trả về kết quả rỗng một cách an toàn. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC05_07: Nhấn vào kết quả sự kiện chuyển hướng đến trang Chi tiết sự kiện

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC05_07` |
| **Tên Test Case** | Nhấn vào kết quả sự kiện chuyển hướng đến trang Chi tiết sự kiện |
| **Use Case liên quan** | UC05 - Tìm kiếm sự kiện (Search Events) |
| **Phân loại kiểm thử** | Navigation / Integration |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Đã có kết quả tìm kiếm hiển thị sự kiện 'ANH TRAI SAYHI - CONCEPT 5'. |
| **Dữ liệu đầu vào (Test Data)** | N/A |
| **Các bước thực hiện** | 1. Click vào thẻ sự kiện 'ANH TRAI SAYHI - CONCEPT 5' trên danh sách kết quả. |
| **Kết quả mong đợi** | Chuyển hướng chính xác sang trang chi tiết sự kiện (/events/{id}), hiển thị đầy đủ tên, ảnh, thời gian (08h - 12h, 26/09/2026), địa điểm (SVĐ Mỹ Đình), ban tổ chức (VIEON CHANNEL), mô tả, sơ đồ chỗ ngồi, nút 'Mua vé ngay' và danh sách sự kiện gợi ý. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

