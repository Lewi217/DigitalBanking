package com.mycompany.app.service.financialPattern;

import com.mycompany.app.client.AccountServiceClient;
import com.mycompany.app.client.TransactionServiceClient;
import com.mycompany.app.dto.AccountDto;
import com.mycompany.app.dto.FinancialPatternDto;
import com.mycompany.app.dto.TransactionDto;
import com.mycompany.app.model.FinancialPattern;
import com.mycompany.app.model.PatternType;
import com.mycompany.app.repository.FinancialPatternRepository;
import com.mycompany.app.response.ApiResponse;
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
public class FinancialPatternService {

    private final FinancialPatternRepository financialPatternRepository;
    private final AccountServiceClient accountServiceClient;
    private final TransactionServiceClient transactionServiceClient;

    @Transactional
    public List<FinancialPatternDto> analyzeUserPatterns(String userId) {
        ApiResponse response = accountServiceClient.getUserAccounts(Long.valueOf(userId));
        List<AccountDto> accounts = (List<AccountDto>) response.getData();

        if (accounts == null || accounts.isEmpty()) {
            return Collections.emptyList();
        }
        List<FinancialPattern> patterns = new ArrayList<>();

        for (AccountDto account : accounts) {
            patterns.addAll(analyzeAccountPatterns(userId, account.getAccountNumber()));
        }
        List<FinancialPattern> savedPatterns = financialPatternRepository.saveAll(patterns);

        return savedPatterns.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private List<FinancialPattern> analyzeAccountPatterns(String userId, String accountNumber) {
        List<FinancialPattern> patterns = new ArrayList<>();
        Instant end = Instant.now();
        Instant start = end.minus(90, ChronoUnit.DAYS);
        ApiResponse response = transactionServiceClient.getTransactionHistory(accountNumber);
        List<TransactionDto> transactions = (List<TransactionDto>) response.getData();
        if (transactions == null || transactions.isEmpty()) {
            return patterns;
        }

        transactions = transactions.stream()
                .filter(t -> t.getTimestamp() != null && t.getTimestamp().isAfter(start))
                .collect(Collectors.toList());
        patterns.addAll(analyzeSpendingPatterns(userId, accountNumber, transactions, start, end));
        patterns.addAll(analyzeIncomePatterns(userId, accountNumber, transactions, start, end));
        return patterns;
    }

    private List<FinancialPattern> analyzeSpendingPatterns(String userId, String accountNumber,List<TransactionDto> transactions, Instant start, Instant end) {
        List<TransactionDto> outgoing = transactions.stream()
                .filter(t -> t.getType() != null && t.getType().isDebit())
                .collect(Collectors.toList());

        if (outgoing.isEmpty()) return Collections.emptyList();

        BigDecimal total = outgoing.stream()
                .map(TransactionDto::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal average = total.divide(BigDecimal.valueOf(outgoing.size()), 2, RoundingMode.HALF_UP);

        FinancialPattern pattern = FinancialPattern.builder()
                .userId(userId)
                .accountId(accountNumber)
                .patternType(PatternType.SPENDING_HABIT)
                .averageAmount(average)
                .totalAmount(total)
                .frequency(outgoing.size())
                .description("Average spending pattern over 90 days")
                .analyzedAt(Instant.now())
                .periodStart(start)
                .periodEnd(end)
                .confidence(0.85)
                .build();

        return List.of(pattern);
    }

    private List<FinancialPattern> analyzeIncomePatterns(String userId, String accountNumber,List<TransactionDto> transactions, Instant start, Instant end) {
        List<TransactionDto> incoming = transactions.stream()
                .filter(t -> t.getType() != null && t.getType().isCredit())
                .collect(Collectors.toList());

        if (incoming.isEmpty()) return Collections.emptyList();

        BigDecimal total = incoming.stream()
                .map(TransactionDto::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal average = total.divide(BigDecimal.valueOf(incoming.size()), 2, RoundingMode.HALF_UP);

        FinancialPattern pattern = FinancialPattern.builder()
                .userId(userId)
                .accountId(accountNumber)
                .patternType(PatternType.INCOME_PATTERN)
                .averageAmount(average)
                .totalAmount(total)
                .frequency(incoming.size())
                .description("Average income pattern over 90 days")
                .analyzedAt(Instant.now())
                .periodStart(start)
                .periodEnd(end)
                .confidence(0.80)
                .build();

        return List.of(pattern);
    }

    public List<FinancialPatternDto> getUserPatterns(String userId, PatternType patternType) {
        List<FinancialPattern> patterns = patternType != null
                ? financialPatternRepository.findByUserIdAndPatternType(userId, patternType)
                : financialPatternRepository.findByUserId(userId);

        return patterns.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<FinancialPatternDto> getHighConfidencePatterns(String userId, Double minConfidence) {
        List<FinancialPattern> patterns = financialPatternRepository
                .findByUserId(userId).stream()
                .filter(p -> p.getConfidence() >= (minConfidence != null ? minConfidence : 0.7))
                .collect(Collectors.toList());

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


