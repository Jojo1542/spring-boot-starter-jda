package es.jojo1542.spring.jda.component.builder;

import es.jojo1542.spring.jda.component.callback.CallbackEntry;
import es.jojo1542.spring.jda.component.callback.CallbackRegistry;
import es.jojo1542.spring.jda.component.callback.ComponentCallback;
import es.jojo1542.spring.jda.component.config.ComponentProperties;
import es.jojo1542.spring.jda.component.handler.ComponentId;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu;

import java.time.Duration;
import java.util.*;

/**
 * Default implementation of {@link EntitySelectBuilder}.
 *
 * @author jojo1542
 */
public class DefaultEntitySelectBuilder implements EntitySelectBuilder {

    private final CallbackRegistry registry;
    private final ComponentProperties properties;

    // Menu configuration
    private EntitySelectMenu.SelectTarget target;
    private final Set<ChannelType> channelTypes = EnumSet.noneOf(ChannelType.class);
    private String placeholder;
    private int minValues = 1;
    private int maxValues = 1;
    private boolean disabled = false;

    // ID configuration
    private String statelessId;
    private String data;

    // Callback configuration
    private ComponentCallback<EntitySelectInteractionEvent> callback;
    private Duration ttl;
    private int maxInvocations = 0;
    private Map<String, Object> contextAttributes;
    private long channelId = 0;
    private long messageId = 0;

    public DefaultEntitySelectBuilder(CallbackRegistry registry, ComponentProperties properties) {
        this.registry = registry;
        this.properties = properties;
        this.ttl = properties.getCallback().getDefaultTtl();
    }

    @Override
    public EntitySelectBuilder users() {
        this.target = EntitySelectMenu.SelectTarget.USER;
        return this;
    }

    @Override
    public EntitySelectBuilder roles() {
        this.target = EntitySelectMenu.SelectTarget.ROLE;
        return this;
    }

    @Override
    public EntitySelectBuilder channels() {
        this.target = EntitySelectMenu.SelectTarget.CHANNEL;
        return this;
    }

    @Override
    public EntitySelectBuilder channels(ChannelType... types) {
        this.target = EntitySelectMenu.SelectTarget.CHANNEL;
        this.channelTypes.addAll(Arrays.asList(types));
        return this;
    }

    @Override
    public EntitySelectBuilder channels(Collection<ChannelType> types) {
        this.target = EntitySelectMenu.SelectTarget.CHANNEL;
        this.channelTypes.addAll(types);
        return this;
    }

    @Override
    public EntitySelectBuilder mentionables() {
        this.target = EntitySelectMenu.SelectTarget.ROLE; // Will use both
        return this;
    }

    @Override
    public EntitySelectBuilder placeholder(String placeholder) {
        this.placeholder = placeholder;
        return this;
    }

    @Override
    public EntitySelectBuilder range(int min, int max) {
        this.minValues = min;
        this.maxValues = max;
        return this;
    }

    @Override
    public EntitySelectBuilder disabled() {
        this.disabled = true;
        return this;
    }

    @Override
    public EntitySelectBuilder disabled(boolean disabled) {
        this.disabled = disabled;
        return this;
    }

    @Override
    public EntitySelectBuilder withId(String id) {
        this.statelessId = id;
        return this;
    }

    @Override
    public EntitySelectBuilder withData(String data) {
        this.data = data;
        return this;
    }

    @Override
    public EntitySelectBuilder onSelect(ComponentCallback<EntitySelectInteractionEvent> callback) {
        this.callback = callback;
        return this;
    }

    @Override
    public EntitySelectBuilder expireAfter(Duration duration) {
        this.ttl = duration;
        return this;
    }

    @Override
    public EntitySelectBuilder maxInvocations(int count) {
        this.maxInvocations = count;
        return this;
    }

    @Override
    public EntitySelectBuilder withContext(String key, Object value) {
        if (contextAttributes == null) {
            contextAttributes = new HashMap<>();
        }
        contextAttributes.put(key, value);
        return this;
    }

    @Override
    public EntitySelectBuilder disableOnExpire(long channelId, long messageId) {
        this.channelId = channelId;
        this.messageId = messageId;
        return this;
    }

    @Override
    public EntitySelectMenu build() {
        if (target == null) {
            throw new IllegalStateException(
                    "Entity select menu must have a target type. " +
                    "Call users(), roles(), channels(), or mentionables() first."
            );
        }

        String componentId = buildComponentId();

        EntitySelectMenu.Builder builder = EntitySelectMenu.create(componentId, target)
                .setMinValues(minValues)
                .setMaxValues(maxValues)
                .setDisabled(disabled);

        if (placeholder != null && !placeholder.isBlank()) {
            builder.setPlaceholder(placeholder);
        }

        if (!channelTypes.isEmpty() && target == EntitySelectMenu.SelectTarget.CHANNEL) {
            builder.setChannelTypes(channelTypes);
        }

        return builder.build();
    }

    private String buildComponentId() {
        // Callback-based menu
        if (callback != null) {
            CallbackEntry entry = new CallbackEntry(
                    callback, ttl, maxInvocations,
                    channelId, messageId, contextAttributes
            );
            String callbackId = registry.register(entry);
            return ComponentId.callback(ComponentId.ComponentType.ENTITY_SELECT, callbackId, data).toRaw();
        }

        // Stateless menu
        if (statelessId != null) {
            return ComponentId.stateless(ComponentId.ComponentType.ENTITY_SELECT, statelessId, data).toRaw();
        }

        throw new IllegalStateException(
                "Entity select menu must have either a callback (onSelect) or a stateless ID (withId)"
        );
    }
}
