/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.kisoft.easybus;

import java.lang.reflect.Method;
import lombok.Getter;
import lombok.extern.java.Log;

/**
 *
 * @author tareq
 */
@Log
public class EventHandler {

    private static final String NO_ANNOTATION = "Handler of type %s Must have the @Handle annotation";
    private static final String NO_METHOD = "Handler of type %s Must have the public method 'handle' with parameter type %s";
    private static final String NO_CLASS = "Handler of type %s is missing the target event class";
    @Getter
    private final Object handler;
    @Getter
    private Method handlerMethod;
    @Getter
    private String eventClassName;
    @Getter
    private boolean async = false;

    EventHandler(Object handler) throws NoSuchMethodException,SecurityException  {// These should be impossible to throw, we use compile time checking
        this.handler = handler;
        findTargetClass(handler);
        findHandleMethod(handler);
        findIsAsync(handler);

    }

    private void findTargetClass(Object handler) {
        Class foundClass = handler.getClass().getAnnotation(Handle.class).event();
        this.eventClassName = foundClass.getCanonicalName();

    }

    private void findHandleMethod(Object handler) throws  NoSuchMethodException,SecurityException {// These should be impossible to throw, we use compile time checking
        Method foundMethod = handler.getClass().getMethod("handle", handler.getClass().getAnnotation(Handle.class).event());
        this.handlerMethod = foundMethod;

    }

    private void findIsAsync(Object handler) {
        this.async = handler.getClass().getAnnotation(Handle.class).async();
    }

    /**
     * handles an event by matching its type to a handler
     *
     * @param event the event to handle
     * @throws Throwable
     */
    final void handle(Object event) {
        try {
            this.handlerMethod.invoke(this.handler, event);
        } catch (Throwable ex) {
            log.severe(ex.getMessage());
        }

    }

}
