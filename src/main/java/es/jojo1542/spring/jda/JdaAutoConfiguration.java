package es.jojo1542.spring.jda;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.hooks.EventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.List;

/**
 * Auto-configuration for JDA (Java Discord API).
 *
 * <p>This configuration is activated when:
 * <ul>
 *   <li>JDA classes are on the classpath</li>
 *   <li>{@code spring.jda.enabled} is {@code true} (default)</li>
 *   <li>No existing {@link JDA} bean is defined</li>
 * </ul>
 *
 * <p>The configuration automatically:
 * <ul>
 *   <li>Creates a {@link JDABuilder} with configured intents and cache flags</li>
 *   <li>Registers all {@link EventListener} beans as JDA listeners</li>
 *   <li>Applies all {@link JdaBuilderCustomizer} beans</li>
 *   <li>Builds and optionally awaits the JDA instance</li>
 * </ul>
 *
 * @author jojo1542
 * @see JdaProperties
 * @see JdaBuilderCustomizer
 */
@AutoConfiguration
@ConditionalOnClass(JDA.class)
@ConditionalOnProperty(prefix = "spring.jda", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(JdaProperties.class)
public class JdaAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(JdaAutoConfiguration.class);

    private final JdaProperties properties;

    public JdaAutoConfiguration(JdaProperties properties) {
        this.properties = properties;
    }

    /**
     * Creates the JDABuilder bean configured with properties.
     *
     * @return the configured JDABuilder
     */
    @Bean
    @ConditionalOnMissingBean
    public JDABuilder jdaBuilder() {
        String token = properties.getToken();
        if (token == null || token.isBlank()) {
            throw new IllegalStateException(
                    "Discord bot token is required. Set 'spring.jda.token' in your configuration."
            );
        }

        log.debug("Creating JDABuilder with {} intents", properties.getIntents().size());

        JDABuilder builder = JDABuilder.create(token, properties.getIntents());

        // Configure cache flags
        if (!properties.getCacheFlags().isEmpty()) {
            builder.enableCache(properties.getCacheFlags());
        }
        if (!properties.getDisabledCacheFlags().isEmpty()) {
            builder.disableCache(properties.getDisabledCacheFlags());
        }

        // Configure status
        builder.setStatus(properties.getStatus());

        // Configure activity
        Activity activity = properties.getActivity().toActivity();
        if (activity != null) {
            builder.setActivity(activity);
        }

        return builder;
    }

    /**
     * Creates the JDA bean by building the JDABuilder.
     *
     * <p>This method:
     * <ul>
     *   <li>Applies all registered {@link JdaBuilderCustomizer} beans</li>
     *   <li>Registers all {@link EventListener} beans</li>
     *   <li>Builds the JDA instance</li>
     *   <li>Optionally awaits ready status based on configuration</li>
     * </ul>
     *
     * @param builder     the JDABuilder to use
     * @param customizers all registered customizers
     * @param listeners   all registered event listeners
     * @return the configured and started JDA instance
     * @throws InterruptedException if awaiting ready is interrupted
     */
    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean
    public JDA jda(
            JDABuilder builder,
            ObjectProvider<JdaBuilderCustomizer> customizers,
            ObjectProvider<EventListener> listeners
    ) throws InterruptedException {

        // Apply all customizers
        customizers.orderedStream().forEach(customizer -> {
            log.debug("Applying JdaBuilderCustomizer: {}", customizer.getClass().getSimpleName());
            customizer.customize(builder);
        });

        // Register all event listeners
        List<EventListener> listenerList = listeners.orderedStream().toList();
        if (!listenerList.isEmpty()) {
            log.info("Registering {} JDA event listener(s)", listenerList.size());
            builder.addEventListeners(listenerList.toArray());
        }

        // Build JDA
        JDA jda = builder.build();
        log.info("JDA instance created, connecting to Discord...");

        // Optionally await ready
        if (properties.isAwaitReady()) {
            log.debug("Awaiting JDA ready status...");
            jda.awaitReady();
            log.info("JDA is ready! Logged in as: {}", jda.getSelfUser().getName());
        }

        return jda;
    }
}
