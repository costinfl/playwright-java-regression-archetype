package ${package}.support;

import ${package}.config.EnvironmentConfig;
import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;

import java.nio.file.Files;
import java.nio.file.Path;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class BaseUiTest {

    protected Playwright playwright;
    protected Browser browser;
    protected BrowserContext context;
    protected Page page;

    @BeforeAll
    void startBrowser() {
        playwright = Playwright.create();

        BrowserType browserType = switch (EnvironmentConfig.browser().toLowerCase()) {
            case "firefox" -> playwright.firefox();
            case "webkit" -> playwright.webkit();
            case "chromium" -> playwright.chromium();
            default -> throw new IllegalArgumentException(
                    "Unsupported browser: " + EnvironmentConfig.browser());
        };

        browser = browserType.launch(
                new BrowserType.LaunchOptions().setHeadless(EnvironmentConfig.headless()));
    }

    @BeforeEach
    void createIsolatedContext() {
        context = browser.newContext(
                new Browser.NewContextOptions().setViewportSize(1440, 900));

        context.setDefaultTimeout(EnvironmentConfig.defaultTimeoutMs());

        context.tracing().start(new Tracing.StartOptions()
                .setScreenshots(true)
                .setSnapshots(true)
                .setSources(true));

        page = context.newPage();
    }

    @AfterEach
    void captureArtifacts(TestInfo testInfo) throws Exception {
        if (context == null) {
            return;
        }

        String safeName = testInfo.getTestClass().map(Class::getSimpleName).orElse("test")
                + "-" + testInfo.getDisplayName().replaceAll("[^a-zA-Z0-9-_]", "_");

        Path traceDirectory = Path.of("target", "playwright-traces");
        Files.createDirectories(traceDirectory);

        try {
            context.tracing().stop(new Tracing.StopOptions()
                    .setPath(traceDirectory.resolve(safeName + ".zip")));
        } finally {
            context.close();
            context = null;
            page = null;
        }
    }

    @AfterAll
    void closeBrowser() {
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }

    protected String applicationUrl(String path) {
        String normalizedPath = path.startsWith("/") ? path : "/" + path;
        return EnvironmentConfig.baseUrl() + normalizedPath;
    }

    protected void captureFailureScreenshot(TestInfo testInfo) {
        if (page == null) {
            return;
        }

        try {
            Path directory = Path.of("target", "screenshots");
            Files.createDirectories(directory);
            String safeName = testInfo.getDisplayName().replaceAll("[^a-zA-Z0-9-_]", "_");
            page.screenshot(new Page.ScreenshotOptions()
                    .setPath(directory.resolve(safeName + ".png"))
                    .setFullPage(true));
        } catch (Exception ignored) {
            // Preserve the original test failure if screenshot capture itself fails.
        }
    }
}
