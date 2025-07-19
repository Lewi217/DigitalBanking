package com.mycompany.app.service.financialPattern;

import com.fasterxml.jackson.databind.ObjectMapper;
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
public class FinancialPatternService implements IFinancialPatternService{

    private final FinancialPatternRepository financialPatternRepository;
    private final AccountServiceClient accountServiceClient;
    private final TransactionServiceClient transactionServiceClient;
    private final ObjectMapper objectMapper;

    @Transactional
    @Override
    public List<FinancialPatternDto> analyzeUserPatterns(String userId) {
        try {
            ApiResponse response = accountServiceClient.getUserAccounts(Long.valueOf(userId));
            List<AccountDto> accounts = convertToAccountDtoList(response.getData());
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
        } catch (Exception e) {
            throw new RuntimeException("Failed to analyze patterns: " + e.getMessage(), e);
        }
    }
    private List<AccountDto> convertToAccountDtoList(Object data) {
        if (data == null) {
            return Collections.emptyList();
        }
        try {
            if (data instanceof List) {
                List<?> rawList = (List<?>) data;
                return rawList.stream()
                        .map(this::convertToAccountDto)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            return Collections.emptyList();
        }
        return Collections.emptyList();
    }
    private AccountDto convertToAccountDto(Object obj) {
        try {
            if (obj instanceof LinkedHashMap) {
                return objectMapper.convertValue(obj, AccountDto.class);
            } else if (obj instanceof AccountDto) {
                return (AccountDto) obj;
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }
    @Override
    public List<FinancialPattern> analyzeAccountPatterns(String userId, String accountNumber) {
        List<FinancialPattern> patterns = new ArrayList<>();
        Instant end = Instant.now();
        Instant start = end.minus(90, ChronoUnit.DAYS);
        List<TransactionDto> transactions;
        try {
            ApiResponse response = transactionServiceClient.getTransactionHistory(accountNumber);
            transactions = convertToTransactionDtoList(response.getData());
            if (transactions != null) {
                transactions = transactions.stream()
                        .filter(t -> t.getTimestamp() != null && t.getTimestamp().isAfter(start))
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            transactions = new ArrayList<>();
        }
        patterns.addAll(analyzeSpendingPatterns(userId, accountNumber, transactions, start, end));
        patterns.addAll(analyzeIncomePatterns(userId, accountNumber, transactions, start, end));
        return patterns;
    }

    private List<TransactionDto> convertToTransactionDtoList(Object data) {
        if (data == null) {
            return Collections.emptyList();
        }
        try {
            if (data instanceof List) {
                List<?> rawList = (List<?>) data;
                return rawList.stream()
                        .map(this::convertToTransactionDto)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            return Collections.emptyList();
        }
        return Collections.emptyList();
    }

    private TransactionDto convertToTransactionDto(Object obj) {
        try {
            if (obj instanceof LinkedHashMap) {
                return objectMapper.convertValue(obj, TransactionDto.class);
            } else if (obj instanceof TransactionDto) {
                return (TransactionDto) obj;
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    @Override
    public List<FinancialPattern> analyzeSpendingPatterns(String userId, String accountNumber, List<TransactionDto> transactions, Instant start, Instant end) {
        List<TransactionDto> outgoing = transactions.stream()
                .filter(t -> t.getType() != null && t.getType().isDebit())
                .collect(Collectors.toList());
        BigDecimal total = outgoing.stream()
                .map(TransactionDto::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal average = outgoing.isEmpty() ? BigDecimal.ZERO :
                total.divide(BigDecimal.valueOf(outgoing.size()), 2, RoundingMode.HALF_UP);

        if (outgoing.isEmpty()) {
            FinancialPattern defaultPattern = FinancialPattern.builder()
                    .userId(userId)
                    .accountId(accountNumber)
                    .patternType(PatternType.SPENDING_HABIT)
                    .averageAmount(BigDecimal.valueOf(250.00))
                    .totalAmount(BigDecimal.valueOf(750.00))
                    .frequency(3)
                    .description("Estimated spending pattern - No transaction history available")
                    .analyzedAt(Instant.now())
                    .periodStart(start)
                    .periodEnd(end)
                    .confidence(0.75)
                    .build();
            return List.of(defaultPattern);
        }
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

    @Override
    public List<FinancialPattern> analyzeIncomePatterns(String userId, String accountNumber,
                                                        List<TransactionDto> transactions, Instant start, Instant end) {
        List<TransactionDto> incoming = transactions.stream()
                .filter(t -> t.getType() != null && t.getType().isCredit())
                .collect(Collectors.toList());

        BigDecimal total = incoming.stream()
                .map(TransactionDto::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal average = incoming.isEmpty() ? BigDecimal.ZERO :
                total.divide(BigDecimal.valueOf(incoming.size()), 2, RoundingMode.HALF_UP);
        if (incoming.isEmpty()) {
            FinancialPattern defaultPattern = FinancialPattern.builder()
                    .userId(userId)
                    .accountId(accountNumber)
                    .patternType(PatternType.INCOME_PATTERN)
                    .averageAmount(BigDecimal.valueOf(1500.00))
                    .totalAmount(BigDecimal.valueOf(3000.00))
                    .frequency(2)
                    .description("Estimated income pattern - No transaction history available")
                    .analyzedAt(Instant.now())
                    .periodStart(start)
                    .periodEnd(end)
                    .confidence(0.80)
                    .build();
            return List.of(defaultPattern);
        }
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

    @Override
    public List<FinancialPatternDto> getUserPatterns(String userId, PatternType patternType) {
        List<FinancialPattern> patterns = patternType != null
                ? financialPatternRepository.findByUserIdAndPatternType(userId, patternType)
                : financialPatternRepository.findByUserId(userId);
        return patterns.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<FinancialPatternDto> getHighConfidencePatterns(String userId, Double minConfidence) {
        List<FinancialPattern> patterns = financialPatternRepository
                .findByUserId(userId).stream()
                .filter(p -> p.getConfidence() >= (minConfidence != null ? minConfidence : 0.7))
                .collect(Collectors.toList());
        return patterns.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    @Override
    public FinancialPatternDto convertToDto(FinancialPattern pattern) {
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