package com.mycompany.app.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

@Document(collection = "analytics_summaries")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsSummary {
    @Id
    private String id;

    @Field("user_id")
    @Indexed
    private String userId;

    @Field("period")
    private String period;

    @Field("period_start")
    private Instant periodStart;

    @Field("period_end")
    private Instant periodEnd;

    @Field("total_transactions")
    private Integer totalTransactions;

    @Field("total_spending")
    private BigDecimal totalSpending;

    @Field("total_income")
    private BigDecimal totalIncome;

    @Field("average_transaction_amount")
    private BigDecimal averageTransactionAmount;

    @Field("login_count")
    private Integer loginCount;

    @Field("total_session_duration")
    private Double totalSessionDuration;

    @Field("most_used_feature")
    private String mostUsedFeature;

    @Field("additional_metrics")
    private Map<String, String> additionalMetrics;

    @Field("created_at")
    private Instant createdAt;
}