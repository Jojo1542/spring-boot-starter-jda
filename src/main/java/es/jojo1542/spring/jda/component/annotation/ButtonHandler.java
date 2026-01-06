package es.jojo1542.spring.jda.component.annotation;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import es.jojo1542.spring.jda.component.annotation.ComponentData;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as a button interaction handler.
 *
 * <p>Methods annotated with {@code @ButtonHandler} will be invoked when
 * a button with a matching ID is clicked. The method must have a
 * {@link ButtonInteractionEvent}
 * as its first parameter.
 *
 * <p>Example usage:
 * <pre>{@code
 * @JdaListener
 * public class MyHandlers extends ListenerAdapter {
 *
 *     // Exact match handler
 *     @ButtonHandler("confirm-delete")
 *     public void onConfirmDelete(ButtonInteractionEvent event) {
 *         event.reply("Deleted!").setEphemeral(true).queue();
 *     }
 *
 *     // Pattern-based handler (matches any ID starting with "ticket-")
 *     @ButtonHandler(value = "ticket-", pattern = true)
 *     public void onTicketAction(ButtonInteractionEvent event, @ComponentData String action) {
 *         // For ID "sc:btn:ticket-close:123", action would be "close:123"
 *         event.reply("Ticket action: " + action).queue();
 *     }
 * }
 * }</pre>
 *
 * @author jojo1542
 * @see ComponentData
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ButtonHandler {

    /**
     * The component ID or prefix to match.
     *
     * <p>For stateless buttons created with {@code withId("confirm-delete")},
     * use the same value here.
     *
     * @return the ID or prefix to match
     */
    String value();

    /**
     * If true, the value is treated as a prefix pattern.
     *
     * <p>When enabled, the handler will match any ID that starts with
     * the specified value. This is useful for handling groups of related
     * buttons.
     *
     * @return true if value should be treated as a prefix
     */
    boolean pattern() default false;
}
