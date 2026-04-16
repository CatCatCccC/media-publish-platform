package com.media.publish.features.publish.strategy;

import com.media.publish.features.publish.constant.PublishStatus;
import lombok.Builder;
import lombok.Data;

/**
 * 发布结果
 */
@Data
@Builder
public class PublishResult {

    private boolean success;
    private PublishStatus status;
    private String articleUrl;
    private String errorMessage;
    private String platform;

    public static PublishResult success(String platform, String articleUrl) {
        return PublishResult.builder()
                .success(true)
                .status(PublishStatus.SUCCESS)
                .platform(platform)
                .articleUrl(articleUrl)
                .build();
    }

    public static PublishResult pending(String platform) {
        return PublishResult.builder()
                .success(false)
                .status(PublishStatus.PENDING)
                .platform(platform)
                .build();
    }

    public static PublishResult failed(String platform, String errorMessage) {
        return PublishResult.builder()
                .success(false)
                .status(PublishStatus.FAILED)
                .platform(platform)
                .errorMessage(errorMessage)
                .build();
    }
}
