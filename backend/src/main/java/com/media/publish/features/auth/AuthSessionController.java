package com.media.publish.features.auth;

import com.media.publish.infrastructure.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证会话控制器
 * 用于获取平台凭证
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthSessionController {

    private final AuthSessionService authSessionService;

    /**
     * 创建认证会话
     * 启动远程浏览器，用户可在浏览器中登录
     * 
     * @param platform 平台类型 (csdn/zhihu/wechat)
     * @return 会话信息，包含 websockify 端口
     */
    @PostMapping("/session/{platform}")
    public ApiResponse<Map<String, Object>> createSession(@PathVariable String platform) {
        Map<String, Object> session = authSessionService.createSession(platform);
        return ApiResponse.success(session);
    }

    /**
     * 获取会话状态
     */
    @GetMapping("/session/{sessionId}")
    public ApiResponse<Map<String, Object>> getSessionStatus(@PathVariable String sessionId) {
        Map<String, Object> status = authSessionService.getSessionStatus(sessionId);
        return ApiResponse.success(status);
    }

    /**
     * 提取 Cookie
     * 用户登录成功后调用此接口获取凭证
     */
    @PostMapping("/session/{sessionId}/extract")
    public ApiResponse<Map<String, Object>> extractCookies(@PathVariable String sessionId) {
        Map<String, Object> result = authSessionService.extractCookies(sessionId);
        return ApiResponse.success(result);
    }

    /**
     * 关闭会话
     */
    @DeleteMapping("/session/{sessionId}")
    public ApiResponse<Void> closeSession(@PathVariable String sessionId) {
        authSessionService.closeSession(sessionId);
        return ApiResponse.success();
    }
}
