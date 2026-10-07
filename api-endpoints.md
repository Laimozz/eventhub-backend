# Tài liệu API Endpoint — EventHub

Base URL local: `http://localhost:8080`.

Tài liệu mô tả các endpoint đang được triển khai trong `AuthController` và `EventController`.

### Headers và cookie

Mọi endpoint dưới đây đều dùng POST và bắt buộc có:

```http
X-CSRF-Protection: 1
```

## 2. Danh sách endpoint

| Use Case | Tên API | Method + Endpoint | Authentication | Thành công |
| --- | --- | --- | --- | --- |
| UC01 | Đăng ký tài khoản | `POST /api/auth/register` | Public | `201 Created` |
| UC02 | Đăng nhập | `POST /api/auth/login` | Public | `200 OK` |
| Hỗ trợ UC02 | Cấp lại token | `POST /api/auth/refresh` | Required — refresh token | `200 OK` |
| UC03 | Đăng xuất | `POST /api/auth/logout` | Public — cookie tùy chọn | `204 No Content` |
| UC19 | Tạo sự kiện | `POST /api/events` | Required — access token, role `ORGANIZER` | `201 Created` |

## 3. Đăng ký tài khoản

| Mục | Nội dung |
| --- | --- |
| **Method + Endpoint** | `POST /api/auth/register` |
| **Authentication** | **Public** — không cần access/refresh token |
| **Role được phép sử dụng** | Guest; endpoint không kiểm tra role người gọi. Role tài khoản được tạo chỉ được là `CUSTOMER` hoặc `ORGANIZER`. |
| **Path Parameters** | Không có |
| **Query Parameters** | Không có |

### Request Body

Bắt buộc gửi JSON với `Content-Type: application/json` và `X-CSRF-Protection: 1`.

| Field | Kiểu | Bắt buộc | Quy tắc |
| --- | --- | --- | --- |
| `email` | string | Có | Đúng định dạng email, tối đa 255 ký tự; trim, chuyển chữ thường; không trùng email đã đăng ký. |
| `password` | string | Có | Không rỗng/toàn khoảng trắng; 8–72 ký tự và tối đa 72 byte UTF-8; không trim. |
| `fullName` | string | Có | Không rỗng/toàn khoảng trắng; tối đa 255 ký tự; trim. |
| `phone` | string | Không | Có thể bỏ qua hoặc null; tối đa 20 ký tự; trim. |
| `role` | string | Có | Chính xác `CUSTOMER` hoặc `ORGANIZER`; không cho tự đăng ký `ADMIN` hoặc `STAFF`. |

```json
{
  "email": "customer@example.com",
  "password": "Example-password-123",
  "fullName": "Nguyễn Văn A",
  "phone": "0901234567",
  "role": "CUSTOMER"
}
```

### Response mẫu

**Thành công — `201 Created`**, body là chuỗi text theo controller hiện tại:

```http
HTTP/1.1 201 Created
Content-Type: text/plain;charset=UTF-8

Registration successful
```

Tài khoản được tạo với trạng thái `ACTIVE`. Đăng ký không tự đăng nhập, không tạo phiên trong `refresh_tokens`, không trả thông tin user và không gửi cookie token. Frontend gọi đăng nhập riêng sau khi đăng ký thành công.

**Email đã tồn tại — `409 Conflict`:**

```json
{
  "timestamp": "2026-09-30T14:00:00Z",
  "status": 409,
  "code": "REQUEST_ERROR",
  "message": "Email is already registered",
  "path": "/api/auth/register",
  "errors": {}
}
```

## 4. Đăng nhập

| Mục | Nội dung |
| --- | --- |
| **Method + Endpoint** | `POST /api/auth/login` |
| **Authentication** | **Public** — xác thực email/password trong body, không cần token trước đó |
| **Role được phép sử dụng** | Tài khoản `CUSTOMER`, `ORGANIZER`, `STAFF`, `ADMIN` có trạng thái `ACTIVE`. Người gọi chưa cần đăng nhập. |
| **Path Parameters** | Không có |
| **Query Parameters** | Không có |

### Request Body

Bắt buộc gửi JSON với `Content-Type: application/json` và `X-CSRF-Protection: 1`.

