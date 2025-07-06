package com.mycompany.app.repository;

import com.mycompany.app.model.FinancialPattern;
import com.mycompany.app.model.PatternType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface FinancialPatternRepository extends MongoRepository<FinancialPattern, String> {

    List<FinancialPattern> findByUserId(String userId);
    List<FinancialPattern> findByUserIdAndPatternType(String userId, PatternType patternType);
    List<FinancialPattern> findByAccountId(String accountId);
    List<FinancialPattern> findByUserIdAndConfidenceGreaterThanEqualOrderByConfidenceDesc(String userId, Double minConfidence);
    List<FinancialPattern> findByAnalyzedAtBetweenOrderByAnalyzedAtDesc(Instant start, Instant end);
}
