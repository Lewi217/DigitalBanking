package com.mycompany.app.model;

import com.mycompany.app.event.NotificationPriority;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String eventType;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition =  "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    private NotificationPriority priority;

    @Enumerated(EnumType.STRING)
    private NotificationStatus status = NotificationStatus.PENDING;

    @Enumerated(EnumType.STRING)
    private NotificationChannel channel;


    @Column
    private String metadata;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime sentAt;

    @Column
    private boolean isRead = false;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
