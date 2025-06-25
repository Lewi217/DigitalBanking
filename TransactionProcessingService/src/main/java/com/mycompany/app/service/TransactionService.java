package com.mycompany.app.service;

import com.mycompany.app.client.NotificationServiceClient;
import com.mycompany.app.dto.TransactionDto;
import com.mycompany.app.dto.TransactionRequest;
import com.mycompany.app.model.Transaction;
import com.mycompany.app.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService implements ITransactionService {

    private final TransactionRepository repo;
    private final NotificationServiceClient notificationClient;

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

        // Send notification asynchronously
        try {
            notificationClient.sendTransactionNotification(
                    request.fromAccountId, // Assuming this maps to userId
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