package com.mycompany.app.controller;

import com.mycompany.app.dto.FinancialPatternDto;
import com.mycompany.app.model.PatternType;
import com.mycompany.app.response.ApiResponse;
import com.mycompany.app.service.financialPattern.FinancialPatternService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.mycompany.app.exceptions.ApiResponseUtils.REQUEST_ERROR_MESSAGE;
import static com.mycompany.app.exceptions.ApiResponseUtils.REQUEST_SUCCESS_MESSAGE;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@RestController
@RequestMapping("${api.prefix}/patterns")
@RequiredArgsConstructor
public class FinancialPatternController {

    private final FinancialPatternService financialPatternService;


    @PostMapping("/analyze")
    public ResponseEntity<ApiResponse> analyzePatterns(@RequestParam String userId) {
        try {
            List<FinancialPatternDto> patterns = financialPatternService.analyzeUserPatterns(userId);
            return ResponseEntity.ok(new ApiResponse(REQUEST_SUCCESS_MESSAGE, patterns));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(REQUEST_ERROR_MESSAGE, "Failed to analyze patterns: " + e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse> getUserPatterns(
            @PathVariable String userId,
            @RequestParam(required = false) PatternType patternType) {
        try {
            List<FinancialPatternDto> patterns = financialPatternService.getUserPatterns(userId, patternType);
            return ResponseEntity.ok(new ApiResponse(REQUEST_SUCCESS_MESSAGE, patterns));
        } catch (Exception e) {
            return ResponseEntity.status(BAD_REQUEST)
                    .body(new ApiResponse(REQUEST_ERROR_MESSAGE, "Failed to fetch patterns: " + e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}/confidence")
    public ResponseEntity<ApiResponse> getHighConfidencePatterns(
            @PathVariable String userId,
            @RequestParam(required = false) Double minConfidence) {
        try {
            List<FinancialPatternDto> patterns = financialPatternService.getHighConfidencePatterns(userId, minConfidence);
            return ResponseEntity.ok(new ApiResponse(REQUEST_SUCCESS_MESSAGE, patterns));
        } catch (Exception e) {
            return ResponseEntity.status(BAD_REQUEST)
                    .body(new ApiResponse(REQUEST_ERROR_MESSAGE, "Failed to retrieve high confidence patterns: " + e.getMessage()));
        }
    }
}