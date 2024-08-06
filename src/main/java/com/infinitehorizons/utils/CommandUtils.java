package com.infinitehorizons.utils;

import com.infinitehorizons.commands.BaseApplicationCommand;
import com.infinitehorizons.commands.ContextCommand;
import com.infinitehorizons.commands.SlashCommand;
import com.infinitehorizons.enums.RegistrationType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.utils.data.DataObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility class providing methods for comparing, managing, and manipulating command data.
 * This class supports operations for both slash commands and context commands.
 *
 * @since v1.1.1-SNAPSHOT
 */
public class CommandUtils {

    private CommandUtils() {
        // Private constructor to prevent instantiation
    }

    /**
     * Compares two {@link DataObject} instances to determine if they are equivalent, ignoring the order of options.
     *
     * @param data  The first {@link DataObject}.
     * @param other The second {@link DataObject}.
     * @return {@code true} if both {@link DataObject} instances have the same properties and options, otherwise {@code false}.
     */
    public static boolean equals(@NotNull DataObject data, @NotNull DataObject other) {
        if (!Arrays.equals(ArrayUtil.sortArrayFromDataArray(data.getArray("options")),
                ArrayUtil.sortArrayFromDataArray(other.getArray("options")))) {
            return false;
        }
        Map<String, Object> dataMap = data.remove("options").toMap();
        Map<String, Object> otherMap = other.remove("options").toMap();
        return dataMap.equals(otherMap);
    }

    /**
     * Compares a {@link Command} that encapsulates a {@link SlashCommandData} with a {@link SlashCommand}.
     *
     * @param cmd  The {@link Command} containing {@link SlashCommandData}.
     * @param data The {@link SlashCommand} to compare against.
     * @return {@code true} if the command data and the slash command are identical, otherwise {@code false}.
     */
    public static boolean compareSlashCommands(@NotNull Command cmd, @NotNull SlashCommand data) {
        return equals(CommandData.fromCommand(cmd).toData(), data.getCommandData().toData());
    }

    /**
     * Compares a {@link Command} that encapsulates a context command with a {@link ContextCommand}.
     *
     * @param cmd  The {@link Command} containing a context command.
     * @param data The {@link ContextCommand} to compare against.
     * @return {@code true} if the command data and the context command are identical, otherwise {@code false}.
     */
    public static boolean compareContextCommands(@NotNull Command cmd, @NotNull ContextCommand<?> data) {
        return equals(CommandData.fromCommand(cmd).toData(), data.getCommandData().toData());
    }

    /**
     * Builds a single command path by joining multiple command components together.
     *
     * @param args The command components to join, provided as {@link String} arguments.
     * @return A single combined command path as a {@link String}.
     */
    @NotNull
    public static String buildCommandPath(@NotNull String... args) {
        return String.join(" ", args);
    }

    /**
     * Generates a formatted string containing the names of all commands from the given sets.
     *
     * @param contextCommands A set of {@link ContextCommand} instances.
     * @param slashCommands   A set of {@link SlashCommand} instances.
     * @return A formatted string containing all command names.
     */
    @NotNull
    public static String getNames(@NotNull Set<ContextCommand<?>> contextCommands, @NotNull Set<SlashCommand> slashCommands) {
        return contextCommands.stream().map(c -> ", " + c.getCommandData().getName())
                .collect(Collectors.joining("", "", ""))
                + slashCommands.stream().map(c -> ", /" + c.getCommandData().getName())
                .collect(Collectors.joining("", "", ""))
                .substring(2); // Remove the leading comma and space
    }

    /**
     * Filters a {@link Pair} of command sets to include only commands matching the specified {@link RegistrationType}.
     *
     * @param pair The {@link Pair} of command sets to filter.
     * @param type The {@link RegistrationType} to filter by.
     * @return A new {@link Pair} containing only the commands matching the specified type.
     */
    @NotNull
    public static Pair<Set<SlashCommand>, Set<ContextCommand<?>>> filterByType(
            @NotNull Pair<Set<SlashCommand>, Set<ContextCommand<?>>> pair,
            @NotNull RegistrationType type) {
        return new Pair<>(
                pair.getFirst().stream().filter(c -> c.getRegistrationType().equals(type)).collect(Collectors.toSet()),
                pair.getSecond().stream().filter(c -> c.getRegistrationType().equals(type)).collect(Collectors.toSet())
        );
    }

    /**
     * Determines whether a command should be registered for a specific guild.
     *
     * @param guild   The {@link Guild} to check.
     * @param command The {@link BaseApplicationCommand} to evaluate.
     * @return {@code true} if the command should be registered, otherwise {@code false}.
     */
    public static boolean shouldBeRegistered(@NotNull Guild guild, @NotNull BaseApplicationCommand<?, ?> command) {
        Long[] guildIds = command.getQueueableGuilds();
        boolean shouldRegister = guildIds.length == 0 || List.of(guildIds).contains(guild.getIdLong());
        if (!shouldRegister) {
            Logger.error(Logger.Type.SLASH_COMMAND_SKIPPED,
                    "Skipping registration of a command for guild %s.", guild.getName());
        }
        return shouldRegister;
    }

    /**
     * Retrieves the mention string for a {@link SlashCommand}.
     *
     * @param command The {@link SlashCommand} to get the mention for.
     * @return The mention string for the command.
     */
    @NotNull
    public static String getAsMention(@NotNull SlashCommand command) {
        Command entity = command.asCommand();
        return entity.getAsMention();
    }

    /**
     * Retrieves the mention string for a {@link SlashCommand.Subcommand}.
     *
     * @param command The {@link SlashCommand.Subcommand} to get the mention for.
     * @return The mention string for the subcommand, or {@code null} if the subcommand is not found.
     */
    @Nullable
    public static String getAsMention(@NotNull SlashCommand.Subcommand command) {
        Command.Subcommand entity = command.asSubcommand();
        return entity != null ? entity.getAsMention() : null;
    }
}
