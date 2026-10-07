# EventHub Backend

Backend của dự án EventHub, sử dụng Java 21, Spring Boot và Maven. README này hướng dẫn cách chạy dự án, tổ chức mã nguồn và phối hợp bằng Git.

## 1. Công nghệ và trạng thái hiện tại

| Thành phần | Cấu hình hiện tại |
| --- | --- |
| Java | 21 |
| Spring Boot | 4.1.1, khai báo trong `pom.xml` |
| Web | Spring Web MVC, cung cấp REST API |
| Build | Maven, có Maven Wrapper trong repository |
| Kiểm thử | Spring Boot Test và JUnit |
| Database | PostgreSQL 16+, Spring Data JPA, Flyway |

Hiện tại dự án có 20 entity/repository, migration Flyway và kiểm thử tích hợp PostgreSQL. API gồm đăng ký, đăng nhập, refresh, đăng xuất bằng JWT qua HttpOnly Cookie và tạo sự kiện dành cho Organizer. Xem [tài liệu API](api-endpoints.md) để tích hợp cookie, header bảo vệ CSRF và request tạo sự kiện.

## 2. Chuẩn bị và chạy dự án

### Yêu cầu

- Cài JDK 21 và Git.
- IDE có thể dùng IntelliJ IDEA, VS Code hoặc Eclipse; đặt SDK của dự án là Java 21.
- Có kết nối Internet trong lần build đầu để tải Maven và các dependency.
- Không bắt buộc cài Maven riêng vì dự án đã có `mvnw` và `mvnw.cmd`.
- PostgreSQL đang chạy và đã tạo database `eventhub_db`.
- Khi chạy test mặc định: Docker đang chạy để Testcontainers tạo PostgreSQL riêng. Có thể dùng database test riêng khi không có Docker (hướng dẫn bên dưới).

Kiểm tra môi trường:

```shell
java -version
git --version
```

### Lấy mã nguồn

Thay `REPOSITORY_URL` bằng URL clone của repository nhóm:

```shell
git clone REPOSITORY_URL
cd eventhub-backend
git fetch origin
git switch -c develop --track origin/develop
```

Nếu đã có nhánh `develop` trên máy, dùng `git switch develop` rồi `git pull --ff-only origin develop`. Nếu chưa có `origin/develop`, người quản lý repository cần tạo nhánh theo mục 8 trước khi nhóm bắt đầu làm việc.

Mở thư mục dự án hoặc import `pom.xml` dưới dạng Maven project trong IDE.

### Các lệnh Maven

Chạy lệnh tại thư mục gốc, nơi chứa `pom.xml`:

| Công việc | Windows PowerShell | macOS / Linux |
| --- | --- | --- |
| Chạy ứng dụng | `.\mvnw.cmd spring-boot:run` | `./mvnw spring-boot:run` |
| Chạy kiểm thử | `.\mvnw.cmd test` | `./mvnw test` |
| Build và kiểm tra trước khi gửi PR | `.\mvnw.cmd clean verify` | `./mvnw clean verify` |

Trên macOS/Linux, nếu thiếu quyền thực thi, chạy `chmod +x mvnw` một lần.

Ứng dụng mặc định chạy tại `http://localhost:8080`. Cần cấu hình `JWT_SECRET` trước khi khởi động; API dùng cookie xác thực thay cho trang đăng nhập mặc định. Dùng `Ctrl+C` để dừng.

Cấu hình chung đặt tại `src/main/resources/application.properties`. Dùng biến môi trường cho mật khẩu và khóa bí mật; chỉ commit cấu hình mẫu không chứa giá trị thật. `.env` đã được bỏ qua trong Git nhưng Spring Boot không tự động đọc file này.

### Kết nối PostgreSQL

Ứng dụng mặc định kết nối `jdbc:postgresql://localhost:5432/eventhub_db`, user `postgres`.
Database cần được tạo sẵn; Flyway tạo các bảng khi ứng dụng khởi động.

| Biến môi trường | Mặc định |
| --- | --- |
| `DB_URL` | `jdbc:postgresql://localhost:5432/eventhub_db` |
| `DB_USERNAME` | `postgres` |
| `DB_PASSWORD` | Trống; cần đặt mật khẩu PostgreSQL của máy |

PowerShell:

