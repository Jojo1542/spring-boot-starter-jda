package es.jojo1542.spring.jda.component.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Configuration properties for the component interaction system.
 *
 * <p>These properties can be configured in your {@code application.yml}:
 * <pre>{@code
 * spring:
 *   jda:
 *     components:
 *       enabled: true
 *       callback:
 *         default-ttl: 15m
 *         max-size: 10000
 *         disable-on-expire: true
 *         expired-message: "This interaction has expired."
 * }</pre>
 *
 * @author jojo1542
 */
@ConfigurationProperties(prefix = "spring.jda.components")
public class ComponentProperties {

    /**
     * Whether to enable the component interaction system.
     */
    private boolean enabled = true;

    /**
     * Callback configuration.
     */
    private CallbackProperties callback = new CallbackProperties();

    /**
     * Handler configuration.
     */
    private HandlerProperties handler = new HandlerProperties();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public CallbackProperties getCallback() {
        return callback;
    }

    public void setCallback(CallbackProperties callback) {
        this.callback = callback;
    }

    public HandlerProperties getHandler() {
        return handler;
    }

    public void setHandler(HandlerProperties handler) {
        this.handler = handler;
    }

    /**
     * Configuration properties for callback-based handlers.
     */
    public static class CallbackProperties {

        /**
         * Default time-to-live for callbacks.
         */
        private Duration defaultTtl = Duration.ofMinutes(15);

        /**
         * Maximum number of callbacks to store.
         */
        private int maxSize = 10000;

        /**
         * Whether to log callback expirations.
         */
        private boolean logExpirations = false;

        /**
         * Whether to disable components when their callback expires.
         *
         * <p>When enabled, the system will attempt to edit the original message
         * and disable all components when a callback expires.
         */
        private boolean disableOnExpire = true;

        /**
         * Message to reply with when a callback has expired.
         *
         * <p>Use empty string to silently ignore expired interactions.
         */
        private String expiredMessage = "This interaction has expired.";

        public Duration getDefaultTtl() {
            return defaultTtl;
        }

        public void setDefaultTtl(Duration defaultTtl) {
            this.defaultTtl = defaultTtl;
        }

        public int getMaxSize() {
            return maxSize;
        }

        public void setMaxSize(int maxSize) {
            this.maxSize = maxSize;
        }

        public boolean isLogExpirations() {
            return logExpirations;
        }

        public void setLogExpirations(boolean logExpirations) {
            this.logExpirations = logExpirations;
        }

        public boolean isDisableOnExpire() {
            return disableOnExpire;
        }

        public void setDisableOnExpire(boolean disableOnExpire) {
            this.disableOnExpire = disableOnExpire;
        }

        public String getExpiredMessage() {
            return expiredMessage;
        }

        public void setExpiredMessage(String expiredMessage) {
            this.expiredMessage = expiredMessage;
        }
    }

    /**
     * Configuration properties for annotation-based handlers.
     */
    public static class HandlerProperties {

        /**
         * Message to reply with when no handler is found.
         *
         * <p>Use empty string to silently ignore unknown interactions.
         */
        private String unknownMessage = "Unknown interaction.";

        /**
         * Whether to auto-acknowledge interactions if handler doesn't.
         */
        private boolean autoAcknowledge = false;

        public String getUnknownMessage() {
            return unknownMessage;
        }

        public void setUnknownMessage(String unknownMessage) {
            this.unknownMessage = unknownMessage;
        }

        public boolean isAutoAcknowledge() {
            return autoAcknowledge;
        }

        public void setAutoAcknowledge(boolean autoAcknowledge) {
            this.autoAcknowledge = autoAcknowledge;
        }
    }
}
