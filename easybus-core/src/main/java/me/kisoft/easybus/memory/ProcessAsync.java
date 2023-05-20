package me.kisoft.easybus.memory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation to tell the memory bus to handle the event using a thread pool.
 * If there is any sort of exception, it will be ignored completely. use
 * cautiously
 *
 * @author tareq
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ProcessAsync {

}
