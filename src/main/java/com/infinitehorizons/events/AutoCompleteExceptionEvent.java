package com.infinitehorizons.events;

import com.infinitehorizons.Shared;
import com.infinitehorizons.handler.AutoCompletableHandler;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.AutoCompleteQuery;
import net.dv8tion.jda.api.interactions.commands.CommandAutoCompleteInteraction;
import org.jetbrains.annotations.NotNull;

/**
 * Event fired when an exception occurs during an autocomplete interaction.
 *
 * @see AutoCompletableHandler#handleAutoComplete(CommandAutoCompleteInteractionEvent, AutoCompleteQuery)
 */
public class AutoCompleteExceptionEvent extends ThrowableEvent<CommandAutoCompleteInteraction> {

    /**
     * Constructs a new instance of AutoCompleteExceptionEvent.
     *
     * @param shared     The {@link Shared} instance that fired the event.
     * @param interaction The {@link CommandAutoCompleteInteraction} involved in the event.
     * @param throwable   The {@link Throwable} that was thrown.
     */
    public AutoCompleteExceptionEvent(
            @NotNull Shared shared,
            @NotNull CommandAutoCompleteInteraction interaction,
            @NotNull Throwable throwable) {
        super("onAutoCompleteException", shared, interaction, throwable);
    }
}
