package ${package}.tests;

import ${package}.config.EnvironmentConfig;
import ${package}.scenarios.AuthenticationScenario;
import ${package}.support.BaseUiTest;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import org.junit.jupiter.api.Test;

class LoginRegressionTest extends BaseUiTest {

    @Test
    void loginPageShouldBeDisplayed() {
        page.navigate(applicationUrl("/login"));

        PlaywrightAssertions.assertThat(page.getByLabel("Username")).isVisible();
        PlaywrightAssertions.assertThat(page.getByLabel("Password")).isVisible();
    }

    @Test
    void successfulLoginShouldOpenDashboard() {
        String username = System.getenv("E2E_USERNAME");
        String password = System.getenv("E2E_PASSWORD");

        if (username == null || password == null) {
            throw new IllegalStateException(
                    "Set E2E_USERNAME and E2E_PASSWORD before running this test.");
        }

        AuthenticationScenario scenario = new AuthenticationScenario(page);
        scenario.authenticate(EnvironmentConfig.baseUrl(), username, password);

        page.waitForURL("**/dashboard");
        PlaywrightAssertions.assertThat(page.getByRole(
                com.microsoft.playwright.options.AriaRole.HEADING,
                new com.microsoft.playwright.Page.GetByRoleOptions().setName("Dashboard")))
                .isVisible();
    }
}
