package com.infinitehorizons.handler;

import com.infinitehorizons.Shared;
import com.infinitehorizons.commands.BaseApplicationCommand;
import com.infinitehorizons.commands.ContextCommand;
import com.infinitehorizons.commands.RestrictedCommand;
import com.infinitehorizons.commands.SlashCommand;
import com.infinitehorizons.components.BuilderComponent;
import com.infinitehorizons.components.BuilderIdComponent;
import com.infinitehorizons.components.MappingIdComponent;
import com.infinitehorizons.config.ConfigLoader;
import com.infinitehorizons.enums.RegistrationType;
import com.infinitehorizons.events.*;
import com.infinitehorizons.exceptions.CommandNotRegisteredException;
import com.infinitehorizons.exceptions.Exceptions;
import com.infinitehorizons.queue.SmartQueue;
import com.infinitehorizons.utils.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * InteractionHandler is responsible for finding, registering, and managing all application commands
 * and their components within the JDA framework. This class extends {@link ListenerAdapter}
 * to handle events fired by the Discord API.
 *
 * @see BuilderComponent#disableAutomaticCommandRegistration()
 * @see Shared#registerInteractions()
 */
@Getter
@AllArgsConstructor
public class InteractionHandler extends ListenerAdapter {

    /**
     * Cache for all retrieved (and/or queued) commands.
     */
    private static final Map<String, Command> RETRIEVED_COMMANDS = new HashMap<>();

    private final Set<SlashCommand> slashCommands;
    private final Set<ContextCommand<?>> contextCommands;
    /**
     * The main {@link Shared} instance.
     */
    private final Shared shared;
    /**
     * The instance's {@link ConfigLoader configuration}.
     */
    private final ConfigLoader config;
    /**
     * An index of all {@link SlashCommand}s with their {@link SlashCommandInteractionEvent#getFullCommandName name} as their key.
     *
     * @see InteractionHandler#findSlashCommands(String)
     */
    private final Map<String, SlashCommand> slashCommandIndex = new HashMap<>();
    /**
     * An index of all {@link SlashCommand.Subcommand}s.
     *
     * @see InteractionHandler#findSlashCommands(String)
     */
    private final Map<String, SlashCommand.Subcommand> subcommandIndex = new HashMap<>();
    /**
     * An index of all {@link ContextCommand.Message}s.
     *
     * @see InteractionHandler#findContextCommands(String)
     */
    private final Map<String, ContextCommand.Message> messageContextIndex = new HashMap<>();
    /**
     * An index of all {@link ContextCommand.User}s.
     *
     * @see InteractionHandler#findContextCommands(String)
     */
    private final Map<String, ContextCommand.User> userContextIndex = new HashMap<>();
    /**
     * An index of all {@link AutoCompletableHandler}s.
     *
     * @see InteractionHandler#findSlashCommands(String)
     */
    private final Map<String, AutoCompletableHandler> autoCompleteIndex = new HashMap<>();

    /**
     * Constructs a new {@link InteractionHandler} from the supplied {@link Shared} instance.
     *
     * @param shared The {@link Shared} instance.
     */
    public InteractionHandler(@NotNull Shared shared) {
        this.shared = shared;
        this.config = shared.getConfig();

        this.slashCommands = new HashSet<>();
        this.contextCommands = new HashSet<>();
        for (String pkg : config.getCommandsPackages()) {
            try {
                findSlashCommands(pkg);
                findContextCommands(pkg);
            } catch (ReflectiveOperationException | Exceptions e) {
                Logger.error("An error occurred while initializing commands in package %s: %s", pkg, e.getMessage());
            }
        }
    }

    /**
     * Returns an unmodifiable map of all retrieved (and/or queued) commands, where the key is the command's name and
     * the value is the {@link Command} instance itself.
     * This map is empty if {@link Shared#registerInteractions()} wasn't called before.
     *
     * @return An immutable {@link Map} containing all global and guild commands.
     */
    @NotNull
    public static Map<String, Command> getRetrievedCommands() {
        return Collections.unmodifiableMap(RETRIEVED_COMMANDS);
    }

