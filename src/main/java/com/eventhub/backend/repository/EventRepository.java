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
    @EntityGraph(attributePaths = {"category", "organizer"})
    Page<Event> findByStatus(EventStatus status, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Event e where e.id = :eventId")
    Optional<Event> findForReview(Integer eventId);

    boolean existsByCategoryId(Integer categoryId);
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

    // Bump the revision even when only guests/tickets change, not scalar event fields.
    @Lock(LockModeType.PESSIMISTIC_FORCE_INCREMENT)
    @Query("select e from Event e where e.id = :eventId and e.organizer.id = :organizerId")
    Optional<Event> findOwnedForUpdate(Integer eventId, Integer organizerId);

    long countByVenueId(Integer venueId);
}
