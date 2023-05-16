package me.kisoft.easybus;

/**
 * An interface for event handler definition.
 *
 * @author tareq
 * @param <T> the type of the event class to handle
 */
@FunctionalInterface
public interface Handler<T> {

    public void handle(T event);
}
