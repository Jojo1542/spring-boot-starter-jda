package es.jojo1542.spring.jda.component.config;

import es.jojo1542.spring.jda.JdaAutoConfiguration;
import es.jojo1542.spring.jda.component.annotation.ButtonHandler;
import es.jojo1542.spring.jda.component.annotation.ModalHandler;
import es.jojo1542.spring.jda.component.annotation.SelectMenuHandler;
import es.jojo1542.spring.jda.component.builder.ComponentBuilder;
import es.jojo1542.spring.jda.component.builder.DefaultComponentBuilder;
import es.jojo1542.spring.jda.component.callback.CaffeineCallbackRegistry;
import es.jojo1542.spring.jda.component.callback.CallbackRegistry;
import es.jojo1542.spring.jda.component.handler.ComponentHandlerRegistry;
import es.jojo1542.spring.jda.component.handler.DefaultComponentHandlerRegistry;
import es.jojo1542.spring.jda.component.handler.InteractionRouter;
import net.dv8tion.jda.api.JDA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import java.lang.reflect.Method;

/**
 * Auto-configuration for the component interaction system.
 *
 * <p>This configuration is activated when:
 * <ul>
 *   <li>JDA classes are on the classpath</li>
 *   <li>A JDA bean exists</li>
 *   <li>{@code spring.jda.components.enabled} is {@code true} (default)</li>
 * </ul>
 *
 * <p>This configuration provides:
 * <ul>
 *   <li>{@link CallbackRegistry} for storing callback-based handlers</li>
 *   <li>{@link ComponentHandlerRegistry} for annotation-based handlers</li>
 *   <li>{@link ComponentBuilder} for creating components</li>
 *   <li>{@link InteractionRouter} for routing interactions to handlers</li>
 * </ul>
 *
 * @author jojo1542
 * @see ComponentProperties
 */
@AutoConfiguration(after = JdaAutoConfiguration.class)
@ConditionalOnClass(JDA.class)
@ConditionalOnBean(JDA.class)
@ConditionalOnProperty(prefix = "spring.jda.components", name = "enabled",
        havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(ComponentProperties.class)
public class ComponentAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(ComponentAutoConfiguration.class);

    /**
     * Creates the callback registry for storing temporary callbacks.
     *
     * @param properties  the component properties
     * @param jdaProvider lazy JDA provider for auto-disable feature (avoids circular dependency)
     * @return the callback registry
     */
    @Bean
    @ConditionalOnMissingBean
    public CallbackRegistry callbackRegistry(ComponentProperties properties,
                                              ObjectProvider<JDA> jdaProvider) {
        log.info("Creating CallbackRegistry with TTL={}, maxSize={}",
                properties.getCallback().getDefaultTtl(),
                properties.getCallback().getMaxSize());
        return new CaffeineCallbackRegistry(properties, jdaProvider);
    }

    /**
     * Creates the handler registry for annotation-based handlers.
     *
     * <p>The registry is populated after all singletons are instantiated
     * via {@link #componentHandlerScanner} to avoid circular dependencies.
     *
     * @return the empty handler registry
     */
    @Bean
    @ConditionalOnMissingBean
    public ComponentHandlerRegistry componentHandlerRegistry() {
        return new DefaultComponentHandlerRegistry();
    }

    /**
     * Scans all beans for component handlers after all singletons are created.
     *
     * <p>Uses {@link SmartInitializingSingleton} to defer scanning until
     * after the application context is fully initialized, avoiding circular
     * dependency issues with JDA and EventListener collection.
     *
     * @param registry the handler registry to populate
     * @param context  the application context for bean lookup
     * @return the scanner singleton
     */
    @Bean
    public SmartInitializingSingleton componentHandlerScanner(
            ComponentHandlerRegistry registry,
            ApplicationContext context) {
        return () -> {
            if (registry instanceof DefaultComponentHandlerRegistry defaultRegistry) {
                context.getBeansOfType(Object.class).values().stream()
                        .filter(this::hasComponentHandlers)
                        .forEach(defaultRegistry::registerHandlers);

                log.info("Registered {} component handler(s)", defaultRegistry.getHandlerCount());
            }
        };
    }

    /**
     * Creates the component builder for creating components.
     *
     * @param callbackRegistry the callback registry
     * @param properties       the component properties
     * @return the component builder
     */
    @Bean
    @ConditionalOnMissingBean
    public ComponentBuilder componentBuilder(CallbackRegistry callbackRegistry,
                                              ComponentProperties properties) {
        return new DefaultComponentBuilder(callbackRegistry, properties);
    }

    /**
     * Creates the interaction router for handling component interactions.
     *
     * <p>This bean implements {@link net.dv8tion.jda.api.hooks.EventListener}
     * and is automatically registered with JDA by {@link es.jojo1542.spring.jda.JdaAutoConfiguration}.
     *
     * @param callbackRegistry the callback registry
     * @param handlerRegistry  the handler registry
     * @param properties       the component properties
     * @return the interaction router
     */
    @Bean
    @ConditionalOnMissingBean
    public InteractionRouter interactionRouter(CallbackRegistry callbackRegistry,
                                                ComponentHandlerRegistry handlerRegistry,
                                                ComponentProperties properties) {
        log.info("Created InteractionRouter for component interactions");
        return new InteractionRouter(callbackRegistry, handlerRegistry, properties);
    }

    /**
     * Check if a bean has any component handler annotations.
     */
    private boolean hasComponentHandlers(Object bean) {
        Class<?> beanClass = bean.getClass();
        for (Method method : beanClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(ButtonHandler.class) ||
                method.isAnnotationPresent(SelectMenuHandler.class) ||
                method.isAnnotationPresent(ModalHandler.class)) {
                return true;
            }
        }
        return false;
    }
}