| Field | Kiểu | Bắt buộc | Quy tắc |
| --- | --- | --- | --- |
| `email` | string | Có | Đúng định dạng, tối đa 255 ký tự; trim và chuyển chữ thường. |
| `password` | string | Có | Không rỗng/toàn khoảng trắng; tối đa 72 ký tự theo DTO; không trim. Service từ chối mật khẩu vượt 72 byte UTF-8 với `401`. |

```json
{
  "email": "customer@example.com",
  "password": "Example-password-123"
}
```

### Response mẫu

**Thành công — `200 OK`:**

```http
HTTP/1.1 200 OK
Content-Type: application/json
Set-Cookie: access_token=<access-token>; Path=/api; Max-Age=900; HttpOnly; SameSite=Lax
Set-Cookie: refresh_token=<refresh-token>; Path=/api/auth; Max-Age=604800; HttpOnly; SameSite=Lax

{
  "id": 1,
  "email": "customer@example.com",
  "fullName": "Nguyễn Văn A",
  "phone": "0901234567",
  "role": "CUSTOMER"
}
```

JSON là `UserResponse`, chỉ có một `role`, không có mảng `roles` hoặc token. `fullName`/`phone` có thể null nếu dữ liệu tài khoản không có giá trị. Mỗi lần đăng nhập thành công tạo một phiên độc lập; không tự thu hồi các phiên trước đó.

**Thông tin đăng nhập không hợp lệ — `401 Unauthorized`:**

```json
{
  "timestamp": "2026-09-30T14:00:00Z",
  "status": 401,
  "code": "UNAUTHORIZED",
  "message": "Invalid credentials or session",
  "path": "/api/auth/login",
  "errors": {}
}
```

## 5. Cấp lại token

| Mục | Nội dung |
| --- | --- |
| **Method + Endpoint** | `POST /api/auth/refresh` |
| **Authentication** | **Required** — cookie `refresh_token` hợp lệ. Không cần access token còn hạn; service kiểm tra refresh token dù đường dẫn được `permitAll()`. |
| **Role được phép sử dụng** | `CUSTOMER`, `ORGANIZER`, `STAFF`, `ADMIN`; user của phiên phải `ACTIVE`. Không yêu cầu role cụ thể. |
| **Path Parameters** | Không có |
| **Query Parameters** | Không có |

### Request Body

Không có. Gửi cookie refresh token và header CSRF:

```http
POST /api/auth/refresh HTTP/1.1
Host: localhost:8080
X-CSRF-Protection: 1
Cookie: refresh_token=<refresh-token>
```

Trình duyệt gửi cookie tự động khi credentials và phạm vi cookie phù hợp; frontend không đọc cookie HttpOnly để tự tạo header.

### Response mẫu

**Thành công — `200 OK`:**

```http
HTTP/1.1 200 OK
Content-Type: application/json
Set-Cookie: access_token=<new-access-token>; Path=/api; Max-Age=900; HttpOnly; SameSite=Lax
Set-Cookie: refresh_token=<new-refresh-token>; Path=/api/auth; Max-Age=604800; HttpOnly; SameSite=Lax

{
  "id": 1,
  "email": "customer@example.com",
  "fullName": "Nguyễn Văn A",
  "phone": "0901234567",
  "role": "CUSTOMER"
}
```

Role trả về là role hiện tại trong database. Phiên cũ bị thu hồi, bộ token mới có thời hạn theo cấu hình. Cả refresh token cũ lẫn access token gắn với phiên cũ đều mất hiệu lực.

Refresh token chỉ dùng thành công một lần, kể cả request đồng thời. Frontend nên cho các request cùng chờ một lần refresh rồi thử lại với cookie mới. Backend không tự refresh khi một API khác trả `401`.

**Refresh token không hợp lệ — `401 Unauthorized`:**

```http
HTTP/1.1 401 Unauthorized
Content-Type: application/json
Set-Cookie: access_token=; Path=/api; Max-Age=0; HttpOnly; SameSite=Lax
Set-Cookie: refresh_token=; Path=/api/auth; Max-Age=0; HttpOnly; SameSite=Lax

{
  "timestamp": "2026-09-30T14:00:00Z",
  "status": 401,
  "code": "UNAUTHORIZED",
  "message": "Invalid credentials or session",
  "path": "/api/auth/refresh",
  "errors": {}
}
```

## 6. Đăng xuất

