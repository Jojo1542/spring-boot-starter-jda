package es.jojo1542.spring.jda.component.builder;

import es.jojo1542.spring.jda.component.callback.CallbackEntry;
import es.jojo1542.spring.jda.component.callback.CallbackRegistry;
import es.jojo1542.spring.jda.component.callback.ComponentCallback;
import es.jojo1542.spring.jda.component.config.ComponentProperties;
import es.jojo1542.spring.jda.component.handler.ComponentId;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

import java.time.Duration;
import java.util.*;
import java.util.function.Function;

/**
 * Default implementation of {@link SelectMenuBuilder}.
 *
 * @author jojo1542
 */
public class DefaultSelectMenuBuilder implements SelectMenuBuilder {

    private final CallbackRegistry registry;
    private final ComponentProperties properties;

    // Menu configuration
    private String placeholder;
    private int minValues = 1;
    private int maxValues = 1;
    private final List<SelectOption> options = new ArrayList<>();
    private final Set<String> defaultValues = new HashSet<>();
    private boolean disabled = false;

    // ID configuration
    private String statelessId;
    private String data;

    // Callback configuration
    private ComponentCallback<StringSelectInteractionEvent> callback;
    private Duration ttl;
    private int maxInvocations = 0;
    private Map<String, Object> contextAttributes;
    private long channelId = 0;
    private long messageId = 0;

    public DefaultSelectMenuBuilder(CallbackRegistry registry, ComponentProperties properties) {
        this.registry = registry;
        this.properties = properties;
        this.ttl = properties.getCallback().getDefaultTtl();
    }

    @Override
    public SelectMenuBuilder placeholder(String placeholder) {
        this.placeholder = placeholder;
        return this;
    }

    @Override
    public SelectMenuBuilder range(int min, int max) {
        this.minValues = min;
        this.maxValues = max;
        return this;
    }

    @Override
    public SelectMenuBuilder option(String label, String value) {
        options.add(SelectOption.of(label, value));
        return this;
    }

    @Override
    public SelectMenuBuilder option(String label, String value, String description) {
        SelectOption option = SelectOption.of(label, value);
        if (description != null && !description.isBlank()) {
            option = option.withDescription(description);
        }
        options.add(option);
        return this;
    }

    @Override
    public SelectMenuBuilder option(String label, String value, String description, String emoji) {
        return option(label, value, description, emoji != null ? Emoji.fromUnicode(emoji) : null);
    }

    @Override
    public SelectMenuBuilder option(String label, String value, String description, Emoji emoji) {
        SelectOption option = SelectOption.of(label, value);
        if (description != null && !description.isBlank()) {
            option = option.withDescription(description);
        }
        if (emoji != null) {
            option = option.withEmoji(emoji);
        }
        options.add(option);
        return this;
    }

    @Override
    public <T> SelectMenuBuilder options(Collection<T> items,
                                         Function<T, String> labelMapper,
                                         Function<T, String> valueMapper) {
        for (T item : items) {
            option(labelMapper.apply(item), valueMapper.apply(item));
        }
        return this;
    }

    @Override
    public SelectMenuBuilder defaultValues(String... values) {
        defaultValues.addAll(Arrays.asList(values));
        return this;
    }

    @Override
    public SelectMenuBuilder disabled() {
        this.disabled = true;
        return this;
    }

    @Override
    public SelectMenuBuilder disabled(boolean disabled) {
        this.disabled = disabled;
        return this;
    }

    @Override
    public SelectMenuBuilder withId(String id) {
        this.statelessId = id;
        return this;
    }

    @Override
    public SelectMenuBuilder withData(String data) {
        this.data = data;
        return this;
    }

    @Override
    public SelectMenuBuilder onSelect(ComponentCallback<StringSelectInteractionEvent> callback) {
        this.callback = callback;
        return this;
    }

    @Override
    public SelectMenuBuilder expireAfter(Duration duration) {
        this.ttl = duration;
        return this;
    }

    @Override
    public SelectMenuBuilder maxInvocations(int count) {
        this.maxInvocations = count;
        return this;
    }

    @Override
    public SelectMenuBuilder withContext(String key, Object value) {
        if (contextAttributes == null) {
            contextAttributes = new HashMap<>();
        }
        contextAttributes.put(key, value);
        return this;
    }

    @Override
    public SelectMenuBuilder disableOnExpire(long channelId, long messageId) {
        this.channelId = channelId;
        this.messageId = messageId;
        return this;
    }

    @Override
    public StringSelectMenu build() {
        if (options.isEmpty()) {
            throw new IllegalStateException("Select menu must have at least one option");
        }

        String componentId = buildComponentId();

        // Apply default values to options
        List<SelectOption> finalOptions = options.stream()
                .map(opt -> defaultValues.contains(opt.getValue())
                        ? opt.withDefault(true)
                        : opt)
                .toList();

        StringSelectMenu.Builder builder = StringSelectMenu.create(componentId)
                .addOptions(finalOptions)
                .setMinValues(minValues)
                .setMaxValues(maxValues)
                .setDisabled(disabled);

        if (placeholder != null && !placeholder.isBlank()) {
            builder.setPlaceholder(placeholder);
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
            return ComponentId.callback(ComponentId.ComponentType.STRING_SELECT, callbackId, data).toRaw();
        }

        // Stateless menu
        if (statelessId != null) {
            return ComponentId.stateless(ComponentId.ComponentType.STRING_SELECT, statelessId, data).toRaw();
        }

        throw new IllegalStateException(
                "Select menu must have either a callback (onSelect) or a stateless ID (withId)"
        );
    }
}
