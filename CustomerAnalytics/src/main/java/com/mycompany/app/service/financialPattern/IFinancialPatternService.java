package com.mycompany.app.service.financialPattern;

import com.mycompany.app.dto.FinancialPatternDto;
import com.mycompany.app.dto.TransactionDto;
import com.mycompany.app.model.FinancialPattern;
import com.mycompany.app.model.PatternType;

import java.time.Instant;
import java.util.List;

public interface IFinancialPatternService {
    List<FinancialPatternDto> analyzeUserPatterns(String userId);
    List<FinancialPattern> analyzeAccountPatterns(String userId, String accountNumber);
    List<FinancialPattern> analyzeSpendingPatterns(String userId, String accountNumber, List<TransactionDto> transactions, Instant start, Instant end);
    List<FinancialPattern> analyzeIncomePatterns(String userId, String accountNumber,List<TransactionDto> transactions, Instant start, Instant end);
    List<FinancialPatternDto> getUserPatterns(String userId, PatternType patternType);
    List<FinancialPatternDto> getHighConfidencePatterns(String userId, Double minConfidence);
    FinancialPatternDto convertToDto(FinancialPattern pattern);
}
