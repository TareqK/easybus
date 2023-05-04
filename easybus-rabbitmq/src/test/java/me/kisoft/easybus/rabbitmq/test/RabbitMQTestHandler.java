package me.kisoft.easybus.rabbitmq.test;

import me.kisoft.easybus.Handler;

public class RabbitMQTestHandler implements Handler<RabbitMQTestEvent> {

    public void handle(RabbitMQTestEvent event) {
        RabbitMQTestEvent.handled = true;
    }
}
