package com.nukkitx.event;

import com.google.common.base.Preconditions;
import com.nukkitx.api.event.Event;
import com.nukkitx.api.event.EventFireHandler;
import com.nukkitx.api.event.EventManager;
import com.nukkitx.api.event.Listener;
import com.nukkitx.event.firehandler.ReflectionEventFireHandler;
import lombok.extern.log4j.Log4j2;

import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.reflect.Method;
import java.util.*;

@Log4j2
@ParametersAreNonnullByDefault
public class SimpleEventManager implements EventManager {
    private final Map<Object, List<Object>> listenersByPlugin = new HashMap<>();
    private final Deque<Object> listeners = new ArrayDeque<>();
    private final Object registerLock = new Object();
    private volatile Map<Class<? extends Event>, EventFireHandler> eventHandlers = Collections.emptyMap();

    @Override
    public void registerListeners(Object plugin, Object listener) {
        Preconditions.checkNotNull(plugin, "plugin");
        Preconditions.checkNotNull(listener, "listener");

        // Verify that all listeners are valid.
        boolean validListener = false;
        for (Method method : listener.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(Listener.class)) {
                if (method.isBridge() || method.isSynthetic()) {
                    continue;
                }
                if (method.getParameterCount() != 1) {
                    throw new IllegalArgumentException("Method " + method.getName() + " in " + listener + " does not accept only one parameter.");
                }

                Class<?> eventClass = method.getParameterTypes()[0];

                if (!Event.class.isAssignableFrom(eventClass)) {
                    throw new IllegalArgumentException("Method " + method.getName() + " in " + listener + " does not accept a subclass of Event.");
                }

                if (eventClass.getAnnotation(Deprecated.class) != null) {
                    log.warn("{} registered deprecated event {} in {}", plugin.getClass().getSimpleName(),
                            eventClass.getSimpleName(), listener.getClass().getSimpleName());
                }

                method.setAccessible(true);
                validListener = true;
            }
        }

        if (validListener) {
            synchronized (registerLock) {
                listenersByPlugin.computeIfAbsent(plugin, k -> new ArrayList<>()).add(listener);
                listeners.add(listener);
                bakeHandlers();
            }
        }
    }

    @Override
    public void fire(Event event) {
        Preconditions.checkNotNull(event, "event");
        EventFireHandler handler = eventHandlers.get(event.getClass());
        if (handler != null) {
            handler.fire(event);
        }
    }

    @Override
    public void deregisterListener(Object listener) {
        Preconditions.checkNotNull(listener, "listener");
        synchronized (registerLock) {
            for (List<Object> listeners : listenersByPlugin.values()) {
                listeners.remove(listener);
            }
            listeners.remove(listener);
            bakeHandlers();
        }
    }

    @Override
    public void deregisterAllListeners(Object plugin) {
        Preconditions.checkNotNull(plugin, "plugin");
        synchronized (registerLock) {
            List<Object> listeners = listenersByPlugin.remove(plugin);
            if (listeners != null) {
                this.listeners.removeAll(listeners);
                bakeHandlers();
            }
        }
    }

    @Override
    public void deregisterListeners(Collection<Object> listeners) {
        Preconditions.checkNotNull(listeners, "listeners");
        synchronized (registerLock) {
            if (listeners.size() > 0) {
                this.listeners.removeAll(listeners);
                bakeHandlers();
            }
        }
    }

    public List<EventFireHandler.ListenerMethod> getEventListenerMethods(Class<? extends Event> eventClass) {
        Preconditions.checkNotNull(eventClass, "eventClass");
        return eventHandlers.get(eventClass).getMethods();
    }

    @SuppressWarnings("unchecked")
    private void bakeHandlers() {
        Map<Class<? extends Event>, List<ReflectionEventFireHandler.ListenerMethod>> listenerMap = new HashMap<>();

        for (Object listener : listeners) {
            for (Method method : listener.getClass().getDeclaredMethods()) {
                if (method.isAnnotationPresent(Listener.class)) {
                    listenerMap.computeIfAbsent((Class<? extends Event>) method.getParameterTypes()[0], (k) -> new ArrayList<>())
                            .add(new ReflectionEventFireHandler.ListenerMethod(listener, method));
                }
            }
        }

        for (List<ReflectionEventFireHandler.ListenerMethod> methods : listenerMap.values()) {
            Collections.sort(methods);
        }

        Map<Class<? extends Event>, EventFireHandler> handlerMap = new HashMap<>();
        for (Map.Entry<Class<? extends Event>, List<ReflectionEventFireHandler.ListenerMethod>> entry : listenerMap.entrySet()) {
            handlerMap.put(entry.getKey(), new ReflectionEventFireHandler(entry.getValue()));
        }
        this.eventHandlers = Collections.unmodifiableMap(handlerMap);
    }
}
