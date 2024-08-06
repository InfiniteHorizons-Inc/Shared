package com.infinitehorizons.utils;

import com.infinitehorizons.exceptions.Exceptions;
import com.infinitehorizons.exceptions.InvalidPackageException;
import com.infinitehorizons.exceptions.ReflectionException;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility class that provides functionality to retrieve all classes within a specified package.
 * This includes all classes within sub-packages and allows filtering by subclass type.
 *
 */
@Getter
public class ClassWalker {

    /**
     * The name of the package to search for classes.
     * Example: com.java.example
     */
    private final String packageName;

    /**
     * Constructs a new instance of {@code ClassWalker} for the specified package.
     *
     * @param packageName The package to perform operations on.
     */
    public ClassWalker(@NotNull String packageName) {
        this.packageName = packageName;
    }

    /**
     * Retrieves all classes within the specified package and its sub-packages.
     *
     * @return An unmodifiable {@link Set} of classes found within the package.
     * @throws Exceptions If an error occurs during reflection operations.
     */
    @NotNull
    public Set<Class<?>> getAllClasses() throws Exceptions {
        try {
            String packagePath = packageName.replace('.', '/');
            ClassLoader classLoader = IoUtils.getClassLoaderForClass(ClassWalker.class);

            URL resourceUrl = classLoader.getResource(packagePath);
            if (resourceUrl == null) {
                throw new InvalidPackageException(String.format("%s package not found in ClassLoader", packagePath));
            }
            URI pkg = resourceUrl.toURI();

            Path root;
            FileSystem fileSystem = null;
            if (pkg.toString().startsWith("jar:")) {
                try {
                    root = FileSystems.getFileSystem(pkg).getPath(packagePath);
                } catch (FileSystemNotFoundException exception) {
                    fileSystem = FileSystems.newFileSystem(pkg, Collections.emptyMap());
                    root = fileSystem.getPath(packagePath);
                }
            } else {
                root = Paths.get(pkg);
            }
            try (Stream<Path> allPaths = Files.walk(root)) {
                return allPaths.filter(Files::isRegularFile)
                        .filter(file -> file.toString().endsWith(".class"))
                        .map(this::mapFileToName)
                        .map(clazz -> {
                            try {
                                return classLoader.loadClass(clazz);
                            } catch (ClassNotFoundException exception) {
                                throw new UncheckedClassLoadException(exception);
                            }
                        })
                        .collect(Collectors.toSet());
            } catch (UncheckedClassLoadException exception) {
                throw new ReflectionException(exception.getCause());
            } finally {
                if (fileSystem != null) {
                    fileSystem.close();
                }
            }
        } catch (URISyntaxException | IOException exception) {
            throw new ReflectionException(exception);
        }
    }

    /**
     * Maps a .class file path to its fully qualified class name.
     *
     * @param file The path to the .class file.
     * @return The fully qualified class name.
     */
    @NotNull
    private String mapFileToName(@NotNull Path file) {
        String path = file.toString().replace(file.getFileSystem().getSeparator(), ".");
        return path.substring(path.indexOf(packageName), path.length() - ".class".length());
    }

    /**
     * Retrieves all classes within the specified package and sub-packages that extend the given parent class.
     *
     * @param <T>  The type of the parent class.
     * @param type The parent class to search for subclasses.
     * @return An unmodifiable {@link Set} of classes that are assignable to the given type.
     * @throws Exceptions If an error occurs during reflection operations.
     */
    @SuppressWarnings("unchecked") // Actual type safety is ensured by filtering with isAssignableFrom.
    @NotNull
    public <T> Set<Class<? extends T>> getSubTypesOf(@NotNull Class<T> type) throws Exceptions {
        return getAllClasses()
                .stream()
                .filter(type::isAssignableFrom)
                .map(clazz -> (Class<? extends T>) clazz)
                .collect(Collectors.toSet());
    }

    /**
     * Runtime exception for handling errors related to class loading within the {@code ClassWalker} class.
     */
    private static class UncheckedClassLoadException extends RuntimeException {
        public UncheckedClassLoadException(@NotNull Throwable cause) {
            super(cause);
        }
    }
}
