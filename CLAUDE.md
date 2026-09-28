# Hướng dẫn AI làm việc với EventHub Backend

Áp dụng cho các tác vụ trong repository này. Đọc [README.md](README.md), `pom.xml` và mã nguồn liên quan trước khi sửa. Ưu tiên yêu cầu cụ thể của người dùng trong tác vụ hiện tại; nếu yêu cầu mâu thuẫn với quy ước dưới đây, nêu rõ điều chỉnh cần thiết.

## 1. Bối cảnh dự án

- Backend dùng Java 21, Spring Boot và Maven Wrapper; lấy phiên bản và dependency thực tế từ `pom.xml`.
- Package gốc: `com.eventhub.backend`.
- Điểm khởi chạy: `src/main/java/com/eventhub/backend/EventhubBackendApplication.java`.
- Cấu hình ứng dụng: `src/main/resources/application.properties`.
- Mã kiểm thử: `src/test/java/com/eventhub/backend/`.
- Không suy đoán tính năng đã tồn tại dựa trên tên thư mục. Kiểm tra mã nguồn trước khi sử dụng database, JPA, validation, security, Lombok hoặc MapStruct.
- README mô tả quy trình nhóm; file này hướng dẫn AI thực hiện công việc. Giữ hai tài liệu nhất quán khi thay đổi quy ước.

## 2. Cách tiếp cận công việc

1. Đọc yêu cầu, chạy `git status --short` và kiểm tra nhánh hiện tại. Xác định thay đổi có sẵn của người dùng trước khi chỉnh sửa.
2. Tìm mã nguồn, test và cấu hình liên quan; ưu tiên `rg` để tìm kiếm. Tái sử dụng cách làm đã có nếu phù hợp.
3. Thực hiện thay đổi nhỏ nhất đáp ứng đầy đủ yêu cầu. Không tự mở rộng tính năng, nâng cấp framework hoặc refactor phần không liên quan.
4. Tự quyết định các chi tiết triển khai thông thường. Chỉ hỏi khi thiếu thông tin ảnh hưởng đến nghiệp vụ, hợp đồng API, dữ liệu hoặc phạm vi được phép thực hiện.
5. Kiểm tra kết quả bằng các lệnh phù hợp. Xem lại diff để phát hiện file thừa, lỗi định dạng và thay đổi ngoài phạm vi.
6. Báo cáo ngắn gọn nội dung đã sửa, kết quả kiểm tra và phần còn vướng nếu có.

Không ghi đè, hoàn tác hoặc gom vào commit các thay đổi của người dùng không thuộc nhiệm vụ. Không tự tạo yêu cầu nghiệp vụ, trạng thái, vai trò hoặc quy tắc phân quyền khi chưa có căn cứ.

## 3. Quy tắc đặt tên

**Dùng `camelCase` cho biến, field, tham số, phương thức và thuộc tính JSON của API do dự án định nghĩa.** Các thành phần khác tuân theo quy ước Java và quy trình Git trong README:

| Thành phần | Quy tắc | Ví dụ |
| --- | --- | --- |
| Biến, field, tham số | `camelCase` | `eventId`, `eventName`, `startTime` |
| Phương thức | `camelCase`, thường bắt đầu bằng động từ | `createEvent`, `findEventById`, `validateEventDates` |
| Thuộc tính boolean | Tên thể hiện điều kiện, trạng thái | `active`, `published`, `hasAvailableTickets` |
| Phương thức kiểm tra boolean | `is...`, `has...`, `can...` | `isActive`, `hasPermission`, `canRegister` |
| Thuộc tính JSON, query/path parameter | `camelCase` | `eventId`, `pageSize`, `{eventId}` |
| Class, interface, record, enum | `PascalCase` | `EventController`, `EventService`, `CreateEventRequest`, `EventStatus` |
| File Java | Trùng tên public type, dùng `PascalCase` | `EventController.java` |
| Hằng số và phần tử enum | `UPPER_SNAKE_CASE` | `MAX_PAGE_SIZE`, `DRAFT`, `PUBLISHED` |
| Package | Chữ thường, không gạch dưới | `com.eventhub.backend.service.impl` |
| Phương thức test | `camelCase`, mô tả hành vi và điều kiện | `shouldRejectEventWhenEndTimeIsBeforeStartTime` |
| Đường dẫn REST | Danh từ số nhiều, đoạn nhiều từ dùng `kebab-case` | `/api/events`, `/api/event-categories` |
| Nhánh Git | Prefix và mô tả dùng `kebab-case` theo README | `feature/create-event`, `fix/event-validation` |

