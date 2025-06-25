package com.mycompany.app.service;

import com.mycompany.app.client.AuthServiceClient;
import com.mycompany.app.dto.NotificationDto;
import com.mycompany.app.dto.NotificationRequest;
import com.mycompany.app.dto.UserDto;
import com.mycompany.app.event.NotificationPriority;
import com.mycompany.app.model.Notification;
import com.mycompany.app.model.NotificationChannel;
import com.mycompany.app.model.NotificationStatus;
import com.mycompany.app.repository.NotificationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService implements INotificationService{

    private final NotificationRepository notificationRepository;
    private final EmailNotificationService emailService;
    private final AuthServiceClient authServiceClient;
    private final ModelMapper modelMapper;

    @Transactional
    @Override
    public NotificationDto createAndSendNotification(NotificationRequest request) {
        try {
            UserDto user = authServiceClient.getUserById(Long.valueOf(request.getUserId()));
            Notification notification = createNotification(request);
            notification = notificationRepository.save(notification);
            sendNotification(notification, user);

            return convertToDto(notification);

        } catch (Exception e) {
            throw new RuntimeException("Failed to create notification: " + e.getMessage());
        }
    }

    @Override
    public Notification createNotification(NotificationRequest request) {
        Notification notification = new Notification();
        notification.setUserId(request.getUserId());
        notification.setEventType(request.getEventType());
        notification.setTitle(request.getTitle());
        notification.setMessage(request.getMessage());
        notification.setPriority(request.getPriority() != null ? request.getPriority() : NotificationPriority.MEDIUM);
        notification.setChannel(request.getChannel() != null ? request.getChannel() : determineChannel(request.getPriority()));
        notification.setMetadata(request.getMetadata());

        return notification;
    }

    @Override
    public NotificationChannel determineChannel(NotificationPriority priority) {
        if (priority == NotificationPriority.CRITICAL || priority == NotificationPriority.HIGH) {
            return NotificationChannel.EMAIL;
        } else {
            return NotificationChannel.IN_APP;
        }
    }

    @Override
    public void sendNotification(Notification notification, UserDto user) {
        try {
            boolean sent = false;

            switch (notification.getChannel()) {
                case EMAIL:
                    sent = emailService.sendEmail(
                            user.getEmail(),
                            notification.getTitle(),
                            notification.getMessage()
                    );
                    break;
                case IN_APP:
                case PUSH:
                    sent = true;
                    break;
            }
            if (sent) {
                notification.setStatus(NotificationStatus.SENT);
                notification.setSentAt(LocalDateTime.now());
            } else {
                notification.setStatus(NotificationStatus.FAILED);
            }

        } catch (Exception e) {
            notification.setStatus(NotificationStatus.FAILED);
        }
        notificationRepository.save(notification);
    }
    @Override
    public List<NotificationDto> getUserNotifications(String userId) {
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return notifications.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<NotificationDto> getUnreadNotifications(String userId) {
        List<Notification> notifications = notificationRepository.findByUserIdAndIsReadOrderByCreatedAtDesc(userId, false);
        return notifications.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public long getUnreadCount(String userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Transactional
    @Override
    public void markAsRead(Long notificationId) {
        Optional<Notification> notificationOpt = notificationRepository.findById(notificationId);
        if (notificationOpt.isPresent()) {
            Notification notification = notificationOpt.get();
            notification.setRead(true);  // Use setRead() instead of setIsRead()
            notificationRepository.save(notification);
        }
    }

    @Override
    public void sendTransactionNotification(String userId, String transactionType, String amount, String accountNumber) {
        NotificationRequest request = new NotificationRequest();
        request.setUserId(userId);
        request.setEventType("TRANSACTION_" + transactionType.toUpperCase());
        request.setTitle("Transaction " + transactionType);
        request.setMessage(String.format("Your %s of %s has been processed for account %s",
                transactionType.toLowerCase(), amount, accountNumber));
        request.setPriority(NotificationPriority.MEDIUM);
        request.setChannel(NotificationChannel.EMAIL);

        createAndSendNotification(request);
    }

    private NotificationDto convertToDto(Notification notification) {
        return modelMapper.map(notification, NotificationDto.class);
    }
}
