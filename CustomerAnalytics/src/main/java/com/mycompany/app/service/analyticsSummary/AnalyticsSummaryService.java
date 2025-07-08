package com.mycompany.app.service.analyticsSummary;

import com.mycompany.app.client.AccountServiceClient;
import com.mycompany.app.client.TransactionServiceClient;
import com.mycompany.app.dto.AccountDto;
import com.mycompany.app.dto.AnalyticsSummaryDto;
import com.mycompany.app.dto.TransactionDto;
import com.mycompany.app.model.AnalyticsSummary;
import com.mycompany.app.model.UserBehavior;
import com.mycompany.app.repository.AnalyticsSummaryRepository;
import com.mycompany.app.repository.UserBehaviorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsSummaryService {

    private final AnalyticsSummaryRepository analyticsSummaryRepository;
    private final UserBehaviorRepository userBehaviorRepository;
    private final AccountServiceClient accountServiceClient;
    private final TransactionServiceClient transactionServiceClient;

    @Transactional
    public AnalyticsSummaryDto generateSummary(String userId, String period) {
        Instant[] periodBounds = calculatePeriodBounds(period);
        Instant start = periodBounds[0];
        Instant end = periodBounds[1];

        log.info("Generating analytics summary for user: {}, period: {}", userId, period);

        // Get user behavior data
        List<UserBehavior> behaviors = userBehaviorRepository.findByUserIdAndTimestampBetween(userId, start, end);

        // Get user accounts and transactions
        ApiResponse<List<AccountDto>> accountsResponse = accountServiceClient.getUserAccounts(Long.valueOf(userId));
        List<TransactionDto> transactions = fetchUserTransactions(userId, accountsResponse.getData(), start, end);

        // Calculate and save summary
        AnalyticsSummary summary = calculateSummaryMetrics(userId, period, start, end, behaviors, transactions);
        AnalyticsSummary savedSummary = analyticsSummaryRepository.save(summary);

        return convertToDto(savedSummary);
    }

    private List<TransactionDto> fetchUserTransactions(String userId, List<AccountDto> accounts, Instant start, Instant end) {
        if (accounts == null || accounts.isEmpty()) {
            return Collections.emptyList();
        }

        List<TransactionDto> allTransactions = new ArrayList<>();

        for (AccountDto account : accounts) {
            ApiResponse<List<TransactionDto>> response = transactionServiceClient.getTransactionHistory(account.getAccountNumber());
            if (response.getData() != null) {
                List<TransactionDto> accountTransactions = response.getData().stream()
                        .filter(t -> t.getCreatedAt().isAfter(start) && t.getCreatedAt().isBefore(end))
                        .collect(Collectors.toList());
                allTransactions.addAll(accountTransactions);
            }
        }

        return allTransactions;
    }

    private AnalyticsSummary calculateSummaryMetrics(String userId, String period, Instant start, Instant end,
                                                     List<UserBehavior> behaviors, List<TransactionDto> transactions) {

        // Calculate behavioral metrics
        int loginCount = (int) behaviors.stream()
                .filter(b -> "LOGIN".equals(b.getAction()))
                .count();

        double totalSessionDuration = behaviors.stream()
                .filter(b -> b.getDuration() != null)
                .mapToDouble(UserBehavior::getDuration)
                .sum();

        String mostUsedFeature = findMostUsedFeature(behaviors);

        // Calculate financial metrics
        List<TransactionDto> outgoingTransactions = transactions.stream()
                .filter(t -> t.getAmount().compareTo(BigDecimal.ZERO) > 0)
                .collect(Collectors.toList());

        List<TransactionDto> incomingTransactions = transactions.stream()
                .filter(t -> t.getAmount().compareTo(BigDecimal.ZERO) > 0)
                .collect(Collectors.toList());

        BigDecimal totalSpending = outgoingTransactions.stream()
                .map(TransactionDto::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalIncome = incomingTransactions.stream()
                .map(TransactionDto::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal averageTransactionAmount = transactions.isEmpty() ? BigDecimal.ZERO :
                transactions.stream()
                        .map(TransactionDto::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(BigDecimal.valueOf(transactions.size()), 2, RoundingMode.HALF_UP);

        Map<String, String> additionalMetrics = calculateAdditionalMetrics(behaviors, transactions);

        return AnalyticsSummary.builder()
                .userId(userId)
                .period(period)
                .periodStart(start)
                .periodEnd(end)
                .totalTransactions(transactions.size())
                .totalSpending(totalSpending)
                .totalIncome(totalIncome)
                .averageTransactionAmount(averageTransactionAmount)
                .loginCount(loginCount)
                .totalSessionDuration(totalSessionDuration)
                .mostUsedFeature(mostUsedFeature)
                .additionalMetrics(additionalMetrics)
                .createdAt(Instant.now())
                .build();
    }

    private String findMostUsedFeature(List<UserBehavior> behaviors) {
        return behaviors.stream()
                .collect(Collectors.groupingBy(UserBehavior::getAction, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("UNKNOWN");
    }

    private Map<String, String> calculateAdditionalMetrics(List<UserBehavior> behaviors, List<TransactionDto> transactions) {
        Map<String, String> metrics = new HashMap<>();

        long uniqueSessions = behaviors.stream()
                .filter(b -> b.getSessionId() != null)
                .map(UserBehavior::getSessionId)
                .distinct()
                .count();
        metrics.put("unique_sessions", String.valueOf(uniqueSessions));

        if (uniqueSessions > 0) {
            double avgSessionDuration = behaviors.stream()
                    .filter(b -> b.getDuration() != null)
                    .mapToDouble(UserBehavior::getDuration)
                    .average()
                    .orElse(0.0);
            metrics.put("avg_session_duration", String.format("%.2f", avgSessionDuration));
        }

        String mostUsedDevice = behaviors.stream()
                .filter(b -> b.getDeviceType() != null)
                .collect(Collectors.groupingBy(UserBehavior::getDeviceType, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("UNKNOWN");
        metrics.put("most_used_device", mostUsedDevice);

        if (!transactions.isEmpty()) {
            Map<String, Long> categoryFrequency = transactions.stream()
                    .filter(t -> t.getCategory() != null)
                    .collect(Collectors.groupingBy(TransactionDto::getCategory, Collectors.counting()));

            String topCategory = categoryFrequency.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("UNKNOWN");
            metrics.put("top_spending_category", topCategory);
        }

        return metrics;
    }

    private Instant[] calculatePeriodBounds(String period) {
        Instant now = Instant.now();
        Instant start, end;

        switch (period.toUpperCase()) {
            case "DAILY":
                start = now.minus(1, ChronoUnit.DAYS);
                end = now;
                break;
            case "WEEKLY":
                start = now.minus(7, ChronoUnit.DAYS);
                end = now;
                break;
            case "MONTHLY":
                start = now.minus(30, ChronoUnit.DAYS);
                end = now;
                break;
            case "QUARTERLY":
                start = now.minus(90, ChronoUnit.DAYS);
                end = now;
                break;
            case "YEARLY":
                start = now.minus(365, ChronoUnit.DAYS);
                end = now;
                break;
            default:
                start = now.minus(30, ChronoUnit.DAYS);
                end = now;
        }

        return new Instant[]{start, end};
    }

    public List<AnalyticsSummaryDto> getUserSummaries(String userId, String period) {
        List<AnalyticsSummary> summaries;
        if (period != null) {
            summaries = analyticsSummaryRepository.findByUserIdAndPeriodOrderByPeriodStartDesc(userId, period);
        } else {
            summaries = analyticsSummaryRepository.findByUserId(userId);
        }

        return summaries.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public AnalyticsSummaryDto getLatestSummary(String userId) {
        List<AnalyticsSummary> summaries = analyticsSummaryRepository.findByUserId(userId);

        return summaries.stream()
                .max(Comparator.comparing(AnalyticsSummary::getPeriodStart))
                .map(this::convertToDto)
                .orElse(null);
    }

    public List<AnalyticsSummaryDto> getSummariesByDateRange(String userId, Instant start, Instant end) {
        List<AnalyticsSummary> summaries = analyticsSummaryRepository.findByPeriodStartBetweenOrderByPeriodStartDesc(start, end);

        return summaries.stream()
                .filter(summary -> summary.getUserId().equals(userId))
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public Map<String, Object> getUserMetricsSummary(String userId) {
        List<AnalyticsSummary> summaries = analyticsSummaryRepository.findByUserId(userId);
        Map<String, Object> metrics = new HashMap<>();

        if (summaries.isEmpty()) {
            return metrics;
        }

        BigDecimal totalSpending = summaries.stream()
                .map(AnalyticsSummary::getTotalSpending)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalIncome = summaries.stream()
                .map(AnalyticsSummary::getTotalIncome)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalLogins = summaries.stream()
                .mapToInt(AnalyticsSummary::getLoginCount)
                .sum();

        double totalSessionTime = summaries.stream()
                .mapToDouble(AnalyticsSummary::getTotalSessionDuration)
                .sum();

        metrics.put("total_spending", totalSpending);
        metrics.put("total_income", totalIncome);
        metrics.put("total_logins", totalLogins);
        metrics.put("total_session_time", totalSessionTime);
        metrics.put("summary_count", summaries.size());

        return metrics;
    }

    @Scheduled(cron = "0 0 2 * * ?") // Run daily at 2 AM
    @Transactional
    public void generateDailySummaries() {
        log.info("Starting daily summary generation");

        Instant yesterday = Instant.now().minus(1, ChronoUnit.DAYS);
        List<UserBehavior> recentBehaviors = userBehaviorRepository.findByUserIdAndTimestampBetween(null, yesterday, Instant.now());

        Set<String> activeUsers = recentBehaviors.stream()
                .map(UserBehavior::getUserId)
                .collect(Collectors.toSet());

        log.info("Found {} active users for daily summary generation", activeUsers.size());

        for (String userId : activeUsers) {
            generateSummary(userId, "DAILY");
            log.debug("Generated daily summary for user: {}", userId);
        }

        log.info("Completed daily summary generation");
    }

    @Scheduled(cron = "0 0 3 * * MON") // Run weekly on Monday at 3 AM
    @Transactional
    public void generateWeeklySummaries() {
        log.info("Starting weekly summary generation");

        Instant lastWeek = Instant.now().minus(7, ChronoUnit.DAYS);
        List<UserBehavior> recentBehaviors = userBehaviorRepository.findByUserIdAndTimestampBetween(null, lastWeek, Instant.now());

        Set<String> activeUsers = recentBehaviors.stream()
                .map(UserBehavior::getUserId)
                .collect(Collectors.toSet());

        for (String userId : activeUsers) {
            generateSummary(userId, "WEEKLY");
        }

        log.info("Completed weekly summary generation");
    }

    @Scheduled(cron = "0 0 4 1 * ?") // Run monthly on 1st day at 4 AM
    @Transactional
    public void generateMonthlySummaries() {
        log.info("Starting monthly summary generation");

        Instant lastMonth = Instant.now().minus(30, ChronoUnit.DAYS);
        List<UserBehavior> recentBehaviors = userBehaviorRepository.findByUserIdAndTimestampBetween(null, lastMonth, Instant.now());

        Set<String> activeUsers = recentBehaviors.stream()
                .map(UserBehavior::getUserId)
                .collect(Collectors.toSet());

        for (String userId : activeUsers) {
            generateSummary(userId, "MONTHLY");
        }

        log.info("Completed monthly summary generation");
    }

    private AnalyticsSummaryDto convertToDto(AnalyticsSummary summary) {
        return AnalyticsSummaryDto.builder()
                .id(summary.getId() != null ? Long.valueOf(summary.getId()) : null)
                .userId(summary.getUserId())
                .period(summary.getPeriod())
                .periodStart(summary.getPeriodStart())
                .periodEnd(summary.getPeriodEnd())
                .totalTransactions(summary.getTotalTransactions())
                .totalSpending(summary.getTotalSpending())
                .totalIncome(summary.getTotalIncome())
                .averageTransactionAmount(summary.getAverageTransactionAmount())
                .loginCount(summary.getLoginCount())
                .totalSessionDuration(summary.getTotalSessionDuration())
                .mostUsedFeature(summary.getMostUsedFeature())
                .additionalMetrics(summary.getAdditionalMetrics())
                .createdAt(summary.getCreatedAt())
                .build();
    }
}
