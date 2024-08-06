package com.infinitehorizons.exceptions;

import org.jetbrains.annotations.NotNull;

/**
 * An exception that is thrown when there is an invalid configuration of command packages within the {@code Shared} library.
 * This exception is used to indicate issues related to command package setup, ensuring that developers can identify and fix configuration problems quickly.
 */
public class InvalidPackageException extends Exceptions {

    /**
     * Constructs a new {@code InvalidPackageException} with the specified detail message.
     * This constructor provides information about the specific configuration issue encountered.
     *
     * @param message The detail message explaining the nature of the invalid package configuration.
     */
    public InvalidPackageException(@NotNull String message) {
        super(message);
    }

}
