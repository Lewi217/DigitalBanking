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
import com.mycompany.app.response.ApiResponse;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsSummaryService implements IAnalyticsSummaryService{

    private final AnalyticsSummaryRepository analyticsSummaryRepository;
    private final UserBehaviorRepository userBehaviorRepository;
    private final AccountServiceClient accountServiceClient;
    private final TransactionServiceClient transactionServiceClient;

    @Transactional
    @Override
    public AnalyticsSummaryDto generateSummary(String userId, String period) {
        Instant[] periodBounds = calculatePeriodBounds(period);
        Instant start = periodBounds[0];
        Instant end = periodBounds[1];
        List<UserBehavior> behaviors = userBehaviorRepository.findByUserIdAndTimestampBetween(userId, start, end);
        ApiResponse accountsResponse = accountServiceClient.getUserAccounts(Long.valueOf(userId));
        List<AccountDto> accountList = (List<AccountDto>) accountsResponse.getData();
        List<TransactionDto> transactions = fetchUserTransactions(userId, accountList, start, end);
        AnalyticsSummary summary = calculateSummaryMetrics(userId, period, start, end, behaviors, transactions);
        AnalyticsSummary savedSummary = analyticsSummaryRepository.save(summary);
        return convertToDto(savedSummary);
    }

    @Override
    public List<TransactionDto> fetchUserTransactions(String userId, List<AccountDto> accounts, Instant start, Instant end) {
        if (accounts == null || accounts.isEmpty()) {
            return Collections.emptyList();
        }

        List<TransactionDto> allTransactions = new ArrayList<>();

        for (AccountDto account : accounts) {
            ApiResponse response = transactionServiceClient.getTransactionHistory(account.getAccountNumber());
            Object data = response.getData();
            if (data instanceof List<?>) {
                for (Object obj : (List<?>) data) {
                    if (obj instanceof TransactionDto) {
                        TransactionDto tx = (TransactionDto) obj;
                        if (tx.getTimestamp() != null &&
                                tx.getTimestamp().isAfter(start) &&
                                tx.getTimestamp().isBefore(end)) {
                            allTransactions.add(tx);
                        }
                    }
                }
            }
        }

        return allTransactions;
    }

    @Override
    public AnalyticsSummary calculateSummaryMetrics(String userId, String period, Instant start, Instant end, List<UserBehavior> behaviors, List<TransactionDto> transactions) {

        int loginCount = (int) behaviors.stream()
                .filter(b -> "LOGIN".equals(b.getAction()))
                .count();

        double totalSessionDuration = behaviors.stream()
                .filter(b -> b.getDuration() != null)
                .mapToDouble(UserBehavior::getDuration)
                .sum();

        String mostUsedFeature = findMostUsedFeature(behaviors);

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

        Map<String, String> additionalMetrics = calculateAdditionalMetrics(behaviors);

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

    @Override
    public String findMostUsedFeature(List<UserBehavior> behaviors) {
        return behaviors.stream()
                .collect(Collectors.groupingBy(UserBehavior::getAction, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("UNKNOWN");
    }

    @Override
    public Map<String, String> calculateAdditionalMetrics(List<UserBehavior> behaviors) {
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

        return metrics;
    }

    @Override
    public Instant[] calculatePeriodBounds(String period) {
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

    @Override
    public List<AnalyticsSummaryDto> getUserSummaries(String userId, String period) {
        List<AnalyticsSummary> summaries = (period != null)
                ? analyticsSummaryRepository.findByUserIdAndPeriodOrderByPeriodStartDesc(userId, period)
                : analyticsSummaryRepository.findByUserId(userId);

        return summaries.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public AnalyticsSummaryDto getLatestSummary(String userId) {
        return analyticsSummaryRepository.findByUserId(userId).stream()
                .max(Comparator.comparing(AnalyticsSummary::getPeriodStart))
                .map(this::convertToDto)
                .orElse(null);
    }

    @Override
    public List<AnalyticsSummaryDto> getSummariesByDateRange(String userId, Instant start, Instant end) {
        return analyticsSummaryRepository.findByPeriodStartBetweenOrderByPeriodStartDesc(start, end).stream()
                .filter(summary -> summary.getUserId().equals(userId))
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getUserMetricsSummary(String userId) {
        List<AnalyticsSummary> summaries = analyticsSummaryRepository.findByUserId(userId);
        Map<String, Object> metrics = new HashMap<>();

        if (summaries.isEmpty()) return metrics;

        metrics.put("total_spending", summaries.stream()
                .map(AnalyticsSummary::getTotalSpending)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        metrics.put("total_income", summaries.stream()
                .map(AnalyticsSummary::getTotalIncome)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        metrics.put("total_logins", summaries.stream()
                .mapToInt(AnalyticsSummary::getLoginCount)
                .sum());
        metrics.put("total_session_time", summaries.stream()
                .mapToDouble(AnalyticsSummary::getTotalSessionDuration)
                .sum());
        metrics.put("summary_count", summaries.size());

        return metrics;
    }

    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    @Override
    public void generateDailySummaries() {
        Instant yesterday = Instant.now().minus(1, ChronoUnit.DAYS);
        List<UserBehavior> recent = userBehaviorRepository.findByUserIdAndTimestampBetween(null, yesterday, Instant.now());

        Set<String> users = recent.stream().map(UserBehavior::getUserId).collect(Collectors.toSet());
        for (String userId : users) {
            generateSummary(userId, "DAILY");
        }
    }

    @Scheduled(cron = "0 0 3 * * MON")
    @Transactional
    @Override
    public void generateWeeklySummaries() {
        Instant lastWeek = Instant.now().minus(7, ChronoUnit.DAYS);
        List<UserBehavior> recent = userBehaviorRepository.findByUserIdAndTimestampBetween(null, lastWeek, Instant.now());

        Set<String> users = recent.stream().map(UserBehavior::getUserId).collect(Collectors.toSet());
        for (String userId : users) {
            generateSummary(userId, "WEEKLY");
        }
    }

    @Scheduled(cron = "0 0 4 1 * ?")
    @Transactional
    @Override
    public void generateMonthlySummaries() {
        Instant lastMonth = Instant.now().minus(30, ChronoUnit.DAYS);
        List<UserBehavior> recent = userBehaviorRepository.findByUserIdAndTimestampBetween(null, lastMonth, Instant.now());

        Set<String> users = recent.stream().map(UserBehavior::getUserId).collect(Collectors.toSet());
        for (String userId : users) {
            generateSummary(userId, "MONTHLY");
        }
    }

    @Override
    public AnalyticsSummaryDto convertToDto(AnalyticsSummary summary) {
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