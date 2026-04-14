package com.media.publish.features.platform.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PlatformConfigDTO {
    private Long id;
    private String platform;
    private String credentialsMasked;
    private Boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
