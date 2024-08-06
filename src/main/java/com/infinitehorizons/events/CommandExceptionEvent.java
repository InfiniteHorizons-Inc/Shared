package com.infinitehorizons.events;

import com.infinitehorizons.Shared;
import com.infinitehorizons.commands.SlashCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import org.jetbrains.annotations.NotNull;

/**
 * Event triggered when an exception occurs during the execution of a command.
 *
 * @see SlashCommand#execute(SlashCommandInteractionEvent)
 * @see SlashCommand.Subcommand#execute(SlashCommandInteractionEvent)
 * @see com.infinitehorizons.commands.ContextCommand.User#execute(Object)
 * @see com.infinitehorizons.commands.ContextCommand.Message#execute(Object)
 */
public class CommandExceptionEvent extends ThrowableEvent<CommandInteraction> {

    /**
     * Constructs a new CommandExceptionEvent.
     *
     * @param shared     The {@link Shared} instance that fired this event.
     * @param interaction The {@link CommandInteraction} during which the exception was raised.
     * @param throwable   The {@link Throwable} that was thrown.
     */
    public CommandExceptionEvent(
            @NotNull Shared shared,
            @NotNull CommandInteraction interaction,
            @NotNull Throwable throwable) {
        super("onCommandException", shared, interaction, throwable);
    }
}