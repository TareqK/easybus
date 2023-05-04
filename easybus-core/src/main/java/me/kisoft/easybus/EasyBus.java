/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.kisoft.easybus;

import me.kisoft.easybus.memory.MemoryBusImpl;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
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
    private static final String REFLECTION_ERROR = "Reflection Error in Class : %s : %s";
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
     * Removes all handlers from the event bus
     */
    public void clear() {
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
        for (Class<? extends Handler> clazz : r.getSubTypesOf(Handler.class)) {
            try {
                Handler o = clazz.getConstructor().newInstance();
                this.addHandler(o);
            } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                throw new IllegalArgumentException(String.format(REFLECTION_ERROR, clazz.getCanonicalName(), ex.getMessage()));
            }
        }
        return this;
    }

    public EasyBus addHandler(Handler handler) {
        Type[] genericInterfaces = handler.getClass().getGenericInterfaces();
        for (Type type : genericInterfaces) {
            if (!(type instanceof ParameterizedType)) {
                continue;
            }
            ParameterizedType parameterizedType = (ParameterizedType) type;

            if (!(parameterizedType.getRawType() == Handler.class)) {
                continue;
            }
            

            Type eventType = parameterizedType.getActualTypeArguments()[0];
            if (!(eventType instanceof Class)) {
                continue;
            }

            Class eventClass = (Class) eventType;
            bus.addHandler(eventClass, handler);

        }
        return this;
    }

    public void close() throws Exception {
        bus.close();
    }

}
