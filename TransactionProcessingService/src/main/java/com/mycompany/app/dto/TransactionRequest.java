package com.mycompany.app.dto;

import com.mycompany.app.model.TransactionType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class TransactionRequest {
    public Long fromAccountId;
    public Long toAccountId;
    public BigDecimal amount;
    public TransactionType type;
    public Instant scheduleAt;
}
