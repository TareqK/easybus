/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.kisoft.easybus;

import java.lang.reflect.Method;
import java.util.logging.Level;
import lombok.extern.java.Log;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author tareq
 */
@Log
public class EventHandler {

    /**
     * Gets the name of the event this handler is responsible for
     *
     * @return a string with the event name
     */
    private String getEventClassName() {
        if (eventClassName == null) {
            eventClassName = this.handler.getClass().getAnnotation(Handles.class).value().getCanonicalName();
        }
        return eventClassName;
    }

    private final Object handler;
    private Method handlerMethod;
    private String eventClassName;

    EventHandler(Object handler) {
        this.handler = handler;
    }

    /**
     * Handles the event if it matches the type of the event
     *
     * @param event the event to handle
     * @throws Exception
     */
    private void doHandle(Object event) throws Exception {
        if (handlerMethod == null) {
            this.handlerMethod = this.handler.getClass().getMethod("handle", event.getClass());
        }
        this.handlerMethod.invoke(this.handler, event);
    }

    /**
     * handles an event by matching its type to a handler
     *
     * @param event the event to handle
     * @throws Throwable
     */
    public final void handleEvent(Object event) {
        if (event != null) {
            log.log(Level.FINE, "Event Thrown : {0}", event.getClass().getCanonicalName());
            try {
                if (StringUtils.equals(this.getEventClassName(), event.getClass().getCanonicalName())) {
                    doHandle(event);
                }
            } catch (Exception ex) {
                log.severe(ex.getMessage());
            }
        }
    }

}
