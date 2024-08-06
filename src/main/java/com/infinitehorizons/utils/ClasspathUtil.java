package com.infinitehorizons.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;

/**
 * Utility class for finding classes on the classpath.
 *
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ClasspathUtil {

    /**
     * Retrieves all {@link URL}s in the specified package.
     *
     * @param packageName The name of the package to search.
     * @return A {@link Collection} of {@link URL}s corresponding to the package name.
     * @throws UncheckedIOException if there is an error accessing resources.
     */
    @NotNull
    public static Collection<URL> forPackage(@NotNull String packageName) {
        List<URL> results = new ArrayList<>();
        ClassLoader loader = IoUtils.getClassLoaderForClass(ClasspathUtil.class);

        try {
            Enumeration<URL> urls = loader.getResources(getResourceName(packageName));
            while (urls.hasMoreElements()) {
                results.add(urls.nextElement());
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to retrieve resources for package: " + packageName, e);
        }
        return results;
    }

    /**
     * Formats and validates the resource path to ensure compatibility with jar files.
     *
     * @param name The path of the resource.
     * @return The formatted resource path.
     */
    @NotNull
    private static String getResourceName(@NotNull String name) {
        String resource = name.replace(".", "/").replace("\\", "/");
        return resource.startsWith("/") ? resource.substring(1) : resource;
    }

}
