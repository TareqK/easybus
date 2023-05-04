package me.kisoft.easybus.rabbitmq.test;

import lombok.Data;

@Data
public class RabbitMQTestEvent {

    public static boolean handled = false;
    public static boolean handled2 = false;
}
