package es.jojo1542.spring.jda;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a class is a JDA event listener.
 *
 * <p>Classes annotated with {@code @JdaListener} that implement
 * {@link net.dv8tion.jda.api.hooks.EventListener} or extend
 * {@link net.dv8tion.jda.api.hooks.ListenerAdapter} will be automatically
 * registered with the JDA instance.
 *
 * <p>This annotation is a specialization of {@link Component}, allowing
 * annotated classes to be auto-detected through classpath scanning.
 *
 * <p>Example usage:
 * <pre>{@code
 * @JdaListener
 * public class MessageListener extends ListenerAdapter {
 *
 *     @Override
 *     public void onMessageReceived(MessageReceivedEvent event) {
 *         // Handle message
 *     }
 * }
 * }</pre>
 *
 * @author jojo1542
 * @see net.dv8tion.jda.api.hooks.EventListener
 * @see net.dv8tion.jda.api.hooks.ListenerAdapter
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface JdaListener {

    /**
     * The value may indicate a suggestion for a logical component name,
     * to be turned into a Spring bean in case of an autodetected component.
     *
     * @return the suggested component name, if any (or empty String otherwise)
     */
    @AliasFor(annotation = Component.class)
    String value() default "";
}
