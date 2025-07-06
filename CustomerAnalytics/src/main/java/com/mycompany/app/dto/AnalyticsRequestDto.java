package com.mycompany.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsRequestDto {
    private String userId;
    private String action;
    private String sessionId;
    private String deviceType;
    private String ipAddress;
    private String userAgent;
    private String location;
    private Double duration;
    private Instant timestamp;
}