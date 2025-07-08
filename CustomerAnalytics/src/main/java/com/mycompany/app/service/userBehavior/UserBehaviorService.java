package com.mycompany.app.service.userBehavior;

import com.mycompany.app.dto.AnalyticsRequestDto;
import com.mycompany.app.dto.UserBehaviorDto;
import com.mycompany.app.model.UserBehavior;
import com.mycompany.app.repository.UserBehaviorRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserBehaviorService implements IUserBehaviorService {

    private final UserBehaviorRepository userBehaviorRepository;
    private final ModelMapper modelMapper;

    @Transactional
    @Override
    public UserBehaviorDto trackUserBehavior(AnalyticsRequestDto request) {
        UserBehavior behavior = createUserBehavior(request);
        behavior = userBehaviorRepository.save(behavior);
        return convertToDto(behavior);
    }

    @Override
    public UserBehavior createUserBehavior(AnalyticsRequestDto request) {
        UserBehavior behavior = new UserBehavior();
        behavior.setUserId(request.getUserId());
        behavior.setSessionId(request.getSessionId());
        behavior.setAction(request.getAction());
        behavior.setTimestamp(request.getTimestamp() != null ? request.getTimestamp() : Instant.now());
        behavior.setDeviceType(request.getDeviceType());
        behavior.setIpAddress(request.getIpAddress());
        behavior.setUserAgent(request.getUserAgent());
        behavior.setLocation(request.getLocation());
        behavior.setDuration(request.getDuration());
        return behavior;
    }

    @Override
    public List<UserBehaviorDto> getUserBehavior(String userId, Instant start, Instant end) {
        List<UserBehavior> behaviors = (start != null && end != null)
                ? userBehaviorRepository.findByUserIdAndTimestampBetween(userId, start, end)
                : userBehaviorRepository.findByUserId(userId);

        return behaviors.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserBehaviorDto> getRecentUserBehavior(String userId, int days) {
        Instant end = Instant.now();
        Instant start = end.minus(days, ChronoUnit.DAYS);

        List<UserBehavior> behaviors = userBehaviorRepository.findByUserIdAndTimestampBetween(userId, start, end);
        return behaviors.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Long> getSessionStatistics(String userId) {
        List<UserBehavior> behaviors = userBehaviorRepository.findByUserId(userId);
        Map<String, Long> stats = new HashMap<>();

        long totalSessions = behaviors.stream()
                .filter(b -> b.getSessionId() != null)
                .map(UserBehavior::getSessionId)
                .distinct()
                .count();

        long totalActions = behaviors.size();

        double averageSessionDuration = behaviors.stream()
                .filter(b -> b.getDuration() != null)
                .mapToDouble(UserBehavior::getDuration)
                .average()
                .orElse(0.0);

        stats.put("total_sessions", totalSessions);
        stats.put("total_actions", totalActions);
        stats.put("average_session_duration", Math.round(averageSessionDuration));

        return stats;
    }

    @Override
    public Map<String, Long> getDeviceTypeStatistics(String userId) {
        List<UserBehavior> behaviors = userBehaviorRepository.findByUserId(userId);

        return behaviors.stream()
                .filter(b -> b.getDeviceType() != null)
                .collect(Collectors.groupingBy(UserBehavior::getDeviceType, Collectors.counting()));
    }

    @Override
    public void trackLogin(String userId, String sessionId, String deviceType, String ipAddress, String userAgent) {
        AnalyticsRequestDto request = new AnalyticsRequestDto();
        request.setUserId(userId);
        request.setSessionId(sessionId);
        request.setAction("LOGIN");
        request.setDeviceType(deviceType);
        request.setIpAddress(ipAddress);
        request.setUserAgent(userAgent);
        request.setTimestamp(Instant.now());

        trackUserBehavior(request);
    }

    @Override
    public void trackLogout(String userId, String sessionId, Double sessionDuration) {
        AnalyticsRequestDto request = new AnalyticsRequestDto();
        request.setUserId(userId);
        request.setSessionId(sessionId);
        request.setAction("LOGOUT");
        request.setDuration(sessionDuration);
        request.setTimestamp(Instant.now());

        trackUserBehavior(request);
    }

    @Override
    public void trackFeatureUsage(String userId, String sessionId, String feature, String deviceType) {
        AnalyticsRequestDto request = new AnalyticsRequestDto();
        request.setUserId(userId);
        request.setSessionId(sessionId);
        request.setAction("FEATURE_USAGE");
        request.setDeviceType(deviceType);
        request.setTimestamp(Instant.now());

        trackUserBehavior(request);
    }

    private UserBehaviorDto convertToDto(UserBehavior behavior) {
        return modelMapper.map(behavior, UserBehaviorDto.class);
    }
}