| Mục | Nội dung |
| --- | --- |
| **Method + Endpoint** | `POST /api/auth/logout` |
| **Authentication** | **Public** — cookie access/refresh tùy chọn. Nên gửi cookie hiện tại để thu hồi phiên; cookie thiếu/sai/hết hạn vẫn không cản trở xóa cookie và trả `204`. |
| **Role được phép sử dụng** | Mọi role: `CUSTOMER`, `ORGANIZER`, `STAFF`, `ADMIN`. Không kiểm tra role; Guest cũng có thể gọi để dọn cookie. |
| **Path Parameters** | Không có |
| **Query Parameters** | Không có |

### Request Body

Không có. Gửi header CSRF và các cookie hiện có:

```http
POST /api/auth/logout HTTP/1.1
Host: localhost:8080
X-CSRF-Protection: 1
Cookie: access_token=<access-token>; refresh_token=<refresh-token>
```

### Response mẫu

**Thành công — `204 No Content`:**

```http
HTTP/1.1 204 No Content
Set-Cookie: access_token=; Path=/api; Max-Age=0; HttpOnly; SameSite=Lax
Set-Cookie: refresh_token=; Path=/api/auth; Max-Age=0; HttpOnly; SameSite=Lax
```

Không có response body. Backend thu hồi phiên xác định được từ refresh token và/hoặc `sid` của access token hợp lệ, đồng thời xóa hai cookie đúng path. Access token của phiên đã thu hồi bị từ chối ở request tiếp theo. Khi hai cookie cùng thuộc phiên hiện tại, các phiên khác không bị ảnh hưởng.

Gọi lặp lại vẫn trả `204` nếu header CSRF hợp lệ. Nếu không gửi token nào, server chỉ yêu cầu xóa cookie, không xác định được phiên trong database để thu hồi.

**Thiếu header CSRF — `403 Forbidden`:**

```json
{
  "timestamp": "2026-09-30T14:00:00Z",
  "status": 403,
  "code": "FORBIDDEN",
  "message": "Access denied or missing CSRF protection header",
  "path": "/api/auth/logout",
  "errors": {}
}
```

## 7. Tạo sự kiện

`POST /api/events` — yêu cầu cookie `access_token` hợp lệ của tài khoản `ORGANIZER` đang `ACTIVE`, header `X-CSRF-Protection: 1` và `multipart/form-data`. Frontend gửi một `FormData` gồm part `event` (JSON, Content-Type `application/json`) và các file ảnh theo mục 9, dùng cookie với `credentials: "include"` hoặc Axios `withCredentials: true`. Để trình duyệt tự đặt Content-Type/boundary. API không nhận body JSON riêng hoặc URL ảnh có sẵn.

Theo UC19, frontend gom dữ liệu các bước của form và gửi một request khi Submit. Backend tạo `venues`, `events`, `ticket_types`, `event_guests` trong cùng transaction; nếu một bước thất bại thì rollback toàn bộ. `categoryId` tham chiếu danh mục có sẵn, không tạo hoặc sửa danh mục.

### JSON trong part `event`

| Field | Bắt buộc | Quy tắc |
| --- | --- | --- |
| `name` | Có | Không trống, tối đa 255 ký tự; trim khi lưu. |
| `description` | Không | Tối đa 255 ký tự theo schema hiện tại. |
| `startTime`, `endTime` | Có | ISO local datetime **theo UTC**, ví dụ `2030-10-20T12:00:00`. Bắt đầu phải ở tương lai; kết thúc phải sau bắt đầu. Frontend đổi giờ địa phương sang UTC trước khi gửi. |
| `categoryId` | Có | Số nguyên dương, danh mục phải tồn tại. |
| `venue` | Có | `city`, `address` không trống, tối đa 255 ký tự; `capacity` là số nguyên dương. Tạo địa điểm mới cho sự kiện. |
| `ticketTypes` | Có | Ít nhất một loại vé; không chứa phần tử null. Tổng `quantity` không vượt `venue.capacity`. |
| `guests` | Không | Có thể bỏ qua, null hoặc `[]`; nếu có thì từng phần tử phải hợp lệ, không null. |

Mỗi phần tử `ticketTypes` gồm:

