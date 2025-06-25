package com.mycompany.app.event;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {
    private String eventId;
    private String userId;
    private String accountId;
    private String eventType;
    private String title;
    private String message;
    private NotificationPriority priority;
    private BigDecimal amount;
    private Instant timestamp;
    private String metadata;
}

