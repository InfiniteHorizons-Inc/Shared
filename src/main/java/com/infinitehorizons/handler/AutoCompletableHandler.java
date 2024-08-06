package com.infinitehorizons.handler;

import com.infinitehorizons.utils.AutoCompleteUtil;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.AutoCompleteQuery;
import org.jetbrains.annotations.NotNull;

/**
 * Interface for handling auto-complete interactions in commands.
 * Implement this interface to provide dynamic suggestions to users based on their input during a command interaction.
 *
 * <p>Example usage:</p>
 *
 * <pre>{@code
 * public class PingCommand extends SlashCommand implements AutoCompletableHandler {
 *
 *     public PingCommand() {
 *         setCommandData(Commands.slash("ping", "Ping someone")
 *             .addOption(OptionType.STRING, "user-id", "The user's ID", true, true));
 *     }
 *
 *     @Override
 *     public void execute(@Nonnull SlashCommandInteractionEvent event) {
 *         String userId = event.getOption("user-id").getAsString();
 *         event.replyFormat("Ping! <@%s>", userId).queue();
 *     }
 *
 *     @Override
 *     public void handleAutoComplete(@Nonnull CommandAutoCompleteInteractionEvent event,
 *                                    @Nonnull AutoCompleteQuery target) {
 *         if ("user-id".equals(target.getName())) {
 *             List<Command.Choice> choices = event.getGuild().getMembers().stream()
 *                 .limit(25)
 *                 .map(member -> new Command.Choice(member.getUser().getAsTag(), member.getId()))
 *                 .collect(Collectors.toList());
 *             event.replyChoices(AutoCompleteUtils.filterChoices(event, choices)).queue();
 *         }
 *     }
 * }
 * }</pre>
 *
 * @see AutoCompleteUtil
 */
public interface AutoCompletableHandler {

    /**
     * Handles auto-complete interactions for a command.
     * Implement this method to provide suggestions based on user input during a command interaction.
     *
     * @param event  The {@link CommandAutoCompleteInteractionEvent} containing the interaction details.
     * @param target The {@link AutoCompleteQuery} representing the user's input.
     */
    void handleAutoComplete(@NotNull CommandAutoCompleteInteractionEvent event, @NotNull AutoCompleteQuery target);
}
