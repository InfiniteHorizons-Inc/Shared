package com.infinitehorizons.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {

    private final Properties properties;

    public ConfigLoader() {
        properties = new Properties();
        loadProperties();
    }

    private void loadProperties() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                System.out.println("Sorry, unable to find application.properties");
                return;
            }
            properties.load(input);
        } catch (IOException ex) {
            ex.fillInStackTrace();
        }
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public String getAppName() {
        return properties.getProperty("app.name");
    }

    public String getAppVersion() {
        return properties.getProperty("app.version");
    }

    public String getJwtSecretKey() {
        return properties.getProperty("security.jwt.secret-key");
    }

    public long getJwtExpiration() {
        return Long.parseLong(properties.getProperty("security.jwt.expiration"));
    }

    public String gitHubUrl() {
        return properties.getProperty("github.url");
    }

    public String getOperationWrapperPrefix() {
        return properties.getProperty("operation.wrapper.prefix");
    }

    public String getOperationBotPrefix() {
        return properties.getProperty("operation.bot.prefix");
    }

}
