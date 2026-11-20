# UC21: Duyệt Yêu Cầu Rút Tiền

## 1. Thông Tin Use Case

- **Mã Use Case:** UC21
- **Tên Use Case:** Phê duyệt yêu cầu rút tiền của Nhà tổ chức (Approve Payout Request)
- **Tác nhân chính (Actor):** Admin (Quản trị viên)
- **Mô tả nghiệp vụ:** Cung cấp cho Admin quy trình đối soát và phê duyệt các lệnh rút tiền doanh thu từ Organizer sau khi Admin đã thực hiện chuyển khoản ngoài hệ thống ngân hàng.
- **Tiền điều kiện (Pre-conditions):** Quản trị viên đã đăng nhập vào hệ thống EventHub.
- **Hậu điều kiện (Post-conditions):** Yêu cầu rút tiền chuyển sang trạng thái 'Đã duyệt' (APPROVED), lưu mã giao dịch ngân hàng, trừ số dư chờ rút và cộng vào tổng số tiền đã rút của Organizer, gửi webhook/thông báo kết quả cho Organizer.

## 2. Danh Sách Test Cases Tổng Hợp

| STT | Mã Test Case | Tên / Mục Tiêu Test Case | Phân Loại | Mức Độ Ưu Tiên |
|:---:|:---|:---|:---|:---:|
| 1 | **TC_UC21_01** | Xem danh sách và lọc yêu cầu rút tiền theo trạng thái Chờ duyệt | Functional (Happy Path) | **High** |
| 2 | **TC_UC21_02** | Xem chi tiết yêu cầu rút tiền của Nhà tổ chức | Functional (Happy Path) | **High** |
| 3 | **TC_UC21_03** | Duyệt yêu cầu rút tiền thành công khi nhập Mã giao dịch ngân hàng hợp lệ | Functional (Happy Path) | **High** |
| 4 | **TC_UC21_04** | Duyệt yêu cầu rút tiền thất bại khi để trống Mã giao dịch ngân hàng | Validation (Negative) | **High** |
| 5 | **TC_UC21_05** | Từ chối yêu cầu rút tiền và hoàn tiền về Số dư khả dụng của Organizer | Functional / Financial | **High** |
| 6 | **TC_UC21_06** | Xử lý gửi Webhook thông báo kết quả duyệt rút tiền thất bại | Exception / Integration | **Medium** |
| 7 | **TC_UC21_07** | Hiển thị thông báo khi không có yêu cầu rút tiền nào cần duyệt | UI / Boundary | **Low** |
| 8 | **TC_UC21_08** | Organizer kiểm tra số dư ví được cập nhật ngay sau khi Admin duyệt | Financial / Integration | **High** |

---

## 3. Thiết Kế Chi Tiết Từng Test Case

