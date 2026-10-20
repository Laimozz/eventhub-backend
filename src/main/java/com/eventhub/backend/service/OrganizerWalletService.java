package com.eventhub.backend.service;

import com.eventhub.backend.dto.response.OrganizerWalletResponse;

public interface OrganizerWalletService {
    OrganizerWalletResponse getWalletAndRevenue(Integer organizerId);
}
