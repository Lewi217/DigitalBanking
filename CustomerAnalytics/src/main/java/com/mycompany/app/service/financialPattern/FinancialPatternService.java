package com.mycompany.app.service.financialPattern;

import com.mycompany.app.client.AccountServiceClient;
import com.mycompany.app.client.TransactionServiceClient;
import com.mycompany.app.dto.AccountDto;
import com.mycompany.app.dto.FinancialPatternDto;
import com.mycompany.app.dto.TransactionDto;
import com.mycompany.app.model.FinancialPattern;
import com.mycompany.app.model.PatternType;
import com.mycompany.app.repository.FinancialPatternRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FinancialPatternService {

    private final FinancialPatternRepository financialPatternRepository;
    private final AccountServiceClient accountServiceClient;
    private final TransactionServiceClient transactionServiceClient;

    @Transactional
    public List<FinancialPatternDto> analyzeUserPatterns(String userId) {
        try {
            // Get user accounts
            ApiResponse<List<AccountDto>> accountsResponse = accountServiceClient.getUserAccounts(Long.valueOf(userId));
            if (accountsResponse.getData() == null || accountsResponse.getData().isEmpty()) {
                log.warn("No accounts found for user: {}", userId);
                return Collections.emptyList();
            }

            List<FinancialPattern> patterns = new ArrayList<>();

            // Analyze patterns for each account
            for (AccountDto account : accountsResponse.getData()) {
                patterns.addAll(analyzeAccountPatterns(userId, account.getAccountNumber()));
            }

            // Save patterns
            List<FinancialPattern> savedPatterns = financialPatternRepository.saveAll(patterns);

            return savedPatterns.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error analyzing financial patterns for user: {}", userId, e);
            return Collections.emptyList();
        }
    }

    private List<FinancialPattern> analyzeAccountPatterns(String userId, String accountNumber) {
        List<FinancialPattern> patterns = new ArrayList<>();

        try {
            // Get last 90 days of transactions
            Instant end = Instant.now();
            Instant start = end.minus(90, ChronoUnit.DAYS);

            ApiResponse<List<TransactionDto>> transactionsResponse =
                    transactionServiceClient.getTransactionHistory(accountNumber);

            if (transactionsResponse.getData() == null) {
                return patterns;
            }

            List<TransactionDto> transactions = transactionsResponse.getData().stream()
                    .filter(t -> t.getCreatedAt().isAfter(start))
                    .collect(Collectors.toList());

            // Analyze spending patterns
            patterns.addAll(analyzeSpendingPatterns(userId, accountNumber, transactions, start, end));

            // Analyze income patterns
            patterns.addAll(analyzeIncomePatterns(userId, accountNumber, transactions, start, end));

            // Analyze category preferences
            patterns.addAll(analyzeCategoryPatterns(userId, accountNumber, transactions, start, end));

        } catch (Exception e) {
            log.error("Error analyzing patterns for account: {}", accountNumber, e);
        }

        return patterns;
    }

    private List<FinancialPattern> analyzeSpendingPatterns(String userId, String accountNumber,
                                                           List<TransactionDto> transactions,
                                                           Instant start, Instant end) {
        List<FinancialPattern> patterns = new ArrayList<>();

        // Filter outgoing transactions
        List<TransactionDto> outgoingTransactions = transactions.stream()
                .filter(t -> t.getFromAccount().equals(accountNumber))
                .filter(t -> t.getAmount().compareTo(BigDecimal.ZERO) > 0)
                .collect(Collectors.toList());

        if (outgoingTransactions.isEmpty()) {
            return patterns;
        }

        // Calculate spending statistics
        BigDecimal totalSpending = outgoingTransactions.stream()
                .map(TransactionDto::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal averageSpending = totalSpending
                .divide(BigDecimal.valueOf(outgoingTransactions.size()), 2, RoundingMode.HALF_UP);

        patterns.add(FinancialPattern.builder()
                .userId(userId)
                .accountId(accountNumber)
                .patternType(PatternType.SPENDING_HABIT)
                .averageAmount(averageSpending)
                .totalAmount(totalSpending)
                .frequency(outgoingTransactions.size())
                .description("Average spending pattern over 90 days")
                .analyzedAt(Instant.now())
                .periodStart(start)
                .periodEnd(end)
                .confidence(0.85)
                .build());

        return patterns;
    }

    private List<FinancialPattern> analyzeIncomePatterns(String userId, String accountNumber,
                                                         List<TransactionDto> transactions,
                                                         Instant start, Instant end) {
        List<FinancialPattern> patterns = new ArrayList<>();

        // Filter incoming transactions
        List<TransactionDto> incomingTransactions = transactions.stream()
                .filter(t -> t.getToAccount().equals(accountNumber))
                .filter(t -> t.getAmount().compareTo(BigDecimal.ZERO) > 0)
                .collect(Collectors.toList());

        if (incomingTransactions.isEmpty()) {
            return patterns;
        }

        // Calculate income statistics
        BigDecimal totalIncome = incomingTransactions.stream()
                .map(TransactionDto::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal averageIncome = totalIncome
                .divide(BigDecimal.valueOf(incomingTransactions.size()), 2, RoundingMode.HALF_UP);

        patterns.add(FinancialPattern.builder()
                .userId(userId)
                .accountId(accountNumber)
                .patternType(PatternType.INCOME_PATTERN)
                .averageAmount(averageIncome)
                .totalAmount(totalIncome)
                .frequency(incomingTransactions.size())
                .description("Average income pattern over 90 days")
                .analyzedAt(Instant.now())
                .periodStart(start)
                .periodEnd(end)
                .confidence(0.80)
                .build());

        return patterns;
    }

    private List<FinancialPattern> analyzeCategoryPatterns(String userId, String accountNumber,
                                                           List<TransactionDto> transactions,
                                                           Instant start, Instant end) {
        List<FinancialPattern> patterns = new ArrayList<>();

        // Group transactions by category
        Map<String, List<TransactionDto>> categoryGroups = transactions.stream()
                .filter(t -> t.getCategory() != null && !t.getCategory().isEmpty())
                .collect(Collectors.groupingBy(TransactionDto::getCategory));

        for (Map.Entry<String, List<TransactionDto>> entry : categoryGroups.entrySet()) {
            String category = entry.getKey();
            List<TransactionDto> categoryTransactions = entry.getValue();

            BigDecimal totalAmount = categoryTransactions.stream()
                    .map(TransactionDto::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal averageAmount = totalAmount
                    .divide(BigDecimal.valueOf(categoryTransactions.size()), 2, RoundingMode.HALF_UP);

            patterns.add(FinancialPattern.builder()
                    .userId(userId)
                    .accountId(accountNumber)
                    .patternType(PatternType.CATEGORY_PREFERENCE)
                    .category(category)
                    .averageAmount(averageAmount)
                    .totalAmount(totalAmount)
                    .frequency(categoryTransactions.size())
                    .description("Spending pattern for category: " + category)
                    .analyzedAt(Instant.now())
                    .periodStart(start)
                    .periodEnd(end)
                    .confidence(0.75)
                    .build());
        }

        return patterns;
    }

    public List<FinancialPatternDto> getUserPatterns(String userId, PatternType patternType) {
        List<FinancialPattern> patterns;
        if (patternType != null) {
            patterns = financialPatternRepository.findByUserIdAndPatternType(userId, patternType);
        } else {
            patterns = financialPatternRepository.findByUserId(userId);
        }

        return patterns.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<FinancialPatternDto> getHighConfidencePatterns(String userId, Double minConfidence) {
        List<FinancialPattern> patterns = financialPatternRepository
                .findHighConfidencePatterns(userId, minConfidence != null ? minConfidence : 0.7);

        return patterns.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private FinancialPatternDto convertToDto(FinancialPattern pattern) {
        return FinancialPatternDto.builder()
                .id(pattern.getId())
                .userId(pattern.getUserId())
                .accountId(pattern.getAccountId())
                .patternType(pattern.getPatternType())
                .averageAmount(pattern.getAverageAmount())
                .totalAmount(pattern.getTotalAmount())
                .frequency(pattern.getFrequency())
                .category(pattern.getCategory())
                .description(pattern.getDescription())
                .analyzedAt(pattern.getAnalyzedAt())
                .periodStart(pattern.getPeriodStart())
                .periodEnd(pattern.getPeriodEnd())
                .confidence(pattern.getConfidence())
                .build();
    }
}

