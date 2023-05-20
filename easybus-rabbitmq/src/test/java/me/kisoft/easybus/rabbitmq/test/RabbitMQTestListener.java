package me.kisoft.easybus.rabbitmq.test;

import me.kisoft.easybus.Listener;

public class RabbitMQTestListener implements Listener<RabbitMQTestEvent> {

    public void on(RabbitMQTestEvent event) {
        RabbitMQTestEvent.handled = true;
    }
}
