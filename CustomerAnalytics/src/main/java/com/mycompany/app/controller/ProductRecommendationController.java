package com.mycompany.app.controller;

import com.mycompany.app.dto.ProductRecommendationDto;
import com.mycompany.app.model.RecommendationStatus;
import com.mycompany.app.response.ApiResponse;
import com.mycompany.app.service.productRecommendation.ProductRecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.mycompany.app.exceptions.ApiResponseUtils.REQUEST_ERROR_MESSAGE;
import static com.mycompany.app.exceptions.ApiResponseUtils.REQUEST_SUCCESS_MESSAGE;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@RestController
@RequestMapping("${api.prefix}/recommendations")
@RequiredArgsConstructor
public class ProductRecommendationController {

    private final ProductRecommendationService productRecommendationService;

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse> generateRecommendations(@RequestParam("userId") String userId) {
        try {
            List<ProductRecommendationDto> recommendations = productRecommendationService.generateAndSaveRecommendations(userId);
            return ResponseEntity.ok(new ApiResponse(REQUEST_SUCCESS_MESSAGE, recommendations));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(REQUEST_ERROR_MESSAGE, "Failed to generate recommendations: " + e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse> getUserRecommendations(@PathVariable("userId") String userId, @RequestParam(value = "status" ,required = false) RecommendationStatus status) {
        try {
            List<ProductRecommendationDto> recommendations = productRecommendationService.getUserRecommendations(userId, status);
            return ResponseEntity.ok(new ApiResponse(REQUEST_SUCCESS_MESSAGE, recommendations));
        } catch (Exception e) {
            return ResponseEntity.status(BAD_REQUEST)
                    .body(new ApiResponse(REQUEST_ERROR_MESSAGE, "Failed to retrieve recommendations: " + e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}/top")
    public ResponseEntity<ApiResponse> getTopRecommendations(@PathVariable("userId") String userId) {
        try {
            List<ProductRecommendationDto> topRecs = productRecommendationService.getTopRecommendations(userId);
            return ResponseEntity.ok(new ApiResponse(REQUEST_SUCCESS_MESSAGE, topRecs));
        } catch (Exception e) {
            return ResponseEntity.status(BAD_REQUEST)
                    .body(new ApiResponse(REQUEST_ERROR_MESSAGE, "Failed to retrieve top recommendations: " + e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}/pending")
    public ResponseEntity<ApiResponse> getPendingRecommendations(@PathVariable("userId") String userId) {
        try {
            List<ProductRecommendationDto> pending = productRecommendationService.getPendingRecommendations(userId);
            return ResponseEntity.ok(new ApiResponse(REQUEST_SUCCESS_MESSAGE, pending));
        } catch (Exception e) {
            return ResponseEntity.status(BAD_REQUEST)
                    .body(new ApiResponse(REQUEST_ERROR_MESSAGE, "Failed to retrieve pending recommendations: " + e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}/pending/count")
    public ResponseEntity<ApiResponse> getPendingCount(@PathVariable("userId") String userId) {
        try {
            long count = productRecommendationService.getPendingRecommendationsCount(userId);
            return ResponseEntity.ok(new ApiResponse(REQUEST_SUCCESS_MESSAGE, count));
        } catch (Exception e) {
            return ResponseEntity.status(BAD_REQUEST)
                    .body(new ApiResponse(REQUEST_ERROR_MESSAGE, "Failed to retrieve pending count: " + e.getMessage()));
        }
    }

    @PutMapping("/{recommendationId}/status")
    public ResponseEntity<ApiResponse> updateRecommendationStatus(@PathVariable("recommendationId") String recommendationId, @RequestParam("status") RecommendationStatus status) {
        try {
            productRecommendationService.updateRecommendationStatus(recommendationId, status);
            return ResponseEntity.ok(new ApiResponse(REQUEST_SUCCESS_MESSAGE, "Status updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(BAD_REQUEST)
                    .body(new ApiResponse(REQUEST_ERROR_MESSAGE, "Failed to update status: " + e.getMessage()));
        }
    }
}

