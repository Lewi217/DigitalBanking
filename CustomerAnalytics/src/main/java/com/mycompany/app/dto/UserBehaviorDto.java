package com.mycompany.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBehaviorDto {
    private String id;
    private String userId;
    private String sessionId;
    private String action;
    private Instant timestamp;
    private String deviceType;
    private String ipAddress;
    private String userAgent;
    private Map<String, String> metadata;
    private Double duration;
    private String location;
}
