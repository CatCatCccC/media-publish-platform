package com.media.publish.features.auth;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.imageio.ImageIO;

/**
 * 浏览器会话服务
 */
@Slf4j
@Service
public class BrowserSessionService {

    private final Map<String, BrowserSession> sessions = new ConcurrentHashMap<>();
    private Playwright playwright;
    private boolean browserInstalled = false;

    /**
     * 初始化 Playwright（会自动下载浏览器）
     */
    private synchronized void ensurePlaywrightInitialized() {
        if (playwright == null) {
            log.info("初始化 Playwright...");
            playwright = Playwright.create();
            browserInstalled = true;
            log.info("Playwright 初始化完成，浏览器已就绪");
        }
    }

    /**
     * 安装 Playwright 浏览器（通过创建 Playwright 实例自动安装）
     */
    public String installBrowser() {
        try {
            log.info("开始安装 Playwright 浏览器...");
            ensurePlaywrightInitialized();
            return "安装成功 - Playwright 浏览器已就绪";
        } catch (Exception e) {
            log.error("安装 Playwright 浏览器失败: {}", e.getMessage(), e);
            return "安装失败: " + e.getMessage();
        }
    }
    
    /**
     * 创建浏览器会话
     */
    public Map<String, Object> createSession(String platform) {
        try {
            ensurePlaywrightInitialized();
            
            String sessionId = UUID.randomUUID().toString().substring(0, 8);
            
            Browser browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions()
                    .setHeadless(true)
            );
            
            var context = browser.newContext();
            var page = context.newPage();
            
            sessions.put(sessionId, new BrowserSession(browser, context, page, platform));
            
            log.info("创建浏览器会话: {} for platform: {}", sessionId, platform);
            return Map.of(
                "success", true,
                "sessionId", sessionId,
                "platform", platform
            );
        } catch (Exception e) {
            log.error("创建浏览器会话失败: {}", e.getMessage(), e);
            return Map.of("success", false, "error", e.getMessage());
        }
    }
    
    /**
     * 获取会话状态
     */
    public Map<String, Object> getSessionStatus(String sessionId) {
        BrowserSession session = sessions.get(sessionId);
        if (session == null) {
            return Map.of("success", false, "error", "会话不存在");
        }
        return Map.of(
            "success", true,
            "sessionId", sessionId,
            "platform", session.platform,
            "url", session.page.url()
        );
    }
    
    /**
     * 获取截图
     */
    public Map<String, Object> getScreenshot(String sessionId) {
        BrowserSession session = sessions.get(sessionId);
        if (session == null) {
            return Map.of("success", false, "error", "会话不存在");
        }
        try {
            byte[] screenshot = session.page.screenshot();
            String base64 = Base64.getEncoder().encodeToString(screenshot);
            return Map.of("success", true, "image", "data:image/png;base64," + base64);
        } catch (Exception e) {
            return Map.of("success", false, "error", e.getMessage());
        }
    }
    
    /**
     * 点击页面
     */
    public Map<String, Object> click(String sessionId, int x, int y) {
        BrowserSession session = sessions.get(sessionId);
        if (session == null) {
            return Map.of("success", false, "error", "会话不存在");
        }
        try {
            session.page.mouse().click(x, y);
            return Map.of("success", true);
        } catch (Exception e) {
            return Map.of("success", false, "error", e.getMessage());
        }
    }
    
    /**
     * 输入文本
     */
    public Map<String, Object> type(String sessionId, String text) {
        BrowserSession session = sessions.get(sessionId);
        if (session == null) {
            return Map.of("success", false, "error", "会话不存在");
        }
        try {
            session.page.keyboard().type(text);
            return Map.of("success", true);
        } catch (Exception e) {
            return Map.of("success", false, "error", e.getMessage());
        }
    }
    
    /**
     * 导航到 URL
     */
    public Map<String, Object> navigate(String sessionId, String url) {
        BrowserSession session = sessions.get(sessionId);
        if (session == null) {
            return Map.of("success", false, "error", "会话不存在");
        }
        try {
            session.page.navigate(url);
            return Map.of("success", true, "url", url);
        } catch (Exception e) {
            return Map.of("success", false, "error", e.getMessage());
        }
    }
    
    /**
     * 提取 Cookie
     */
    public Map<String, Object> extractCookies(String sessionId) {
        BrowserSession session = sessions.get(sessionId);
        if (session == null) {
            return Map.of("success", false, "error", "会话不存在");
        }
        try {
            var cookies = session.context.cookies();
            return Map.of("success", true, "cookies", cookies);
        } catch (Exception e) {
            return Map.of("success", false, "error", e.getMessage());
        }
    }
    
    /**
     * 关闭会话
     */
    public void closeSession(String sessionId) {
        BrowserSession session = sessions.remove(sessionId);
        if (session != null) {
            session.browser.close();
            log.info("关闭浏览器会话: {}", sessionId);
        }
    }
    
    /**
     * 浏览器会话内部类
     */
    private static class BrowserSession {
        final Browser browser;
        final com.microsoft.playwright.BrowserContext context;
        final com.microsoft.playwright.Page page;
        final String platform;
        
        BrowserSession(Browser browser, com.microsoft.playwright.BrowserContext context, 
                       com.microsoft.playwright.Page page, String platform) {
            this.browser = browser;
            this.context = context;
            this.page = page;
            this.platform = platform;
        }
    }
}