- Dùng tiếng Anh có nghĩa cho tên trong mã nguồn; không dùng tên tiếng Việt không dấu hoặc tên mơ hồ như `data1`, `temp2`, `handleStuff`.
- Từ viết tắt được viết như một từ: `userId`, `apiUrl`, `CreateEventRequest`; tránh `userID`, `APIUrl`, `CreateEventDTO` cho tên mới.
- DTO đầu vào có hậu tố `Request`, DTO đầu ra có hậu tố `Response`; implementation của service có hậu tố `Impl`.
- Không thêm tiền tố `I` vào interface hoặc hậu tố `Entity` cho mọi entity khi không có nhu cầu phân biệt.
- Với database, dùng quy ước schema/migration hiện có; không tự đổi tên bảng/cột để ép sang camelCase. Tên thuộc tính Java vẫn dùng camelCase và ánh xạ khi cần.
- Giữ nguyên tên bắt buộc từ framework, biến môi trường và hợp đồng dịch vụ ngoài. Không đổi tên API hoặc schema đang được sử dụng chỉ để áp dụng quy tắc mới.

Ví dụ tên hợp lệ:

```java
public interface EventService {
    EventResponse findEventById(Long eventId);
    EventResponse createEvent(CreateEventRequest request);
}
```

## 4. Tổ chức mã nguồn và trách nhiệm từng tầng

Đặt mã nguồn dưới `src/main/java/com/eventhub/backend/`:

| Package | Trách nhiệm |
| --- | --- |
| `controller` | Nhận HTTP request, validation đầu vào, gọi service, trả response |
| `service` | Interface các thao tác nghiệp vụ |
| `service.impl` | Triển khai nghiệp vụ và ranh giới transaction khi có persistence |
| `repository` | Truy vấn và lưu dữ liệu |
| `entity` | Đối tượng ánh xạ database khi sử dụng JPA |
| `dto.request`, `dto.response` | Hợp đồng dữ liệu vào và ra của API |
| `mapper` | Chuyển đổi DTO và entity, không xử lý nghiệp vụ |
| `config` | Bean và cấu hình chung |
| `security` | Xác thực, phân quyền, filter và cấu hình bảo mật |
| `exception` | Exception nghiệp vụ và xử lý lỗi tập trung |
| `enums` | Tập giá trị cố định thuộc nghiệp vụ |
| `integration` | Kết nối dịch vụ bên ngoài |
| `util` | Tiện ích dùng chung, không chứa nghiệp vụ |

- Luồng chính: controller → service → repository. Controller không gọi repository trực tiếp.
- Trả response DTO từ API, không công khai entity hoặc các trường nội bộ.
- Dùng constructor injection; dependency thường là field `private final`. Không dùng field injection bằng `@Autowired`.
- Không tạo lớp trung gian, generic base service hoặc tiện ích dùng chung chỉ để phục vụ một thao tác đơn giản.
- Một class nên có trách nhiệm rõ ràng. Tách phương thức khi giúp hiểu nghiệp vụ hoặc tái sử dụng thực sự.
- Theo định dạng đang có trong file; không format toàn bộ repository khi sửa một tính năng.
- Comment giải thích lý do hoặc ràng buộc khó thấy. Không thêm comment chỉ lặp lại tên lệnh.
- Chỉ tạo package khi cần dùng. Có thể xóa `.gitkeep` trong thư mục đã có mã nguồn được theo dõi; không sửa nội dung `target/`.

## 5. API, validation và xử lý lỗi

- Kiểm tra endpoint, DTO và quy ước response hiện có trước khi thêm API. Giữ tương thích trừ khi tác vụ yêu cầu thay đổi hợp đồng.
- Dùng HTTP method và status code phù hợp; không trả HTTP 200 cho mọi lỗi.
- Validation hình thức đầu vào nằm ở request DTO/controller; validation nghiệp vụ nằm ở service.
- Chỉ dùng annotation validation khi đã có dependency tương ứng. Khi bổ sung dependency cần thiết, ghi rõ mục đích.
- Phân biệt lỗi đầu vào, không tìm thấy, chưa xác thực, thiếu quyền và xung đột nghiệp vụ. Tận dụng handler tập trung trong `exception/`.
- Không bắt `Exception` chung rồi bỏ qua, trả `null` hoặc response thành công. Không trả stack trace hay thông tin cấu hình nội bộ cho client.
- API danh sách có thể tăng lớn cần phân trang và giới hạn kích thước hợp lý theo yêu cầu.
- Xác định rõ múi giờ khi xử lý thời gian. Với tiền, dùng `BigDecimal` và đơn vị tiền tệ rõ ràng; không dùng `float`/`double`.

