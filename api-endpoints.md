# Tài liệu API Endpoint — EventHub

Base URL local: `http://localhost:8080`.

Tài liệu mô tả các endpoint xác thực, Organizer, **11 API Admin UC31–36** (mục 11) và **API thông báo** (mục 12), gồm request/response, quyền truy cập, validation và status code.

### Headers và cookie

Các request thay đổi dữ liệu (POST/PUT/PATCH/DELETE) bắt buộc có header:

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
| UC06 | Xem danh sách sự kiện | `GET /api/public/events` | Public | `200 OK` |
| UC07 | Tìm kiếm sự kiện | `GET /api/public/events` | Public | `200 OK` |
| UC08 | Lọc sự kiện | `GET /api/public/events` | Public | `200 OK` |
| UC09 | Xem chi tiết sự kiện | `GET /api/public/events/{eventId}` | Public | `200 OK` |
| Hỗ trợ UC08, UC19 | Lấy danh sách danh mục | `GET /api/categories` | Public | `200 OK` |
| UC31 | Danh sách người dùng | `GET /api/admin/users` | Required — access token, role `ADMIN` | `200 OK` |
| UC31 | Tạo người dùng | `POST /api/admin/users` | Required — access token, role `ADMIN` | `201 Created` |
| UC31 | Khóa/mở người dùng | `PATCH /api/admin/users/{userId}/status` | Required — access token, role `ADMIN` | `200 OK` |
| UC32 | Danh sách danh mục | `GET /api/admin/event-categories` | Required — access token, role `ADMIN` | `200 OK` |
| UC32 | Thêm danh mục | `POST /api/admin/event-categories` | Required — access token, role `ADMIN` | `201 Created` |
| UC32 | Sửa danh mục | `PUT /api/admin/event-categories/{categoryId}` | Required — access token, role `ADMIN` | `200 OK` |
| UC32 | Xóa danh mục | `DELETE /api/admin/event-categories/{categoryId}` | Required — access token, role `ADMIN` | `204 No Content` |
| UC33 | Danh sách sự kiện chờ duyệt | `GET /api/admin/events/pending` | Required — access token, role `ADMIN` | `200 OK` |
| UC34 | Chi tiết hồ sơ xét duyệt | `GET /api/admin/events/pending/{eventId}` | Required — access token, role `ADMIN` | `200 OK` |
| UC35 | Duyệt sự kiện | `POST /api/admin/events/{eventId}/approve` | Required — access token, role `ADMIN` | `200 OK` |
| UC36 | Từ chối sự kiện | `POST /api/admin/events/{eventId}/reject` | Required — access token, role `ADMIN` | `200 OK` |
| Hỗ trợ UC35–36 | Organizer đọc thông báo | `GET /api/notifications` | Required — access token, role `ORGANIZER` | `200 OK` |
| UC28 | Xem danh sách sự kiện được phân công | `GET /api/staff/events` | Required — access token, role `STAFF` | `200 OK` |
| UC29 | Xem chi tiết sự kiện được phân công | `GET /api/staff/events/{eventId}` | Required — access token, role `STAFF` | `200 OK` |
| Hỗ trợ UC29 | Danh sách khách hàng đã đặt vé | `GET /api/staff/events/{eventId}/customers` | Required — access token, role `STAFF` | `200 OK` |
| Hỗ trợ UC29 | Danh sách loại vé sự kiện | `GET /api/staff/events/{eventId}/ticket-types` | Required — access token, role `STAFF` | `200 OK` |
| Hỗ trợ UC29 | Thống kê vé sự kiện | `GET /api/staff/events/{eventId}/stats` | Required — access token, role `STAFF` | `200 OK` |
| Hỗ trợ UC29 | Danh sách vé và bộ lọc soát vé | `GET /api/staff/events/{eventId}/tickets` | Required — access token, role `STAFF` | `200 OK` |
| Hỗ trợ UC29 | Soát vé / Check-in QR (Mobile) | `POST /api/staff/events/{eventId}/check-in` | Required — access token, role `STAFF` | `200 OK` |

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
| `REJECTED` | Bị từ chối, lý do trong rejectReason |
| `ONGOING` | Đang diễn ra |
| `COMPLETED` | Đã kết thúc |
| `PENDING_CANCELLATION` | Chờ hủy |
| `CANCELED` | Đã hủy |

Sự kiện chờ duyệt được lưu trong DB để chức năng Admin truy vấn sau. Sau khi lưu dữ liệu sự kiện, backend tìm Admin ACTIVE và tạo một bản ghi `notifications` trong cùng transaction: `users_id` là ID của Admin, `title = Có sự kiện mới chờ duyệt`, `type = EVENT_PENDING_APPROVAL`, `is_read = false`, nội dung chứa tên sự kiện. Tên dài được rút gọn trong thông báo để giữ nội dung trong giới hạn 255 ký tự của DB. Thông báo chỉ để hiển thị. Nếu chưa có Admin ACTIVE, sự kiện vẫn được tạo; nếu lưu thông báo thất bại, toàn bộ transaction rollback. Với giả định hệ thống có một Admin, truy vấn lấy một Admin ACTIVE theo ID tăng dần.

API Organizer gửi yêu cầu hủy được mô tả ở mục 10. API Admin duyệt/từ chối và cập nhật trạng thái vé được mô tả ở mục 11; Organizer đọc thông báo tại mục 12. Chuyển trạng thái sự kiện theo thời gian, Admin đọc thông báo, đánh dấu đã đọc và nghiệp vụ bán vé thuộc các tính năng tương ứng.

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

## 10. Organizer xem và quản lý sự kiện

Các endpoint dưới đây chỉ dành cho ORGANIZER ACTIVE, lấy tài khoản từ cookie. Chỉ truy cập sự kiện của chính Organizer; ID không tồn tại hoặc thuộc tài khoản khác đều trả `404`. Request ghi dữ liệu cần `X-CSRF-Protection: 1`. Tất cả thời gian API là UTC không offset; FE nhập/hiển thị GMT+7.

| Method và endpoint | Hành vi |
| --- | --- |
| `GET /api/events/mine` | Danh sách riêng của Organizer, tìm theo tên và lọc trạng thái, mới tạo trước. |
| `GET /api/events/{eventId}` | Trả `EventResponse`, gồm địa điểm, danh mục, loại vé, khách mời và ảnh đã lưu. |
| `PUT /api/events/{eventId}` | Cập nhật toàn bộ hồ sơ bằng multipart, gửi Admin duyệt lại. |
| `POST /api/events/{eventId}/cancel` | Gửi yêu cầu hủy cho Admin bằng JSON `{ "reason": "Lý do hủy" }`. |

### Danh sách và chi tiết

Query danh sách: `page` bắt đầu từ 0 (mặc định 0), `size` từ 1–50 (mặc định 9), `search` tối đa 255 ký tự (không phân biệt hoa/thường; tìm chuỗi trong tên), `status` tùy chọn dùng đúng enum ở mục 7. Response gồm `content`, `page`, `size`, `totalElements`, `totalPages`, `statusCounts`. Mỗi phần tử `content` gồm `id`, `name`, `description`, `thumbnailImageUrl`, `categoryName`, `city`, `address`, `startTime`, `endTime`, `status`, `canEdit`, `canCancel`. `statusCounts` đếm toàn bộ sự kiện của tài khoản theo từng trạng thái, không phụ thuộc bộ lọc. Danh sách trống trả `content: []`, `totalElements: 0`, `totalPages: 0`.

`EventResponse` dùng cho tạo/chi tiết/sửa/hủy trả thêm `categoryName`, `cancelReason`, `canceledAt`, `canEdit`, `canCancel`, `rejectReason`. FE dùng hai cờ quyền thao tác để hiển thị nút; BE luôn kiểm tra lại trạng thái và thời gian trong transaction. Số vé đã bán = `quantity - remainingQuantity - reservedQuantity`, không tính vé đang giữ chỗ vào vé đã bán. REJECTED không được sửa/gửi lại trong phạm vi hiện tại.

### Sửa và gửi duyệt lại (UC20)

