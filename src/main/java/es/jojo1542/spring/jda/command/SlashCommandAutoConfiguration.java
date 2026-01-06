package es.jojo1542.spring.jda.command;

import dev.triumphteam.cmd.jda.JdaCommandManager;
import dev.triumphteam.cmd.jda.sender.Sender;
import net.dv8tion.jda.api.JDA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

import es.jojo1542.spring.jda.JdaAutoConfiguration;

import java.util.List;

/**
 * Auto-configuration for Triumph CMDs slash command support.
 *
 * <p>This configuration is activated when:
 * <ul>
 *   <li>Triumph CMDs classes are on the classpath</li>
 *   <li>A {@link JDA} bean exists</li>
 *   <li>{@code spring.jda.commands.enabled} is {@code true} (default)</li>
 * </ul>
 *
 * <p>All beans that extend {@link BaseCommand} and are annotated with
 * {@link SlashCommand} will be automatically registered.
 *
 * @author jojo1542
 * @see SlashCommand
 * @see JdaCommandManager
 */
@AutoConfiguration(after = JdaAutoConfiguration.class)
@ConditionalOnClass({JdaCommandManager.class, BaseCommand.class})
@ConditionalOnBean(JDA.class)
@ConditionalOnProperty(prefix = "spring.jda.commands", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SlashCommandAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(SlashCommandAutoConfiguration.class);

    /**
     * Creates the SlashCommandManager bean.
     *
     * @param jda         the JDA instance
     * @param commands    all registered BaseCommand beans
     * @param customizers optional customizers for the manager
     * @return the configured SlashCommandManager
     */
    @Bean
    @ConditionalOnMissingBean
    public JdaCommandManager<Sender> slashCommandManager(
            JDA jda,
            ObjectProvider<BaseCommand> commands,
            ObjectProvider<JdaCommandManagerCustomizer> customizers
    ) {
        JdaCommandManager<Sender> manager = JdaCommandManager.create(jda);

        // Apply customizers first
        customizers.orderedStream().forEach(customizer -> {
            log.debug("Applying JdaCommandManagerCustomizer: {}", customizer.getClass().getSimpleName());
            customizer.customize(manager);
        });

        // Register all BaseCommand beans
        List<BaseCommand> commandList = commands.orderedStream().toList();
        if (!commandList.isEmpty()) {
            log.info("Registering {} slash command(s)", commandList.size());
            commandList.forEach(command -> {
                log.debug("Registering slash command: {}", command.getClass().getSimpleName());
                manager.registerCommand(command);
            });
            manager.pushCommands();
        }

        return manager;
    }
}
