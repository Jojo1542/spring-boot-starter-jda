package es.jojo1542.spring.jda.component.builder;

import es.jojo1542.spring.jda.component.callback.CallbackEntry;
import es.jojo1542.spring.jda.component.callback.CallbackRegistry;
import es.jojo1542.spring.jda.component.callback.ComponentCallback;
import es.jojo1542.spring.jda.component.config.ComponentProperties;
import es.jojo1542.spring.jda.component.handler.ComponentId;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Default implementation of {@link ModalBuilder}.
 *
 * @author jojo1542
 */
public class DefaultModalBuilder implements ModalBuilder {

    private final CallbackRegistry registry;

    // Modal configuration
    private String title;
    private final List<TextInput> inputs = new ArrayList<>();

    // ID configuration
    private String statelessId;
    private String data;

    // Callback configuration
    private ComponentCallback<ModalInteractionEvent> callback;
    private Duration ttl;
    private Map<String, Object> contextAttributes;

    public DefaultModalBuilder(CallbackRegistry registry, ComponentProperties properties) {
        this.registry = registry;
        this.ttl = properties.getCallback().getDefaultTtl();
    }

    @Override
    public ModalBuilder title(String title) {
        this.title = title;
        return this;
    }

    @Override
    public ModalBuilder shortInput(String id, String label) {
        return shortInput(id, label, null, true);
    }

    @Override
    public ModalBuilder shortInput(String id, String label, String placeholder) {
        return shortInput(id, label, placeholder, true);
    }

    @Override
    public ModalBuilder shortInput(String id, String label, String placeholder, boolean required) {
        return shortInput(id, label, placeholder, required, 0, 0);
    }

    @Override
    public ModalBuilder shortInput(String id, String label, String placeholder,
                                   boolean required, int minLength, int maxLength) {
        return textInput(id, label, TextInputStyle.SHORT, placeholder, required, minLength, maxLength, null);
    }

    @Override
    public ModalBuilder paragraphInput(String id, String label) {
        return paragraphInput(id, label, null, true);
    }

    @Override
    public ModalBuilder paragraphInput(String id, String label, String placeholder) {
        return paragraphInput(id, label, placeholder, true);
    }

    @Override
    public ModalBuilder paragraphInput(String id, String label, String placeholder, boolean required) {
        return paragraphInput(id, label, placeholder, required, 0, 0);
    }

    @Override
    public ModalBuilder paragraphInput(String id, String label, String placeholder,
                                       boolean required, int minLength, int maxLength) {
        return textInput(id, label, TextInputStyle.PARAGRAPH, placeholder, required, minLength, maxLength, null);
    }

    @Override
    public ModalBuilder textInput(String id, String label, TextInputStyle style,
                                  String placeholder, boolean required,
                                  int minLength, int maxLength, String value) {
        TextInput.Builder builder = TextInput.create(id, label, style)
                .setRequired(required);

        if (placeholder != null && !placeholder.isBlank()) {
            builder.setPlaceholder(placeholder);
        }

        if (minLength > 0) {
            builder.setMinLength(minLength);
        }

        if (maxLength > 0) {
            builder.setMaxLength(maxLength);
        }

        if (value != null) {
            builder.setValue(value);
        }

        inputs.add(builder.build());
        return this;
    }

    @Override
    public ModalBuilder withId(String id) {
        this.statelessId = id;
        return this;
    }

    @Override
    public ModalBuilder withData(String data) {
        this.data = data;
        return this;
    }

    @Override
    public ModalBuilder onSubmit(ComponentCallback<ModalInteractionEvent> callback) {
        this.callback = callback;
        return this;
    }

    @Override
    public ModalBuilder expireAfter(Duration duration) {
        this.ttl = duration;
        return this;
    }

    @Override
    public ModalBuilder withContext(String key, Object value) {
        if (contextAttributes == null) {
            contextAttributes = new HashMap<>();
        }
        contextAttributes.put(key, value);
        return this;
    }

    @Override
    public Modal build() {
        if (title == null || title.isBlank()) {
            throw new IllegalStateException("Modal must have a title");
        }

        if (inputs.isEmpty()) {
            throw new IllegalStateException("Modal must have at least one text input");
        }

        if (inputs.size() > 5) {
            throw new IllegalStateException("Modal can have at most 5 text inputs");
        }

        String modalId = buildModalId();

        Modal.Builder builder = Modal.create(modalId, title);

        // Each text input goes in its own action row
        for (TextInput input : inputs) {
            builder.addComponents(ActionRow.of(input));
        }

        return builder.build();
    }

    private String buildModalId() {
        // Callback-based modal
        if (callback != null) {
            // Modals don't need message reference for auto-disable
            CallbackEntry entry = new CallbackEntry(
                    callback, ttl, 1, // Modals are typically single-use
                    0, 0, contextAttributes
            );
            String callbackId = registry.register(entry);
            return ComponentId.callback(ComponentId.ComponentType.MODAL, callbackId, data).toRaw();
        }

        // Stateless modal
        if (statelessId != null) {
            return ComponentId.stateless(ComponentId.ComponentType.MODAL, statelessId, data).toRaw();
        }

        throw new IllegalStateException(
                "Modal must have either a callback (onSubmit) or a stateless ID (withId)"
        );
    }
}
