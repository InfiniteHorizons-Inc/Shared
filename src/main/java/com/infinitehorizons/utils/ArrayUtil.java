package com.infinitehorizons.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.dv8tion.jda.api.utils.data.DataArray;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * Utility class providing methods for array manipulation and inspection.
 * <p>
 * This class offers functionalities to check for the presence of elements in arrays and
 * to sort arrays derived from {@link DataArray}.
 *
 * @since v0.0.1-SNAPSHOT
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ArrayUtil {

    /**
     * Checks if the specified object is present in the given array.
     *
     * @param array  The array to search through.
     * @param search The {@link Object} to find within the array.
     * @return {@code true} if the object is found in the array, {@code false} otherwise.
     * @since v0.0.1-SNAPSHOT
     */
    public static boolean contains(@NotNull Object[] array, @NotNull Object search) {
        return Arrays.binarySearch(array, search) >= 0;
    }

    /**
     * Converts a {@link DataArray} to a sorted byte array.
     *
     * @param dataArray The {@link DataArray} to convert and sort.
     * @return A sorted byte array representing the JSON form of the {@link DataArray}.
     * @since v0.0.1-SNAPSHOT
     */
    @NotNull
    public static byte[] sortArrayFromDataArray(@NotNull DataArray dataArray) {
        byte[] array = dataArray.toJson();
        Arrays.sort(array);
        return array;
    }
}
