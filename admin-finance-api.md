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

## UC38 hoàn tiền

Refund hiện chỉ là mô phỏng nội bộ, không gửi API VNPAY và không chuyển tiền ngân hàng. Thanh toán PAY/IPN vẫn dùng VNPAY. Không có cấu hình hoặc worker refund.

Organizer hủy sự kiện sẽ tạo hồ sơ WAITING cho payment SUCCESS có số tiền lớn hơn 0, hủy vé và giải phóng giữ chỗ trong cùng transaction. Admin xác nhận để xử lý. IPN thành công muộn tạo WAITING mới, không tự hoàn trước khi Admin xác nhận.

Các API chỉ cho ADMIN ACTIVE; POST cần `X-CSRF-Protection: 1`. ID trong `/refunds` là **paymentId**.

| Method và endpoint | Hành vi |
| --- | --- |
| GET `/api/admin/refunds?eventId=8&page=0&pageSize=20` | Danh sách phân trang; eventId tùy chọn. |
| GET `/api/admin/refunds/{paymentId}` | Chi tiết khoản hoàn. |
| POST `/api/admin/refunds/{paymentId}/complete` | Không có body; hoàn từng giao dịch ngay và trả chi tiết. |
| POST `/api/admin/refund-events/{eventId}/refunds` | Không có body; hoàn khoản đủ điều kiện của sự kiện CANCELED, trả `{refundedCount: 1}`. |

Chi tiết gồm `paymentId`, `amount`, `status`, `method`, `reference`, `refundedAt`, `paymentMethod`, `processedBy`, `eventId`, `eventCanceled`. Không có attempts hoặc mã phản hồi VNPAY. Kết quả mới là COMPLETED, method INTERNAL, mã RF-...; FE không hiển thị nhãn mô phỏng.

Cho phép xử lý khoản lỗi/chờ API cũ; COMPLETED là idempotent. Khóa sự kiện tuần tự hóa các lần hoàn đồng thời, bao gồm hoàn đơn lẻ và hàng loạt. Kiểm tra payment SUCCESS, sự kiện đã hủy hoặc booking PAYMENT_REVIEW, số tiền dương và chưa giải ngân/đảo sổ. Trong cùng transaction: đảo đúng phí/phần Organizer đã chụp khi thanh toán, cập nhật refund và thông báo REFUND_COMPLETED một lần. Khi lỗi, rollback toàn bộ. Payment SUCCESS giữ làm lịch sử thu tiền.

Customer `GET /api/bookings/{id}` trả `payments[].refund` gồm `{amount,status,method,reference,refundedAt}` chỉ cho chủ booking.

## Nguồn tiền và worker giải ngân

Payment thành công được chụp gross/phí 5%/phần Organizer vào payment_finances duy nhất theo payment. PAID cộng pending một lần; PAYMENT_REVIEW chưa cộng ví. Worker tài chính chỉ giải ngân pending sang available sau khi sự kiện kết thúc và đáp ứng điều kiện vé; không xử lý refund.

`FINANCE_WORKER_ENABLED` và `FINANCE_WORKER_DELAY` chỉ điều khiển tiến trình giải ngân. Refund không phụ thuộc merchant, IP, chứng chỉ hay API public.

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

V15 xóa hai bảng `refund_attempts` và `payment_confirmation_emails`; dữ liệu lịch sử trong hai bảng này bị xóa. Không tạo bảng thay thế. Giữ bảng refunds/payment_finances cùng dữ liệu tiền. Khoản QUEUED/PROCESSING/FAILED/RECONCILIATION cũ chuyển về WAITING để Admin xử lý bằng luồng mới. Không sửa các migration đã áp dụng.

Email xác nhận thanh toán gửi qua listener sau commit, không lưu outbox; lỗi SMTP không rollback thanh toán và không tự retry.

Kiểm thử bao gồm phân quyền, CSRF, hoàn đơn lẻ/hàng loạt, các trạng thái lỗi cũ, đồng thời, idempotence, đảo sổ và rollback; kiểm tra schema không còn hai bảng phụ. Frontend dùng tests/admin-finance.spec.ts và tests/booking.spec.ts.
