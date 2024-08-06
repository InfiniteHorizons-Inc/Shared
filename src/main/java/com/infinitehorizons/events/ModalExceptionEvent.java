package com.infinitehorizons.events;

import com.infinitehorizons.Shared;
import net.dv8tion.jda.api.interactions.modals.ModalInteraction;
import org.jetbrains.annotations.NotNull;

/**
 * Event triggered when an exception occurs during a modal interaction.
 */
public class ModalExceptionEvent extends ThrowableEvent<ModalInteraction> {

    /**
     * Constructs a new ModalExceptionEvent.
     *
     * @param shared     The {@link Shared} instance that fired this event.
     * @param interaction The {@link ModalInteraction} where the exception occurred.
     * @param throwable   The {@link Throwable} that caused this event.
     */
    public ModalExceptionEvent(
            @NotNull Shared shared,
            @NotNull ModalInteraction interaction,
            @NotNull Throwable throwable) {
        super("onModalException", shared, interaction, throwable);
    }
}
