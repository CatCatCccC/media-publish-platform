package com.media.publish.features.auth;

import com.media.publish.infrastructure.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 浏览器会话控制器
 * 提供远程浏览器操作接口
 */
@RestController
@RequestMapping("/api/browser")
@RequiredArgsConstructor
public class BrowserController {

    private final BrowserSessionService browserSessionService;

    /**
     * 创建浏览器会话
     */
    @PostMapping("/session/{platform}")
    public ApiResponse<Map<String, Object>> createSession(@PathVariable String platform) {
        return ApiResponse.success(browserSessionService.createSession(platform));
    }

    /**
     * 获取会话状态
     */
    @GetMapping("/session/{sessionId}")
    public ApiResponse<Map<String, Object>> getStatus(@PathVariable String sessionId) {
        return ApiResponse.success(browserSessionService.getSessionStatus(sessionId));
    }

    /**
     * 获取截图
     */
    @GetMapping("/session/{sessionId}/screenshot")
    public ApiResponse<Map<String, Object>> getScreenshot(@PathVariable String sessionId) {
        return ApiResponse.success(browserSessionService.getScreenshot(sessionId));
    }

    /**
     * 点击页面
     */
    @PostMapping("/session/{sessionId}/click")
    public ApiResponse<Map<String, Object>> click(
            @PathVariable String sessionId,
            @RequestBody Map<String, Integer> coords) {
        int x = coords.getOrDefault("x", 0);
        int y = coords.getOrDefault("y", 0);
        return ApiResponse.success(browserSessionService.click(sessionId, x, y));
    }

    /**
     * 输入文本
     */
    @PostMapping("/session/{sessionId}/type")
    public ApiResponse<Map<String, Object>> type(
            @PathVariable String sessionId,
            @RequestBody Map<String, String> body) {
        String text = body.getOrDefault("text", "");
        return ApiResponse.success(browserSessionService.type(sessionId, text));
    }

    /**
     * 导航到 URL
     */
    @PostMapping("/session/{sessionId}/navigate")
    public ApiResponse<Map<String, Object>> navigate(
            @PathVariable String sessionId,
            @RequestBody Map<String, String> body) {
        String url = body.getOrDefault("url", "");
        return ApiResponse.success(browserSessionService.navigate(sessionId, url));
    }

    /**
     * 提取 Cookie
     */
    @PostMapping("/session/{sessionId}/cookies")
    public ApiResponse<Map<String, Object>> extractCookies(@PathVariable String sessionId) {
        return ApiResponse.success(browserSessionService.extractCookies(sessionId));
    }

    /**
     * 关闭会话
     */
    @DeleteMapping("/session/{sessionId}")
    public ApiResponse<Void> closeSession(@PathVariable String sessionId) {
        browserSessionService.closeSession(sessionId);
        return ApiResponse.success();
    }
}
