package com.mycompany.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsSummaryDto {
    private Long id;
    private String userId;
    private String period;
    private Instant periodStart;
    private Instant periodEnd;
    private Integer totalTransactions;
    private BigDecimal totalSpending;
    private BigDecimal totalIncome;
    private BigDecimal averageTransactionAmount;
    private Integer loginCount;
    private Double totalSessionDuration;
    private String mostUsedFeature;
    private Map<String, String> additionalMetrics;
    private Instant createdAt;
}
