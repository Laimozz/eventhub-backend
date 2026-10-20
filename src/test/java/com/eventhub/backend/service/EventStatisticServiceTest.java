package com.eventhub.backend.service;

import com.eventhub.backend.dto.response.EventStatisticResponse;
import com.eventhub.backend.entity.Event;
import com.eventhub.backend.entity.TicketType;
import com.eventhub.backend.entity.User;
import com.eventhub.backend.enums.EventStatus;
import com.eventhub.backend.enums.Role;
import com.eventhub.backend.exception.EventStatisticException;
import com.eventhub.backend.repository.EventRepository;
import com.eventhub.backend.repository.TicketTypeRepository;
import com.eventhub.backend.repository.UserRepository;
import com.eventhub.backend.service.impl.EventStatisticServiceImpl;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventStatisticServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private TicketTypeRepository ticketTypeRepository;

    @Mock
    private UserRepository userRepository;

    private Clock fixedClock;
    private EventStatisticServiceImpl eventStatisticService;

    private User organizer;
    private Event event;

    @BeforeEach
    void setUp() {
        // Fixed clock at 2026-10-16 10:00:00 UTC
        fixedClock = Clock.fixed(Instant.parse("2026-10-16T10:00:00Z"), ZoneOffset.UTC);
        eventStatisticService = new EventStatisticServiceImpl(
                eventRepository, ticketTypeRepository, userRepository, fixedClock);

        organizer = new User();
        organizer.setId(1);
        organizer.setEmail("organizer@gmail.com");
        organizer.setFullName("Nguyễn Văn Tổ Chức");
        organizer.setRole(Role.ORGANIZER);
        organizer.setStatus("ACTIVE");

        event = new Event();
        event.setId(10);
        event.setName("Đại Nhạc Hội Mùa Thu");
        event.setOrganizer(organizer);
        event.setStatus(EventStatus.APPROVED);
        event.setStartTime(LocalDateTime.of(2026, 11, 1, 19, 0));
        event.setEndTime(LocalDateTime.of(2026, 11, 1, 23, 0));
    }

    @Nested
    @DisplayName("Pre-conditions & Access Control")
    class PreconditionsTests {

        @Test
        @DisplayName("Ném lỗi FORBIDDEN nếu tài khoản organizer không tồn tại hoặc không ACTIVE")
        void shouldThrowForbiddenWhenOrganizerNotFoundOrInactive() {
            when(userRepository.findById(1)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> eventStatisticService.getEventStatistics(1, 10))
                    .isInstanceOf(EventStatisticException.class)
                    .satisfies(ex -> {
                        var e = (EventStatisticException) ex;
                        assertThat(e.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
                        assertThat(e.getCode()).isEqualTo("FORBIDDEN");
                    });
        }

        @Test
        @DisplayName("Ném lỗi NOT_FOUND nếu không tìm thấy sự kiện")
        void shouldThrowNotFoundWhenEventDoesNotExist() {
            when(userRepository.findById(1)).thenReturn(Optional.of(organizer));
            when(eventRepository.findById(10)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> eventStatisticService.getEventStatistics(1, 10))
                    .isInstanceOf(EventStatisticException.class)
                    .satisfies(ex -> {
                        var e = (EventStatisticException) ex;
                        assertThat(e.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                        assertThat(e.getCode()).isEqualTo("EVENT_NOT_FOUND");
                    });
        }

        @Test
        @DisplayName("Ném lỗi FORBIDDEN nếu sự kiện không thuộc về organizer đang đăng nhập")
        void shouldThrowForbiddenWhenEventDoesNotBelongToOrganizer() {
            User otherOrganizer = new User();
            otherOrganizer.setId(99);
            event.setOrganizer(otherOrganizer);

            when(userRepository.findById(1)).thenReturn(Optional.of(organizer));
            when(eventRepository.findById(10)).thenReturn(Optional.of(event));

            assertThatThrownBy(() -> eventStatisticService.getEventStatistics(1, 10))
                    .isInstanceOf(EventStatisticException.class)
                    .satisfies(ex -> {
                        var e = (EventStatisticException) ex;
                        assertThat(e.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
                        assertThat(e.getCode()).isEqualTo("FORBIDDEN");
                    });
        }
    }

    @Nested
    @DisplayName("Rule 1.1: Sự kiện chưa được duyệt hoặc chưa mở bán")
    class Rule11Tests {

        @Test
        @DisplayName("Ném lỗi EVENT_NOT_APPROVED khi sự kiện chưa được Admin duyệt (ví dụ: PENDING_APPROVAL)")
        void shouldThrowBadRequestWhenEventNotApproved() {
            event.setStatus(EventStatus.PENDING_APPROVAL);

            when(userRepository.findById(1)).thenReturn(Optional.of(organizer));
            when(eventRepository.findById(10)).thenReturn(Optional.of(event));

            assertThatThrownBy(() -> eventStatisticService.getEventStatistics(1, 10))
                    .isInstanceOf(EventStatisticException.class)
                    .satisfies(ex -> {
                        var e = (EventStatisticException) ex;
                        assertThat(e.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                        assertThat(e.getCode()).isEqualTo("EVENT_NOT_APPROVED");
                        assertThat(e.getMessage()).isEqualTo("Sự kiện chưa được duyệt hoặc chưa mở bán");
                    });
        }

        @Test
        @DisplayName("Ném lỗi EVENT_NOT_OPEN_FOR_SALE khi sự kiện đã duyệt nhưng thời gian mở bán chưa tới")
        void shouldThrowBadRequestWhenSalesNotStartedYet() {
            // Clock is 2026-10-16 10:00:00, ticket sales start at 2026-10-20
            TicketType vip = new TicketType();
            vip.setId(101);
            vip.setName("Vé VIP");
            vip.setQuantity(100);
            vip.setRemainingQuantity(100);
            vip.setSaleStartTime(LocalDateTime.of(2026, 10, 20, 8, 0));
            vip.setSaleEndTime(LocalDateTime.of(2026, 10, 31, 23, 59));

            when(userRepository.findById(1)).thenReturn(Optional.of(organizer));
            when(eventRepository.findById(10)).thenReturn(Optional.of(event));
            when(ticketTypeRepository.findByEventIdOrderByIdAsc(10)).thenReturn(List.of(vip));

            assertThatThrownBy(() -> eventStatisticService.getEventStatistics(1, 10))
                    .isInstanceOf(EventStatisticException.class)
                    .satisfies(ex -> {
                        var e = (EventStatisticException) ex;
                        assertThat(e.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                        assertThat(e.getCode()).isEqualTo("EVENT_NOT_OPEN_FOR_SALE");
                        assertThat(e.getMessage()).isEqualTo("Sự kiện chưa được duyệt hoặc chưa mở bán");
                    });
        }
    }

    @Nested
    @DisplayName("Main Events 2, 3 & Rule 3.1: Thống kê bán vé và doanh thu")
    class StatisticsCalculationTests {

        @Test
        @DisplayName("Tính toán chính xác số liệu tổng quan và chi tiết theo từng loại vé (khớp số liệu prompt)")
        void shouldCalculateStatisticsAccuratelyMatchingPrompt() {
            // Vé VIP: 100 phát hành, đã bán 65, còn lại 35, giá 1.500.000đ -> Doanh thu 97.500.000đ, Tỷ lệ 65%
            TicketType vip = new TicketType();
            vip.setId(101);
            vip.setName("Vé VIP");
            vip.setPrice(new BigDecimal("1500000.00"));
            vip.setQuantity(100);
            vip.setRemainingQuantity(35);
            vip.setReservedQuantity(0);
            vip.setSaleStartTime(LocalDateTime.of(2026, 10, 1, 8, 0)); // Đã mở bán
            vip.setSaleEndTime(LocalDateTime.of(2026, 10, 31, 23, 59));

            // Vé Thường: 300 phát hành, đã bán 210, còn lại 90, giá 500.000đ -> Doanh thu 105.000.000đ, Tỷ lệ 70%
            TicketType standard = new TicketType();
            standard.setId(102);
            standard.setName("Vé Thường");
            standard.setPrice(new BigDecimal("500000.00"));
            standard.setQuantity(300);
            standard.setRemainingQuantity(90);
            standard.setReservedQuantity(0);
            standard.setSaleStartTime(LocalDateTime.of(2026, 10, 1, 8, 0)); // Đã mở bán
            standard.setSaleEndTime(LocalDateTime.of(2026, 10, 31, 23, 59));

            when(userRepository.findById(1)).thenReturn(Optional.of(organizer));
            when(eventRepository.findById(10)).thenReturn(Optional.of(event));
            when(ticketTypeRepository.findByEventIdOrderByIdAsc(10)).thenReturn(List.of(vip, standard));

            EventStatisticResponse response = eventStatisticService.getEventStatistics(1, 10);

            // Kiểm tra số liệu tổng quan
            assertThat(response.eventId()).isEqualTo(10);
            assertThat(response.eventName()).isEqualTo("Đại Nhạc Hội Mùa Thu");
            assertThat(response.totalSoldTickets()).isEqualTo(275);
            assertThat(response.totalTickets()).isEqualTo(400);
            assertThat(response.totalRemainingTickets()).isEqualTo(125);
            assertThat(response.soldRatioFormatted()).isEqualTo("275 / 400");
            assertThat(response.totalRevenue()).isEqualByComparingTo("202500000.00");
            assertThat(response.overallSalesRate()).isEqualTo(68.75);
            assertThat(response.formattedOverallSalesRate()).isEqualTo("68.75%");

            // Kiểm tra chi tiết từng loại vé
            assertThat(response.ticketTypes()).hasSize(2);

            var vipStat = response.ticketTypes().get(0);
            assertThat(vipStat.ticketTypeId()).isEqualTo(101);
            assertThat(vipStat.name()).isEqualTo("Vé VIP");
            assertThat(vipStat.totalQuantity()).isEqualTo(100);
            assertThat(vipStat.soldQuantity()).isEqualTo(65);
            assertThat(vipStat.remainingQuantity()).isEqualTo(35);
            assertThat(vipStat.salesRate()).isEqualTo(65.0);
            assertThat(vipStat.formattedSalesRate()).isEqualTo("65%");
            assertThat(vipStat.revenue()).isEqualByComparingTo("97500000.00");

            var stdStat = response.ticketTypes().get(1);
            assertThat(stdStat.ticketTypeId()).isEqualTo(102);
            assertThat(stdStat.name()).isEqualTo("Vé Thường");
            assertThat(stdStat.totalQuantity()).isEqualTo(300);
            assertThat(stdStat.soldQuantity()).isEqualTo(210);
            assertThat(stdStat.remainingQuantity()).isEqualTo(90);
            assertThat(stdStat.salesRate()).isEqualTo(70.0);
            assertThat(stdStat.formattedSalesRate()).isEqualTo("70%");
            assertThat(stdStat.revenue()).isEqualByComparingTo("105000000.00");
        }

        @Test
        @DisplayName("Rule 3.1: Sự kiện chưa có vé nào được bán -> hiển thị số liệu bằng 0")
        void shouldReturnZeroMetricsWhenNoTicketsSold() {
            TicketType vip = new TicketType();
            vip.setId(101);
            vip.setName("Vé VIP");
            vip.setPrice(new BigDecimal("1500000.00"));
            vip.setQuantity(100);
            vip.setRemainingQuantity(100); // Chưa bán vé nào
            vip.setReservedQuantity(0);
            vip.setSaleStartTime(LocalDateTime.of(2026, 10, 1, 8, 0)); // Đã mở bán
            vip.setSaleEndTime(LocalDateTime.of(2026, 10, 31, 23, 59));

            when(userRepository.findById(1)).thenReturn(Optional.of(organizer));
            when(eventRepository.findById(10)).thenReturn(Optional.of(event));
            when(ticketTypeRepository.findByEventIdOrderByIdAsc(10)).thenReturn(List.of(vip));

            EventStatisticResponse response = eventStatisticService.getEventStatistics(1, 10);

            // Kiểm tra số liệu tổng quan bằng 0
            assertThat(response.totalSoldTickets()).isEqualTo(0);
            assertThat(response.totalTickets()).isEqualTo(100);
            assertThat(response.totalRemainingTickets()).isEqualTo(100);
            assertThat(response.soldRatioFormatted()).isEqualTo("0 / 100");
            assertThat(response.totalRevenue()).isEqualByComparingTo("0.00");
            assertThat(response.overallSalesRate()).isEqualTo(0.0);
            assertThat(response.formattedOverallSalesRate()).isEqualTo("0%");

            // Chi tiết loại vé
            var vipStat = response.ticketTypes().get(0);
            assertThat(vipStat.soldQuantity()).isEqualTo(0);
            assertThat(vipStat.remainingQuantity()).isEqualTo(100);
            assertThat(vipStat.salesRate()).isEqualTo(0.0);
            assertThat(vipStat.formattedSalesRate()).isEqualTo("0%");
            assertThat(vipStat.revenue()).isEqualByComparingTo("0.00");
        }
    }
}
