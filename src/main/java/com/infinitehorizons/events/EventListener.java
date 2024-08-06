package com.infinitehorizons.events;

import com.infinitehorizons.commands.RestrictedCommand;
import com.infinitehorizons.commands.SlashCommand;
import com.infinitehorizons.components.EventExceptionComponent;
import com.infinitehorizons.handler.AutoCompletableHandler;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.AutoCompleteQuery;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

/**
 * Interface for handling various events that {@code Shared} can trigger.
 * This includes events related to exceptions, permissions, and command execution contexts.
 * <p>
 * Implementers can choose to override only the methods they're interested in.
 */
public interface EventListener {

    /**
     * Fired when an exception occurs during command execution.
     *
     * @param event The {@link EventExceptionComponent} that was triggered.
     * @see SlashCommand#execute(SlashCommandInteractionEvent)
     * @see SlashCommand.Subcommand#execute(SlashCommandInteractionEvent)
     * @see com.infinitehorizons.commands.ContextCommand.User#execute(Object)
     * @see com.infinitehorizons.commands.ContextCommand.Message#execute(Object)
     */
    default void onCommandException(@NotNull CommandExceptionEvent event) {}

    /**
     * Fired when an exception occurs during interaction with a message component.
     *
     * @param event The {@link EventExceptionComponent} that was triggered.
     */
    default void onComponentException(@NotNull EventExceptionComponent event) {}

    /**
     * Fired when an exception occurs during an autocomplete interaction.
     *
     * @param event The {@link AutoCompleteExceptionEvent} that was triggered.
     * @see AutoCompletableHandler#handleAutoComplete(CommandAutoCompleteInteractionEvent, AutoCompleteQuery)
     */
    default void onAutoCompleteException(@NotNull AutoCompleteExceptionEvent event) {}

    /**
     * Fired when an exception occurs during a modal interaction.
     *
     * @param event The {@link ModalExceptionEvent} that was triggered.
     */
    default void onModalException(@NotNull ModalExceptionEvent event) {}

    /**
     * Fired when a command is invoked by a user without the required permissions.
     *
     * @param event The {@link InsufficientPermissionsEvent} that was triggered.
     * @see RestrictedCommand#setRequiredPermissions(Permission...)
     */
    default void onInsufficientPermissions(@NotNull InsufficientPermissionsEvent event) {}

    /**
     * Fired when a command is invoked by a user who is not authorized to use it.
     *
     * @param event The {@link InvalidUserEvent} that was triggered.
     * @see RestrictedCommand#setRequiredUsers(Long...)
     */
    default void onInvalidUser(@NotNull InvalidUserEvent event) {}

    /**
     * Fired when a command is invoked by a user who does not have the required roles.
     *
     * @param event The {@link InvalidRoleEvent} that was triggered.
     * @see RestrictedCommand#setRequiredRoles(Long...)
     */
    default void onInvalidRole(@NotNull InvalidRoleEvent event) {}

    /**
     * Fired when a command is invoked outside the required guild.
     *
     * @param event The {@link InvalidGuildEvent} that was triggered.
     * @see RestrictedCommand#setRequiredGuilds(Long...)
     */
    default void onInvalidGuild(@NotNull InvalidGuildEvent event) {}

    /**
     * Fired when a command is invoked by a user who is still under cooldown.
     * <br><br>
     * <b>Note:</b> Command cooldowns do not persist between sessions.
     *
     * @param event The {@link CommandCooldownEvent} that was triggered.
     * @see RestrictedCommand#setCommandCooldown(Duration)
     */
    default void onCommandCooldown(@NotNull CommandCooldownEvent event) {}
}
