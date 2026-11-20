# UC11: Chat Với AI



## 1. Thông Tin Use Case 

- **Mã Use Case:** UC11
- **Tên Use Case:** Chat với trợ lý ảo AI (AI Assistant Chatbot)
- **Tác nhân chính (Actor):** Customer, Staff
- **Mô tả nghiệp vụ:** Cung cấp trợ lý thông minh hỗ trợ giải đáp thắc mắc, tư vấn sự kiện, tìm kiếm lịch diễn thông qua hội thoại tương tác thời gian thực.
- **Tiền điều kiện (Pre-conditions):** Người dùng đã đăng nhập vào hệ thống EventHub.
- **Hậu điều kiện (Post-conditions):** Hệ thống chatbot AI phản hồi câu hỏi của người dùng và đưa ra thông tin gợi ý các sự kiện phù hợp.

## 2. Danh Sách Test Cases Tổng Hợp

| STT | Mã Test Case | Tên / Mục Tiêu Test Case | Phân Loại | Mức Độ Ưu Tiên |
|:---:|:---|:---|:---|:---:|
| 1 | **TC_UC11_01** | Mở cửa sổ Chatbot AI và hiển thị thông điệp chào mừng | Functional (Happy Path) | **High** |
| 2 | **TC_UC11_02** | Gửi tin nhắn hỏi sự kiện và nhận phản hồi gợi ý sự kiện chính xác từ AI | Functional (Happy Path) | **High** |
| 3 | **TC_UC11_03** | Gửi tin nhắn thất bại khi để ô nhập tin nhắn trống | Validation (Negative) | **Low** |
| 4 | **TC_UC11_04** | Xử lý khi hệ thống dịch vụ AI gặp sự cố hoặc timeout | Exception / Reliability | **Medium** |
| 5 | **TC_UC11_05** | Đóng và mở lại cửa sổ chat giữ nguyên lịch sử trò chuyện trong phiên | UI / State Management | **Medium** |
| 6 | **TC_UC11_06** | Kiểm tra giới hạn độ dài ký tự của tin nhắn gửi đi | Boundary | **Low** |
| 7 | **TC_UC11_07** | Chống chèn mã độc HTML/Script qua khung chat (XSS Prevention) | Security | **High** |

---

## 3. Thiết Kế Chi Tiết Từng Test Case

