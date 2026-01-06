package es.jojo1542.spring.jda.component.builder;

import es.jojo1542.spring.jda.component.callback.ComponentCallback;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

import java.time.Duration;
import java.util.Collection;
import java.util.function.Function;

/**
 * Fluent builder for creating string select menus.
 *
 * <p>Example:
 * <pre>{@code
 * var menu = components.selectMenu()
 *     .placeholder("Choose a color")
 *     .range(1, 1)
 *     .option("Red", "red", "A vibrant red", "🔴")
 *     .option("Blue", "blue", "A cool blue", "🔵")
 *     .option("Green", "green", "A fresh green", "🟢")
 *     .onSelect((event, ctx) -> {
 *         String selected = event.getValues().get(0);
 *         event.reply("You selected: " + selected).queue();
 *     })
 *     .expireAfter(Duration.ofMinutes(5))
 *     .build();
 * }</pre>
 *
 * @author jojo1542
 */
public interface SelectMenuBuilder {

    /**
     * Set the placeholder text shown when nothing is selected.
     *
     * @param placeholder the placeholder text (max 150 chars)
     * @return this builder
     */
    SelectMenuBuilder placeholder(String placeholder);

    /**
     * Set the minimum and maximum number of selections.
     *
     * @param min minimum selections (default 1)
     * @param max maximum selections (default 1)
     * @return this builder
     */
    SelectMenuBuilder range(int min, int max);

    /**
     * Add an option with just label and value.
     *
     * @param label the display label
     * @param value the value sent to the handler
     * @return this builder
     */
    SelectMenuBuilder option(String label, String value);

    /**
     * Add an option with description.
     *
     * @param label       the display label
     * @param value       the value sent to the handler
     * @param description the description shown below the label
     * @return this builder
     */
    SelectMenuBuilder option(String label, String value, String description);

    /**
     * Add an option with description and emoji.
     *
     * @param label       the display label
     * @param value       the value sent to the handler
     * @param description the description (can be null)
     * @param emoji       the Unicode emoji
     * @return this builder
     */
    SelectMenuBuilder option(String label, String value, String description, String emoji);

    /**
     * Add an option with description and emoji.
     *
     * @param label       the display label
     * @param value       the value sent to the handler
     * @param description the description (can be null)
     * @param emoji       the emoji
     * @return this builder
     */
    SelectMenuBuilder option(String label, String value, String description, Emoji emoji);

    /**
     * Add multiple options from a collection.
     *
     * <p>Example:
     * <pre>{@code
     * List<Role> roles = ...;
     * menu.options(roles, Role::getName, r -> r.getId());
     * }</pre>
     *
     * @param items       the items to convert to options
     * @param labelMapper function to extract the label
     * @param valueMapper function to extract the value
     * @param <T>         the item type
     * @return this builder
     */
    <T> SelectMenuBuilder options(Collection<T> items,
                                  Function<T, String> labelMapper,
                                  Function<T, String> valueMapper);

    /**
     * Set the default selected values.
     *
     * @param values the default values
     * @return this builder
     */
    SelectMenuBuilder defaultValues(String... values);

    /**
     * Disable the select menu.
     *
     * @return this builder
     */
    SelectMenuBuilder disabled();

    /**
     * Set whether the select menu is disabled.
     *
     * @param disabled true to disable
     * @return this builder
     */
    SelectMenuBuilder disabled(boolean disabled);

    /**
     * Set a stateless ID that routes to an annotation-based handler.
     *
     * @param id the handler ID
     * @return this builder
     */
    SelectMenuBuilder withId(String id);

    /**
     * Append data to the component ID.
     *
     * @param data the data to embed
     * @return this builder
     */
    SelectMenuBuilder withData(String data);

    /**
     * Register a callback handler for this select menu.
     *
     * @param callback the callback to execute when selection changes
     * @return this builder
     */
    SelectMenuBuilder onSelect(ComponentCallback<StringSelectInteractionEvent> callback);

    /**
     * Set the callback expiration time.
     *
     * @param duration the time-to-live
     * @return this builder
     */
    SelectMenuBuilder expireAfter(Duration duration);

    /**
     * Set the maximum invocation count.
     *
     * @param count max invocations (0 = unlimited until TTL)
     * @return this builder
     */
    SelectMenuBuilder maxInvocations(int count);

    /**
     * Store additional context for the callback.
     *
     * @param key   the attribute key
     * @param value the attribute value
     * @return this builder
     */
    SelectMenuBuilder withContext(String key, Object value);

    /**
     * Enable auto-disable when the callback expires.
     *
     * @param channelId the channel ID
     * @param messageId the message ID
     * @return this builder
     */
    SelectMenuBuilder disableOnExpire(long channelId, long messageId);

    /**
     * Build the select menu.
     *
     * @return the configured JDA StringSelectMenu
     * @throws IllegalStateException if configuration is invalid
     */
    StringSelectMenu build();
}
