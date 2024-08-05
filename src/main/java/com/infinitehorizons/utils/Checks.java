package com.infinitehorizons.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;

/**
 * Utility class for performing various checks and validations.
 * <p>
 * This class provides methods to verify implementations, constructors, and nullability of objects.
 *
 * @since v0.0.1-SNAPSHOT
 */
public class Checks {

    /**
     * Checks if the given base class implements the specified interface or class.
     *
     * @param base The base class to check.
     * @param implementation The interface or class to verify against.
     * @return {@code true} if the base class implements the specified interface or class, {@code false} otherwise.
     * @since v0.0.1-SNAPSHOT
     */
    public static boolean checkImplementation(@NotNull Class<?> base, @NotNull Class<?> implementation) {
        return ClassUtil.doesImplement(base, implementation);
    }

    /**
     * Checks if the given class has a no-argument (empty) constructor.
     *
     * @param base The class to inspect for an empty constructor.
     * @return {@code true} if the class has an empty constructor, {@code false} otherwise.
     * Logs a warning if the class does not have an empty constructor.
     * @since v0.0.1-SNAPSHOT
     */
    public static boolean checkEmptyConstructor(@NotNull Class<?> base) {
        for (Constructor<?> c : base.getConstructors()) {
            if (c.getParameterCount() == 0) return true;
        }
        Logger.warn(String.format("Class %s contains unknown constructor parameters!", base.getSimpleName()));
        return false;
    }

    /**
     * Validates that the given object is not null.
     *
     * @param argument The object to check for nullability.
     * @param name     The name of the object, used in the exception message if the object is null.
     * @throws IllegalArgumentException if the given argument is null.
     * @since v0.0.1-SNAPSHOT
     */
    public static void notNull(@Nullable final Object argument, @NotNull final String name) {
        if (argument == null) {
            throw new IllegalArgumentException(name + " may not be null");
        }
    }
}
