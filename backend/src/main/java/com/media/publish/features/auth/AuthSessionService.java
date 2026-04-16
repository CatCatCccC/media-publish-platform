package com.media.publish.features.auth;

import com.microsoft.playwright.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 认证会话服务
 * 管理远程浏览器会话，用于获取平台凭证
 */
@Slf4j
@Service
public class AuthSessionService {

    private final Map<String, AuthSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, Browser> browsers = new ConcurrentHashMap<>();
    private final Map<String, Playwright> playwrights = new ConcurrentHashMap<>();
    
    private static final int BASE_PORT = 6080;
    private static final int SESSION_TIMEOUT_SECONDS = 300;
    
    /**
     * 创建认证会话
     * @param platform 平台类型 (csdn/zhihu/wechat)
     * @return 会话信息
     */
    public Map<String, Object> createSession(String platform) {
        String sessionId = UUID.randomUUID().toString().substring(0, 8);
        int websockifyPort = BASE_PORT + sessions.size();
        
        AuthSession session = new AuthSession();
        session.setSessionId(sessionId);
        session.setPlatform(platform);
        session.setWebsockifyPort(websockifyPort);
        session.setStatus("STARTING");
        session.setCreatedAt(java.time.LocalDateTime.now());
        session.setExpiresAt(java.time.LocalDateTime.now().plusSeconds(SESSION_TIMEOUT_SECONDS));
        
        sessions.put(sessionId, session);
        
        // 异步启动浏览器环境
        new Thread(() -> {
            try {
                startBrowserWithVnc(session);
            } catch (Exception e) {
                log.error("启动浏览器失败", e);
                session.setStatus("FAILED");
            }
        }, "browser-starter-" + sessionId).start();
        
        Map<String, Object> result = new HashMap<>();
        result.put("sessionId", sessionId);
        result.put("platform", platform);
        result.put("status", "STARTING");
        result.put("websockifyPort", websockifyPort);
        result.put("loginUrl", getLoginUrl(platform));
        result.put("expiresIn", SESSION_TIMEOUT_SECONDS);
        
        return result;
    }
    
    /**
     * 启动浏览器和 VNC 服务
     */
    private void startBrowserWithVnc(AuthSession session) throws Exception {
        String sessionId = session.getSessionId();
        String platform = session.getPlatform();
        int displayNum = 99 + sessions.size() - 1;
        int vncPort = 5900 + displayNum;
        int wsPort = session.getWebsockifyPort();
        
        log.info("[{}] 启动浏览器环境, display=:{}, vncPort={}, wsPort={}", 
            sessionId, displayNum, vncPort, wsPort);
        
        // 1. 启动 Xvfb
        Process xvfb = Runtime.getRuntime().exec(new String[]{
            "Xvfb", ":" + displayNum, 
            "-screen", "0", "1280x800x24", "-ac"
        });
        Thread.sleep(1500);
        
        // 2. 启动 x11vnc
        Process vnc = Runtime.getRuntime().exec(new String[]{
            "x11vnc", "-display", ":" + displayNum,
            "-rfbport", String.valueOf(vncPort),
            "-forever", "-shared", "-nopw", "-bg"
        });
        Thread.sleep(1000);
        
        // 3. 启动 websockify
        Process websockify = Runtime.getRuntime().exec(new String[]{
            "websockify", String.valueOf(wsPort), "localhost:" + vncPort
        });
        Thread.sleep(1000);
        
        // 4. 使用 Playwright 启动浏览器
        Playwright playwright = Playwright.create();
        playwrights.put(sessionId, playwright);
        
        Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
            .setHeadless(false)
            .setArgs(Arrays.asList(
                "--display=:" + displayNum,
                "--no-sandbox",
                "--disable-setuid-sandbox"
            )));
        browsers.put(sessionId, browser);
        
        // 5. 打开登录页面
        BrowserContext context = browser.newContext();
        Page page = context.newPage();
        page.navigate(getLoginUrl(platform));
        
        session.setStatus("READY");
        log.info("[{}] 会话就绪，连接地址: ws://localhost:{}", sessionId, wsPort);
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
        AuthSession session = sessions.get(sessionId);
        if (session == null) {
            return Map.of("error", "会话不存在");
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("sessionId", session.getSessionId());
        result.put("platform", session.getPlatform());
        result.put("status", session.getStatus());
        result.put("websockifyPort", session.getWebsockifyPort());
        
        return result;
    }
    
    /**
     * 提取 Cookie
     */
    public Map<String, Object> extractCookies(String sessionId) {
        AuthSession session = sessions.get(sessionId);
        Browser browser = browsers.get(sessionId);
        
        if (session == null || browser == null) {
            return Map.of("success", false, "error", "会话不存在或已过期");
        }
        
        try {
            // 获取所有上下文的 Cookie
            List<Map<String, Object>> allCookies = new ArrayList<>();
            
            for (BrowserContext context : browser.contexts()) {
                for (Cookie cookie : context.cookies()) {
                    Map<String, Object> cookieMap = new HashMap<>();
                    cookieMap.put("name", cookie.name);
                    cookieMap.put("value", cookie.value);
                    cookieMap.put("domain", cookie.domain);
                    cookieMap.put("path", cookie.path);
                    allCookies.add(cookieMap);
                }
            }
            
            // 格式化为 Cookie 字符串
            String cookieString = allCookies.stream()
                .map(c -> c.get("name") + "=" + c.get("value"))
                .reduce((a, b) -> a + "; " + b)
                .orElse("");
            
            session.setExtractedCookies(cookieString);
            session.setStatus("SUCCESS");
            
            return Map.of(
                "success", true,
                "cookies", allCookies,
                "cookieString", cookieString
            );
            
        } catch (Exception e) {
            log.error("提取 Cookie 失败", e);
            return Map.of("success", false, "error", e.getMessage());
        }
    }
    
    /**
     * 关闭会话
     */
    public void closeSession(String sessionId) {
        AuthSession session = sessions.remove(sessionId);
        Browser browser = browsers.remove(sessionId);
        Playwright playwright = playwrights.remove(sessionId);
        
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
        
        // 清理端口进程
        if (session != null) {
            try {
                Runtime.getRuntime().exec("pkill -f 'websockify.*" + session.getWebsockifyPort() + "'");
            } catch (Exception ignored) {}
        }
        
        log.info("[{}] 会话已关闭", sessionId);
    }
    
    /**
     * 清理过期会话
     */
    public void cleanupExpiredSessions() {
        sessions.keySet().forEach(sid -> {
            AuthSession s = sessions.get(sid);
            if (s != null && s.getExpiresAt().isBefore(java.time.LocalDateTime.now())) {
                closeSession(sid);
            }
        });
    }
}
