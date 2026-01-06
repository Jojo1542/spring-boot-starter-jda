package es.jojo1542.spring.jda.component.callback;

import java.time.Duration;
import java.util.Optional;

/**
 * Registry for storing and retrieving component callbacks with TTL support.
 *
 * <p>This registry manages the lifecycle of callback handlers for interactive
 * Discord components. It provides:
 * <ul>
 *   <li>Automatic expiration based on TTL</li>
 *   <li>Optional invocation limits</li>
 *   <li>Optional auto-disable of expired components</li>
 * </ul>
 *
 * @author jojo1542
 * @see CaffeineCallbackRegistry
 */
public interface CallbackRegistry {

    /**
     * Register a callback with default TTL.
     *
     * @param callback the callback to register
     * @return the generated callback ID (8-character UUID)
     */
    String register(ComponentCallback<?> callback);

    /**
     * Register a callback with custom TTL.
     *
     * @param callback the callback to register
     * @param ttl      time-to-live duration
     * @return the generated callback ID
     */
    String register(ComponentCallback<?> callback, Duration ttl);

    /**
     * Register a callback with custom TTL and invocation limit.
     *
     * @param callback       the callback to register
     * @param ttl            time-to-live duration
     * @param maxInvocations maximum number of invocations (0 = unlimited until TTL)
     * @return the generated callback ID
     */
    String register(ComponentCallback<?> callback, Duration ttl, int maxInvocations);

    /**
     * Register a callback with message reference for auto-disable on expiration.
     *
     * @param callback       the callback to register
     * @param ttl            time-to-live duration
     * @param maxInvocations maximum number of invocations
     * @param channelId      the channel ID of the message containing the component
     * @param messageId      the message ID containing the component
     * @return the generated callback ID
     */
    String register(ComponentCallback<?> callback, Duration ttl, int maxInvocations,
                    long channelId, long messageId);

    /**
     * Register a pre-built callback entry.
     *
     * @param entry the callback entry containing all configuration
     * @return the generated callback ID
     */
    String register(CallbackEntry entry);

    /**
     * Retrieve a callback by its ID.
     *
     * <p>This method also handles invocation counting. If the callback has
     * reached its maximum invocations, it will be removed after this call.
     *
     * @param callbackId the callback ID
     * @return the callback if found and not expired
     */
    Optional<CallbackEntry> get(String callbackId);

    /**
     * Remove a callback explicitly.
     *
     * @param callbackId the callback ID to remove
     * @return true if a callback was removed
     */
    boolean remove(String callbackId);

    /**
     * Get the number of active callbacks.
     *
     * @return active callback count
     */
    long size();

    /**
     * Clear all callbacks.
     */
    void clear();
}
