package es.jojo1542.spring.jda.component.builder;

import es.jojo1542.spring.jda.component.callback.ComponentCallback;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;

import java.time.Duration;

/**
 * Fluent builder for creating modals (popup forms).
 *
 * <p>Modals are popup forms that can contain text inputs. They must be
 * shown as a response to an interaction (button click, command, etc.).
 *
 * <p>Example:
 * <pre>{@code
 * var feedbackModal = components.modal()
 *     .title("Send Feedback")
 *     .shortInput("subject", "Subject", "Brief description", true)
 *     .paragraphInput("details", "Details", "Tell us more...", false)
 *     .onSubmit((event, ctx) -> {
 *         String subject = event.getValue("subject").getAsString();
 *         String details = event.getValue("details").getAsString();
 *         event.reply("Thanks for your feedback!").setEphemeral(true).queue();
 *     })
 *     .build();
 *
 * // Show the modal in response to a button click
 * event.replyModal(feedbackModal).queue();
 * }</pre>
 *
 * @author jojo1542
 */
public interface ModalBuilder {

    /**
     * Set the modal title.
     *
     * @param title the title (max 45 chars)
     * @return this builder
     */
    ModalBuilder title(String title);

    /**
     * Add a short (single-line) text input.
     *
     * @param id    the input ID (for retrieving the value)
     * @param label the input label
     * @return this builder
     */
    ModalBuilder shortInput(String id, String label);

    /**
     * Add a short text input with placeholder.
     *
     * @param id          the input ID
     * @param label       the input label
     * @param placeholder the placeholder text
     * @return this builder
     */
    ModalBuilder shortInput(String id, String label, String placeholder);

    /**
     * Add a short text input with full configuration.
     *
     * @param id          the input ID
     * @param label       the input label
     * @param placeholder the placeholder text (can be null)
     * @param required    whether the input is required
     * @return this builder
     */
    ModalBuilder shortInput(String id, String label, String placeholder, boolean required);

    /**
     * Add a short text input with length constraints.
     *
     * @param id          the input ID
     * @param label       the input label
     * @param placeholder the placeholder text (can be null)
     * @param required    whether the input is required
     * @param minLength   minimum length (0 = no minimum)
     * @param maxLength   maximum length (0 = no maximum, max 4000)
     * @return this builder
     */
    ModalBuilder shortInput(String id, String label, String placeholder,
                            boolean required, int minLength, int maxLength);

    /**
     * Add a paragraph (multi-line) text input.
     *
     * @param id    the input ID
     * @param label the input label
     * @return this builder
     */
    ModalBuilder paragraphInput(String id, String label);

    /**
     * Add a paragraph text input with placeholder.
     *
     * @param id          the input ID
     * @param label       the input label
     * @param placeholder the placeholder text
     * @return this builder
     */
    ModalBuilder paragraphInput(String id, String label, String placeholder);

    /**
     * Add a paragraph text input with full configuration.
     *
     * @param id          the input ID
     * @param label       the input label
     * @param placeholder the placeholder text (can be null)
     * @param required    whether the input is required
     * @return this builder
     */
    ModalBuilder paragraphInput(String id, String label, String placeholder, boolean required);

    /**
     * Add a paragraph text input with length constraints.
     *
     * @param id          the input ID
     * @param label       the input label
     * @param placeholder the placeholder text (can be null)
     * @param required    whether the input is required
     * @param minLength   minimum length (0 = no minimum)
     * @param maxLength   maximum length (0 = no maximum, max 4000)
     * @return this builder
     */
    ModalBuilder paragraphInput(String id, String label, String placeholder,
                                boolean required, int minLength, int maxLength);

    /**
     * Add a text input with full control.
     *
     * @param id          the input ID
     * @param label       the input label
     * @param style       SHORT or PARAGRAPH
     * @param placeholder the placeholder text (can be null)
     * @param required    whether the input is required
     * @param minLength   minimum length (0 = no minimum)
     * @param maxLength   maximum length (0 = no maximum)
     * @param value       pre-filled value (can be null)
     * @return this builder
     */
    ModalBuilder textInput(String id, String label, TextInputStyle style,
                           String placeholder, boolean required,
                           int minLength, int maxLength, String value);

    /**
     * Set a stateless ID that routes to an annotation-based handler.
     *
     * @param id the handler ID
     * @return this builder
     */
    ModalBuilder withId(String id);

    /**
     * Append data to the modal ID.
     *
     * @param data the data to embed
     * @return this builder
     */
    ModalBuilder withData(String data);

    /**
     * Register a callback handler for modal submission.
     *
     * @param callback the callback to execute when the modal is submitted
     * @return this builder
     */
    ModalBuilder onSubmit(ComponentCallback<ModalInteractionEvent> callback);

    /**
     * Set the callback expiration time.
     *
     * @param duration the time-to-live
     * @return this builder
     */
    ModalBuilder expireAfter(Duration duration);

    /**
     * Store additional context for the callback.
     *
     * @param key   the attribute key
     * @param value the attribute value
     * @return this builder
     */
    ModalBuilder withContext(String key, Object value);

    /**
     * Build the modal.
     *
     * @return the configured JDA Modal
     * @throws IllegalStateException if configuration is invalid
     */
    Modal build();
}
