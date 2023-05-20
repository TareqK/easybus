package me.kisoft.easybus;

/**
 * An interface for event listener definition.
 *
 * @author tareq
 * @param <T> the type of the event class to handle
 */
@FunctionalInterface
public interface Listener<T> {

    public void on(T event);
}
