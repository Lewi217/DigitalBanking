package com.mycompany.app.dto;

import com.mycompany.app.model.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDto {
    private Long id;
    public BigDecimal amount;
    public TransactionType type;
    public Instant timestamp;
}
