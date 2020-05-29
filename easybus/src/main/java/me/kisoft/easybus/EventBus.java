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

    private static EventBus instance = getInstance();
    private final List<EventHandler> handlers = new ArrayList<>();
    
    private EventBus() {
    }

    public static final EventBus getInstance() {
        if (instance == null) {
            instance = new EventBus();
        }
        return instance;
    }

    /**
     * Resets the event bus
     */
    public void removeHandlers() {
        handlers.clear();
    }

    /**
     * Posts an event to the domain bus
     * @param event  the event to post
     */
    public void post(Object event) {
        handlers.stream().forEach(handler ->{
            handler.handleEvent(event);
        });
    }

    
    public final void subscribeHandlers(Reflections r) {
        System.out.println(r.getTypesAnnotatedWith(Handles.class));
        for (Class clazz : r.getTypesAnnotatedWith(Handles.class)) {
            try {
                Object o = clazz.getConstructor().newInstance();
                EventBus.getInstance().subscribe(new EventHandler(o));
                log.log(Level.INFO, "Added Event Handler {0}", clazz.getSimpleName());
            } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                log.log(Level.SEVERE, null, ex);
            }
        }
    }
    /**
     * Add a handler to the event bus
     * @param handler  the handler to add
     */
    public void subscribe(EventHandler handler) {
       handlers.add(handler);
    }
    
    public void removeHandler(EventHandler handler){
        handlers.remove(handler);
    }
}
