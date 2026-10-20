package com.eventhub.backend.service;

import com.eventhub.backend.dto.request.AddEventStaffRequest;
import com.eventhub.backend.dto.request.UpdateEventStaffStatusRequest;
import com.eventhub.backend.dto.response.EventStaffResponse;
import com.eventhub.backend.entity.Event;
import com.eventhub.backend.entity.EventStaff;
import com.eventhub.backend.entity.User;
import com.eventhub.backend.enums.EventStatus;
import com.eventhub.backend.enums.Role;
import com.eventhub.backend.exception.EventStaffException;
import com.eventhub.backend.repository.EventRepository;
import com.eventhub.backend.repository.EventStaffRepository;
import com.eventhub.backend.repository.TicketRepository;
import com.eventhub.backend.repository.UserRepository;
import com.eventhub.backend.service.impl.EventStaffServiceImpl;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventStaffServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventStaffRepository eventStaffRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private EventStaffServiceImpl eventStaffService;

    private User organizer;
    private Event event;
    private User customer;

    @BeforeEach
    void setUp() {
        organizer = new User();
        organizer.setId(1);
        organizer.setEmail("organizer@example.com");
        organizer.setRole(Role.ORGANIZER);
        organizer.setStatus("ACTIVE");

        event = new Event();
        event.setId(10);
        event.setName("Hòa nhạc Mùa Thu");
        event.setOrganizer(organizer);
        event.setStatus(EventStatus.APPROVED);
        event.setStartTime(LocalDateTime.now(ZoneOffset.UTC).plusDays(2));
        event.setEndTime(LocalDateTime.now(ZoneOffset.UTC).plusDays(2).plusHours(3));

        customer = new User();
        customer.setId(2);
        customer.setEmail("nguyennam60yb@gmail.com");
        customer.setFullName("Nguyễn Đăng Nam");
        customer.setRole(Role.CUSTOMER);
        customer.setStatus("ACTIVE");
    }

    @Nested
    @DisplayName("Pre-condition: Event verification tests")
    class EventVerificationTests {
        @Test
        void shouldThrowNotFoundWhenEventDoesNotExist() {
            when(eventRepository.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> eventStaffService.getStaffs(organizer.getId(), 999))
                    .isInstanceOf(EventStaffException.class)
                    .satisfies(ex -> {
                        EventStaffException e = (EventStaffException) ex;
                        assertThat(e.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                        assertThat(e.getCode()).isEqualTo("EVENT_NOT_FOUND");
                    });
        }

        @Test
        void shouldThrowNotFoundWhenEventBelongsToAnotherOrganizer() {
            User otherOrganizer = new User();
            otherOrganizer.setId(99);
            event.setOrganizer(otherOrganizer);

            when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));

            assertThatThrownBy(() -> eventStaffService.getStaffs(organizer.getId(), event.getId()))
                    .isInstanceOf(EventStaffException.class)
                    .satisfies(ex -> {
                        EventStaffException e = (EventStaffException) ex;
                        assertThat(e.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                        assertThat(e.getCode()).isEqualTo("EVENT_NOT_FOUND");
                    });
        }

        @Test
        void shouldThrowBadRequestWhenEventIsNotApproved() {
            event.setStatus(EventStatus.PENDING_APPROVAL);
            when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));

            assertThatThrownBy(() -> eventStaffService.getStaffs(organizer.getId(), event.getId()))
                    .isInstanceOf(EventStaffException.class)
                    .satisfies(ex -> {
                        EventStaffException e = (EventStaffException) ex;
                        assertThat(e.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                        assertThat(e.getCode()).isEqualTo("EVENT_NOT_APPROVED");
                        assertThat(e.getMessage()).contains("Admin duyệt");
                    });
        }
    }

    @Nested
    @DisplayName("List Staffs tests (Main Events 1 & 2)")
    class ListStaffsTests {
        @Test
        void shouldReturnStaffListSuccessfully() {
            when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));

            EventStaff staff1 = new EventStaff();
            staff1.setId(101);
            staff1.setEvent(event);
            staff1.setStaff(customer);
            staff1.setAssignedBy(organizer);
            staff1.setStatus("ACTIVE");

            when(eventStaffRepository.findByEventIdOrderByCreatedAtDesc(event.getId()))
                    .thenReturn(List.of(staff1));
            when(ticketRepository.existsByCheckedInByIdAndEventId(customer.getId(), event.getId()))
                    .thenReturn(false);

            List<EventStaffResponse> result = eventStaffService.getStaffs(organizer.getId(), event.getId());

            assertThat(result).hasSize(1);
            assertThat(result.get(0).email()).isEqualTo("nguyennam60yb@gmail.com");
            assertThat(result.get(0).fullName()).isEqualTo("Nguyễn Đăng Nam");
            assertThat(result.get(0).status()).isEqualTo("ACTIVE");
        }
    }

    @Nested
    @DisplayName("Add Staff tests (UC13 Main events 3-6 & Exceptions 5.1, 5.2)")
    class AddStaffTests {
        @Test
        void shouldAddStaffSuccessfullyAndPromoteRole() {
            when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
            when(userRepository.findById(organizer.getId())).thenReturn(Optional.of(organizer));
            when(userRepository.findByNormalizedEmail("nguyennam60yb@gmail.com")).thenReturn(Optional.of(customer));
            when(eventStaffRepository.existsByEventIdAndStaffId(event.getId(), customer.getId())).thenReturn(false);
            when(ticketRepository.existsByCheckedInByIdAndEventId(customer.getId(), event.getId())).thenReturn(false);

            EventStaff saved = new EventStaff();
            saved.setId(101);
            saved.setEvent(event);
            saved.setStaff(customer);
            saved.setAssignedBy(organizer);
            saved.setStatus("ACTIVE");

            when(eventStaffRepository.save(any(EventStaff.class))).thenReturn(saved);

            AddEventStaffRequest request = new AddEventStaffRequest("nguyennam60yb@gmail.com");
            EventStaffResponse response = eventStaffService.addStaff(organizer.getId(), event.getId(), request);

            assertThat(response).isNotNull();
            assertThat(response.email()).isEqualTo("nguyennam60yb@gmail.com");
            assertThat(response.status()).isEqualTo("ACTIVE");
            assertThat(customer.getRole()).isEqualTo(Role.STAFF);
            verify(userRepository).save(customer);
        }

        @Test
        void shouldThrowNotFoundWhenEmailDoesNotExist_Exception5_1() {
            when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
            when(userRepository.findById(organizer.getId())).thenReturn(Optional.of(organizer));
            when(userRepository.findByNormalizedEmail("unknown@gmail.com")).thenReturn(Optional.empty());

            AddEventStaffRequest request = new AddEventStaffRequest("unknown@gmail.com");
            assertThatThrownBy(() -> eventStaffService.addStaff(organizer.getId(), event.getId(), request))
                    .isInstanceOf(EventStaffException.class)
                    .satisfies(ex -> {
                        EventStaffException e = (EventStaffException) ex;
                        assertThat(e.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                        assertThat(e.getCode()).isEqualTo("USER_NOT_FOUND");
                        assertThat(e.getMessage()).isEqualTo("Email nhập vào không tồn tại trong hệ thống");
                    });
        }

        @Test
        void shouldThrowBadRequestWhenUserNotCustomer_Exception5_2() {
            User organizerAccount = new User();
            organizerAccount.setId(3);
            organizerAccount.setEmail("another_organizer@gmail.com");
            organizerAccount.setRole(Role.ORGANIZER);
            organizerAccount.setStatus("ACTIVE");

            when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
            when(userRepository.findById(organizer.getId())).thenReturn(Optional.of(organizer));
            when(userRepository.findByNormalizedEmail("another_organizer@gmail.com")).thenReturn(Optional.of(organizerAccount));

            AddEventStaffRequest request = new AddEventStaffRequest("another_organizer@gmail.com");
            assertThatThrownBy(() -> eventStaffService.addStaff(organizer.getId(), event.getId(), request))
                    .isInstanceOf(EventStaffException.class)
                    .satisfies(ex -> {
                        EventStaffException e = (EventStaffException) ex;
                        assertThat(e.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                        assertThat(e.getCode()).isEqualTo("NOT_CUSTOMER_ACCOUNT");
                        assertThat(e.getMessage()).isEqualTo("Email nhập vào không thuộc tài khoản Customer");
                    });
        }

        @Test
        void shouldThrowConflictWhenStaffAlreadyAssigned() {
            when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
            when(userRepository.findById(organizer.getId())).thenReturn(Optional.of(organizer));
            when(userRepository.findByNormalizedEmail("nguyennam60yb@gmail.com")).thenReturn(Optional.of(customer));
            when(eventStaffRepository.existsByEventIdAndStaffId(event.getId(), customer.getId())).thenReturn(true);

            AddEventStaffRequest request = new AddEventStaffRequest("nguyennam60yb@gmail.com");
            assertThatThrownBy(() -> eventStaffService.addStaff(organizer.getId(), event.getId(), request))
                    .isInstanceOf(EventStaffException.class)
                    .satisfies(ex -> {
                        EventStaffException e = (EventStaffException) ex;
                        assertThat(e.getStatus()).isEqualTo(HttpStatus.CONFLICT);
                        assertThat(e.getCode()).isEqualTo("STAFF_ALREADY_ASSIGNED");
                    });
        }
    }

    @Nested
    @DisplayName("Update Staff Status tests (UC13 Main events 7 & 8)")
    class UpdateStaffStatusTests {
        @Test
        void shouldUpdateStaffStatusFromLockedToActive() {
            when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));

            EventStaff staff = new EventStaff();
            staff.setId(102);
            staff.setEvent(event);
            staff.setStaff(customer);
            staff.setAssignedBy(organizer);
            staff.setStatus("LOCKED");

            when(eventStaffRepository.findByIdAndEventId(102, event.getId())).thenReturn(Optional.of(staff));
            when(eventStaffRepository.save(staff)).thenReturn(staff);
            when(ticketRepository.existsByCheckedInByIdAndEventId(customer.getId(), event.getId())).thenReturn(false);

            UpdateEventStaffStatusRequest request = new UpdateEventStaffStatusRequest("ACTIVE");
            EventStaffResponse response = eventStaffService.updateStaffStatus(organizer.getId(), event.getId(), 102, request);

            assertThat(response.status()).isEqualTo("ACTIVE");
            verify(eventStaffRepository).save(staff);
        }
    }

    @Nested
    @DisplayName("Delete Staff tests (UC13 Main events 9 & 10)")
    class DeleteStaffTests {
        @Test
        void shouldDeleteStaffAndRevertRoleWhenNoOtherAssignments() {
            when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));

            customer.setRole(Role.STAFF);
            EventStaff staff = new EventStaff();
            staff.setId(103);
            staff.setEvent(event);
            staff.setStaff(customer);
            staff.setAssignedBy(organizer);
            staff.setStatus("ACTIVE");

            when(eventStaffRepository.findByIdAndEventId(103, event.getId())).thenReturn(Optional.of(staff));
            when(eventStaffRepository.countByStaffId(customer.getId())).thenReturn(0L);

            eventStaffService.deleteStaff(organizer.getId(), event.getId(), 103);

            verify(eventStaffRepository).delete(staff);
            assertThat(customer.getRole()).isEqualTo(Role.CUSTOMER);
            verify(userRepository).save(customer);
        }
    }
}
