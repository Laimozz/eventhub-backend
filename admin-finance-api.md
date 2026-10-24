# API tài chính Admin UC37–UC42

Các API này ghép với security, cookie HttpOnly và response lỗi đang có. Quyền: ADMIN ACTIVE. Request ghi cần header `X-CSRF-Protection: 1`. Danh sách dùng `items`, `page`, `pageSize`, `totalElements`, `totalPages`; page từ 0, pageSize 1–100, mặc định 20, ID giảm dần. Tiền VND/BigDecimal; datetime UTC không offset, frontend hiển thị GMT+7.

## UC37 thống kê

`GET /api/admin/statistics`, không có body/query. Response 200:

```json
{
  "totalEvents": 0,
  "totalUsers": 0,
  "totalTicketsSold": 0,
  "pendingBalance": 0,
  "availableBalance": 0,
  "withdrawalPendingBalance": 0,
  "totalWithdrawn": 0,
  "grossRevenue": 0,
  "netRevenue": 0,
  "totalRefunded": 0,
  "revenueUnavailableReason": null
}
```

Ví dụ mô tả DB trống, không phải dữ liệu production hardcode. Tất cả số lượng/số dư tính từ DB trong một query. Vé bán dùng tổng `quantity - remainingQuantity - reservedQuantity` theo convention API sự kiện. Các số dư là SUM cột tương ứng ở organizer_wallets; không được diễn giải tổng các số dư thành tiền thuộc hệ thống. grossRevenue là tổng payment SUCCESS (gồm khoản đang chờ hoàn); totalRefunded là tổng refund COMPLETED. netRevenue là tổng phí nền tảng 5% chụp tại thời điểm thanh toán, làm tròn HALF_UP đến đồng, trừ phí của khoản đã hoàn. Không tính từ giá vé hiện tại.

## UC39–UC40 danh sách và chi tiết

- `GET /api/admin/withdrawal-requests?status=PENDING&page=0&pageSize=20`: status tùy chọn PENDING/APPROVED/REJECTED/CANCELLED; bỏ status lấy tất cả. Không có body. Sai status/phân trang trả 400.
- `GET /api/admin/withdrawal-requests/{requestId}`: ID nguyên dương; không có body/query. Sai ID 400, không tồn tại 404.

Một phần tử gồm `id`, `organizer` (`id`, `fullName`, `email`), `amount`, `bankName`, `accountNumber`, `status`, `requestedAt`, `processedBy`, `processedAt`, `rejectReason`, `transactionCode`, `payouts`. List không công khai số tài khoản (`accountNumber=null`) và trả `payouts=[]`; detail Admin trả tài khoản cùng lịch sử payout. Trường xử lý chưa có trả null. Mỗi payout có `id`, `amount`, `status`, `transactionCode`, `failureReason`, `initiatedAt`, `completedAt`.

PENDING/APPROVED/REJECTED là tên kỹ thuật cho trạng thái Chờ duyệt/Đã duyệt/Bị từ chối của DOCX. Organizer tạo/hủy yêu cầu qua UC25/UC27; hủy dùng CANCELLED và trả khoản giữ về available. Tạo/hủy/duyệt/từ chối khóa ví để bảo toàn số dư. Không migrate hoặc tự suy diễn trạng thái cũ; yêu cầu trạng thái khác không được quyết định như PENDING.

## UC41 ghi nhận chuyển khoản đã thực hiện

`POST /api/admin/withdrawal-requests/{requestId}/approve`

```json
{ "transactionCode": "<mã ngân hàng của chuyển khoản đã thực hiện ngoài hệ thống>" }
```

Mã bắt buộc, trim, tối đa 255 ký tự. Admin chỉ gọi **sau khi chuyển khoản ngoài hệ thống**, theo hợp đồng API hiện tại được giữ lại trong kế hoạch đã duyệt. File phân công DOCX chỉ nêu tên UC41, không quy định cơ chế chuyển khoản. API không gửi lệnh chuyển tiền, không tự xác minh giao dịch tại ngân hàng.

Trong transaction: khóa request và ví; chỉ PENDING, khoản giữ đủ, chưa có payout cần đối soát; trừ withdrawalPendingBalance, cộng totalWithdrawn, giữ availableBalance; lưu request APPROVED, processedBy/processedAt từ principal/server, mã chuyển khoản, payout COMPLETED và notification WITHDRAWAL_APPROVED. initiatedAt/completedAt của payout là lúc ghi nhận tại hệ thống. Response 200 là chi tiết request cập nhật.

Webhook/retry chưa triển khai: thiếu URL nhận, xác thực, payload và outbox/delivery contract. Notification DB không thay webhook.

## UC42 từ chối

`POST /api/admin/withdrawal-requests/{requestId}/reject`

```json
{ "reason": "Thông tin ngân hàng cần được kiểm tra lại." }
```

