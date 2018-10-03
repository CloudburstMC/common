package com.nukkitx.event.firehandler;

import com.nukkitx.api.event.Event;
import com.nukkitx.api.event.EventException;
import com.nukkitx.api.event.EventFireHandler;
import com.nukkitx.api.event.Listener;
import lombok.extern.log4j.Log4j2;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Log4j2
public class ReflectionEventFireHandler implements EventFireHandler {
    private static final long LONG_RUNNING_EVENT_TIME = TimeUnit.MILLISECONDS.toNanos(5);
    private final List<ListenerMethod> methods = new ArrayList<>();

    public ReflectionEventFireHandler(Collection<ListenerMethod> methods) {
        this.methods.addAll(methods);
    }

    @Override
    public void fire(Event event) {
        long start = System.nanoTime();
        for (ListenerMethod method : methods) {
            try {
                method.run(event);
            } catch (InvocationTargetException | IllegalAccessException e) {
                throw new EventException("Exception occurred while executing method " + method + " for " + event, e);
            }
        }
        long differenceTaken = System.nanoTime() - start;
        if (differenceTaken >= LONG_RUNNING_EVENT_TIME) {
            log.warn("Event " + event + " took " +
                    BigDecimal.valueOf(differenceTaken)
                            .divide(new BigDecimal("1000000"), RoundingMode.HALF_UP)
                            .setScale(2, RoundingMode.HALF_UP) +
                    "ms to fire", null, true, false);
        }
    }

    @Override
    public List<EventFireHandler.ListenerMethod> getMethods() {
        return Collections.unmodifiableList(new ArrayList<>(methods));
    }

    public static class ListenerMethod implements EventFireHandler.ListenerMethod {
        private final Object listener;
        private final Method method;

        public ListenerMethod(Object listener, Method method) {
            this.listener = listener;
            this.method = method;
        }

        public void run(Event event) throws InvocationTargetException, IllegalAccessException {
            method.invoke(listener, event);
        }

        @Override
        public String toString() {
            return listener.getClass().getName() + "#" + method.getName();
        }

        @Override
        public Object getListener() {
            return listener;
        }

        @Override
        public Method getMethod() {
            return method;
        }

        @Override
        public int compareTo(@Nonnull EventFireHandler.ListenerMethod o) {
            Listener thisListener = getMethod().getAnnotation(Listener.class);
            if (listener == null) {
                return -1;
            }

            Listener thatListener = o.getMethod().getAnnotation(Listener.class);
            if (thatListener == null) {
                return 1;
            }

            return Integer.compare(thisListener.priority().ordinal(), thatListener.priority().ordinal());
        }
    }
}