    /**
     * Registers all interactions.
     * This method can be accessed from the {@link Shared} instance.
     * <br>This is automatically executed each time the {@link ListenerAdapter#onReady(net.dv8tion.jda.api.events.session.ReadyEvent)} event is executed.
     * (can be disabled using {@link BuilderComponent#disableAutomaticCommandRegistration()})
     */
    public void registerInteractions() {
        // retrieve (and smart queue) guild commands
        Pair<Set<SlashCommand>, Set<ContextCommand<?>>> data = new Pair<>(getSlashCommands(), getContextCommandData());
        for (Guild guild : config.getJda().getGuilds()) {
            guild.retrieveCommands(true).queue(existing -> {
                Pair<Set<SlashCommand>, Set<ContextCommand<?>>> guildData = CommandUtils.filterByType(data, RegistrationType.GUILD);
                existing.forEach(this::cacheCommand);
                // check if smart queuing is enabled
                if (config.isGuildSmartQueue()) {
                    guildData = new SmartQueue(guildData.getFirst(), guildData.getSecond(), config.isDeleteUnknownCommands()).checkGuild(guild, existing);
                }
                // upsert all (remaining) guild commands
                if (!guildData.getFirst().isEmpty() || !guildData.getSecond().isEmpty()) {
                    upsert(guild, guildData.getFirst(), guildData.getSecond());
                }
            }, error -> Logger.error("Could not retrieve commands for guild %s!" +
                    " Please make sure that the bot was invited with the application.commands scope!", guild.getName()));
        }
        // retrieve (and smart queue) global commands
        config.getJda().retrieveCommands(true).queue(existing -> {
            Pair<Set<SlashCommand>, Set<ContextCommand<?>>> globalData = CommandUtils.filterByType(data, RegistrationType.GLOBAL);
            existing.forEach(this::cacheCommand);
            // check if smart queuing is enabled
            if (config.isGlobalSmartQueue()) {
                globalData = new SmartQueue(globalData.getFirst(), globalData.getSecond(), config.isDeleteUnknownCommands()).checkGlobal(existing);
            }
            // upsert all (remaining) global commands
            if (!globalData.getFirst().isEmpty() || !globalData.getSecond().isEmpty()) {
                upsert(config.getJda(), globalData.getFirst(), globalData.getSecond());
                Logger.info(Logger.Type.COMMANDS_QUEUED, "Queued %s global command(s): %s",
                        globalData.getFirst().size() + globalData.getSecond().size(), CommandUtils.getNames(globalData.getSecond(), globalData.getFirst()));
            }
        }, error -> Logger.error("Could not retrieve global commands!"));
        // Log autocomplete bindings
        if (!autoCompleteIndex.isEmpty()) {
            // print autocomplete bindings
            Logger.info("Created %s AutoComplete binding(s): %s", autoCompleteIndex.size(),
                    autoCompleteIndex.entrySet().stream().map(entry -> entry.getKey() + "=" + entry.getValue().getClass().getSimpleName()).collect(Collectors.joining(", ")));
        }
    }

    /**
     * Uses the provided {@link Set} of {@link SlashCommand} and {@link ContextCommand ContextCommandData}
     * and updates them globally, using the {@link JDA} instance.
     *
     * @param jda             The {@link JDA} instance.
     * @param slashCommands   A {@link Set} of {@link SlashCommand}.
     * @param contextCommands A {@link Set} of {@link ContextCommand}.
     * @since v1.7
     */
    private void update(@NotNull JDA jda, @NotNull Set<SlashCommand> slashCommands, @NotNull Set<ContextCommand<?>> contextCommands) {
        jda.updateCommands()
                .addCommands(slashCommands.stream().map(SlashCommand::getCommandData).collect(Collectors.toSet()))
                .addCommands(contextCommands.stream().map(ContextCommand::getCommandData).collect(Collectors.toSet()))
                .queue(comdList -> comdList.forEach(this::cacheCommand));
    }

