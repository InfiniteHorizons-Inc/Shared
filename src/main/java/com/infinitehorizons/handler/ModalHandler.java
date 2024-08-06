package com.infinitehorizons.handler;

import com.infinitehorizons.Shared;
import com.infinitehorizons.components.BuilderIdComponent;
import com.infinitehorizons.components.MappingIdComponent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Interface for handling interactions with {@link Modal} components in Discord.
 * Implement this interface to define custom behavior when a user interacts with a modal.
 * This interface is typically used with {@link BuilderIdComponent} to manage modal identifiers.
 *
 * <p>Example implementation:</p>
 *
 * <pre>{@code
 * public class TestCommand extends SlashCommand implements ModalHandler {
 *
 *     public TestCommand() {
 *         setCommandData(Commands.slash("test", "test description"));
 *     }
 *
 *     @Override
 *     public void execute(SlashCommandInteractionEvent event) {
 *         Role applied = // ... get the role;
 *         TextInput email = TextInput.create("email", "Email", TextInputStyle.SHORT)
 *                 .setPlaceholder("Enter your E-mail")
 *                 .setMinLength(10)
 *                 .setMaxLength(100)
 *                 .build();
 *
 *         TextInput body = TextInput.create("body", "Body", TextInputStyle.PARAGRAPH)
 *                 .setPlaceholder("Your application goes here")
 *                 .setMinLength(30)
 *                 .setMaxLength(1000)
 *                 .build();
 *
 *         Modal modal = Modal.create(ComponentIdBuilder.build("test-modal", applied.getIdLong()), "Apply for " + applied.getName())
 *                 .addActionRows(ActionRow.of(email), ActionRow.of(body))
 *                 .build();
 *         event.replyModal(modal).queue();
 *     }
 *
 *     @Override
 *     public void handleModal(ModalInteractionEvent event, List<ModalMapping> values) {
 *         Role role = event.getGuild().getRoleById(ComponentIdBuilder.split(event.getModalId())[1]);
 *         String email = event.getValue("email").getAsString();
 *         String body = event.getValue("body").getAsString();
 *
 *         createApplication(role, email, body);
 *
 *         event.reply("Thanks for your application!").queue();
 *     }
 * }
 * }</pre>
 *
 * <p>To use this handler, configure the corresponding modal mappings as follows:</p>
 *
 * <pre>{@code
 * shared.addModalMappings(MappingIdComponent.of(new TestCommand(), "test-modal"));
 * }</pre>
 *
 * @see Shared#addModalMappings(MappingIdComponent[])
 */
public interface ModalHandler {

    /**
     * Handles interactions with a {@link Modal}.
     * Implement this method to define the action taken when a modal is interacted with.
     *
     * @param event  The {@link ModalInteractionEvent} that occurred.
     * @param values A list of {@link ModalMapping} values submitted by the user.
     */
    void handleModal(@NotNull ModalInteractionEvent event, @NotNull List<ModalMapping> values);
}