package me.kisoft.easybus.rabbitmq.test;

import me.kisoft.easybus.Handler;
import me.kisoft.easybus.rabbitmq.QueueName;

@QueueName("named.queue")
public class RabbitMQNamedTestHandler implements Handler<RabbitMQNamedTestEvent> {

    public void handle(RabbitMQNamedTestEvent event) {
        RabbitMQNamedTestEvent.handled = true;
    }
}
