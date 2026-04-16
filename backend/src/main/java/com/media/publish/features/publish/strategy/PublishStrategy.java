package com.media.publish.features.publish.strategy;

import com.media.publish.features.article.entity.Article;
import com.media.publish.features.publish.constant.PlatformType;

/**
 * 发布策略接口
 * 使用策略模式，不同平台实现不同的发布逻辑
 */
public interface PublishStrategy {

    /**
     * 获取支持的平台类型
     */
    PlatformType getPlatformType();

    /**
     * 发布文章
     *
     * @param article    文章内容
     * @param credential 凭证（Cookie或API密钥）
     * @return 发布结果
     */
    PublishResult publish(Article article, String credential);

    /**
     * 检查凭证是否有效
     *
     * @param credential 凭证
     * @return 是否有效
     */
    default boolean validateCredential(String credential) {
        return credential != null && !credential.isEmpty();
    }

    /**
     * 测试连接
     *
     * @param credential 凭证
     * @return 测试结果
     */
    TestConnectionResult testConnection(String credential);

    /**
     * 连接测试结果
     */
    record TestConnectionResult(boolean isSuccess, String message) {
        public static TestConnectionResult ok() {
            return new TestConnectionResult(true, "连接成功");
        }

        public static TestConnectionResult fail(String message) {
            return new TestConnectionResult(false, message);
        }
    }
}
