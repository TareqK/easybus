package me.kisoft.easybus.rabbitmq.test;

import lombok.Data;
import me.kisoft.easybus.rabbitmq.ExchangeName;

/**
 *
 * @author tareq
 */
@ExchangeName("named.exchange")
@Data
public class RabbitMQNamedTestEvent {

    public static boolean handled = false;
}