    /**
     * Uses the provided {@link Set} of {@link SlashCommand} and {@link ContextCommand ContextCommandData}
     * and queues them, using the specified {@link Guild} instance.
     * This also checks for {@link BaseApplicationCommand#getQueueableGuilds queueable guilds} and skips them if needed.
     *
     * @param guild           The {@link Guild}.
     * @param slashCommands   A {@link Set} of {@link SlashCommand}.
     * @param contextCommands A {@link Set} of {@link ContextCommand}.
     */
    private void update(@NotNull Guild guild, @NotNull Set<SlashCommand> slashCommands, @NotNull Set<ContextCommand<?>> contextCommands) {
        Set<SlashCommand> queueableSlashCommands = slashCommands.stream()
                .filter(cmd -> CommandUtils.shouldBeRegistered(guild, cmd)).collect(Collectors.toSet());
        Set<ContextCommand<?>> queueableContextCommands = contextCommands.stream()
                .filter(cmd -> CommandUtils.shouldBeRegistered(guild, cmd)).collect(Collectors.toSet());
        guild.updateCommands()
                .addCommands(queueableSlashCommands.stream().map(SlashCommand::getCommandData).collect(Collectors.toSet()))
                .addCommands(queueableContextCommands.stream().map(ContextCommand::getCommandData).collect(Collectors.toSet()))
                .queue(comdList -> comdList.forEach(this::cacheCommand));

        if (!queueableSlashCommands.isEmpty() || !queueableContextCommands.isEmpty()) {
            List<String> commandNames = queueableSlashCommands.stream()
                    .map(cmd -> cmd.getCommandData().getName()).collect(Collectors.toList());
            commandNames.addAll(queueableContextCommands.stream().map(cmd -> cmd.getCommandData().getName()).toList());
            String commandNameStr = String.join(", ", commandNames);
            Logger.info(Logger.Type.COMMANDS_QUEUED, "Queued %s command(s) in guild %s: %s",
                    slashCommands.size() + contextCommands.size(), guild.getName(), commandNameStr);
        }
    }

    /**
     * Uses the provided {@link Set} of {@link SlashCommand} and {@link ContextCommand ContextCommandData}
     * and queues them globally, using the {@link JDA} instance.
     *
     * @param jda             The {@link JDA} instance.
     * @param slashCommands   A {@link Set} of {@link SlashCommand}.
     * @param contextCommands A {@link Set} of {@link ContextCommand}.
     */
    private void upsert(@NotNull JDA jda, @NotNull Set<SlashCommand> slashCommands, @NotNull Set<ContextCommand<?>> contextCommands) {
        if (config.isDeleteUnknownCommands()) {
            update(jda, slashCommands, contextCommands);
        } else {
            slashCommands.forEach(data -> jda.upsertCommand(data.getCommandData()).queue(this::cacheCommand));
            contextCommands.forEach(data -> jda.upsertCommand(data.getCommandData()).queue(this::cacheCommand));
        }
    }

    /**
     * Uses the provided {@link Set} of {@link SlashCommand} and {@link ContextCommand ContextCommandData}
     * and queues them, using the specified {@link Guild} instance.
     * This also checks for {@link BaseApplicationCommand#getQueueableGuilds queueable guilds} and skips them if needed.
     *
     * @param guild           The {@link Guild}.
     * @param slashCommands   A {@link Set} of {@link SlashCommand}.
     * @param contextCommands A {@link Set} of {@link ContextCommand}.
     */
    private void upsert(@NotNull Guild guild, @NotNull Set<SlashCommand> slashCommands, @NotNull Set<ContextCommand<?>> contextCommands) {
        if (config.isDeleteUnknownCommands()) {
            update(guild, slashCommands, contextCommands);
        } else {
            StringBuilder commandNames = new StringBuilder();
            slashCommands.forEach(data -> {
                if (CommandUtils.shouldBeRegistered(guild, data)) {
                    guild.upsertCommand(data.getCommandData()).queue(this::cacheCommand);
                    commandNames.append(", /").append(data.getCommandData().getName());
                }
            });
            contextCommands.forEach(data -> {
                if (CommandUtils.shouldBeRegistered(guild, data)) {
                    guild.upsertCommand(data.getCommandData()).queue(this::cacheCommand);
                    commandNames.append(", ").append(data.getCommandData().getName());
                }
            });
            if (!commandNames.isEmpty()) {
                Logger.info(Logger.Type.COMMANDS_QUEUED, "Queued %s command(s) in guild %s: %s",
                        slashCommands.size() + contextCommands.size(), guild.getName(), commandNames.substring(2));
            }
        }
    }

    /**
     * Caches the provided command by adding it to the map.
     *
     * @param command The {@link Command} to be cached.
     */
    private void cacheCommand(@NotNull Command command) {
        RETRIEVED_COMMANDS.put(command.getName(), command);
    }

