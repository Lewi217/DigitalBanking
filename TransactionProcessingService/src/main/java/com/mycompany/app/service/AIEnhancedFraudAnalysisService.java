package com.mycompany.app.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.app.dto.FraudAnalysisResult;
import com.mycompany.app.model.FraudAnalysis;
import com.mycompany.app.model.FraudStatus;
import com.mycompany.app.model.RiskLevel;
import com.mycompany.app.model.Transaction;
import com.mycompany.app.repository.FraudAnalysisRepository;
import com.mycompany.app.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIEnhancedFraudAnalysisService {

    private final FraudAnalysisRepository fraudAnalysisRepository;
    private final TransactionRepository transactionRepository;
    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    public FraudAnalysis analyzeTransactionWithAI(Transaction transaction) {
        FraudAnalysisResult ruleBasedResult = performRuleBasedAnalysis(transaction);
        FraudAnalysisResult aiResult = performAIAnalysis(transaction, ruleBasedResult);
        FraudAnalysisResult finalResult = combineAnalysisResults(ruleBasedResult, aiResult);
        return saveFraudAnalysis(transaction, finalResult);
    }

    private FraudAnalysisResult performRuleBasedAnalysis(Transaction transaction) {
        List<String> riskFactors = new ArrayList<>();
        int totalRiskScore = 0;
        totalRiskScore += checkHighAmount(transaction, riskFactors);
        totalRiskScore += checkVelocity(transaction, riskFactors);
        totalRiskScore += checkUnusualTime(transaction, riskFactors);
        totalRiskScore += checkWeekendActivity(transaction, riskFactors);
        totalRiskScore += checkAccountPattern(transaction, riskFactors);

        return new FraudAnalysisResult(totalRiskScore, riskFactors, "Rule-based analysis");
    }

    private FraudAnalysisResult performAIAnalysis(Transaction transaction, FraudAnalysisResult ruleBasedResult) {
        try {
            String transactionContext = buildTransactionContext(transaction);
            String prompt = String.format("""
                Analyze this banking transaction for fraud patterns:
                
                Transaction Details:
                %s
                
                Rule-based Analysis Results:
                - Risk Score: %d
                - Risk Factors: %s
                
                Additional Context:
                %s
                
                Please provide:
                1. AI Risk Score (0-100)
                2. Risk Level (LOW/MEDIUM/HIGH/CRITICAL) 
                3. AI-detected patterns or anomalies
                4. Confidence level (0-100)
                5. Specific reasoning
                
                Respond in JSON format only.
                """,
                    formatTransactionForAI(transaction),
                    ruleBasedResult.getRiskScore(),
                    String.join(", ", ruleBasedResult.getRiskFactors()),
                    transactionContext
            );

            String aiResponse = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            return parseAIResponse(aiResponse);

        } catch (Exception e) {
            return new FraudAnalysisResult(0, List.of("AI_ANALYSIS_FAILED"), "AI analysis unavailable");
        }
    }

    private String buildTransactionContext(Transaction transaction) {
        List<Transaction> recentTransactions = transactionRepository
                .findByFromAccountIdAndCreatedAtAfter(
                        transaction.getFromAccountId(),
                        Instant.now().minus(7, ChronoUnit.DAYS)
                );
        Map<String, Object> context = new HashMap<>();
        context.put("recent_transaction_count", recentTransactions.size());
        context.put("account_age_days", calculateAccountAge(transaction.getFromAccountId()));
        context.put("average_transaction_amount", calculateAverageAmount(recentTransactions));
        context.put("transaction_frequency", calculateFrequency(recentTransactions));
        context.put("common_transaction_types", getCommonTransactionTypes(recentTransactions));

        try {
            return objectMapper.writeValueAsString(context);
        } catch (Exception e) {
            return null;
        }
    }

    private String formatTransactionForAI(Transaction transaction) {
        return String.format("""
            - Amount: $%.2f
            - Type: %s
            - From Account: %s
            - To Account: %s
            - Timestamp: %s
            - Day of Week: %s
            - Hour of Day: %d
            """,
                transaction.getAmount(),
                transaction.getType(),
                transaction.getFromAccountId(),
                transaction.getToAccountId(),
                transaction.getCreatedAt(),
                transaction.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).getDayOfWeek(),
                transaction.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).getHour()
        );
    }

    private FraudAnalysisResult parseAIResponse(String aiResponse) {
        try {
            JsonNode jsonResponse = objectMapper.readTree(aiResponse);

            int aiRiskScore = jsonResponse.path("ai_risk_score").asInt(0);
            String riskLevel = jsonResponse.path("risk_level").asText("LOW");
            String reasoning = jsonResponse.path("reasoning").asText("AI analysis completed");
            int confidence = jsonResponse.path("confidence").asInt(50);

            List<String> aiRiskFactors = new ArrayList<>();
            JsonNode patternsNode = jsonResponse.path("patterns");
            if (patternsNode.isArray()) {
                patternsNode.forEach(pattern -> aiRiskFactors.add("AI_" + pattern.asText()));
            }

            return new FraudAnalysisResult(aiRiskScore, aiRiskFactors, reasoning, confidence);

        } catch (Exception e) {
            return new FraudAnalysisResult(0, List.of("AI_PARSE_ERROR"), "Failed to parse AI response");
        }
    }

    private FraudAnalysisResult combineAnalysisResults(FraudAnalysisResult ruleBasedResult, FraudAnalysisResult aiResult) {
        int combinedScore = (int) (ruleBasedResult.getRiskScore() * 0.6 + aiResult.getRiskScore() * 0.4);
        List<String> combinedFactors = new ArrayList<>();
        combinedFactors.addAll(ruleBasedResult.getRiskFactors());
        combinedFactors.addAll(aiResult.getRiskFactors());

        String combinedReasoning = String.format(
                "Rule-based: %s | AI: %s",
                ruleBasedResult.getReasoning(),
                aiResult.getReasoning()
        );

        return new FraudAnalysisResult(combinedScore, combinedFactors, combinedReasoning);
    }

    private FraudAnalysis saveFraudAnalysis(Transaction transaction, FraudAnalysisResult result) {
        RiskLevel riskLevel = determineRiskLevel(result.getRiskScore());
        FraudStatus status = determineStatus(riskLevel);

        FraudAnalysis analysis = new FraudAnalysis();
        analysis.setTransactionId(transaction.getId().toString());
        analysis.setAccountId(transaction.getFromAccountId());
        analysis.setAmount(transaction.getAmount());
        analysis.setTransactionType(transaction.getType().toString());
        analysis.setRiskLevel(riskLevel);
        analysis.setRiskScore(result.getRiskScore());
        analysis.setRiskFactors(String.join(", ", result.getRiskFactors()));
        analysis.setAnalyzedAt(Instant.now());
        analysis.setStatus(status);
        analysis.setNotes(result.getReasoning());

        return fraudAnalysisRepository.save(analysis);
    }

    private int calculateAccountAge(String accountId) {
        return 30;
    }

    private double calculateAverageAmount(List<Transaction> transactions) {
        return transactions.stream()
                .mapToDouble(t -> t.getAmount().doubleValue())
                .average()
                .orElse(0.0);
    }

    private double calculateFrequency(List<Transaction> transactions) {
        return transactions.size() / 7.0;
    }

    private List<String> getCommonTransactionTypes(List<Transaction> transactions) {
        return transactions.stream()
                .map(t -> t.getType().toString())
                .distinct()
                .toList();
    }

    private int checkHighAmount(Transaction transaction, List<String> riskFactors) {
        BigDecimal threshold = new BigDecimal("5000");
        if (transaction.getAmount().compareTo(threshold) > 0) {
            riskFactors.add("HIGH_AMOUNT");
            return 30;
        }
        return 0;
    }

    private int checkVelocity(Transaction transaction, List<String> riskFactors) {
        Instant oneHourAgo = Instant.now().minus(1, ChronoUnit.HOURS);
        List<Transaction> recentTransactions = transactionRepository
                .findByFromAccountIdAndCreatedAtAfter(transaction.getFromAccountId(), oneHourAgo);

        if (recentTransactions.size() > 3) {
            riskFactors.add("HIGH_VELOCITY");
            return 25;
        }
        return 0;
    }

    private int checkUnusualTime(Transaction transaction, List<String> riskFactors) {
        int hour = Instant.now().atZone(java.time.ZoneId.systemDefault()).getHour();
        if (hour < 6 || hour > 22) {
            riskFactors.add("UNUSUAL_TIME");
            return 15;
        }
        return 0;
    }

    private int checkWeekendActivity(Transaction transaction, List<String> riskFactors) {
        java.time.DayOfWeek dayOfWeek = Instant.now()
                .atZone(java.time.ZoneId.systemDefault()).getDayOfWeek();

        if (dayOfWeek == java.time.DayOfWeek.SATURDAY ||
                dayOfWeek == java.time.DayOfWeek.SUNDAY) {
            BigDecimal weekendThreshold = new BigDecimal("1000");
            if (transaction.getAmount().compareTo(weekendThreshold) > 0) {
                riskFactors.add("HIGH_WEEKEND_ACTIVITY");
                return 20;
            }
        }
        return 0;
    }

    private int checkAccountPattern(Transaction transaction, List<String> riskFactors) {
        List<Transaction> accountHistory = transactionRepository
                .findByFromAccountIdOrToAccountId(transaction.getFromAccountId(), transaction.getFromAccountId());

        if (accountHistory.size() <= 1) {
            if (transaction.getAmount().compareTo(new BigDecimal("500")) > 0) {
                riskFactors.add("NEW_ACCOUNT_HIGH_AMOUNT");
                return 35;
            }
        }
        return 0;
    }

    private RiskLevel determineRiskLevel(int riskScore) {
        if (riskScore >= 80) return RiskLevel.CRITICAL;
        if (riskScore >= 60) return RiskLevel.HIGH;
        if (riskScore >= 30) return RiskLevel.MEDIUM;
        return RiskLevel.LOW;
    }

    private FraudStatus determineStatus(RiskLevel riskLevel) {
        switch (riskLevel) {
            case CRITICAL: return FraudStatus.BLOCKED;
            case HIGH: return FraudStatus.UNDER_REVIEW;
            case MEDIUM: return FraudStatus.FLAGGED;
            default: return FraudStatus.CLEARED;
        }
    }
}
