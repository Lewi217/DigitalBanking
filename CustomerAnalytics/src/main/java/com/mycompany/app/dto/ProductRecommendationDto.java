package com.mycompany.app.dto;

import com.mycompany.app.model.RecommendationStatus;
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
public class ProductRecommendationDto {
    private Long id;
    private String userId;
    private String productType;
    private String productName;
    private String description;
    private Double score;
    private String reasoning;
    private Instant createdAt;
    private Instant validUntil;
    private RecommendationStatus status;
    private BigDecimal estimatedValue;
    private String targetAudience;
}