package com.infinitehorizons.events;

import com.infinitehorizons.Shared;
import net.dv8tion.jda.api.interactions.components.ComponentInteraction;
import org.jetbrains.annotations.NotNull;

/**
 * Event triggered when an exception occurs during interaction with a message component.
 */
public class ComponentExceptionEvent extends ThrowableEvent<ComponentInteraction> {

    /**
     * Constructs a new ComponentExceptionEvent.
     *
     * @param shared     The {@link Shared} instance that fired this event.
     * @param interaction The {@link ComponentInteraction} during which the exception was raised.
     * @param throwable   The {@link Throwable} that was thrown.
     */
    public ComponentExceptionEvent(
            @NotNull Shared shared,
            @NotNull ComponentInteraction interaction,
            @NotNull Throwable throwable) {
        super("onComponentException", shared, interaction, throwable);
    }
}
