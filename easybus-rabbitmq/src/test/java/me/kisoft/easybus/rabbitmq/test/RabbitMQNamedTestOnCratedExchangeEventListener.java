package me.kisoft.easybus.rabbitmq.test;

import me.kisoft.easybus.Listener;
import me.kisoft.easybus.rabbitmq.RoutingKey;

@RoutingKey("all")
@RoutingKey("heheh")
public class RabbitMQNamedTestOnCratedExchangeEventListener implements Listener<RabbitMQNamedTestOnCreatedExchangeEvent> {

    @Override
    public void on(RabbitMQNamedTestOnCreatedExchangeEvent event) {
        RabbitMQNamedTestOnCreatedExchangeEvent.handled = true;
    }
}
