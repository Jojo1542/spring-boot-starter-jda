package es.jojo1542.spring.jda.component.callback;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Internal entry wrapper for callbacks with metadata.
 *
 * <p>This record stores the callback along with its configuration
 * such as TTL, invocation limits, and message reference.
 *
 * @author jojo1542
 */
public final class CallbackEntry {

    private final ComponentCallback<?> callback;
    private final Duration ttl;
    private final int maxInvocations;
    private final long createdAt;
    private final AtomicInteger invocationCount;
    private final long channelId;
    private final long messageId;
    private final Map<String, Object> attributes;

    /**
     * Create a new callback entry.
     *
     * @param callback       the callback handler
     * @param ttl            time-to-live duration
     * @param maxInvocations maximum invocations (0 = unlimited)
     * @param channelId      channel ID for auto-disable (0 = disabled)
     * @param messageId      message ID for auto-disable (0 = disabled)
     * @param attributes     custom attributes
     */
    public CallbackEntry(ComponentCallback<?> callback, Duration ttl, int maxInvocations,
                         long channelId, long messageId, Map<String, Object> attributes) {
        this.callback = callback;
        this.ttl = ttl;
        this.maxInvocations = maxInvocations;
        this.createdAt = System.currentTimeMillis();
        this.invocationCount = new AtomicInteger(0);
        this.channelId = channelId;
        this.messageId = messageId;
        this.attributes = attributes != null ? Map.copyOf(attributes) : Collections.emptyMap();
    }

    /**
     * Create a simple callback entry without message reference.
     */
    public CallbackEntry(ComponentCallback<?> callback, Duration ttl, int maxInvocations) {
        this(callback, ttl, maxInvocations, 0L, 0L, null);
    }

    /**
     * Get the callback handler.
     */
    public ComponentCallback<?> callback() {
        return callback;
    }

    /**
     * Get the TTL duration.
     */
    public Duration ttl() {
        return ttl;
    }

    /**
     * Get the maximum invocations allowed.
     */
    public int maxInvocations() {
        return maxInvocations;
    }

    /**
     * Get the creation timestamp.
     */
    public long createdAt() {
        return createdAt;
    }

    /**
     * Get the current invocation count.
     */
    public int invocationCount() {
        return invocationCount.get();
    }

    /**
     * Increment and return the new invocation count.
     */
    public int incrementInvocations() {
        return invocationCount.incrementAndGet();
    }

    /**
     * Get the channel ID for auto-disable.
     *
     * @return channel ID, or 0 if auto-disable is not configured
     */
    public long channelId() {
        return channelId;
    }

    /**
     * Get the message ID for auto-disable.
     *
     * @return message ID, or 0 if auto-disable is not configured
     */
    public long messageId() {
        return messageId;
    }

    /**
     * Check if auto-disable is configured for this callback.
     */
    public boolean hasMessageReference() {
        return channelId != 0 && messageId != 0;
    }

    /**
     * Get custom attributes.
     */
    public Map<String, Object> attributes() {
        return attributes;
    }

    /**
     * Calculate remaining time before expiration.
     *
     * @return remaining duration, or Duration.ZERO if expired
     */
    public Duration remainingTime() {
        long elapsed = System.currentTimeMillis() - createdAt;
        long remaining = ttl.toMillis() - elapsed;
        return remaining > 0 ? Duration.ofMillis(remaining) : Duration.ZERO;
    }

    /**
     * Check if this callback has remaining invocations.
     *
     * @return true if more invocations are allowed
     */
    public boolean hasRemainingInvocations() {
        return maxInvocations == 0 || invocationCount.get() < maxInvocations;
    }

    /**
     * Get the remaining invocations.
     *
     * @return remaining count, or -1 if unlimited
     */
    public int remainingInvocations() {
        if (maxInvocations == 0) {
            return -1;
        }
        return Math.max(0, maxInvocations - invocationCount.get());
    }
}
