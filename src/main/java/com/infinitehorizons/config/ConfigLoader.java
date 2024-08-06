package com.infinitehorizons.config;

import com.infinitehorizons.Shared;
import com.infinitehorizons.events.EventListener;
import com.infinitehorizons.events.ThrowableEvent;
import com.infinitehorizons.exceptions.CommandNotRegisteredException;
import com.infinitehorizons.queue.SmartQueue;
import com.infinitehorizons.utils.Logger;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.session.ReadyEvent;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

/**
 * Configuration manager for {@code Shared}, handling both Shared settings and dynamic property loading.
 */
@Data
@NoArgsConstructor(force = true)
public class ConfigLoader {

    private static final Path CONFIG_DIRECTORY = Paths.get("config");
    private final Properties properties = new Properties();
    private final Path propertiesFilePath;

    /**
     * The {@link JDA} instance that {@link Shared} uses.
     */
    private JDA jda;

    /**
     * The packages where the commands are located in.
     * <b>Default:</b> {@code new String[]{}}
     */
    private String[] commandsPackages = new String[]{};

    /**
     * Determines if commands are registered on the {@link ReadyEvent}.
     * <b>Default:</b> {@code true}
     */
    private boolean registerOnReady = true;

    /**
     * Determines if {@link SmartQueue} is enabled for global commands.
     * <b>Default:</b> {@code true}
     */
    private boolean globalSmartQueue = true;

    /**
     * Determines if {@link SmartQueue} is enabled for guild commands.
     * <b>Default:</b> {@code true}
     */
    private boolean guildSmartQueue = true;

    /**
     * Determines if unknown commands should be deleted.
     * <b>Default:</b> {@code true}
     */
    private boolean deleteUnknownCommands = true;

    /**
     * Determines if a {@link CommandNotRegisteredException} should be thrown.
     * <b>Default:</b> {@code true}
     */
    private boolean throwUnregisteredException = true;

    /**
     * Determines if the stack trace should be printed on an {@link ThrowableEvent}
     * if no {@link EventListener} is registered.
     * <b>Default:</b> {@code true}
     */
    private boolean defaultPrintStacktrace = true;

    /**
     * The {@link Executor} used to handle command executions.
     * Recommended for Java 21: {@link Executors#newVirtualThreadPerTaskExecutor()}.
     * <b>Default:</b> {@link ForkJoinPool#commonPool()}
     */
    private Executor executor = ForkJoinPool.commonPool();

    /**
     * Creates a new ConfigLoader for the specified properties file.
     *
     * @param fileName the name of the properties file to manage.
     */
    public ConfigLoader(String fileName) {
        ensureConfigDirectoryExists();
        this.propertiesFilePath = CONFIG_DIRECTORY.resolve(fileName);
        loadProperties();
    }

    /**
     * Disables logging for the specified {@link Logger.Type} types.
     *
     * @param types The logger types to disable.
     */
    public void disableLogging(Logger.Type... types) {
        Logger.disableLogging(types);
    }

    /**
     * Ensures the configuration directory exists.
     */
    private void ensureConfigDirectoryExists() {
        if (!Files.exists(CONFIG_DIRECTORY)) {
            try {
                Files.createDirectories(CONFIG_DIRECTORY);
            } catch (IOException e) {
                Logger.error("Failed to create config directory: %s", e.getMessage());
            }
        }
    }

    /**
     * Loads the properties from the specified file.
     * If the file doesn't exist, a new file is created.
     */
    private void loadProperties() {
        if (!Files.exists(propertiesFilePath)) {
            createNewPropertiesFile();
        }
        try (InputStream input = Files.newInputStream(propertiesFilePath)) {
            properties.load(input);
        } catch (IOException ex) {
            Logger.error("Failed to load properties: %s", ex.getMessage());
        }
    }

    /**
     * Creates a new properties file, overwriting if it already exists.
     */
    public void createNewPropertiesFile() {
        try (OutputStream output = Files.newOutputStream(propertiesFilePath)) {
            properties.store(output, "New Properties File");
        } catch (IOException ex) {
            Logger.error("Failed to create new properties file: %s", ex.getMessage());
        }
    }

    /**
     * Saves the current properties to the file, overwriting existing content.
     */
    public void saveProperties() {
        try (OutputStream output = Files.newOutputStream(propertiesFilePath)) {
            properties.store(output, "Updated Properties File");
        } catch (IOException ex) {
            Logger.error("Failed to save properties: %s", ex.getMessage());
        }
    }

    /**
     * Deletes the properties file.
     */
    public void deletePropertiesFile() {
        try {
            Files.deleteIfExists(propertiesFilePath);
        } catch (IOException ex) {
            Logger.error("Failed to delete properties file: %s", ex.getMessage());
        }
    }

    /**
     * Reloads the properties from the file, discarding any unsaved changes.
     */
    public void reloadProperties() {
        loadProperties();
    }

    /**
     * Sets a property value in memory.
     *
     * @param key   the property key.
     * @param value the property value.
     */
    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }

    /**
     * Returns the value of the specified property key.
     *
     * @param key the property key.
     * @return the property value, or {@code null} if the key is not found.
     */
    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    /**
     * Returns the value of the specified property key or a default value if the key is not found.
     *
     * @param key          the property key.
     * @param defaultValue the default value to return if the key is not found.
     * @return the property value, or the default value if the key is not found.
     */
    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
}
