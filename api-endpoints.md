# Tài liệu API Endpoint — EventHub

Base URL local: `http://localhost:8080`.

Tài liệu mô tả bốn endpoint đang được triển khai trong `AuthController`.

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

