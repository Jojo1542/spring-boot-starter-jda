package es.jojo1542.spring.jda.component.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;

/**
 * Marks a method as a modal submission handler.
 *
 * <p>Methods annotated with {@code @ModalHandler} will be invoked when
 * a modal with a matching ID is submitted. The method must have a
 * {@link ModalInteractionEvent}
 * as its first parameter.
 *
 * <p>Example usage:
 * <pre>{@code
 * @JdaListener
 * public class MyHandlers extends ListenerAdapter {
 *
 *     @ModalHandler("feedback-form")
 *     public void onFeedback(ModalInteractionEvent event) {
 *         String subject = event.getValue("subject").getAsString();
 *         String details = event.getValue("details").getAsString();
 *
 *         event.reply("Thanks for your feedback!")
 *             .setEphemeral(true)
 *             .queue();
 *     }
 *
 *     @ModalHandler(value = "report-", pattern = true)
 *     public void onReport(ModalInteractionEvent event, @ComponentData String reportType) {
 *         // For modal ID "sc:mdl:report-bug:123", reportType would be "bug:123"
 *         event.reply("Report submitted: " + reportType).queue();
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
public @interface ModalHandler {

    /**
     * The modal ID or prefix to match.
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
