package com.fintech.app.service.impl;

import com.fintech.app.dto.request.TransferRequest;
import com.fintech.app.dto.response.PaginatedResponse;
import com.fintech.app.dto.response.TransactionResponse;
import com.fintech.app.entity.Account;
import com.fintech.app.entity.Transaction;
import com.fintech.app.entity.User;
import com.fintech.app.exception.BadRequestException;
import com.fintech.app.exception.InsufficientBalanceException;
import com.fintech.app.exception.ResourceNotFoundException;
import com.fintech.app.model.TransactionStatus;
import com.fintech.app.model.TransactionType;
import com.fintech.app.model.UserStatus;
import com.fintech.app.repository.AccountRepository;
import com.fintech.app.repository.TransactionRepository;
import com.fintech.app.repository.UserRepository;
import com.fintech.app.security.JwtUtil;
import com.fintech.app.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    @Override
    @Transactional
    public TransactionResponse transfer(TransferRequest request) {
        Long senderId = JwtUtil.getUserId();
        log.info("Transfer initiated: senderId={}, receiverId={}, amount={}",
                senderId, request.getReceiverUserId(), request.getAmount());

        if (senderId.equals(request.getReceiverUserId())) {
            throw new BadRequestException("Cannot transfer money to yourself");
        }

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("Sender user not found"));
        User receiver = userRepository.findById(request.getReceiverUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Receiver user not found"));

        if (sender.getStatus() != UserStatus.ACTIVE) {
            throw new BadRequestException("Sender account is not active");
        }
        if (receiver.getStatus() != UserStatus.ACTIVE) {
            throw new BadRequestException("Receiver account is not active");
        }

        Account senderAccount = accountRepository.findByUser(sender)
                .orElseThrow(() -> new ResourceNotFoundException("Sender account not found"));
        Account receiverAccount = accountRepository.findByUser(receiver)
                .orElseThrow(() -> new ResourceNotFoundException("Receiver account not found"));

        if (senderAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException(
                    "Insufficient balance. Available: ₹" + senderAccount.getBalance());
        }

        // Deduct from sender
        senderAccount.setBalance(senderAccount.getBalance().subtract(request.getAmount()));
        accountRepository.save(senderAccount);

        // Credit to receiver
        receiverAccount.setBalance(receiverAccount.getBalance().add(request.getAmount()));
        accountRepository.save(receiverAccount);

        // Save transaction record
        Transaction transaction = Transaction.builder()
                .sender(sender)
                .receiver(receiver)
                .amount(request.getAmount())
                .status(TransactionStatus.SUCCESS)
                .description(request.getDescription() != null ? request.getDescription() : "Fund Transfer")
                .build();

        transaction = transactionRepository.save(transaction);

        log.info("Transfer successful: transactionId={}, amount={}", transaction.getId(), request.getAmount());

        return mapToResponse(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<TransactionResponse> getTransactionHistory(Long userId, int page, int size,
                                                                        String sortBy, String orderBy) {
        log.info("Fetching transaction history for userId={}", userId);

        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        List<TransactionResponse> responses = new ArrayList<>();
        PageRequest pageRequest = PageRequest.of(page, size,
                Sort.by(Sort.Direction.fromString(orderBy.toUpperCase()), sortBy));

        Page<Transaction> transactions = transactionRepository.findAllByUserId(userId, pageRequest);
        log.info("Found {} transactions for userId={}", transactions.getContent().size(), userId);
        if (!transactions.isEmpty()) {
            transactions.forEach(transaction -> responses.add(mapToResponse(transaction)));
        }
        return PaginatedResponse.<TransactionResponse>builder()
                .list(responses)
                .page(page)
                .pageSize(size)
                .totalElements(transactions.getTotalElements())
                .totalPage(transactions.getTotalPages())
                .lastPage(transactions.isLast())
                .build();
    }

    private TransactionResponse mapToResponse(Transaction t) {
        return TransactionResponse.builder()
                .transactionId(t.getId())
                .senderId(t.getSender().getId())
                .senderName(t.getSender().getName())
                .receiverId(t.getReceiver().getId())
                .receiverName(t.getReceiver().getName())
                .amount(t.getAmount())
                .type(JwtUtil.getUserId().equals(t.getSender().getId()) ?
                        TransactionType.DEBIT.name() : TransactionType.CREDIT.name())
                .status(t.getStatus().name())
                .description(t.getDescription())
                .createdAt(t.getCreatedAt())
                .build();
    }
}
