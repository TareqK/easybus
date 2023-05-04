package me.kisoft.easybus;

public interface Handler<T> {

    public void handle(T event);
}
