package com.infinitehorizons.events;

import com.infinitehorizons.Shared;
import com.infinitehorizons.commands.RestrictedCommand;
import lombok.Getter;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Event triggered when a command is executed outside the required guilds.
 *
 * @see RestrictedCommand#setRequiredGuilds(Long...)
 */
@Getter
public class InvalidGuildEvent extends Event<CommandInteraction> {

    /**
     * The set of guild IDs where the command is permitted to be executed.
     */
    private final Set<Long> guildIds;

    /**
     * Constructs a new InvalidGuildEvent.
     *
     * @param shared     The {@link Shared} instance that fired this event.
     * @param interaction The {@link CommandInteraction} that was attempted.
     * @param guildIds    The set of guild IDs where the {@link RestrictedCommand} can be executed.
     */
    public InvalidGuildEvent(
            @NotNull Shared shared,
            @NotNull CommandInteraction interaction,
            @NotNull Set<Long> guildIds) {
        super("onInvalidGuild", shared, interaction);
        this.guildIds = guildIds;
    }
}
