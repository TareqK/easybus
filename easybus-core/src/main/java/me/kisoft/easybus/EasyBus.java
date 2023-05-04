package me.kisoft.easybus;

import me.kisoft.easybus.memory.MemoryBackingBusImpl;
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
    private static final String REFLECTION_ERROR = "Reflection Error in Class : %s : %s";
    private final BackingBus backingBus;

    /**
     * Creates a new EventBus
     */
    public EasyBus() {
        backingBus = new MemoryBackingBusImpl();
    }

    public EasyBus(BackingBus backingBus) {
        this.backingBus = backingBus;
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

    /**
     * Adds a new handler for all the specified event types it handlers.
     * Operation is verified and type checked, and will only work on classes
     * that implement Handler
     *
     * @param handler a handler object
     * @return the current easybus instance
     */
    public EasyBus addHandler(Handler handler) {
        Type[] genericInterfaces = handler.getClass().getGenericInterfaces();
        for (Type type : genericInterfaces) {
            if (!(type instanceof ParameterizedType)) {
                continue;
            }
            ParameterizedType parameterizedType = (ParameterizedType) type;

            /*
            This code may look wrong, but because of type erasure, 
            a handler can only handle a single event, so no need to look 
            more, as the compiler will not compile multi inheritence of the
            same generic
             */
            if ((parameterizedType.getRawType() == Handler.class)) {
                Type eventType = parameterizedType.getActualTypeArguments()[0];

                /*
                This is impossible to happen, but this is to make sure the compiler
                doesnt raise flags
                 */
                if (!(eventType instanceof Class)) {
                    continue;
                }
                Class eventClass = (Class) eventType;

                backingBus.addHandler(eventClass, handler);
                break;
            }
        }
        return this;
    }

    public void close() throws Exception {
        backingBus.close();
    }

}
