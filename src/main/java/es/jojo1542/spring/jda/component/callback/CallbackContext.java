package es.jojo1542.spring.jda.component.callback;

import java.util.Map;
import java.util.Optional;

/**
 * Context provided to callback handlers with additional metadata and utilities.
 *
 * <p>The context provides access to:
 * <ul>
 *   <li>The original component ID</li>
 *   <li>Any data embedded in the component ID</li>
 *   <li>Custom attributes stored with the callback</li>
 *   <li>Utility methods for callback lifecycle management</li>
 * </ul>
 *
 * @author jojo1542
 */
public interface CallbackContext {

    /**
     * Get the full component ID as it appears in Discord.
     *
     * @return the raw component ID string
     */
    String getComponentId();

    /**
     * Get the callback identifier (without prefix and type).
     *
     * @return the callback UUID or handler name
     */
    String getCallbackId();

    /**
     * Get embedded data from the component ID.
     *
     * <p>For component IDs like "cb:btn:abc123:mydata", this returns "mydata".
     *
     * @return the data portion if present
     */
    Optional<String> getData();

    /**
     * Get embedded data split by a delimiter.
     *
     * <p>For component IDs like "cb:btn:abc123:user:123:action:delete",
     * calling {@code getDataParts(":")} returns ["user", "123", "action", "delete"].
     *
     * @param delimiter the delimiter to split by
     * @return array of data parts, empty array if no data
     */
    String[] getDataParts(String delimiter);

    /**
     * Get embedded data split by colon (default delimiter).
     *
     * @return array of data parts, empty array if no data
     */
    default String[] getDataParts() {
        return getDataParts(":");
    }

    /**
     * Get additional attributes stored with the callback.
     *
     * <p>Attributes can be set when building the component using
     * {@code withContext(key, value)}.
     *
     * @return immutable map of attributes
     */
    Map<String, Object> getAttributes();

    /**
     * Get a specific attribute by key.
     *
     * @param key the attribute key
     * @param <T> the expected type
     * @return the attribute value if present
     */
    @SuppressWarnings("unchecked")
    default <T> Optional<T> getAttribute(String key) {
        return Optional.ofNullable((T) getAttributes().get(key));
    }

    /**
     * Get a specific attribute by key with a default value.
     *
     * @param key          the attribute key
     * @param defaultValue the default value if not present
     * @param <T>          the expected type
     * @return the attribute value or the default
     */
    @SuppressWarnings("unchecked")
    default <T> T getAttribute(String key, T defaultValue) {
        Object value = getAttributes().get(key);
        return value != null ? (T) value : defaultValue;
    }

    /**
     * Invalidate this callback, preventing future invocations.
     *
     * <p>This is useful for single-use scenarios or when you want to
     * manually control callback lifecycle.
     */
    void invalidate();

    /**
     * Check if this callback is still valid.
     *
     * @return true if the callback can still be invoked
     */
    boolean isValid();

    /**
     * Get the remaining invocations for this callback.
     *
     * @return remaining invocations, or -1 if unlimited
     */
    int getRemainingInvocations();
}
