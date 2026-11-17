package com.eventhub.backend.service;

import com.eventhub.backend.dto.response.AiChatResponses.Source;
import com.eventhub.backend.dto.response.UserResponse;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

/** Only fixed, parameterized SELECTs. The model cannot execute SQL or choose another user's scope. */
@Service
@RequiredArgsConstructor
public class AiContextService {
    private final JdbcClient jdbc;
    private final ObjectMapper json;
    private final Clock clock;

    public record Context(String data, List<Source> sources, Instant capturedAt) {}

    // Rank matching words without turning any part of the user's question into SQL.
    private static final String RELEVANCE = """
            (select count(*) from regexp_split_to_table(lower(:question), '[[:space:][:punct:]]+') word
             where length(word) >= 3 and word not in
               ('cho','tôi','của','các','những','thông','tin','kiện','được','nào','bao','nhiêu','giúp','hãy','đang','cần','với')
             and position(word in lower(concat(e.name, ' ', v.city, ' ', c.name))) > 0) desc,
            case when e.end_time > (current_timestamp at time zone 'UTC') then 0 else 1 end,
            e.start_time desc, e.id desc limit 12
            """;

    @Transactional(readOnly = true)
    public Context load(UserResponse user, String question) {
        var data = new LinkedHashMap<String, Object>();
        var sources = new ArrayList<Source>();
        data.put("role", user.role().name());
        data.put("currency", "VND");
        data.put("timestampTimezone", "UTC; convert to Asia/Ho_Chi_Minh (UTC+7) when answering in Vietnamese");
        data.put("limits", "Event lists contain at most 12 relevant records; booking lists at most 10 recent records. "
                + "An absent record is NOT proof that it does not exist. Counts in overview are global within the user's scope.");

        String scope = switch (user.role()) {
            case CUSTOMER -> "e.status in ('APPROVED','ONGOING','COMPLETED')";
            case ORGANIZER -> "e.organizer_id = :userId";
            case STAFF -> "exists (select 1 from event_staffs es where es.events_id=e.id and es.staff_id=:userId and es.status='ACTIVE')";
            case ADMIN -> "true";
        };
        String extra = switch (user.role()) {
            case ORGANIZER, ADMIN -> """
                    , (select count(*) from bookings b where b.events_id=e.id and b.status='PAID') as paid_bookings
                    , (select coalesce(sum(b.total_amount),0) from bookings b where b.events_id=e.id and b.status='PAID') as paid_booking_amount
                    """;
            default -> "";
        };
        if (user.role() != com.eventhub.backend.enums.Role.CUSTOMER) {
            extra += """
                    , (select count(*) from tickets t join booking_items bi on bi.id=t.booking_items_id
                       join bookings b on b.id=bi.bookings_id where b.events_id=e.id and b.status='PAID') as issued_tickets
                    , (select count(*) from tickets t join booking_items bi on bi.id=t.booking_items_id
                       join bookings b on b.id=bi.bookings_id where b.events_id=e.id and b.status='PAID' and t.status='USED') as checked_in_tickets
                    """;
        }
        String sql = """
                select e.id, e.name, e.description, e.start_time, e.end_time,
                  case when e.status in ('APPROVED','ONGOING') and e.end_time <= (current_timestamp at time zone 'UTC') then 'COMPLETED'
                       when e.status='APPROVED' and e.start_time <= (current_timestamp at time zone 'UTC') then 'ONGOING'
                       else e.status end as status,
                  v.city, v.address, c.name as category
                """ + extra + " from events e join venues v on v.id=e.venues_id join categories c on c.id=e.categories_id where "
                + scope + " order by " + RELEVANCE;
        var query = jdbc.sql(sql).param("question", question);
        if (user.role() != com.eventhub.backend.enums.Role.ADMIN && user.role() != com.eventhub.backend.enums.Role.CUSTOMER) {
            query = query.param("userId", user.id());
        }
        var events = query.query().listOfRows();
        data.put("events", events);
        if (!events.isEmpty()) {
            var ids = events.stream().map(row -> row.get("id")).toList();
            data.put("ticketTypes", jdbc.sql("""
                    select tt.events_id as event_id, tt.name, tt.price, tt.remaining_quantity,
                      tt.sale_start_time, tt.sale_end_time,
                      case when e.start_time <= (current_timestamp at time zone 'UTC') then 'SALES_ENDED'
                           when e.status <> 'APPROVED' or tt.status <> 'ACTIVE' then 'UNAVAILABLE'
                           when tt.sale_start_time > (current_timestamp at time zone 'UTC') then 'NOT_ON_SALE'
                           when tt.sale_end_time <= (current_timestamp at time zone 'UTC') then 'SALES_ENDED'
                           when tt.remaining_quantity <= 0 then 'SOLD_OUT' else 'ON_SALE' end as sales_status
                    from ticket_types tt join events e on e.id=tt.events_id
                    where tt.events_id in (:ids) order by tt.events_id, tt.price, tt.id limit 60
                    """).param("ids", ids).query().listOfRows());
        }

        switch (user.role()) {
            case CUSTOMER, STAFF -> {
                data.put("myRecentBookings", jdbc.sql("""
                        select b.id, e.name as event_name, e.start_time, b.total_amount, b.status, b.expired_at,
                          (select count(*) from tickets t join booking_items bi on bi.id=t.booking_items_id
                           where bi.bookings_id=b.id) as ticket_count,
                          (select max(r.status) from refunds r join payments p on p.id=r.payments_id
                           where p.bookings_id=b.id) as refund_status
                        from bookings b join events e on e.id=b.events_id where b.users_id=:userId
                        order by b.id desc limit 10
                        """).param("userId", user.id()).query().listOfRows());
                sources.add(new Source("Đơn đặt vé của tôi", "/customer/booking-cua-toi"));
                sources.add(new Source("Vé của tôi", "/customer/ve-cua-toi"));
                sources.add(user.role() == com.eventhub.backend.enums.Role.STAFF
                        ? new Source("Sự kiện được phân công", "/staff")
                        : new Source("Khám phá sự kiện", "/customer/explore"));
            }
            case ORGANIZER -> {
                data.put("myEventCounts", jdbc.sql("select status, count(*) as total from events where organizer_id=:userId group by status")
                        .param("userId", user.id()).query().listOfRows());
                data.put("myWallet", jdbc.sql("""
                        select pending_balance, available_balance, withdrawal_pending_balance, total_withdrawn
                        from organizer_wallets where organizer_id=:userId
                        """).param("userId", user.id()).query().listOfRows());
                data.put("myRecentWithdrawals", jdbc.sql("""
                        select wr.id, wr.amount, wr.status, wr.requested_at, wr.reject_reason
                        from withdrawal_requests wr join organizer_wallets w on w.id=wr.organizer_wallets_id
                        where w.organizer_id=:userId order by wr.id desc limit 10
                        """).param("userId", user.id()).query().listOfRows());
                sources.add(new Source("Sự kiện của tôi", "/organizer/events"));
                sources.add(new Source("Ví doanh thu", "/organizer/wallet"));
                sources.add(new Source("Báo cáo", "/organizer/reports"));
            }
            case ADMIN -> {
                data.put("overview", jdbc.sql("""
                        select (select count(*) from users) as total_users,
                          (select count(*) from events) as total_events,
                          (select count(*) from events where status='PENDING_APPROVAL') as events_pending_review,
                          (select count(*) from withdrawal_requests where status='PENDING') as pending_withdrawals,
                          (select count(*) from refunds where status='WAITING') as pending_refunds,
                          (select coalesce(sum(amount),0) from payments where status='SUCCESS') as gross_revenue,
                          (select coalesce(sum(amount),0) from refunds where status='COMPLETED') as refunded_amount
                        """).query().singleRow());
                data.put("eventCounts", jdbc.sql("select status, count(*) as total from events group by status").query().listOfRows());
                data.put("userCounts", jdbc.sql("select role, status, count(*) as total from users group by role, status").query().listOfRows());
                data.put("pendingEvents", jdbc.sql("""
                        select id, name, start_time, created_at from events where status='PENDING_APPROVAL'
                        order by created_at, id limit 10
                        """).query().listOfRows());
                sources.add(new Source("Thống kê tổng quan", "/admin/statistics"));
                sources.add(new Source("Sự kiện chờ duyệt", "/admin/events/pending"));
                sources.add(new Source("Yêu cầu rút tiền", "/admin/withdrawals"));
                sources.add(new Source("Sự kiện đã hủy & hoàn tiền", "/admin/refunds"));
            }
        }
        Instant now = clock.instant();
        data.put("capturedAt", now.toString());
        return new Context(json.writeValueAsString(data), List.copyOf(sources), now);
    }
}
