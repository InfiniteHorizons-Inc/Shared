package com.infinitehorizons.utils;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

/**
 * Utility class providing methods related to Input/Output operations.
 *
 * <p>This class is designed to provide convenience methods for common IO tasks,
 * such as retrieving the appropriate class loader for a given class.</p>
 *
 */
@UtilityClass
public class IoUtils {

    /**
     * Retrieves the class loader for a specified class.
     *
     * <p>If the class loader of the given class is null, the method returns the context
     * class loader of the current thread.</p>
     *
     * @param clazz The class for which the class loader is requested.
     * @return The {@link ClassLoader} associated with the provided class, or the
     *         context class loader of the current thread if the class loader is null.
     * @since v1.6
     */
    @NotNull
    public ClassLoader getClassLoaderForClass(@NotNull Class<?> clazz) {
        return clazz.getClassLoader() != null ? clazz.getClassLoader() : Thread.currentThread().getContextClassLoader();
    }
}
