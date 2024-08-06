package com.infinitehorizons.events;

import com.infinitehorizons.Shared;
import com.infinitehorizons.commands.RestrictedCommand;
import com.infinitehorizons.utils.CooldownUtil;
import lombok.Getter;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

/**
 * Event triggered when a command invocation is prevented due to a command cooldown restriction.
 *
 * <p>
 * <b>Note:</b> Command cooldowns do not persist between sessions.
 * </p>
 *
 * @see RestrictedCommand#setCommandCooldown(Duration)
 */
public class CommandCooldownEvent extends Event<CommandInteraction> {

    /** The cooldown details for the command. */
    @Getter
    private final CooldownUtil cooldown;

    /**
     * Constructs a new CommandCooldownEvent.
     *
     * @param shared     The {@link Shared} instance that fired this event.
     * @param interaction The {@link CommandInteraction} involved in this event.
     * @param cooldown    The {@link CooldownUtil} affecting the command usage.
     */
    public CommandCooldownEvent(
            @NotNull Shared shared,
            @NotNull CommandInteraction interaction,
            @NotNull CooldownUtil cooldown) {
        super("onCommandCooldown", shared, interaction);
        this.cooldown = cooldown;
    }
}
