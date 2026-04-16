package com.media.publish.features.publish.strategy;

import com.media.publish.features.article.entity.Article;
import com.microsoft.playwright.*;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 抽象浏览器发布器
 * 提供浏览器自动化的通用能力
 */
@Slf4j
public abstract class AbstractBrowserPublisher implements PublishStrategy {

    /**
     * 浏览器状态存储目录
     */
    protected static final String STATE_DIR = "/home/coze/browser-state";

    /**
     * 登录等待超时时间（毫秒）
     */
    protected static final long LOGIN_TIMEOUT_MS = 120_000;

    /**
     * 页面加载超时时间（毫秒）
     */
    protected static final long PAGE_LOAD_TIMEOUT_MS = 30_000;

    /**
     * 操作延迟（毫秒），模拟真人操作
     */
    protected static final int SLOW_MO_MS = 50;

    static {
        // 确保状态目录存在
        try {
            Files.createDirectories(Paths.get(STATE_DIR));
        } catch (Exception e) {
            log.warn("创建浏览器状态目录失败: {}", e.getMessage());
        }
    }

    /**
     * 获取状态存储路径
     */
    protected Path getStatePath() {
        return Paths.get(STATE_DIR, getPlatformType().getCode().toLowerCase() + ".json");
    }

    /**
     * 创建浏览器实例
     */
    protected Browser createBrowser() {
        Playwright playwright = Playwright.create();
        return playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(false)
                .setSlowMo(SLOW_MO_MS));
    }

    /**
     * 创建浏览器上下文（加载持久化状态）
     */
    protected BrowserContext createContext(Browser browser) {
        Path statePath = getStatePath();

        try {
            if (Files.exists(statePath) && Files.size(statePath) > 0) {
                log.info("[{}] 加载已保存的登录状态", getPlatformType().getName());
                return browser.newContext(new Browser.NewContextOptions()
                        .setStorageStatePath(statePath));
            }
        } catch (Exception e) {
            log.warn("[{}] 加载登录状态失败，将创建新上下文: {}", getPlatformType().getName(), e.getMessage());
        }

        return browser.newContext();
    }

    /**
     * 保存登录状态
     */
    protected void saveState(BrowserContext context) {
        try {
            Path statePath = getStatePath();
            context.storageState(new BrowserContext.StorageStateOptions()
                    .setPath(statePath));
            log.info("[{}] 登录状态已保存", getPlatformType().getName());
        } catch (Exception e) {
            log.error("[{}] 保存登录状态失败: {}", getPlatformType().getName(), e.getMessage());
        }
    }

    /**
     * 检查是否需要登录
     */
    protected abstract boolean needLogin(Page page);

    /**
     * 等待用户登录
     */
    protected void waitForLogin(Page page) {
        log.info("[{}] 请在浏览器窗口中完成登录...", getPlatformType().getName());

        try {
            // 等待登录成功的标志
            page.waitForCondition(
                    () -> !needLogin(page),
                    new Page.WaitForConditionOptions().setTimeout(LOGIN_TIMEOUT_MS)
            );
            log.info("[{}] 登录成功", getPlatformType().getName());
        } catch (PlaywrightException e) {
            throw new RuntimeException("登录超时，请重试");
        }
    }

    /**
     * 填写文章内容
     */
    protected abstract void fillArticle(Page page, Article article);

    /**
     * 点击发布按钮
     */
    protected abstract void clickPublish(Page page);

    /**
     * 获取发布后的文章URL
     */
    protected abstract String getArticleUrl(Page page);

    @Override
    public PublishResult publish(Article article, String credential) {
        Playwright playwright = null;
        Browser browser = null;
        BrowserContext context = null;

        try {
            playwright = Playwright.create();
            browser = createBrowser();
            context = createContext(browser);
            Page page = context.newPage();

            // 设置超时
            page.setDefaultTimeout(PAGE_LOAD_TIMEOUT_MS);

            // 访问编辑页面
            log.info("[{}] 正在打开编辑页面...", getPlatformType().getName());
            page.navigate(getPlatformType().getEditorUrl());

            // 检查是否需要登录
            if (needLogin(page)) {
                log.info("[{}] 需要登录", getPlatformType().getName());
                waitForLogin(page);
                saveState(context);
            }

            // 填写文章内容
            log.info("[{}] 正在填写文章内容...", getPlatformType().getName());
            fillArticle(page, article);

            // 点击发布
            log.info("[{}] 正在发布文章...", getPlatformType().getName());
            clickPublish(page);

            // 获取文章URL
            String articleUrl = getArticleUrl(page);
            log.info("[{}] 发布成功: {}", getPlatformType().getName(), articleUrl);

            // 保存状态
            saveState(context);

            return PublishResult.success(getPlatformType().getCode(), articleUrl);

        } catch (Exception e) {
            log.error("[{}] 发布失败: {}", getPlatformType().getName(), e.getMessage(), e);
            return PublishResult.failed(getPlatformType().getCode(), e.getMessage());
        } finally {
            // 清理资源
            if (context != null) {
                try {
                    saveState(context);
                } catch (Exception ignored) {
                }
                context.close();
            }
            if (browser != null) {
                browser.close();
            }
            if (playwright != null) {
                playwright.close();
            }
        }
    }

    @Override
    public TestConnectionResult testConnection(String credential) {
        Playwright playwright = null;
        Browser browser = null;

        try {
            playwright = Playwright.create();
            browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                    .setHeadless(false));

            BrowserContext context = createContext(browser);
            Page page = context.newPage();

            page.navigate(getPlatformType().getEditorUrl());

            if (needLogin(page)) {
                return TestConnectionResult.fail("未登录或凭证已过期");
            }

            return TestConnectionResult.ok();

        } catch (Exception e) {
            return TestConnectionResult.fail("测试连接失败: " + e.getMessage());
        } finally {
            if (browser != null) {
                browser.close();
            }
            if (playwright != null) {
                playwright.close();
            }
        }
    }
}
