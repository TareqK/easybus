package me.kisoft.easybus.memory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import me.kisoft.easybus.BackingBus;
import me.kisoft.easybus.Handler;

/**
 *
 * @author tareq
 */
public class MemoryBackingBusImpl extends BackingBus {

    private final Map<Class, Set<Handler<Object>>> handlerMap = new HashMap<>();
    private final ExecutorService pool;

    public MemoryBackingBusImpl() {
        pool = Executors.newCachedThreadPool();
    }

    @Override
    public void clear() {
        handlerMap.clear();
    }

    public Set<Handler> getHandlers() {
        return handlerMap.values().stream().flatMap(list -> list.stream()).distinct().collect(Collectors.toSet());
    }

    @Override
    public void addHandler(Class eventClass, Handler handler) {
        if (!handlerMap.containsKey(eventClass)) {
            handlerMap.put(eventClass, new HashSet<>());
        }
        if (handlerMap.get(eventClass).contains(handler)) {
            return;
        }
        handlerMap.get(eventClass).add(handler);
    }

    /**
     * A Method that determines if the event should be handled using the thread
     * pool or in the current thread and handles it accordingly.
     *
     * @param <T> the event type
     * @param handler the event handler
     * @param event the event
     */
    private <T extends Object> void doHandle(Handler<T> handler, T event) {
        if (handler.getClass().isAnnotationPresent(HandleAsync.class)) {
            pool.submit(() -> this.handle(event, handler));
        } else {
            this.handle(event, handler);
        }
    }

    @Override
    public <T> void post(T event) {
        Set<Handler<Object>> eventHandlers = handlerMap.entrySet()
                .stream()
                .filter(entry -> entry.getKey().isAssignableFrom(event.getClass()))
                .flatMap(entry -> entry.getValue().stream())
                .collect(Collectors.toSet());
        if (eventHandlers == null || eventHandlers.isEmpty()) {
            return;
        }
        eventHandlers.stream().forEach(handler -> doHandle(handler, event));
    }

    @Override
    public void close() throws Exception {
        pool.shutdown();
    }

}
