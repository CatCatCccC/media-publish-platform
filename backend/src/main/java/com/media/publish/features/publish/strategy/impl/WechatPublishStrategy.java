package com.media.publish.features.publish.strategy.impl;

import com.media.publish.features.article.entity.Article;
import com.media.publish.features.publish.constant.PlatformType;
import com.media.publish.features.publish.strategy.AbstractBrowserPublisher;
import com.media.publish.features.publish.strategy.PublishResult;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 微信公众号发布策略
 * 注意：微信公众号通常使用官方API，这里提供浏览器自动化作为备选方案
 */
@Slf4j
@Component
public class WechatPublishStrategy extends AbstractBrowserPublisher {

    private static final String EDITOR_URL = "https://mp.weixin.qq.com/";
    private static final String NEW_ARTICLE_BUTTON = "text=新的创作";
    private static final String TITLE_SELECTOR = "#title";
    private static final String CONTENT_SELECTOR = "#js_editor";
    private static final String PUBLISH_BUTTON = "text=保存并发布";

    @Override
    public PlatformType getPlatformType() {
        return PlatformType.WECHAT;
    }

    @Override
    protected boolean needLogin(Page page) {
        String url = page.url();
        return !url.contains("mp.weixin.qq.com") ||
               page.locator("text=登录").isVisible() ||
               page.locator(".weui-desktop-account").count() == 0;
    }

    @Override
    protected void fillArticle(Page page, Article article) {
        try {
            // 等待页面加载
            page.waitForLoadState();

            // 点击新建图文
            page.click(NEW_ARTICLE_BUTTON);

            // 等待编辑器
            page.waitForSelector(TITLE_SELECTOR);

            // 填写标题
            page.fill(TITLE_SELECTOR, article.getTitle());

            // 填写内容
            page.click(CONTENT_SELECTOR);
            page.keyboard().type(article.getContent());

            log.info("[微信公众号] 文章内容填写完成: {}", article.getTitle());
        } catch (PlaywrightException e) {
            log.error("[微信公众号] 填写文章内容失败: {}", e.getMessage());
            throw new RuntimeException("填写文章内容失败: " + e.getMessage());
        }
    }

    @Override
    protected void clickPublish(Page page) {
        try {
            page.click(PUBLISH_BUTTON);
            // 微信公众号需要确认发布
            page.click("text=确认发布");
        } catch (PlaywrightException e) {
            throw new RuntimeException("发布失败: " + e.getMessage());
        }
    }

    @Override
    protected String getArticleUrl(Page page) {
        // 微信公众号文章URL在发布后需要从页面获取
        return "https://mp.weixin.qq.com/s/pending";
    }

    /**
     * 微信公众号建议使用官方API
     * 这里重写publish方法，提供API方式支持
     */
    @Override
    public PublishResult publish(Article article, String credential) {
        // 如果凭证包含冒号，说明是 AppID:AppSecret 格式，使用官方API
        if (credential != null && credential.contains(":")) {
            return publishViaApi(article, credential);
        }
        // 否则使用浏览器自动化
        return super.publish(article, credential);
    }

    /**
     * 通过官方API发布
     */
    private PublishResult publishViaApi(Article article, String credential) {
        // TODO: 实现微信官方API发布
        log.warn("[微信公众号] 官方API发布暂未实现，请使用浏览器方式");
        return PublishResult.failed(PlatformType.WECHAT.getCode(), "官方API发布暂未实现");
    }
}
