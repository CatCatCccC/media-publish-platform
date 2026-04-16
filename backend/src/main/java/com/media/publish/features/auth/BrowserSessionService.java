package com.media.publish.features.auth;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.Cookie;
import com.microsoft.playwright.options.LoadState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.time.LocalDateTime;

/**
 * 浏览器会话服务
 * 提供远程浏览器操作能力
 */
@Slf4j
@Service
public class BrowserSessionService {

    private final Map<String, BrowserSession> sessions = new ConcurrentHashMap<>();
    private static final int SESSION_TIMEOUT_SECONDS = 300;
    
    /**
     * 创建浏览器会话
     */
    public Map<String, Object> createSession(String platform) {
        String sessionId = UUID.randomUUID().toString().substring(0, 8);
        
        BrowserSession session = new BrowserSession();
        session.setSessionId(sessionId);
        session.setPlatform(platform);
        session.setCreatedAt(LocalDateTime.now());
        session.setExpiresAt(LocalDateTime.now().plusSeconds(SESSION_TIMEOUT_SECONDS));
        session.setStatus("CREATING");
        
        sessions.put(sessionId, session);
        
        // 异步启动浏览器
        new Thread(() -> {
            try {
                startBrowser(session);
            } catch (Exception e) {
                log.error("[{}] 启动浏览器失败: {}", sessionId, e.getMessage());
                session.setStatus("FAILED");
            }
        }, "browser-" + sessionId).start();
        
        Map<String, Object> result = new HashMap<>();
        result.put("sessionId", sessionId);
        result.put("platform", platform);
        result.put("status", "CREATING");
        result.put("expiresIn", SESSION_TIMEOUT_SECONDS);
        
        return result;
    }
    
    /**
     * 启动浏览器
     */
    private void startBrowser(BrowserSession session) {
        String sessionId = session.getSessionId();
        log.info("[{}] 正在启动浏览器...", sessionId);
        
        Playwright playwright = Playwright.create();
        Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
            .setHeadless(true)
            .setArgs(Arrays.asList("--no-sandbox", "--disable-setuid-sandbox")));
        
        BrowserContext context = browser.newContext(new Browser.NewContextOptions()
            .setViewportSize(1280, 800)
            .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"));
        
        Page page = context.newPage();
        
        // 导航到登录页面
        String loginUrl = getLoginUrl(session.getPlatform());
        log.info("[{}] 导航到: {}", sessionId, loginUrl);
        page.navigate(loginUrl);
        page.waitForLoadState(LoadState.NETWORKIDLE);
        
        session.setPlaywright(playwright);
        session.setBrowser(browser);
        session.setContext(context);
        session.setPage(page);
        session.setStatus("READY");
        
        log.info("[{}] 浏览器已就绪", sessionId);
    }
    
    /**
     * 获取登录 URL
     */
    private String getLoginUrl(String platform) {
        return switch (platform.toLowerCase()) {
            case "csdn" -> "https://passport.csdn.net/login";
            case "zhihu" -> "https://www.zhihu.com/signin";
            case "wechat" -> "https://mp.weixin.qq.com";
            default -> "https://" + platform + ".com";
        };
    }
    
    /**
     * 获取会话状态
     */
    public Map<String, Object> getSessionStatus(String sessionId) {
        BrowserSession session = sessions.get(sessionId);
        if (session == null) {
            return Map.of("error", "会话不存在");
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("sessionId", session.getSessionId());
        result.put("platform", session.getPlatform());
        result.put("status", session.getStatus());
        result.put("url", session.getCurrentUrl());
        
        return result;
    }
    
    /**
     * 获取截图
     */
    public Map<String, Object> getScreenshot(String sessionId) {
        BrowserSession session = sessions.get(sessionId);
        if (session == null || session.getPage() == null) {
            return Map.of("success", false, "error", "会话不存在或未就绪");
        }
        
        String screenshot = session.captureScreenshot();
        if (screenshot == null) {
            return Map.of("success", false, "error", "截图失败");
        }
        
        return Map.of(
            "success", true,
            "screenshot", screenshot,
            "url", session.getCurrentUrl()
        );
    }
    
    /**
     * 点击页面
     */
    public Map<String, Object> click(String sessionId, int x, int y) {
        BrowserSession session = sessions.get(sessionId);
        if (session == null || session.getPage() == null) {
            return Map.of("success", false, "error", "会话不存在或未就绪");
        }
        
        try {
            session.click(x, y);
            Thread.sleep(500); // 等待页面响应
            return getScreenshot(sessionId);
        } catch (Exception e) {
            return Map.of("success", false, "error", e.getMessage());
        }
    }
    
    /**
     * 输入文本
     */
    public Map<String, Object> type(String sessionId, String text) {
        BrowserSession session = sessions.get(sessionId);
        if (session == null || session.getPage() == null) {
            return Map.of("success", false, "error", "会话不存在或未就绪");
        }
        
        try {
            session.type(text);
            Thread.sleep(200);
            return getScreenshot(sessionId);
        } catch (Exception e) {
            return Map.of("success", false, "error", e.getMessage());
        }
    }
    
    /**
     * 提取 Cookie
     */
    public Map<String, Object> extractCookies(String sessionId) {
        BrowserSession session = sessions.get(sessionId);
        if (session == null || session.getContext() == null) {
            return Map.of("success", false, "error", "会话不存在或已过期");
        }
        
        try {
            List<Cookie> cookies = session.getContext().cookies();
            
            List<Map<String, Object>> cookieList = new ArrayList<>();
            StringBuilder cookieString = new StringBuilder();
            
            for (Cookie cookie : cookies) {
                Map<String, Object> c = new HashMap<>();
                c.put("name", cookie.name);
                c.put("value", cookie.value);
                c.put("domain", cookie.domain);
                cookieList.add(c);
                
                if (cookieString.length() > 0) cookieString.append("; ");
                cookieString.append(cookie.name).append("=").append(cookie.value);
            }
            
            session.setStatus("SUCCESS");
            
            return Map.of(
                "success", true,
                "cookies", cookieList,
                "cookieString", cookieString.toString()
            );
            
        } catch (Exception e) {
            log.error("提取 Cookie 失败: {}", e.getMessage());
            return Map.of("success", false, "error", e.getMessage());
        }
    }
    
    /**
     * 导航
     */
    public Map<String, Object> navigate(String sessionId, String url) {
        BrowserSession session = sessions.get(sessionId);
        if (session == null || session.getPage() == null) {
            return Map.of("success", false, "error", "会话不存在或未就绪");
        }
        
        try {
            session.getPage().navigate(url);
            session.getPage().waitForLoadState(LoadState.NETWORKIDLE);
            return getScreenshot(sessionId);
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
            session.close();
            log.info("[{}] 会话已关闭", sessionId);
        }
    }
    
    /**
     * 清理过期会话
     */
    public void cleanupExpiredSessions() {
        sessions.keySet().removeIf(sid -> {
            BrowserSession s = sessions.get(sid);
            if (s != null && s.getExpiresAt().isBefore(LocalDateTime.now())) {
                s.close();
                return true;
            }
            return false;
        });
    }
}
