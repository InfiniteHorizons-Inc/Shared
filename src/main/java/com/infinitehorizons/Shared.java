package com.infinitehorizons;

import com.infinitehorizons.commands.ApplicationCommand;
import com.infinitehorizons.commands.BaseApplicationCommand;
import com.infinitehorizons.commands.ContextCommand;
import com.infinitehorizons.commands.SlashCommand;
import com.infinitehorizons.components.BuilderComponent;
import com.infinitehorizons.components.BuilderIdComponent;
import com.infinitehorizons.components.MappingIdComponent;
import com.infinitehorizons.config.ConfigLoader;
import com.infinitehorizons.enums.RegistrationType;
import com.infinitehorizons.events.EventListener;
import com.infinitehorizons.exceptions.Exceptions;
import com.infinitehorizons.handler.*;
import com.infinitehorizons.utils.ChecksUtils;
import lombok.AccessLevel;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * {@code Shared} is a framework for managing Discord interactions through JDA.
 * It supports automatic and manual registration of slash commands, context menus,
 * and component handlers such as buttons, select menus, and modals.
 * <p>
 * <b>Getting Started:</b>
 * <pre>{@code
 * Shared shared = BuilderComponent
 *         .setJDA(jda) // Your JDA instance
 *         .build();
 * }</pre>
 * <p>
 * <b>Manual Command Registration:</b>
 * <pre>{@code
 * Shared shared = BuilderComponent
 *         .setJDA(jda) // Your JDA instance
 *         .build();
 * shared.addSlashCommands(new PingCommand(), new HelloWorldCommand());
 * shared.addContextMenus(new PingUserContext(), new HelloWorldMessageContext());
 * }</pre>
 * <p>
 * <b>Automatic Command Registration:</b>
 * <pre>{@code
 * Shared shared = BuilderComponent
 *         .setJDA(jda) // Your JDA instance
 *         .setCommandPackages("com.infinitehorizons.bot.commands") // OPTIONAL: The package(s) containing all your commands
 *         .build();
 * }</pre>
 * When the build method is called, all commands in the specified packages are registered.
 *
 * @since v2.2.2-SNAPSHOT
 */
public class Shared extends ListenerAdapter {

    /**
     * The default {@link RegistrationType} used for queuing new commands.
     * This can be overridden using {@link BaseApplicationCommand#setRegistrationType(RegistrationType)}.
     */
    @Getter(AccessLevel.PUBLIC)
    private static RegistrationType defaultRegistrationType = RegistrationType.GLOBAL;

    // Component Handlers
    @Getter(AccessLevel.PUBLIC)
    private MappingIdComponent<ButtonHandler>[] buttonMappings = null;

    @Getter(AccessLevel.PUBLIC)
    private MappingIdComponent<StringSelectMenuHandler>[] stringSelectMenuMappings = null;

    @Getter(AccessLevel.PUBLIC)
    private MappingIdComponent<EntitySelectMenuHandler>[] entitySelectMenuMappings = null;

    @Getter(AccessLevel.PUBLIC)
    private MappingIdComponent<ModalHandler>[] modalMappings = null;

    /**
     * The {@link ConfigLoader} instance linked to this specific {@link Shared} instance.
     */
    @Getter(AccessLevel.PUBLIC)
    private final ConfigLoader config;

    /**
     * A set of all {@link EventListener}s registered to this instance.
     */
    @Getter(AccessLevel.PUBLIC)
    private final Set<EventListener> eventListeners;

    private final InteractionHandler handler;

    /**
     * Constructs a new {@code Shared} instance using the specified {@link ConfigLoader}.
     * It is <b>highly recommended</b> to use the {@link BuilderComponent} instead.
     *
     * @param config The instance's {@link ConfigLoader configuration}.
     * @throws Exceptions if the given {@link ConfigLoader} is invalid.
     *
     * @since v2.2.2-SNAPSHOT
     */
    public Shared(@NotNull ConfigLoader config) throws Exceptions {
        validateConfig(config);
        this.config = config;
        this.handler = new InteractionHandler(this);
        this.config.getJda().addEventListener(this, handler);
        this.eventListeners = new HashSet<>();
    }

    /**
     * Fired once the {@link JDA} instance fires the {@link ReadyEvent}.
     * Registers interactions if configured to do so.
     *
     * @param event The {@link ReadyEvent} that was fired.
     *
     * @since v2.2.2-SNAPSHOT
     */
    @Override
    public void onReady(@NotNull ReadyEvent event) {
        if (config.isRegisterOnReady() && handler != null) {
            handler.registerInteractions();
        }
    }

    /**
     * Sets the default {@link RegistrationType} for all
     * {@link ApplicationCommand Application Commands}.
     * Defaults to {@link RegistrationType#GLOBAL}.
     *
     * @param type The {@link RegistrationType}.
     *
     * @since v2.2.2-SNAPSHOT
     */
    public static void setDefaultRegistrationType(@NotNull RegistrationType type) {
        Shared.defaultRegistrationType = type;
    }

    /**
     * Registers all interactions and replaces old ones.
     * Note that global commands may take up to an hour to register.
     *
     * @since v2.2.2-SNAPSHOT
     */
    public void registerInteractions() {
        if (handler != null) {
            handler.registerInteractions();
        }
    }