    /**
     * Finds all Slash Commands using the {@link ClassWalker}.
     * Loops through all classes found in the commands package that is a subclass of
     * {@link SlashCommand}.
     *
     * @param pkg The package to search for Slash Commands.
     * @throws ReflectiveOperationException If reflection fails.
     * @throws Exceptions If an error occurs during command initialization.
     */
    private void findSlashCommands(@NotNull String pkg) throws ReflectiveOperationException, Exceptions {
        ClassWalker classes = new ClassWalker(pkg);
        Set<Class<? extends SlashCommand>> subTypes = classes.getSubTypesOf(SlashCommand.class);
        for (Class<? extends SlashCommand> subType : subTypes) {
            if (ChecksUtils.checkEmptyConstructor(subType)) {
                slashCommands.add((SlashCommand) ClassUtil.getInstance(subType));
            } else {
                Logger.error("Could not initialize %s! The class MUST contain an empty public constructor.", subType.getName());
            }
        }
    }

    /**
     * Finds all Context Commands using the {@link ClassWalker}.
     * Loops through all classes found in the commands package that is a subclass of
     * {@link ContextCommand}.
     *
     * @param pkg The package to search for Context Commands.
     * @throws ReflectiveOperationException If reflection fails.
     * @throws Exceptions If an error occurs during command initialization.
     */
    private void findContextCommands(@NotNull String pkg) throws ReflectiveOperationException, Exceptions {
        ClassWalker classes = new ClassWalker(pkg);
        Set<Class<? extends ContextCommand>> subTypes = classes.getSubTypesOf(ContextCommand.class);
        for (Class<? extends ContextCommand> subType : subTypes) {
            if (ChecksUtils.checkEmptyConstructor(subType)) {
                contextCommands.add((ContextCommand<?>) ClassUtil.getInstance(subType));
            } else {
                Logger.error("Could not initialize %s! The class MUST contain an empty public constructor.", subType.getName());
            }
        }
    }

    /**
     * Gets all Slash Commands that were found in {@link InteractionHandler#findSlashCommands(String)} and adds
     * them to the {@link InteractionHandler#slashCommandIndex}.
     *
     * @return A set of {@link SlashCommand}.
     */
    @NotNull
    public Set<SlashCommand> getSlashCommands() {
        Set<SlashCommand> commands = new HashSet<>();
        for (SlashCommand command : this.slashCommands) {
            if (command != null) {
                SlashCommandData data = getBaseCommandData(command);
                command.setCommandData(data);
                if (command.getRegistrationType() != RegistrationType.GUILD && command.getQueueableGuilds().length != 0) {
                    throw new UnsupportedOperationException(command.getClass().getName() + " attempted to require guilds for a non-global command!");
                }
                searchForAutoCompletable(command, command.getClass());
                commands.add(command);
            }
        }
        return commands;
    }

    /**
     * Searches for {@link SlashCommand}s or {@link SlashCommand.Subcommand}s which implement the {@link AutoCompletableHandler} interface.
     *
     * @param command The base {@link SlashCommand}.
     * @param clazz   The command's class.
     */
    private void searchForAutoCompletable(@NotNull SlashCommand command, @NotNull Class<? extends SlashCommand> clazz) {
        // check base command
        String baseName = command.getCommandData().getName();
        if (ChecksUtils.checkImplementation(clazz, AutoCompletableHandler.class)) {
            autoCompleteIndex.put(baseName, (AutoCompletableHandler) command);
        }
        // check subcommands
        for (SlashCommand.Subcommand child : command.getSubcommands()) {
            if (ChecksUtils.checkImplementation(child.getClass(), AutoCompletableHandler.class)) {
                autoCompleteIndex.put(CommandUtils.buildCommandPath(baseName, child.getCommandData().getName()), (AutoCompletableHandler) child);
            }
        }
        // check subcommand groups
        for (SlashCommand.SubcommandGroup childGroup : command.getSubcommandGroups()) {
            String groupName = childGroup.getData().getName();
            // check subcommands
            for (SlashCommand.Subcommand child : childGroup.getSubcommands()) {
                if (ChecksUtils.checkImplementation(child.getClass(), AutoCompletableHandler.class)) {
                    autoCompleteIndex.put(CommandUtils.buildCommandPath(baseName, groupName, child.getCommandData().getName()), (AutoCompletableHandler) child);
                }
            }
        }
    }

