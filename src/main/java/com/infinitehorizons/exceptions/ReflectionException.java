package com.infinitehorizons.exceptions;

import org.jetbrains.annotations.NotNull;

/**
 * An exception that is thrown for errors related to reflection operations within the {@code Shared} library.
 * This exception encapsulates reflection-related issues, providing a clear indication of the source of the problem.
 */
public class ReflectionException extends Exceptions {

    /**
     * Constructs a new {@code ReflectionException} with the specified cause.
     * This constructor is used to wrap a lower-level {@link Throwable} that occurred during reflection operations,
     * enabling better error handling and debugging within the {@code Shared} framework.
     *
     * @param cause The underlying {@link Throwable} that caused this exception.
     */
    public ReflectionException(@NotNull Throwable cause) {
        super(cause);
    }

}
