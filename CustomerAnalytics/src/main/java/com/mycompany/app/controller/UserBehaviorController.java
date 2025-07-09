package com.mycompany.app.controller;

import com.mycompany.app.dto.AnalyticsRequestDto;
import com.mycompany.app.dto.UserBehaviorDto;
import com.mycompany.app.response.ApiResponse;
import com.mycompany.app.service.userBehavior.UserBehaviorService;
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
@RequestMapping("${api.prefix}/behavior")
@RequiredArgsConstructor
public class UserBehaviorController {

    private final UserBehaviorService userBehaviorService;

    @PostMapping("/track")
    public ResponseEntity<ApiResponse> trackUserBehavior(@RequestBody AnalyticsRequestDto request) {
        try {
            UserBehaviorDto behavior = userBehaviorService.trackUserBehavior(request);
            return ResponseEntity.ok(new ApiResponse(REQUEST_SUCCESS_MESSAGE, behavior));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(REQUEST_ERROR_MESSAGE, e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse> getUserBehavior(@PathVariable String userId, @RequestParam(required = false) Instant start, @RequestParam(required = false) Instant end) {
        try {
            List<UserBehaviorDto> behaviorList = userBehaviorService.getUserBehavior(userId, start, end);
            return ResponseEntity.ok(new ApiResponse(REQUEST_SUCCESS_MESSAGE, behaviorList));
        } catch (Exception e) {
            return ResponseEntity.status(BAD_REQUEST)
                    .body(new ApiResponse(REQUEST_ERROR_MESSAGE, e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}/recent")
    public ResponseEntity<ApiResponse> getRecentUserBehavior(@PathVariable String userId, @RequestParam(defaultValue = "7") int days) {
        try {
            List<UserBehaviorDto> recent = userBehaviorService.getRecentUserBehavior(userId, days);
            return ResponseEntity.ok(new ApiResponse(REQUEST_SUCCESS_MESSAGE, recent));
        } catch (Exception e) {
            return ResponseEntity.status(BAD_REQUEST)
                    .body(new ApiResponse(REQUEST_ERROR_MESSAGE, e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}/stats/sessions")
    public ResponseEntity<ApiResponse> getSessionStats(@PathVariable String userId) {
        try {
            Map<String, Long> stats = userBehaviorService.getSessionStatistics(userId);
            return ResponseEntity.ok(new ApiResponse(REQUEST_SUCCESS_MESSAGE, stats));
        } catch (Exception e) {
            return ResponseEntity.status(BAD_REQUEST)
                    .body(new ApiResponse(REQUEST_ERROR_MESSAGE, e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}/stats/devices")
    public ResponseEntity<ApiResponse> getDeviceStats(@PathVariable String userId) {
        try {
            Map<String, Long> stats = userBehaviorService.getDeviceTypeStatistics(userId);
            return ResponseEntity.ok(new ApiResponse(REQUEST_SUCCESS_MESSAGE, stats));
        } catch (Exception e) {
            return ResponseEntity.status(BAD_REQUEST)
                    .body(new ApiResponse(REQUEST_ERROR_MESSAGE, e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> trackLogin(@RequestParam String userId, @RequestParam String sessionId, @RequestParam String deviceType, @RequestParam(required = false) String ipAddress, @RequestParam(required = false) String userAgent) {
        try {
            userBehaviorService.trackLogin(userId, sessionId, deviceType, ipAddress, userAgent);
            return ResponseEntity.ok(new ApiResponse(REQUEST_SUCCESS_MESSAGE, null));
        } catch (Exception e) {
            return ResponseEntity.status(BAD_REQUEST)
                    .body(new ApiResponse(REQUEST_ERROR_MESSAGE, e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> trackLogout(@RequestParam String userId, @RequestParam String sessionId, @RequestParam(required = false) Double duration) {
        try {
            userBehaviorService.trackLogout(userId, sessionId, duration);
            return ResponseEntity.ok(new ApiResponse(REQUEST_SUCCESS_MESSAGE, null));
        } catch (Exception e) {
            return ResponseEntity.status(BAD_REQUEST)
                    .body(new ApiResponse(REQUEST_ERROR_MESSAGE, e.getMessage()));
        }
    }

    @PostMapping("/feature-usage")
    public ResponseEntity<ApiResponse> trackFeatureUsage(@RequestParam String userId, @RequestParam String sessionId, @RequestParam String feature, @RequestParam(required = false) String deviceType) {
        try {
            userBehaviorService.trackFeatureUsage(userId, sessionId, feature, deviceType);
            return ResponseEntity.ok(new ApiResponse(REQUEST_SUCCESS_MESSAGE, null));
        } catch (Exception e) {
            return ResponseEntity.status(BAD_REQUEST)
                    .body(new ApiResponse(REQUEST_ERROR_MESSAGE, e.getMessage()));
        }
    }
}