    /**
     * Gets the complete {@link SlashCommandData} (including Subcommands & Subcommand Groups) from a single {@link SlashCommand}.
     *
     * @param command The base command's instance.
     * @return The new {@link CommandListUpdateAction}.
     */
    @NotNull
    private SlashCommandData getBaseCommandData(@NotNull SlashCommand command) {
        // find component (and modal) handlers
        SlashCommandData commandData = command.getCommandData();
        if (command.getSubcommandGroups().length != 0) {
            commandData.addSubcommandGroups(getSubcommandGroupData(command));
        }
        if (command.getSubcommands().length != 0) {
            commandData.addSubcommands(getSubcommandData(command, command.getSubcommands(), null));
        }
        if (command.getSubcommandGroups().length == 0 && command.getSubcommands().length == 0) {
            slashCommandIndex.put(CommandUtils.buildCommandPath(commandData.getName()), command);
            Logger.info(Logger.Type.SLASH_COMMAND_REGISTERED, "\t[*] Registered command: /%s (%s)", command.getCommandData().getName(), command.getRegistrationType().name());
        }
        return commandData;
    }

    /**
     * Gets all {@link SubcommandGroupData} (including Subcommands) from a single {@link SlashCommand}.
     *
     * @param command The base command's instance.
     * @return All {@link SubcommandGroupData} stored in a Set.
     */
    @NotNull
    private Set<SubcommandGroupData> getSubcommandGroupData(@NotNull SlashCommand command) {
        Set<SubcommandGroupData> groupDataList = new HashSet<>();
        for (SlashCommand.SubcommandGroup group : command.getSubcommandGroups()) {
            if (group != null) {
                SubcommandGroupData groupData = group.getData();
                if (group.getSubcommands().length == 0) {
                    Logger.warn("SubcommandGroup %s is missing Subcommands. It will be ignored.", groupData.getName());
                    continue;
                }
                groupData.addSubcommands(getSubcommandData(command, group.getSubcommands(), groupData.getName()));
                groupDataList.add(groupData);
            }
        }
        return groupDataList;
    }

    /**
     * Gets all {@link SubcommandData} from the given array of {@link SlashCommand.Subcommand} classes.
     *
     * @param command      The base command's instance.
     * @param subcommands  All subcommand classes.
     * @param subGroupName The Subcommand Group's name. (if available)
     * @return The new {@link CommandListUpdateAction}.
     */
    @NotNull
    private Set<SubcommandData> getSubcommandData(@NotNull SlashCommand command, @NotNull SlashCommand.Subcommand[] subcommands,
                                                  @Nullable String subGroupName) {
        Set<SubcommandData> subDataList = new HashSet<>();
        for (SlashCommand.Subcommand subcommand : subcommands) {
            if (subcommand != null) {
                String commandPath;
                if (subGroupName == null) {
                    commandPath = CommandUtils.buildCommandPath(command.getCommandData().getName(), subcommand.getCommandData().getName());
                } else {
                    commandPath = CommandUtils.buildCommandPath(command.getCommandData().getName(), subGroupName, subcommand.getCommandData().getName());
                }
                subcommandIndex.put(commandPath, subcommand);
                Logger.info(Logger.Type.SLASH_COMMAND_REGISTERED, "\t[*] Registered command: /%s (%s)", commandPath, command.getRegistrationType().name());
                subDataList.add(subcommand.getCommandData());
            }
        }
        return subDataList;
    }

    /**
     * Gets all Guild Context commands registered in {@link InteractionHandler#findContextCommands(String)} and
     * returns their {@link CommandData} as a Set.
     *
     * @return A set of {@link ContextCommand}.
     */
    @NotNull
    private Set<ContextCommand<?>> getContextCommandData() {
        Set<ContextCommand<?>> commands = new HashSet<>();
        for (ContextCommand<?> context : contextCommands) {
            if (context != null) {
                CommandData data = getContextCommandData(context);
                if (data != null) {
                    context.setCommandData(data);
                }
                if (context.getRegistrationType() != RegistrationType.GUILD && context.getQueueableGuilds().length != 0) {
                    throw new UnsupportedOperationException(context.getClass().getName() + " attempted to require guilds for a non-global command!");
                }
                commands.add(context);
            }
        }
        return commands;
    }

