package es.jojo1542.spring.jda.component.builder;

import es.jojo1542.spring.jda.component.callback.ComponentCallback;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu;

import java.time.Duration;
import java.util.Collection;

/**
 * Fluent builder for creating entity select menus.
 *
 * <p>Entity select menus allow users to select Discord entities like
 * users, roles, channels, or mentionables.
 *
 * <p>Example:
 * <pre>{@code
 * // User selector
 * var userMenu = components.entitySelect()
 *     .users()
 *     .placeholder("Select a user")
 *     .range(1, 3)
 *     .onSelect((event, ctx) -> {
 *         List<User> users = event.getMentions().getUsers();
 *         event.reply("Selected: " + users).queue();
 *     })
 *     .build();
 *
 * // Role selector
 * var roleMenu = components.entitySelect()
 *     .roles()
 *     .placeholder("Select roles")
 *     .range(1, 5)
 *     .withId("role-selector")
 *     .build();
 *
 * // Channel selector with type filter
 * var channelMenu = components.entitySelect()
 *     .channels(ChannelType.TEXT, ChannelType.VOICE)
 *     .placeholder("Select a channel")
 *     .build();
 * }</pre>
 *
 * @author jojo1542
 */
public interface EntitySelectBuilder {

    /**
     * Create a user select menu.
     *
     * @return this builder
     */
    EntitySelectBuilder users();

    /**
     * Create a role select menu.
     *
     * @return this builder
     */
    EntitySelectBuilder roles();

    /**
     * Create a channel select menu.
     *
     * @return this builder
     */
    EntitySelectBuilder channels();

    /**
     * Create a channel select menu with type filter.
     *
     * @param types the allowed channel types
     * @return this builder
     */
    EntitySelectBuilder channels(ChannelType... types);

    /**
     * Create a channel select menu with type filter.
     *
     * @param types the allowed channel types
     * @return this builder
     */
    EntitySelectBuilder channels(Collection<ChannelType> types);

    /**
     * Create a mentionable select menu (users and roles).
     *
     * @return this builder
     */
    EntitySelectBuilder mentionables();

    /**
     * Set the placeholder text.
     *
     * @param placeholder the placeholder text
     * @return this builder
     */
    EntitySelectBuilder placeholder(String placeholder);

    /**
     * Set the minimum and maximum number of selections.
     *
     * @param min minimum selections (default 1)
     * @param max maximum selections (default 1)
     * @return this builder
     */
    EntitySelectBuilder range(int min, int max);

    /**
     * Disable the select menu.
     *
     * @return this builder
     */
    EntitySelectBuilder disabled();

    /**
     * Set whether the select menu is disabled.
     *
     * @param disabled true to disable
     * @return this builder
     */
    EntitySelectBuilder disabled(boolean disabled);

    /**
     * Set a stateless ID that routes to an annotation-based handler.
     *
     * @param id the handler ID
     * @return this builder
     */
    EntitySelectBuilder withId(String id);

    /**
     * Append data to the component ID.
     *
     * @param data the data to embed
     * @return this builder
     */
    EntitySelectBuilder withData(String data);

    /**
     * Register a callback handler for this select menu.
     *
     * @param callback the callback to execute when selection changes
     * @return this builder
     */
    EntitySelectBuilder onSelect(ComponentCallback<EntitySelectInteractionEvent> callback);

    /**
     * Set the callback expiration time.
     *
     * @param duration the time-to-live
     * @return this builder
     */
    EntitySelectBuilder expireAfter(Duration duration);

    /**
     * Set the maximum invocation count.
     *
     * @param count max invocations (0 = unlimited until TTL)
     * @return this builder
     */
    EntitySelectBuilder maxInvocations(int count);

    /**
     * Store additional context for the callback.
     *
     * @param key   the attribute key
     * @param value the attribute value
     * @return this builder
     */
    EntitySelectBuilder withContext(String key, Object value);

    /**
     * Enable auto-disable when the callback expires.
     *
     * @param channelId the channel ID
     * @param messageId the message ID
     * @return this builder
     */
    EntitySelectBuilder disableOnExpire(long channelId, long messageId);

    /**
     * Build the entity select menu.
     *
     * @return the configured JDA EntitySelectMenu
     * @throws IllegalStateException if configuration is invalid
     */
    EntitySelectMenu build();
}
