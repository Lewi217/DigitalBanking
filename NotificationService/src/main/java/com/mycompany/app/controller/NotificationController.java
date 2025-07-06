package com.mycompany.app.controller;

import com.mycompany.app.dto.NotificationDto;
import com.mycompany.app.dto.NotificationRequest;
import com.mycompany.app.response.ApiResponse;
import com.mycompany.app.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

import static com.mycompany.app.exceptions.ApiResponseUtils.REQUEST_ERROR_MESSAGE;
import static com.mycompany.app.exceptions.ApiResponseUtils.REQUEST_SUCCESS_MESSAGE;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@RestController
@RequestMapping("${api.prefix}/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/send")
    public ResponseEntity<ApiResponse> sendNotification(@RequestBody NotificationRequest request) {
        try {
            NotificationDto notification = notificationService.createAndSendNotification(request);
            return ResponseEntity.ok(new ApiResponse(REQUEST_SUCCESS_MESSAGE, notification));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(REQUEST_ERROR_MESSAGE, "Failed to send notification: " + e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse> getUserNotifications(@PathVariable String userId) {
        try {
            List<NotificationDto> notifications = notificationService.getUserNotifications(userId);
            return ResponseEntity.ok(new ApiResponse(REQUEST_SUCCESS_MESSAGE, notifications));
        } catch (Exception e) {
            return ResponseEntity.status(BAD_REQUEST)
                    .body(new ApiResponse(REQUEST_ERROR_MESSAGE, "Failed to retrieve notifications: " + e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}/unread")
    public ResponseEntity<ApiResponse> getUnreadNotifications(@PathVariable String userId) {
        try {
            List<NotificationDto> notifications = notificationService.getUnreadNotifications(userId);
            return ResponseEntity.ok(new ApiResponse(REQUEST_SUCCESS_MESSAGE, notifications));
        } catch (Exception e) {
            return ResponseEntity.status(BAD_REQUEST)
                    .body(new ApiResponse(REQUEST_ERROR_MESSAGE, "Failed to retrieve unread notifications: " + e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}/count")
    public ResponseEntity<ApiResponse> getUnreadCount(@PathVariable String userId) {
        try {
            long count = notificationService.getUnreadCount(userId);
            return ResponseEntity.ok(new ApiResponse(REQUEST_SUCCESS_MESSAGE, count));
        } catch (Exception e) {
            return ResponseEntity.status(BAD_REQUEST)
                    .body(new ApiResponse(REQUEST_ERROR_MESSAGE, "Failed to get unread count: " + e.getMessage()));
        }
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse> markAsRead(@PathVariable Long notificationId) {
        try {
            notificationService.markAsRead(notificationId);
            return ResponseEntity.ok(new ApiResponse(REQUEST_SUCCESS_MESSAGE, "Notification marked as read"));
        } catch (Exception e) {
            return ResponseEntity.status(BAD_REQUEST)
                    .body(new ApiResponse(REQUEST_ERROR_MESSAGE, "Failed to mark notification as read: " + e.getMessage()));
        }
    }

    // Helper endpoint for testing transaction notifications
    @PostMapping("/transaction")
    public ResponseEntity<ApiResponse> sendTransactionNotification(
            @RequestParam String userId,
            @RequestParam String transactionType,
            @RequestParam String amount,
            @RequestParam String accountNumber) {
        try {
            notificationService.sendTransactionNotification(userId, transactionType, amount, accountNumber);
            return ResponseEntity.ok(new ApiResponse(REQUEST_SUCCESS_MESSAGE, "Transaction notification sent"));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(REQUEST_ERROR_MESSAGE, "Failed to send transaction notification: " + e.getMessage()));
        }
    }
}

