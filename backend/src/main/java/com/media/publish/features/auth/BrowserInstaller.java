package com.media.publish.features.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 应用启动时检查 Playwright 浏览器是否就绪
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BrowserInstaller implements CommandLineRunner {

    private final BrowserSessionService browserSessionService;

    @Override
    public void run(String... args) {
        // 异步初始化，不阻塞应用启动
        new Thread(() -> {
            try {
                log.info("开始检查 Playwright 浏览器...");
                String result = browserSessionService.installBrowser();
                log.info("Playwright 浏览器状态: {}", result);
            } catch (Exception e) {
                log.warn("Playwright 浏览器初始化异常（不影响应用运行）: {}", e.getMessage());
            }
        }, "playwright-init").start();
    }
}
