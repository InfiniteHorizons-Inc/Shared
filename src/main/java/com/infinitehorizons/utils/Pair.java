package com.infinitehorizons.utils;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

/**
 * A generic container to hold a pair of two elements. This class provides a simple way to store two related objects
 * together and is commonly used when a method needs to return two different objects.
 *
 * @param <F> The type of the first element.
 * @param <S> The type of the second element.
 * @since v1.1.1-SNAPSHOT
 */
public class Pair<F, S> {

    /**
     * The first element of the pair.
     */
    @Getter
    private final F first;

    /**
     * The second element of the pair.
     */
    @Getter
    private final S second;

    /**
     * Constructs a new {@link Pair} with the specified elements.
     *
     * @param first  the first element of the pair, must not be null.
     * @param second the second element of the pair, must not be null.
     */
    public Pair(@NotNull F first, @NotNull S second) {
        this.first = first;
        this.second = second;
    }
}