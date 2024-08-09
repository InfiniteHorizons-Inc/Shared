package com.infinitehorizons.utils;

import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;

public class UserUtil {

    @NotNull
    public static String getUserTag(@NotNull User user) {
        String name = user.getName();
        String discriminator = user.getDiscriminator();
        if ("0000".equals(discriminator)) {
            return name;
        } else {
            return name + "#" + discriminator;
        }
    }

}
