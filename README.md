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

Hiện tại dự án có bộ khung ứng dụng và bài kiểm thử khởi tạo Spring context. Chưa có API nghiệp vụ, cấu hình database, JPA, xác thực hay phân quyền. Các ví dụ tên class trong tài liệu là quy ước để phát triển tiếp, không có nghĩa là chức năng đó đã được triển khai.

## 2. Chuẩn bị và chạy dự án

### Yêu cầu

- Cài JDK 21 và Git.
- IDE có thể dùng IntelliJ IDEA, VS Code hoặc Eclipse; đặt SDK của dự án là Java 21.
- Có kết nối Internet trong lần build đầu để tải Maven và các dependency.
- Không bắt buộc cài Maven riêng vì dự án đã có `mvnw` và `mvnw.cmd`.

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

Ứng dụng mặc định chạy tại `http://localhost:8080`. Khi chưa có controller, truy cập `/` có thể trả về 404; kiểm tra log khởi động để xác nhận ứng dụng đang chạy. Dùng `Ctrl+C` để dừng.

Cấu hình chung đặt tại `src/main/resources/application.properties`. Khi bổ sung database hoặc dịch vụ ngoài, dùng biến môi trường cho mật khẩu và khóa bí mật; chỉ commit cấu hình mẫu không chứa giá trị thật. `.env` không được Spring Boot tự động đọc và hiện cũng chưa nằm trong `.gitignore`.

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
| `service/` | Interface mô tả các thao tác nghiệp vụ | `EventService.java` |
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
