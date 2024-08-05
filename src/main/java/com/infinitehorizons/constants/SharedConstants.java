package com.infinitehorizons.constants;

import com.infinitehorizons.config.ConfigLoader;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.awt.*;

/**
 * Defines shared constants used across the application.
 * <p>
 * These constants are loaded from a properties file at runtime, allowing for easy configuration and modification.
 */
@Getter
@NoArgsConstructor
public final class SharedConstants {

    public static final Color CASCADE_COLOR;
    public static final String GITHUB_URL;
    public static final String WRAPPER_OP_PREFIX;
    public static final String BOT_OP_PREFIX;

    static {
        ConfigLoader configLoader = new ConfigLoader("application.properties");

        String colorProperty = configLoader.getProperty("cascade.color", "255,255,255");
        configLoader.setProperty("cascade.color", colorProperty);
        String[] rgb = colorProperty.split(",");
        CASCADE_COLOR = new Color(
                Integer.parseInt(rgb[0].trim()),
                Integer.parseInt(rgb[1].trim()),
                Integer.parseInt(rgb[2].trim())
        );

        GITHUB_URL = configLoader.getProperty("github.url", "https://github.com/yourrepo");
        configLoader.setProperty("github.url", GITHUB_URL);

        WRAPPER_OP_PREFIX = configLoader.getProperty("operation.wrapper.prefix", "wrap-");
        configLoader.setProperty("operation.wrapper.prefix", WRAPPER_OP_PREFIX);

        BOT_OP_PREFIX = configLoader.getProperty("operation.bot.prefix", "bot-");
        configLoader.setProperty("operation.bot.prefix", BOT_OP_PREFIX);

        configLoader.saveProperties();
    }

}
