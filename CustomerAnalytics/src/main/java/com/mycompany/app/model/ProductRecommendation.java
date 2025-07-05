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

@Document(collection = "product_recommendations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRecommendation {
    @Id
    private String id;

    @Field("user_id")
    @Indexed
    private String userId;

    @Field("product_type")
    private String productType;

    @Field("product_name")
    private String productName;

    @Field("description")
    private String description;

    @Field("score")
    private Double score;

    @Field("reasoning")
    private String reasoning;

    @Field("created_at")
    private Instant createdAt;

    @Field("valid_until")
    private Instant validUntil;

    @Field("status")
    private RecommendationStatus status;

    @Field("estimated_value")
    private BigDecimal estimatedValue;

    @Field("target_audience")
    private String targetAudience;
}

