package com.eventhub.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record UserInteractionRequest(
        Integer userId,
        @NotNull Integer eventId,
        @NotBlank @Pattern(regexp = "VIEW|CLICK|SEARCH_CLICK|BOOK", message = "Action type must be VIEW, CLICK, SEARCH_CLICK or BOOK") String actionType,
        Integer dwellSeconds,
        Integer recLogId
) {
    public UserInteractionRequest {
        if (dwellSeconds == null || dwellSeconds < 0) {
            dwellSeconds = 0;
        }
    }
}
