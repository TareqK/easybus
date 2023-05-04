package me.kisoft.easybus.rabbitmq.test;

import me.kisoft.easybus.Handler;

/**
 *
 * @author tareq
 */
public class MultiHandlerTestHandler implements Handler<RabbitMQTestEvent> {

    public void handle(RabbitMQTestEvent event) {
        RabbitMQTestEvent.handled2 = true;
    }

}
