package com.mycompany.app.dto;

import com.mycompany.app.model.PatternType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancialPatternDto {
    private String id;
    private String userId;
    private String accountId;
    private PatternType patternType;
    private BigDecimal averageAmount;
    private BigDecimal totalAmount;
    private Integer frequency;
    private String category;
    private String description;
    private Instant analyzedAt;
    private Instant periodStart;
    private Instant periodEnd;
    private Double confidence;
}
