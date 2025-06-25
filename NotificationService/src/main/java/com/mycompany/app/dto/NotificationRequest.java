package com.mycompany.app.dto;

import com.mycompany.app.event.NotificationPriority;
import com.mycompany.app.model.NotificationChannel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {
    private String userId;
    private String eventType;
    private String title;
    private String message;
    private NotificationPriority priority;
    private NotificationChannel channel;
    private BigDecimal amount;
    private String metadata;
}