Lý do bắt buộc, trim, tối đa 255 ký tự. Khóa và kiểm tra như duyệt. Trừ withdrawalPendingBalance, cộng availableBalance, giữ totalWithdrawn; lưu REJECTED, lý do/người/thời gian xử lý và notification WITHDRAWAL_REJECTED. Response 200 là chi tiết cập nhật.

Lỗi ghi dữ liệu hoặc notification rollback toàn bộ quyết định/số dư/payout. Request đã quyết định hoặc gửi lặp trả 409, không tạo thêm tác dụng phụ. Unique index V7 bảo vệ mã chuyển khoản không null trên payout cả khi cạnh tranh giữa hai request.

## UC38 hoàn tiền và đối soát

Organizer hủy trước startTime: chuyển CANCELED, ngừng bán, giải phóng giữ vé và vô hiệu vé VALID. Thao tác hủy chưa tạo hồ sơ refund và không gọi VNPAY. Luồng hoàn tiền có thể tạo hồ sơ riêng khi Admin chủ động xếp hàng hoàn cho sự kiện; luồng IPN thành công muộn/PAYMENT_REVIEW vẫn giữ cơ chế đối soát hiện có. FREE không gọi ngân hàng.

Các API dưới đây chỉ ADMIN ACTIVE được truy cập; POST/PUT cần `X-CSRF-Protection: 1`. ID trong nhóm `/refunds` là **paymentId**.

| Method và endpoint | Hành vi |
| --- | --- |
| GET `/api/admin/refund-events?page=0&pageSize=20` | Các sự kiện CANCELED. |
| GET `/api/admin/refund-events/{eventId}` | Lịch sử payment/refund của sự kiện; canRefund=true. Điều kiện từng khoản vẫn được kiểm tra khi thao tác. |
| GET `/api/admin/refunds?eventId=1&page=0&pageSize=20` | Danh sách hồ sơ, bỏ eventId lấy toàn bộ để hiển thị dashboard. |
| GET `/api/admin/refunds/{paymentId}` | Chi tiết, tài khoản đã xác minh, lịch sử attempt, hasEvidence. |
| POST `/api/admin/refund-events/{eventId}/refunds` | Xếp các khoản WAITING/VNPAY vào hàng đợi, trả queuedCount; không có body. |
| POST `/api/admin/refunds/{paymentId}/automatic` | Xếp một khoản vào hàng đợi; không có body. |
| POST `/api/admin/refunds/{paymentId}/reconcile` | Truy vấn VNPAY cho RECONCILIATION; không gửi lại lệnh hoàn. |
| PUT `/api/admin/refunds/{paymentId}/bank-account` | Lưu tài khoản gốc đã được Admin xác minh. |
| POST `/api/admin/refunds/{paymentId}/manual-reservation` | Giữ luồng thủ công trước khi chuyển khoản ngoài hệ thống. |
| POST `/api/admin/refunds/{paymentId}/manual-confirmation` | Multipart transactionCode + evidence; ghi nhận chuyển khoản đã thực hiện. |
| GET `/api/admin/refunds/{paymentId}/evidence` | Download chứng từ riêng, no-store, attachment. |

Body tài khoản:

```json
{"bankCode":"VCB","accountNumber":"0123456789","accountHolderName":"NGUYEN VAN A","originalAccountVerified":true}
```

Tài khoản lưu ở `refund_bank_accounts`, khóa chính payment_id, kèm verified_by/verified_at. Không dùng tài khoản rút tiền Organizer, không giả định mã ngân hàng VNPAY là số tài khoản. Chỉ Admin nhập/đọc sau khi xác minh với người mua. WAITING/FAILED mới được sửa tài khoản. Chứng từ PDF/PNG/JPEG tối đa 5 MB lưu BYTEA riêng, kiểm tra loại và magic bytes; không dùng URL công khai. Mã chuyển khoản hoàn thủ công là duy nhất trong refund_evidence.

Luồng tự động: WAITING → QUEUED → PROCESSING → COMPLETED / FAILED / RECONCILIATION. Timeout, chữ ký sai, kết quả chưa chắc chắn hoặc tiến trình gián đoạn không được coi là thất bại cuối; không tự gửi lại refund hoặc mở chuyển khoản thủ công. Worker khôi phục PROCESSING quá 10 phút sang RECONCILIATION. Chỉ phản hồi đã xác minh hoàn toàn bộ thành công mới hoàn tất; query giao dịch thanh toán gốc không chứng minh hoàn thành refund.

Luồng thủ công: WAITING/FAILED → MANUAL_PENDING → COMPLETED. Phải lưu tài khoản và giữ luồng trước khi chuyển tiền, sau đó nộp mã/chứng từ. MANUAL_PENDING khóa thao tác hoàn tự động và thay tài khoản. Không tự mở lại khoản này vì hệ thống không thể biết chuyển khoản ngoài ngân hàng đã xảy ra hay chưa. Gửi lại xác nhận đã COMPLETED không trừ tiền/thông báo lần nữa.

