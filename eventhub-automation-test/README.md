# EventHub Selenium Automation Testing Framework

Framework kiểm thử tự động giao diện (UI Automation Testing) toàn diện cho hệ thống **EventHub**, sử dụng **Java 21, Selenium WebDriver 4.x, TestNG và mô hình Page Object Model (POM)**.

> **Quy chuẩn thiết kế:** 
> - **Mỗi 1 Test Case là 1 file Java độc lập** (tương ứng với Test Case ID trong các file tài liệu Markdown `test/UC*.md`).
> - **Chế độ quan sát trực quan**: Tự động làm chậm mỗi thao tác 2 giây (`step.delay=2000`) và viền đỏ làm nổi bật phần tử (`highlightElement`).

---

## 1. Cấu Trúc Dự Án

```text
eventhub-automation-test/
├── pom.xml                                   # Khai báo Selenium 4.25.0, TestNG 7.10.2, SLF4J
├── testng.xml                                # Cấu hình Test Suite chạy toàn bộ hoặc từng phân hệ
├── mvnw / mvnw.cmd                           # Maven Wrapper (Java 21)
└── src/
    ├── main/
    │   ├── resources/
    │   │   └── config.properties             # URL, browser, step.delay=2000 (chậm 2s), tài khoản test
    │   └── java/com/eventhub/automation/
    │       ├── config/
    │       │   └── ConfigurationManager.java # Đọc cấu hình môi trường & tài khoản
    │       ├── driver/
    │       │   └── DriverManager.java        # Quản lý WebDriver (Chrome, Edge, Headless)
    │       ├── pages/                        # Page Objects (Định vị Selector & Thao tác UI)
    │       │   ├── BasePage.java             # Hàm dùng chung: highlight viền đỏ, delay 2s, wait, click...
    │       │   ├── LoginPage.java            # Màn hình Đăng nhập (UC02)
    │       │   ├── RegisterPage.java         # Màn hình Đăng ký (UC01)
    │       │   ├── CustomerHeaderComponent.java # Header, Dropdown menu & Modal đăng xuất (UC03)
    │       │   ├── ProfilePage.java          # Màn hình Hồ sơ & Chỉnh sửa thông tin cá nhân (UC04)
    │       │   ├── ExplorePage.java          # Màn hình Tìm kiếm & Lọc sự kiện (UC05, UC06)
    │       │   ├── EventDetailPage.java      # Màn hình Chi tiết sự kiện & Nút mua vé
    │       │   ├── SelectTicketsPage.java    # Màn hình Chọn loại vé & Tạm tính (UC07)
    │       │   ├── BookingCheckoutPage.java  # Màn hình Xác nhận đặt vé & Thanh toán (UC07)
    │       │   ├── BookingHistoryPage.java   # Màn hình Lịch sử đặt vé (UC08)
    │       │   ├── BookingDetailPage.java    # Màn hình Chi tiết đơn hàng booking (UC08)
    │       │   ├── MyTicketsPage.java        # Màn hình Vé điện tử của tôi & Modal QR (UC09)
    │       │   ├── NotificationsPage.java    # Màn hình Quản lý thông báo người dùng (UC10)
    │       │   ├── CreateEventPage.java      # Màn hình Tạo sự kiện mới - Wizard 3 bước (UC12)
    │       │   ├── StaffDispatchPage.java    # Màn hình Điều phối & Phân công Staff (UC13)
    │       │   ├── OrganizerEventsPage.java  # Màn hình Sự kiện của tôi & Hủy sự kiện (UC14)
    │       │   ├── OrganizerWalletPage.java  # Màn hình Ví tài chính & Rút tiền (UC15)
    │       │   ├── StaffDashboardPage.java   # Màn hình Bảng làm việc của Staff (UC16)
    │       │   ├── StaffEventTicketsPage.java# Màn hình Danh sách vé soát của Staff (UC16)
    │       │   ├── AdminUsersPage.java       # Màn hình Quản trị người dùng (UC17)
    │       │   ├── AdminCategoriesPage.java  # Màn hình Quản trị danh mục sự kiện (UC18)
    │       │   ├── AdminPendingEventsPage.java# Màn hình Danh sách sự kiện chờ duyệt (UC19)
    │       │   ├── AdminEventReviewPage.java # Màn hình Phê duyệt & Từ chối sự kiện (UC19)
    │       │   ├── AdminRefundsPage.java     # Màn hình Danh sách sự kiện đã hủy (UC20)
    │       │   ├── AdminRefundDetailPage.java# Màn hình Đối soát & Lịch sử hoàn tiền (UC20)
    │       │   ├── AdminWithdrawalsPage.java # Màn hình Quản lý yêu cầu rút tiền (UC21)
    │       │   ├── AdminWithdrawalDetailPage.java # Màn hình Duyệt chi trả rút tiền (UC21)
    │       │   ├── OrganizerReportsPage.java # Màn hình Báo cáo & Thống kê Ban tổ chức (UC22)
    │       │   ├── EditEventPage.java        # Màn hình Chỉnh sửa thông tin sự kiện (UC23)
    │       │   └── AdminDashboardPage.java   # Màn hình Thống kê tổng quan Admin (UC24)
    │       └── utils/
    │           ├── DataHelper.java           # Sinh Email, SĐT, Tên ngẫu nhiên
    │           └── DatabaseCleaner.java      # Snapshot & Rollback DB tự động sau mỗi test
    └── test/
        └── java/com/eventhub/automation/
            ├── BaseTest.java                 # Khởi tạo browser, chụp ảnh khi fail, rollback DB
            └── tests/                        # 177 TEST CASE ĐỘC LẬP
                ├── CheckEnvironmentTest.java
                ├── uc01_register/            # 8 Test Cases
                ├── uc02_login/               # 8 Test Cases
                ├── uc03_logout/              # 5 Test Cases
                ├── uc04_profile/             # 8 Test Cases
                ├── uc05_search/              # 7 Test Cases
                ├── uc06_filter/              # 8 Test Cases
                ├── uc07_booking/             # 9 Test Cases
                ├── uc08_bookings/            # 7 Test Cases
                ├── uc09_tickets/             # 6 Test Cases
                ├── uc10_notifications/       # 6 Test Cases
                ├── uc11_ai_chat/             # 8 Test Cases
                ├── uc12_create_event/        # 9 Test Cases
                ├── uc13_staff_dispatch/      # 6 Test Cases
                ├── uc14_cancel_event/        # 7 Test Cases
                ├── uc15_withdrawal/          # 8 Test Cases
                ├── uc16_checkin/             # 8 Test Cases
                ├── uc17_admin_users/         # 9 Test Cases
                ├── uc18_admin_categories/    # 8 Test Cases
                ├── uc19_event_approval/      # 8 Test Cases
                ├── uc20_refunds/             # 7 Test Cases
                ├── uc21_admin_payout/        # 8 Test Cases
                ├── uc22_organizer_reports/   # 4 Test Cases
                ├── uc23_edit_event/          # 3 Test Cases
                ├── uc24_admin_dashboard/     # 2 Test Cases
                └── uc25_change_password/     # 10 Test Cases
```

