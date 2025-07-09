package com.mycompany.app.service.analyticsSummary;

import com.mycompany.app.dto.AccountDto;
import com.mycompany.app.dto.AnalyticsSummaryDto;
import com.mycompany.app.dto.TransactionDto;
import com.mycompany.app.model.AnalyticsSummary;
import com.mycompany.app.model.UserBehavior;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public interface IAnalyticsSummaryService {
    AnalyticsSummaryDto generateSummary(String userId, String period);
    List<TransactionDto> fetchUserTransactions(String userId, List<AccountDto> accounts, Instant start, Instant end);
    AnalyticsSummary calculateSummaryMetrics(String userId, String period, Instant start, Instant end, List<UserBehavior> behaviors, List<TransactionDto> transactions);
    String findMostUsedFeature(List<UserBehavior> behaviors);
    Map<String, String> calculateAdditionalMetrics(List<UserBehavior> behaviors);
    Instant[] calculatePeriodBounds(String period);
    List<AnalyticsSummaryDto> getUserSummaries(String userId, String period);
    AnalyticsSummaryDto getLatestSummary(String userId);
    List<AnalyticsSummaryDto> getSummariesByDateRange(String userId, Instant start, Instant end);
    Map<String, Object> getUserMetricsSummary(String userId);
    void generateDailySummaries();
    void generateWeeklySummaries();
    void generateMonthlySummaries();
    AnalyticsSummaryDto convertToDto(AnalyticsSummary summary);
}
