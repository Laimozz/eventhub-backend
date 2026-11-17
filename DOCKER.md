# Docker cho EventHub Backend

Image sử dụng Maven + Java 21 để build, sau đó chạy JRE 21 bằng user `eventhub` không có quyền root. Profile `docker` sử dụng biến môi trường, không đọc `application-local.properties`. Healthcheck gọi `/api/categories` để kiểm tra API và kết nối DB.

Hướng dẫn chạy cả FE + BE + PostgreSQL, tạo JWT và điền Cloudinary/VNPay/SMTP/OpenRouter nằm ở [DOCKER.md của frontend](../eventhub-frontend/DOCKER.md). Đặt hai repo cạnh nhau rồi chạy Compose từ repo frontend. File cấu hình mẫu là [`.env.docker.example`](../eventhub-frontend/.env.docker.example).

## Build riêng image BE

```powershell
cd D:\DOAN\eventhub-backend
docker build -t eventhub-backend:local .
```

Build image không cần DB hoặc khóa thật. Maven trong Dockerfile bỏ qua chạy test; đây không thay thế bước kiểm thử của CI.

## Cấu hình tối thiểu khi chạy riêng BE

Tạo `.env` trong repo BE (đã được Git bỏ qua):

```dotenv
DB_URL=jdbc:postgresql://host.docker.internal:5432/eventhub_db
DB_USERNAME=postgres
DB_PASSWORD=YOUR_DATABASE_PASSWORD
JWT_SECRET=YOUR_RANDOM_BASE64_KEY
AUTH_ALLOWED_ORIGINS=http://localhost:5173
AUTH_COOKIE_SECURE=false
PAYMENT_EMAIL_ENABLED=false
```

Thay mật khẩu và JWT bằng giá trị thật, dùng DB đã tạo. `.env` cho `docker run --env-file` dùng `KEY=value` không bọc thêm dấu nháy quanh giá trị (khác parser `.env` của Compose).

```powershell
docker run --detach --name eventhub-backend-local --env-file .env -p 127.0.0.1:8080:8080 eventhub-backend:local
```

Flyway áp dụng các migration khi BE khởi động. `host.docker.internal` phù hợp với Docker Desktop khi DB nằm trên máy Windows; nếu DB nằm trong cùng mạng Docker, dùng tên service DB. Cloudinary, VNPay, email và chatbot tùy chọn giống bảng cấu hình ở tài liệu FE.

Các giá trị nhạy cảm mặc định trước đây đã được bỏ khỏi `src/main/resources/application.properties` và giữ lại trong `application-local.properties` trên máy phát triển. File local không được đưa vào image. Khi clone mới và chạy trực tiếp ngoài Docker, tự điền DB/JWT/Cloudinary bằng biến môi trường hoặc file local.
