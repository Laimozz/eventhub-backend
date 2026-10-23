package com.eventhub.backend.service;

import com.eventhub.backend.dto.response.OrganizerWalletResponse;
import com.eventhub.backend.entity.Event;
import com.eventhub.backend.entity.OrganizerWallet;
import com.eventhub.backend.entity.TicketType;
import com.eventhub.backend.entity.User;
import com.eventhub.backend.enums.Role;
import com.eventhub.backend.exception.OrganizerWalletException;
import com.eventhub.backend.repository.EventRepository;
import com.eventhub.backend.repository.OrganizerWalletRepository;
import com.eventhub.backend.repository.TicketTypeRepository;
import com.eventhub.backend.repository.UserRepository;
import com.eventhub.backend.service.impl.OrganizerWalletServiceImpl;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrganizerWalletServiceTest {

    @Mock
    private OrganizerWalletRepository organizerWalletRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private TicketTypeRepository ticketTypeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock private com.eventhub.backend.repository.FinanceLedgerRepository financeLedger;
    @InjectMocks
    private OrganizerWalletServiceImpl organizerWalletService;

    private User organizer;

    @BeforeEach
    void setUp() {
        organizer = new User();
        organizer.setId(1);
        organizer.setEmail("organizer@gmail.com");
        organizer.setFullName("Nguyễn Văn Tổ Chức");
        organizer.setRole(Role.ORGANIZER);
        organizer.setStatus("ACTIVE");
    }

    @Nested
    @DisplayName("Pre-conditions & Access Control")
    class PreconditionsTests {

        @Test
        @DisplayName("Ném lỗi FORBIDDEN nếu tài khoản organizer không tồn tại")
        void shouldThrowForbiddenWhenOrganizerNotFound() {
            when(userRepository.findById(1)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> organizerWalletService.getWalletAndRevenue(1))
                    .isInstanceOf(OrganizerWalletException.class)
                    .satisfies(ex -> {
                        var e = (OrganizerWalletException) ex;
                        assertThat(e.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
                        assertThat(e.getCode()).isEqualTo("FORBIDDEN");
                    });
        }

        @Test
        @DisplayName("Ném lỗi FORBIDDEN nếu tài khoản không ACTIVE hoặc không phải role ORGANIZER")
        void shouldThrowForbiddenWhenNotActiveOrNotOrganizerRole() {
            organizer.setStatus("LOCKED");
            when(userRepository.findById(1)).thenReturn(Optional.of(organizer));

            assertThatThrownBy(() -> organizerWalletService.getWalletAndRevenue(1))
                    .isInstanceOf(OrganizerWalletException.class)
                    .satisfies(ex -> {
                        var e = (OrganizerWalletException) ex;
                        assertThat(e.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
                    });
        }
    }

    @Nested
    @DisplayName("Main Events 2 & 3: Hiển thị ví và doanh thu từng sự kiện")
    class MainEventsTests {

        @Test
        @DisplayName("Hiển thị chính xác thông tin số dư ví và doanh thu theo từng sự kiện (khớp prompt UC24)")
        void shouldReturnWalletAndRevenuesMatchingPrompt() {
            // Sự kiện 1: "Đêm nhạc Acoustic Thu" - bán vé VIP doanh thu 97.500.000đ
            Event event1 = new Event();
            event1.setId(10);
            event1.setName("Đêm nhạc Acoustic Thu");

            TicketType ticket1 = new TicketType();
            ticket1.setId(101);
            ticket1.setName("Vé VIP");
            ticket1.setPrice(new BigDecimal("1500000.00"));
            ticket1.setQuantity(100);
            ticket1.setRemainingQuantity(35); // Đã bán 65 -> 97.500.000đ
            ticket1.setReservedQuantity(0);

            // Sự kiện 2: "Hội thảo Công nghệ AI" - bán vé Thường doanh thu 105.000.000đ
            Event event2 = new Event();
            event2.setId(20);
            event2.setName("Hội thảo Công nghệ AI");

            TicketType ticket2 = new TicketType();
            ticket2.setId(201);
            ticket2.setName("Vé Thường");
            ticket2.setPrice(new BigDecimal("500000.00"));
            ticket2.setQuantity(300);
            ticket2.setRemainingQuantity(90); // Đã bán 210 -> 105.000.000đ
            ticket2.setReservedQuantity(0);

            // Ví organizer
            OrganizerWallet wallet = new OrganizerWallet();
            wallet.setId(1);
            wallet.setOrganizer(organizer);
            wallet.setPendingBalance(new BigDecimal("15000000.00"));
            wallet.setAvailableBalance(new BigDecimal("42300000.00"));
            wallet.setWithdrawalPendingBalance(new BigDecimal("5000000.00"));
            wallet.setTotalWithdrawn(new BigDecimal("80000000.00"));

            when(userRepository.findById(1)).thenReturn(Optional.of(organizer));
            when(eventRepository.findByOrganizerIdOrderByIdAsc(1)).thenReturn(List.of(event1, event2));
            when(financeLedger.eventRevenue(10)).thenReturn(new BigDecimal("97500000.00"));
            when(ticketTypeRepository.findByEventIdOrderByIdAsc(10)).thenReturn(List.of(ticket1));
            when(financeLedger.eventRevenue(20)).thenReturn(new BigDecimal("105000000.00"));
            when(ticketTypeRepository.findByEventIdOrderByIdAsc(20)).thenReturn(List.of(ticket2));
            when(organizerWalletRepository.findByOrganizerId(1)).thenReturn(Optional.of(wallet));

            OrganizerWalletResponse response = organizerWalletService.getWalletAndRevenue(1);

            // Kiểm tra số dư ví khớp số liệu UC24
            assertThat(response.pendingBalance()).isEqualByComparingTo("15000000.00");
            assertThat(response.formattedPendingBalance()).isEqualTo("15.000.000đ");

            assertThat(response.availableBalance()).isEqualByComparingTo("42300000.00");
            assertThat(response.formattedAvailableBalance()).isEqualTo("42.300.000đ");

            assertThat(response.withdrawalPendingBalance()).isEqualByComparingTo("5000000.00");
            assertThat(response.formattedWithdrawalPendingBalance()).isEqualTo("5.000.000đ");

            assertThat(response.totalWithdrawn()).isEqualByComparingTo("80000000.00");
            assertThat(response.formattedTotalWithdrawn()).isEqualTo("80.000.000đ");

            // Kiểm tra doanh thu theo từng sự kiện
            assertThat(response.eventRevenues()).hasSize(2);

            var rev1 = response.eventRevenues().get(0);
            assertThat(rev1.eventId()).isEqualTo(10);
            assertThat(rev1.eventName()).isEqualTo("Đêm nhạc Acoustic Thu");
            assertThat(rev1.revenue()).isEqualByComparingTo("97500000.00");
            assertThat(rev1.formattedRevenue()).isEqualTo("97.500.000đ");
            assertThat(rev1.soldTickets()).isEqualTo(65);
            assertThat(rev1.totalTickets()).isEqualTo(100);

            var rev2 = response.eventRevenues().get(1);
            assertThat(rev2.eventId()).isEqualTo(20);
            assertThat(rev2.eventName()).isEqualTo("Hội thảo Công nghệ AI");
            assertThat(rev2.revenue()).isEqualByComparingTo("105000000.00");
            assertThat(rev2.formattedRevenue()).isEqualTo("105.000.000đ");
            assertThat(rev2.soldTickets()).isEqualTo(210);
            assertThat(rev2.totalTickets()).isEqualTo(300);
        }

        @Test
        @DisplayName("Tự động khởi tạo ví mới nếu organizer đã bán vé nhưng ví chưa được tạo trước đó")
        void shouldInitializeWalletWhenMissing() {
            Event event1 = new Event();
            event1.setId(10);
            event1.setName("Sự kiện Demo");

            TicketType ticket1 = new TicketType();
            ticket1.setId(101);
            ticket1.setPrice(new BigDecimal("100000.00"));
            ticket1.setQuantity(10);
            ticket1.setRemainingQuantity(8); // Bán 2 vé
            ticket1.setReservedQuantity(0);

            OrganizerWallet newWallet = new OrganizerWallet();
            newWallet.setOrganizer(organizer);
            newWallet.setPendingBalance(BigDecimal.ZERO);
            newWallet.setAvailableBalance(BigDecimal.ZERO);
            newWallet.setWithdrawalPendingBalance(BigDecimal.ZERO);
            newWallet.setTotalWithdrawn(BigDecimal.ZERO);

            when(userRepository.findById(1)).thenReturn(Optional.of(organizer));
            when(eventRepository.findByOrganizerIdOrderByIdAsc(1)).thenReturn(List.of(event1));
            when(financeLedger.eventRevenue(10)).thenReturn(new BigDecimal("97500000.00"));
            when(ticketTypeRepository.findByEventIdOrderByIdAsc(10)).thenReturn(List.of(ticket1));
            when(organizerWalletRepository.findByOrganizerId(1)).thenReturn(Optional.empty());


            OrganizerWalletResponse response = organizerWalletService.getWalletAndRevenue(1);

            verify(organizerWalletRepository, never()).save(any(OrganizerWallet.class));
            assertThat(response.availableBalance()).isEqualByComparingTo("0.00");
            assertThat(response.formattedAvailableBalance()).isEqualTo("0đ");
            assertThat(response.eventRevenues()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Exception 2.1: Nhà tổ chức chưa từng bán vé sự kiện nào")
    class Exception21Tests {

        @Test
        @DisplayName("Hiển thị toàn bộ số dư bằng 0 và danh sách doanh thu trống khi chưa từng tạo sự kiện nào")
        void shouldReturnZerosAndEmptyListWhenNoEvents() {
            when(userRepository.findById(1)).thenReturn(Optional.of(organizer));
            when(eventRepository.findByOrganizerIdOrderByIdAsc(1)).thenReturn(List.of());

            OrganizerWalletResponse response = organizerWalletService.getWalletAndRevenue(1);

            assertThat(response.pendingBalance()).isEqualByComparingTo("0.00");
            assertThat(response.formattedPendingBalance()).isEqualTo("0đ");
            assertThat(response.availableBalance()).isEqualByComparingTo("0.00");
            assertThat(response.formattedAvailableBalance()).isEqualTo("0đ");
            assertThat(response.withdrawalPendingBalance()).isEqualByComparingTo("0.00");
            assertThat(response.formattedWithdrawalPendingBalance()).isEqualTo("0đ");
            assertThat(response.totalWithdrawn()).isEqualByComparingTo("0.00");
            assertThat(response.formattedTotalWithdrawn()).isEqualTo("0đ");
            assertThat(response.eventRevenues()).isEmpty();
        }

        @Test
        @DisplayName("Hiển thị toàn bộ số dư bằng 0 và danh sách doanh thu trống khi có sự kiện nhưng chưa bán được vé nào")
        void shouldReturnZerosAndEmptyListWhenEventsHaveZeroSales() {
            Event event1 = new Event();
            event1.setId(10);
            event1.setName("Sự kiện chưa bán được vé");

            TicketType ticket1 = new TicketType();
            ticket1.setId(101);
            ticket1.setPrice(new BigDecimal("200000.00"));
            ticket1.setQuantity(50);
            ticket1.setRemainingQuantity(50); // Còn nguyên 50 vé -> đã bán 0
            ticket1.setReservedQuantity(0);

            when(userRepository.findById(1)).thenReturn(Optional.of(organizer));
            when(eventRepository.findByOrganizerIdOrderByIdAsc(1)).thenReturn(List.of(event1));
            when(financeLedger.eventRevenue(10)).thenReturn(BigDecimal.ZERO);
            when(ticketTypeRepository.findByEventIdOrderByIdAsc(10)).thenReturn(List.of(ticket1));

            OrganizerWalletResponse response = organizerWalletService.getWalletAndRevenue(1);

            assertThat(response.pendingBalance()).isEqualByComparingTo("0.00");
            assertThat(response.formattedPendingBalance()).isEqualTo("0đ");
            assertThat(response.availableBalance()).isEqualByComparingTo("0.00");
            assertThat(response.formattedAvailableBalance()).isEqualTo("0đ");
            assertThat(response.withdrawalPendingBalance()).isEqualByComparingTo("0.00");
            assertThat(response.formattedWithdrawalPendingBalance()).isEqualTo("0đ");
            assertThat(response.totalWithdrawn()).isEqualByComparingTo("0.00");
            assertThat(response.formattedTotalWithdrawn()).isEqualTo("0đ");
            assertThat(response.eventRevenues()).isEmpty();
        }
    }
}
