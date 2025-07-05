package com.mycompany.app.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.Instant;
import java.util.Map;

@Document(collection = "user_behavior")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBehavior {
    @Id
    private String id;

    @Field("user_id")
    @Indexed
    private String userId;

    @Field("session_id")
    @Indexed
    private String sessionId;

    @Field("action")
    private String action;

    @Field("timestamp")
    @Indexed
    private Instant timestamp;

    @Field("device_type")
    private String deviceType;

    @Field("ip_address")
    private String ipAddress;

    @Field("user_agent")
    private String userAgent;

    @Field("metadata")
    private Map<String, String> metadata;

    @Field("duration")
    private Double duration; // in seconds

    @Field("location")
    private String location;
}
