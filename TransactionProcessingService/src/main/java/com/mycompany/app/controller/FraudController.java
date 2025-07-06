package com.mycompany.app.controller;

import com.mycompany.app.model.FraudAnalysis;
import com.mycompany.app.model.RiskLevel;
import com.mycompany.app.repository.FraudAnalysisRepository;
import com.mycompany.app.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

import static com.mycompany.app.exceptions.ApiResponseUtils.REQUEST_SUCCESS_MESSAGE;

@RestController
@RequestMapping("${api.prefix}/fraud")
@RequiredArgsConstructor
public class FraudController {

    private final FraudAnalysisRepository fraudAnalysisRepository;

    @GetMapping("/analysis/{accountId}")
    public ResponseEntity<ApiResponse> getAccountFraudHistory(@PathVariable String accountId) {
        List<FraudAnalysis> analyses = fraudAnalysisRepository.findByAccountId(accountId);
        return ResponseEntity.ok(new ApiResponse(REQUEST_SUCCESS_MESSAGE, analyses));
    }

    @GetMapping("/high-risk")
    public ResponseEntity<ApiResponse> getHighRiskTransactions() {
        List<FraudAnalysis> highRisk = fraudAnalysisRepository.findByRiskLevel(RiskLevel.HIGH);
        List<FraudAnalysis> critical = fraudAnalysisRepository.findByRiskLevel(RiskLevel.CRITICAL);
        highRisk.addAll(critical);
        return ResponseEntity.ok(new ApiResponse(REQUEST_SUCCESS_MESSAGE, highRisk));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse> getFraudDashboard(
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to) {

        if (from == null) from = Instant.now().minusSeconds(24 * 60 * 60);
        if (to == null) to = Instant.now();

        List<FraudAnalysis> analyses = fraudAnalysisRepository.findByAnalyzedAtBetween(from, to);
        return ResponseEntity.ok(new ApiResponse(REQUEST_SUCCESS_MESSAGE, analyses));
    }
}
