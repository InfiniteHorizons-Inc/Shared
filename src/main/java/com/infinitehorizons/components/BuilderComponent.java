package com.infinitehorizons.components;

import com.infinitehorizons.Shared;
import com.infinitehorizons.config.ConfigLoader;
import com.infinitehorizons.exceptions.Exceptions;
import com.infinitehorizons.exceptions.InvalidPackageException;
import com.infinitehorizons.utils.ClassWalker;
import com.infinitehorizons.utils.ClasspathUtil;
import com.infinitehorizons.utils.Logger;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.dv8tion.jda.api.JDA;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Builder class used to instantiate a new {@link Shared} instance.
 *
 * <p>This builder provides a fluent interface for configuring and creating a {@code Shared} instance.
 * It offers various configuration options such as setting the JDA instance, specifying command packages,
 * customizing executors, and more.</p>
 *
 * @see Shared
 */
@Getter
@Setter
@Accessors(chain = true, fluent = true)
public class BuilderComponent {

    private final JDA jda;
    private final ConfigLoader config;

    /**
     * Creates a new builder instance for the specified JDA instance.
     *
     * @param jda The JDA instance to be used by Shared.
     */
    private BuilderComponent(@NotNull JDA jda) {
        this.config = new ConfigLoader();
        this.jda = jda;
    }

    /**
     * Initializes the builder with the specified JDA instance.
     *
     * @param instance The JDA instance to use.
     * @return A new instance of BuilderComponent for chaining convenience.
     */
    @NotNull
    public static BuilderComponent setJDA(@NotNull JDA instance) {
        return new BuilderComponent(instance);
    }

    /**
     * Specifies packages containing application command classes. Shared will use the {@link ClassWalker} to scan for these classes.
     *
     * @param packages The packages to scan for command classes.
     * @return The BuilderComponent instance for chaining convenience.
     */
    @NotNull
    public BuilderComponent setCommandPackages(@NotNull String... packages) {
        config.setCommandsPackages(packages);
        return this;
    }

    /**
     * Sets a custom executor for handling command and event executions.
     *
     * <p>Using Java 21 or later, it is recommended to use {@link Executors#newVirtualThreadPerTaskExecutor()}.</p>
     *
     * @param executor The custom executor.
     * @return The BuilderComponent instance for chaining convenience.
     */
    @NotNull
    public BuilderComponent setExecutor(@NotNull Executor executor) {
        config.setExecutor(executor);
        return this;
    }

    /**
     * Sets the types of logging to be disabled.
     *
     * @param types The logging types to disable.
     * @return The BuilderComponent instance for chaining convenience.
     */
    @NotNull
    public BuilderComponent disableLogging(@NotNull Logger.Type... types) {
        Logger.Type[] blocked = types.length < 1 ? Logger.Type.values() : types;
        return this;
    }

    /**
     * Disables stack trace printing for exceptions not caught by an event listener.
     *
     * @return The BuilderComponent instance for chaining convenience.
     */
    @NotNull
    public BuilderComponent disableStacktracePrinting() {
        config.setDefaultPrintStacktrace(false);
        return this;
    }

    /**
     * Disables automatic command registration on each onReady event.
     *
     * <p>Manual registration can be performed using {@link Shared#registerInteractions()}.</p>
     *
     * @return The BuilderComponent instance for chaining convenience.
     */
    @NotNull
    public BuilderComponent disableAutomaticCommandRegistration() {
        config.setRegisterOnReady(false);
        return this;
    }

    /**
     * Configures global Smart Queueing for command updates.
     *
     * <p>When disabled, global commands are overridden on each registration, causing potential downtime.</p>
     *
     * @param enable Whether to enable Smart Queueing for global commands.
     * @return The BuilderComponent instance for chaining convenience.
     */
    @NotNull
    public BuilderComponent setGlobalSmartQueue(boolean enable) {
        config.setGlobalSmartQueue(enable);
        return this;
    }

    /**
     * Configures guild Smart Queueing for command updates.
     *
     * <p>Recommended to disable for 300+ servers to reduce start-up time.</p>
     *
     * @param enable Whether to enable Smart Queueing for guild commands.
     * @return The BuilderComponent instance for chaining convenience.
     */
    @NotNull
    public BuilderComponent setGuildSmartQueue(boolean enable) {
        config.setGuildSmartQueue(enable);
        return this;
    }

    /**
     * Disables the deletion of unknown or unused commands when using Smart Queue.
     *
     * @return The BuilderComponent instance for chaining convenience.
     */
    @NotNull
    public BuilderComponent disableUnknownCommandDeletion() {
        config.setDeleteUnknownCommands(false);
        return this;
    }

    /**
     * Disables the exception thrown for unregistered commands.
     *
     * @return The BuilderComponent instance for chaining convenience.
     */
    @NotNull
    public BuilderComponent disableUnregisteredCommandException() {
        config.setThrowUnregisteredException(false);
        return this;
    }

    /**
     * Builds and returns a validated Shared instance.
     *
     * @return The constructed Shared instance.
     * @throws Exceptions If the configuration is invalid.
     */
    @NotNull
    public Shared build() throws Exceptions {
        if (Runtime.getRuntime().availableProcessors() == 1) {
            Logger.warn("Running Shared on a single-core CPU. Asynchronous command execution disabled.");
            System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "1");
        }
        for (String pkg : config.getCommandsPackages()) {
            if (pkg.isBlank() || pkg.isEmpty()) {
                throw new InvalidPackageException("Commands package cannot be empty or blank.");
            }
            if (ClasspathUtil.forPackage(pkg).isEmpty()) {
                throw new InvalidPackageException("Package '" + pkg + "' does not exist.");
            }
        }
        config.setJda(jda);
        return new Shared(config);
    }

}
