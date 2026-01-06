package es.jojo1542.spring.jda;

import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.EnumSet;
import java.util.Set;

/**
 * Configuration properties for JDA (Java Discord API).
 *
 * @author jojo1542
 */
@ConfigurationProperties(prefix = "spring.jda")
public class JdaProperties {

    /**
     * Discord bot token. Required for the bot to authenticate.
     */
    private String token;

    /**
     * Whether to automatically create and start the JDA instance.
     */
    private boolean enabled = true;

    /**
     * Gateway intents to enable. Determines which events the bot receives.
     * Default includes common intents for message and guild events.
     */
    private Set<GatewayIntent> intents = EnumSet.noneOf(GatewayIntent.class);

    /**
     * Cache flags to enable. Controls what data JDA caches in memory.
     */
    private Set<CacheFlag> cacheFlags = EnumSet.noneOf(CacheFlag.class);

    /**
     * Cache flags to disable explicitly.
     */
    private Set<CacheFlag> disabledCacheFlags = EnumSet.noneOf(CacheFlag.class);

    /**
     * Online status of the bot when it connects.
     */
    private OnlineStatus status = OnlineStatus.ONLINE;

    /**
     * Activity settings for the bot's presence.
     */
    private ActivityProperties activity = new ActivityProperties();

    /**
     * Whether to await JDA ready status on startup.
     * If true, the application will wait until JDA is fully connected.
     */
    private boolean awaitReady = true;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Set<GatewayIntent> getIntents() {
        return intents;
    }

    public void setIntents(Set<GatewayIntent> intents) {
        this.intents = intents;
    }

    public Set<CacheFlag> getCacheFlags() {
        return cacheFlags;
    }

    public void setCacheFlags(Set<CacheFlag> cacheFlags) {
        this.cacheFlags = cacheFlags;
    }

    public Set<CacheFlag> getDisabledCacheFlags() {
        return disabledCacheFlags;
    }

    public void setDisabledCacheFlags(Set<CacheFlag> disabledCacheFlags) {
        this.disabledCacheFlags = disabledCacheFlags;
    }

    public OnlineStatus getStatus() {
        return status;
    }

    public void setStatus(OnlineStatus status) {
        this.status = status;
    }

    public ActivityProperties getActivity() {
        return activity;
    }

    public void setActivity(ActivityProperties activity) {
        this.activity = activity;
    }

    public boolean isAwaitReady() {
        return awaitReady;
    }

    public void setAwaitReady(boolean awaitReady) {
        this.awaitReady = awaitReady;
    }

    /**
     * Activity configuration for the bot's Discord presence.
     */
    public static class ActivityProperties {

        /**
         * Type of activity to display.
         */
        private ActivityType type = ActivityType.PLAYING;

        /**
         * Text to display as the activity description.
         */
        private String text;

        /**
         * URL for streaming activity (only used when type is STREAMING).
         */
        private String url;

        public ActivityType getType() {
            return type;
        }

        public void setType(ActivityType type) {
            this.type = type;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        /**
         * Converts this configuration to a JDA Activity instance.
         *
         * @return the Activity, or null if no text is configured
         */
        public Activity toActivity() {
            if (text == null || text.isBlank()) {
                return null;
            }

            return switch (type) {
                case PLAYING -> Activity.playing(text);
                case STREAMING -> Activity.streaming(text, url);
                case LISTENING -> Activity.listening(text);
                case WATCHING -> Activity.watching(text);
                case COMPETING -> Activity.competing(text);
                case CUSTOM -> Activity.customStatus(text);
            };
        }
    }

    /**
     * Supported activity types for the bot's presence.
     */
    public enum ActivityType {
        PLAYING,
        STREAMING,
        LISTENING,
        WATCHING,
        COMPETING,
        CUSTOM
    }
}
