package com.mycompany.app.service;

import com.mycompany.app.client.AuthServiceClient;
import com.mycompany.app.client.NotificationServiceClient;
import com.mycompany.app.dto.TransactionDto;
import com.mycompany.app.dto.TransactionRequest;
import com.mycompany.app.dto.UserDto;
import com.mycompany.app.events.TransactionCreatedEvent;
import com.mycompany.app.model.Transaction;
import com.mycompany.app.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService implements ITransactionService {

    private final TransactionRepository repo;
    private final AuthServiceClient authServiceClient;
    private final NotificationServiceClient notificationClient;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public TransactionDto process(TransactionRequest request) {
        // Save to db
        Transaction transaction = new Transaction();
        transaction.setFromAccountId(request.fromAccountId);
        transaction.setToAccountId(request.toAccountId);
        transaction.setAmount(request.amount);
        transaction.setType(request.type);
        transaction.setCreatedAt(Instant.now());
        transaction.setScheduledAt(request.scheduleAt);
        transaction = repo.save(transaction);

        try {
            UserDto user = authServiceClient.getUserById(Long.parseLong(request.fromAccountId));
            log.info("Transaction created by: {}", user.getName());
        } catch (Exception e) {
            log.warn("Failed to fetch user details for ID {}: {}", request.fromAccountId, e.getMessage());
        }

        eventPublisher.publishEvent(new TransactionCreatedEvent(this, transaction));
        // Send notification asynchronously
        try {
            notificationClient.sendTransactionNotification(
                    request.fromAccountId,
                    request.type.toString(),
                    request.amount.toString(),
                    request.fromAccountId
            );
        } catch (Exception e) {
        }

        return new TransactionDto(
                transaction.getId(),
                transaction.getAmount(),
                transaction.getType(),
                transaction.getCreatedAt()
        );
    }

    @Override
    public List<TransactionDto> history(String accountId){
        return repo.findByFromAccountIdOrToAccountId(accountId, accountId)
                .stream()
                .map(transaction -> new TransactionDto(
                        transaction.getId(),
                        transaction.getAmount(),
                        transaction.getType(),
                        transaction.getCreatedAt())
                )
                .toList();
    }

    @Override
    public List<TransactionDto> search(Instant from, Instant to){
        return repo.findByCreatedAtBetween(from,to)
                .stream()
                .map(transaction -> new TransactionDto(
                        transaction.getId(),
                        transaction.getAmount(),
                        transaction.getType(),
                        transaction.getCreatedAt()
                ))
                .toList();
    }
}