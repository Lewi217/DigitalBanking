package com.mycompany.app.service;

import com.mycompany.app.dto.TransactionDto;
import com.mycompany.app.dto.TransactionRequest;

import java.time.Instant;
import java.util.List;

public interface ITransactionService {
    TransactionDto process(TransactionRequest request);
    List<TransactionDto> history(Long accountId);
    List<TransactionDto> search(Instant from, Instant to);
}
