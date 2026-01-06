package es.jojo1542.spring.jda.component.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;

/**
 * Marks a method as a select menu interaction handler.
 *
 * <p>Methods annotated with {@code @SelectMenuHandler} will be invoked when
 * a select menu with a matching ID is used. The method should have the
 * appropriate event type as its first parameter:
 * <ul>
 *   <li>{@link StringSelectInteractionEvent}
 *       for string select menus</li>
 *   <li>{@link EntitySelectInteractionEvent}
 *       for entity select menus</li>
 * </ul>
 *
 * <p>Example usage:
 * <pre>{@code
 * @JdaListener
 * public class MyHandlers extends ListenerAdapter {
 *
 *     @SelectMenuHandler("color-picker")
 *     public void onColorSelect(StringSelectInteractionEvent event) {
 *         String color = event.getValues().get(0);
 *         event.reply("You selected: " + color).queue();
 *     }
 *
 *     @SelectMenuHandler("user-select")
 *     public void onUserSelect(EntitySelectInteractionEvent event) {
 *         List<User> users = event.getMentions().getUsers();
 *         event.reply("Selected " + users.size() + " users").queue();
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
public @interface SelectMenuHandler {

    /**
     * The component ID or prefix to match.
     *
     * @return the ID or prefix to match
     */
    String value();

    /**
     * If true, the value is treated as a prefix pattern.
     *
     * @return true if value should be treated as a prefix
     */
    boolean pattern() default false;
}
