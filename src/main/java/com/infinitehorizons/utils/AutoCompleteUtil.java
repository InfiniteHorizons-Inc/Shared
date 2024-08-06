package com.infinitehorizons.utils;

import lombok.NoArgsConstructor;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Utility class for filtering AutoComplete choices based on user input in the {@code Shared} framework.
 * This class provides methods to filter command choices dynamically, aiding in the creation of responsive
 * AutoComplete functionality.
 *
 */
@NoArgsConstructor
public final class AutoCompleteUtil {

    /**
     * Filters AutoComplete choices based on the user's current input from a {@link CommandAutoCompleteInteractionEvent}.
     * Returns a list of choices that match the user's input.
     *
     * <pre>{@code
     * return event.replyChoices(AutoCompleteUtils.filterChoices(event, choices));
     * }</pre>
     *
     * @param event   The {@link CommandAutoCompleteInteractionEvent} containing the user's input.
     * @param choices A {@link List} of {@link Command.Choice} to filter.
     * @return A filtered {@link List} of {@link Command.Choice} matching the input.
     */
    @NotNull
    public static List<Command.Choice> filterChoices(@NotNull CommandAutoCompleteInteractionEvent event,
                                                     @NotNull List<Command.Choice> choices) {
        return filterChoices(event.getFocusedOption().getValue(), choices);
    }

    /**
     * Filters AutoComplete choices based on the user's current input from a {@link CommandAutoCompleteInteractionEvent}.
     * Accepts choices as an array.
     *
     * <pre>{@code
     * return event.replyChoices(AutoCompleteUtils.filterChoices(event, choices));
     * }</pre>
     *
     * @param event   The {@link CommandAutoCompleteInteractionEvent} containing the user's input.
     * @param choices An array of {@link Command.Choice} to filter.
     * @return A filtered {@link List} of {@link Command.Choice} matching the input.
     */
    @NotNull
    public static List<Command.Choice> filterChoices(@NotNull CommandAutoCompleteInteractionEvent event,
                                                     @NotNull Command.Choice... choices) {
        return filterChoices(event.getFocusedOption().getValue(), List.of(choices));
    }

    /**
     * Filters AutoComplete choices based on a provided filter string.
     * Returns a list of choices that contain the filter string.
     *
     * <pre>{@code
     * return event.replyChoices(AutoCompleteUtils.filterChoices("abc", choices));
     * }</pre>
     *
     * @param filter  The filter string to match against choice names.
     * @param choices A {@link List} of {@link Command.Choice} to filter.
     * @return A filtered {@link List} of {@link Command.Choice} matching the filter.
     */
    @NotNull
    public static List<Command.Choice> filterChoices(@NotNull String filter, @NotNull List<Command.Choice> choices) {
        String lowercaseFilter = filter.toLowerCase(Locale.ROOT);
        return choices.stream()
                .filter(choice -> choice.getName().toLowerCase(Locale.ROOT).contains(lowercaseFilter))
                .limit(OptionData.MAX_CHOICES)
                .collect(Collectors.toList());
    }

    /**
     * Filters AutoComplete choices based on a provided filter string.
     * Accepts choices as an array and returns a list of choices that contain the filter string.
     *
     * <pre>{@code
     * return event.replyChoices(AutoCompleteUtils.filterChoices("abc", choices));
     * }</pre>
     *
     * @param filter  The filter string to match against choice names.
     * @param choices An array of {@link Command.Choice} to filter.
     * @return A filtered {@link List} of {@link Command.Choice} matching the filter.
     */
    @NotNull
    public static List<Command.Choice> filterChoices(@NotNull String filter, @NotNull Command.Choice... choices) {
        return filterChoices(filter, List.of(choices));
    }
}
