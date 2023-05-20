package me.kisoft.easybus.memory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import me.kisoft.easybus.BackingBus;
import me.kisoft.easybus.Listener;

/**
 *
 * @author tareq
 */
public class MemoryBackingBusImpl extends BackingBus {

    private final Map<Class, Set<Listener<Object>>> handlerMap = new HashMap<>();
    private final ExecutorService pool;

    public MemoryBackingBusImpl() {
        pool = Executors.newCachedThreadPool();
    }

    @Override
    public void clear() {
        handlerMap.clear();
    }

    @Override
    public void addHandler(Class eventClass, Listener listener) {
        if (!handlerMap.containsKey(eventClass)) {
            handlerMap.put(eventClass, new HashSet<>());
        }

        /*
        Paranoid code that handles an unreal scenario where java type erasure
        doesnt exist or work, making sure that a listener is only ever added
        once in the listener set of an event.
         */
        Listener handlerToAdd = handlerMap.get(eventClass)
                .stream()
                .filter(existingHandler -> existingHandler.getClass().equals(listener.getClass()))
                .findAny()
                .orElse(listener);

        handlerMap.get(eventClass).add(handlerToAdd);

    }

    /**
     * A Method that determines if the event should be handled using the thread
     * pool or in the current thread and handles it accordingly.
     *
     * @param <T> the event type
     * @param listener the event listener
     * @param event the event
     */
    private <T extends Object> void doHandle(Listener<T> listener, T event) {
        if (listener.getClass().isAnnotationPresent(ProcessAsync.class)) {
            pool.submit(() -> this.handle(event, listener));
        } else {
            this.handle(event, listener);
        }
    }

    @Override
    public <T> void post(T event) {
        Set<Listener<Object>> eventHandlers = handlerMap.entrySet()
                .stream()
                .filter(entry -> entry.getKey().isAssignableFrom(event.getClass()))
                .flatMap(entry -> entry.getValue().stream())
                .collect(Collectors.toSet());

        /*
        Code in case the listener set is ever empty.
         */
        Optional.of(eventHandlers)
                .orElse(new HashSet<>())
                .stream()
                .forEach(listener -> doHandle(listener, event));
    }

    @Override
    public void close() throws Exception {
        pool.shutdown();
    }

}