## 6. Database, bảo mật và dịch vụ ngoài

Áp dụng khi nhiệm vụ có sử dụng các thành phần này; không tự cài thêm chỉ vì có hướng dẫn ở đây.

- Không tự thay đổi schema, xóa dữ liệu hoặc chạy migration trên môi trường dùng chung ngoài phạm vi được giao. Thay đổi schema cần migration phù hợp với công cụ dự án và mô tả ảnh hưởng.
- Không dùng cấu hình tự tạo/xóa schema cho môi trường triển khai để xử lý lỗi migration.
- Với JPA, đặt transaction ở tầng service; chú ý lazy loading, truy vấn N+1 và truy vấn không giới hạn. Tránh chuyển toàn bộ quan hệ sang eager để chữa lỗi truy vấn.
- Với thao tác đồng thời như đăng ký vé hoặc cập nhật số lượng còn lại, kiểm tra tính nhất quán và ràng buộc database; không chỉ dựa vào một lần kiểm tra trong bộ nhớ.
- Không đưa mật khẩu, token, khóa API hoặc dữ liệu cá nhân vào code, log, test fixture hay tài liệu. Dùng biến môi trường và cấu hình mẫu không có bí mật thật.
- Không đọc hoặc in nội dung file bí mật nếu không cần cho nhiệm vụ. Nếu cần file cấu hình cục bộ, kiểm tra `.gitignore` trước khi tạo và stage.
- Không tắt xác thực, phân quyền, validation hoặc mở CORS cho mọi nguồn để làm test vượt qua. Cấu hình bảo mật phải phù hợp kiểu client và môi trường.
- Khi có tài khoản người dùng, dùng cơ chế băm mật khẩu của thư viện bảo mật; không lưu mật khẩu thuần văn bản và không tự viết thuật toán mã hóa.
- Kiểm tra quyền truy cập tài nguyên ở backend; không tin `userId`, vai trò hoặc quyền do client tự gửi.
- Client gọi dịch vụ ngoài cần timeout và xử lý lỗi. Không retry mù quáng thao tác tạo thanh toán hoặc thao tác có tác dụng phụ; xem xét tính idempotent.
- Test không được gửi email, tạo thanh toán thật hoặc phụ thuộc dịch vụ production. Dùng mock hoặc môi trường test đã được cấu hình.

## 7. Dependency và cấu hình

- Kiểm tra `pom.xml` trước khi import API mới. Không giả định Lombok, MapStruct, JPA, Spring Security hoặc thư viện JWT đã có.
- Ưu tiên phiên bản do Spring Boot quản lý; không tự nâng Java, Spring Boot, Maven Wrapper hoặc override phiên bản dependency ngoài phạm vi tác vụ.
- Chỉ bổ sung thư viện phục vụ trực tiếp yêu cầu. Không thêm cả một framework cho công việc có thể giải quyết đơn giản bằng thành phần sẵn có.
- Khi tra cứu API thư viện, đối chiếu tài liệu chính thức với phiên bản dự án; không sao chép ví dụ không tương thích.
- Không đổi cấu hình dùng chung chỉ để phù hợp đường dẫn hoặc môi trường cá nhân. Ghi lại biến môi trường mới và cách chạy trong README khi cần.

## 8. Kiểm thử và kiểm tra trước khi bàn giao

Chạy lệnh từ thư mục chứa `pom.xml`:

| Mục đích | Windows PowerShell | macOS / Linux |
| --- | --- | --- |
| Chạy test | `.\mvnw.cmd test` | `./mvnw test` |
| Chạy một lớp test | `.\mvnw.cmd "-Dtest=EventServiceTest" test` | `./mvnw -Dtest=EventServiceTest test` |
| Kiểm tra đầy đủ thay đổi mã nguồn | `.\mvnw.cmd clean verify` | `./mvnw clean verify` |
| Chạy ứng dụng khi cần kiểm tra thủ công | `.\mvnw.cmd spring-boot:run` | `./mvnw spring-boot:run` |

