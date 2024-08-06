package com.infinitehorizons.events;

import com.infinitehorizons.Shared;
import com.infinitehorizons.commands.RestrictedCommand;
import lombok.Getter;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Event triggered when a command is invoked by a user who is not allowed to use it.
 *
 * @see RestrictedCommand#setRequiredUsers(Long...)
 */
@Getter
public class InvalidUserEvent extends Event<CommandInteraction> {

    /**
     * The set of user IDs required to execute the command.
     */
    private final Set<Long> userIds;

    /**
     * Constructs a new InvalidUserEvent.
     *
     * @param shared     The {@link Shared} instance that fired this event.
     * @param interaction The {@link CommandInteraction} attempted.
     * @param userIds     The set of user IDs required to execute the {@link RestrictedCommand}.
     */
    public InvalidUserEvent(
            @NotNull Shared shared,
            @NotNull CommandInteraction interaction,
            @NotNull Set<Long> userIds) {
        super("onInvalidUser", shared, interaction);
        this.userIds = userIds;
    }
}
