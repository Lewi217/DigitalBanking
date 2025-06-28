package com.mycompany.app.repository;


import com.mycompany.app.model.FraudAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.time.Instant;

@Repository
public interface FraudAnalysisRepository extends JpaRepository<FraudAnalysis, Long> {
    List<FraudAnalysis> findByAccountId(String accountId);
    List<FraudAnalysis> findByRiskLevel(com.mycompany.app.model.RiskLevel riskLevel);
    List<FraudAnalysis> findByAnalyzedAtBetween(Instant start, Instant end);
}