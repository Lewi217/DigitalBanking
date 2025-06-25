package com.mycompany.app.repository;

import com.mycompany.app.model.Notification;
import com.mycompany.app.model.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserIdOrderByCreatedAtDesc(String userId);
    List<Notification> findByUserIdAndIsReadOrderByCreatedAtDesc(String userId, boolean isRead);
    List<Notification> findByStatusAndCreatedAtBefore(NotificationStatus status, LocalDateTime dateTime);
    long countByUserIdAndIsReadFalse(String userId);
    Optional<Notification> findById(Long notificationId);
}