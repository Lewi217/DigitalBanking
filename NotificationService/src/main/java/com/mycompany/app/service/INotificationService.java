package com.mycompany.app.service;

import com.mycompany.app.dto.NotificationDto;
import com.mycompany.app.dto.NotificationRequest;
import com.mycompany.app.dto.UserDto;
import com.mycompany.app.event.NotificationPriority;
import com.mycompany.app.model.Notification;
import com.mycompany.app.model.NotificationChannel;

import java.util.List;

public interface INotificationService {
    NotificationDto createAndSendNotification(NotificationRequest request);
    Notification createNotification(NotificationRequest request);
    NotificationChannel determineChannel(NotificationPriority priority);
    void sendNotification(Notification notification, UserDto user);
    List<NotificationDto> getUserNotifications(String userId);
    List<NotificationDto> getUnreadNotifications(String userId);
    long getUnreadCount(String userId);
    void markAsRead(Long notificationId);
    void sendTransactionNotification(String userId, String transactionType, String amount, String accountNumber);


}
