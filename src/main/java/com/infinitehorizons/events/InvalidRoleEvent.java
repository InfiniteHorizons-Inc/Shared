package com.infinitehorizons.events;

import com.infinitehorizons.Shared;
import com.infinitehorizons.commands.RestrictedCommand;
import lombok.Getter;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Event triggered when a command is invoked by a user who lacks the required roles.
 *
 * @see RestrictedCommand#setRequiredRoles(Long...)
 */
@Getter
public class InvalidRoleEvent extends Event<CommandInteraction> {

    /**
     * The set of role IDs required to execute the command.
     */
    private final Set<Long> roleIds;

    /**
     * Constructs a new InvalidRoleEvent.
     *
     * @param shared     The {@link Shared} instance that fired this event.
     * @param interaction The {@link CommandInteraction} attempted.
     * @param roleIds     The set of role IDs required to execute the {@link RestrictedCommand}.
     */
    public InvalidRoleEvent(
            @NotNull Shared shared,
            @NotNull CommandInteraction interaction,
            @NotNull Set<Long> roleIds) {
        super("onInvalidRole", shared, interaction);
        this.roleIds = roleIds;
    }
}