### TC_UC21_01: Xem danh sách và lọc yêu cầu rút tiền theo trạng thái Chờ duyệt

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC21_01` |
| **Tên Test Case** | Xem danh sách và lọc yêu cầu rút tiền theo trạng thái Chờ duyệt |
| **Use Case liên quan** | UC21 - Phê duyệt yêu cầu rút tiền của Nhà tổ chức (Approve Payout Request) |
| **Phân loại kiểm thử** | Functional (Happy Path) |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Có các yêu cầu rút tiền ở trạng thái 'Chờ duyệt' và 'Đã duyệt'. |
| **Dữ liệu đầu vào (Test Data)** | Bộ lọc: 'Chờ duyệt' |
| **Các bước thực hiện** | 1. Admin chọn menu 'Yêu cầu rút tiền'.<br/>2. Chọn bộ lọc theo trạng thái 'Chờ duyệt'.<br/>3. Quan sát danh sách hiển thị. |
| **Kết quả mong đợi** | Danh sách chỉ hiển thị các yêu cầu có trạng thái 'Chờ duyệt' (ví dụ yêu cầu của Trần Thị B: 10.000.000đ, Vietcombank, Ngày 10/09/2026). |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC21_02: Xem chi tiết yêu cầu rút tiền của Nhà tổ chức

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC21_02` |
| **Tên Test Case** | Xem chi tiết yêu cầu rút tiền của Nhà tổ chức |
| **Use Case liên quan** | UC21 - Phê duyệt yêu cầu rút tiền của Nhà tổ chức (Approve Payout Request) |
| **Phân loại kiểm thử** | Functional (Happy Path) |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Có yêu cầu rút tiền của Trần Thị B. |
| **Dữ liệu đầu vào (Test Data)** | N/A |
| **Các bước thực hiện** | 1. Nhấn xem chi tiết yêu cầu rút tiền của Trần Thị B. |
| **Kết quả mong đợi** | Hiển thị đầy đủ thông tin: Tên Nhà tổ chức (Trần Thị B), Số tiền (10.000.000đ), Tên ngân hàng (Vietcombank), Số tài khoản (0123456789), Ngày yêu cầu, Trạng thái ('Chờ duyệt') cùng 2 nút: 'Duyệt' và 'Từ chối'. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC21_03: Duyệt yêu cầu rút tiền thành công khi nhập Mã giao dịch ngân hàng hợp lệ

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC21_03` |
| **Tên Test Case** | Duyệt yêu cầu rút tiền thành công khi nhập Mã giao dịch ngân hàng hợp lệ |
| **Use Case liên quan** | UC21 - Phê duyệt yêu cầu rút tiền của Nhà tổ chức (Approve Payout Request) |
| **Phân loại kiểm thử** | Functional (Happy Path) |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Admin đã thực hiện chuyển tiền 10.000.000đ cho Trần Thị B qua ngân hàng thành công. |
| **Dữ liệu đầu vào (Test Data)** | Mã giao dịch ngân hàng: 'MGD001' |
| **Các bước thực hiện** | 1. Tại màn chi tiết yêu cầu, nhấn nút 'Duyệt'.<br/>2. Hệ thống hiển thị ô nhập 'Mã giao dịch chuyển tiền từ ngân hàng'. Nhập 'MGD001'.<br/>3. Nhấn 'Xác nhận duyệt'. |
| **Kết quả mong đợi** | Hệ thống cập nhật trạng thái yêu cầu thành 'Đã duyệt'. Lưu mã giao dịch 'MGD001', tên Admin duyệt và thời gian duyệt. Số tiền 10.000.000đ được trừ khỏi Số dư đang chờ rút và cộng vào Tổng số tiền đã rút của Trần Thị B. Gửi thông báo đến Organizer. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC21_04: Duyệt yêu cầu rút tiền thất bại khi để trống Mã giao dịch ngân hàng

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC21_04` |
| **Tên Test Case** | Duyệt yêu cầu rút tiền thất bại khi để trống Mã giao dịch ngân hàng |
| **Use Case liên quan** | UC21 - Phê duyệt yêu cầu rút tiền của Nhà tổ chức (Approve Payout Request) |
| **Phân loại kiểm thử** | Validation (Negative) |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Đang ở popup nhập mã giao dịch ngân hàng. |
| **Dữ liệu đầu vào (Test Data)** | Mã giao dịch: '' (để trống) |
| **Các bước thực hiện** | 1. Nhấn nút 'Duyệt'.<br/>2. Không nhập gì vào ô mã giao dịch.<br/>3. Nhấn 'Xác nhận duyệt'. |
| **Kết quả mong đợi** | Hệ thống chặn duyệt và báo lỗi: 'Vui lòng nhập mã giao dịch chuyển tiền từ ngân hàng để đối soát'. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC21_05: Từ chối yêu cầu rút tiền và hoàn tiền về Số dư khả dụng của Organizer

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC21_05` |
| **Tên Test Case** | Từ chối yêu cầu rút tiền và hoàn tiền về Số dư khả dụng của Organizer |
| **Use Case liên quan** | UC21 - Phê duyệt yêu cầu rút tiền của Nhà tổ chức (Approve Payout Request) |
| **Phân loại kiểm thử** | Functional / Financial |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Yêu cầu rút tiền sai thông tin số tài khoản hoặc ngân hàng thụ hưởng. |
| **Dữ liệu đầu vào (Test Data)** | Lý do từ chối: 'Số tài khoản ngân hàng không đúng hoặc không tồn tại' |
| **Các bước thực hiện** | 1. Tại màn chi tiết yêu cầu, nhấn nút 'Từ chối'.<br/>2. Nhập lý do từ chối.<br/>3. Nhấn 'Xác nhận từ chối'. |
| **Kết quả mong đợi** | Yêu cầu chuyển sang trạng thái 'Bị từ chối'. Số tiền rút (10.000.000đ) được hoàn trả từ 'Số dư đang chờ rút' quay trở lại 'Số dư khả dụng' trong ví của Organizer. Gửi thông báo lý do từ chối đến Organizer. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC21_06: Xử lý gửi Webhook thông báo kết quả duyệt rút tiền thất bại

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC21_06` |
| **Tên Test Case** | Xử lý gửi Webhook thông báo kết quả duyệt rút tiền thất bại |
| **Use Case liên quan** | UC21 - Phê duyệt yêu cầu rút tiền của Nhà tổ chức (Approve Payout Request) |
| **Phân loại kiểm thử** | Exception / Integration |
| **Mức độ ưu tiên** | **Medium** |
| **Tiền điều kiện** | Hệ thống có webhook thông báo kết quả sang hệ thống đối tác/email của Organizer gặp lỗi kết nối. |
| **Dữ liệu đầu vào (Test Data)** | Mã giao dịch: 'MGD002' |
| **Các bước thực hiện** | 1. Thực hiện duyệt yêu cầu rút tiền.<br/>2. Webhook gửi đi gặp lỗi (HTTP 500 hoặc Timeout). |
| **Kết quả mong đợi** | Hệ thống vẫn giữ nguyên trạng thái 'Đã duyệt' cho yêu cầu (không bị rollback sai lệch tiền), ghi nhận log lỗi gửi webhook và lên lịch tự động gửi lại (retry background task). |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC21_07: Hiển thị thông báo khi không có yêu cầu rút tiền nào cần duyệt

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC21_07` |
| **Tên Test Case** | Hiển thị thông báo khi không có yêu cầu rút tiền nào cần duyệt |
| **Use Case liên quan** | UC21 - Phê duyệt yêu cầu rút tiền của Nhà tổ chức (Approve Payout Request) |
| **Phân loại kiểm thử** | UI / Boundary |
| **Mức độ ưu tiên** | **Low** |
| **Tiền điều kiện** | Không có yêu cầu rút tiền nào đang chờ duyệt. |
| **Dữ liệu đầu vào (Test Data)** | N/A |
| **Các bước thực hiện** | 1. Mở danh sách lọc theo 'Chờ duyệt'. |
| **Kết quả mong đợi** | Hệ thống hiển thị bảng trống kèm thông báo: 'Không có yêu cầu rút tiền nào cần phê duyệt'. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC21_08: Organizer kiểm tra số dư ví được cập nhật ngay sau khi Admin duyệt

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC21_08` |
| **Tên Test Case** | Organizer kiểm tra số dư ví được cập nhật ngay sau khi Admin duyệt |
| **Use Case liên quan** | UC21 - Phê duyệt yêu cầu rút tiền của Nhà tổ chức (Approve Payout Request) |
| **Phân loại kiểm thử** | Financial / Integration |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Admin vừa duyệt thành công lệnh rút 10.000.000đ của Trần Thị B. |
| **Dữ liệu đầu vào (Test Data)** | N/A |
| **Các bước thực hiện** | 1. Đăng nhập tài khoản Organizer Trần Thị B.<br/>2. Mở trang 'Doanh thu & Số dư'. |
| **Kết quả mong đợi** | Số dư đang chờ rút giảm 10.000.000đ, Tổng số tiền đã rút tăng 10.000.000đ. Nhận được thông báo: 'Yêu cầu rút tiền 10.000.000đ của bạn đã được duyệt thành công. Mã giao dịch: MGD001'. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

