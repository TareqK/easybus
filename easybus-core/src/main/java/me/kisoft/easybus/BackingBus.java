package me.kisoft.easybus;

/**
 *
 * @author tareq
 */
public abstract class BackingBus implements AutoCloseable {

    /**
     * Posts an object to the backing bus.
     *
     * @param <T> The type of the object posted
     * @param object the Object to post. Must have the annotation @Event to work
     * correctly
     * @deprecated method was made public to allow nesting backing busses for
     * ease of code; should not be used externally.
     */
    @Deprecated
    public abstract <T extends Object> void post(T object);

    /**
     * Clears all handlers from the event bus
     */
    protected abstract void clear();

    /**
     * Adds a handler to the backing bus.
     *
     * @param eventClass the class of the event to handle
     * @param handler the handler to add
     */
    protected abstract void addHandler(Class eventClass, Handler handler);

    /**
     * Calls the handler with a specific event.
     *
     * @param <T> the event type
     * @param event the event data
     * @param handler the handler to use
     */
    protected <T extends Object> void handle(T event, Handler<T> handler) {
        handler.handle(event);
    }

    @Override
    public void close() throws Exception {

    }

}
