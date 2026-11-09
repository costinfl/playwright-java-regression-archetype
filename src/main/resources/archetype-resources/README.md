# ${artifactId}

Standalone Playwright Java regression test suite generated from the Playwright Java Regression Test Archetype.

See the archetype's README for the full usage guide. Quick start:

```bash
mvn exec:java -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install chromium"
mvn clean test -Denv=local
```

## Source layout

- `${package}/config`: environment and runtime configuration
- `${package}/support`: browser lifecycle and shared test support
- `${package}/pages`: Page Objects
- `${package}/scenarios`: reusable business workflows
- `${package}/tests`: JUnit regression tests
- `src/test/resources/environments`: target environment configuration
- `src/test/resources/testdata`: non-sensitive test fixtures

Configure the target URL with `-DbaseUrl=...` or in the selected environment properties file.
Do not commit credentials; inject them through environment variables or your CI secret store.
