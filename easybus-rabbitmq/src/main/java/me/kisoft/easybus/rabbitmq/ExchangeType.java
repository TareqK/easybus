package me.kisoft.easybus.rabbitmq;

import com.rabbitmq.client.BuiltinExchangeType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation to specify the rabbitMQ queue name.
 *
 * @author tareq
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ExchangeType {

    public BuiltinExchangeType value();

}