    /**
     * Gets the complete {@link CommandData} from a single {@link ContextCommand}.
     *
     * @param command The base context command's instance.
     * @return The new {@link CommandListUpdateAction}.
     */
    @Nullable
    private CommandData getContextCommandData(@NotNull ContextCommand<?> command) {
        CommandData data = command.getCommandData();
        if (data.getType() == Command.Type.MESSAGE) {
            messageContextIndex.put(data.getName(), (ContextCommand.Message) command);
        } else if (command.getCommandData().getType() == Command.Type.USER) {
            userContextIndex.put(data.getName(), (ContextCommand.User) command);
        } else {
            Logger.error("Invalid Command Type \"%s\" for Context Command! This command will be ignored.", data.getType());
            return null;
        }
        Logger.info(Logger.Type.CONTEXT_COMMAND_REGISTERED, "\t[*] Registered context command: %s (%s)", data.getName(), command.getRegistrationType().name());
        return data;
    }

    /**
     * Handles a single {@link SlashCommand} or {@link SlashCommand.Subcommand}.
     * If a {@link SlashCommandInteractionEvent} is fired, the corresponding class is found and the command is executed.
     *
     * @param event The {@link SlashCommandInteractionEvent} that was fired.
     * @throws CommandNotRegisteredException If the command is not registered.
     */
    private void handleSlashCommand(@NotNull SlashCommandInteractionEvent event) throws CommandNotRegisteredException {
        SlashCommand slashcommand = slashCommandIndex.get(event.getFullCommandName());
        SlashCommand.Subcommand subcommand = subcommandIndex.get(event.getFullCommandName());
        if (slashcommand == null && subcommand == null) {
            if (config.isThrowUnregisteredException()) {
                throw new CommandNotRegisteredException(String.format("Slash Command \"%s\" is not registered.", event.getFullCommandName()));
            }
        } else {
            // check for parent if command is subcommand
            if (slashcommand == null) {
                BaseApplicationCommand<SlashCommandInteractionEvent, ?> base = subcommand.getParent();
                if (base != null) {
                    if (passesRequirements(event, base, base.getRegistrationType()) && passesRequirements(event, subcommand, base.getRegistrationType())) {
                        subcommand.execute(event);
                    }
                }
            } else if (passesRequirements(event, slashcommand, slashcommand.getRegistrationType())) {
                slashcommand.execute(event);
            }
        }
    }

    /**
     * Handles a single {@link ContextCommand.User}.
     * If a {@link UserContextInteractionEvent} is fired, the corresponding class is found and the command is executed.
     *
     * @param event The {@link UserContextInteractionEvent} that was fired.
     * @throws CommandNotRegisteredException If the command is not registered.
     */
    private void handleUserContextCommand(@NotNull UserContextInteractionEvent event) throws CommandNotRegisteredException {
        ContextCommand.User context = userContextIndex.get(event.getFullCommandName());
        if (context == null) {
            if (config.isThrowUnregisteredException()) {
                throw new CommandNotRegisteredException(String.format("Context Command \"%s\" is not registered.", event.getFullCommandName()));
            }
        } else {
            if (passesRequirements(event, context, context.getRegistrationType())) {
                context.execute(event);
            }
        }
    }

    /**
     * Handles a single {@link ContextCommand.Message}.
     * If a {@link MessageContextInteractionEvent} is fired, the corresponding class is found and the command is executed.
     *
     * @param event The {@link MessageContextInteractionEvent} that was fired.
     * @throws CommandNotRegisteredException If the command is not registered.
     */
    private void handleMessageContextCommand(@NotNull MessageContextInteractionEvent event) throws CommandNotRegisteredException {
        ContextCommand.Message context = messageContextIndex.get(event.getFullCommandName());
        if (context == null) {
            if (config.isThrowUnregisteredException()) {
                throw new CommandNotRegisteredException(String.format("Context Command \"%s\" is not registered.", event.getFullCommandName()));
            }
        } else {
            if (passesRequirements(event, context, context.getRegistrationType())) {
                context.execute(event);
            }
        }
    }

