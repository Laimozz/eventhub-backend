package com.eventhub.backend.repository;

import com.eventhub.backend.entity.TicketType;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface TicketTypeRepository extends JpaRepository<TicketType, Integer> {
    List<TicketType> findByEventIdOrderByIdAsc(Integer eventId);

    @Query("select t from TicketType t where t.event.id in :eventIds order by t.id")
    List<TicketType> findByEventIds(Collection<Integer> eventIds);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from TicketType t where t.event.id = :eventId order by t.id")
    List<TicketType> findForUpdate(Integer eventId);
}
