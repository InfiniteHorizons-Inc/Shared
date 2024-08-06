package com.infinitehorizons.handler;

import com.infinitehorizons.components.BuilderIdComponent;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.entities.IMentionable;
import org.jetbrains.annotations.NotNull;
import java.util.List;

/**
 * Interface for handling interactions with {@link EntitySelectMenu} components in Discord.
 * Implement this interface to define custom behavior when a user interacts with an entity select menu.
 * This interface is typically used with {@link BuilderIdComponent} to manage select menu identifiers.
 *
 * <p>Example implementation:</p>
 *
 * <pre>{@code
 * public class TestCommand extends SlashCommand implements EntitySelectMenuHandler {
 *
 *     public TestCommand() {
 *         setCommandData(Commands.slash("test", "test description"));
 *     }
 *
 *     @Override
 *     public void execute(SlashCommandInteractionEvent event) {
 *         // Build and send your select menu here.
 *     }
 *
 *     @Override
 *     public void handleEntitySelectMenu(EntitySelectInteractionEvent event, List<IMentionable> values) {
 *         values.forEach(entity -> event.getChannel()
 *             .sendMessage(String.format("Mention: %s", entity.getAsMention())).queue());
 *     }
 * }
 * }</pre>
 *
 * <p>To use this handler, configure the corresponding entity-select menu mappings as follows:</p>
 *
 * <pre>{@code
 * shared.addEntitySelectMenuMappings(IdMapping.of(new TestCommand(), "test-entity-select-menu"));
 * }</pre>
 *
 */
public interface EntitySelectMenuHandler {

    /**
     * Handles interactions with an {@link EntitySelectMenu}.
     * Implement this method to define the action taken when an entity select menu is interacted with.
     *
     * @param event  The {@link EntitySelectInteractionEvent} that occurred.
     * @param values The list of {@link IMentionable} values selected by the user.
     */
    void handleEntitySelectMenu(@NotNull EntitySelectInteractionEvent event, @NotNull List<IMentionable> values);
}
