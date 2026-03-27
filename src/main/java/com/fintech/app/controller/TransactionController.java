package com.fintech.app.controller;

import com.fintech.app.dto.request.TransferRequest;
import com.fintech.app.dto.response.ApiResponse;
import com.fintech.app.dto.response.PaginatedResponse;
import com.fintech.app.dto.response.TransactionResponse;
import com.fintech.app.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Transactions", description = "Money transfer and transaction history APIs")
@SecurityRequirement(name = "Bearer Authentication")
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/transfer")
    @Operation(summary = "Transfer money", description = "Transfer funds between two active users")
    public ResponseEntity<ApiResponse<TransactionResponse>> transfer(@Valid @RequestBody TransferRequest request) {
        log.info("POST /api/transfer -  receiver={}, amount={}", request.getReceiverUserId(), request.getAmount());
        TransactionResponse response = transactionService.transfer(request);
        return ResponseEntity.ok(ApiResponse.success("Transfer successful", response));
    }

    @GetMapping("/transactions/{userId}")
    @Operation(summary = "Get transaction history", description = "Fetch all transactions for a given user ID")
    public ResponseEntity<ApiResponse<PaginatedResponse<TransactionResponse>>> getTransactionHistory(@PathVariable Long userId,
                                                                                                     @RequestParam(defaultValue = "0") int page,
                                                                                                     @RequestParam(defaultValue = "10") int size,
                                                                                                     @RequestParam(defaultValue = "id") String sortBy,
                                                                                                     @RequestParam(defaultValue = "desc") String orderBy) {
        log.info("GET /api/transactions/{}", userId);
        PaginatedResponse<TransactionResponse> transactions = transactionService.getTransactionHistory(userId,page,size,sortBy,orderBy);
        return ResponseEntity.ok(ApiResponse.success("Transactions fetched successfully", transactions));
    }

}
