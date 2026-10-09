package com.eventhub.backend.repository;

import com.eventhub.backend.entity.Event;
import com.eventhub.backend.enums.EventStatus;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface EventRepository extends JpaRepository<Event, Integer> {
    @EntityGraph(attributePaths = {"venue", "category"})
    @Query("""
            select e from Event e where e.organizer.id = :organizerId
            and (:status is null or e.status = :status)
            and lower(e.name) like :search escape '!'
            """)
    Page<Event> findOrganizerEvents(Integer organizerId, EventStatus status, String search, Pageable pageable);

    @Query("select e.status as status, count(e) as total from Event e where e.organizer.id = :organizerId group by e.status")
    List<StatusCount> countOrganizerStatuses(Integer organizerId);

    interface StatusCount {
        EventStatus getStatus();
        long getTotal();
    }

    @EntityGraph(attributePaths = {"venue", "category", "organizer"})
    Optional<Event> findByIdAndOrganizerId(Integer id, Integer organizerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Event e where e.id = :eventId and e.organizer.id = :organizerId")
    Optional<Event> findOwnedForUpdate(Integer eventId, Integer organizerId);

    long countByVenueId(Integer venueId);

    @Query("""
            select new com.eventhub.backend.dto.response.PublicEventSummaryResponse(
                e.id, e.name, e.thumbnailImageUrl, e.startTime, e.endTime, e.venue.city,
                (select min(t.price) from TicketType t where t.event.id = e.id)
            )
            from Event e
            where e.status in :statuses
            and (:categoryId is null or e.category.id = :categoryId)
            and (:city is null or lower(e.venue.city) = :city)
            and (cast(:fromDate as timestamp) is null or e.startTime >= :fromDate)
            and (cast(:toDate as timestamp) is null or e.startTime <= :toDate)
            and lower(e.name) like :search escape '!'
            """)
    Page<com.eventhub.backend.dto.response.PublicEventSummaryResponse> findPublicEvents(
            Integer categoryId, String city, java.time.LocalDateTime fromDate, java.time.LocalDateTime toDate,
            String search, List<EventStatus> statuses, Pageable pageable);

    @Query("""
            select new com.eventhub.backend.dto.response.PublicEventSummaryResponse(
                e.id, e.name, e.thumbnailImageUrl, e.startTime, e.endTime, e.venue.city,
                (select min(t.price) from TicketType t where t.event.id = e.id)
            )
            from Event e
            where e.status in :statuses
            and e.id != :eventId
            order by e.createdAt desc
            """)
    List<com.eventhub.backend.dto.response.PublicEventSummaryResponse> findSuggestedEvents(Integer eventId, List<EventStatus> statuses, Pageable pageable);
}
