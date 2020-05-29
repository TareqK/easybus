/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.kisoft.easybus;

import java.lang.reflect.Method;
import lombok.extern.java.Log;

/**
 *
 * @author tareq
 */
@Log
public class EventHandler {

    private final Object handler;
    private Method handlerMethod;
    private String eventClassName;

    /**
     * Gets the name of the event this handler is responsible for
     *
     * @return a string with the event name
     */
    String getEventClassName() {
        if (eventClassName == null) {
            eventClassName = this.handler.getClass().getAnnotation(Handle.class).event().getCanonicalName();
        }
        return eventClassName;
    }

    EventHandler(Object handler) {
        if (handler.getClass().getAnnotation(Handle.class) == null) {
            throw new IllegalArgumentException("Handlers Must have the @Handle annotation");
        }
        this.handler = handler;
    }

    /**
     * handles an event by matching its type to a handler
     *
     * @param event the event to handle
     * @throws Throwable
     */
    final void handle(Object event) {
        try {
            if (handlerMethod == null) {
                this.handlerMethod = this.handler.getClass().getMethod("handle", event.getClass());
            }
            this.handlerMethod.invoke(this.handler, event);
        } catch (Throwable ex) {
            log.severe(ex.getMessage());
        }

    }

    /**
     * Is this event supposed to be handled in a synchronous or asynchronous
     * way?
     *
     * @return if the event should be handled async or sync
     */
    boolean isAsync() {
        return this.handler.getClass().getAnnotation(Handle.class).async();
    }

}