Hoàn tất đảo đúng phí/số tiền Organizer đã chụp theo payment, giảm pending nếu trước đó được ghi có, tạo thông báo REFUND_COMPLETED một lần. Payment SUCCESS được giữ làm lịch sử tiền đã thu. Khoản đã giải ngân bị chặn để đối soát riêng; không tự trừ tiền khả dụng của sự kiện khác. Các cuộc gọi provider nằm ngoài transaction giữ khóa DB.

## Nguồn tiền, worker và cấu hình

Payment thành công được chụp gross/phí 5%/phần Organizer vào payment_finances duy nhất theo payment. PAID cộng pending một lần; PAYMENT_REVIEW chưa cộng ví. Worker chỉ chuyển pending sang available sau endTime, sự kiện APPROVED/ONGOING/COMPLETED, đã cấp đủ vé VALID/USED, không có refund và chưa giải ngân/đảo tiền. GET ví không tạo ví hoặc suy ra số dư bằng 0 từ số vé hiện tại.

- Giữ vé mặc định 5 phút; grace nhận IPN 120 giây. Cấu hình hold hợp lệ 60–500 giây.
- `VNPAY_TMN_CODE`, `VNPAY_HASH_SECRET` dùng cùng merchant thanh toán; cấu hình `VNPAY_API_URL` và `VNPAY_API_IP_ADDRESS` cho query/refund.
- URL sandbox mặc định: `https://sandbox.vnpayment.vn/merchant_webapi/api/transaction`.
- `FINANCE_WORKER_ENABLED` mặc định true; `FINANCE_WORKER_DELAY` mặc định 30000 ms. Test đặt worker false.
- HTTP timeout kết nối 5 giây, đọc 20 giây. Không tự retry lệnh refund chưa rõ kết quả.

Contract tham chiếu [VNPAY querydr/refund](https://sandbox.vnpayment.vn/apis/docs/truy-van-hoan-tien/querydr%26refund.html). Unit/integration test dùng phản hồi giả lập, chưa chứng minh quyền query/refund của merchant; cần nghiệm thu sandbox với cấu hình thật trước vận hành.

## Mã lỗi

Giữ cấu trúc `{timestamp,status,code,message,path,errors}` hiện có.

| HTTP | Code | Trường hợp |
| --- | --- | --- |
| 400 | INVALID_ID / INVALID_PAGE / INVALID_STATUS / INVALID_REQUEST / VALIDATION_ERROR | Tham số/body sai. |
| 401 | UNAUTHORIZED | Thiếu/sai phiên hoặc user không ACTIVE. |
| 403 | FORBIDDEN | Sai role hoặc thiếu CSRF cho POST. |
| 404 | WITHDRAWAL_NOT_FOUND / EVENT_NOT_FOUND | Không có tài nguyên. |
| 409 | WITHDRAWAL_NOT_PENDING | Request đã xử lý hoặc không có trạng thái PENDING. |
| 409 | INSUFFICIENT_RESERVED_BALANCE | Số dư đang chờ rút không đủ. |
| 409 | PAYOUT_REQUIRES_RECONCILIATION | Request đã có payout, cần đối soát. |
| 409 | TRANSACTION_CODE_IN_USE | Mã chuyển khoản đã lưu, phát hiện ở service. |
| 409 | DATA_CONFLICT | Unique/FK vi phạm ở DB, kể cả cạnh tranh cùng mã chuyển khoản. |
| 409 | EVENT_NOT_CANCELED | Sự kiện chưa hủy chính thức. |

Lỗi 500 ngoài dự kiến theo cơ chế chung hiện có. Không đổi handler hoặc contract các API cũ.

## Migration và kiểm thử

V6 giữ migration booking; V7 thêm `uk_payout_transactions_transfer_reference`; V8 thêm payment_finances, refund_attempts, refund_bank_accounts, refund_evidence và trạng thái bổ sung trên refunds. Nếu dữ liệu cũ có mã trùng, dừng để đối soát; không xóa/gộp tự động. Không sửa V1–V5, không chạy migration vào DB nghiệp vụ để test.

`AdminFinanceIntegrationTests` kiểm tra trên PostgreSQL test riêng: phân quyền, list/detail, wallet math, duplicate decision, concurrency cùng request/cùng ví, unique transfer reference, rollback notification, canceled event/partial refund read-only. `DatabaseIntegrationTests` kiểm tra nâng V1 lên V8. Frontend test giả lập API nằm ở `tests/admin-finance.spec.ts`; production gọi API thật.

Kết quả 22/10/2026: backend clean verify 355 test đạt trên PostgreSQL tạm; thêm 2 ca cạnh tranh chạy trong BookingIntegrationTests (38/38 đạt). API multipart chứng từ cũng được kiểm thử qua MockMvc. Frontend build/lint đạt (còn cảnh báo cũ), các suite finance/Admin/booking/notification/auth/event/refresh được chạy và xử lý các lỗi phát hiện. Xem mục 13 của ../plan.md để biết từng lượt và giới hạn nghiệm thu sandbox.
