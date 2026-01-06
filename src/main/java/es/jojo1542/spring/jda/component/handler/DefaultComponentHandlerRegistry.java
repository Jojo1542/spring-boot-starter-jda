package es.jojo1542.spring.jda.component.handler;

import es.jojo1542.spring.jda.component.annotation.ButtonHandler;
import es.jojo1542.spring.jda.component.annotation.ModalHandler;
import es.jojo1542.spring.jda.component.annotation.SelectMenuHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default implementation of {@link ComponentHandlerRegistry}.
 *
 * <p>Scans beans for handler annotations and stores them for efficient lookup.
 *
 * @author jojo1542
 */
public class DefaultComponentHandlerRegistry implements ComponentHandlerRegistry {

    private static final Logger log = LoggerFactory.getLogger(DefaultComponentHandlerRegistry.class);

    // Exact match handlers: type -> id -> handler
    private final Map<ComponentId.ComponentType, Map<String, HandlerInfo>> exactHandlers =
            new ConcurrentHashMap<>();

    // Pattern handlers: type -> list of handlers (checked in order)
    private final Map<ComponentId.ComponentType, List<HandlerInfo>> patternHandlers =
            new ConcurrentHashMap<>();

    public DefaultComponentHandlerRegistry() {
        // Initialize maps for each component type
        for (ComponentId.ComponentType type : ComponentId.ComponentType.values()) {
            exactHandlers.put(type, new ConcurrentHashMap<>());
            patternHandlers.put(type, Collections.synchronizedList(new ArrayList<>()));
        }
    }

    @Override
    public void registerHandlers(Object bean) {
        Class<?> beanClass = bean.getClass();

        for (Method method : beanClass.getDeclaredMethods()) {
            // Check for ButtonHandler
            ButtonHandler buttonHandler = method.getAnnotation(ButtonHandler.class);
            if (buttonHandler != null) {
                registerHandler(bean, method, buttonHandler.value(), buttonHandler.pattern(),
                        ComponentId.ComponentType.BUTTON);
            }

            // Check for SelectMenuHandler
            SelectMenuHandler selectHandler = method.getAnnotation(SelectMenuHandler.class);
            if (selectHandler != null) {
                // Register for both STRING_SELECT and ENTITY_SELECT
                registerHandler(bean, method, selectHandler.value(), selectHandler.pattern(),
                        ComponentId.ComponentType.STRING_SELECT);
                registerHandler(bean, method, selectHandler.value(), selectHandler.pattern(),
                        ComponentId.ComponentType.ENTITY_SELECT);
            }

            // Check for ModalHandler
            ModalHandler modalHandler = method.getAnnotation(ModalHandler.class);
            if (modalHandler != null) {
                registerHandler(bean, method, modalHandler.value(), modalHandler.pattern(),
                        ComponentId.ComponentType.MODAL);
            }
        }
    }

    private void registerHandler(Object bean, Method method, String value, boolean isPattern,
                                 ComponentId.ComponentType type) {
        method.setAccessible(true);

        HandlerInfo info = new HandlerInfo(bean, method, value, isPattern, type);

        if (isPattern) {
            patternHandlers.get(type).add(info);
            log.debug("Registered pattern handler: {}#{} for {}:{}*",
                    bean.getClass().getSimpleName(), method.getName(), type, value);
        } else {
            exactHandlers.get(type).put(value, info);
            log.debug("Registered exact handler: {}#{} for {}:{}",
                    bean.getClass().getSimpleName(), method.getName(), type, value);
        }
    }

    @Override
    public Optional<HandlerInfo> findHandler(ComponentId componentId) {
        if (!componentId.isStateless()) {
            return Optional.empty();
        }

        ComponentId.ComponentType type = componentId.componentType();
        String identifier = componentId.identifier();

        // First, try exact match
        HandlerInfo exact = exactHandlers.get(type).get(identifier);
        if (exact != null) {
            return Optional.of(exact);
        }

        // Then, try pattern matching
        List<HandlerInfo> patterns = patternHandlers.get(type);
        for (HandlerInfo pattern : patterns) {
            if (pattern.matches(componentId)) {
                return Optional.of(pattern);
            }
        }

        return Optional.empty();
    }

    @Override
    public int getHandlerCount() {
        int count = 0;
        for (Map<String, HandlerInfo> map : exactHandlers.values()) {
            count += map.size();
        }
        for (List<HandlerInfo> list : patternHandlers.values()) {
            count += list.size();
        }
        return count;
    }

    @Override
    public void clear() {
        for (Map<String, HandlerInfo> map : exactHandlers.values()) {
            map.clear();
        }
        for (List<HandlerInfo> list : patternHandlers.values()) {
            list.clear();
        }
    }
}
