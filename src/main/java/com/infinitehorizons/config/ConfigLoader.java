package com.infinitehorizons.config;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Handles dynamic creation, loading, and management of configuration properties.
 */
public class ConfigLoader {

    private final Properties properties = new Properties();
    private final Path propertiesFilePath;

    /**
     * Constructs a new {@code ConfigLoader} for the specified properties file.
     *
     * @param fileName the name of the properties file to manage.
     */
    public ConfigLoader(String fileName) {
        // Ensure the config directory exists
        Path configDirectory = Paths.get("config");
        if (!Files.exists(configDirectory)) {
            try {
                Files.createDirectories(configDirectory);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Create the full path for the properties file
        this.propertiesFilePath = configDirectory.resolve(fileName);
        loadProperties();
    }

    /**
     * Loads the properties from the specified file.
     * If the file does not exist, a new file is created.
     */
    private void loadProperties() {
        if (!Files.exists(propertiesFilePath)) {
            createNewPropertiesFile();
        }
        try (InputStream input = new FileInputStream(propertiesFilePath.toFile())) {
            properties.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Creates a new properties file, overwriting if it already exists.
     */
    public void createNewPropertiesFile() {
        try (OutputStream output = new FileOutputStream(propertiesFilePath.toFile())) {
            properties.store(output, "Nuevo Archivo de Propiedades");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Saves the current properties to the file, overwriting existing content.
     */
    public void saveProperties() {
        try (OutputStream output = new FileOutputStream(propertiesFilePath.toFile())) {
            properties.store(output, "Archivo de Propiedades Actualizado");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Deletes the properties file.
     */
    public void deletePropertiesFile() {
        try {
            Files.deleteIfExists(propertiesFilePath);
        } catch (IOException ex) {
            ex.printStackTrace();
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
     * Returns the value of the specified property key, or a default value if the key is not found.
     *
     * @param key          the property key.
     * @param defaultValue the default value to return if the key is not found.
     * @return the property value, or the default value if the key is not found.
     */
    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

}