- `name`: bắt buộc, không trống, tối đa 255 ký tự; `description` tùy chọn, tối đa 255 ký tự. Ảnh vé gửi bằng part `ticketImage{index}` riêng, bắt buộc cho mỗi loại vé.
- `price`: bắt buộc, không âm (0 cho vé miễn phí), tối đa 17 chữ số nguyên và 2 chữ số thập phân, dùng VND theo use case của dự án.
- `quantity`: số nguyên dương.
- `saleStartTime`, `saleEndTime`: bắt buộc, cùng định dạng UTC với sự kiện. Kết thúc bán không trước bắt đầu bán; cả hai phải **trước** `startTime` của sự kiện.

Mỗi phần tử `guests` gồm `name`, `role` bắt buộc, không trống, tối đa 50 ký tự; `description` tùy chọn, tối đa 255 ký tự. Ảnh khách mời tùy chọn, gửi bằng part `guestImage{index}` riêng.

```json
{
  "name": "Đêm nhạc Acoustic Thu",
  "description": "Chương trình âm nhạc acoustic",
  "startTime": "2030-10-20T12:00:00",
  "endTime": "2030-10-20T15:00:00",
  "categoryId": 1,
  "venue": {
    "city": "Hà Nội",
    "address": "123 Nguyễn Trãi, Thanh Xuân",
    "capacity": 500
  },
  "ticketTypes": [
    {
      "name": "Vé VIP",
      "description": "Khu vực gần sân khấu",
      "price": 1500000,
      "quantity": 100,
      "saleStartTime": "2030-10-01T01:00:00",
      "saleEndTime": "2030-10-19T16:59:00"
    }
  ],
  "guests": [
    {
      "name": "Nguyễn Văn A",
      "role": "Ca sĩ chính",
      "description": "Ca sĩ acoustic"
    }
  ]
}
```

### Response và trạng thái

Thành công trả `201 Created` với `EventResponse`: thông tin sự kiện như request, thêm `id`, `organizerId`, `status`, `createdAt` và URL Cloudinary `thumbnailImageUrl`, `bannerImageUrl`, `imageZoneUrl`; `venue`, từng loại vé và khách mời có `id` đã lưu. Loại vé trả thêm `imageUrl`, `reservedQuantity`, `remainingQuantity`, `status`; khách mời trả thêm `imageUrl`. Ảnh tùy chọn không gửi có URL null. `guests` luôn là một mảng, kể cả khi không có khách mời.

Backend lấy Organizer từ phiên đăng nhập, tự gán `status = PENDING_APPROVAL`. Các trường duyệt/hủy giữ null. Loại vé có `reservedQuantity = 0`, `remainingQuantity = quantity`, `status = INACTIVE`. Client không điều khiển Organizer, trạng thái, người duyệt hoặc số lượng vé giữ chỗ/còn lại qua request này.

| Giá trị `events.status` | Ý nghĩa |
| --- | --- |
| `PENDING_APPROVAL` | Chờ duyệt |
| `APPROVED` | Đã duyệt |
| `ONGOING` | Đang diễn ra |
| `COMPLETED` | Đã kết thúc |
| `PENDING_CANCELLATION` | Chờ hủy |
| `CANCELED` | Đã hủy |

Sự kiện chờ duyệt được lưu trong DB để chức năng Admin truy vấn sau. Sau khi lưu dữ liệu sự kiện, backend tìm Admin ACTIVE và tạo một bản ghi `notifications` trong cùng transaction: `users_id` là ID của Admin, `title = Có sự kiện mới chờ duyệt`, `type = EVENT_PENDING_APPROVAL`, `is_read = false`, nội dung chứa tên sự kiện. Tên dài được rút gọn trong thông báo để giữ nội dung trong giới hạn 255 ký tự của DB. Thông báo chỉ để hiển thị. Nếu chưa có Admin ACTIVE, sự kiện vẫn được tạo; nếu lưu thông báo thất bại, toàn bộ transaction rollback. Với giả định hệ thống có một Admin, truy vấn lấy một Admin ACTIVE theo ID tăng dần.

API duyệt, chuyển trạng thái theo thời gian, hủy, xem/đánh dấu đã đọc thông báo và mở bán vé sẽ được triển khai ở các tính năng tương ứng.