    /**
     * Registers {@link EventListener event listener} classes.
     *
     * @param classes Implementations of {@link EventListener}.
     * @since v2.2.2-SNAPSHOT
     */
    public void addEventListener(@NotNull Object... classes) {
        for (Object o : classes) {
            try {
                EventListener adapter = (EventListener) o;
                eventListeners.add(adapter);
            } catch (ClassCastException e) {
                throw new IllegalArgumentException("Listener classes must implement EventListener!");
            }
        }
    }

    /**
     * Gets the {@link JDA} instance {@link Shared} uses.
     *
     * @return The {@link JDA} instance.
     *
     * @since v2.2.2-SNAPSHOT
     */
    @NotNull
    public JDA getJDA() {
        return config.getJda();
    }

    /**
     * Manually registers {@link SlashCommand}s.
     *
     * @param commands An array of commands to register.
     */
    public void addSlashCommands(@NotNull SlashCommand... commands) {
        handler.getSlashCommands().addAll(List.of(commands));
    }

    /**
     * Manually registers {@link ContextCommand}s.
     *
     * @param commands An array of commands to register.
     */
    public void addContextCommands(@NotNull ContextCommand<?>... commands) {
        handler.getContextCommands().addAll(List.of(commands));
    }

    /**
     * Binds all {@link ButtonHandler}s to their IDs using {@link MappingIdComponent}.
     * <pre>{@code
     * shared.addButtonMappings(
     *     MappingIdComponent.of(new PingCommand(), "ping", "hello-world"),
     *     MappingIdComponent.of(new DummyCommand(), "dummy", "test")
     * );
     * }</pre>
     * Best used with {@link BuilderIdComponent#build(String, Object...)}.
     *
     * @param mappings All {@link ButtonHandler}, as an array of {@link MappingIdComponent}.
     */
    @SafeVarargs
    public final void addButtonMappings(@NotNull MappingIdComponent<ButtonHandler>... mappings) {
        validateMappings(mappings);
        this.buttonMappings = mappings;
    }

    /**
     * Binds all {@link StringSelectMenuHandler}s to their IDs.
     * <pre>{@code
     * shared.addStringSelectMenuMappings(
     *     MappingIdComponent.of(new PingCommand(), "ping", "hello-world"),
     *     MappingIdComponent.of(new DummyCommand(), "dummy", "test")
     * );
     * }</pre>
     * Best used with {@link BuilderIdComponent#build(String, Object...)}.
     *
     * @param mappings All {@link StringSelectMenuHandler}, as an array of {@link MappingIdComponent}.
     * @see Shared#addEntitySelectMenuMappings(MappingIdComponent[])
     */
    @SafeVarargs
    public final void addStringSelectMenuMappings(@NotNull MappingIdComponent<StringSelectMenuHandler>... mappings) {
        validateMappings(mappings);
        this.stringSelectMenuMappings = mappings;
    }

    /**
     * Binds all {@link EntitySelectMenuHandler}s to their IDs.
     * <pre>{@code
     * shared.addEntitySelectMenuMappings(
     *     MappingIdComponent.of(new PingCommand(), "ping", "hello-world"),
     *     MappingIdComponent.of(new DummyCommand(), "dummy", "test")
     * );
     * }</pre>
     * Best used with {@link BuilderIdComponent#build(String, Object...)}.
     *
     * @param mappings All {@link EntitySelectMenuHandler}, as an array of {@link MappingIdComponent}.
     * @see Shared#addStringSelectMenuMappings(MappingIdComponent[])
     */
    @SafeVarargs
    public final void addEntitySelectMenuMappings(@NotNull MappingIdComponent<EntitySelectMenuHandler>... mappings) {
        validateMappings(mappings);
        this.entitySelectMenuMappings = mappings;
    }

    /**
     * Binds all {@link ModalHandler}s to their IDs.
     * <pre>{@code
     * shared.addModalMappings(
     *     MappingIdComponent.of(new PingCommand(), "ping", "hello-world"),
     *     MappingIdComponent.of(new DummyCommand(), "dummy", "test")
     * );
     * }</pre>
     * Best used with {@link BuilderIdComponent#build(String, Object...)}.
     *
     * @param mappings All {@link ModalHandler}, as an array of {@link MappingIdComponent}.
     */
    @SafeVarargs
    public final void addModalMappings(@NotNull MappingIdComponent<ModalHandler>... mappings) {
        validateMappings(mappings);
        this.modalMappings = mappings;
    }

    /**
     * Validates the specified {@link MappingIdComponent}s, throwing an {@link IllegalArgumentException}
     * if they're invalid.
     *
     * @param mappings The {@link MappingIdComponent}-array to validate.
     * @param <T> The mappings' type.
     */
    @SafeVarargs
    private <T> void validateMappings(@NotNull MappingIdComponent<T>... mappings) {
        for (MappingIdComponent<T> mapping : mappings) {
            if (mapping.getIds().length == 0) {
                throw new IllegalArgumentException("IDs may not be empty or null!");
            }
        }
    }

    /**
     * Validates the specified {@link ConfigLoader}, throwing an {@link IllegalArgumentException}
     * if the config is invalid.
     *
     * @param config The {@link ConfigLoader} to validate.
     * @throws IllegalArgumentException If the config is invalid.
     */
    private void validateConfig(@NotNull ConfigLoader config) {
        ChecksUtils.notNull(config.getJda(), "JDA instance");
        ChecksUtils.notNull(config.getCommandsPackages(), "Command Packages");
        ChecksUtils.notNull(config.getExecutor(), "Executor");
    }
}
