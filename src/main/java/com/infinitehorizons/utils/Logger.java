package com.infinitehorizons.utils;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.dv8tion.jda.internal.utils.JDALogger;
import org.jetbrains.annotations.NotNull;
import org.slf4j.event.Level;

/**
 * Logger for the InfiniteHorizons framework, handling various logging operations.
 * <p>
 * This logger provides methods to log messages at different levels and allows disabling specific log types.
 *
 * @since v0.0.1-SNAPSHOT
 */
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class Logger {

    static {
        JDALogger.getLog(Logger.class);
    }

    @Getter
    @Setter
    private static Type[] blockedLogTypes = new Type[]{};

    /**
     * Disables logging for the specified types.
     *
     * @param types The log {@link Type} to disable.
     */
    public static void disableLogging(@NotNull Type... types) {
        blockedLogTypes = types;
    }

    private static void logMessage(@NotNull String message, @NotNull Type type, @NotNull Level level) {
        if (ArrayUtil.contains(blockedLogTypes, type)) return;
        switch (level) {
            case INFO -> info(message);
            case WARN -> warn(message);
            case ERROR -> error(message);
            case DEBUG -> debug(message);
            case TRACE -> trace(message);
        }
    }

    /**
     * Logs an informational message with a specified {@link Type}.
     *
     * @param type    The type of the message.
     * @param message The message to log.
     * @param args    Arguments for formatting the message.
     */
    public static void info(@NotNull Type type, @NotNull String message, @NotNull Object... args) {
        logMessage(String.format(message, args), type, Level.INFO);
    }

    /**
     * Logs an informational message.
     *
     * @param message The message to log.
     * @param args    Arguments for formatting the message.
     */
    public static void info(@NotNull String message, @NotNull Object... args) {
        logMessage(String.format(message, args), Type.INFO, Level.INFO);
    }

    /**
     * Logs a warning message with a specified {@link Type}.
     *
     * @param type    The type of the message.
     * @param message The message to log.
     * @param args    Arguments for formatting the message.
     */
    public static void warn(@NotNull Type type, @NotNull String message, @NotNull Object... args) {
        logMessage(String.format(message, args), type, Level.WARN);
    }

    /**
     * Logs a warning message.
     *
     * @param message The message to log.
     * @param args    Arguments for formatting the message.
     */
    public static void warn(@NotNull String message, @NotNull Object... args) {
        logMessage(String.format(message, args), Type.WARN, Level.WARN);
    }

    /**
     * Logs an error message with a specified {@link Type}.
     *
     * @param type    The type of the message.
     * @param message The message to log.
     * @param args    Arguments for formatting the message.
     */
    public static void error(@NotNull Type type, @NotNull String message, @NotNull Object... args) {
        logMessage(String.format(message, args), type, Level.ERROR);
    }

    /**
     * Logs an error message.
     *
     * @param message The message to log.
     * @param args    Arguments for formatting the message.
     */
    public static void error(@NotNull String message, @NotNull Object... args) {
        logMessage(String.format(message, args), Type.ERROR, Level.ERROR);
    }

    /**
     * Logs a debug message with a specified {@link Type}.
     *
     * @param type    The type of the message.
     * @param message The message to log.
     * @param args    Arguments for formatting the message.
     */
    public static void debug(@NotNull Type type, @NotNull String message, @NotNull Object... args) {
        logMessage(String.format(message, args), type, Level.DEBUG);
    }

    /**
     * Logs a debug message.
     *
     * @param message The message to log.
     * @param args    Arguments for formatting the message.
     */
    public static void debug(@NotNull String message, @NotNull Object... args) {
        logMessage(String.format(message, args), Type.DEBUG, Level.DEBUG);
    }

    /**
     * Logs a trace message with a specified {@link Type}.
     *
     * @param type    The type of the message.
     * @param message The message to log.
     * @param args    Arguments for formatting the message.
     */
    public static void trace(@NotNull Type type, @NotNull String message, @NotNull Object... args) {
        logMessage(String.format(message, args), type, Level.TRACE);
    }

    /**
     * Logs a trace message.
     *
     * @param message The message to log.
     * @param args    Arguments for formatting the message.
     */
    public static void trace(@NotNull String message, @NotNull Object... args) {
        logMessage(String.format(message, args), Type.TRACE, Level.TRACE);
    }

    /**
     * Enum representing the types of logs supported by the {@link Logger}.
     */
    public enum Type {
        /** Informational messages. */
        INFO,
        /** Warning messages. */
        WARN,
        /** Error messages. */
        ERROR,
        /** Debugging messages. */
        DEBUG,
        /** Trace messages. */
        TRACE,
        /** Logs related to command queuing. */
        COMMANDS_QUEUED,
        /** Slash command registration events. */
        SLASH_COMMAND_REGISTERED,
        /** Slash command skipping events. */
        SLASH_COMMAND_SKIPPED,
        /** Context command registration events. */
        CONTEXT_COMMAND_REGISTERED,
        /** Context command skipping events. */
        CONTEXT_COMMAND_SKIPPED,
        /** Logs related to the SmartQueue. */
        SMART_QUEUE,
        /** SmartQueue ignored command events. */
        SMART_QUEUE_IGNORED,
        /** SmartQueue deleted unknown command events. */
        SMART_QUEUE_DELETED_UNKNOWN,
        /** SmartQueue ignored unknown command events. */
        SMART_QUEUE_IGNORED_UNKNOWN,
        /** Button not found events. */
        BUTTON_NOT_FOUND,
        /** Select menu not found events. */
        SELECT_MENU_NOT_FOUND,
        /** Modal not found events. */
        MODAL_NOT_FOUND,
        /** Missing event handler implementations. */
        EVENT_MISSING_HANDLER
    }
}
