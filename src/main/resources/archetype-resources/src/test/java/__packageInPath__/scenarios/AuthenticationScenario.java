package ${package}.scenarios;

import ${package}.pages.LoginPage;
import com.microsoft.playwright.Page;

public class AuthenticationScenario {

    private final LoginPage loginPage;

    public AuthenticationScenario(Page page) {
        this.loginPage = new LoginPage(page);
    }

    public void authenticate(String baseUrl, String username, String password) {
        loginPage.navigate(baseUrl);
        loginPage.login(username, password);
    }
}
