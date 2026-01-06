package es.jojo1542.spring.jda.component.builder;

import es.jojo1542.spring.jda.component.callback.CallbackRegistry;
import es.jojo1542.spring.jda.component.config.ComponentProperties;

/**
 * Default implementation of {@link ComponentBuilder}.
 *
 * <p>This is the main entry point for building Discord components.
 * It creates new builder instances for each component type.
 *
 * @author jojo1542
 */
public class DefaultComponentBuilder implements ComponentBuilder {

    private final CallbackRegistry registry;
    private final ComponentProperties properties;

    /**
     * Create a new component builder.
     *
     * @param registry   the callback registry for storing callbacks
     * @param properties the component configuration properties
     */
    public DefaultComponentBuilder(CallbackRegistry registry, ComponentProperties properties) {
        this.registry = registry;
        this.properties = properties;
    }

    @Override
    public ButtonBuilder button() {
        return new DefaultButtonBuilder(registry, properties);
    }

    @Override
    public SelectMenuBuilder selectMenu() {
        return new DefaultSelectMenuBuilder(registry, properties);
    }

    @Override
    public EntitySelectBuilder entitySelect() {
        return new DefaultEntitySelectBuilder(registry, properties);
    }

    @Override
    public ModalBuilder modal() {
        return new DefaultModalBuilder(registry, properties);
    }
}