```powershell
$env:DB_URL = "jdbc:postgresql://localhost:5432/eventhub_db"
$env:DB_USERNAME = "postgres"
$dbCredential = Get-Credential -UserName postgres -Message "PostgreSQL credentials"
$env:DB_PASSWORD = $dbCredential.GetNetworkCredential().Password
.\mvnw.cmd spring-boot:run
```

Hoặc copy `application-local.properties.example` thành `application-local.properties` **ở thư mục gốc dự án**, rồi điền mật khẩu. File này được nạp tự động khi chạy từ thư mục gốc và đã nằm trong `.gitignore`; không cần bật profile. Giá trị trong file local có ưu tiên hơn các placeholder `DB_*` ở cấu hình chung; chọn một cách cấu hình để tránh nhầm lẫn.

### Cấu hình xác thực

Tạo khóa JWT ngẫu nhiên cho môi trường local bằng PowerShell:

```powershell
$jwtKey = New-Object byte[] 32
$jwtRandom = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$jwtRandom.GetBytes($jwtKey)
$env:JWT_SECRET = [Convert]::ToBase64String($jwtKey)
$jwtRandom.Dispose()
$env:AUTH_COOKIE_SECURE = "false" # Chỉ cho local HTTP
.\mvnw.cmd spring-boot:run
```

Hoặc đặt `app.auth.jwt-secret` và `app.auth.cookie-secure=false` trong `application-local.properties` theo file mẫu. Nếu file local khai báo các giá trị này, chúng ưu tiên hơn placeholder môi trường trong cấu hình chung; không để khóa trống trong file local khi định dùng `JWT_SECRET`.

Môi trường triển khai dùng khóa riêng lưu trong secret manager/biến môi trường và đặt `AUTH_COOKIE_SECURE=true` với HTTPS (cấu hình chung mặc định `false` cho local HTTP). Giữ khóa ổn định giữa các lần restart và các instance; đổi khóa làm access token cũ mất hiệu lực. Đăng ký công khai yêu cầu frontend gửi `role` là CUSTOMER hoặc ORGANIZER; ADMIN và STAFF không được tự đăng ký. Đăng ký chỉ trả thông báo thành công, chưa tạo phiên; đăng nhập mới trả thông tin user và hai cookie token.

`ApplicationProperties` tập trung các cấu hình tùy chỉnh `app.*` với nhóm `auth`; cấu hình `spring.*` vẫn do Spring Boot quản lý. Thời hạn access token và refresh token được đặt trong `application.properties`, dùng chung cho JWT, cookie và thời hạn phiên trong database:

```properties
app.auth.access-token-ttl=${JWT_ACCESS_TOKEN_TTL:15m}
app.auth.refresh-token-ttl=${JWT_REFRESH_TOKEN_TTL:7d}
```

Có thể thay đổi bằng biến môi trường tương ứng hoặc cấu hình trong file local. Giá trị phải là số giây nguyên dương, ví dụ `30s`, `15m`, `7d`.

Frontend cần gửi credentials và header `X-CSRF-Protection: 1` khi gọi POST. Không cần endpoint lấy CSRF token. Chi tiết 4 endpoint, rotation và ví dụ Fetch nằm trong [api-endpoints.md](api-endpoints.md).

### Entity và migration

