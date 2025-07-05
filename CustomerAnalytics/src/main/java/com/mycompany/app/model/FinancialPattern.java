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

@Document(collection = "financial_patterns")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancialPattern {
    @Id
    private String id;

    @Field("user_id")
    @Indexed
    private String userId;

    @Field("account_id")
    @Indexed
    private String accountId;

    @Field("pattern_type")
    private PatternType patternType;

    @Field("average_amount")
    private BigDecimal averageAmount;

    @Field("total_amount")
    private BigDecimal totalAmount;

    @Field("frequency")
    private Integer frequency;

    @Field("category")
    private String category;

    @Field("description")
    private String description;

    @Field("analyzed_at")
    private Instant analyzedAt;

    @Field("period_start")
    private Instant periodStart;

    @Field("period_end")
    private Instant periodEnd;

    @Field("confidence")
    private Double confidence;
}