### TC_UC11_01: Mở cửa sổ Chatbot AI và hiển thị thông điệp chào mừng

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC11_01` |
| **Tên Test Case** | Mở cửa sổ Chatbot AI và hiển thị thông điệp chào mừng |
| **Use Case liên quan** | UC11 - Chat với trợ lý ảo AI (AI Assistant Chatbot) |
| **Phân loại kiểm thử** | Functional (Happy Path) |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Đã đăng nhập tài khoản. |
| **Dữ liệu đầu vào (Test Data)** | N/A |
| **Các bước thực hiện** | 1. Tại giao diện chính, click vào biểu tượng chatbot ở góc dưới cùng bên phải màn hình.<br/>2. Quan sát cửa sổ chat hiển thị. |
| **Kết quả mong đợi** | Cửa sổ chat mở ra với tiêu đề 'AI Assistant', hiển thị khung chat, lời chào tự động: 'Xin chào, tôi có thể giúp gì cho bạn hôm nay?', ô nhập tin nhắn và nút 'Gửi'. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC11_02: Gửi tin nhắn hỏi sự kiện và nhận phản hồi gợi ý sự kiện chính xác từ AI

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC11_02` |
| **Tên Test Case** | Gửi tin nhắn hỏi sự kiện và nhận phản hồi gợi ý sự kiện chính xác từ AI |
| **Use Case liên quan** | UC11 - Chat với trợ lý ảo AI (AI Assistant Chatbot) |
| **Phân loại kiểm thử** | Functional (Happy Path) |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Hệ thống có sự kiện 'Anh trai say hi' đang mở bán. |
| **Dữ liệu đầu vào (Test Data)** | Tin nhắn: 'Hôm nay có sự kiện gì hot?' |
| **Các bước thực hiện** | 1. Mở cửa sổ chat AI.<br/>2. Nhập vào ô chat: 'Hôm nay có sự kiện gì hot?'.<br/>3. Nhấn nút 'Gửi' hoặc phím Enter. |
| **Kết quả mong đợi** | 1. Tin nhắn của người dùng xuất hiện bên phải khung chat.<br/>2. Hiệu ứng đang soạn tin nhắn (typing indicator) hiển thị.<br/>3. AI phản hồi câu trả lời liệt kê các sự kiện nổi bật: 'Hôm nay có sự kiện Anh Trai Say Hi... kèm link xem chi tiết'. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC11_03: Gửi tin nhắn thất bại khi để ô nhập tin nhắn trống

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC11_03` |
| **Tên Test Case** | Gửi tin nhắn thất bại khi để ô nhập tin nhắn trống |
| **Use Case liên quan** | UC11 - Chat với trợ lý ảo AI (AI Assistant Chatbot) |
| **Phân loại kiểm thử** | Validation (Negative) |
| **Mức độ ưu tiên** | **Low** |
| **Tiền điều kiện** | Cửa sổ chat đang mở, ô nhập tin nhắn trống hoặc chỉ toàn khoảng trắng. |
| **Dữ liệu đầu vào (Test Data)** | Chuỗi: '   ' (khoảng trắng) |
| **Các bước thực hiện** | 1. Không nhập gì hoặc nhập toàn phím cách vào ô chat.<br/>2. Nhấn nút 'Gửi' hoặc bấm Enter. |
| **Kết quả mong đợi** | Hệ thống không gửi tin nhắn, nút 'Gửi' bị disabled hoặc không phát sinh request lên server. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC11_04: Xử lý khi hệ thống dịch vụ AI gặp sự cố hoặc timeout

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC11_04` |
| **Tên Test Case** | Xử lý khi hệ thống dịch vụ AI gặp sự cố hoặc timeout |
| **Use Case liên quan** | UC11 - Chat với trợ lý ảo AI (AI Assistant Chatbot) |
| **Phân loại kiểm thử** | Exception / Reliability |
| **Mức độ ưu tiên** | **Medium** |
| **Tiền điều kiện** | Mất kết nối mạng hoặc server AI trả về mã 503 / Timeout. |
| **Dữ liệu đầu vào (Test Data)** | Tin nhắn: 'Tìm kiếm sự kiện âm nhạc' |
| **Các bước thực hiện** | 1. Gửi tin nhắn trong điều kiện ngắt kết nối API AI.<br/>2. Quan sát phản hồi của chatbot. |
| **Kết quả mong đợi** | Chatbot không bị treo, hiển thị thông báo lỗi thân thiện: 'Hiện tại hệ thống AI đang bận hoặc quá tải. Vui lòng thử lại sau ít phút!'. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC11_05: Đóng và mở lại cửa sổ chat giữ nguyên lịch sử trò chuyện trong phiên

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC11_05` |
| **Tên Test Case** | Đóng và mở lại cửa sổ chat giữ nguyên lịch sử trò chuyện trong phiên |
| **Use Case liên quan** | UC11 - Chat với trợ lý ảo AI (AI Assistant Chatbot) |
| **Phân loại kiểm thử** | UI / State Management |
| **Mức độ ưu tiên** | **Medium** |
| **Tiền điều kiện** | Đã có các tin nhắn trao đổi trong cửa sổ chat. |
| **Dữ liệu đầu vào (Test Data)** | N/A |
| **Các bước thực hiện** | 1. Nhấn nút 'Đóng' (x) trên cửa sổ chat.<br/>2. Nhấn lại biểu tượng chatbot để mở lại. |
| **Kết quả mong đợi** | Cửa sổ mở lại và các tin nhắn đã trò chuyện trước đó vẫn hiển thị đầy đủ, không bị mất. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC11_06: Kiểm tra giới hạn độ dài ký tự của tin nhắn gửi đi

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC11_06` |
| **Tên Test Case** | Kiểm tra giới hạn độ dài ký tự của tin nhắn gửi đi |
| **Use Case liên quan** | UC11 - Chat với trợ lý ảo AI (AI Assistant Chatbot) |
| **Phân loại kiểm thử** | Boundary |
| **Mức độ ưu tiên** | **Low** |
| **Tiền điều kiện** | Cửa sổ chat đang mở. |
| **Dữ liệu đầu vào (Test Data)** | Chuỗi văn bản dài hơn 1000 ký tự |
| **Các bước thực hiện** | 1. Dán văn bản dài hơn 1000 ký tự vào ô chat.<br/>2. Quan sát giới hạn hiển thị hoặc gửi tin. |
| **Kết quả mong đợi** | Hệ thống cắt gọn hoặc hiển thị cảnh báo tin nhắn vượt quá độ dài tối đa cho phép. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

### TC_UC11_07: Chống chèn mã độc HTML/Script qua khung chat (XSS Prevention)

| Thuộc tính | Chi tiết |
|:---|:---|
| **Mã Test Case** | `TC_UC11_07` |
| **Tên Test Case** | Chống chèn mã độc HTML/Script qua khung chat (XSS Prevention) |
| **Use Case liên quan** | UC11 - Chat với trợ lý ảo AI (AI Assistant Chatbot) |
| **Phân loại kiểm thử** | Security |
| **Mức độ ưu tiên** | **High** |
| **Tiền điều kiện** | Cửa sổ chat đang mở. |
| **Dữ liệu đầu vào (Test Data)** | Tin nhắn: '<script>alert("XSS")</script>' |
| **Các bước thực hiện** | 1. Gửi tin nhắn chứa thẻ script độc hại.<br/>2. Quan sát tin nhắn hiển thị trong khung chat. |
| **Kết quả mong đợi** | Mã script được escape thành text thuần túy, không có hộp thoại alert nào được kích hoạt. |
| **Kết quả thực tế** | *(Chờ thực thi kiểm thử)* |
| **Trạng thái** | Ready (Sẵn sàng kiểm thử) |

