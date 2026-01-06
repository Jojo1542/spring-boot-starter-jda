package es.jojo1542.spring.jda.component.handler;

import java.util.Optional;

/**
 * Registry for annotation-based component handlers.
 *
 * <p>This registry stores handlers discovered from Spring beans that are
 * annotated with {@code @ButtonHandler}, {@code @SelectMenuHandler}, or
 * {@code @ModalHandler}.
 *
 * @author jojo1542
 */
public interface ComponentHandlerRegistry {

    /**
     * Register all handlers found in a bean.
     *
     * <p>Scans the bean's methods for handler annotations and registers
     * them for later lookup.
     *
     * @param bean the Spring bean to scan
     */
    void registerHandlers(Object bean);

    /**
     * Find a handler for the given component ID.
     *
     * @param componentId the parsed component ID
     * @return the handler info if found
     */
    Optional<HandlerInfo> findHandler(ComponentId componentId);

    /**
     * Get the number of registered handlers.
     *
     * @return the handler count
     */
    int getHandlerCount();

    /**
     * Clear all registered handlers.
     */
    void clear();
}
