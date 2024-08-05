package com.infinitehorizons.utils;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * Utility class for various date and time operations.
 * <p>
 * Provides methods to format, manipulate, and compare dates and times.
 *
 * @since v0.0.1-SNAPSHOT
 */
@UtilityClass
public class DateUtil {

    private static final DateTimeFormatter simpleDateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS Z");

    /**
     * Returns the current date and time formatted as a string in a universal format.
     *
     * @return The current date and time as a {@link String}.
     */
    public static @NotNull String getUniversalCurrentDateTime() {
        LocalDateTime current = LocalDateTime.now();
        return current.format(simpleDateFormat);
    }

    /**
     * Calculates the number of seconds between the given date and the Unix epoch.
     *
     * @param dateTime The {@link LocalDateTime} to calculate the seconds from the epoch.
     * @return The number of seconds since the Unix epoch.
     */
    public static @NotNull Long secondsFromOrigin(LocalDateTime dateTime) {
        LocalDateTime origin = LocalDateTime.of(1970, 1, 1, 0, 0, 0);
        return ChronoUnit.SECONDS.between(origin, dateTime);
    }

    /**
     * Calculates the number of seconds between the current time and the specified time.
     *
     * @param dateTime The {@link LocalDateTime} to calculate the seconds from now.
     * @return The number of seconds between now and the given time.
     */
    public static @NotNull Long seconds(LocalDateTime dateTime) {
        return ChronoUnit.SECONDS.between(LocalDateTime.now(), dateTime);
    }

    /**
     * Calculates the number of seconds remaining from now until the specified time.
     *
     * @param dateTime The {@link LocalDateTime} to calculate the remaining seconds until.
     * @return The number of seconds remaining until the given time.
     */
    public static @NotNull Long remains(LocalDateTime dateTime) {
        return ChronoUnit.SECONDS.between(LocalDateTime.now(), dateTime);
    }

    /**
     * Calculates the number of milliseconds remaining from now until the specified time.
     *
     * @param dateTime The {@link LocalDateTime} to calculate the remaining milliseconds until.
     * @return The number of milliseconds remaining until the given time.
     */
    public static @NotNull Long remainsMillis(LocalDateTime dateTime) {
        return ChronoUnit.MILLIS.between(LocalDateTime.now(), dateTime);
    }

    /**
     * Returns a {@link LocalDateTime} that is offset by a specified number of milliseconds from now.
     *
     * @param offsetMillis The number of milliseconds to offset from now.
     * @return The resulting {@link LocalDateTime}.
     */
    public static @NotNull LocalDateTime offsetMillis(long offsetMillis) {
        return LocalDateTime.now().plus(offsetMillis, ChronoUnit.MILLIS);
    }

    /**
     * Returns a {@link LocalDateTime} that is offset by a specified number of milliseconds from now.
     *
     * @param offsetMillis The number of milliseconds to offset from now.
     * @return The resulting {@link LocalDateTime}.
     */
    public static @NotNull LocalDateTime nowMillis(int offsetMillis) {
        return LocalDateTime.now().plus(offsetMillis, ChronoUnit.MILLIS);
    }

    /**
     * Returns a {@link LocalDateTime} that is offset by a specified number of years from now.
     *
     * @param offsetYears The number of years to offset from now.
     * @return The resulting {@link LocalDateTime}.
     */
    public static @NotNull LocalDateTime offsetYears(int offsetYears) {
        return LocalDateTime.now().plusYears(offsetYears);
    }

    /**
     * Returns a {@link LocalDateTime} that is offset by a specified number of seconds from now.
     *
     * @param offsetSeconds The number of seconds to offset from now.
     * @return The resulting {@link LocalDateTime}.
     */
    public static @NotNull LocalDateTime now(int offsetSeconds) {
        return LocalDateTime.now().plusSeconds(offsetSeconds);
    }

    /**
     * Returns a {@link LocalDateTime} that is offset by a specified number of seconds from the given date and time.
     *
     * @param dateTime The {@link LocalDateTime} to offset.
     * @param offsetSeconds The number of seconds to offset from the given date and time.
     * @return The resulting {@link LocalDateTime}.
     */
    @Contract(pure = true)
    public static @NotNull LocalDateTime offset(@NotNull LocalDateTime dateTime, int offsetSeconds) {
        return dateTime.plusSeconds(offsetSeconds);
    }

    /**
     * Checks if the given date is the same as the current date.
     *
     * @param date The {@link LocalDateTime} to check.
     * @return {@code true} if the given date is the same as the current date, {@code false} otherwise.
     */
    public static boolean currentDay(LocalDateTime date) {
        return sameDay(now(), date);
    }

    /**
     * Checks if two dates are on the same day.
     *
     * @param date1 The first {@link LocalDateTime} to compare.
     * @param date2 The second {@link LocalDateTime} to compare.
     * @return {@code true} if both dates are on the same day, {@code false} otherwise.
     */
    public static boolean sameDay(@NotNull LocalDateTime date1, @NotNull LocalDateTime date2) {
        return date1.toLocalDate().isEqual(date2.toLocalDate());
    }

    /**
     * Converts a {@link Date} to a {@link LocalDateTime}.
     *
     * @param date The {@link Date} to convert.
     * @return The resulting {@link LocalDateTime}.
     */
    public static LocalDateTime localDateTime(@NotNull Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * Converts a timestamp in milliseconds to a {@link LocalDateTime}.
     *
     * @param millis The number of milliseconds since the epoch.
     * @return The resulting {@link LocalDateTime}.
     */
    @Contract("_ -> new")
    public static @NotNull LocalDateTime localDateTime(long millis) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault());
    }

    /**
     * Converts a timestamp in milliseconds to a {@link LocalDateTime}.
     *
     * @param millis The number of milliseconds since the epoch.
     * @return The resulting {@link LocalDateTime}.
     */
    @Contract("_ -> new")
    public static @NotNull LocalDateTime date(long millis) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault());
    }

    /**
     * Returns the current {@link LocalDateTime}.
     *
     * @return The current {@link LocalDateTime}.
     */
    @Contract(" -> new")
    public static @NotNull LocalDateTime now() {
        return LocalDateTime.now();
    }

    /**
     * Returns the current time in milliseconds since the Unix epoch.
     *
     * @return The current time in milliseconds.
     */
    public static @NotNull Long millis() {
        return System.currentTimeMillis();
    }

}
