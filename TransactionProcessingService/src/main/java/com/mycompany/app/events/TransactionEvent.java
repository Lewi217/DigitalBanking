package com.mycompany.app.events;

import com.mycompany.app.model.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionEvent {
    private String transactionId;
    private Long fromAccountId;
    private Long toAccountId;
    private BigDecimal amount;
    private Instant timestamp;
    private TransactionType type;
}
