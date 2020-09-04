/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.kisoft.easybus;

import java.lang.reflect.Method;
import java.util.Objects;
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
    @Getter
    private Class eventClass;

    EventHandler(Object handler) throws NoSuchMethodException, SecurityException {// These should be impossible to throw, we use compile time checking
        this.handler = handler;
        findTargetClass(handler);
        findHandleMethod(handler);
        findIsAsync(handler);

    }

    private void findTargetClass(Object handler) {
        Class foundClass = handler.getClass().getAnnotation(Handle.class).event();
        this.eventClass = foundClass;
        this.eventClassName = foundClass.getCanonicalName();

    }

    private void findHandleMethod(Object handler) throws NoSuchMethodException, SecurityException {// These should be impossible to throw, we use compile time checking
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
     * @throws RuntimeException if anything should happen
     */
    public final void handle(Object event) throws RuntimeException {
        try {
            this.handlerMethod.invoke(this.handler, event);
        } catch (Throwable ex) {
            log.severe(ex.getMessage());
            throw new RuntimeException(ex);
        }

    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 31 * hash + Objects.hashCode(this.handler);
        hash = 31 * hash + Objects.hashCode(this.handlerMethod);
        hash = 31 * hash + (this.async ? 1 : 0);
        hash = 31 * hash + Objects.hashCode(this.eventClass);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final EventHandler other = (EventHandler) obj;
        if (this.async != other.async) {
            return false;
        }
        if (!Objects.equals(this.handler, other.handler)) {
            return false;
        }
        if (!Objects.equals(this.handlerMethod, other.handlerMethod)) {
            return false;
        }
        if (!Objects.equals(this.eventClass, other.eventClass)) {
            return false;
        }
        return true;
    }
    
    

}
