package es.jojo1542.spring.jda.component.annotation;

import java.lang.annotation.*;

/**
 * Injects parsed data from the component ID into a handler parameter.
 *
 * <p>When a component is created with embedded data using {@code withData()},
 * this annotation extracts that data and injects it into the handler method.
 *
 * <p>For a component ID like "sc:btn:ticket-close:123:pending", the data
 * portion is "123:pending". This can be injected as:
 * <ul>
 *   <li>A single String containing all data</li>
 *   <li>Individual parts by specifying an index</li>
 * </ul>
 *
 * <p>Example usage:
 * <pre>{@code
 * @ButtonHandler(value = "ticket-", pattern = true)
 * public void onTicket(
 *         ButtonInteractionEvent event,
 *         @ComponentData String allData,           // "close:123:pending"
 *         @ComponentData(index = 0) String action, // "close"
 *         @ComponentData(index = 1) String id,     // "123"
 *         @ComponentData(index = 2) String status  // "pending"
 * ) {
 *     // Handle the ticket action
 * }
 * }</pre>
 *
 * <p>For pattern-based handlers, the data includes everything after the
 * matched prefix:
 * <pre>{@code
 * // Handler: @ButtonHandler(value = "user-action-", pattern = true)
 * // Button ID: "sc:btn:user-action-delete:456"
 * // @ComponentData gives: "delete:456"
 * // @ComponentData(index = 0) gives: "delete"
 * // @ComponentData(index = 1) gives: "456"
 * }</pre>
 *
 * @author jojo1542
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ComponentData {

    /**
     * The index of the data segment to extract (0-based).
     *
     * <p>Data segments are split by colons. Use -1 (default) to get
     * the entire data string without splitting.
     *
     * @return the segment index, or -1 for the full data string
     */
    int index() default -1;

    /**
     * The delimiter used to split data segments.
     *
     * <p>Default is colon (:).
     *
     * @return the delimiter string
     */
    String delimiter() default ":";

    /**
     * Default value if the data or segment is not present.
     *
     * @return the default value
     */
    String defaultValue() default "";
}
