package com.media.publish.features.publish.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 发布状态枚举
 */
@Getter
@RequiredArgsConstructor
public enum PublishStatus {

    PENDING(0, "发布中"),
    SUCCESS(1, "已发布"),
    FAILED(2, "发布失败");

    private final int code;
    private final String description;

    public static PublishStatus fromCode(int code) {
        for (PublishStatus status : values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("无效的发布状态: " + code);
    }
}
