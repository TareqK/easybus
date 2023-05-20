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
    protected abstract void addHandler(Class eventClass, Listener listener);

    /**
     * Calls the listener with a specific event.
     *
     * @param <T> the event type
     * @param event the event data
     * @param listener the listener to use
     */
    protected <T extends Object> void handle(T event, Listener<T> listener) {
        listener.on(event);
    }

    @Override
    public void close() throws Exception {

    }

}
