package com.mycompany.app.repository;

import com.mycompany.app.model.AnalyticsSummary;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface AnalyticsSummaryRepository extends MongoRepository<AnalyticsSummary, String> {

    List<AnalyticsSummary> findByUserId(String userId);
    List<AnalyticsSummary> findByUserIdAndPeriod(String userId, String period);
    List<AnalyticsSummary> findByUserIdAndPeriodOrderByPeriodStartDesc(String userId, String period);
    List<AnalyticsSummary> findByPeriodStartBetweenOrderByPeriodStartDesc(Instant start, Instant end);
}