package com.media.publish.features.publish.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * 平台类型枚举
 */
@Getter
@RequiredArgsConstructor
public enum PlatformType {

    CSDN("CSDN", "CSDN博客", "https://editor.csdn.net/", "blog.csdn.net/article/details/"),
    ZHIHU("ZHIHU", "知乎专栏", "https://zhuanlan.zhihu.com/write", "zhuanlan.zhihu.com/p/"),
    WECHAT("WECHAT", "微信公众号", "https://mp.weixin.qq.com/", "mp.weixin.qq.com/s/");

    private final String code;
    private final String name;
    private final String editorUrl;
    private final String articleUrlPrefix;

    public static PlatformType fromCode(String code) {
        return Arrays.stream(values())
                .filter(p -> p.getCode().equalsIgnoreCase(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("不支持的平台: " + code));
    }
}
