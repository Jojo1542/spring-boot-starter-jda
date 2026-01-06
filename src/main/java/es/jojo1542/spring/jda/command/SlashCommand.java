package es.jojo1542.spring.jda.command;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a class is a Discord slash command.
 *
 * <p>Classes annotated with {@code @SlashCommand} that extend
 * {@link es.jojo1542.spring.jda.command.BaseCommand} will be automatically
 * registered with the {@link dev.triumphteam.cmd.jda.JdaCommandManager}.
 *
 * <p>This annotation is a specialization of {@link Component}, allowing
 * annotated classes to be auto-detected through classpath scanning.
 *
 * <p>Example usage:
 * <pre>{@code
 * @SlashCommand
 * @Command("ping")
 * public class PingCommand extends BaseCommand {
 *
 *     @Command
 *     public void execute(SlashSender sender) {
 *         sender.reply("Pong!").queue();
 *     }
 * }
 * }</pre>
 *
 * <p>Subcommands can be created by extending the parent command:
 * <pre>{@code
 * @SlashCommand
 * @Command("admin")
 * public class AdminCommand extends BaseCommand {
 *
 *     @Command("ban")
 *     public void ban(SlashSender sender, @Argument User user) {
 *         // Ban logic
 *     }
 * }
 * }</pre>
 *
 * @author jojo1542
 * @see es.jojo1542.spring.jda.command.BaseCommand
 * @see dev.triumphteam.cmd.jda.JdaCommandManager
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface SlashCommand {

    /**
     * The value may indicate a suggestion for a logical component name,
     * to be turned into a Spring bean in case of an autodetected component.
     *
     * @return the suggested component name, if any (or empty String otherwise)
     */
    @AliasFor(annotation = Component.class)
    String value() default "";
}
