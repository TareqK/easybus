/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.kisoft.easybus;

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
        return this.handler.getClass().getAnnotation(Handles.class).value().getCanonicalName();
    }

    private final Object handler;

    EventHandler(Object handler) {
        this.handler = handler;
    }

    /**
     * Handles the event if it matches the name of the event
     *
     * @param event the event to handle
     * @throws Exception
     */
    private void doHandle(Object event) throws Exception {
        this.handler.getClass().getMethod("handle", event.getClass()).invoke(this.handler, event);
    }

    /**
     * handles an event by matching its name
     *
     * @param event the event to handle
     * @throws Throwable
     */
    public final void handleEvent(Object event) {
        if (event != null) {
            log.log(Level.INFO, "Event Thrown : {0}", event.getClass().getCanonicalName());
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
