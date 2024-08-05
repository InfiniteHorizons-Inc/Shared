package com.infinitehorizons.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.List;

/**
 * Utility class for performing reflection-based operations on classes.
 * <p>
 * Provides methods for checking class implementations and instantiating classes dynamically.
 *
 * @since v0.0.1-SNAPSHOT
 */
@Getter
@Setter
@NoArgsConstructor
public class ClassUtil {

    /**
     * Determines if the specified base class implements or extends the given implementation class or interface.
     *
     * @param base           The base class to check.
     * @param implementation The interface or class to verify against.
     * @return {@code true} if the base class implements or extends the given implementation; {@code false} otherwise.
     * @since v0.0.1-SNAPSHOT
     */
    public static boolean doesImplement(@NotNull Class<?> base, @NotNull Class<?> implementation) {
        return implementation.isAssignableFrom(base);
    }

    /**
     * Creates a new instance of the specified class using its no-argument constructor.
     * <p>
     * If the class is abstract or no accessible no-argument constructor exists, the method returns {@code null}.
     *
     * @param clazz The class to instantiate.
     * @return A new instance of the class as a generic {@link Object}, or {@code null} if instantiation is not possible.
     * @throws ReflectiveOperationException If an error occurs during instantiation.
     * @since v0.0.1-SNAPSHOT
     */
    @Nullable
    public static Object getInstance(@NotNull Class<?> clazz) throws ReflectiveOperationException {
        if (Modifier.isAbstract(clazz.getModifiers())) {
            return null;
        }
        for (Constructor<?> constructor : clazz.getConstructors()) {
            List<Class<?>> params = List.of(constructor.getParameterTypes());
            if (params.isEmpty()) {
                return clazz.getConstructor().newInstance();
            } else {
                throw new IllegalArgumentException("Cannot access constructor with types: " + params);
            }
        }
        return null;
    }
}
