package com.infinitehorizons.exceptions;

import com.infinitehorizons.Shared;
import org.jetbrains.annotations.NotNull;

/**
 * Exception thrown when a command is not registered with the {@link Shared} instance.
 * This exception indicates that an attempt to execute or access a command failed because the command was not properly registered.
 */
public class CommandNotRegisteredException extends Exceptions {

    /**
     * Constructs a new {@code CommandNotRegisteredException} with the specified detail message.
     *
     * @param message The detail message explaining the reason for the exception.
     */
    public CommandNotRegisteredException(@NotNull String message) {
        super(message);
    }
}
