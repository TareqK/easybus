/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.kisoft.easybus;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.stream.Collectors;
import lombok.extern.java.Log;
import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;

/**
 *
 * @author tareq
 */
@Log
public class EasyBus {

    private final List<EventHandler> handlers = new ArrayList<>();
    private final ExecutorService pool;

    /**
     * Creates a new EventBus
     */
    public EasyBus() {
        pool = Executors.newFixedThreadPool(5);
    }

    /**
     * Creates a new EventBus with the specified number of background executors for async processing
     * @param numberOfExecutors the number of threads to allocate for async processing
     */
    public EasyBus(int numberOfExecutors) {
        pool = Executors.newFixedThreadPool(numberOfExecutors);
    }

    /**
     * Resets the event bus
     */
    public void removeHandlers() {
        handlers.clear();
    }

    /**
     * Posts an event to the domain bus
     *
     * @param event the event to post
     */
    public void post(Object event) {
        if (event != null) {
            log.log(Level.FINE, "Event Thrown : {0}", event.getClass().getCanonicalName());
            handlers.parallelStream()
                    .filter(handler -> StringUtils.equals(handler.getEventClassName(), event.getClass().getCanonicalName()))
                    .collect(Collectors.toList())
                    .stream()
                    .forEach(handler -> doHandle(handler, event));
        }
    }

    private void doHandle(EventHandler handler, Object event) {
        if (handler.isAsync()) {
            pool.submit(() -> handler.handle(event));
        } else {
            handler.handle(event);
        }
    }

    /**
     * Search a package name or reflections criteria for events and handlers
     * @param name the name of the package or the criteria
     * @return the current event bus
     */
    public final EasyBus search(String name) {
        return search(new Reflections(name));
    }

    /**
     * Search the class instances for events and handlers
     * @param clazz the class to search
     * @return the current event bus
     */
    public final EasyBus search(Class clazz) {
        return search(new Reflections(clazz));
    }

    /**
     * Search the classloader for events and handlers
     * @param loader the classloader to search
     * @return  the current Event Bus
     */
    public final EasyBus search(ClassLoader loader) {
        return search(new Reflections(loader));
    }

    public final EasyBus search(Reflections r) {
        for (Class clazz : r.getTypesAnnotatedWith(Handle.class)) {
            try {
                Object o = clazz.getConstructor().newInstance();
                this.addHandler(new EventHandler(o));
                log.log(Level.INFO, "Added Event Handler {0}", clazz.getSimpleName());
            } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                log.log(Level.SEVERE, null, ex);
            }
        }
        return this;
    }

    /**
     * Add a handler to the event bus
     *
     * @param handler the handler to add
     */
    public void addHandler(EventHandler handler) {
        handlers.add(handler);
    }

    
    /**
     * Remove a handler from the event bus
     * @param handler  the handler to remove
     */
    public void removeHandler(EventHandler handler) {
        handlers.remove(handler);
    }
    
    public List<EventHandler> getHandlers(){
        return handlers;
    }
    

}
