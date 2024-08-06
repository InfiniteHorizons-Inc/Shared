package com.infinitehorizons.exceptions;

import org.jetbrains.annotations.NotNull;

/**
 * The top-level exception for all errors related to the {@code Shared} library.
 * This exception serves as a base class for specific exceptions thrown by the library,
 * providing a unified type for catching {@code Shared}-related errors.
 */
public class Exceptions extends Exception {

    /**
     * Constructs a new {@code Exceptions} with the specified detail message.
     *
     * @param message The detail message explaining the reason for the exception.
     */
    public Exceptions(@NotNull String message) {
        super(message);
    }

    /**
     * Constructs a new {@code Exceptions} with the specified cause.
     * This constructor allows an exception to be created when a lower-level exception
     * triggers a higher-level error in the {@code Shared} library.
     *
     * @param cause The underlying {@link Throwable} that caused this exception.
     */
    public Exceptions(@NotNull Throwable cause) {
        super(cause);
    }

}
