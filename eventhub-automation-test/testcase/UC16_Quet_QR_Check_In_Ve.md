# UC16: Quét QR Check-In Vé

## 1. Thông Tin Use Case

- **Mã Use Case:** UC16
- **Tên Use Case:** Quét mã QR soát vé và check-in (QR Check-in on Mobile)
- **Tác nhân chính (Actor):** Staff (Nhân viên soát vé)
- **Mô tả nghiệp vụ:** Cho phép nhân viên sử dụng camera điện thoại quét mã QR trên vé điện tử của khách hàng tại cổng sự kiện để xác thực tính hợp lệ và ghi nhận check-in.
- **Tiền điều kiện (Pre-conditions):** Staff đã đăng nhập vào ứng dụng trên thiết bị di động và đã chọn sự kiện mình được phân công phụ trách đối chiếu.
- **Hậu điều kiện (Post-conditions):** Vé hợp lệ được cập nhật trạng thái 'Đã sử dụng' (USED). Hệ thống lưu lại mã nhân viên thực hiện check-in và thời điểm check-in chính xác.

## 2. Danh Sách Test Cases Tổng Hợp

| STT | Mã Test Case | Tên / Mục Tiêu Test Case | Phân Loại | Mức Độ Ưu Tiên |
|:---:|:---|:---|:---|:---:|
| 1 | **TC_UC16_01** | Quét và Check-in thành công cho vé hợp lệ chưa sử dụng | Functional (Happy Path) | **High** |
| 2 | **TC_UC16_02** | Cảnh báo khi quét vé đã được Check-in trước đó (Vé đã sử dụng) | Security / Business Rule | **High** |
| 3 | **TC_UC16_03** | Từ chối Check-in khi vé không thuộc sự kiện Staff đã chọn | Business Rule (Negative) | **High** |
| 4 | **TC_UC16_04** | Từ chối Check-in khi vé thuộc booking chưa thanh toán hoặc sự kiện đã bị hủy | Business Rule (Negative) | **High** |
| 5 | **TC_UC16_05** | Xử lý khi mã QR bị mờ, rách hoặc chuỗi QR không tồn tại trong hệ thống | Exception / Boundary | **Medium** |
| 6 | **TC_UC16_06** | Kiểm tra tính năng Bật/Tắt đèn Flash khi quét vé trong môi trường thiếu sáng | UI / Hardware | **Low** |
| 7 | **TC_UC16_07** | Nhập mã vé thủ công bằng bàn phím khi camera không đọc được QR | Functional (Alternative Flow) | **Medium** |
| 8 | **TC_UC16_08** | Tự động thống kê số lượng vé đã check-in theo thời gian thực | Functional / Statistics | **Low** |

---

## 3. Thiết Kế Chi Tiết Từng Test Case

