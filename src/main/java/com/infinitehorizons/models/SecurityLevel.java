package com.infinitehorizons.models;

/**
 * Enum representing different security levels for user roles.
 * <p>
 * This enum defines four security levels: CONTRIBUTOR, STAFF, DEVELOPER, and OWNER.
 * It also provides a method to check if a given security level is authorized.
 */
public enum SecurityLevel {
    CONTRIBUTOR,
    STAFF,
    DEVELOPER,
    OWNER;

    /**
     * Checks if the current security level is authorized to perform actions at or below the specified level.
     *
     * @param level the security level to compare against.
     * @return {@code true} if the current level is equal to or higher than the specified level, {@code false} otherwise.
     */
    public boolean isAuthorised(SecurityLevel level) {
        return level.ordinal() <= this.ordinal();
    }
}
