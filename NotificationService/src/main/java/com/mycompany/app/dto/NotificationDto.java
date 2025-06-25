package com.mycompany.app.dto;

import com.mycompany.app.event.NotificationPriority;
import com.mycompany.app.model.NotificationChannel;
import com.mycompany.app.model.NotificationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {
    private Long id;
    private String userId;
    private String eventType;
    private String title;
    private String message;
    private NotificationPriority priority;
    private NotificationStatus status;
    private NotificationChannel channel;
    private String metadata;
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
    private boolean isRead;
}
