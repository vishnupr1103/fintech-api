package com.fintech.app.service;

import com.fintech.app.dto.request.TransferRequest;
import com.fintech.app.dto.response.PaginatedResponse;
import com.fintech.app.dto.response.TransactionResponse;

public interface TransactionService {
    TransactionResponse transfer(TransferRequest request);

    PaginatedResponse<TransactionResponse> getTransactionHistory(Long userId, int page, int size, String sortBy, String orderBy);
}
