package com.media.publish.features.auth;

import com.microsoft.playwright.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Base64;

/**
 * 浏览器会话
 */
@Data
@Slf4j
public class BrowserSession {
    private String sessionId;
    private String platform;
    private Playwright playwright;
    private Browser browser;
    private BrowserContext context;
    private Page page;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private String status;
    
    /**
     * 获取当前页面截图
     */
    public String captureScreenshot() {
        if (page == null) return null;
        try {
            byte[] bytes = page.screenshot(new Page.ScreenshotOptions()
                .setFullPage(false));
            return Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            log.error("截图失败: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 点击页面坐标
     */
    public void click(int x, int y) {
        if (page == null) return;
        try {
            page.mouse().click(x, y);
            log.info("[{}] 点击坐标 ({}, {})", sessionId, x, y);
        } catch (Exception e) {
            log.error("点击失败: {}", e.getMessage());
        }
    }
    
    /**
     * 输入文本
     */
    public void type(String text) {
        if (page == null) return;
        try {
            page.keyboard().type(text);
            log.info("[{}] 输入文本: {}", sessionId, text);
        } catch (Exception e) {
            log.error("输入失败: {}", e.getMessage());
        }
    }
    
    /**
     * 获取页面 URL
     */
    public String getCurrentUrl() {
        if (page == null) return null;
        return page.url();
    }
    
    /**
     * 关闭会话
     */
    public void close() {
        try {
            if (page != null) page.close();
            if (context != null) context.close();
            if (browser != null) browser.close();
            if (playwright != null) playwright.close();
        } catch (Exception e) {
            log.error("关闭会话失败: {}", e.getMessage());
        }
    }
}
