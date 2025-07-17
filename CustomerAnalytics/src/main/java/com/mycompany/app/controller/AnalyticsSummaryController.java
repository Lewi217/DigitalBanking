package com.mycompany.app.controller;

import com.mycompany.app.dto.AnalyticsSummaryDto;
import com.mycompany.app.response.ApiResponse;
import com.mycompany.app.service.analyticsSummary.AnalyticsSummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static com.mycompany.app.exceptions.ApiResponseUtils.REQUEST_ERROR_MESSAGE;
import static com.mycompany.app.exceptions.ApiResponseUtils.REQUEST_SUCCESS_MESSAGE;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@RestController
@RequestMapping("${api.prefix}/analytics")
@RequiredArgsConstructor
public class AnalyticsSummaryController {

    private final AnalyticsSummaryService analyticsSummaryService;


    @PostMapping("/generate")
    public ResponseEntity<ApiResponse> generateAnalytics(@RequestParam("userId") String userId, @RequestParam("period") String period) {
        try {
            AnalyticsSummaryDto summary = analyticsSummaryService.generateSummary(userId, period);
            return ResponseEntity.ok(new ApiResponse(REQUEST_SUCCESS_MESSAGE, summary));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(REQUEST_ERROR_MESSAGE, "Failed to generate analytics: " + e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse> getUserAnalytics(@PathVariable("userId") String userId, @RequestParam(value= "period", required = false) String period) {
        try {
            List<AnalyticsSummaryDto> summaries = analyticsSummaryService.getUserSummaries(userId, period);
            return ResponseEntity.ok(new ApiResponse(REQUEST_SUCCESS_MESSAGE, summaries));
        } catch (Exception e) {
            return ResponseEntity.status(BAD_REQUEST)
                    .body(new ApiResponse(REQUEST_ERROR_MESSAGE, "Failed to retrieve analytics: " + e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}/latest")
    public ResponseEntity<ApiResponse> getLatestAnalytics(@PathVariable("userId") String userId) {
        try {
            AnalyticsSummaryDto summary = analyticsSummaryService.getLatestSummary(userId);
            return ResponseEntity.ok(new ApiResponse(REQUEST_SUCCESS_MESSAGE, summary));
        } catch (Exception e) {
            return ResponseEntity.status(BAD_REQUEST)
                    .body(new ApiResponse(REQUEST_ERROR_MESSAGE, "Failed to get latest summary: " + e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}/range")
    public ResponseEntity<ApiResponse> getAnalyticsByDateRange(@PathVariable("userId") String userId, @RequestParam("start") Instant start, @RequestParam("end") Instant end) {
        try {
            List<AnalyticsSummaryDto> summaries = analyticsSummaryService.getSummariesByDateRange(userId, start, end);
            return ResponseEntity.ok(new ApiResponse(REQUEST_SUCCESS_MESSAGE, summaries));
        } catch (Exception e) {
            return ResponseEntity.status(BAD_REQUEST)
                    .body(new ApiResponse(REQUEST_ERROR_MESSAGE, "Failed to retrieve summaries: " + e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}/metrics")
    public ResponseEntity<ApiResponse> getUserMetricsSummary(@PathVariable("userId") String userId) {
        try {
            Map<String, Object> metrics = analyticsSummaryService.getUserMetricsSummary(userId);
            return ResponseEntity.ok(new ApiResponse(REQUEST_SUCCESS_MESSAGE, metrics));
        } catch (Exception e) {
            return ResponseEntity.status(BAD_REQUEST)
                    .body(new ApiResponse(REQUEST_ERROR_MESSAGE, "Failed to retrieve metrics: " + e.getMessage()));
        }
    }
}

