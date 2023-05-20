package me.kisoft.easybus.rabbitmq.test;

import me.kisoft.easybus.Listener;

/**
 *
 * @author tareq
 */
public class MultiHandlerTestListener implements Listener<RabbitMQTestEvent> {

    public void on(RabbitMQTestEvent event) {
        RabbitMQTestEvent.handled2 = true;
    }

}
