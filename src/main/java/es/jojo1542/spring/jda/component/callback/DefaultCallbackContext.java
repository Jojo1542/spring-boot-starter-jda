package es.jojo1542.spring.jda.component.callback;

import es.jojo1542.spring.jda.component.handler.ComponentId;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * Default implementation of {@link CallbackContext}.
 *
 * @author jojo1542
 */
public class DefaultCallbackContext implements CallbackContext {

    private final ComponentId componentId;
    private final CallbackEntry entry;
    private final CallbackRegistry registry;
    private final String callbackId;
    private volatile boolean valid = true;

    /**
     * Create a new context.
     *
     * @param componentId the parsed component ID
     * @param entry       the callback entry
     * @param registry    the registry for invalidation
     * @param callbackId  the callback registry key
     */
    public DefaultCallbackContext(ComponentId componentId, CallbackEntry entry,
                                  CallbackRegistry registry, String callbackId) {
        this.componentId = componentId;
        this.entry = entry;
        this.registry = registry;
        this.callbackId = callbackId;
    }

    @Override
    public String getComponentId() {
        return componentId.toRaw();
    }

    @Override
    public String getCallbackId() {
        return callbackId;
    }

    @Override
    public Optional<String> getData() {
        return Optional.ofNullable(componentId.data());
    }

    @Override
    public String[] getDataParts(String delimiter) {
        String data = componentId.data();
        if (data == null || data.isEmpty()) {
            return new String[0];
        }
        return data.split(delimiter);
    }

    @Override
    public Map<String, Object> getAttributes() {
        return entry != null ? entry.attributes() : Collections.emptyMap();
    }

    @Override
    public void invalidate() {
        if (valid && registry != null) {
            registry.remove(callbackId);
            valid = false;
        }
    }

    @Override
    public boolean isValid() {
        return valid && entry != null && entry.hasRemainingInvocations();
    }

    @Override
    public int getRemainingInvocations() {
        return entry != null ? entry.remainingInvocations() : 0;
    }
}
