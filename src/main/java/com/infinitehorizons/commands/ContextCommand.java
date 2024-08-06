package com.infinitehorizons.commands;

import com.infinitehorizons.Shared;
import lombok.NoArgsConstructor;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

/**
 * Represents a generic context command within the {@link Shared} framework.
 * This class serves as a base for creating specific user and message context commands, allowing developers
 * to define how the application responds to these types of interactions.
 *
 * @param <E> The type of event this command handles, extending {@link GenericCommandInteractionEvent}.
 * @since v1.5
 */
@NoArgsConstructor
public abstract class ContextCommand<E extends GenericCommandInteractionEvent> extends BaseApplicationCommand<E, CommandData> {

    /**
     * Represents a user-context command, which is triggered by a user interaction in Discord.
     * Developers should extend this class to define the behavior for user context commands.
     */
    @NoArgsConstructor
    public abstract static class User extends ContextCommand<UserContextInteractionEvent> {
        // Custom user context command implementation goes here
    }

    /**
     * Represents a message-context command, which is triggered by a message interaction in Discord.
     * Developers should extend this class to define the behavior for message context commands.
     */
    @NoArgsConstructor
    public abstract static class Message extends ContextCommand<MessageContextInteractionEvent> {
        // Custom message context command implementation goes here
    }
}
