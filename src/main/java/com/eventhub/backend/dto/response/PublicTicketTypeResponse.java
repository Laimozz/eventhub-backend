package com.eventhub.backend.dto.response;

import com.eventhub.backend.enums.TicketSalesStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PublicTicketTypeResponse(
        Integer id, String name, String description, BigDecimal price, int remainingQuantity,
        LocalDateTime saleStartTime, LocalDateTime saleEndTime, TicketSalesStatus salesStatus) {}
