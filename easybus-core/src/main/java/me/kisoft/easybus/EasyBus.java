/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.kisoft.easybus;

import java.lang.reflect.InvocationTargetException;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tareq
 */
public class EasyBus {
    
    private final Logger log = LoggerFactory.getLogger(EasyBus.class);
    private static final String NO_EVENT_CLASS_ERROR = "Error in Class : %s : No Event Class Specified.";
    private static final String EVENT_CLASS_NOT_ANNOTATED = "Error in Class : %s : Event Class : %s :  Not annotated with @Event";
    private static final String NO_METHOD_DEFINED_ERROR = "Error in Class : %s : 'handle' method for Specified Event type : %s : not defined";
    
    private final Bus bus;

    /**
     * Creates a new EventBus
     */
    public EasyBus() {
        bus = new MemoryBusImpl();
    }
    
    public EasyBus(Bus bus) {
        this.bus = bus;
    }

    /**
     * Resets the event bus
     */
    public void removeHandlers() {
        bus.clear();
    }

    /**
     * Posts an event to the event bus
     *
     * @param event the event to post
     */
    public void post(Object event) {
        if (event != null) {
            log.debug(String.format("Event Thrown : %s", event.getClass().getCanonicalName()));
            bus.post(event);
        }
    }

    /**
     * Search a package name or reflections criteria for events and handlers
     *
     * @param name the name of the package or the criteria
     * @return the current event bus
     */
    public final EasyBus search(String name) {
        return search(new Reflections(name));
    }

    /**
     * Search the class instances for events and handlers
     *
     * @param clazz the class to search
     * @return the current event bus
     */
    public final EasyBus search(Class clazz) {
        return search(new Reflections(clazz));
    }

    /**
     * Search the classloader for events and handlers
     *
     * @param loader the classloader to search
     * @return the current Event Bus
     */
    public final EasyBus search(ClassLoader loader) {
        return search(new Reflections(loader));
    }

    /**
     * Searches for handlers in reflections
     *
     * @param r the reflections to search for handlers in
     * @return the current eventbus
     */
    public final EasyBus search(Reflections r) {
        for (Class clazz : r.getTypesAnnotatedWith(Handle.class)) {
            
            try {
                Object o = clazz.getConstructor().newInstance();
                if (o.getClass().getAnnotation(Handle.class).event() == null) {
                    throw new IllegalArgumentException(String.format(NO_EVENT_CLASS_ERROR, o.getClass().getCanonicalName()));
                } else if (o.getClass().getAnnotation(Handle.class).event().getAnnotation(Event.class) == null) {
                    throw new IllegalArgumentException(String.format(EVENT_CLASS_NOT_ANNOTATED, o.getClass().getCanonicalName(), o.getClass().getAnnotation(Handle.class).event().getCanonicalName()));
                } else {
                    try {
                        o.getClass().getMethod("handle", o.getClass().getAnnotation(Handle.class).event());
                    } catch (NoSuchMethodException | SecurityException ex) {
                        throw new IllegalArgumentException(String.format(NO_METHOD_DEFINED_ERROR, o.getClass().getCanonicalName(), o.getClass().getAnnotation(Handle.class).event().getCanonicalName()));
                    }
                }
                this.addHandler(new EventHandler(o));
                log.info(String.format("Added Event Handler %s", clazz.getSimpleName()));
            } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                log.error(ex.getMessage());
                throw new RuntimeException(ex);
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
        bus.addHandler(handler);
    }

    /**
     * Remove a handler from the event bus
     *
     * @param handler the handler to remove
     */
    public void removeHandler(EventHandler handler) {
        bus.removeHandler(handler);
    }
    
    public void close() throws Exception {
        bus.close();
    }
    
}
