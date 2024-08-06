package com.infinitehorizons.handler;

import com.infinitehorizons.Shared;
import com.infinitehorizons.components.BuilderIdComponent;
import com.infinitehorizons.components.MappingIdComponent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Interface for handling interactions with {@link StringSelectMenu} components in Discord.
 * Implement this interface to define custom behavior when a user interacts with a string select menu.
 * This interface is typically used with {@link BuilderIdComponent} to manage select menu identifiers.
 *
 * <p>Example implementation:</p>
 *
 * <pre>{@code
 * public class TestCommand extends SlashCommand implements StringSelectMenuHandler {
 *
 *     public TestCommand() {
 *         setCommandData(Commands.slash("test", "test description"));
 *     }
 *
 *     @Override
 *     public void execute(SlashCommandInteractionEvent event) {
 *         List<Role> roles = // Retrieve roles list
 *         SelectMenu.Builder menu = SelectMenu.create("test-select-menu");
 *         roles.forEach(role -> menu.addOption(role.getName(), role.getId()));
 *         event.reply("Choose your rank!").addActionRow(menu.build()).queue();
 *     }
 *
 *     @Override
 *     public void handleStringSelectMenu(StringSelectInteractionEvent event, List<String> values) {
 *         values.forEach(roleId ->
 *             event.getGuild().addRoleToMember(event.getMember(), event.getGuild().getRoleById(roleId)).queue()
 *         );
 *         event.reply("Successfully added " + String.join(", ", event.getValues())).queue();
 *     }
 * }
 * }</pre>
 *
 * <p>To use this handler, configure the corresponding string-select menu mappings as follows:</p>
 *
 * <pre>{@code
 * shared.addStringSelectMenuMappings(MappingIdComponent.of(new TestCommand(), "test-string-select-menu"));
 * }</pre>
 *
 * @see Shared#addStringSelectMenuMappings(MappingIdComponent[])
 * @since v1.4
 */
public interface StringSelectMenuHandler {

    /**
     * Handles interactions with a {@link StringSelectMenu}.
     * Implement this method to define the action taken when a string select menu is interacted with.
     *
     * @param event  The {@link StringSelectInteractionEvent} that occurred.
     * @param values The list of selected string values.
     */
    void handleStringSelectMenu(@NotNull StringSelectInteractionEvent event, @NotNull List<String> values);
}