| HTTP status | Trường hợp |
| --- | --- |
| `400 Bad Request` | Thiếu/sai dữ liệu, thời gian không hợp lệ, tổng số vé vượt sức chứa. Lỗi annotation trả `VALIDATION_ERROR` và field cụ thể, kể cả field lồng nhau; lỗi nghiệp vụ trả `REQUEST_ERROR`. |
| `401 Unauthorized` | Thiếu hoặc sai phiên, phiên hết hạn/thu hồi, tài khoản không ACTIVE. |
| `403 Forbidden` | Role khác ORGANIZER hoặc thiếu header CSRF. |
| `404 Not Found` | `categoryId` không tồn tại. |
| `409 Conflict` | Vi phạm ràng buộc DB khi lưu; toàn bộ thao tác được rollback. |
| `415 Unsupported Media Type` | Gửi body JSON riêng thay vì multipart hoặc sai Content-Type của part `event`. |

## 8. Danh mục dùng trong form tạo sự kiện

`GET /api/categories` yêu cầu phiên đăng nhập hợp lệ, trả danh sách danh mục hiện có sắp xếp theo tên. Mỗi phần tử gồm `id`, `name`, `description`; danh sách trống trả `[]`. API chỉ đọc dữ liệu để Organizer chọn `categoryId`, không tạo/sửa danh mục.

## 9. Ảnh dùng trong form tạo sự kiện

Frontend dùng `POST /api/events` trong `EventController` với multipart, yêu cầu phiên ORGANIZER và `X-CSRF-Protection: 1`. Không tự đặt Content-Type/boundary ở frontend. Các part:

| Part | Nội dung |
| --- | --- |
| `event` | JSON `CreateEventRequest` (Content-Type `application/json`), chỉ gồm thông tin ở mục 7; không có trường URL ảnh. |
| `bannerImage`, `thumbnailImage` | Hai file ảnh sự kiện bắt buộc. |
| `imageZone` | File sơ đồ khu vực, tùy chọn. |
| `ticketImage0`, `ticketImage1`, ... | File ảnh bắt buộc cho mỗi loại vé, chỉ số theo thứ tự `ticketTypes`. |
| `guestImage0`, `guestImage1`, ... | File ảnh tùy chọn cho mỗi khách mời, chỉ số theo thứ tự `guests`. |

Backend kiểm tra ảnh PNG/JPEG hợp lệ (mỗi file tối đa 5 MB và 20 megapixel; tổng request tối đa 50 MB), kiểm tra nghiệp vụ rồi lưu/flush hồ sơ trong transaction. Sau đó mới upload tới Cloudinary (`image/upload`, folder `eventhub/events`) và cập nhật URL trước khi commit. Không upload khi chọn ảnh, lưu nháp hoặc khi dữ liệu không hợp lệ. Không gửi API key/secret cho frontend.

Thành công trả `201 Created` với `EventResponse` như mục 7, bao gồm các trường URL HTTPS do Cloudinary trả về. DB chỉ lưu URL; ảnh nằm trên Cloudinary. Chỉ có một API tạo sự kiện multipart, không có endpoint upload ảnh riêng. Thiếu ảnh bắt buộc hoặc gửi tên part ảnh không khớp dữ liệu vé/khách mời trả `400` trước khi upload.

File sai/trống/vượt 20 megapixel trả `400`; multipart vượt giới hạn trả `413`; thiếu cấu hình Cloudinary trả `503`; lỗi Cloudinary/kết nối/response URL không hợp lệ trả `502` với thông báo chung, không lộ nội dung lỗi provider. Kết nối có timeout 5 giây, đọc response 30 giây và không tự retry upload.

Cần cấu hình `CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_API_KEY`, `CLOUDINARY_API_SECRET` trên backend theo README. Mỗi ảnh dùng public ID UUID riêng, không ghi đè ảnh cũ. Upload/lưu URL thất bại thì rollback toàn bộ DB và gọi Cloudinary destroy cho các ID đã thử upload (kể cả request timeout). Nếu destroy thất bại, ghi log ID để xử lý, giữ nguyên lỗi ban đầu; không thể đảm bảo atomicity giữa hai dịch vụ khi kết nối/provider gặp sự cố. Hủy bản nháp chưa gửi không tạo ảnh trên Cloudinary. Không lưu ảnh lâu dài trên ổ đĩa backend và không có API đọc ảnh local.

