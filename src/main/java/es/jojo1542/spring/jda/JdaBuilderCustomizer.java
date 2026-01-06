package es.jojo1542.spring.jda;

import net.dv8tion.jda.api.JDABuilder;

/**
 * Callback interface for customizing the {@link JDABuilder} before
 * the JDA instance is created.
 *
 * <p>Implement this interface and register it as a Spring bean to apply
 * custom configurations to the JDA builder. Multiple customizers can be
 * registered and will be applied in order.
 *
 * <p>Example usage:
 * <pre>{@code
 * @Bean
 * public JdaBuilderCustomizer myCustomizer() {
 *     return builder -> builder
 *         .enableIntents(GatewayIntent.GUILD_PRESENCES)
 *         .setMemberCachePolicy(MemberCachePolicy.ALL);
 * }
 * }</pre>
 *
 * @author jojo1542
 * @see JdaAutoConfiguration
 */
@FunctionalInterface
public interface JdaBuilderCustomizer {

    /**
     * Customize the given JDA builder.
     *
     * @param builder the JDA builder to customize
     */
    void customize(JDABuilder builder);
}
