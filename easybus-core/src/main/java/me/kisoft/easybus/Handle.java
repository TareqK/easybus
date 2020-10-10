/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.kisoft.easybus;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation that specifies that this class is an event handler, and what event class it handles
 * @author tareq
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Handle {

    /**
     * The class of the event that this handler handles
     * @return the class of the event that this handler handles
     */
    public Class<?> event();
    /**
     * Whether or not this handler is async. This may or may not be honored by the backing bus
     * @return whether or not this handler is async
     */
    public boolean async() default false;
}
