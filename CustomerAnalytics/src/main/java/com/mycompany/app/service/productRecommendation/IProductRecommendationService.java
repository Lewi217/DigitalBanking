package com.mycompany.app.service.productRecommendation;

import com.mycompany.app.dto.AccountDto;
import com.mycompany.app.dto.ProductRecommendationDto;
import com.mycompany.app.model.FinancialPattern;
import com.mycompany.app.model.ProductRecommendation;
import com.mycompany.app.model.RecommendationStatus;

import java.math.BigDecimal;
import java.util.List;

public interface IProductRecommendationService {
    List<ProductRecommendationDto> generateAndSaveRecommendations(String userId);
    List<ProductRecommendation> generateAllRecommendations(String userId, List<FinancialPattern> patterns, List<AccountDto> accounts);
    List<ProductRecommendation> generateSavingsRecommendations(String userId, List<FinancialPattern> patterns, List<AccountDto> accounts);
    List<ProductRecommendation> generateCreditRecommendations(String userId, List<FinancialPattern> patterns, List<AccountDto> accounts);
    List<ProductRecommendation> generateLoanRecommendations(String userId, List<FinancialPattern> patterns, List<AccountDto> accounts);
    ProductRecommendation createSavingsRecommendation(String userId, BigDecimal surplus);
    ProductRecommendation createCreditRecommendation(String userId, BigDecimal monthlySpending);
    ProductRecommendation createLoanRecommendation(String userId, BigDecimal monthlyIncome);
    List<ProductRecommendationDto> getUserRecommendations(String userId, RecommendationStatus status);
    List<ProductRecommendationDto> getTopRecommendations(String userId);
    List<ProductRecommendationDto> getPendingRecommendations(String userId);
    long getPendingRecommendationsCount(String userId);
    List<AccountDto> getAccountsForUser(String userId);
    void updateRecommendationStatus(String recommendationId, RecommendationStatus status);
}
