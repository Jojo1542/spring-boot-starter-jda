package es.jojo1542.spring.jda.command;

import dev.triumphteam.cmd.jda.JdaCommandManager;
import dev.triumphteam.cmd.jda.sender.Sender;

/**
 * Callback interface for customizing the {@link JdaCommandManager} before
 * commands are registered.
 *
 * <p>Implement this interface and register it as a Spring bean to apply
 * custom configurations to the slash command manager. Multiple customizers
 * can be registered and will be applied in order.
 *
 * <p>Use this to configure:
 * <ul>
 *   <li>Custom argument resolvers</li>
 *   <li>Message providers</li>
 *   <li>Suggestion providers</li>
 *   <li>Custom sender mappers</li>
 * </ul>
 *
 * <p>Example usage:
 * <pre>{@code
 * @Bean
 * public SlashCommandManagerCustomizer myCustomizer() {
 *     return manager -> {
 *         manager.registerArgument(MyType.class, (sender, arg) -> MyType.parse(arg));
 *         manager.registerSuggestion(MyType.class, (sender, context) -> List.of("option1", "option2"));
 *     };
 * }
 * }</pre>
 *
 * @author jojo1542
 * @see SlashCommandAutoConfiguration
 */
@FunctionalInterface
public interface JdaCommandManagerCustomizer {

    /**
     * Customize the given slash command manager.
     *
     * @param manager the slash command manager to customize
     */
    void customize(JdaCommandManager<Sender> manager);
}