    /**
     * Checks if the given {@link CommandInteraction} passes the
     * {@link RestrictedCommand} requirements.
     * If not, this will then fire the corresponding event using {@link Event#fire(Event)}
     *
     * @param interaction The {@link CommandInteraction}.
     * @param command     The {@link RestrictedCommand} which contains the (possible) restrictions.
     * @param type        The {@link RegistrationType} of the {@link BaseApplicationCommand}.
     * @return Whether the event was fired.
     */
    private boolean passesRequirements(@NotNull CommandInteraction interaction, @NotNull RestrictedCommand command,
                                       @NotNull RegistrationType type) {
        long userId = interaction.getUser().getIdLong();
        Long[] guildIds = command.getRequiredGuilds();
        Permission[] permissions = command.getRequiredPermissions();
        Long[] userIds = command.getRequiredUsers();
        Long[] roleIds = command.getRequiredRoles();
        if (type == RegistrationType.GUILD && guildIds.length != 0 && interaction.isFromGuild() &&
                !List.of(guildIds).contains(Objects.requireNonNull(interaction.getGuild()).getIdLong())) {
            Event.fire(new InvalidGuildEvent(shared, interaction, Set.of(guildIds)));
            return false;
        }
        if (permissions.length != 0 && interaction.isFromGuild() &&
                interaction.getMember() != null && !interaction.getMember().hasPermission(permissions)) {
            Event.fire(new InsufficientPermissionsEvent(shared, interaction, Set.of(permissions)));
            return false;
        }
        if (userIds.length != 0 && !List.of(userIds).contains(userId)) {
            Event.fire(new InvalidUserEvent(shared, interaction, Set.of(userIds)));
            return false;
        }
        if (interaction.isFromGuild() && interaction.getMember() != null) {
            Member member = interaction.getMember();
            if (roleIds.length != 0 && !member.getRoles().isEmpty() &&
                    member.getRoles().stream().noneMatch(r -> List.of(roleIds).contains(r.getIdLong()))) {
                Event.fire(new InvalidRoleEvent(shared, interaction, Set.of(roleIds)));
                return false;
            }
        }
        // check if the command has enabled some sort of cooldown
        if (!command.getCommandCooldown().equals(Duration.ZERO)) {
            if (command.hasCooldown(userId)) {
                Event.fire(new CommandCooldownEvent(shared, interaction, command.retrieveCooldown(userId)));
                return false;
            } else {
                command.applyCooldown(userId, Instant.now().plus(command.getCommandCooldown()));
            }
        }
        return true;
    }

