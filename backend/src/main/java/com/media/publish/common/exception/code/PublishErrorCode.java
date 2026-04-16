package com.media.publish.common.exception.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 发布模块错误码
 */
@Getter
@RequiredArgsConstructor
public enum PublishErrorCode {

    // 文章相关错误 PUB_001xxx
    ARTICLE_NOT_FOUND("PUB_001001", "文章不存在"),
    ARTICLE_ID_EMPTY("PUB_001002", "文章ID不能为空"),

    // 平台相关错误 PUB_002xxx
    PLATFORM_NOT_SUPPORTED("PUB_002001", "不支持的平台"),
    PLATFORM_NOT_CONFIGURED("PUB_002002", "平台未配置"),
    PLATFORM_CREDENTIAL_EXPIRED("PUB_002003", "平台凭证已过期"),

    // 发布相关错误 PUB_003xxx
    PUBLISH_FAILED("PUB_003001", "发布失败"),
    PUBLISH_TIMEOUT("PUB_003002", "发布超时"),
    LOGIN_REQUIRED("PUB_003003", "需要登录"),
    LOGIN_TIMEOUT("PUB_003004", "登录超时"),

    // 参数校验错误 PUB_004xxx
    PLATFORM_EMPTY("PUB_004001", "请选择至少一个发布平台");

    private final String code;
    private final String message;
}
