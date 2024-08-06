package com.infinitehorizons.components;

import com.infinitehorizons.Shared;
import net.dv8tion.jda.api.interactions.components.ComponentInteraction;
import com.infinitehorizons.events.ThrowableEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Event fired when an exception occurs during a message component interaction.
 */
public class EventExceptionComponent extends ThrowableEvent<ComponentInteraction> {

    /**
     * Constructs a new instance of the EventExceptionComponent.
     *
     * @param shared     The {@link Shared} instance that fired this event.
     * @param interaction The {@link ComponentInteraction} involved in the event.
     * @param throwable   The {@link Throwable} that was thrown.
     */
    public EventExceptionComponent(
            @NotNull Shared shared,
            @NotNull ComponentInteraction interaction,
            @NotNull Throwable throwable) {
        super("onExceptionComponent", shared, interaction, throwable);
    }
}
