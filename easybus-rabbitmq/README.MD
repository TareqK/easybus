# EasyBus RabbitMQ

This is the RabbitMQ connector for easybus. In order to use it, you need to provide a 
RabbitMQ connection.

## Usage

```java
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
    factory.setPort(5672);
    factory.setUsername("guest");
    factory.setPassword("guest");
    Connection connection = factory.newConnection();
    bus = new EasyBus(new RabbitMQBusImpl(connection));
    bus.search("me.kisoft.easybus.rabbitmq.test");
```

Events can also be attached to specific queues, by using the ```@QueueName``` annotation

```java
    @Event
    @QueueName("named_event")
    public class NamedEvent{
        ....
    }
```

If no name is given, then the name of the event class itself will be used as the queue name.

## Caveats

1. All messages go on the main exchange
2. Each Event has its own channel open, which might not be optimal in a larger application
3. Object De/Serialization is done using Jackson, which might not be ideal, and right now,
there is no way to customize the ```ObjectMapper``` class.
4. You need Getters/Setters for everything you want to de/serialize
5. You cant rely on object references - this makes callbacks a little more difficult.
6. All Queues are destroyable(no persistent queues)