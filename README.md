# Playwright Java Regression Test Archetype

A standalone Maven project template for browser-based regression testing of Java web applications.
The generated project is independent of the application being tested: it communicates with the
application over HTTP and does not need dependencies on Spring, Hibernate, MyBatis, CXF, or the
application WAR.

## What this archetype generates

- Java 17 test runner
- Playwright Java browser automation
- JUnit 5
- Browser lifecycle shared per test class, with an isolated `BrowserContext` per test
- Page Object and business-scenario examples
- Environment-specific `.properties` files
- Failure screenshots and Playwright trace artifacts
- Maven Surefire test execution

## 1. Build and install the archetype

Requirements:

- JDK 17 or newer
- Maven 3.9+
- Internet access for Maven dependencies and Playwright browser installation

From the archetype project root:

```bash
mvn clean install
```

This installs the archetype into your local Maven repository.

## 2. Generate a regression test project

Run from the directory where you want the new project created:

```bash
mvn archetype:generate \
  -DarchetypeGroupId=com.example.testing \
  -DarchetypeArtifactId=playwright-java-regression-archetype \
  -DarchetypeVersion=1.0.0 \
  -DgroupId=com.company.testing \
  -DartifactId=my-application-e2e \
  -Dversion=1.0-SNAPSHOT \
  -Dpackage=com.company.e2e \
  -DinteractiveMode=false
```

The generated project is a standalone Maven project. It is not added as a module to the
application's parent POM, so it can be built and executed independently.

## 3. Install Playwright browsers

Change into the generated project:

```bash
cd my-application-e2e
mvn exec:java -Dexec.mainClass=com.microsoft.playwright.CLI \
  -Dexec.args="install chromium"
```

On Linux CI agents, install the browser's OS dependencies as well (requires suitable permissions):

```bash
mvn exec:java -Dexec.mainClass=com.microsoft.playwright.CLI \
  -Dexec.args="install --with-deps chromium"
```

The Playwright Java library version and browser binaries must stay aligned. Re-run the install
command after upgrading the Playwright dependency.

## 4. Configure the target environment

Environment configuration lives in:

```text
src/test/resources/environments/
  local.properties
  qa.properties
  staging.properties
```

Each file can define:

```properties
baseUrl=http://localhost:8080
browser=chromium
headless=true
defaultTimeoutMs=10000
```

Select an environment with `-Denv=...`. Maven system properties override values from the
environment file:

```bash
mvn test -Denv=qa
mvn test -Denv=staging -Dbrowser=firefox -Dheadless=false
```

You can also supply the URL directly:

```bash
mvn test -DbaseUrl=https://qa.example.internal
```

The default environment is `local`. The default URL is `http://localhost:8080`.

## 5. Put files in the right places

```text
src/test/java/<your-package>/
  config/       Environment and runtime configuration
  support/      Browser lifecycle, base test classes, shared test utilities
  pages/        One Page Object per screen or reusable UI component
  scenarios/    Business workflows composed from Page Objects
  tests/        JUnit test classes; test methods describe expected behavior

src/test/resources/
  environments/ Environment properties (do not put secrets here)
  testdata/     Non-sensitive fixtures, JSON, CSV, or other static test data

target/
  playwright-traces/ Playwright trace ZIP files
  screenshots/        Failure screenshots
  surefire-reports/   JUnit/Surefire reports
```

### Page Objects

Put screen-specific selectors and browser interactions in `pages/`. Prefer accessible locators
(role/name/label) and stable `data-testid` attributes. Keep business assertions and workflows out
of Page Objects where practical.

### Scenarios

Put reusable business journeys in `scenarios/`, for example:
- authenticate a user
- create and search for a customer
- submit and approve a request

Scenarios coordinate Page Objects. They should describe the business flow rather than the
underlying JSP, Thymeleaf, HTMX, or Vaadin implementation.

### Tests

Put JUnit test classes in `tests/`. A test should arrange data, invoke a scenario, and assert the
observable result. Use dedicated test accounts and deterministic test data.

The sample login tests assume the application exposes a `/login` page with accessible labels
`Username` and `Password`, a `Sign in` button, and a post-login `/dashboard` page with a
`Dashboard` heading. Adapt the Page Object and assertions to your application's actual UI.

The login-page test requires the target application to be running and reachable. The authenticated
login test is skipped when `E2E_USERNAME` or `E2E_PASSWORD` is not configured.

### Test data and credentials

Put non-sensitive, stable fixtures in `testdata/`. Do not commit passwords, tokens, production
personal data, or other secrets. Inject credentials through environment variables or your CI
secret store, for example:

```bash
export E2E_USERNAME=test-user
export E2E_PASSWORD='provided-by-secret-store'
mvn test -Denv=qa
```

## 6. Run and debug tests

The target application must be running and reachable at the configured `baseUrl`.

Run all tests headlessly:

```bash
mvn clean test -Dheadless=true
```

Run with a visible browser:

```bash
mvn test -Dheadless=false
```

Run a single test class:

```bash
mvn test -Dtest=LoginRegressionTest
```

Choose a browser:

```bash
mvn test -Dbrowser=chromium
mvn test -Dbrowser=firefox
mvn test -Dbrowser=webkit
```

Open a trace:

```bash
mvn exec:java -Dexec.mainClass=com.microsoft.playwright.CLI \
  -Dexec.args="show-trace target/playwright-traces/<trace-file>.zip"
```

Traces and screenshots can contain page content and sensitive information. Treat them as test
artifacts with the same access controls as application logs.

## 7. Running in Jenkins

Use a dedicated agent with JDK 17+, Maven, and Playwright browser dependencies installed.
A typical pipeline step is:

```groovy
sh '''
  mvn clean test \
    -Denv=qa \
    -DbaseUrl=${E2E_BASE_URL} \
    -Dbrowser=chromium \
    -Dheadless=true
'''
```

Publish `target/surefire-reports/*.xml` as JUnit results and archive
`target/playwright-traces/**` and `target/screenshots/**` on failure or always, according to
your retention policy. Inject credentials through Jenkins Credentials, not source files or
plain-text build parameters.

## 8. Using this during a UI migration

Keep business scenarios stable across the legacy and modern application. If the DOM and UI
interaction patterns differ substantially, create separate legacy and modern Page Object
implementations behind a small interface, then select the implementation using a system property
or separate test profile. Avoid sprinkling `if (legacy)` branches throughout individual Page
Objects.

The suite should validate externally observable behavior through the browser. It should not
depend on application implementation classes or direct database access unless a separately
managed test-fixture mechanism explicitly requires it.

## 9. Automated verification

The GitHub Actions workflow in `.github/workflows/verify.yml` builds and installs this archetype,
generates a sample project, and compiles its test sources. This validates archetype generation and
Java compilation without requiring a running application or test credentials.

## 10. Important operational notes

- The test runner's Java version is independent of the target application's Java version.
  A Java 17 test runner can test a Java 11 application over HTTP.
- Each test receives a fresh browser context, isolating cookies and local storage.
- The sample starts with sequential execution. Introduce parallelism only after test data and
  account isolation are designed for it.
- Avoid fixed sleeps. Prefer Playwright's auto-waiting and assertions.
- Do not point destructive tests at production.
