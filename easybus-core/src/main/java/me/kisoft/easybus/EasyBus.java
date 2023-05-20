package me.kisoft.easybus;

import me.kisoft.easybus.memory.MemoryBackingBusImpl;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import static me.kisoft.easybus.Activator.DEFAULT_ACTIVATOR;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tareq
 */
public class EasyBus {

    private final Logger log = LoggerFactory.getLogger(EasyBus.class);
    private final Activator activator;
    private final BackingBus backingBus;

    /**
     * Creates a new EventBus
     */
    public EasyBus() {
        backingBus = new MemoryBackingBusImpl();
        activator = DEFAULT_ACTIVATOR;
    }

    public EasyBus(BackingBus backingBus) {
        this.backingBus = backingBus;
        activator = DEFAULT_ACTIVATOR;
    }

    public EasyBus(Activator activator) {
        backingBus = new MemoryBackingBusImpl();
        this.activator = activator;
    }

    public EasyBus(BackingBus backingBus, Activator activator) {
        this.backingBus = backingBus;
        this.activator = activator;
    }

    /**
     * Removes all handlers from the event bus
     */
    public void clear() {
        backingBus.clear();
    }

    /**
     * Posts an event to the event bus
     *
     * @param event the event to post
     */
    public void post(Object event) {
        if (event != null) {
            log.debug(String.format("Event Thrown : %s", event.getClass().getCanonicalName()));
            backingBus.post(event);
        }
    }

    /**
     * Adds a listener class through activation. Default activator is a no-args
     * constructor
     *
     * @param handlerClass the listener class to activate
     * @return the current easybus instance
     */
    public EasyBus register(Class<? extends Listener> handlerClass) {
        try {
            return this.register(this.activator.activate(handlerClass));
        } catch (Exception ex) {
            throw new ActivationFailureException(ex);
        }
    }

    /**
     * Adds a new listener for all the specified event types it handlers.
     * Operation is verified and type checked, and will only work on classes
     * that implement Handler
     *
     * @param listener a listener object
     * @return the current easybus instance
     */
    public EasyBus register(Listener listener) {
        Type[] genericInterfaces = listener.getClass().getGenericInterfaces();
        for (Type type : genericInterfaces) {
            if (!(type instanceof ParameterizedType)) {
                continue;
            }
            ParameterizedType parameterizedType = (ParameterizedType) type;

            /*
            This code may look wrong, but because of type erasure, 
            a listener can only handle a single event, so no need to look 
            more, as the compiler will not compile multi inheritence of the
            same generic
             */
            if ((parameterizedType.getRawType() == Listener.class)) {
                Type eventType = parameterizedType.getActualTypeArguments()[0];

                /*
                This is impossible to happen, but this is to make sure the compiler
                doesnt raise flags
                 */
                if (!(eventType instanceof Class)) {
                    continue;
                }
                Class eventClass = (Class) eventType;

                backingBus.addHandler(eventClass, listener);
                break;
            }
        }
        return this;
    }

    public void close() throws Exception {
        backingBus.close();
    }

    public class ActivationFailureException extends RuntimeException {

        ActivationFailureException(Exception ex) {
            super(ex);
        }
    }

}
