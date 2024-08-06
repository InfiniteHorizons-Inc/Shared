package com.infinitehorizons.events;

import com.infinitehorizons.Shared;
import com.infinitehorizons.commands.RestrictedCommand;
import lombok.Getter;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Event triggered when a user lacks the required permissions to execute a command.
 *
 * @see RestrictedCommand#setRequiredPermissions(Permission...)
 */
@Getter
public class InsufficientPermissionsEvent extends Event<CommandInteraction> {

    /**
     * The set of permissions that were required for the command.
     */
    private final Set<Permission> permissions;

    /**
     * Constructs a new InsufficientPermissionsEvent.
     *
     * @param shared     The {@link Shared} instance that fired this event.
     * @param interaction The {@link CommandInteraction} that was attempted.
     * @param permissions The {@link Set} of {@link Permission} objects that were required.
     */
    public InsufficientPermissionsEvent(
            @NotNull Shared shared,
            @NotNull CommandInteraction interaction,
            @NotNull Set<Permission> permissions) {
        super("onInsufficientPermissions", shared, interaction);
        this.permissions = permissions;
    }
}