---

## 2. Hướng Dẫn Chạy Test (Cú Pháp Nhanh & Chuẩn)

`mvnw` / `mvnw.cmd` đã được tích hợp sẵn bộ phân giải lệnh thông minh ([`.mvn/test-cli.ps1`](file:///e:/Coding/Do_an/Code/eventhub-automation-test/.mvn/test-cli.ps1) và [`.mvn/test-cli.sh`](file:///e:/Coding/Do_an/Code/eventhub-automation-test/.mvn/test-cli.sh)) giúp chạy test ngắn gọn trực tiếp theo mã Use Case, mã Test Case hoặc vai trò:

```powershell
cd e:\Coding\Do_an\Code\eventhub-automation-test

# 0. Xem danh sách toàn bộ Use Case, số lượng test & hướng dẫn lệnh:
.\mvnw test list

# 1. Chạy toàn bộ test của 1 Use Case (không phân biệt hoa/thường):
.\mvnw test UC01
.\mvnw test UC07
.\mvnw test UC19

# 2. Chạy nhiều Use Case hoặc theo dải UC:
.\mvnw test UC01 UC02
.\mvnw test UC01-UC05

# 3. Chạy 1 Test Case cụ thể (chỉ cần gõ mã TC hoặc UCxx_yy):
.\mvnw test UC01_01
.\mvnw test TC_UC07_07
.\mvnw test UC02_01 UC04_02

# 4. Chạy theo Vai trò (Actor) / Nhóm nhanh:
.\mvnw test env          # Kiểm tra kết nối Backend, Frontend & Database
.\mvnw test smoke        # Chạy test kiểm tra nhanh (mỗi UC 1 test chính)
.\mvnw test customer     # Toàn bộ UC của Customer (UC01 - UC10)
.\mvnw test organizer    # Toàn bộ UC của Organizer (UC12 - UC15, UC22, UC23)
.\mvnw test staff        # Toàn bộ UC của Staff (UC16)
.\mvnw test admin        # Toàn bộ UC của Admin (UC17 - UC21, UC24)
.\mvnw test all          # Chạy toàn bộ test suite (testng.xml)

# 5. Tùy chọn kết hợp (không cần gõ -D dài dòng):
.\mvnw test UC01 headless            # Chạy ngầm Chrome (không hiện cửa sổ)
.\mvnw test UC01 fast                # Tắt delay giữa các bước (step.delay=0)
.\mvnw test UC01 headless fast       # Chạy ngầm siêu nhanh
.\mvnw test UC01 slow                # Chạy chậm 2s/bước để trình diễn/quan sát
.\mvnw test UC01 edge                # Đổi sang trình duyệt Edge
.\mvnw test UC01 no-rollback         # Tạm tắt rollback DB (nếu muốn giữ data để debug)
```

> **Lưu ý:** Cú pháp Maven truyền thống (như `.\mvnw test -Dtest=TC_UC02_01_LoginCustomerSuccessTest`) vẫn hoạt động bình thường. Lưu ý **UC11 (Chat với AI)** được loại trừ theo yêu cầu đồ án.
