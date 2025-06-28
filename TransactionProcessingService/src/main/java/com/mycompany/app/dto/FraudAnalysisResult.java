package com.mycompany.app.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FraudAnalysisResult {
    private int riskScore;
    private List<String> riskFactors;
    private String reasoning;
    private int confidence = 100;

    public FraudAnalysisResult(int riskScore, List<String> riskFactors, String reasoning) {
        this.riskScore = riskScore;
        this.riskFactors = riskFactors;
        this.reasoning = reasoning;
        this.confidence = 100;
    }
}
