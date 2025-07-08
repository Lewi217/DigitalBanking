package com.mycompany.app.service.userBehavior;

import com.mycompany.app.dto.AnalyticsRequestDto;
import com.mycompany.app.dto.UserBehaviorDto;
import com.mycompany.app.model.UserBehavior;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public interface IUserBehaviorService {
    UserBehaviorDto trackUserBehavior(AnalyticsRequestDto request);
    UserBehavior createUserBehavior(AnalyticsRequestDto request);
    List<UserBehaviorDto> getUserBehavior(String userId, Instant start, Instant end);
    List<UserBehaviorDto> getRecentUserBehavior(String userId, int days);
    Map<String, Long> getSessionStatistics(String userId);
    Map<String, Long> getDeviceTypeStatistics(String userId);
    void trackLogin(String userId, String sessionId, String deviceType, String ipAddress, String userAgent);
    void trackLogout(String userId, String sessionId, Double sessionDuration);
    void trackFeatureUsage(String userId, String sessionId, String feature, String deviceType);


}
