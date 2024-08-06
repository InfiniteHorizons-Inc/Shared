package com.infinitehorizons.commands;

import com.infinitehorizons.Shared;
import com.infinitehorizons.enums.RegistrationType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;

/**
 * An abstract class that extends {@link ApplicationCommand}, representing a top-level command
 * which can be queued either per-guild or globally.
 *
 * @param <E> The event type that this command uses, extending {@link GenericCommandInteractionEvent}.
 * @param <T> The type of {@link CommandData} that this command uses.
 */
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public abstract class BaseApplicationCommand<E extends GenericCommandInteractionEvent, T> extends ApplicationCommand<E, T> {

    /**
     * The registration type assigned to the command.
     */
    private RegistrationType registrationType = Shared.getDefaultRegistrationType();

    /**
     * The list of guild IDs where this command should be queued.
     * An empty array indicates that the command should be queued globally.
     */
    private Long[] queueableGuilds = new Long[]{};

    /**
     * Sets the registration type for this command.
     * <p>
     * This method is not applicable for {@link SlashCommand.Subcommand}.
     *
     * @param type the {@link RegistrationType} to set.
     * @return the current instance of {@link BaseApplicationCommand} for method chaining.
     */
    @NotNull
    public BaseApplicationCommand<E, T> setRegistrationType(@NotNull RegistrationType type) {
        this.registrationType = type;
        return this;
    }

    /**
     * Limits this command to only be queued in the specified guilds.
     * Leave this parameter empty to queue the command everywhere.
     *
     * @param queueableGuilds The guild IDs as a {@link Long} array.
     * @return the current instance of {@link BaseApplicationCommand} for method chaining.
     */
    @NotNull
    public BaseApplicationCommand<E, T> setQueueableGuilds(@NotNull Long... queueableGuilds) {
        this.queueableGuilds = queueableGuilds;
        return this;
    }
}
