package com.media.publish.features.platform.dto;

import lombok.Data;

@Data
public class PlatformConfigRequest {
    private String platform;
    private String credentials;
    private Boolean enabled;
}
