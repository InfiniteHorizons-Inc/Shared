package com.infinitehorizons.handler;

import com.infinitehorizons.Shared;
import com.infinitehorizons.components.BuilderIdComponent;
import com.infinitehorizons.components.MappingIdComponent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;

/**
 * Interface for handling button interactions in Discord.
 * Implement this interface to define custom behavior when a user interacts with a {@link Button}.
 * This interface is typically used in conjunction with {@link BuilderIdComponent} to manage button identifiers.
 *
 * <p>Example implementation:</p>
 *
 * <pre>{@code
 * public class TestCommand extends SlashCommand implements ButtonHandler {
 *
 *     public TestCommand() {
 *         setCommandData(Commands.slash("test", "test description"));
 *     }
 *
 *     @Override
 *     public void execute(SlashCommandInteractionEvent event) {
 *         event.reply("test")
 *              .addActionRow(
 *                  Button.secondary(ComponentIdBuilder.build("test-button", 1), "Click me!"),
 *                  Button.secondary(ComponentIdBuilder.build("test-button", 2), "NO! Click me!")
 *              ).queue();
 *     }
 *
 *     @Override
 *     public void handleButton(ButtonInteractionEvent event, Button button) {
 *         String[] id = ComponentIdBuilder.split(button.getId());
 *         String content = switch (id[1]) {
 *             case "1" -> "Thanks for not clicking the other button! :)";
 *             case "2" -> "Phew, thanks for clicking me...";
 *             default -> "Unknown button!";
 *         };
 *         event.reply(content).queue();
 *     }
 * }
 * }</pre>
 *
 * <p>Button mappings must be configured to associate the handler with button identifiers:</p>
 *
 * <pre>{@code
 * shared.addButtonMappings(IdMapping.of(new TestCommand(), "test-button"));
 * }</pre>
 *
 * @see Shared#addButtonMappings(MappingIdComponent[])
 * @since v1.4
 */
public interface ButtonHandler {

    /**
     * Handles button interactions for a command. Implement this method to define
     * the action taken when a button is clicked.
     *
     * @param event  The {@link ButtonInteractionEvent} triggered by the button interaction.
     * @param button The {@link Button} that was interacted with.
     */
    void handleButton(@NotNull ButtonInteractionEvent event, @NotNull Button button);
}
