package es.jojo1542.spring.jda.component.handler;

import es.jojo1542.spring.jda.component.annotation.ComponentData;
import es.jojo1542.spring.jda.component.callback.*;
import es.jojo1542.spring.jda.component.config.ComponentProperties;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Optional;

/**
 * Central router for all component interactions.
 *
 * <p>This listener routes component interactions to either:
 * <ul>
 *   <li>Callback-based handlers stored in the {@link CallbackRegistry}</li>
 *   <li>Annotation-based handlers registered in the {@link ComponentHandlerRegistry}</li>
 * </ul>
 *
 * @author jojo1542
 */
public class InteractionRouter extends ListenerAdapter {

    private static final Logger log = LoggerFactory.getLogger(InteractionRouter.class);

    private final CallbackRegistry callbackRegistry;
    private final ComponentHandlerRegistry handlerRegistry;
    private final ComponentProperties properties;

    public InteractionRouter(CallbackRegistry callbackRegistry,
                             ComponentHandlerRegistry handlerRegistry,
                             ComponentProperties properties) {
        this.callbackRegistry = callbackRegistry;
        this.handlerRegistry = handlerRegistry;
        this.properties = properties;
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        route(event, event.getComponentId(), ComponentId.ComponentType.BUTTON);
    }

    @Override
    public void onStringSelectInteraction(@NotNull StringSelectInteractionEvent event) {
        route(event, event.getComponentId(), ComponentId.ComponentType.STRING_SELECT);
    }

