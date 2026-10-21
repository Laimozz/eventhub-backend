package com.eventhub.backend.repository.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class StaffProjections {
    private StaffProjections() {}

    public interface CustomerSelect {
        Integer getId();
        String getFullName();
        String getEmail();
        String getPhone();
        Long getTicketCount();
    }

    public interface TicketTypeSelect {
        Integer getId();
        String getName();
        BigDecimal getPrice();
        Integer getQuantity();
    }

    public interface TicketStats {
        Long getTotalTickets();
        Long getCheckedInTickets();
        Long getRemainingTickets();
    }

    public interface TicketItem {
        Integer getTicketId();
        String getTicketCode();
        String getStatus();
        LocalDateTime getCheckedInAt();
        Integer getCustomerId();
        String getCustomerName();
        String getCustomerEmail();
        String getCustomerPhone();
        Integer getTicketTypeId();
        String getTicketTypeName();
        BigDecimal getTicketPrice();
        String getCheckedInByName();
    }

    public interface EventDetail {
        Integer getEventId();
        String getEventName();
        String getDescription();
        String getThumbnailImageUrl();
        String getBannerImageUrl();
        String getImageZoneUrl();
        LocalDateTime getStartTime();
        LocalDateTime getEndTime();
        String getEventStatus();
        String getVenueCity();
        String getVenueAddress();
        Integer getVenueCapacity();
        String getOrganizerName();
        String getStaffStatus();
        LocalDateTime getAssignedAt();
    }
}
