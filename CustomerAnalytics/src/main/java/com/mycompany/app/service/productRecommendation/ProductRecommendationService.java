package com.mycompany.app.service.productRecommendation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.app.client.AccountServiceClient;
import com.mycompany.app.client.AuthServiceClient;
import com.mycompany.app.dto.AccountDto;
import com.mycompany.app.dto.ProductRecommendationDto;
import com.mycompany.app.dto.UserDto;
import com.mycompany.app.model.FinancialPattern;
import com.mycompany.app.model.PatternType;
import com.mycompany.app.model.ProductRecommendation;
import com.mycompany.app.model.RecommendationStatus;
import com.mycompany.app.repository.FinancialPatternRepository;
import com.mycompany.app.repository.ProductRecommendationRepository;
import com.mycompany.app.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductRecommendationService implements IProductRecommendationService {

    private final ProductRecommendationRepository recommendationRepository;
    private final FinancialPatternRepository financialPatternRepository;
    private final AccountServiceClient accountServiceClient;
    private final AuthServiceClient authServiceClient;
    private final ModelMapper modelMapper;
    private final ObjectMapper objectMapper;

    @Transactional
    @Override
    public List<ProductRecommendationDto> generateAndSaveRecommendations(String userId) {
        try {
            List<FinancialPattern> patterns = financialPatternRepository.findByUserId(userId);
            if (patterns.isEmpty()) {
                patterns = generateDefaultPatterns(userId);
            }

            List<AccountDto> accounts = getAccountsForUser(userId);
            List<ProductRecommendation> recommendations = generateAllRecommendations(userId, patterns, accounts);
            List<ProductRecommendation> saved = recommendationRepository.saveAll(recommendations);

            return saved.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate recommendations: " + e.getMessage());
        }
    }

    private List<FinancialPattern> generateDefaultPatterns(String userId) {
        List<FinancialPattern> defaultPatterns = new ArrayList<>();
        Instant now = Instant.now();
        Instant start = now.minus(90, ChronoUnit.DAYS);

        // Create default spending pattern
        FinancialPattern spendingPattern = FinancialPattern.builder()
                .userId(userId)
                .accountId("default_account")
                .patternType(PatternType.SPENDING_HABIT)
                .averageAmount(BigDecimal.valueOf(1200.00))
                .totalAmount(BigDecimal.valueOf(3600.00))
                .frequency(3)
                .description("Estimated spending pattern - No transaction history available")
                .analyzedAt(now)
                .periodStart(start)
                .periodEnd(now)
                .confidence(0.75)
                .build();

        // Create default income pattern
        FinancialPattern incomePattern = FinancialPattern.builder()
                .userId(userId)
                .accountId("default_account")
                .patternType(PatternType.INCOME_PATTERN)
                .averageAmount(BigDecimal.valueOf(2500.00))
                .totalAmount(BigDecimal.valueOf(5000.00))
                .frequency(2)
                .description("Estimated income pattern - No transaction history available")
                .analyzedAt(now)
                .periodStart(start)
                .periodEnd(now)
                .confidence(0.80)
                .build();

        defaultPatterns.add(spendingPattern);
        defaultPatterns.add(incomePattern);

        return financialPatternRepository.saveAll(defaultPatterns);
    }

    @Override
    public List<ProductRecommendation> generateAllRecommendations(String userId,
                                                                  List<FinancialPattern> patterns,
                                                                  List<AccountDto> accounts) {
        List<ProductRecommendation> recommendations = new ArrayList<>();

        List<ProductRecommendation> savingsRecs = generateSavingsRecommendations(userId, patterns, accounts);
        recommendations.addAll(savingsRecs);

        List<ProductRecommendation> creditRecs = generateCreditRecommendations(userId, patterns, accounts);
        recommendations.addAll(creditRecs);

        List<ProductRecommendation> loanRecs = generateLoanRecommendations(userId, patterns, accounts);
        recommendations.addAll(loanRecs);

        return recommendations;
    }

    @Override
    public List<ProductRecommendation> generateSavingsRecommendations(String userId,
                                                                      List<FinancialPattern> patterns,
                                                                      List<AccountDto> accounts) {
        List<ProductRecommendation> recommendations = new ArrayList<>();

        Optional<FinancialPattern> spendingPattern = patterns.stream()
                .filter(p -> p.getPatternType() == PatternType.SPENDING_HABIT)
                .findFirst();

        Optional<FinancialPattern> incomePattern = patterns.stream()
                .filter(p -> p.getPatternType() == PatternType.INCOME_PATTERN)
                .findFirst();

        if (spendingPattern.isPresent() && incomePattern.isPresent()) {
            BigDecimal monthlyIncome = incomePattern.get().getAverageAmount();
            BigDecimal monthlySpending = spendingPattern.get().getAverageAmount();
            BigDecimal surplus = monthlyIncome.subtract(monthlySpending);

            if (surplus.compareTo(BigDecimal.valueOf(500)) > 0) {
                recommendations.add(createSavingsRecommendation(userId, surplus));
            }
        }

        return recommendations;
    }

    @Override
    public List<ProductRecommendation> generateCreditRecommendations(String userId,
                                                                     List<FinancialPattern> patterns,
                                                                     List<AccountDto> accounts) {
        List<ProductRecommendation> recommendations = new ArrayList<>();

        boolean hasCreditAccount = accounts.stream()
                .anyMatch(a -> "CREDIT".equals(a.getAccountType()));

        if (!hasCreditAccount) {
            Optional<FinancialPattern> spendingPattern = patterns.stream()
                    .filter(p -> p.getPatternType() == PatternType.SPENDING_HABIT)
                    .findFirst();

            if (spendingPattern.isPresent()) {
                BigDecimal monthlySpending = spendingPattern.get().getAverageAmount();
                recommendations.add(createCreditRecommendation(userId, monthlySpending));
            }
        }

        return recommendations;
    }

    @Override
    public List<ProductRecommendation> generateLoanRecommendations(String userId,
                                                                   List<FinancialPattern> patterns,
                                                                   List<AccountDto> accounts) {
        List<ProductRecommendation> recommendations = new ArrayList<>();

        Optional<FinancialPattern> incomePattern = patterns.stream()
                .filter(p -> p.getPatternType() == PatternType.INCOME_PATTERN)
                .findFirst();

        if (incomePattern.isPresent()) {
            BigDecimal monthlyIncome = incomePattern.get().getAverageAmount();

            if (monthlyIncome.compareTo(BigDecimal.valueOf(3000)) > 0) {
                recommendations.add(createLoanRecommendation(userId, monthlyIncome));
            }
        }

        return recommendations;
    }

    @Override
    public ProductRecommendation createSavingsRecommendation(String userId, BigDecimal surplus) {
        return ProductRecommendation.builder()
                .userId(userId)
                .productType("SAVINGS_ACCOUNT")
                .productName("High-Yield Savings Account")
                .description("Earn more interest on your surplus funds with our high-yield savings account")
                .score(0.85)
                .reasoning("You have a monthly surplus of $" + surplus + " that could earn higher interest")
                .createdAt(Instant.now())
                .validUntil(Instant.now().plus(30, ChronoUnit.DAYS))
                .status(RecommendationStatus.PENDING)
                .estimatedValue(surplus.multiply(BigDecimal.valueOf(0.02)))
                .targetAudience("Users with regular surplus funds")
                .build();
    }

    @Override
    public ProductRecommendation createCreditRecommendation(String userId, BigDecimal monthlySpending) {
        return ProductRecommendation.builder()
                .userId(userId)
                .productType("CREDIT_CARD")
                .productName("Cashback Credit Card")
                .description("Earn cashback on your daily purchases with our rewards credit card")
                .score(0.75)
                .reasoning("Based on your spending pattern of $" + monthlySpending + "/month, you could benefit from a credit card")
                .createdAt(Instant.now())
                .validUntil(Instant.now().plus(30, ChronoUnit.DAYS))
                .status(RecommendationStatus.PENDING)
                .estimatedValue(monthlySpending.multiply(BigDecimal.valueOf(0.02)))
                .targetAudience("Users without credit cards")
                .build();
    }

    @Override
    public ProductRecommendation createLoanRecommendation(String userId, BigDecimal monthlyIncome) {
        return ProductRecommendation.builder()
                .userId(userId)
                .productType("PERSONAL_LOAN")
                .productName("Personal Loan")
                .description("Get competitive rates on personal loans for your major purchases")
                .score(0.60)
                .reasoning("Your stable income of $" + monthlyIncome + "/month qualifies you for competitive loan rates")
                .createdAt(Instant.now())
                .validUntil(Instant.now().plus(30, ChronoUnit.DAYS))
                .status(RecommendationStatus.PENDING)
                .estimatedValue(BigDecimal.valueOf(25000))
                .targetAudience("Users with stable income")
                .build();
    }

    @Override
    public List<ProductRecommendationDto> getUserRecommendations(String userId, RecommendationStatus status) {
        List<ProductRecommendation> recommendations;
        if (status != null) {
            recommendations = recommendationRepository.findByUserIdAndStatus(userId, status);
        } else {
            recommendations = recommendationRepository.findByUserId(userId);
        }

        return recommendations.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductRecommendationDto> getTopRecommendations(String userId) {
        List<ProductRecommendation> recommendations = recommendationRepository
                .findByUserIdAndStatusOrderByScoreDesc(userId, RecommendationStatus.PENDING);

        return recommendations.stream()
                .limit(5)
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductRecommendationDto> getPendingRecommendations(String userId) {
        List<ProductRecommendation> recommendations = recommendationRepository
                .findByUserIdAndStatus(userId, RecommendationStatus.PENDING);

        return recommendations.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public long getPendingRecommendationsCount(String userId) {
        List<ProductRecommendation> recommendations = recommendationRepository
                .findByUserIdAndStatus(userId, RecommendationStatus.PENDING);
        return recommendations.size();
    }

    @Transactional
    @Override
    public void updateRecommendationStatus(String recommendationId, RecommendationStatus status) {
        Optional<ProductRecommendation> recommendationOpt = recommendationRepository.findById(recommendationId);
        if (recommendationOpt.isPresent()) {
            ProductRecommendation recommendation = recommendationOpt.get();
            recommendation.setStatus(status);
            recommendationRepository.save(recommendation);
        }
    }

    @Override
    public List<AccountDto> getAccountsForUser(String userId) {
        try {
            ApiResponse response = accountServiceClient.getUserAccounts(Long.valueOf(userId));
            List<AccountDto> accounts = convertToAccountDtoList(response != null ? response.getData() : null);
            return accounts;
        } catch (Exception e) {
            // Return empty list and let the service continue with default patterns
            return Collections.emptyList();
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

    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional
    public void expireOldRecommendations() {
        try {
            List<ProductRecommendation> expiredRecommendations =
                    recommendationRepository.findByValidUntilBeforeAndStatus(Instant.now(), RecommendationStatus.PENDING);

            expiredRecommendations.forEach(rec -> rec.setStatus(RecommendationStatus.EXPIRED));
            recommendationRepository.saveAll(expiredRecommendations);
        } catch (Exception ignored) {
        }
    }

    private ProductRecommendationDto convertToDto(ProductRecommendation recommendation) {
        return modelMapper.map(recommendation, ProductRecommendationDto.class);
    }
}