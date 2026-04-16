package com.media.publish.features.publish.strategy.impl;

import com.media.publish.features.article.entity.Article;
import com.media.publish.features.publish.constant.PlatformType;
import com.media.publish.features.publish.strategy.AbstractBrowserPublisher;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * CSDN 发布策略
 */
@Slf4j
@Component
public class CsdnPublishStrategy extends AbstractBrowserPublisher {

    private static final String EDITOR_URL = "https://editor.csdn.net/";
    private static final String TITLE_SELECTOR = "#title";
    private static final String CONTENT_SELECTOR = "#markdown_editor_editor";
    private static final String PUBLISH_BUTTON = "button:has-text('发布')";
    private static final String ARTICLE_URL_PATTERN = "article/details/";

    @Override
    public PlatformType getPlatformType() {
        return PlatformType.CSDN;
    }

    @Override
    protected boolean needLogin(Page page) {
        String url = page.url();
        return url.contains("passport") || 
               url.contains("login") ||
               page.locator("text=登录").isVisible();
    }

    @Override
    protected void fillArticle(Page page, Article article) {
        try {
            // 等待编辑器加载
            page.waitForSelector(TITLE_SELECTOR);

            // 填写标题
            page.fill(TITLE_SELECTOR, article.getTitle());

            // 填写内容（Markdown编辑器）
            page.fill(CONTENT_SELECTOR, article.getContent());

            log.info("[CSDN] 文章内容填写完成: {}", article.getTitle());
        } catch (PlaywrightException e) {
            log.error("[CSDN] 填写文章内容失败: {}", e.getMessage());
            throw new RuntimeException("填写文章内容失败: " + e.getMessage());
        }
    }

    @Override
    protected void clickPublish(Page page) {
        try {
            // 点击发布按钮
            page.click(PUBLISH_BUTTON);

            // 等待发布完成
            page.waitForURL(url -> url.toString().contains(ARTICLE_URL_PATTERN),
                    new Page.WaitForURLOptions().setTimeout(30_000));
        } catch (PlaywrightException e) {
            throw new RuntimeException("发布失败: " + e.getMessage());
        }
    }

    @Override
    protected String getArticleUrl(Page page) {
        return page.url();
    }
}
