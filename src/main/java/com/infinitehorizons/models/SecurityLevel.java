package com.infinitehorizons.models;

public enum SecurityLevel {
    CONTRIBUTOR,
    STAFF,
    DEVELOPER,
    OWNER;

    public boolean isAuthorised(SecurityLevel level) {
        return level.ordinal() <= this.ordinal();
    }

}
