package com.infinitehorizons.constants;

import com.infinitehorizons.config.ConfigLoader;

import java.awt.*;

public final class SharedConstants {

    public static final Color CASCADE_COLOR;
    public static final String GITHUB_URL;
    public static final String WRAPPER_OP_PREFIX;
    public static final String BOT_OP_PREFIX;

    static {
        ConfigLoader configLoader = new ConfigLoader();

        String colorProperty = configLoader.getProperty("cascade.color");
        String[] rgb = colorProperty.split(",");
        CASCADE_COLOR = new Color(
                Integer.parseInt(rgb[0].trim()),
                Integer.parseInt(rgb[1].trim()),
                Integer.parseInt(rgb[2].trim())
        );

        GITHUB_URL = configLoader.getProperty("github.url");
        WRAPPER_OP_PREFIX = configLoader.getProperty("operation.wrapper.prefix");
        BOT_OP_PREFIX = configLoader.getProperty("operation.bot.prefix");
    }

    private SharedConstants() {
    }
}