    /**
     * Fired if Discord reports a {@link SlashCommandInteractionEvent}.
     *
     * @param event The {@link SlashCommandInteractionEvent} that was fired.
     */
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        CompletableFuture.runAsync(() -> {
            try {
                handleSlashCommand(event);
            } catch (Throwable e) {
                Event.fire(new CommandExceptionEvent(shared, event, e));
            }
        }, config.getExecutor());
    }

    /**
     * Fired if Discord reports a {@link UserContextInteractionEvent}.
     *
     * @param event The {@link UserContextInteractionEvent} that was fired.
     */
    @Override
    public void onUserContextInteraction(@NotNull UserContextInteractionEvent event) {
        CompletableFuture.runAsync(() -> {
            try {
                handleUserContextCommand(event);
            } catch (Throwable e) {
                Event.fire(new CommandExceptionEvent(shared, event, e));
            }
        }, config.getExecutor());
    }

    /**
     * Fired if Discord reports a {@link MessageContextInteractionEvent}.
     *
     * @param event The {@link MessageContextInteractionEvent} that was fired.
     */
    @Override
    public void onMessageContextInteraction(@NotNull MessageContextInteractionEvent event) {
        CompletableFuture.runAsync(() -> {
            try {
                handleMessageContextCommand(event);
            } catch (Throwable e) {
                Event.fire(new CommandExceptionEvent(shared, event, e));
            }
        }, config.getExecutor());
    }

    /**
     * Fired if Discord reports a {@link CommandAutoCompleteInteractionEvent}.
     *
     * @param event The {@link CommandAutoCompleteInteractionEvent} that was fired.
     */
    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        CompletableFuture.runAsync(() -> {
            try {
                AutoCompletableHandler autoComplete = autoCompleteIndex.get(event.getFullCommandName());
                if (autoComplete != null) {
                    autoComplete.handleAutoComplete(event, event.getFocusedOption());
                }
            } catch (Throwable e) {
                Event.fire(new AutoCompleteExceptionEvent(shared, event, e));
            }
        }, config.getExecutor());
    }

    /**
     * Fired if Discord reports a {@link ButtonInteractionEvent}.
     *
     * @param event The {@link ButtonInteractionEvent} that was fired.
     */
    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        CompletableFuture.runAsync(() -> {
            try {
                if (shared.getButtonMappings().length == 0) return;
                Optional<ButtonHandler> buttonOptional = Arrays.stream(shared.getButtonMappings())
                        .filter(map -> List.of(map.getIds()).contains(BuilderIdComponent.split(event.getComponentId())[0]))
                        .map(MappingIdComponent::getHandler)
                        .findFirst();
                if (buttonOptional.isEmpty()) {
                    Logger.warn(Logger.Type.BUTTON_NOT_FOUND, "Button with id \"%s\" could not be found.", event.getComponentId());
                } else {
                    buttonOptional.get().handleButton(event, event.getButton());
                }
            } catch (Throwable e) {
                Event.fire(new ComponentExceptionEvent(shared, event, e));
            }
        }, config.getExecutor());
    }

    /**
     * Fired if Discord reports a {@link StringSelectInteractionEvent}.
     *
     * @param event The {@link StringSelectInteractionEvent} that was fired.
     */
    @Override
    public void onStringSelectInteraction(@NotNull StringSelectInteractionEvent event) {
        CompletableFuture.runAsync(() -> {
            try {
                if (shared.getStringSelectMenuMappings().length == 0) return;
                Optional<StringSelectMenuHandler> selectMenuOptional = Arrays.stream(shared.getStringSelectMenuMappings())
                        .filter(map -> List.of(map.getIds()).contains(BuilderIdComponent.split(event.getComponentId())[0]))
                        .map(MappingIdComponent::getHandler)
                        .findFirst();
                if (selectMenuOptional.isEmpty()) {
                    Logger.warn(Logger.Type.SELECT_MENU_NOT_FOUND, "Select Menu with id \"%s\" could not be found.", event.getComponentId());
                } else {
                    selectMenuOptional.get().handleStringSelectMenu(event, event.getValues());
                }
            } catch (Throwable e) {
                Event.fire(new ComponentExceptionEvent(shared, event, e));
            }
        }, config.getExecutor());
    }

    /**
     * Fired if Discord reports a {@link EntitySelectInteractionEvent}.
     *
     * @param event The {@link EntitySelectInteractionEvent} that was fired.
     */
    @Override
    public void onEntitySelectInteraction(@NotNull EntitySelectInteractionEvent event) {
        CompletableFuture.runAsync(() -> {
            try {
                if (shared.getEntitySelectMenuMappings().length == 0) return;
                Optional<EntitySelectMenuHandler> selectMenuOptional = Arrays.stream(shared.getEntitySelectMenuMappings())
                        .filter(map -> List.of(map.getIds()).contains(BuilderIdComponent.split(event.getComponentId())[0]))
                        .map(MappingIdComponent::getHandler)
                        .findFirst();
                if (selectMenuOptional.isEmpty()) {
                    Logger.warn(Logger.Type.SELECT_MENU_NOT_FOUND, "Select Menu with id \"%s\" could not be found.", event.getComponentId());
                } else {
                    selectMenuOptional.get().handleEntitySelectMenu(event, event.getValues());
                }
            } catch (Throwable e) {
                Event.fire(new ComponentExceptionEvent(shared, event, e));
            }
        }, config.getExecutor());
    }

    /**
     * Fired if Discord reports a {@link ModalInteractionEvent}.
     *
     * @param event The {@link ModalInteractionEvent} that was fired.
     */
    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        CompletableFuture.runAsync(() -> {
            try {
                if (shared.getModalMappings().length == 0) return;
                Optional<ModalHandler> modalOptional = Arrays.stream(shared.getModalMappings())
                        .filter(map -> List.of(map.getIds()).contains(BuilderIdComponent.split(event.getModalId())[0]))
                        .map(MappingIdComponent::getHandler)
                        .findFirst();
                if (modalOptional.isEmpty()) {
                    Logger.warn(Logger.Type.MODAL_NOT_FOUND, "Modal with id \"%s\" could not be found.", event.getModalId());
                } else {
                    modalOptional.get().handleModal(event, event.getValues());
                }
            } catch (Throwable e) {
                Event.fire(new ModalExceptionEvent(shared, event, e));
            }
        }, config.getExecutor());
    }
}
