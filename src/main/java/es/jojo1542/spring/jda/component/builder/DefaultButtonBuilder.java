package es.jojo1542.spring.jda.component.builder;

import es.jojo1542.spring.jda.component.callback.CallbackEntry;
import es.jojo1542.spring.jda.component.callback.CallbackRegistry;
import es.jojo1542.spring.jda.component.callback.ComponentCallback;
import es.jojo1542.spring.jda.component.config.ComponentProperties;
import es.jojo1542.spring.jda.component.handler.ComponentId;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Default implementation of {@link ButtonBuilder}.
 *
 * @author jojo1542
 */
public class DefaultButtonBuilder implements ButtonBuilder {

    private final CallbackRegistry registry;
    private final ComponentProperties properties;

    // Button configuration
    private ButtonStyle style = ButtonStyle.PRIMARY;
    private String label;
    private Emoji emoji;
    private boolean disabled = false;
    private String url; // For link buttons

    // ID configuration
    private String statelessId;
    private String data;

    // Callback configuration
    private ComponentCallback<ButtonInteractionEvent> callback;
    private Duration ttl;
    private int maxInvocations = 0;
    private Map<String, Object> contextAttributes;
    private long channelId = 0;
    private long messageId = 0;

    public DefaultButtonBuilder(CallbackRegistry registry, ComponentProperties properties) {
        this.registry = registry;
        this.properties = properties;
        this.ttl = properties.getCallback().getDefaultTtl();
    }

    @Override
    public ButtonBuilder primary(String label) {
        this.style = ButtonStyle.PRIMARY;
        this.label = label;
        return this;
    }

    @Override
    public ButtonBuilder secondary(String label) {
        this.style = ButtonStyle.SECONDARY;
        this.label = label;
        return this;
    }

    @Override
    public ButtonBuilder success(String label) {
        this.style = ButtonStyle.SUCCESS;
        this.label = label;
        return this;
    }

    @Override
    public ButtonBuilder danger(String label) {
        this.style = ButtonStyle.DANGER;
        this.label = label;
        return this;
    }

    @Override
    public ButtonBuilder link(String label, String url) {
        this.style = ButtonStyle.LINK;
        this.label = label;
        this.url = url;
        return this;
    }

    @Override
    public ButtonBuilder style(ButtonStyle style) {
        this.style = style;
        return this;
    }

    @Override
    public ButtonBuilder label(String label) {
        this.label = label;
        return this;
    }

    @Override
    public ButtonBuilder emoji(String unicode) {
        this.emoji = Emoji.fromUnicode(unicode);
        return this;
    }

    @Override
    public ButtonBuilder emoji(Emoji emoji) {
        this.emoji = emoji;
        return this;
    }

    @Override
    public ButtonBuilder disabled() {
        this.disabled = true;
        return this;
    }

    @Override
    public ButtonBuilder disabled(boolean disabled) {
        this.disabled = disabled;
        return this;
    }

    @Override
    public ButtonBuilder withId(String id) {
        this.statelessId = id;
        return this;
    }

    @Override
    public ButtonBuilder withData(String data) {
        this.data = data;
        return this;
    }

    @Override
    public ButtonBuilder withData(String... values) {
        StringJoiner joiner = new StringJoiner(":");
        for (String value : values) {
            joiner.add(value);
        }
        this.data = joiner.toString();
        return this;
    }

    @Override
    public ButtonBuilder onClick(ComponentCallback<ButtonInteractionEvent> callback) {
        this.callback = callback;
        return this;
    }

    @Override
    public ButtonBuilder expireAfter(Duration duration) {
        this.ttl = duration;
        return this;
    }

    @Override
    public ButtonBuilder maxInvocations(int count) {
        this.maxInvocations = count;
        return this;
    }

    @Override
    public ButtonBuilder withContext(String key, Object value) {
        if (contextAttributes == null) {
            contextAttributes = new HashMap<>();
        }
        contextAttributes.put(key, value);
        return this;
    }

    @Override
    public ButtonBuilder disableOnExpire(long channelId, long messageId) {
        this.channelId = channelId;
        this.messageId = messageId;
        return this;
    }

    @Override
    public Button build() {
        // Link buttons are special - no ID, just URL
        if (style == ButtonStyle.LINK) {
            if (url == null || url.isBlank()) {
                throw new IllegalStateException("Link button requires a URL");
            }
            Button button = Button.link(url, label);
            if (emoji != null) {
                button = button.withEmoji(emoji);
            }
            return disabled ? button.asDisabled() : button;
        }

        // Determine the component ID
        String componentId = buildComponentId();

        // Create the button
        Button button = Button.of(style, componentId, label);

        if (emoji != null) {
            button = button.withEmoji(emoji);
        }

        return disabled ? button.asDisabled() : button;
    }

    /**
     * Build the component ID based on configuration.
     */
    private String buildComponentId() {
        // Callback-based button
        if (callback != null) {
            CallbackEntry entry = new CallbackEntry(
                    callback, ttl, maxInvocations,
                    channelId, messageId, contextAttributes
            );
            String callbackId = registry.register(entry);
            return ComponentId.callback(ComponentId.ComponentType.BUTTON, callbackId, data).toRaw();
        }

        // Stateless button (annotation-based)
        if (statelessId != null) {
            return ComponentId.stateless(ComponentId.ComponentType.BUTTON, statelessId, data).toRaw();
        }

        throw new IllegalStateException(
                "Button must have either a callback (onClick) or a stateless ID (withId)"
        );
    }
}
