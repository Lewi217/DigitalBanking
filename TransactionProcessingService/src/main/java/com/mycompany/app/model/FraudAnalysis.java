package com.mycompany.app.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "fraud_analysis")
public class FraudAnalysis {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String transactionId;
    private String accountId;
    private BigDecimal amount;
    private String transactionType;

    @Enumerated(EnumType.STRING)
    private RiskLevel riskLevel;

    private Integer riskScore;
    private String riskFactors;
    private Instant analyzedAt;

    @Enumerated(EnumType.STRING)
    private FraudStatus status;

    private String notes;
}