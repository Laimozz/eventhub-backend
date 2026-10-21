package com.eventhub.backend.service;

import com.eventhub.backend.dto.request.CreateWithdrawalRequest;
import com.eventhub.backend.dto.response.WithdrawalRequestPreviewResponse;
import com.eventhub.backend.dto.response.WithdrawalRequestResponse;
import java.util.List;

public interface WithdrawalRequestService {

    WithdrawalRequestResponse createWithdrawalRequest(Integer organizerId, CreateWithdrawalRequest request);

    WithdrawalRequestPreviewResponse previewWithdrawalRequest(Integer organizerId, CreateWithdrawalRequest request);

    List<WithdrawalRequestResponse> getWithdrawalRequests(Integer organizerId);

    List<WithdrawalRequestResponse> getWithdrawalRequests(Integer organizerId, String status);

    WithdrawalRequestResponse getWithdrawalRequestDetail(Integer organizerId, Integer requestId);

    WithdrawalRequestResponse cancelWithdrawalRequest(Integer organizerId, Integer requestId);
}
