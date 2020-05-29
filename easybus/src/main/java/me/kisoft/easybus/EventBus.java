/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.kisoft.easybus;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import lombok.extern.java.Log;
import org.reflections.Reflections;

/**
 *
 * @author tareq
 */
@Log
public class EventBus {

    private final List<EventHandler> handlers = new ArrayList<>();

    public EventBus() {
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
        handlers.stream().forEach(handler -> {
            handler.handleEvent(event);
        });
    }

    public final EventBus search(String name) {
        return search(new Reflections(name));
    }

    public final EventBus search(Class clazz) {
        return search(new Reflections(clazz));
    }

    public final EventBus search(ClassLoader loader) {
        return search(new Reflections(loader));
    }

    public final EventBus search(Reflections r) {
        for (Class clazz : r.getTypesAnnotatedWith(Handles.class)) {
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

    public void removeHandler(EventHandler handler) {
        handlers.remove(handler);
    }
}