Tên `EventServiceTest` là ví dụ; thay bằng lớp test có thật liên quan đến tác vụ.

- Thêm hoặc cập nhật test khi thay đổi nghiệp vụ, validation, phân quyền hoặc sửa lỗi. Kiểm tra hành vi quan sát được, gồm trường hợp thành công, trường hợp lỗi và điều kiện biên liên quan.
- Ưu tiên unit test cho nghiệp vụ, test tầng web cho hợp đồng API, integration test khi cần kiểm tra tương tác persistence hoặc nhiều thành phần. Không dùng `@SpringBootTest` cho mọi bài test.
- Giữ test độc lập và có kết quả ổn định; tránh phụ thuộc thứ tự chạy, thời gian thực hoặc dữ liệu production.
- Không xóa test, bỏ assertion hoặc skip test để che lỗi. Phân biệt lỗi có sẵn, lỗi môi trường và lỗi do thay đổi hiện tại.
- Với thay đổi mã nguồn, chạy test phù hợp và `clean verify` trước bàn giao khi môi trường cho phép. Không chạy lặp lại khi mã nguồn không đổi và kết quả đã đủ.
- Với thay đổi chỉ có tài liệu, kiểm tra nội dung, đường dẫn, khối mã Markdown và `git diff --check`; không cần chạy lại ứng dụng.
- Chỉ nói test/build thành công khi đã chạy và thấy kết quả. Nếu không chạy được, nêu lệnh, lý do và phần chưa xác minh.

## 9. Quy tắc Git dành cho AI

- Tuân thủ quy trình chi tiết trong README: nhánh công việc từ `develop`, PR vào `develop`, không push trực tiếp vào `main` hoặc `develop`.
- Kiểm tra nhánh và working tree trước khi chuyển nhánh. Dùng nhánh công việc hiện tại nếu đúng nhiệm vụ; không tự di chuyển các thay đổi chưa commit của người dùng.
- Khi bắt đầu công việc mới với working tree sạch, lấy `develop` mới nhất bằng fast-forward rồi tạo nhánh theo README. Nếu chưa có `develop` hoặc có thay đổi khiến việc chuyển nhánh không an toàn, giải thích tình trạng và thống nhất cách giữ công việc trước khi thao tác Git có ảnh hưởng.
- Yêu cầu sửa file không mặc nhiên là yêu cầu commit, push, tạo PR, merge hoặc xóa nhánh. Thực hiện các bước này khi nằm trong yêu cầu hoặc đã được người dùng cho phép; không hỏi lại việc đã được cho phép.
- Trước khi commit, xem diff và stage rõ từng file thuộc nhiệm vụ. Không dùng `git add .` để gom thay đổi không liên quan.
- Commit message bằng tiếng Anh theo `type(scope): description`; ví dụ `feat(event): add event creation endpoint` hoặc `docs: add AI development guidelines`.
- Không tự chạy `git reset --hard`, `git clean -fd`, force push hoặc ghi đè lịch sử để xử lý lỗi. Không sửa/xóa nhánh của người khác.
- Không tự merge PR của mình. Tuân thủ yêu cầu review và kiểm tra của nhóm.
- Chỉ dọn nhánh khi được giao, PR đã ở trạng thái Merged và không có commit mới cần giữ. Ưu tiên `git branch -d`; không tự chuyển sang `-D` khi Git từ chối.
- Xóa nhánh sau merge là dọn tham chiếu Git; giữ nguyên mã nguồn tính năng đã tích hợp vào `develop`.

## 10. Cách giao tiếp và báo cáo

- Trả lời bằng tiếng Việt, trừ khi người dùng yêu cầu ngôn ngữ khác. Tên trong code và commit message dùng tiếng Anh.
- Thông báo ngắn trước thay đổi đáng kể; cập nhật tiến độ khi công việc kéo dài hoặc phát hiện vấn đề ảnh hưởng kết quả.
- Khi hoàn tất, nêu: đã thay đổi gì, file chính, kiểm tra đã chạy và giới hạn còn lại. Không khẳng định đã commit, push, triển khai hoặc hoàn thành tính năng nếu chưa thực hiện.
- Nếu thay đổi cách chạy, cấu hình, API hoặc quy trình nhóm, cập nhật tài liệu liên quan trong cùng nhiệm vụ.
- Không tạo file báo cáo, dependency, cấu hình CI hoặc chức năng ngoài yêu cầu chỉ để làm kết quả trông đầy đủ hơn.
