package com.infinitehorizons.components;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

/**
 * A simple POJO that holds a handler of type {@link T} and an array of {@link String}s representing IDs.
 * This class is used to associate a handler with multiple component IDs, facilitating the management of interactions
 * within the {@code Shared} framework.
 *
 * @param <T> The type of the handler.
 */
@Getter
@RequiredArgsConstructor(staticName = "of")
public class MappingIdComponent<T> {

    /**
     * The handler associated with the component IDs.
     */
    @NotNull
    private final T handler;

    /**
     * The array of component IDs linked to the handler.
     */
    @NotNull
    private final String[] ids;
}
