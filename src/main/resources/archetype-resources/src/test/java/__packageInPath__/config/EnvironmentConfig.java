package ${package}.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class EnvironmentConfig {

    private static final Properties PROPERTIES = load();

    private EnvironmentConfig() {
    }

    private static Properties load() {
        String environment = System.getProperty("env", "local");
        String resource = "/environments/" + environment + ".properties";

        Properties properties = new Properties();

        try (InputStream input = EnvironmentConfig.class.getResourceAsStream(resource)) {
            if (input == null) {
                throw new IllegalStateException("Environment configuration not found: " + resource);
            }
            properties.load(input);
            return properties;
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot load " + resource, exception);
        }
    }

    public static String get(String key, String defaultValue) {
        String systemValue = System.getProperty(key);
        if (systemValue != null && !systemValue.isBlank()) {
            return systemValue;
        }

        String environmentValue = System.getenv(toEnvironmentVariable(key));
        if (environmentValue != null && !environmentValue.isBlank()) {
            return environmentValue;
        }

        return PROPERTIES.getProperty(key, defaultValue);
    }

    public static String baseUrl() {
        return get("baseUrl", "http://localhost:8080").replaceAll("/+$", "");
    }

    public static String browser() {
        return get("browser", "chromium");
    }

    public static boolean headless() {
        return Boolean.parseBoolean(get("headless", "true"));
    }

    public static int defaultTimeoutMs() {
        return Integer.parseInt(get("defaultTimeoutMs", "10000"));
    }

    private static String toEnvironmentVariable(String key) {
        return "E2E_" + key.replaceAll("([a-z])([A-Z])", "$1_$2")
                .replace('-', '_')
                .toUpperCase();
    }
}
