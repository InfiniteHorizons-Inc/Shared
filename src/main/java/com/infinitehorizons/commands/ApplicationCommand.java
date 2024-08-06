package com.infinitehorizons.commands;

import com.infinitehorizons.utils.ChecksUtils;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;

/**
 * Abstract class representing an application command with restricted execution capabilities.
 * <p>
 * This class serves as a base for commands that can be executed within a specific context,
 * with additional command data that can be set and retrieved.
 *
 * @param <E> The type of event that triggers the command execution.
 *            Must extend {@link GenericCommandInteractionEvent}.
 * @param <T> The type of additional data associated with the command.
 */
@Getter
@Setter
@NoArgsConstructor
public abstract class ApplicationCommand<E extends GenericCommandInteractionEvent, T>
        extends RestrictedCommand implements ExecutableCommand<E> {

    /** The additional data associated with the command. */
    private T data;

    /**
     * Sets the additional data for the command.
     *
     * @param data The data to associate with the command.
     */
    public final void setCommandData(T data) {
        this.data = data;
    }

    /**
     * Retrieves the additional data associated with the command.
     *
     * @return The additional data of the command.
     * @throws IllegalArgumentException if the command data is null.
     */
    public final T getCommandData() {
        ChecksUtils.notNull(data, "Command data");
        return data;
    }
}