- Part `event` dùng các trường như request tạo ở mục 7. Mỗi vé/khách mời có thêm `id` khi sửa bản ghi hiện có; bỏ `id` hoặc gửi null khi thêm mới. ID phải thuộc chính sự kiện, không được trùng trong một mảng. Bản ghi cũ không còn trong mảng sẽ được xóa. Phải có ít nhất một loại vé; `guests` có thể trống/null/bỏ qua để xóa toàn bộ khách mời.
- Ảnh hiện có được giữ khi không gửi file thay thế. Không nhận URL ảnh từ client để cập nhật. Vé mới bắt buộc có `ticketImage{index}`; ảnh khách mời mới tùy chọn. Chỉ số file luôn theo thứ tự mảng JSON sau chỉnh sửa. Ảnh bìa/thumbnail/vé đã lưu không được xóa, có thể thay bằng file mới. `removeImageZone: true` xóa sơ đồ; `guests[i].removeImage: true` xóa ảnh khách mời. Các cờ có thể bỏ qua/false để giữ ảnh; không vừa xóa vừa gửi file thay thế cùng một ảnh.
- Chỉ sửa `PENDING_APPROVAL` hoặc `APPROVED` trước `startTime`. Validation thời gian, sức chứa, giá và ảnh giữ như tạo sự kiện. Sau sửa, trạng thái là `PENDING_APPROVAL`, xóa thông tin duyệt cũ, tất cả vé `INACTIVE`, tạo thông báo `EVENT_PENDING_APPROVAL` cho Admin ACTIVE.
- Giữ ID, số vé đã bán và đang giữ chỗ của vé hiện có. Khi đổi tổng số lượng, `remainingQuantity` thay đổi theo chênh lệch; không được giảm tổng dưới số vé đã bán + giữ chỗ. Không xóa loại vé đã bán, giữ chỗ hoặc còn được `booking_items` tham chiếu. Giá trong booking cũ giữ theo `unit_price` đã lưu.
- Khóa sự kiện và các loại vé khi sửa/hủy. Validation dữ liệu/ảnh hoàn tất trước upload. Upload hoặc lưu DB thất bại thì rollback toàn bộ hồ sơ và dọn ảnh mới đã thử upload như luồng tạo; không gửi file mới thì không cần gọi Cloudinary. Địa điểm dùng chung không bị sửa theo sự kiện này.
- Thành công trả `200` và hồ sơ đã lưu. `400` khi dữ liệu, ID con hoặc file không hợp lệ; `409` khi trạng thái không cho sửa, số lượng vé hoặc liên kết booking không cho phép thay đổi. Các lỗi ảnh giữ HTTP status như mục 9.

### Gửi yêu cầu hủy (UC23)

`reason` bắt buộc, không chỉ chứa khoảng trắng, tối đa 255 ký tự. Cho phép yêu cầu hủy sự kiện `PENDING_APPROVAL`, `APPROVED` hoặc `ONGOING` khi chưa qua `endTime`. Backend lưu lý do, chuyển sang `PENDING_CANCELLATION`, tạm ngừng bán các loại vé (`INACTIVE`), tạo thông báo `EVENT_PENDING_CANCELLATION` cho Admin ACTIVE. `canceledAt` giữ null vì Admin chưa duyệt hủy. Thành công trả `200` với `EventResponse`; yêu cầu lặp hoặc trạng thái kết thúc/đã hủy/chờ hủy trả `409`. FE không thể sửa hoặc tiếp tục gửi yêu cầu hủy khi đang chờ Admin.

Không xóa sự kiện, booking hoặc vé; không đánh dấu `CANCELED`, hoàn tiền hay triển khai thao tác duyệt của Admin trong endpoint này.

## 11. Xem danh sách sự kiện

| Mục | Nội dung |
| --- | --- |
| **Use Case liên quan** | UC06 (Xem danh sách sự kiện) |
| **Tên API** | Xem danh sách sự kiện |
| **Method + Endpoint** | `GET /api/public/events` |
| **Authentication** | **Public** |
| **Role được phép sử dụng** | Guest, Customer, Staff, Organizer, Admin |
| **Path Parameters** | Không có |
| **Query Parameters** | `page` (integer, mặc định 0): Trang hiện tại.<br>`size` (integer, mặc định 9): Số phần tử trên mỗi trang. |
| **Request Body** | Không có |

### Response mẫu
<a id="admin-api"></a>

## 11. API quản trị UC31–36

11 endpoint trong mục này chỉ dành cho tài khoản `ADMIN` đang `ACTIVE`. API thông báo dành cho Organizer nằm ở mục 12. Hợp đồng dưới đây phản ánh code hiện tại.

### Quy ước chung

- Xác thực bằng cookie HttpOnly hiện có. Trình duyệt gửi `credentials: "include"` hoặc Axios `withCredentials: true`; frontend dùng HTTP client chung để xử lý refresh.
- Request POST/PUT/PATCH/DELETE bắt buộc có `X-CSRF-Protection: 1`. Request có body gửi JSON với `Content-Type: application/json`. GET không bắt buộc header CSRF.
- Admin, người nhận thông báo và thời gian quyết định lấy từ phiên/server; client không gửi `adminId`, `reviewedBy` hoặc `reviewedAt`. Response không chứa mật khẩu/hash; trường số điện thoại là `phone`.
- Danh sách: `page` mặc định 0 và không âm; `pageSize` mặc định 20, từ 1–100; sắp xếp ID giảm dần. Response gồm `items`, `page`, `pageSize`, `totalElements`, `totalPages`. Trang không có kết quả trả `items: []`; tập kết quả rỗng có `totalElements: 0`, `totalPages: 0`.
- Thời gian nghiệp vụ là chuỗi UTC không offset, ví dụ `2026-10-07T10:00:00`; FE hiển thị GMT+7. Tiền là VND, backend dùng BigDecimal.
- Lỗi validation, xác thực, phân quyền và nghiệp vụ được xử lý có cấu trúc `{timestamp,status,code,message,path,errors}`. Validation có lỗi theo trường trong `errors`; timestamp lỗi là Instant UTC có hậu tố `Z`. Lỗi `500` ngoài dự kiến chưa được chuẩn hóa về cấu trúc này.
- `400`: dữ liệu/tham số sai; `401`: phiên không hợp lệ hoặc tài khoản bị khóa; `403`: sai role hoặc thiếu/sai CSRF; `404`/`409`: theo từng endpoint. CSRF được kiểm tra trước xác thực nên request ghi thiếu header có thể trả `403` dù chưa đăng nhập.
- FK/unique index bảo vệ cả request đồng thời. Xung đột ràng buộc DB chưa được ánh xạ riêng trả `409 DATA_CONFLICT`.

Ví dụ lỗi nghiệp vụ:

```json
{
  "timestamp": "2026-10-07T10:00:00Z",
  "status": 409,
  "code": "CATEGORY_IN_USE",
  "message": "Danh mục đang được sử dụng",
  "path": "/api/admin/event-categories/3",
  "errors": {}
}
```

Ví dụ lỗi validation khi thiếu lý do từ chối:

```json
{
  "timestamp": "2026-10-07T10:00:00Z",
  "status": 400,
  "code": "VALIDATION_ERROR",
  "message": "Request validation failed",
  "path": "/api/admin/events/42/reject",
  "errors": {
    "reason": "must not be blank"
  }
}
```

### 11.1. Danh sách người dùng (UC31)

| Mục | Nội dung |
| --- | --- |
| **Method + Endpoint** | `GET /api/admin/users` |
| **Authentication** | **Required** — cookie `access_token` hợp lệ, tài khoản đang `ACTIVE`. |
| **Role được phép sử dụng** | `ADMIN` |
| **Path Parameters** | Không có. |
| **Query Parameters** | `keyword` (string, tùy chọn, mặc định chuỗi rỗng): tìm tên/email không phân biệt hoa thường, trim và tìm chuỗi literal; `role` (tùy chọn): ADMIN/CUSTOMER/ORGANIZER/STAFF. `page` (integer, tùy chọn, mặc định 0, không âm), `pageSize` (integer, tùy chọn, mặc định 20, từ 1–100). |

#### Request Body

Không có.

#### Response mẫu

**Thành công — `200 OK`:**

