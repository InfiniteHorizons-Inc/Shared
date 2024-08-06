package com.infinitehorizons.queue;

import com.infinitehorizons.commands.ContextCommand;
import com.infinitehorizons.commands.SlashCommand;
import com.infinitehorizons.utils.CommandUtils;
import com.infinitehorizons.utils.Logger;
import com.infinitehorizons.utils.Pair;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Handles the smart queuing functionality for Discord commands, minimizing unnecessary updates by comparing
 * existing commands with local ones. This mechanism only queues new or modified commands and optionally deletes
 * unknown commands not found locally.
 * <p>
 * The SmartQueue can be controlled via SharedBuilder's configuration methods to enable or disable its functionality
 * for global and guild commands.
 * </p>
 *
 * @since v1.5
 */
public class SmartQueue {

    @Getter
    private final Set<SlashCommand> slashCommands;

    @Getter
    private final Set<ContextCommand<?>> contextCommands;

    private final boolean deleteUnknown;

    /**
     * Constructs a SmartQueue instance with the specified commands and configuration.
     *
     * @param slashCommands   The set of slash commands to manage.
     * @param contextCommands The set of context commands to manage.
     * @param deleteUnknown   Whether to delete unknown commands that are not found locally.
     */
    public SmartQueue(@NotNull Set<SlashCommand> slashCommands,
                      @NotNull Set<ContextCommand<?>> contextCommands,
                      boolean deleteUnknown) {
        this.slashCommands = slashCommands;
        this.contextCommands = contextCommands;
        this.deleteUnknown = deleteUnknown;
    }

    /**
     * Checks global commands against existing commands, removing duplicates and optionally deleting unknown ones.
     *
     * @param existing The list of existing commands to compare against.
     * @return A pair of the remaining slash commands and context commands after comparison.
     */
    @NotNull
    public Pair<Set<SlashCommand>, Set<ContextCommand<?>>> checkGlobal(@NotNull List<Command> existing) {
        return removeDuplicates(existing, null);
    }

    /**
     * Checks guild-specific commands against existing commands, removing duplicates and optionally deleting unknown ones.
     *
     * @param guild    The guild to retrieve existing commands from.
     * @param existing The list of existing commands to compare against.
     * @return A pair of the remaining slash commands and context commands after comparison.
     */
    @NotNull
    public Pair<Set<SlashCommand>, Set<ContextCommand<?>>> checkGuild(@NotNull Guild guild, @NotNull List<Command> existing) {
        return removeDuplicates(existing, guild);
    }

    /**
     * Removes duplicates from the list of existing commands, deleting unknown commands if configured to do so.
     *
     * @param existing The list of existing commands.
     * @param guild    An optional guild parameter used for guild-specific checks.
     * @return A pair of the remaining slash commands and context commands.
     */
    @NotNull
    private Pair<Set<SlashCommand>, Set<ContextCommand<?>>> removeDuplicates(@NotNull List<Command> existing, Guild guild) {
        List<Command> commands = new ArrayList<>(existing);
        boolean global = (guild == null);
        String prefix = String.format("[%s] ", global ? "Global" : guild.getName());
        Logger.info(Logger.Type.SMART_QUEUE, prefix + "Found %s existing command(s)", existing.size());

        // Remove duplicates from existing commands
        commands.removeIf(cmd -> {
            boolean isDuplicate = isDuplicateCommand(cmd, guild, global);
            if (isDuplicate && global) {
                Logger.info(Logger.Type.SMART_QUEUE_IGNORED, prefix + "Found duplicate %s command, which will be ignored: %s", cmd.getType(), cmd.getName());
            }
            return isDuplicate;
        });

        // Remove processed commands from the local sets
        slashCommands.removeIf(data -> commands.stream().anyMatch(cmd -> CommandUtils.compareSlashCommands(cmd, data)));
        contextCommands.removeIf(data -> commands.stream().anyMatch(cmd -> CommandUtils.compareContextCommands(cmd, data)));

        // Delete unknown commands if enabled
        if (!commands.isEmpty()) {
            commands.forEach(cmd -> checkUnknown(prefix, cmd));
        }
        return new Pair<>(slashCommands, contextCommands);
    }

    private boolean isDuplicateCommand(Command cmd, Guild guild, boolean global) {
        boolean isCheckingGuilds;
        if (cmd.getType().equals(Command.Type.SLASH)) {
            isCheckingGuilds = slashCommands.stream().anyMatch(data -> CommandUtils.compareSlashCommands(cmd, data));
            if (!global) {
                slashCommands.forEach(slash -> checkRequiredGuilds(guild, cmd, slash));
            }
        } else {
            isCheckingGuilds = contextCommands.stream().anyMatch(data -> CommandUtils.compareContextCommands(cmd, data));
            if (!global) {
                contextCommands.forEach(context -> checkRequiredGuilds(guild, cmd, context));
            }
        }
        return isCheckingGuilds;
    }

    private void checkUnknown(@NotNull String prefix, @NotNull Command command) {
        if (deleteUnknown) {
            Logger.info(Logger.Type.SMART_QUEUE_DELETED_UNKNOWN, prefix + "Deleting unknown %s command: %s", command.getType(), command.getName());
        } else {
            Logger.info(Logger.Type.SMART_QUEUE_IGNORED_UNKNOWN, prefix + "Ignored unknown %s command: %s", command.getType(), command.getName());
        }
    }

    private void checkRequiredGuilds(Guild guild, Command cmd, @NotNull SlashCommand data) {
        if (CommandUtils.compareSlashCommands(cmd, data) && data.getQueueableGuilds().length != 0 &&
                !List.of(data.getQueueableGuilds()).contains(guild.getIdLong())) {
            Logger.info("Deleting /%s in non-queueable Guild: %s", cmd.getName(), guild.getName());
        }
    }

    private void checkRequiredGuilds(Guild guild, Command cmd, @NotNull ContextCommand<?> data) {
        if (CommandUtils.compareContextCommands(cmd, data) && data.getQueueableGuilds().length != 0 &&
                !List.of(data.getQueueableGuilds()).contains(guild.getIdLong())) {
            Logger.info("Deleting /%s in non-queueable Guild: %s", cmd.getName(), guild.getName());
        }
    }
}