    @Override
    public void onEntitySelectInteraction(@NotNull EntitySelectInteractionEvent event) {
        route(event, event.getComponentId(), ComponentId.ComponentType.ENTITY_SELECT);
    }

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        routeModal(event, event.getModalId());
    }

    /**
     * Route a component interaction to the appropriate handler.
     */
    private void route(GenericComponentInteractionCreateEvent event, String rawId,
                       ComponentId.ComponentType expectedType) {
        ComponentId componentId = ComponentId.parse(rawId);

        // Check if it's one of our managed components
        if (!componentId.isValid()) {
            // Not our format, ignore
            return;
        }

        if (componentId.isCallback()) {
            routeToCallback(event, componentId);
        } else if (componentId.isStateless()) {
            routeToHandler(event, componentId);
        }
    }

    /**
     * Route a modal interaction.
     */
    private void routeModal(ModalInteractionEvent event, String rawId) {
        ComponentId componentId = ComponentId.parse(rawId);

        if (!componentId.isValid()) {
            return;
        }

        if (componentId.isCallback()) {
            routeModalToCallback(event, componentId);
        } else if (componentId.isStateless()) {
            routeModalToHandler(event, componentId);
        }
    }

    /**
     * Route to a callback-based handler.
     */
    @SuppressWarnings("unchecked")
    private void routeToCallback(GenericComponentInteractionCreateEvent event, ComponentId componentId) {
        String callbackId = componentId.identifier();

        Optional<CallbackEntry> entryOpt = callbackRegistry.get(callbackId);

        if (entryOpt.isEmpty()) {
            handleExpiredCallback(event);
            return;
        }

        CallbackEntry entry = entryOpt.get();
        CallbackContext context = new DefaultCallbackContext(componentId, entry, callbackRegistry, callbackId);

        try {
            ComponentCallback<GenericInteractionCreateEvent> callback =
                    (ComponentCallback<GenericInteractionCreateEvent>) entry.callback();
            callback.handle(event, context);
        } catch (Exception e) {
            log.error("Error executing callback for component {}", componentId, e);
            handleCallbackError(event, e);
        }
    }

    /**
     * Route modal to a callback-based handler.
     */
    @SuppressWarnings("unchecked")
    private void routeModalToCallback(ModalInteractionEvent event, ComponentId componentId) {
        String callbackId = componentId.identifier();

        Optional<CallbackEntry> entryOpt = callbackRegistry.get(callbackId);

        if (entryOpt.isEmpty()) {
            handleExpiredModal(event);
            return;
        }

        CallbackEntry entry = entryOpt.get();
        CallbackContext context = new DefaultCallbackContext(componentId, entry, callbackRegistry, callbackId);

        try {
            ComponentCallback<GenericInteractionCreateEvent> callback =
                    (ComponentCallback<GenericInteractionCreateEvent>) entry.callback();
            callback.handle(event, context);
        } catch (Exception e) {
            log.error("Error executing modal callback for {}", componentId, e);
            handleModalError(event, e);
        }
    }

    /**
     * Route to an annotation-based handler.
     */
    private void routeToHandler(GenericComponentInteractionCreateEvent event, ComponentId componentId) {
        Optional<HandlerInfo> handlerOpt = handlerRegistry.findHandler(componentId);

        if (handlerOpt.isEmpty()) {
            handleUnknownComponent(event);
            return;
        }

        HandlerInfo handler = handlerOpt.get();
        invokeHandler(handler, event, componentId);
    }

    /**
     * Route modal to an annotation-based handler.
     */
    private void routeModalToHandler(ModalInteractionEvent event, ComponentId componentId) {
        Optional<HandlerInfo> handlerOpt = handlerRegistry.findHandler(componentId);

        if (handlerOpt.isEmpty()) {
            handleUnknownModal(event);
            return;
        }

        HandlerInfo handler = handlerOpt.get();
        invokeModalHandler(handler, event, componentId);
    }

    /**
     * Invoke a handler method with the appropriate parameters.
     */
    private void invokeHandler(HandlerInfo handler, GenericComponentInteractionCreateEvent event,
                               ComponentId componentId) {
        Method method = handler.method();
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];
        String data = handler.extractData(componentId);

        for (int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i];
            Class<?> type = param.getType();

            // First parameter should be the event
            if (i == 0 && GenericComponentInteractionCreateEvent.class.isAssignableFrom(type)) {
                args[i] = event;
                continue;
            }

            // Check for @ComponentData annotation
            ComponentData dataAnnotation = param.getAnnotation(ComponentData.class);
            if (dataAnnotation != null) {
                args[i] = extractComponentData(data, dataAnnotation, type);
                continue;
            }

            // Unknown parameter, set to null
            args[i] = null;
        }

        try {
            method.invoke(handler.bean(), args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            log.error("Error invoking handler {}#{}", handler.bean().getClass().getSimpleName(),
                    method.getName(), e);
            handleHandlerError(event, e);
        }
    }

    /**
     * Invoke a modal handler method.
     */
    private void invokeModalHandler(HandlerInfo handler, ModalInteractionEvent event,
                                    ComponentId componentId) {
        Method method = handler.method();
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];
        String data = handler.extractData(componentId);

        for (int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i];
            Class<?> type = param.getType();

            if (i == 0 && ModalInteractionEvent.class.isAssignableFrom(type)) {
                args[i] = event;
                continue;
            }

            ComponentData dataAnnotation = param.getAnnotation(ComponentData.class);
            if (dataAnnotation != null) {
                args[i] = extractComponentData(data, dataAnnotation, type);
                continue;
            }

            args[i] = null;
        }

        try {
            method.invoke(handler.bean(), args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            log.error("Error invoking modal handler {}#{}", handler.bean().getClass().getSimpleName(),
                    method.getName(), e);
            handleModalError(event, e.getCause() != null ? e.getCause() : e);
        }
    }

    /**
     * Extract data from component ID based on @ComponentData annotation.
     */
    private Object extractComponentData(String data, ComponentData annotation, Class<?> targetType) {
        if (data == null || data.isEmpty()) {
            String defaultValue = annotation.defaultValue();
            return defaultValue.isEmpty() ? null : convertToType(defaultValue, targetType);
        }

        int index = annotation.index();
        String delimiter = annotation.delimiter();

        if (index < 0) {
            // Return full data string
            return convertToType(data, targetType);
        }

        // Split and get specific index
        String[] parts = data.split(delimiter);
        if (index >= parts.length) {
            String defaultValue = annotation.defaultValue();
            return defaultValue.isEmpty() ? null : convertToType(defaultValue, targetType);
        }

        return convertToType(parts[index], targetType);
    }

    /**
     * Convert a string value to the target type.
     */
    private Object convertToType(String value, Class<?> targetType) {
        if (targetType == String.class) {
            return value;
        }
        if (targetType == int.class || targetType == Integer.class) {
            return Integer.parseInt(value);
        }
        if (targetType == long.class || targetType == Long.class) {
            return Long.parseLong(value);
        }
        if (targetType == boolean.class || targetType == Boolean.class) {
            return Boolean.parseBoolean(value);
        }
        return value;
    }

    private void handleExpiredCallback(GenericComponentInteractionCreateEvent event) {
        String message = properties.getCallback().getExpiredMessage();
        if (message != null && !message.isEmpty()) {
            event.reply(message).setEphemeral(true).queue();
        }
    }

    private void handleExpiredModal(ModalInteractionEvent event) {
        String message = properties.getCallback().getExpiredMessage();
        if (message != null && !message.isEmpty()) {
            event.reply(message).setEphemeral(true).queue();
        }
    }

    private void handleUnknownComponent(GenericComponentInteractionCreateEvent event) {
        String message = properties.getHandler().getUnknownMessage();
        if (message != null && !message.isEmpty()) {
            event.reply(message).setEphemeral(true).queue();
        }
    }

    private void handleUnknownModal(ModalInteractionEvent event) {
        String message = properties.getHandler().getUnknownMessage();
        if (message != null && !message.isEmpty()) {
            event.reply(message).setEphemeral(true).queue();
        }
    }

    private void handleCallbackError(GenericComponentInteractionCreateEvent event, Exception e) {
        if (!event.isAcknowledged()) {
            event.reply("An error occurred processing your request.")
                    .setEphemeral(true)
                    .queue();
        }
    }

    private void handleHandlerError(GenericComponentInteractionCreateEvent event, Exception e) {
        if (!event.isAcknowledged()) {
            event.reply("An error occurred processing your request.")
                    .setEphemeral(true)
                    .queue();
        }
    }

    private void handleModalError(ModalInteractionEvent event, Throwable e) {
        if (!event.isAcknowledged()) {
            event.reply("An error occurred processing your request.")
                    .setEphemeral(true)
                    .queue();
        }
    }
}
