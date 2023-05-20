package me.kisoft.easybus;

@FunctionalInterface
public interface Activator {

    Listener activate(Class<? extends Listener> clazz) throws Exception;

    public static Activator DEFAULT_ACTIVATOR = (clazz) -> clazz.getConstructor().newInstance();

}
