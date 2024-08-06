package com.infinitehorizons.components;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Utility class for constructing and managing component IDs with a consistent format using a configurable separator.
 * This class facilitates the creation of component IDs by joining identifiers with arguments using a separator,
 * making it easier to manage and parse component IDs in the {@code Shared} framework.
 *
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BuilderIdComponent {

    /**
     * The default separator used to join identifiers and arguments in component IDs.
     */
    @Getter
    @Setter
    private static String defaultSeparator = ":";

    /**
     * Constructs a component ID using the specified identifier and optional arguments.
     * The components are joined using the currently set separator.
     *
     * <pre>{@code
     * Button.secondary(ComponentIdBuilder.build("self-role", roleId), "Click me!");
     * }</pre>
     *
     * @param identifier The base identifier for the component.
     * @param args Optional arguments to append to the identifier.
     * @return The constructed component ID as a {@link String}.
     */
    @NotNull
    public static String build(@NotNull String identifier, @NotNull Object... args) {
        StringBuilder sb = new StringBuilder(identifier);
        if (args.length > 0) {
            String joinedArgs = Arrays.stream(args)
                    .map(Object::toString)
                    .collect(Collectors.joining(defaultSeparator));
            sb.append(defaultSeparator).append(joinedArgs);
        }
        return sb.toString();
    }

    /**
     * Splits the given component ID into its constituent parts using the current separator.
     *
     * @param id The component ID to split.
     * @return An array of strings representing the split components.
     */
    @NotNull
    public static String[] split(@NotNull String id) {
        return id.split(defaultSeparator);
    }

}
