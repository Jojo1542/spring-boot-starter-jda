package es.jojo1542.spring.jda.component.builder;

import es.jojo1542.spring.jda.component.callback.ComponentCallback;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;

import java.time.Duration;

/**
 * Fluent builder for creating Discord buttons.
 *
 * <p>Supports both callback-based (temporary) and stateless (permanent) buttons.
 *
 * <p>Callback-based example:
 * <pre>{@code
 * var button = components.button()
 *     .primary("Click me")
 *     .emoji("👍")
 *     .onClick((event, ctx) -> {
 *         event.reply("Clicked!").setEphemeral(true).queue();
 *     })
 *     .expireAfter(Duration.ofMinutes(5))
 *     .singleUse()
 *     .build();
 * }</pre>
 *
 * <p>Stateless example:
 * <pre>{@code
 * var button = components.button()
 *     .danger("Delete")
 *     .withId("delete-item")  // Routes to @ButtonHandler("delete-item")
 *     .withData(itemId)       // Appends data to ID
 *     .build();
 * }</pre>
 *
 * @author jojo1542
 */
public interface ButtonBuilder {

    /**
     * Create a primary (blue) button with the given label.
     *
     * @param label the button label
     * @return this builder
     */
    ButtonBuilder primary(String label);

    /**
     * Create a secondary (gray) button with the given label.
     *
     * @param label the button label
     * @return this builder
     */
    ButtonBuilder secondary(String label);

    /**
     * Create a success (green) button with the given label.
     *
     * @param label the button label
     * @return this builder
     */
    ButtonBuilder success(String label);

    /**
     * Create a danger (red) button with the given label.
     *
     * @param label the button label
     * @return this builder
     */
    ButtonBuilder danger(String label);

    /**
     * Create a link button that opens a URL.
     *
     * <p>Link buttons don't have callbacks and don't trigger events.
     *
     * @param label the button label
     * @param url   the URL to open
     * @return this builder
     */
    ButtonBuilder link(String label, String url);

    /**
     * Set the button style.
     *
     * @param style the button style
     * @return this builder
     */
    ButtonBuilder style(ButtonStyle style);

    /**
     * Set the button label.
     *
     * @param label the label text (max 80 chars)
     * @return this builder
     */
    ButtonBuilder label(String label);

    /**
     * Set a Unicode emoji on the button.
     *
     * @param unicode the Unicode emoji string (e.g., "✅", "🎉")
     * @return this builder
     */
    ButtonBuilder emoji(String unicode);

    /**
     * Set an emoji on the button.
     *
     * @param emoji the emoji
     * @return this builder
     */
    ButtonBuilder emoji(Emoji emoji);

    /**
     * Disable the button.
     *
     * @return this builder
     */
    ButtonBuilder disabled();

    /**
     * Set whether the button is disabled.
     *
     * @param disabled true to disable
     * @return this builder
     */
    ButtonBuilder disabled(boolean disabled);

    /**
     * Set a stateless ID that routes to an annotation-based handler.
     *
     * <p>Use this for permanent handlers that survive bot restarts.
     * The ID should match a {@code @ButtonHandler} value.
     *
     * @param id the handler ID
     * @return this builder
     */
    ButtonBuilder withId(String id);

    /**
     * Append data to the component ID.
     *
     * <p>The data will be available in the handler via {@code @ComponentData}
     * or {@code CallbackContext.getData()}.
     *
     * @param data the data to embed
     * @return this builder
     */
    ButtonBuilder withData(String data);

    /**
     * Append multiple data values to the component ID.
     *
     * <p>Values are joined with colons. For example:
     * {@code withData("user", "123", "action", "delete")} produces
     * data string "user:123:action:delete".
     *
     * @param values the data values
     * @return this builder
     */
    ButtonBuilder withData(String... values);

    /**
     * Register a callback handler for this button.
     *
     * <p>Creates a callback-based button that expires after the configured TTL.
     *
     * @param callback the callback to execute when clicked
     * @return this builder
     */
    ButtonBuilder onClick(ComponentCallback<ButtonInteractionEvent> callback);

    /**
     * Set the callback expiration time.
     *
     * @param duration the time-to-live
     * @return this builder
     */
    ButtonBuilder expireAfter(Duration duration);

    /**
     * Set the maximum invocation count.
     *
     * @param count max invocations (0 = unlimited until TTL)
     * @return this builder
     */
    ButtonBuilder maxInvocations(int count);

    /**
     * Mark as single-use (maxInvocations = 1).
     *
     * @return this builder
     */
    default ButtonBuilder singleUse() {
        return maxInvocations(1);
    }

    /**
     * Store additional context for the callback.
     *
     * <p>This data is available via {@code CallbackContext.getAttribute(key)}.
     *
     * @param key   the attribute key
     * @param value the attribute value
     * @return this builder
     */
    ButtonBuilder withContext(String key, Object value);

    /**
     * Enable auto-disable when the callback expires.
     *
     * <p>This stores the message reference so the button can be disabled
     * when the callback expires. Must be called after the message is sent.
     *
     * @param channelId the channel ID
     * @param messageId the message ID
     * @return this builder
     */
    ButtonBuilder disableOnExpire(long channelId, long messageId);

    /**
     * Build the button.
     *
     * @return the configured JDA Button
     * @throws IllegalStateException if configuration is invalid
     */
    Button build();
}
