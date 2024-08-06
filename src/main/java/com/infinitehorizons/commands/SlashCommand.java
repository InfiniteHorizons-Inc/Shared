package com.infinitehorizons.commands;

import com.infinitehorizons.handler.InteractionHandler;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a slash command in Discord, which can include subcommands and subcommand groups.
 * This class provides a structure for managing complex command hierarchies.
 */
@Setter
@Getter
public abstract class SlashCommand extends BaseApplicationCommand<SlashCommandInteractionEvent, SlashCommandData> {

    private Subcommand[] subcommands = new Subcommand[]{};

    private SubcommandGroup[] subcommandGroups = new SubcommandGroup[]{};

    protected SlashCommand() {}

    /**
     * Adds {@link Subcommand}s to this {@link SlashCommand}.
     * Each subcommand's parent is set to this command.
     *
     * @param classes Instances of the {@link Subcommand}s to add.
     */
    public final void addSubcommands(@NotNull Subcommand... classes) {
        for (Subcommand subcommand : classes) {
            subcommand.setParent(this);
        }
        this.subcommands = classes;
    }

    /**
     * Adds {@link SubcommandGroup}s to this {@link SlashCommand}.
     * Sets each subcommand's parent within the group to this command.
     *
     * @param groups Instances of the {@link SubcommandGroup} class.
     */
    public final void addSubcommandGroups(@NotNull SubcommandGroup... groups) {
        for (SubcommandGroup group : groups) {
            for (Subcommand subcommand : group.getSubcommands()) {
                subcommand.setParent(this);
            }
        }
        this.subcommandGroups = groups;
    }

    /**
     * Executes the command logic when a slash command interaction event is triggered.
     * This method should be overridden by subclasses to define specific command behavior.
     *
     * @param event The {@link SlashCommandInteractionEvent} that triggered the execution.
     */
    @Override
    public void execute(@NotNull SlashCommandInteractionEvent event) {}

    /**
     * Retrieves the corresponding {@link Command JDA entity} for this command.
     * Returns if the command wasn't cached or queued before (for example, in sharded environments).
     *
     * @return The {@link Command} corresponding to this class, or null if not found.
     */
    @NotNull
    public Command asCommand() {
        return InteractionHandler.getRetrievedCommands().get(getCommandData().getName());
    }

    /**
     * Represents a single subcommand within a slash command.
     */
    @Setter
    public abstract static class Subcommand extends ApplicationCommand<SlashCommandInteractionEvent, SubcommandData> {

        private SlashCommand parent = null;

        public Subcommand() {}

        /**
         * Gets the parent {@link SlashCommand} for this subcommand.
         *
         * @return The corresponding {@link SlashCommand}.
         */
        @Nullable
        public SlashCommand getParent() {
            return parent;
        }

        /**
         * Retrieves the corresponding {@link Command.Subcommand JDA entity} for this subcommand.
         * Returns null if the subcommand wasn't cached or queued before (for example, in sharded environments).
         *
         * @return The {@link Command.Subcommand} corresponding to this class, or null if not found.
         */
        @Nullable
        public Command.Subcommand asSubcommand() {
            Command cmd = parent.asCommand();
            List<Command.Subcommand> subcommands = new ArrayList<>(cmd.getSubcommands());
            cmd.getSubcommandGroups().forEach(g -> subcommands.addAll(g.getSubcommands()));
            return subcommands.stream()
                    .filter(c -> c.getName().equals(getCommandData().getName()))
                    .findFirst()
                    .orElse(null);
        }
    }

    /**
     * Represents a group of subcommands within a slash command.
     * This class holds the {@link SubcommandGroupData} and an array of all {@link Subcommand}s.
     */
    @Getter
    public static class SubcommandGroup {

        private final SubcommandGroupData data;

        private final Subcommand[] subcommands;

        private SubcommandGroup(@NotNull SubcommandGroupData data, @NotNull Subcommand... subcommands) {
            this.data = data;
            this.subcommands = subcommands;
        }

        /**
         * Creates a new instance of the {@link SubcommandGroup} class.
         *
         * @param data        The {@link SubcommandGroupData} to use.
         * @param subcommands An array of {@link Subcommand}s. This shouldn't be empty!
         * @return The {@link SubcommandGroup}.
         * @throws IllegalArgumentException if subcommands is empty.
         */
        @NotNull
        public static SubcommandGroup of(@NotNull SubcommandGroupData data, @NotNull Subcommand... subcommands) {
            if (subcommands.length == 0) {
                throw new IllegalArgumentException("Subcommands may not be empty!");
            }
            return new SubcommandGroup(data, subcommands);
        }
    }
}

