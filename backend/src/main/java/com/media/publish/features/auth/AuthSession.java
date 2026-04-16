package com.media.publish.features.auth;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 认证会话
 */
@Data
public class AuthSession {
    private String sessionId;
    private String platform;
    private int websockifyPort;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private String status;
    private String extractedCookies;
}
