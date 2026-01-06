package es.jojo1542.spring.jda.component.handler;

import java.util.Objects;

/**
 * Parsed component ID with handler type, component type, identifier, and optional data.
 *
 * <p>Component IDs follow this format:
 * <pre>{@code
 * [prefix]:[type]:[identifier]:[data...]
 *
 * Prefixes:
 *   sc - Stateless component (annotation-based handler)
 *   cb - Callback-based component (registered callback)
 *
 * Types:
 *   btn - Button
 *   sel - String select menu
 *   ent - Entity select menu
 *   mdl - Modal
 *
 * Examples:
 *   sc:btn:confirm-delete:123    → Stateless button with data "123"
 *   cb:btn:a1b2c3d4              → Callback button with ID "a1b2c3d4"
 *   sc:sel:role-picker           → Stateless select menu
 *   cb:mdl:f7e8d9c0:user:456     → Callback modal with data "user:456"
 * }</pre>
 *
 * @author jojo1542
 */
public record ComponentId(
        HandlerType handlerType,
        ComponentType componentType,
        String identifier,
        String data
) {

    /**
     * Type of handler routing.
     */
    public enum HandlerType {
        /**
         * Stateless handler (annotation-based, e.g., @ButtonHandler).
         */
        STATELESS("sc"),

        /**
         * Callback-based handler (registered in CallbackRegistry).
         */
        CALLBACK("cb"),

        /**
         * Unknown/invalid handler type.
         */
        UNKNOWN("");

        private final String prefix;

        HandlerType(String prefix) {
            this.prefix = prefix;
        }

        public String getPrefix() {
            return prefix;
        }

        public static HandlerType fromPrefix(String prefix) {
            for (HandlerType type : values()) {
                if (type.prefix.equals(prefix)) {
                    return type;
                }
            }
            return UNKNOWN;
        }
    }

    /**
     * Type of component.
     */
    public enum ComponentType {
        BUTTON("btn"),
        STRING_SELECT("sel"),
        ENTITY_SELECT("ent"),
        MODAL("mdl"),
        UNKNOWN("");

        private final String code;

        ComponentType(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }

        public static ComponentType fromCode(String code) {
            for (ComponentType type : values()) {
                if (type.code.equals(code)) {
                    return type;
                }
            }
            return UNKNOWN;
        }
    }

    private static final String SEPARATOR = ":";

    /**
     * Parse a raw component ID string.
     *
     * @param rawId the raw ID from Discord
     * @return parsed ComponentId
     */
    public static ComponentId parse(String rawId) {
        if (rawId == null || rawId.isEmpty()) {
            return new ComponentId(HandlerType.UNKNOWN, ComponentType.UNKNOWN, "", null);
        }

        String[] parts = rawId.split(SEPARATOR, 4);

        if (parts.length < 3) {
            // Not our format - treat as legacy/unknown
            return new ComponentId(HandlerType.UNKNOWN, ComponentType.UNKNOWN, rawId, null);
        }

        HandlerType handlerType = HandlerType.fromPrefix(parts[0]);
        ComponentType componentType = ComponentType.fromCode(parts[1]);
        String identifier = parts[2];
        String data = parts.length > 3 ? parts[3] : null;

        return new ComponentId(handlerType, componentType, identifier, data);
    }

    /**
     * Create a stateless component ID.
     *
     * @param type       the component type
     * @param identifier the handler identifier (matches @ButtonHandler value, etc.)
     * @param data       optional data to embed
     * @return the component ID
     */
    public static ComponentId stateless(ComponentType type, String identifier, String data) {
        return new ComponentId(HandlerType.STATELESS, type, identifier, data);
    }

    /**
     * Create a stateless component ID without data.
     */
    public static ComponentId stateless(ComponentType type, String identifier) {
        return stateless(type, identifier, null);
    }

    /**
     * Create a callback-based component ID.
     *
     * @param type       the component type
     * @param callbackId the callback registry ID
     * @param data       optional data to embed
     * @return the component ID
     */
    public static ComponentId callback(ComponentType type, String callbackId, String data) {
        return new ComponentId(HandlerType.CALLBACK, type, callbackId, data);
    }

    /**
     * Create a callback-based component ID without data.
     */
    public static ComponentId callback(ComponentType type, String callbackId) {
        return callback(type, callbackId, null);
    }

    /**
     * Convert this component ID to a raw string for Discord.
     *
     * @return the raw ID string (max 100 chars)
     */
    public String toRaw() {
        StringBuilder sb = new StringBuilder();
        sb.append(handlerType.getPrefix())
                .append(SEPARATOR)
                .append(componentType.getCode())
                .append(SEPARATOR)
                .append(identifier);

        if (data != null && !data.isEmpty()) {
            sb.append(SEPARATOR).append(data);
        }

        String raw = sb.toString();

        // Discord limit is 100 characters
        if (raw.length() > 100) {
            throw new IllegalStateException(
                    "Component ID exceeds 100 character limit: " + raw.length() + " chars"
            );
        }

        return raw;
    }

    /**
     * Check if this is a valid component ID (not unknown).
     */
    public boolean isValid() {
        return handlerType != HandlerType.UNKNOWN && componentType != ComponentType.UNKNOWN;
    }

    /**
     * Check if this is a callback-based component.
     */
    public boolean isCallback() {
        return handlerType == HandlerType.CALLBACK;
    }

    /**
     * Check if this is a stateless component.
     */
    public boolean isStateless() {
        return handlerType == HandlerType.STATELESS;
    }

    /**
     * Check if this component has embedded data.
     */
    public boolean hasData() {
        return data != null && !data.isEmpty();
    }

    /**
     * Get the stateless handler ID for pattern matching.
     *
     * <p>For ID "sc:btn:ticket-close:123", returns "ticket-close".
     * For pattern-based handlers, check if the identifier starts with
     * the handler's value.
     *
     * @return the identifier for handler matching
     */
    public String getHandlerId() {
        return identifier;
    }

    @Override
    public String toString() {
        return toRaw();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ComponentId that = (ComponentId) o;
        return handlerType == that.handlerType &&
                componentType == that.componentType &&
                Objects.equals(identifier, that.identifier) &&
                Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(handlerType, componentType, identifier, data);
    }
}
