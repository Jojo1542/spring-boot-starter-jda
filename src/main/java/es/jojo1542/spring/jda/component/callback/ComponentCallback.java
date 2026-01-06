package es.jojo1542.spring.jda.component.callback;

import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;

/**
 * Functional interface for handling component interactions.
 *
 * <p>This interface is used to define callback handlers for interactive
 * Discord components such as buttons, select menus, and modals.
 *
 * <p>Example usage:
 * <pre>{@code
 * ComponentCallback<ButtonInteractionEvent> callback = (event, context) -> {
 *     event.reply("Button clicked!").setEphemeral(true).queue();
 * };
 * }</pre>
 *
 * @param <E> The specific event type (ButtonInteractionEvent, StringSelectInteractionEvent,
 *           ModalInteractionEvent, etc.)
 * @author jojo1542
 * @see CallbackContext
 */
@FunctionalInterface
public interface ComponentCallback<E extends GenericInteractionCreateEvent> {

    /**
     * Handle the component interaction.
     *
     * @param event   the interaction event from JDA
     * @param context callback context with additional data and utilities
     */
    void handle(E event, CallbackContext context);
}