- Entity: `src/main/java/com/eventhub/backend/entity/`; repository: `src/main/java/com/eventhub/backend/repository/`.
- Mỗi entity là class độc lập, khai báo trực tiếp `id` và các trường thời gian tương ứng với bảng. Các callback `@PrePersist`/`@PreUpdate` nằm ngay trong entity; không dùng lớp cha chung.
- Migration: `src/main/resources/db/migration/`: V1 schema, V2 thêm xác thực/refresh tokens với `revoked BOOLEAN NOT NULL DEFAULT FALSE` và cho phép role STAFF; V3 giới hạn `events.status` theo sáu giá trị trong `EventStatus`. V3 kiểm tra dữ liệu hiện có, không tự đổi các status cũ ngoài danh sách.
- Hibernate dùng `ddl-auto=validate`: chỉ kiểm tra mapping, Flyway chịu trách nhiệm thay đổi schema. Cấu hình này theo [hướng dẫn Spring Boot](https://docs.spring.io/spring-boot/how-to/data-initialization.html).
- Sau khi V1 đã chạy, mọi thay đổi schema cần file mới như `V2__add_event_field.sql`; không sửa migration đã áp dụng. Không bật tự động baseline cho database đã có bảng.
- Các quan hệ dùng lazy loading, không cascade xóa. Khóa ngoại ngăn xóa bản ghi còn được tham chiếu; PostgreSQL có index trên các cột khóa ngoại.

Điều chỉnh so với ký hiệu MySQL trong sơ đồ:

- `int(10)` → PostgreSQL `INTEGER`/Java `Integer`, ID tự tăng bằng identity. Tên bảng/cột dùng chữ thường, gồm `categories`/`categories_id`.
- Tiền dùng `NUMERIC(19,2)`/`BigDecimal` thay cho `double`. Schema chưa có cột tiền tệ; tầng nghiệp vụ cần thống nhất đơn vị khi triển khai thanh toán.
- `datetime` → `TIMESTAMP(6) WITHOUT TIME ZONE`/`LocalDateTime`, quy ước giá trị là UTC; giao diện cần chuyển múi giờ khi hiển thị.
- `Event`, `TicketType`, `EventGuest`, `Notification` dùng `@JdbcTypeCode(SqlTypes.LOCAL_DATE_TIME)` để truyền thời gian trực tiếp qua JDBC, giữ nguyên giá trị UTC khi JVM chạy ở múi giờ khác. Các bảng này không tự chuyển đổi dữ liệu thời gian đã lưu trước thay đổi. Xem [cơ chế ánh xạ thời gian của Hibernate](https://docs.hibernate.org/orm/7.4/javadocs/org/hibernate/type/SqlTypes.html#LOCAL_DATE_TIME).
- `created_at` và `updated_at` (ở các bảng có cột này) được JPA tự gán, luôn có giá trị; SQL có default khi insert. Khi update bằng SQL trực tiếp/bulk query, phải tự cập nhật `updated_at`.
- `is_read` dùng Boolean, mặc định false; `reserved_quantity` và `retry_count` mặc định 0. Các số dư ví mặc định 0.
- `reviewed_by`, `checked_in_at`, `refunded_at`, `processed_by`, `processed_at`, `completed_at` và `transaction_code` của rút tiền/chi trả cho phép NULL khi thao tác chưa xảy ra.
- Unique theo cặp `event_staffs(events_id, staff_id)` và `booking_items(bookings_id, ticket_types_id)`: một sự kiện có nhiều nhân viên, một đơn có nhiều loại vé. Mỗi organizer có tối đa một ví, mỗi payment có tối đa một refund.
- Có CHECK cho tiền/số lượng không âm, số lượng vé đặt mua dương, tổng vé giữ chỗ và vé còn lại không vượt số vé, thời gian kết thúc không trước thời gian bắt đầu.
- `users.role` ánh xạ enum Role, mỗi user chỉ có một role ADMIN/CUSTOMER/ORGANIZER/STAFF. Khi được phân công, nghiệp vụ đổi role CUSTOMER thành STAFF trong database. Auth chỉ cho phép user có `status=ACTIVE`, đọc trực tiếp role hiện tại và không truy vấn phân công trong `event_staffs`; `UserResponse` và JWT chỉ có `role`, không có tập `roles`. Chưa có API quản lý phân công. `events.status` ánh xạ `EventStatus`; các status/type nghiệp vụ khác vẫn giữ dạng chuỗi.

### Tạo sự kiện

`POST /api/events` dành cho `ORGANIZER`, lưu địa điểm mới, sự kiện, loại vé và khách mời (nếu có) trong cùng transaction. Danh mục được chọn bằng `categoryId` đã tồn tại. Sự kiện mới luôn là `PENDING_APPROVAL` để Admin xét duyệt sau; chưa triển khai API duyệt. Hệ thống có một Admin: khi có Admin ACTIVE, tạo một thông báo chưa đọc `EVENT_PENDING_APPROVAL` trong cùng transaction; nếu chưa có Admin ACTIVE, sự kiện vẫn nằm trong hàng chờ duyệt. Thông báo chỉ hiển thị nội dung, dùng bảng hiện tại với `users_id`. Loại vé khởi tạo `INACTIVE`, số vé giữ chỗ bằng 0 và số vé còn lại bằng số lượng. Chi tiết request, validation và sáu trạng thái nằm trong [tài liệu API](api-endpoints.md#7-tạo-sự-kiện).

Form frontend dùng `GET /api/categories` để lấy danh mục trong DB. Ảnh được xem trước tại trình duyệt, chưa upload khi chọn file. Khi gửi duyệt, frontend gọi API duy nhất `POST /api/events` trong `EventController` với multipart gồm JSON sự kiện và các file ảnh (mỗi file PNG/JPEG tối đa 5 MB, 20 megapixel; tổng request tối đa 50 MB). Request JSON chỉ chứa thông tin sự kiện, vé và khách mời; không nhận URL ảnh từ client, không hỗ trợ body JSON riêng. Backend kiểm tra toàn bộ dữ liệu/file, lưu và flush hồ sơ trong transaction trước khi gọi [Cloudinary Upload API](https://cloudinary.com/documentation/image_upload_api_reference) bằng Spring RestClient hiện có. Sau khi upload, cập nhật URL HTTPS (`secure_url`) vào các cột ảnh hiện có rồi commit; response trả hồ sơ và URL. `EventService` xử lý tạo hồ sơ và transaction, `EventImageService` xử lý ảnh và Cloudinary. Không có endpoint upload ảnh riêng, thư mục lưu ảnh hoặc API đọc ảnh local.

Cấu hình ba biến môi trường **trên backend** (lấy từ API Keys trong Cloudinary Console):

| Biến môi trường | Giá trị |
| --- | --- |
| `CLOUDINARY_CLOUD_NAME` | Cloud name của môi trường Cloudinary |
| `CLOUDINARY_API_KEY` | API key |
| `CLOUDINARY_API_SECRET` | API secret; không đặt trong frontend hoặc commit vào Git |

Hoặc đặt `app.cloudinary.cloud-name`, `app.cloudinary.api-key`, `app.cloudinary.api-secret` trong `application-local.properties` đã được Git bỏ qua. Khởi động lại backend sau khi cấu hình. Thiếu cấu hình thì tạo sự kiện multipart trả `503` và rollback; các API khác vẫn hoạt động. Không cần unsigned upload preset. Ảnh upload vào `eventhub/events` với ID UUID riêng cho mỗi lần tạo. Nếu upload hoặc lưu DB thất bại, transaction rollback và backend gọi destroy cho các ID của lần tạo đó. Cloudinary và PostgreSQL không có transaction chung: nếu destroy cũng thất bại, backend ghi log để xử lý ảnh còn lại, không che lỗi tạo sự kiện. Hủy bản nháp chưa gửi không tạo ảnh trên Cloudinary. Chi tiết trong [tài liệu API](api-endpoints.md#9-ảnh-dùng-trong-form-tạo-sự-kiện).

### Kiểm thử database

`mvnw test` và `mvnw clean verify` mặc định dùng Testcontainers với `postgres:16-alpine`. PostgreSQL test tách biệt với `eventhub_db`, tự dọn khi Spring context đóng; các test dữ liệu chạy trong transaction và rollback. Lần đầu cần mạng để tải image.

Bộ test chạy Flyway, Hibernate validate, ràng buộc dữ liệu và 4 luồng auth qua MockMvc trên PostgreSQL thật. Kiểm tra đăng ký không tạo phiên, hash, thời hạn token/cookie theo cấu hình, role STAFF lấy từ database, header CSRF/CORS, token sai/hết hạn/rotation, refresh đồng thời, đăng xuất, user bị khóa và nâng V1 lên migration mới nhất. Test tạo sự kiện kiểm tra quyền Organizer, trạng thái chờ duyệt, dữ liệu liên quan, validation thời gian/sức chứa và rollback khi lưu khách mời thất bại. Khóa JWT test riêng được nạp tự động. Dữ liệu fixture auth/sự kiện được dọn sau mỗi test; các test database dùng transaction rollback hoặc schema tạm riêng.

Nếu không dùng Docker, tạo **database test riêng, trống** và cung cấp thông tin rõ ràng:

```powershell
$env:TEST_DB_URL = "jdbc:postgresql://localhost:5432/eventhub_test"
$env:TEST_DB_USERNAME = "postgres"
$testCredential = Get-Credential -UserName postgres -Message "Test PostgreSQL credentials"
$env:TEST_DB_PASSWORD = $testCredential.GetNetworkCredential().Password
.\mvnw.cmd "-Dtest.database.mode=external" clean verify
```

Chế độ external giữ lại schema và lịch sử Flyway để dùng lại; không trỏ `TEST_DB_URL` tới database nghiệp vụ.

## 3. Cấu trúc thư mục và nơi đặt mã nguồn

Cấu trúc tổ chức của dự án như sau. Thư mục chưa tồn tại trong checkout có thể được tạo khi bắt đầu triển khai phần tương ứng.

```text
eventhub-backend/
├── .mvn/                         # Cấu hình Maven Wrapper
├── .gitignore
├── mvnw                          # Maven Wrapper cho macOS/Linux
├── mvnw.cmd                      # Maven Wrapper cho Windows
├── pom.xml                       # Dependency và cấu hình build
├── README.md
├── src/
│   ├── main/
│   │   ├── java/com/eventhub/backend/
│   │   │   ├── config/
│   │   │   ├── controller/
│   │   │   ├── service/
│   │   │   │   └── impl/
│   │   │   ├── repository/
│   │   │   ├── entity/
│   │   │   ├── dto/
│   │   │   │   ├── request/
│   │   │   │   └── response/
│   │   │   ├── mapper/
│   │   │   ├── security/
│   │   │   ├── exception/
│   │   │   ├── enums/
│   │   │   ├── scheduler/
│   │   │   ├── integration/
│   │   │   ├── util/
│   │   │   └── EventhubBackendApplication.java
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── static/
│   │       └── templates/
│   └── test/java/com/eventhub/backend/
└── target/                       # Maven sinh ra khi build, không commit
```

Các đường dẫn trong bảng dưới đây tính từ `src/main/java/com/eventhub/backend/`:

| Thư mục | Đặt những gì ở đây? | Ví dụ |
| --- | --- | --- |
| `config/` | Cấu hình Spring, bean dùng chung, CORS | `WebConfig.java` |
| `controller/` | Endpoint, nhận request và trả HTTP response | `EventController.java` |
| `service/` | Xử lý nghiệp vụ; chỉ tách interface/implementation khi cần | `AuthService.java` |
| `repository/` | Truy vấn và lưu dữ liệu khi bổ sung tầng persistence | `EventRepository.java` |
| `entity/` | Đối tượng ánh xạ dữ liệu database khi bổ sung JPA | `Event.java`, `User.java` |
| `dto/request/` | Dữ liệu nhận từ client, quy tắc validation đầu vào | `CreateEventRequest.java` |
| `dto/response/` | Dữ liệu trả cho client | `EventResponse.java` |
| `mapper/` | Chuyển đổi giữa entity và DTO | `EventMapper.java` |
| `security/` | Cấu hình bảo mật, xác thực, phân quyền và filter | `SecurityConfig.java` |
| `exception/` | Exception nghiệp vụ và xử lý lỗi API tập trung | `ResourceNotFoundException.java`, `GlobalExceptionHandler.java` |
| `enums/` | Tập giá trị cố định trong miền nghiệp vụ | `EventStatus.java` |
| `integration/` | Client kết nối dịch vụ bên ngoài | `PaymentClient.java`, `EmailClient.java` |
| `util/` | Hàm tiện ích dùng chung, không chứa nghiệp vụ | `DateTimeUtils.java` |

`resources/static/` chứa tài nguyên tĩnh; `resources/templates/` chứa template render phía server nếu sử dụng. Backend chỉ cung cấp REST API có thể để trống hai thư mục này. Các bài kiểm thử nằm trong `src/test/java/com/eventhub/backend/`, tổ chức theo package tương ứng với mã nguồn.


### Luồng xử lý và quy ước code

```text
Client → Controller → Service → Repository → Database
```

- Controller nhận DTO, kiểm tra đầu vào, gọi service và trả response; không đặt nghiệp vụ hoặc truy vấn database tại đây.
- Service xử lý nghiệp vụ, phối hợp repository và các dịch vụ ngoài. Đặt ranh giới transaction tại service khi đã tích hợp persistence.
- Repository tập trung vào truy cập dữ liệu. Không trả entity trực tiếp từ API; dùng response DTO để kiểm soát trường được công khai.
- Mapper chỉ chuyển đổi dữ liệu; không đặt nghiệp vụ trong mapper hoặc util.
- Đặt class mới dưới package `com.eventhub.backend` để nằm trong phạm vi quét mặc định của ứng dụng.
- Tên class dùng `PascalCase`, method và biến dùng `camelCase`, hằng số dùng `UPPER_SNAKE_CASE`, package dùng chữ thường. Dùng tên tiếng Anh có ý nghĩa.
- Ưu tiên constructor injection; xử lý lỗi thống nhất qua `exception/`, không bỏ qua exception âm thầm.
- Thêm dependency vào `pom.xml` khi tính năng thực sự cần, chẳng hạn JPA, validation hoặc Spring Security. Thư mục có sẵn không đồng nghĩa dependency đã được cài.
- Khi thêm hoặc sửa nghiệp vụ, bổ sung kiểm thử cho hành vi chính và trường hợp lỗi liên quan; cập nhật tài liệu API nếu có thay đổi.

## 4. Quy tắc Git của nhóm

### Vai trò các nhánh

| Nhánh | Mục đích | Cách cập nhật |
| --- | --- | --- |
| `main` | Phiên bản ổn định dùng để phát hành hoặc bàn giao | Người phụ trách tạo PR từ `develop` vào `main` sau khi kiểm tra |
| `develop` | Tích hợp công việc của các thành viên | Nhận PR đã review từ các nhánh công việc |
| `feature/...` | Phát triển một tính năng | Tạo từ `develop`, gửi PR vào `develop` |
| `fix/...` | Sửa một lỗi | Tạo từ `develop`, gửi PR vào `develop` |
| `docs/...` | Cập nhật tài liệu | Tạo từ `develop`, gửi PR vào `develop` |
| `refactor/...`, `chore/...` | Cải tổ code hoặc chỉnh cấu hình, công cụ | Tạo từ `develop`, gửi PR vào `develop` |

**Quy tắc bắt buộc:**

1. Không code trực tiếp hoặc push trực tiếp vào `main` và `develop` trong công việc hằng ngày.
2. Luôn tạo nhánh công việc từ `develop` đã được cập nhật, không tạo từ `main` hoặc nhánh tính năng khác.
3. Mỗi nhánh tập trung vào một công việc. Thống nhất người phụ trách để tránh hai người làm trùng tính năng.
4. Push lên nhánh công việc của mình rồi tạo pull request (PR) với nhánh đích là `develop`.
5. Cần ít nhất một thành viên khác review và approve; xử lý góp ý, conflict và kiểm tra thất bại trước khi merge.
6. Không force push, xóa nhánh dùng chung hoặc sửa lịch sử nhánh của người khác.
7. Chỉ xóa nhánh tính năng sau khi PR đã **merge** và không còn công việc cần giữ. Xóa nhánh không xóa mã nguồn đã được merge vào `develop`.
8. Không commit mật khẩu, token, dữ liệu riêng tư, `target/` hoặc cấu hình IDE cá nhân. Kiểm tra nội dung staged trước mỗi commit.

### Đặt tên nhánh

Dùng chữ thường không dấu và dấu gạch ngang. Nếu có mã issue, đưa mã đó vào tên nhánh:

```text
feature/12-create-event
feature/user-login
fix/event-date-validation
docs/update-readme
chore/configure-database
refactor/event-service
```

## 5. Quy trình làm việc cho từng thành viên

Ví dụ dưới đây dùng nhánh `feature/create-event`. Thay tên này bằng nhánh của công việc bạn được giao. 

### Bước 1: Cập nhật develop và tạo nhánh

Trước khi chuyển nhánh, chạy `git status`. Nếu có thay đổi chưa commit, lưu trên đúng nhánh đang làm hoặc dùng stash theo mục 7.

```shell
git switch develop
git pull --ff-only origin develop
git switch -c feature/create-event
git branch --show-current
```

`git switch -c` tương đương `git checkout -b`. Nếu `pull --ff-only` báo hai nhánh đã phân kỳ, dừng và kiểm tra lịch sử với người phụ trách; không dùng reset hoặc force push để bỏ qua lỗi.

### Bước 2: Code, kiểm tra và commit

Chạy kiểm tra trước khi đưa thay đổi lên review:

```shell
git status
git diff
git add .
git diff --cached
git commit -m "feat(event): add event creation endpoint"
```
### Bước 3: Đồng bộ với develop trước khi gửi PR

Thực hiện khi đang ở nhánh tính năng và working tree sạch:

```shell
git fetch origin
git merge origin/develop
```

Nếu có conflict, xử lý theo mục 7. Sau khi merge, chạy lại kiểm tra khi có thay đổi mã nguồn, rồi push:

```shell
git push -u origin feature/create-event
```

### Bước 4: Tạo pull request vào develop

Trên GitHub, mở pull request với:

- **Base:** `develop`.
- **Compare:** `feature/create-event`.
- **Title:** mô tả ngắn thay đổi, ví dụ `feat(event): add event creation endpoint`.
- **Reviewer:** ít nhất một thành viên khác trong nhóm.

Nội dung PR nên có:

```markdown
## Thay đổi
- Thêm API tạo sự kiện.
- Kiểm tra dữ liệu đầu vào và trả response DTO.

## Cách kiểm tra
- Lệnh đã chạy và kết quả thực tế.
- Các trường hợp thành công/thất bại đã kiểm tra.

## Ảnh hưởng
- Thay đổi API, database, dependency hoặc cấu hình nếu có.

## Issue liên quan
- Mã issue hoặc đường dẫn công việc nếu có.
```

Chỉ ghi kiểm tra đã thực sự chạy. Có thể mở Draft PR khi cần trao đổi sớm. Khi reviewer yêu cầu sửa, commit và push tiếp vào **cùng nhánh**; PR tự cập nhật. Không cần tạo PR mới cho mỗi lần sửa.

### Bước 5: Review và merge

Người review kiểm tra tính đúng đắn, cấu trúc code, trường hợp lỗi và kiểm thử. Người được nhóm phân công merge sau khi PR được approve, hết conflict và các kiểm tra bắt buộc đều thành công.

Trong lúc chờ review, nếu `develop` có thay đổi liên quan, đồng bộ lại như bước 3. Việc PR được approve hoặc đóng chưa đồng nghĩa đã merge; kiểm tra trạng thái **Merged** trước khi dọn nhánh.

### Bước 6: Xóa nhánh đã hoàn thành

Sau khi PR đã merge, chuyển về `develop`, lấy mã mới và xóa nhánh local:

```shell
git switch develop
git pull --ff-only origin develop
git branch -d feature/create-event
```

Xóa nhánh remote bằng nút **Delete branch** trên PR đã merge; hoặc, nếu nhánh remote vẫn còn, dùng:

```shell
git push origin --delete feature/create-event
git fetch --prune origin
```

Chỉ xóa nhánh thuộc công việc đã hoàn thành của mình. Nếu Git báo nhánh chưa merge, kiểm tra PR và các commit còn lại. Squash/rebase merge có thể khiến Git không nhận diện nhánh đã tích hợp; không đổi sang `-D` máy móc. Chỉ dùng `git branch -D feature/create-event` sau khi xác nhận PR đã merge, nội dung cần giữ đã có trên `develop` và nhánh không có commit mới cần giữ.

Bắt đầu công việc tiếp theo bằng một nhánh mới từ `develop`, không tái sử dụng nhánh đã merge.

## 6. Quy ước commit message

Viết commit message bằng tiếng Anh, ngắn gọn và mô tả việc đã thay đổi:

```text
type(scope): description
```

| Type | Khi sử dụng | Ví dụ |
| --- | --- | --- |
| `feat` | Thêm tính năng | `feat(event): add event creation endpoint` |
| `fix` | Sửa lỗi | `fix(event): reject invalid event dates` |
| `docs` | Sửa tài liệu | `docs: add development and Git workflow guide` |
| `refactor` | Cải tổ code, giữ nguyên hành vi | `refactor(event): extract response mapping` |
| `test` | Thêm hoặc sửa kiểm thử | `test(event): cover event creation validation` |
| `chore` | Cấu hình, công cụ, bảo trì dự án | `chore: initialize Spring Boot backend project structure` |

Tránh message như `update`, `fix bug`, `done`, `abc` vì không giúp người khác hiểu thay đổi.

## 7. Checklist trước khi yêu cầu review

- [ ] Nhánh công việc được tạo từ `develop` và đã tích hợp thay đổi liên quan mới nhất.
- [ ] Mã nguồn nằm đúng package, không gộp công việc ngoài phạm vi.
- [ ] Đã kiểm tra `git diff` và nội dung staged, không đưa bí mật hoặc file build lên Git.
- [ ] Đã chạy kiểm tra phù hợp; thay đổi mã nguồn phải vượt qua `clean verify`.
- [ ] Đã thêm/cập nhật test cho nghiệp vụ thay đổi và tài liệu liên quan.
- [ ] Commit message rõ ràng, bằng tiếng Anh.
- [ ] PR có base là `develop`, mô tả thay đổi và kết quả kiểm tra thực tế.
- [ ] Đã chọn reviewer; chỉ dọn nhánh sau khi PR được merge.
