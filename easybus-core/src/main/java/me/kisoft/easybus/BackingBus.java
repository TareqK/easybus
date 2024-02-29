package me.kisoft.easybus;

import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tareq
 */
public abstract class BackingBus implements AutoCloseable {

    protected static final Logger log = LoggerFactory.getLogger(EasyBus.class);

    /**
     * Posts an object to the backing bus.
     *
     * @param <T> The type of the object posted
     * @param object the Object to post. Must have the annotation @Event to work
     * correctly
     */
    protected abstract <T extends Object> void post(T object);

    /**
     * Clears all handlers from the event bus
     */
    protected abstract void clear();

    /**
     * Adds a listener to the backing bus.
     *
     * @param eventClass the class of the event to handle
     * @param listener the listener to add
     */
    protected abstract void addListener(Class eventClass, Listener listener);

    /**
     * Calls the listener with a specific event.
     *
     * @param <T> the event type
     * @param event the event data
     * @param listener the listener to use
     */
    protected final <T extends Object> void handle(T event, Listener<T> listener) {
        if (Objects.isNull(event)) {
            log.warn("Received Null Event - skipping");
            throw new IllegalArgumentException("Event Cannot be null");
        }
        if (Objects.isNull(listener)) {
            log.warn("Received Null Listener - skipping ");
            throw new IllegalArgumentException("Listener Cannot be null");
        }
        try {
            listener.on(event);
        } catch (Exception ex) {
            log.info("Failed to Process Event {} : {} : {} caused by : {}", event.getClass().getCanonicalName(), ex.getClass().getCanonicalName(), ex.getMessage(), Optional.ofNullable(ex.getCause()).map(cause -> cause.getClass().getCanonicalName()).orElse("Unknown"));
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void close() throws Exception {

    }

}