```json
{
  "content": [
    {
      "id": 1,
      "name": "Anh trai say hi",
      "thumbnailImageUrl": "https://picsum.photos/400/300",
      "startTime": "2026-09-24T06:00:00",
      "endTime": "2026-09-24T10:00:00",
      "city": "Hà Nội",
      "startingPrice": 300000.00
    }
  ],
  "page": 0,
  "size": 9,
  "items": [
    {
      "id": 12,
      "fullName": "Người dùng mẫu",
      "email": "member@example.invalid",
      "phone": "0900000000",
      "role": "CUSTOMER",
      "status": "ACTIVE"
    }
  ],
  "page": 0,
  "pageSize": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

**Trạng thái/Error chính:**
- `200 OK`: Danh sách trả về chỉ bao gồm các sự kiện có trạng thái `APPROVED` hoặc `ONGOING`. Nếu không có, mảng `content` rỗng.

## 12. Tìm kiếm sự kiện

| Mục | Nội dung |
| --- | --- |
| **Use Case liên quan** | UC07 (Tìm kiếm sự kiện) |
| **Tên API** | Tìm kiếm sự kiện |
| **Method + Endpoint** | `GET /api/public/events` |
| **Authentication** | **Public** |
| **Role được phép sử dụng** | Guest, Customer, Staff, Organizer, Admin |
| **Path Parameters** | Không có |
| **Query Parameters** | `search` (string, bắt buộc): Từ khóa tìm kiếm theo tên sự kiện.<br>`page` (integer, mặc định 0): Trang hiện tại.<br>`size` (integer, mặc định 9): Số phần tử trên mỗi trang. |
| **Request Body** | Không có |

*(Response tương tự như mục 11)*

## 13. Lọc sự kiện

| Mục | Nội dung |
| --- | --- |
| **Use Case liên quan** | UC08 (Lọc sự kiện) |
| **Tên API** | Lọc sự kiện |
| **Method + Endpoint** | `GET /api/public/events` |
| **Authentication** | **Public** |
| **Role được phép sử dụng** | Guest, Customer, Staff, Organizer, Admin |
| **Path Parameters** | Không có |
| **Query Parameters** | `fromDate` (datetime ISO, tùy chọn): Thời gian bắt đầu.<br>`toDate` (datetime ISO, tùy chọn): Thời gian kết thúc.<br>`categoryId` (integer, tùy chọn): ID của danh mục.<br>`city` (string, tùy chọn): Tên thành phố.<br>`page` (integer, mặc định 0): Trang hiện tại.<br>`size` (integer, mặc định 9): Số phần tử trên trang. |
| **Request Body** | Không có |

*(Response tương tự như mục 11)*

## 14. Xem chi tiết sự kiện

| Mục | Nội dung |
| --- | --- |
| **Use Case liên quan** | UC09 |
| **Tên API** | Xem chi tiết sự kiện |
| **Method + Endpoint** | `GET /api/public/events/{eventId}` |
| **Authentication** | **Public** |
| **Role được phép sử dụng** | Guest, Customer, Staff, Organizer, Admin |
| **Path Parameters** | `eventId` (integer): ID của sự kiện. |
| **Query Parameters** | Không có |
| **Request Body** | Không có |

### Response mẫu
Không có kết quả trả `200` với `items: []`. Kết quả sắp xếp ID giảm dần.

#### Mã lỗi

| HTTP status | Code | Trường hợp |
| --- | --- | --- |
| `400` | `INVALID_PAGE` / `INVALID_REQUEST` | Phân trang không hợp lệ hoặc role sai. |
| `401` | `UNAUTHORIZED` | Thiếu/sai phiên, phiên hết hạn/thu hồi hoặc tài khoản bị khóa. |
| `403` | `FORBIDDEN` | Sai role. |

Các lỗi ngoài dự kiến có thể trả `500`; xem quy ước lỗi ở mục 11.

### 11.2. Tạo người dùng (UC31)

| Mục | Nội dung |
| --- | --- |
| **Method + Endpoint** | `POST /api/admin/users` |
| **Authentication** | **Required** — cookie `access_token` hợp lệ, tài khoản đang `ACTIVE`. |
| **Role được phép sử dụng** | `ADMIN` |
| **Path Parameters** | Không có. |
| **Query Parameters** | Không có. |

#### Request Body

JSON với `Content-Type: application/json` và `X-CSRF-Protection: 1`.

| Field | Kiểu | Quy tắc |
| --- | --- | --- |
| `fullName` | string | Không rỗng/toàn khoảng trắng, trim, tối đa 255 ký tự. |
| `email` | string | Đúng định dạng email, trim và lowercase, tối đa 255 ký tự; duy nhất sau chuẩn hóa. |
| `phone` | string | Không rỗng/toàn khoảng trắng, trim, tối đa 20 ký tự. |
| `password` | string | Không rỗng/toàn khoảng trắng, 8–72 ký tự và tối đa 72 byte UTF-8; không trim. |
| `role` | string | Chỉ CUSTOMER hoặc ORGANIZER. |

```json
{
  "fullName": "Người dùng mẫu",
  "email": "member@example.invalid",
  "phone": "0900000000",
  "password": "<mật khẩu hợp lệ>",
  "role": "CUSTOMER"
}
```

#### Response mẫu

**Thành công — `201 Created`:**

```json
{
  "id": 12,
  "fullName": "Người dùng mẫu",
  "email": "member@example.invalid",
  "phone": "0900000000",
  "role": "CUSTOMER",
  "status": "ACTIVE"
}
```

Tất cả trường đều bắt buộc. Tài khoản mới có trạng thái `ACTIVE`; mật khẩu được băm bằng BCrypt chung của auth và không được trả trong response. STAFF được quản lý qua nghiệp vụ phân công; API này không tạo ADMIN/STAFF.

#### Mã lỗi

| HTTP status | Code | Trường hợp |
| --- | --- | --- |
| `400` | `VALIDATION_ERROR` / `INVALID_REQUEST` | Thiếu/sai dữ liệu, role không được phép hoặc mật khẩu không hợp lệ. |
| `409` | `EMAIL_ALREADY_EXISTS` | Email đã tồn tại, kể cả khi tạo đồng thời. |
| `401` | `UNAUTHORIZED` | Thiếu/sai phiên, phiên hết hạn/thu hồi hoặc tài khoản bị khóa. |
| `403` | `FORBIDDEN` | Sai role hoặc thiếu/sai header CSRF. |

Các lỗi ngoài dự kiến có thể trả `500`; xem quy ước lỗi ở mục 11.

### 11.3. Khóa/mở người dùng (UC31)

| Mục | Nội dung |
| --- | --- |
| **Method + Endpoint** | `PATCH /api/admin/users/{userId}/status` |
| **Authentication** | **Required** — cookie `access_token` hợp lệ, tài khoản đang `ACTIVE`. |
| **Role được phép sử dụng** | `ADMIN` |
| **Path Parameters** | `userId`: integer dương, ID người dùng. |
| **Query Parameters** | Không có. |

#### Request Body

JSON với `Content-Type: application/json` và `X-CSRF-Protection: 1`.

| Field | Kiểu | Quy tắc |
| --- | --- | --- |
| `status` | string | Bắt buộc; ACTIVE hoặc LOCKED. |

```json
{
  "status": "LOCKED"
}
```

#### Response mẫu

**Thành công — `200 OK`:**

```json
{
  "id": 12,
  "fullName": "Người dùng mẫu",
  "email": "member@example.invalid",
  "phone": "0900000000",
  "role": "CUSTOMER",
  "status": "LOCKED"
}
```

Gửi `{"status":"ACTIVE"}` để mở khóa. Đặt lại cùng trạng thái vẫn thành công. Không được tự khóa tài khoản đang đăng nhập. Auth đọc trạng thái DB trên mỗi request và khi refresh nên tài khoản bị khóa không dùng được access/refresh còn hạn. Sau mở khóa, phiên còn hiệu lực có thể xác thực lại; nếu cookie đã bị xóa sau refresh thất bại thì cần đăng nhập lại.

#### Mã lỗi

| HTTP status | Code | Trường hợp |
| --- | --- | --- |
| `400` | `INVALID_ID` / `VALIDATION_ERROR` / `INVALID_REQUEST` | ID hoặc status không hợp lệ. |
| `404` | `USER_NOT_FOUND` | Người dùng không tồn tại. |
| `409` | `CANNOT_LOCK_SELF` | Admin tự khóa tài khoản đang đăng nhập. |
| `401` | `UNAUTHORIZED` | Thiếu/sai phiên, phiên hết hạn/thu hồi hoặc tài khoản bị khóa. |
| `403` | `FORBIDDEN` | Sai role hoặc thiếu/sai header CSRF. |

Các lỗi ngoài dự kiến có thể trả `500`; xem quy ước lỗi ở mục 11.

### 11.4. Danh sách danh mục (UC32)

| Mục | Nội dung |
| --- | --- |
| **Method + Endpoint** | `GET /api/admin/event-categories` |
| **Authentication** | **Required** — cookie `access_token` hợp lệ, tài khoản đang `ACTIVE`. |
| **Role được phép sử dụng** | `ADMIN` |
| **Path Parameters** | Không có. |
| **Query Parameters** | `page` (integer, tùy chọn, mặc định 0, không âm), `pageSize` (integer, tùy chọn, mặc định 20, từ 1–100). |

#### Request Body

Không có.

#### Response mẫu

**Thành công — `200 OK`:**

```json
{
  "id": 1,
  "name": "Anh trai say hi",
  "description": "Show âm nhạc bùng nổ",
  "thumbnailImageUrl": "https://picsum.photos/400/300",
  "bannerImageUrl": "https://picsum.photos/1200/400",
  "imageZoneUrl": "https://picsum.photos/800/600",
  "startTime": "2026-09-24T06:00:00",
  "endTime": "2026-09-24T10:00:00",
  "organizerName": "VieON Channel",
  "categoryName": "Âm nhạc",
  "venue": {
    "city": "Hà Nội",
    "address": "Sân Vận Động Quốc Gia Mỹ Đình"
  },
  "startingPrice": 300000.00,
  "suggestedEvents": [
    {
      "id": 2,
      "name": "LOALOA SHOW",
      "thumbnailImageUrl": "https://picsum.photos/400/300",
      "startTime": "2026-09-26T08:00:00",
      "endTime": "2026-09-26T12:00:00",
      "city": "Hà Nội",
      "startingPrice": 300000.00
    }
  ]
}
```

**Trạng thái/Error chính:**
- `404 Not Found`: Sự kiện không tồn tại hoặc chưa được duyệt (`PENDING_APPROVAL`, `CANCELED`,...).

## 15. Lấy danh sách danh mục (Cập nhật cho Public)

| Mục | Nội dung |
| --- | --- |
| **Use Case liên quan** | Hỗ trợ UC08 (Lọc sự kiện), UC19 (Tạo sự kiện) |
| **Tên API** | Lấy danh sách danh mục |
| **Method + Endpoint** | `GET /api/categories` |
| **Authentication** | **Public** |
| **Role được phép sử dụng** | Guest, Customer, Staff, Organizer, Admin |
| **Path Parameters** | Không có |
| **Query Parameters** | Không có |
| **Request Body** | Không có |
  "items": [
    {
      "id": 3,
      "name": "Âm nhạc",
      "description": "Biểu diễn âm nhạc"
    }
  ],
  "page": 0,
  "pageSize": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

Danh sách rỗng trả `200` với `items: []`. API Organizer `GET /api/categories` tiếp tục đọc cùng bảng; endpoint admin trả phân trang và sắp xếp ID giảm dần.

#### Mã lỗi

| HTTP status | Code | Trường hợp |
| --- | --- | --- |
| `400` | `INVALID_PAGE` / `INVALID_REQUEST` | Phân trang không hợp lệ. |
| `401` | `UNAUTHORIZED` | Thiếu/sai phiên, phiên hết hạn/thu hồi hoặc tài khoản bị khóa. |
| `403` | `FORBIDDEN` | Sai role. |

Các lỗi ngoài dự kiến có thể trả `500`; xem quy ước lỗi ở mục 11.

### 11.5. Thêm danh mục (UC32)

| Mục | Nội dung |
| --- | --- |
| **Method + Endpoint** | `POST /api/admin/event-categories` |
| **Authentication** | **Required** — cookie `access_token` hợp lệ, tài khoản đang `ACTIVE`. |
| **Role được phép sử dụng** | `ADMIN` |
| **Path Parameters** | Không có. |
| **Query Parameters** | Không có. |

#### Request Body

JSON với `Content-Type: application/json` và `X-CSRF-Protection: 1`.

| Field | Kiểu | Quy tắc |
| --- | --- | --- |
| `name` | string | Bắt buộc, trim, không trắng, tối đa 255 ký tự; duy nhất theo LOWER(BTRIM(name)). |
| `description` | string | Bắt buộc, trim, không trắng, tối đa 255 ký tự. |

```json
{
  "name": "Âm nhạc",
  "description": "Biểu diễn âm nhạc"
}
```

#### Response mẫu

**Thành công — `201 Created`:**

```json
{
  "id": 3,
  "name": "Âm nhạc",
  "description": "Biểu diễn âm nhạc"
}
```

Unique index bảo vệ tên danh mục cả khi có request đồng thời.

#### Mã lỗi

| HTTP status | Code | Trường hợp |
| --- | --- | --- |
| `400` | `VALIDATION_ERROR` / `INVALID_REQUEST` | Tên hoặc mô tả không hợp lệ. |
| `409` | `CATEGORY_NAME_ALREADY_EXISTS` | Tên danh mục đã tồn tại sau chuẩn hóa. |
| `401` | `UNAUTHORIZED` | Thiếu/sai phiên, phiên hết hạn/thu hồi hoặc tài khoản bị khóa. |
| `403` | `FORBIDDEN` | Sai role hoặc thiếu/sai header CSRF. |

Các lỗi ngoài dự kiến có thể trả `500`; xem quy ước lỗi ở mục 11.

### 11.6. Sửa danh mục (UC32)

| Mục | Nội dung |
| --- | --- |
| **Method + Endpoint** | `PUT /api/admin/event-categories/{categoryId}` |
| **Authentication** | **Required** — cookie `access_token` hợp lệ, tài khoản đang `ACTIVE`. |
| **Role được phép sử dụng** | `ADMIN` |
| **Path Parameters** | `categoryId`: integer dương, ID danh mục. |
| **Query Parameters** | Không có. |

#### Request Body

JSON với `Content-Type: application/json` và `X-CSRF-Protection: 1`.

| Field | Kiểu | Quy tắc |
| --- | --- | --- |
| `name` | string | Bắt buộc, trim, không trắng, tối đa 255 ký tự; duy nhất theo LOWER(BTRIM(name)). |
| `description` | string | Bắt buộc, trim, không trắng, tối đa 255 ký tự. |

```json
{
  "name": "Âm nhạc",
  "description": "Biểu diễn âm nhạc cập nhật"
}
```

#### Response mẫu

**Thành công — `200 OK`:**

```json
{
  "id": 3,
  "name": "Âm nhạc",
  "description": "Biểu diễn âm nhạc cập nhật"
}
```

Gửi đầy đủ tên và mô tả. Giữ nguyên tên của chính bản ghi được phép.

#### Mã lỗi

| HTTP status | Code | Trường hợp |
| --- | --- | --- |
| `400` | `INVALID_ID` / `VALIDATION_ERROR` / `INVALID_REQUEST` | ID, tên hoặc mô tả không hợp lệ. |
| `404` | `CATEGORY_NOT_FOUND` | Danh mục không tồn tại. |
| `409` | `CATEGORY_NAME_ALREADY_EXISTS` | Tên trùng với danh mục khác. |
| `401` | `UNAUTHORIZED` | Thiếu/sai phiên, phiên hết hạn/thu hồi hoặc tài khoản bị khóa. |
| `403` | `FORBIDDEN` | Sai role hoặc thiếu/sai header CSRF. |

Các lỗi ngoài dự kiến có thể trả `500`; xem quy ước lỗi ở mục 11.

### 11.7. Xóa danh mục (UC32)

| Mục | Nội dung |
| --- | --- |
| **Method + Endpoint** | `DELETE /api/admin/event-categories/{categoryId}` |
| **Authentication** | **Required** — cookie `access_token` hợp lệ, tài khoản đang `ACTIVE`. |
| **Role được phép sử dụng** | `ADMIN` |
| **Path Parameters** | `categoryId`: integer dương, ID danh mục. |
| **Query Parameters** | Không có. |

#### Request Body

Không có.

#### Response mẫu

**Thành công — `204 No Content`:**

```http
HTTP/1.1 204 No Content
```

Không có response body. Chặn xóa nếu bất kỳ sự kiện nào đang tham chiếu, kể cả chờ duyệt/từ chối. Không cascade xóa sự kiện; khóa ngoại bảo vệ cả trường hợp có sự kiện được tạo đồng thời.

#### Mã lỗi

| HTTP status | Code | Trường hợp |
| --- | --- | --- |
| `400` | `INVALID_ID` / `INVALID_REQUEST` | ID không hợp lệ. |
| `404` | `CATEGORY_NOT_FOUND` | Danh mục không tồn tại. |
| `409` | `CATEGORY_IN_USE` | Danh mục đang được sự kiện sử dụng. |
| `401` | `UNAUTHORIZED` | Thiếu/sai phiên, phiên hết hạn/thu hồi hoặc tài khoản bị khóa. |
| `403` | `FORBIDDEN` | Sai role hoặc thiếu/sai header CSRF. |

Các lỗi ngoài dự kiến có thể trả `500`; xem quy ước lỗi ở mục 11.

### 11.8. Danh sách sự kiện chờ duyệt (UC33)

| Mục | Nội dung |
| --- | --- |
| **Method + Endpoint** | `GET /api/admin/events/pending` |
| **Authentication** | **Required** — cookie `access_token` hợp lệ, tài khoản đang `ACTIVE`. |
| **Role được phép sử dụng** | `ADMIN` |
| **Path Parameters** | Không có. |
| **Query Parameters** | `page` (integer, tùy chọn, mặc định 0, không âm), `pageSize` (integer, tùy chọn, mặc định 20, từ 1–100). |

#### Request Body

Không có.

#### Response mẫu

**Thành công — `200 OK`:**

```json
{
  "items": [
    {
      "id": 42,
      "name": "Đêm nhạc",
      "thumbnailImageUrl": "https://example.invalid/thumbnail.png",
      "categoryName": "Âm nhạc",
      "organizer": {
        "id": 13,
        "fullName": "Nhà tổ chức mẫu",
        "email": "organizer@example.invalid"
      },
      "createdAt": "2026-10-07T03:00:00",
      "status": "PENDING_APPROVAL"
    }
  ],
  "page": 0,
  "pageSize": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

Server cố định trạng thái `PENDING_APPROVAL`; không nhận bộ lọc trạng thái. Danh sách rỗng trả `200` với `items: []`; sắp xếp ID giảm dần.

#### Mã lỗi

| HTTP status | Code | Trường hợp |
| --- | --- | --- |
| `400` | `INVALID_PAGE` / `INVALID_REQUEST` | Phân trang không hợp lệ. |
| `401` | `UNAUTHORIZED` | Thiếu/sai phiên, phiên hết hạn/thu hồi hoặc tài khoản bị khóa. |
| `403` | `FORBIDDEN` | Sai role. |

Các lỗi ngoài dự kiến có thể trả `500`; xem quy ước lỗi ở mục 11.

### 11.9. Chi tiết hồ sơ xét duyệt (UC34)

| Mục | Nội dung |
| --- | --- |
| **Method + Endpoint** | `GET /api/admin/events/pending/{eventId}` |
| **Authentication** | **Required** — cookie `access_token` hợp lệ, tài khoản đang `ACTIVE`. |
| **Role được phép sử dụng** | `ADMIN` |
| **Path Parameters** | `eventId`: integer dương, ID sự kiện. |
| **Query Parameters** | Không có. |

#### Request Body

Không có.

#### Response mẫu

**Thành công — `200 OK`:**

```json
{
  "event": {
    "id": 42,
    "organizerId": 13,
    "categoryId": 3,
    "name": "Đêm nhạc",
    "description": "Biểu diễn",
    "thumbnailImageUrl": "https://example.invalid/thumb.png",
    "bannerImageUrl": "https://example.invalid/banner.png",
    "imageZoneUrl": null,
    "startTime": "2026-11-10T12:00:00",
    "endTime": "2026-11-10T15:00:00",
    "status": "PENDING_APPROVAL",
    "createdAt": "2026-10-07T03:00:00",
    "venue": {
      "id": 2,
      "city": "Hồ Chí Minh",
      "address": "Địa điểm mẫu",
      "capacity": 100
    },
    "ticketTypes": [
      {
        "id": 5,
        "name": "Vé phổ thông",
        "description": "Vào cửa",
        "imageUrl": "https://example.invalid/ticket.png",
        "price": 200000,
        "quantity": 100,
        "reservedQuantity": 0,
        "remainingQuantity": 100,
        "saleStartTime": "2026-10-09T01:00:00",
        "saleEndTime": "2026-11-09T10:00:00",
        "status": "INACTIVE"
      }
    ],
    "guests": [
      {
        "id": 7,
        "name": "Khách mời",
        "role": "Ca sĩ",
        "description": "Biểu diễn",
        "imageUrl": null
      }
    ],
    "categoryName": "Âm nhạc",
    "cancelReason": null,
    "canceledAt": null,
    "canEdit": true,
    "canCancel": true,
    "rejectReason": null
  },
  "organizer": {
    "id": 13,
    "fullName": "Nhà tổ chức mẫu",
    "email": "organizer@example.invalid"
  },
  "version": 0
}
```

`event` dùng cấu trúc `EventResponse` hiện có, gồm ảnh, địa điểm/sức chứa, thời gian, danh mục, vé và khách mời. `organizer` chứa thông tin Nhà tổ chức; `version` dùng khi duyệt/từ chối. `canEdit`/`canCancel` mô tả khả năng của Organizer, không cấp quyền chỉnh sửa cho Admin. Backend khóa bản ghi khi đọc hồ sơ để version, vé và khách mời thuộc cùng một lần cập nhật.

#### Mã lỗi

| HTTP status | Code | Trường hợp |
| --- | --- | --- |
| `400` | `INVALID_ID` / `INVALID_REQUEST` | ID không hợp lệ. |
| `404` | `EVENT_NOT_FOUND` | Sự kiện không tồn tại. |
| `409` | `EVENT_NOT_PENDING` | Sự kiện không còn chờ duyệt. |
| `401` | `UNAUTHORIZED` | Thiếu/sai phiên, phiên hết hạn/thu hồi hoặc tài khoản bị khóa. |
| `403` | `FORBIDDEN` | Sai role. |

Các lỗi ngoài dự kiến có thể trả `500`; xem quy ước lỗi ở mục 11.

### 11.10. Duyệt sự kiện (UC35)

| Mục | Nội dung |
| --- | --- |
| **Method + Endpoint** | `POST /api/admin/events/{eventId}/approve` |
| **Authentication** | **Required** — cookie `access_token` hợp lệ, tài khoản đang `ACTIVE`. |
| **Role được phép sử dụng** | `ADMIN` |
| **Path Parameters** | `eventId`: integer dương, ID sự kiện. |
| **Query Parameters** | Không có. |

#### Request Body

JSON với `Content-Type: application/json` và `X-CSRF-Protection: 1`.

| Field | Kiểu | Quy tắc |
| --- | --- | --- |
| `version` | integer (int64) | Bắt buộc, không âm; lấy từ hồ sơ UC34 vừa đọc. |

```json
{
  "version": 0
}
```

#### Response mẫu

**Thành công — `200 OK`:**

```json
{
  "eventId": 42,
  "status": "APPROVED",
  "reviewedBy": 1,
  "reviewedAt": "2026-10-07T10:00:00",
  "reason": null,
  "version": 1
}
```

Chỉ xử lý `PENDING_APPROVAL` và version còn khớp. Lưu danh tính Admin từ principal, giờ UTC server; chuyển sự kiện thành `APPROVED`, xóa lý do từ chối, vé chuyển `ACTIVE`, tạo thông báo cho Organizer. Việc bán vé vẫn phải tuân thủ cửa sổ thời gian bán. Frontend yêu cầu xác nhận; hủy dialog không gọi API.

#### Mã lỗi

| HTTP status | Code | Trường hợp |
| --- | --- | --- |
| `400` | `INVALID_ID` / `VALIDATION_ERROR` / `INVALID_REQUEST` | ID hoặc version không hợp lệ. |
| `404` | `EVENT_NOT_FOUND` | Sự kiện không tồn tại. |
| `409` | `EVENT_NOT_PENDING` | Sự kiện không còn chờ duyệt. |
| `409` | `EVENT_VERSION_CONFLICT` | Hồ sơ đã thay đổi; cần đọc lại UC34 trước khi quyết định. |
| `401` | `UNAUTHORIZED` | Thiếu/sai phiên, phiên hết hạn/thu hồi hoặc tài khoản bị khóa. |
| `403` | `FORBIDDEN` | Sai role hoặc thiếu/sai header CSRF. |

Các lỗi ngoài dự kiến có thể trả `500`; xem quy ước lỗi ở mục 11.

### 11.11. Từ chối sự kiện (UC36)

| Mục | Nội dung |
| --- | --- |
| **Method + Endpoint** | `POST /api/admin/events/{eventId}/reject` |
| **Authentication** | **Required** — cookie `access_token` hợp lệ, tài khoản đang `ACTIVE`. |
| **Role được phép sử dụng** | `ADMIN` |
| **Path Parameters** | `eventId`: integer dương, ID sự kiện. |
| **Query Parameters** | Không có. |

#### Request Body

JSON với `Content-Type: application/json` và `X-CSRF-Protection: 1`.

| Field | Kiểu | Quy tắc |
| --- | --- | --- |
| `version` | integer (int64) | Bắt buộc, không âm; lấy từ hồ sơ UC34 vừa đọc. |
| `reason` | string | Bắt buộc, trim, không trắng, tối đa 255 ký tự. |

```json
{
  "version": 0,
  "reason": "Vui lòng bổ sung thông tin địa điểm."
}
```

#### Response mẫu

**Thành công — `200 OK`:**

```json
{
  "eventId": 42,
  "status": "REJECTED",
  "reviewedBy": 1,
  "reviewedAt": "2026-10-07T10:00:00",
  "reason": "Vui lòng bổ sung thông tin địa điểm.",
  "version": 1
}
```

Chỉ xử lý `PENDING_APPROVAL` và version còn khớp. Chuyển sự kiện thành `REJECTED`, vé `INACTIVE`; lưu Admin từ principal, giờ UTC server và lý do; tạo thông báo cho Organizer. `GET /api/events/{eventId}` trả `rejectReason`; frontend hiển thị trạng thái/lý do. Chưa mở quyền sửa/gửi lại REJECTED. Hủy dialog không gọi API.

#### Mã lỗi

| HTTP status | Code | Trường hợp |
| --- | --- | --- |
| `400` | `INVALID_ID` / `VALIDATION_ERROR` / `INVALID_REQUEST` | ID, version hoặc lý do không hợp lệ. |
| `404` | `EVENT_NOT_FOUND` | Sự kiện không tồn tại. |
| `409` | `EVENT_NOT_PENDING` | Sự kiện không còn chờ duyệt. |
| `409` | `EVENT_VERSION_CONFLICT` | Hồ sơ đã thay đổi; cần đọc lại UC34 trước khi quyết định. |
| `401` | `UNAUTHORIZED` | Thiếu/sai phiên, phiên hết hạn/thu hồi hoặc tài khoản bị khóa. |
| `403` | `FORBIDDEN` | Sai role hoặc thiếu/sai header CSRF. |

Các lỗi ngoài dự kiến có thể trả `500`; xem quy ước lỗi ở mục 11.

### 11.12. Transaction, version và thông báo xét duyệt

Quyết định, trạng thái vé và bản ghi `notifications` được lưu trong cùng transaction. Lưu thông báo lỗi thì rollback toàn bộ. Sau commit, backend đẩy SSE đến các phiên web của người nhận. Duyệt/từ chối lặp hoặc đồng thời chỉ một quyết định thành công; mỗi người nhận được một thông báo.

Event dùng `@Version`. Organizer sửa/hủy dùng `PESSIMISTIC_FORCE_INCREMENT`, bảo đảm thay đổi chỉ vé/khách mời cũng tăng version. Admin đọc/xử lý khóa cùng bản ghi, kiểm tra `PENDING_APPROVAL` rồi so version. Khi nhận `404`/`409`, frontend chặn quyết định tiếp cho đến khi đọc lại hồ sơ. Form giữ dữ liệu khi lỗi có thể thử lại, chặn gửi lặp; hủy dialog không gọi API.

### 11.13. Migration và kiểm thử

V4 thêm version mặc định 0, trạng thái REJECTED và unique index tên danh mục. Không chỉnh V1–V3. Nếu dữ liệu đang có tên trùng sau chuẩn hóa, migration dừng; cần xử lý dữ liệu theo quyết định của nhóm, không tự gộp/xóa.
AdminIntegrationTests kiểm tra security, tạo user/hash, khóa phiên, danh mục, version, tranh chấp quyết định, rollback thông báo và giới hạn dữ liệu người nhận. Test PostgreSQL chạy trong môi trường riêng theo README.

## 12. Thông báo trên web (Admin, Organizer, Customer)

| Mục | Nội dung |
| --- | --- |
| **Method + Endpoint** | `GET /api/notifications` |
| **Authentication** | **Required** — cookie `access_token` hợp lệ, tài khoản đang `ACTIVE`. |
| **Role được phép sử dụng** | `ADMIN`, `ORGANIZER`, `CUSTOMER` |
| **Path Parameters** | Không có. |
| **Query Parameters** | `page` (integer, tùy chọn, mặc định 0, không âm), `pageSize` (integer, tùy chọn, mặc định 20, từ 1–100). |

### Request Body

Không có.

### Response mẫu

**Thành công — `200 OK`:**

```json
{
  "items": [
    {
      "id": 8,
      "title": "Sự kiện #42 bị từ chối",
      "content": "Vui lòng bổ sung thông tin địa điểm.",
      "type": "EVENT_REJECTED",
      "createdAt": "2026-10-07T10:00:00",
      "read": false
    }
  ],
  "page": 0,
  "pageSize": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

Người nhận lấy từ principal, không nhận `userId`; chỉ trả thông báo của tài khoản đang đăng nhập, sắp xếp ID giảm dần. Danh sách rỗng trả `200` với `items: []`; `createdAt` là UTC.

### Trạng thái đọc và SSE

| Method + Endpoint | Kết quả |
| --- | --- |
| `GET /api/notifications/unread-count` | `200`, JSON number là số chưa đọc của tài khoản hiện tại. |
| `PATCH /api/notifications/{id}/read` | `204`, đánh dấu đã đọc; gọi lại vẫn thành công. ID không tồn tại hoặc thuộc tài khoản khác trả `404 NOTIFICATION_NOT_FOUND`. |
| `PATCH /api/notifications/read-all` | `204`, đánh dấu tất cả thông báo hiện tại đã đọc. |
| `GET /api/notifications/stream` | `200 text/event-stream`, xác thực bằng cookie; chỉ nhận thay đổi của chính tài khoản. |

Mọi PATCH cần `X-CSRF-Protection: 1`. Kết nối SSE khác origin cần `withCredentials: true` và origin được backend cho phép. Stream gửi `connected` khi kết nối, `notifications-changed` sau khi tạo/đánh dấu đọc đã commit và heartbeat mỗi 25 giây. SSE chỉ báo cần tải lại dữ liệu; frontend lấy danh sách/số chưa đọc qua REST. Mỗi kết nối tồn tại tối đa 120 giây, frontend tự kết nối lại với backoff tối đa 30 giây và đồng bộ lại DB. Phiên hết hạn/thu hồi hoặc tài khoản bị khóa sẽ bị đóng stream.

Thông báo phát sinh từ các nghiệp vụ hiện có:

- Admin ACTIVE nhận sự kiện mới, cập nhật chờ duyệt và yêu cầu hủy.
- Organizer nhận kết quả xét duyệt và booking đã xác nhận.
- Customer nhận thông báo khi vé được phát hành thành công (vé miễn phí hoặc IPN VNPay hợp lệ).
- Customer có booking PAID nhận cập nhật, yêu cầu hủy và kết quả xét duyệt của sự kiện liên quan. Yêu cầu hủy được mô tả là đang chờ xét duyệt, chưa phải sự kiện đã hủy.

STAFF tạm thời không nhận thông báo mới và không được truy cập các REST API/SSE thông báo (`403 FORBIDDEN`).

DB là nguồn dữ liệu chính: đóng web/mất mạng vẫn lưu thông báo, mở lại sẽ tải qua REST. SSE dùng bộ nhớ của một instance backend; triển khai nhiều instance cần bổ sung cơ chế chia sẻ sự kiện. Không thêm email, FCM hoặc Kafka.

### Mã lỗi

| HTTP status | Code | Trường hợp |
| --- | --- | --- |
| `400` | `INVALID_PAGE` / `INVALID_REQUEST` | Phân trang không hợp lệ. |
| `401` | `UNAUTHORIZED` | Thiếu/sai phiên, phiên hết hạn/thu hồi hoặc tài khoản bị khóa. |
| `403` | `FORBIDDEN` | Sai role hoặc PATCH thiếu CSRF header. |

Các lỗi ngoài dự kiến có thể trả `500`; xem quy ước lỗi ở mục 11.

<a id="customer-booking-api"></a>

## 16. Customer Booking API

Các endpoint `/api/bookings/**` yêu cầu cookie đăng nhập, tài khoản ACTIVE và role CUSTOMER. Mọi POST phải có `X-CSRF-Protection: 1`; frontend khác origin dùng `credentials: "include"`. Customer lấy từ principal, không nhận `userId`, giá tiền hoặc trạng thái do client quyết định. Thời gian trong JSON là UTC; số tiền dùng VND và BigDecimal.

| Method | Endpoint | Chức năng |
| --- | --- | --- |
| GET | `/api/bookings/events/{eventId}/ticket-types` | Danh sách loại vé ACTIVE của sự kiện APPROVED, giá, số lượng còn lại và `bookable` |
| POST | `/api/bookings` | Đặt vé, giữ số lượng và trả chi tiết booking (`201`) |
| POST | `/api/bookings/{bookingId}/payments/vnpay` | Tạo/lấy lại URL VNPay Sandbox (`200`, không cần body) |
| GET | `/api/bookings?page=0&size=20` | Lịch sử booking của customer, mới nhất trước |
| GET | `/api/bookings/{bookingId}` | Chi tiết booking, items, vé và các lần thanh toán |
| GET | `/api/payments/vnpay/ipn` | VNPay server gọi để xác nhận thanh toán, xác thực bằng chữ ký |
| GET | `/api/payments/vnpay/return` | Kiểm tra chữ ký và hiển thị kết quả quay về, không cập nhật booking |

### Đặt vé

```json
{
  "eventId": 42,
  "items": [
    { "ticketTypeId": 7, "quantity": 2 },
    { "ticketTypeId": 8, "quantity": 1 }
  ]
}
```

Sự kiện phải APPROVED và chưa bắt đầu; vé ACTIVE, đã mở bán và chưa đóng bán. Mỗi loại vé chỉ xuất hiện một lần, thuộc đúng sự kiện, quantity từ 1–100; tối đa 20 loại và 100 vé trong một booking. Backend lưu `unitPrice` tại lúc đặt vé và tự tính tổng. Không đủ vé trả `409`, không tạo booking dở dang.

Ví dụ response `201` rút gọn:

```json
{
  "id": 120,
  "status": "PENDING",
  "totalAmount": 450000.00,
  "currency": "VND",
  "createdAt": "2026-10-12T12:00:00",
  "expiredAt": "2026-10-12T12:15:00",
  "event": { "id": 42, "name": "EventHub Live", "city": "Hồ Chí Minh", "address": "Địa điểm sự kiện" },
  "items": [
    { "id": 201, "ticketTypeId": 7, "ticketTypeName": "Standard", "quantity": 2, "unitPrice": 150000.00, "subtotal": 300000.00, "tickets": [] },
    { "id": 202, "ticketTypeId": 8, "ticketTypeName": "Early Bird", "quantity": 1, "unitPrice": 150000.00, "subtotal": 150000.00, "tickets": [] }
  ],
  "payments": []
}
```

`event` đầy đủ còn có `thumbnailImageUrl`, `status`, `startTime`, `endTime`. Booking giữ vé mặc định 15 phút, rút ngắn nếu sắp đóng bán hoặc sự kiện bắt đầu. Vé giữ được trừ khỏi `remainingQuantity` và cộng vào `reservedQuantity`; thanh toán thành công chỉ giảm reserved, hết hạn trả reserved về remaining. Booking miễn phí chuyển PAID và phát vé ngay, payment có `paymentMethod: "FREE"`.

### Thanh toán

Gọi `POST /api/bookings/120/payments/vnpay`, response:

```json
{
  "bookingId": 120,
  "paymentId": 51,
  "transactionId": "merchant-reference-unique-per-attempt",
  "paymentUrl": "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?...",
  "expiredAt": "2026-10-12T12:15:00"
}
```

Frontend chuyển trình duyệt đến `paymentUrl`. Gọi lại trong khi cùng payment đang PENDING và chưa đến expiredAt trả cùng reference/URL. Nếu payment thất bại, có thể tạo lần thử mới trước expiredAt; không thanh toán lại booking PAYMENT_CONFIRMING/PAID/EXPIRED/PAYMENT_REVIEW. Số tiền VNPay lấy từ booking, nhân 100 theo giao thức; vượt giới hạn 12 chữ số của `vnp_Amount` trả `400` và rollback lần tạo payment.

Callback chỉ chấp nhận HMAC-SHA512 hợp lệ, đúng merchant/reference/số tiền. Phải đồng thời có `vnp_ResponseCode=00` và `vnp_TransactionStatus=00` mới thành công. IPN lưu payment và phát vé trong cùng transaction; IPN lặp không phát thêm vé. Chỉ GET IPN/return được công khai và dùng chữ ký thay phiên đăng nhập; các API customer vẫn giữ xác thực và CSRF hiện tại.

IPN trả JSON theo quy định VNPay (tên trường bên ngoài giữ nguyên):

```json
{ "RspCode": "00", "Message": "Confirm Success" }
```

| RspCode | Ý nghĩa |
| --- | --- |
| `00` | Đã ghi nhận kết quả vào DB, gồm cả giao dịch bị từ chối/hủy |
| `02` | Lần thanh toán đã được xử lý trước đó |
| `01` | Không tìm thấy giao dịch |
| `04` | Số tiền sai |
| `97` | Chữ ký/merchant sai hoặc query parameter bị lặp |
| `99` | Tham số kết quả không hợp lệ hoặc lưu DB thất bại; VNPay có thể retry |

Return URL trả `bookingId`, `bookingStatus`, `paymentStatus`, `responseCode`, `transactionStatus`. `responseCode=00` ở trang quay về chưa đảm bảo backend đã nhận IPN. Frontend đọc lại `GET /api/bookings/{bookingId}` cho đến khi backend xác nhận; chỉ hiển thị vé khi booking PAID.

Booking có payment VNPay PENDING được giữ thêm BOOKING_IPN_GRACE_PERIOD (mặc định 120 giây) sau expiredAt để chờ IPN. VNPay vẫn nhận vnp_ExpireDate theo expiredAt ban đầu. IPN thành công chỉ phát vé khi vnp_PayDate <= expiredAt, booking vẫn giữ vé, sự kiện APPROVED và backend xử lý trước expiredAt + khoảng chờ. Nếu thanh toán sau hạn ban đầu, IPN quá hạn chờ (kể cả job chưa chạy), vé đã được giải phóng hoặc sự kiện đã chuyển khỏi APPROVED, backend lưu payment SUCCESS và booking PAYMENT_REVIEW, không phát vé; cần đối soát/hoàn tiền. Chưa có API hoàn tiền trong tính năng này.

### Lịch sử và chi tiết

Lịch sử trả `{ "items": [...], "page": 0, "size": 20, "totalElements": 2, "totalPages": 1 }`. Mỗi summary có `id`, `status`, `totalAmount`, `currency`, `createdAt`, `expiredAt`, `event`. `page >= 0`, `size` từ 1–50. Sắp xếp createdAt rồi id giảm dần; danh sách rỗng trả `200` và `items: []`.

Chi tiết trả cùng cấu trúc response đặt vé, bổ sung trong mỗi item danh sách `tickets`: `id`, `ticketCode`, `qrToken`, `status`, `checkedInAt`. QR token là dữ liệu để frontend tạo ảnh QR, chỉ xuất hiện trong chi tiết của chủ booking. `payments` trả `id`, `transactionId` (reference của backend), `gatewayTransactionId` (mã VNPay), `paymentMethod`, `amount`, `status`, `responseCode`, `createdAt`, `paidAt`; lần thử mới nhất trước. Giá trong items là giá lúc đặt, không bị thay đổi khi Organizer sửa giá vé.

| Booking status | Ý nghĩa |
| --- | --- |
| `PENDING` | Đang giữ vé và chờ thanh toán |
| `PAYMENT_CONFIRMING` | Đã hết hạn thanh toán, đang giữ vé thêm để chờ IPN; không tạo thanh toán mới |
| `PAID` | Đã xác nhận và phát vé VALID |
| `EXPIRED` | Hết thời hạn thanh toán; không thể tiếp tục trả tiền |
| `PAYMENT_REVIEW` | Đã nhận tiền nhưng không thể phát vé an toàn, chờ đối soát |

Lịch sử/chi tiết trả thêm paymentConfirmationDeadline (UTC), bằng expiredAt + khoảng chờ khi booking còn PENDING và có payment VNPay PENDING, còn lại null. PAYMENT_CONFIRMING chỉ là trạng thái hiển thị của API; trong database vẫn giữ PENDING, không thêm migration. Khi now >= expiredAt: có payment đang chờ và now < paymentConfirmationDeadline thì hiển thị PAYMENT_CONFIRMING; còn lại EXPIRED. Return API cũng trả bookingStatus theo quy tắc này. FE tiếp tục đọc lại booking trong khoảng chờ, không thông báo hết hạn hoặc cho thanh toán lại sớm.

Job giải phóng kho mặc định mỗi 30 giây, và thao tác tạo booking cũng dọn tối đa 500 booking đủ điều kiện hết hạn của cùng sự kiện. Cả hai bỏ qua booking còn trong khoảng chờ. Khi đến đúng paymentConfirmationDeadline mới được giải phóng vé; nếu không có payment đang chờ thì giải phóng ngay từ expiredAt. IPN thất bại trong khoảng chờ cũng giải phóng vé ngay. Mỗi đợt job xử lý tối đa 100 sự kiện, mỗi sự kiện 500 booking, rồi tiếp tục ở đợt sau.

### Vé điện tử / QR của customer

| API | Quyền | Mô tả |
| --- | --- | --- |
| `GET /api/tickets?page=0&size=20&bookingId=120&status=VALID` | CUSTOMER | Các filter bookingId/status là tùy chọn, size 1–50; chỉ trả vé của chủ tài khoản |

Danh sách vé trả `{items,page,size,totalElements,totalPages}`. Mỗi vé có `id`, `bookingId`, `ticketCode`, `qrToken`, `status`, `ticketTypeName`, `unitPrice`, `ownerName`, `checkedInAt`, `canCheckIn`, `event`. Dữ liệu ngày giờ không có offset là UTC. FE hiển thị theo Asia/Ho_Chi_Minh. QR chỉ có giá trị khi `canCheckIn=true`; vé USED hoặc sự kiện không còn hiệu lực trả `qrToken:null`.

API này chỉ đọc vé của customer. Chức năng quét và cập nhật check-in dành cho staff/organizer không nằm trong phạm vi triển khai.

### Lỗi HTTP

| HTTP | Trường hợp |
| --- | --- |
| `400` | Body/ID/phân trang không hợp lệ, loại vé sai sự kiện/trùng, quá giới hạn, callback return sai chữ ký |
| `401` | Chưa đăng nhập, phiên hết hạn hoặc tài khoản không còn ACTIVE |
| `403` | Sai role hoặc thiếu `X-CSRF-Protection: 1` trên POST |
| `404` | Booking không tồn tại hoặc thuộc customer khác; sự kiện không tồn tại hoặc chưa thể xem danh sách vé |
| `409` | Hết vé, đóng bán, sự kiện không thể đặt, booking không thể thanh toán |
| `503` | Chưa cấu hình merchant/secret VNPay Sandbox hợp lệ |

Lỗi dùng `GlobalExceptionHandler` hiện tại (`REQUEST_ERROR`, `VALIDATION_ERROR`, `INVALID_REQUEST`, ...). Cấu hình môi trường Sandbox và IPN công khai xem [README](README.md#customer-booking-và-vnpay-sandbox); tham khảo giao thức trong [tài liệu VNPay PAY](https://sandbox.vnpayment.vn/apis/docs/thanh-toan-pay/pay.html).

---

## 17. Phân hệ Nhân viên soát vé — UC28 & UC29 (Staff Portal APIs)

Các endpoint dành riêng cho nhân viên soát vé (role `STAFF`) phục vụ **UC28 (Xem danh sách sự kiện được phân công)** và **UC29 (Xem chi tiết sự kiện được phân công)**, bao gồm tra cứu sự kiện, kiểm tra danh sách vé, bộ lọc vé, thống kê soát vé và thực hiện check-in.

### 17.1. UC28 — Xem danh sách sự kiện được phân công

| Mục | Nội dung |
| --- | --- |
| **Method + Endpoint** | `GET /api/staff/events` |
| **Authentication** | Required — access token qua HttpOnly Cookie |
| **Role** | `STAFF` |
| **Query Parameters** | `date` (tùy chọn, format YYYY-MM-DD), `status` (tùy chọn: `APPROVED`, `ONGOING`, `COMPLETED`) |
| **Response** | `200 OK` — `List<StaffAssignedEventResponse>` (`eventId`, `eventName`, `thumbnailImageUrl`, `bannerImageUrl`, `startTime`, `endTime`, `eventStatus`, `venueName`, `venueAddress`, `staffStatus`, `assignedAt`, `checkedInTickets`, `totalPaidTickets`, `checkInRate`) |

### 17.2. UC29 — Xem chi tiết sự kiện được phân công

| Mục | Nội dung |
| --- | --- |
| **Method + Endpoint** | `GET /api/staff/events/{eventId}` |
| **Authentication** | Required — access token qua HttpOnly Cookie |
| **Role** | `STAFF` (chỉ xem được sự kiện mình được phân công) |
| **Response** | `200 OK` — `StaffEventDetailDto` đầy đủ thông tin sự kiện, địa điểm, ban tổ chức, sơ đồ `imageZoneUrl`, và trạng thái phân công `staffStatus`. |

### 17.3. Hỗ trợ UC29 — Danh sách khách hàng đã đặt vé

| Mục | Nội dung |
| --- | --- |
| **Method + Endpoint** | `GET /api/staff/events/{eventId}/customers` |
| **Authentication** | Required — access token qua HttpOnly Cookie |
| **Role** | `STAFF` |
| **Mô tả** | Truy vấn danh sách khách hàng có booking `PAID` của sự kiện để gán vào thanh lọc (select box) tại giao diện chi tiết sự kiện. |
| **Response** | `200 OK` — `List<StaffCustomerDto>` (`id`, `fullName`, `email`, `phone`, `ticketCount`) |

### 17.4. Hỗ trợ UC29 — Danh sách loại vé sự kiện

| Mục | Nội dung |
| --- | --- |
| **Method + Endpoint** | `GET /api/staff/events/{eventId}/ticket-types` |
| **Authentication** | Required — access token qua HttpOnly Cookie |
| **Role** | `STAFF` |
| **Mô tả** | Trả về danh sách loại vé (Standard, VIP, Early Bird, ...) để gán vào select box lọc loại vé. |
| **Response** | `200 OK` — `List<StaffTicketTypeDto>` (`id`, `name`, `price`, `quantity`) |

### 17.5. Hỗ trợ UC29 — Thống kê vé sự kiện

| Mục | Nội dung |
| --- | --- |
| **Method + Endpoint** | `GET /api/staff/events/{eventId}/stats` |
| **Authentication** | Required — access token qua HttpOnly Cookie |
| **Role** | `STAFF` |
| **Response** | `200 OK` — `StaffTicketStatsDto` (`totalTickets`, `checkedInTickets`, `remainingTickets`, `checkInRate`) |

### 17.6. Hỗ trợ UC29 — Danh sách vé theo bộ lọc và phân trang

| Mục | Nội dung |
| --- | --- |
| **Method + Endpoint** | `GET /api/staff/events/{eventId}/tickets` |
| **Authentication** | Required — access token qua HttpOnly Cookie |
| **Role** | `STAFF` |
| **Query Parameters** | `customerId` (tùy chọn), `ticketTypeId` (tùy chọn), `status` (tùy chọn: `CHECKED_IN`, `NOT_CHECKED_IN`), `page` (default `0`), `size` (default `20`) |
| **Response** | `200 OK` — `StaffTicketPageResponse` (`content: List<StaffTicketDto>`, `pageNumber`, `pageSize`, `totalElements`, `totalPages`, `last`) |

### 17.7. Hỗ trợ UC29 (Mobile App) — Check-in vé tại cổng

| Mục | Nội dung |
| --- | --- |
| **Method + Endpoint** | `POST /api/staff/events/{eventId}/check-in` |
| **Authentication** | Required — access token qua HttpOnly Cookie |
| **Role** | `STAFF` |
| **Header** | `X-CSRF-Protection: 1` |
| **Request Body** | `{ "ticketCode": "TIC-123456", "qrToken": "..." }` (hoặc ticketId) |
| **Response** | `200 OK` — `{ "success": true, "message": "Check-in thành công", "ticketId": 123, "checkedInAt": "..." }` |
| **Lỗi** | `400` Vé không hợp lệ / sai sự kiện; `409` Vé đã được check-in trước đó. |
| **Lưu ý** | Giao diện Web hiển thị chế độ giám sát và tra cứu (chỉ đọc); chức năng quét mã check-in thực tế được vận hành qua ứng dụng di động (Mobile App). |

