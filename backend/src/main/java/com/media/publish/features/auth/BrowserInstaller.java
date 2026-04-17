package com.media.publish.features.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 应用启动时自动安装 Playwright 浏览器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BrowserInstaller implements CommandLineRunner {

    private final BrowserSessionService browserSessionService;

    @Override
    public void run(String... args) {
        log.info("开始自动安装 Playwright 浏览器...");
        String result = browserSessionService.installBrowser();
        log.info("Playwright 浏览器安装结果: {}", result);
    }
}
