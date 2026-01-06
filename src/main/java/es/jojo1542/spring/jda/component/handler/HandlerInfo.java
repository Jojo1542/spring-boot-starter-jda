package es.jojo1542.spring.jda.component.handler;

import java.lang.reflect.Method;

/**
 * Information about a registered component handler.
 *
 * @param bean          the Spring bean containing the handler method
 * @param method        the handler method
 * @param value         the ID or pattern to match
 * @param isPattern     whether the value is a prefix pattern
 * @param componentType the type of component this handler handles
 * @author jojo1542
 */
public record HandlerInfo(
        Object bean,
        Method method,
        String value,
        boolean isPattern,
        ComponentId.ComponentType componentType
) {

    /**
     * Check if this handler matches the given component ID.
     *
     * @param componentId the parsed component ID
     * @return true if this handler should handle the component
     */
    public boolean matches(ComponentId componentId) {
        if (componentType != componentId.componentType()) {
            return false;
        }

        String identifier = componentId.identifier();
        if (isPattern) {
            return identifier.startsWith(value);
        } else {
            return identifier.equals(value);
        }
    }

    /**
     * Extract the data portion for pattern-based handlers.
     *
     * <p>For exact matches, returns the component's data.
     * For pattern matches, returns everything after the pattern prefix
     * plus the component's data (if any).
     *
     * @param componentId the parsed component ID
     * @return the extracted data string, or null if none
     */
    public String extractData(ComponentId componentId) {
        String identifier = componentId.identifier();
        String data = componentId.data();

        if (isPattern && identifier.length() > value.length()) {
            // Extract the part after the pattern prefix
            String afterPrefix = identifier.substring(value.length());
            if (data != null && !data.isEmpty()) {
                return afterPrefix + ":" + data;
            }
            return afterPrefix;
        }

        return data;
    }
}
