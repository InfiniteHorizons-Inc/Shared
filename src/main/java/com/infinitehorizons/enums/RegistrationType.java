package com.infinitehorizons.enums;

/**
 * Represents the different types of registration available in the system.
 * This enum is used to distinguish between global and guild-specific registrations.
 */
public enum RegistrationType {

    /**
     * GLOBAL registration type.
     * <p>
     * This type of registration is applicable across the entire system,
     * allowing for universal access and permissions.
     * It is not restricted to any specific guild or group.
     */
    GLOBAL,

    /**
     * GUILD registration type.
     * <p>
     * This type of registration is limited to a specific guild or group.
     * It is used when access and permissions need to be restricted
     * to a particular community or organization within the system.
     */
    GUILD

}
