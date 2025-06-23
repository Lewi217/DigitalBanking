package com.mycompany.app.service;

import com.mycompany.app.dto.TransactionDto;
import com.mycompany.app.dto.TransactionRequest;
import com.mycompany.app.model.Transaction;
import com.mycompany.app.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService implements ITransactionService{
    private final TransactionRepository repo;
    //private final KafkaTemplate<String, TransactionEvent> kafka;

    @Override
    public TransactionDto process(TransactionRequest request){
        //Save to db
        Transaction transaction = new Transaction();
        transaction.setFromAccountId(request.fromAccountId);
        transaction.setToAccountId(request.toAccountId);
        transaction.setAmount(request.amount);
        transaction.setType(request.type);
        transaction.setCreatedAt(Instant.now());
        transaction.setScheduledAt(request.scheduleAt);
        transaction = repo.save(transaction);

//        //publish event
//        TransactionEvent event = new TransactionEvent(
//                transaction.getId().toString(),
//                transaction.getFromAccountId(),
//                transaction.getToAccountId(),
//                transaction.getAmount(),
//                transaction.getCreatedAt(),
//                transaction.getType()
//        );
//        kafka.send(Topics.TRANSACTION_EVENTS, event.getTransactionId(), event);

        // Return the TransactionDto
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