### TC_UC16_01: Quét và Check-in thành công cho vé hợp lệ chưa sử dụng

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC16_01` |
| **Tên Test Case** | Quét và Check-in thành công cho vé hợp lệ chưa sử dụng |
| **Use Case liên quan** | UC16 - Quét mã QR soát vé và check-in (QR Check-in on Mobile) |
| **Phân loại kiểm thử** | Functional (Happy Path) |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Staff đang phụ trách sự kiện 'Đêm nhạc Acoustic Thu', khách hàng xuất trình mã QR vé VIP hợp lệ (TK-20261020-0042) chưa từng check-in. |
| **Dữ liệu đầu vào (Test Data)** | Mã vé: 'TK-20261020-0042', Khách hàng: 'Nguyễn Văn D', Loại vé: 'Vé VIP' |
| **Các bước thực hiện** | 1. Staff chọn sự kiện 'Đêm nhạc Acoustic Thu' trên app.<br/>2. Chọn tính năng 'Quét mã QR'. Hệ thống kích hoạt camera.<br/>3. Đưa camera hướng vào mã QR trên điện thoại khách hàng.<br/>4. Hệ thống hiển thị kết quả: 'Vé hợp lệ' kèm Tên khách hàng (Nguyễn Văn D), Loại vé (Vé VIP), Mã vé (TK-20261020-0042).<br/>5. Staff nhấn nút 'Xác nhận Check-in'. |
| **Kết quả mong đợi** | Hệ thống thông báo: 'Check-in thành công!'. Cập nhật trạng thái vé thành 'Đã sử dụng', lưu lại thông tin Staff và thời gian check-in. Khách hàng hợp lệ được vào cửa. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC16_02: Cảnh báo khi quét vé đã được Check-in trước đó (Vé đã sử dụng)

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC16_02` |
| **Tên Test Case** | Cảnh báo khi quét vé đã được Check-in trước đó (Vé đã sử dụng) |
| **Use Case liên quan** | UC16 - Quét mã QR soát vé và check-in (QR Check-in on Mobile) |
| **Phân loại kiểm thử** | Security / Business Rule |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Mã vé 'TK-20261020-0042' đã được check-in vào lúc 18:30 cùng ngày. |
| **Dữ liệu đầu vào (Test Data)** | Mã QR của vé đã sử dụng |
| **Các bước thực hiện** | 1. Quét mã QR của vé đã check-in trước đó. |
| **Kết quả mong đợi** | Hệ thống hiển thị màn hình cảnh báo màu đỏ: 'VÉ ĐÃ ĐƯỢC SỬ DỤNG' kèm thông tin chi tiết thời gian đã check-in (ví dụ: 'Đã check-in lúc 18:30 bởi Staff Trần Văn B'). Không cho phép check-in lại. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC16_03: Từ chối Check-in khi vé không thuộc sự kiện Staff đã chọn

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC16_03` |
| **Tên Test Case** | Từ chối Check-in khi vé không thuộc sự kiện Staff đã chọn |
| **Use Case liên quan** | UC16 - Quét mã QR soát vé và check-in (QR Check-in on Mobile) |
| **Phân loại kiểm thử** | Business Rule (Negative) |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Staff chọn sự kiện A, nhưng khách hàng đưa vé của sự kiện B. |
| **Dữ liệu đầu vào (Test Data)** | Vé của sự kiện khác |
| **Các bước thực hiện** | 1. Quét mã QR của sự kiện khác. |
| **Kết quả mong đợi** | Hệ thống hiển thị cảnh báo: 'Vé không hợp lệ: Vé này thuộc sự kiện khác (ANH TRAI SAY HI), không thuộc sự kiện Đêm nhạc Acoustic Thu'. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC16_04: Từ chối Check-in khi vé thuộc booking chưa thanh toán hoặc sự kiện đã bị hủy

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC16_04` |
| **Tên Test Case** | Từ chối Check-in khi vé thuộc booking chưa thanh toán hoặc sự kiện đã bị hủy |
| **Use Case liên quan** | UC16 - Quét mã QR soát vé và check-in (QR Check-in on Mobile) |
| **Phân loại kiểm thử** | Business Rule (Negative) |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Vé thuộc đơn hàng bị hủy hoặc sự kiện đã hủy. |
| **Dữ liệu đầu vào (Test Data)** | Vé có trạng thái CANCELLED |
| **Các bước thực hiện** | 1. Quét mã QR của vé bị hủy. |
| **Kết quả mong đợi** | Hệ thống thông báo: 'Vé không hợp lệ: Vé này đã bị hủy hoặc chưa hoàn tất thanh toán'. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC16_05: Xử lý khi mã QR bị mờ, rách hoặc chuỗi QR không tồn tại trong hệ thống

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC16_05` |
| **Tên Test Case** | Xử lý khi mã QR bị mờ, rách hoặc chuỗi QR không tồn tại trong hệ thống |
| **Use Case liên quan** | UC16 - Quét mã QR soát vé và check-in (QR Check-in on Mobile) |
| **Phân loại kiểm thử** | Exception / Boundary |
| **Mức độ ưu tiên** | **Medium** |
| **Tiền điều kiện** | Khách hàng đưa mã QR ngẫu nhiên (ví dụ mã vạch sản phẩm hoặc QR giả mạo). |
| **Dữ liệu đầu vào (Test Data)** | Chuỗi QR giả mạo: 'UNKNOWN_QR_12345' |
| **Các bước thực hiện** | 1. Quét mã QR giả mạo.<br/>2. Quan sát thông báo từ ứng dụng. |
| **Kết quả mong đợi** | Hệ thống thông báo lỗi: 'Mã QR không hợp lệ hoặc không tồn tại trong hệ thống EventHub' và cho phép tiếp tục quét lại. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC16_06: Kiểm tra tính năng Bật/Tắt đèn Flash khi quét vé trong môi trường thiếu sáng

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC16_06` |
| **Tên Test Case** | Kiểm tra tính năng Bật/Tắt đèn Flash khi quét vé trong môi trường thiếu sáng |
| **Use Case liên quan** | UC16 - Quét mã QR soát vé và check-in (QR Check-in on Mobile) |
| **Phân loại kiểm thử** | UI / Hardware |
| **Mức độ ưu tiên** | **Low** |
| **Tiền điều kiện** | Đang mở giao diện camera quét mã QR. |
| **Dữ liệu đầu vào (Test Data)** | N/A |
| **Các bước thực hiện** | 1. Nhấn vào biểu tượng đèn pin (Flash).<br/>2. Nhấn lại một lần nữa. |
| **Kết quả mong đợi** | Đèn flash điện thoại bật sáng để hỗ trợ đọc mã QR trong tối, nhấn lần 2 đèn tắt bình thường. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC16_07: Nhập mã vé thủ công bằng bàn phím khi camera không đọc được QR

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC16_07` |
| **Tên Test Case** | Nhập mã vé thủ công bằng bàn phím khi camera không đọc được QR |
| **Use Case liên quan** | UC16 - Quét mã QR soát vé và check-in (QR Check-in on Mobile) |
| **Phân loại kiểm thử** | Functional (Alternative Flow) |
| **Mức độ ưu tiên** | **Medium** |
| **Tiền điều kiện** | Màn hình điện thoại của khách hàng bị nứt vỡ khiến camera không thể quét QR. |
| **Dữ liệu đầu vào (Test Data)** | Mã vé nhập tay: 'TK-20261020-0042' |
| **Các bước thực hiện** | 1. Chọn 'Nhập mã thủ công'.<br/>2. Gõ mã vé 'TK-20261020-0042'.<br/>3. Nhấn 'Kiểm tra'. |
| **Kết quả mong đợi** | Hệ thống tra cứu theo mã ký tự và hiển thị kết quả kiểm tra vé tương tự như khi quét QR thành công. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC16_08: Tự động thống kê số lượng vé đã check-in theo thời gian thực

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC16_08` |
| **Tên Test Case** | Tự động thống kê số lượng vé đã check-in theo thời gian thực |
| **Use Case liên quan** | UC16 - Quét mã QR soát vé và check-in (QR Check-in on Mobile) |
| **Phân loại kiểm thử** | Functional / Statistics |
| **Mức độ ưu tiên** | **Low** |
| **Tiền điều kiện** | Staff đang soát vé tại sự kiện. |
| **Dữ liệu đầu vào (Test Data)** | N/A |
| **Các bước thực hiện** | 1. Thực hiện check-in thành công cho 1 vé.<br/>2. Quan sát bộ đếm vé trên màn hình Staff. |
| **Kết quả mong đợi** | Bộ đếm 'Đã check-in / Tổng số vé' tự động nhảy số tăng thêm 1 (ví dụ: '43/100 vé VIP'). |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

