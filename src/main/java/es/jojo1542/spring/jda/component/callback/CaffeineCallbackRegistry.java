package es.jojo1542.spring.jda.component.callback;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import com.github.benmanes.caffeine.cache.RemovalCause;
import es.jojo1542.spring.jda.component.config.ComponentProperties;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import org.checkerframework.checker.index.qual.NonNegative;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Caffeine-based callback registry with automatic expiration and auto-disable.
 *
 * <p>This implementation uses Caffeine cache to provide:
 * <ul>
 *   <li>Per-entry TTL based on callback configuration</li>
 *   <li>Automatic eviction when max size is reached</li>
 *   <li>Optional auto-disable of components when callback expires</li>
 * </ul>
 *
 * @author jojo1542
 */
public class CaffeineCallbackRegistry implements CallbackRegistry {

    private static final Logger log = LoggerFactory.getLogger(CaffeineCallbackRegistry.class);

    private final Cache<String, CallbackEntry> cache;
    private final ComponentProperties.CallbackProperties properties;
    private final Duration defaultTtl;
    private final ObjectProvider<JDA> jdaProvider;

    /**
     * Create a new registry.
     *
     * @param properties  the component properties
     * @param jdaProvider lazy JDA provider for auto-disable feature (avoids circular dependency)
     */
    public CaffeineCallbackRegistry(ComponentProperties properties, ObjectProvider<JDA> jdaProvider) {
        this.properties = properties.getCallback();
        this.defaultTtl = this.properties.getDefaultTtl();
        this.jdaProvider = jdaProvider;

        this.cache = Caffeine.newBuilder()
                .maximumSize(this.properties.getMaxSize())
                .expireAfter(new CallbackExpiry())
                .removalListener(this::onRemoval)
                .build();

        log.info("Initialized CaffeineCallbackRegistry with TTL={}, maxSize={}",
                defaultTtl, this.properties.getMaxSize());
    }

    @Override
    public String register(ComponentCallback<?> callback) {
        return register(callback, defaultTtl);
    }

    @Override
    public String register(ComponentCallback<?> callback, Duration ttl) {
        return register(callback, ttl, 0);
    }

    @Override
    public String register(ComponentCallback<?> callback, Duration ttl, int maxInvocations) {
        return register(callback, ttl, maxInvocations, 0L, 0L);
    }

    @Override
    public String register(ComponentCallback<?> callback, Duration ttl, int maxInvocations,
                           long channelId, long messageId) {
        String id = generateId();
        CallbackEntry entry = new CallbackEntry(callback, ttl, maxInvocations, channelId, messageId, null);
        cache.put(id, entry);

        if (log.isDebugEnabled()) {
            log.debug("Registered callback: id={}, ttl={}, maxInvocations={}, hasMessageRef={}",
                    id, ttl, maxInvocations, entry.hasMessageReference());
        }

        return id;
    }

    /**
     * Register a callback with attributes.
     *
     * @param entry the pre-built callback entry
     * @return the generated callback ID
     */
    public String register(CallbackEntry entry) {
        String id = generateId();
        cache.put(id, entry);

        if (log.isDebugEnabled()) {
            log.debug("Registered callback: id={}, ttl={}, maxInvocations={}",
                    id, entry.ttl(), entry.maxInvocations());
        }

        return id;
    }

    @Override
    public Optional<CallbackEntry> get(String callbackId) {
        CallbackEntry entry = cache.getIfPresent(callbackId);
        if (entry == null) {
            return Optional.empty();
        }

        // Check and handle invocation limit
        if (entry.maxInvocations() > 0) {
            int count = entry.incrementInvocations();
            if (count >= entry.maxInvocations()) {
                cache.invalidate(callbackId);
                if (log.isDebugEnabled()) {
                    log.debug("Callback {} reached max invocations ({}), removed",
                            callbackId, entry.maxInvocations());
                }
            }
        }

        return Optional.of(entry);
    }

    @Override
    public boolean remove(String callbackId) {
        CallbackEntry existing = cache.getIfPresent(callbackId);
        if (existing != null) {
            cache.invalidate(callbackId);
            return true;
        }
        return false;
    }

    @Override
    public long size() {
        return cache.estimatedSize();
    }

    @Override
    public void clear() {
        cache.invalidateAll();
    }

    /**
     * Generate a short unique ID for callbacks.
     *
     * <p>Uses first 8 characters of UUID for space efficiency
     * (component IDs are limited to 100 chars).
     */
    private String generateId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Handle callback removal - implements auto-disable feature.
     */
    private void onRemoval(String key, CallbackEntry entry, RemovalCause cause) {
        if (entry == null) return;

        boolean shouldLog = properties.isLogExpirations();
        boolean shouldDisable = properties.isDisableOnExpire() && entry.hasMessageReference();

        // Log if configured
        if (shouldLog && cause.wasEvicted()) {
            log.debug("Callback expired: id={}, cause={}, channelId={}, messageId={}",
                    key, cause, entry.channelId(), entry.messageId());
        }

        // Auto-disable components if configured
        if (shouldDisable && (cause == RemovalCause.EXPIRED || cause == RemovalCause.SIZE)) {
            disableComponents(entry.channelId(), entry.messageId());
        }
    }

    /**
     * Attempt to disable components on the original message.
     */
    private void disableComponents(long channelId, long messageId) {
        JDA jda = jdaProvider.getIfAvailable();
        if (jda == null) {
            log.warn("Cannot disable components: JDA instance not available");
            return;
        }

        try {
            MessageChannel channel = jda.getChannelById(MessageChannel.class, channelId);
            if (channel == null) {
                log.debug("Cannot disable components: channel {} not found", channelId);
                return;
            }

            channel.retrieveMessageById(messageId).queue(
                    message -> {
                        List<ActionRow> disabledRows = message.getActionRows().stream()
                                .map(LayoutComponent::asDisabled)
                                .map(lc -> (ActionRow) lc)
                                .toList();

                        if (!disabledRows.isEmpty()) {
                            message.editMessageComponents(disabledRows).queue(
                                    success -> log.debug("Disabled components on message {}", messageId),
                                    error -> log.debug("Failed to disable components: {}", error.getMessage())
                            );
                        }
                    },
                    error -> log.debug("Message {} not found for auto-disable: {}",
                            messageId, error.getMessage())
            );
        } catch (Exception e) {
            log.debug("Error during auto-disable: {}", e.getMessage());
        }
    }

    /**
     * Custom expiry policy that uses each entry's TTL.
     */
    private static class CallbackExpiry implements Expiry<String, CallbackEntry> {

        @Override
        public long expireAfterCreate(String key, CallbackEntry entry, long currentTime) {
            return TimeUnit.MILLISECONDS.toNanos(entry.ttl().toMillis());
        }

        @Override
        public long expireAfterUpdate(String key, CallbackEntry entry,
                                      long currentTime, @NonNegative long currentDuration) {
            // Don't change expiration on update
            return currentDuration;
        }

        @Override
        public long expireAfterRead(String key, CallbackEntry entry,
                                    long currentTime, @NonNegative long currentDuration) {
            // Don't extend expiration on read
            return currentDuration;
        }
    }
}
