package me.kisoft.easybus.rabbitmq.test;

import me.kisoft.easybus.rabbitmq.QueueName;
import me.kisoft.easybus.Listener;

@QueueName("named.queue")
public class RabbitMQNamedTestListener implements Listener<RabbitMQNamedTestEvent> {

    public void on(RabbitMQNamedTestEvent event) {
        RabbitMQNamedTestEvent.handled = true;
    }
}
