# API tài chính Admin UC37–UC42

Các API này ghép với security, cookie HttpOnly và response lỗi đang có. Quyền: ADMIN ACTIVE. POST cần JSON và `X-CSRF-Protection: 1`. Danh sách dùng `items`, `page`, `pageSize`, `totalElements`, `totalPages`; page từ 0, pageSize 1–100, mặc định 20, ID giảm dần. Tiền VND/BigDecimal; datetime UTC không offset, frontend hiển thị GMT+7.

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
  "grossRevenue": null,
  "netRevenue": null,
  "revenueUnavailableReason": "Chưa có định nghĩa doanh thu và trạng thái thanh toán thành công trong tài liệu tích hợp."
}
```

Ví dụ mô tả DB trống, không phải dữ liệu production hardcode. Tất cả số lượng/số dư tính từ DB trong một query. Vé bán dùng tổng `quantity - remainingQuantity - reservedQuantity` theo convention API sự kiện. Các số dư là SUM cột tương ứng ở organizer_wallets; không được diễn giải tổng các số dư thành tiền thuộc hệ thống. Doanh thu null do tài liệu chưa định nghĩa gross/net/phí/refund/payment thành công.

## UC39–UC40 danh sách và chi tiết

- `GET /api/admin/withdrawal-requests?status=PENDING&page=0&pageSize=20`: status tùy chọn PENDING/APPROVED/REJECTED; bỏ status lấy tất cả. Không có body. Sai status/phân trang trả 400.
- `GET /api/admin/withdrawal-requests/{requestId}`: ID nguyên dương; không có body/query. Sai ID 400, không tồn tại 404.

Một phần tử gồm `id`, `organizer` (`id`, `fullName`, `email`), `amount`, `bankName`, `accountNumber`, `status`, `requestedAt`, `processedBy`, `processedAt`, `rejectReason`, `transactionCode`, `payouts`. List không công khai số tài khoản (`accountNumber=null`) và trả `payouts=[]`; detail Admin trả tài khoản cùng lịch sử payout. Trường xử lý chưa có trả null. Mỗi payout có `id`, `amount`, `status`, `transactionCode`, `failureReason`, `initiatedAt`, `completedAt`.

PENDING/APPROVED/REJECTED là tên kỹ thuật cho trạng thái Chờ duyệt/Đã duyệt/Bị từ chối của DOCX. Chưa có module UC25 tạo request trong checkout; cần thống nhất các tên này với module đó. Không migrate hoặc tự suy diễn trạng thái cũ; yêu cầu trạng thái khác không được quyết định như PENDING.

## UC41 ghi nhận chuyển khoản đã thực hiện

`POST /api/admin/withdrawal-requests/{requestId}/approve`

```json
{ "transactionCode": "<mã ngân hàng của chuyển khoản đã thực hiện ngoài hệ thống>" }
```

Mã bắt buộc, trim, tối đa 255 ký tự. Admin chỉ gọi **sau khi chuyển khoản ngoài hệ thống**, theo DOCX. API không gửi lệnh chuyển tiền, không tự xác minh giao dịch tại ngân hàng.

Trong transaction: khóa request và ví; chỉ PENDING, khoản giữ đủ, chưa có payout cần đối soát; trừ withdrawalPendingBalance, cộng totalWithdrawn, giữ availableBalance; lưu request APPROVED, processedBy/processedAt từ principal/server, mã chuyển khoản, payout COMPLETED và notification WITHDRAWAL_APPROVED. initiatedAt/completedAt của payout là lúc ghi nhận tại hệ thống. Response 200 là chi tiết request cập nhật.

Webhook/retry chưa triển khai: thiếu URL nhận, xác thực, payload và outbox/delivery contract. Notification DB không thay webhook.

## UC42 từ chối

`POST /api/admin/withdrawal-requests/{requestId}/reject`

```json
{ "reason": "Thông tin ngân hàng cần được kiểm tra lại." }
```

Lý do bắt buộc, trim, tối đa 255 ký tự. Khóa và kiểm tra như duyệt. Trừ withdrawalPendingBalance, cộng availableBalance, giữ totalWithdrawn; lưu REJECTED, lý do/người/thời gian xử lý và notification WITHDRAWAL_REJECTED. Response 200 là chi tiết cập nhật.

Lỗi ghi dữ liệu hoặc notification rollback toàn bộ quyết định/số dư/payout. Request đã quyết định hoặc gửi lặp trả 409, không tạo thêm tác dụng phụ. Unique index V6 bảo vệ mã chuyển khoản không null trên payout cả khi cạnh tranh giữa hai request.

## UC38 đối soát chỉ đọc

- `GET /api/admin/refund-events?page=0&pageSize=20`: phân trang các sự kiện CANCELED, phần tử `id`, `name`, `organizer`, `cancelReason`.
- `GET /api/admin/refund-events/{eventId}?page=0&pageSize=20`: response `eventId`, `eventName`, `cancelReason`, `payments` (PageResponse), `canRefund=false`, `unavailableReason`.

Mỗi payment gồm `id`, `bookingId`, `customerName`, `paidAmount`, `paymentStatus`, `paidAt`, `refundId`, `refundAmount`, `refundStatus`, `retryCount`, `refundedAt`. Đây là amount/status đã lưu, **chưa lọc thành giao dịch đủ điều kiện hoàn tiền**; refundAmount chưa được suy diễn là khoản đã hoàn thành nếu chưa có provider contract. Không công khai token, mã gateway hay lỗi nội bộ provider.

ID không tồn tại 404; sự kiện không CANCELED, kể cả PENDING_CANCELLATION, trả 409 EVENT_NOT_CANCELED. Chưa có POST refund, provider client, worker retry hoặc sửa wallet/booking/ticket; không đánh dấu thành công hay tự chuyển tiền. Đọc lặp không ghi dữ liệu.

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

V6 thêm `uk_payout_transactions_transfer_reference`. Nếu dữ liệu cũ có mã trùng, dừng để đối soát; không xóa/gộp tự động. Không sửa V1–V5, không chạy migration vào DB nghiệp vụ để test.

`AdminFinanceIntegrationTests` kiểm tra trên PostgreSQL test riêng: phân quyền, list/detail, wallet math, duplicate decision, concurrency cùng request/cùng ví, unique transfer reference, rollback notification, canceled event/partial refund read-only. `DatabaseIntegrationTests` kiểm tra nâng V1 lên V6. Frontend test giả lập API nằm ở `tests/admin-finance.spec.ts`; production gọi API thật.
