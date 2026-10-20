package com.eventhub.backend.service.impl;

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
import com.eventhub.backend.service.EventStaffService;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class EventStaffServiceImpl implements EventStaffService {

    private final EventRepository eventRepository;
    private final EventStaffRepository eventStaffRepository;
    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;

    @Override
    @Transactional(readOnly = true)
    public List<EventStaffResponse> getStaffs(Integer organizerId, Integer eventId) {
        Event event = getAndVerifyEvent(organizerId, eventId);
        List<EventStaff> staffs = eventStaffRepository.findByEventIdOrderByCreatedAtDesc(eventId);
        return staffs.stream()
                .map(staff -> toResponse(event, staff))
                .toList();
    }

    @Override
    public EventStaffResponse addStaff(Integer organizerId, Integer eventId, AddEventStaffRequest request) {
        Event event = getAndVerifyEvent(organizerId, eventId);
        User organizer = userRepository.findById(organizerId)
                .orElseThrow(() -> new EventStaffException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Phiên đăng nhập không hợp lệ"));

        String email = request.email();
        User candidate = userRepository.findByNormalizedEmail(email)
                .orElseThrow(() -> new EventStaffException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND",
                        "Email nhập vào không tồn tại trong hệ thống"));

        if (candidate.getRole() != Role.CUSTOMER && candidate.getRole() != Role.STAFF) {
            throw new EventStaffException(HttpStatus.BAD_REQUEST, "NOT_CUSTOMER_ACCOUNT",
                    "Email nhập vào không thuộc tài khoản Customer");
        }

        if (eventStaffRepository.existsByEventIdAndStaffId(eventId, candidate.getId())) {
            throw new EventStaffException(HttpStatus.CONFLICT, "STAFF_ALREADY_ASSIGNED",
                    "Staff này đã được phân công cho sự kiện");
        }

        if (!"ACTIVE".equalsIgnoreCase(candidate.getStatus())) {
            throw new EventStaffException(HttpStatus.BAD_REQUEST, "USER_LOCKED",
                    "Tài khoản người dùng đang bị khóa");
        }

        EventStaff eventStaff = new EventStaff();
        eventStaff.setEvent(event);
        eventStaff.setStaff(candidate);
        eventStaff.setAssignedBy(organizer);
        eventStaff.setStatus("ACTIVE");
        EventStaff saved = eventStaffRepository.save(eventStaff);

        if (candidate.getRole() == Role.CUSTOMER) {
            candidate.setRole(Role.STAFF);
            userRepository.save(candidate);
        }

        return toResponse(event, saved);
    }

    @Override
    public EventStaffResponse updateStaffStatus(Integer organizerId, Integer eventId, Integer staffId,
            UpdateEventStaffStatusRequest request) {
        Event event = getAndVerifyEvent(organizerId, eventId);
        EventStaff eventStaff = findEventStaff(eventId, staffId);

        eventStaff.setStatus(request.status());
        EventStaff saved = eventStaffRepository.save(eventStaff);
        return toResponse(event, saved);
    }

    @Override
    public void deleteStaff(Integer organizerId, Integer eventId, Integer staffId) {
        getAndVerifyEvent(organizerId, eventId);
        EventStaff eventStaff = findEventStaff(eventId, staffId);
        User staffUser = eventStaff.getStaff();

        eventStaffRepository.delete(eventStaff);
        eventStaffRepository.flush();

        if (eventStaffRepository.countByStaffId(staffUser.getId()) == 0 && staffUser.getRole() == Role.STAFF) {
            staffUser.setRole(Role.CUSTOMER);
            userRepository.save(staffUser);
        }
    }

    private Event getAndVerifyEvent(Integer organizerId, Integer eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventStaffException(HttpStatus.NOT_FOUND, "EVENT_NOT_FOUND", "Không tìm thấy sự kiện"));

        if (!event.getOrganizer().getId().equals(organizerId)) {
            throw new EventStaffException(HttpStatus.NOT_FOUND, "EVENT_NOT_FOUND", "Không tìm thấy sự kiện");
        }

        EventStatus status = event.getStatus();
        if (status != EventStatus.APPROVED && status != EventStatus.ONGOING && status != EventStatus.COMPLETED) {
            throw new EventStaffException(HttpStatus.BAD_REQUEST, "EVENT_NOT_APPROVED",
                    "Chỉ có thể quản lý staff cho sự kiện đã được Admin duyệt");
        }

        return event;
    }

    private EventStaff findEventStaff(Integer eventId, Integer staffId) {
        return eventStaffRepository.findByIdAndEventId(staffId, eventId)
                .or(() -> eventStaffRepository.findByEventIdAndStaffId(eventId, staffId))
                .orElseThrow(() -> new EventStaffException(HttpStatus.NOT_FOUND, "STAFF_NOT_FOUND",
                        "Không tìm thấy staff trong sự kiện"));
    }

    private boolean computeInCheckinShift(Event event, EventStaff eventStaff) {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        boolean isOngoing = event.getStatus() == EventStatus.ONGOING;
        boolean isWithinTime = !now.isBefore(event.getStartTime().minusHours(2)) && !now.isAfter(event.getEndTime());
        boolean hasCheckedInTickets = ticketRepository.existsByCheckedInByIdAndEventId(
                eventStaff.getStaff().getId(), event.getId());
        return "ACTIVE".equalsIgnoreCase(eventStaff.getStatus()) && (isOngoing || isWithinTime || hasCheckedInTickets);
    }

    private EventStaffResponse toResponse(Event event, EventStaff eventStaff) {
        return new EventStaffResponse(
                eventStaff.getId(),
                eventStaff.getStaff().getId(),
                eventStaff.getStaff().getFullName(),
                eventStaff.getStaff().getEmail(),
                eventStaff.getStaff().getPhone(),
                eventStaff.getStatus(),
                computeInCheckinShift(event, eventStaff),
                eventStaff.getCreatedAt()
        );
    }
}
