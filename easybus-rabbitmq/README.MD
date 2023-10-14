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
    //add your listeners
```

Events can also be attached to specific exchanges, by using the ``@ExchangeName`` annotation

```java
    @ExchangeName("exchange.name")
    public class NamedEvent{
        ....
    }
```

If no name is given, then the name of the event class itself will be used as the exchange name.


Event Listeners can also be attached to specific queues, by using the ``@QueueName`` annotation

```java
    @QueueName("queue.name")
    public class MyEventListener implements Listener<MyEvent>{
        ....
    }
```

If no name is given, then the name of the listener class itself will be used as the queue name.


Event Listeners can also be given specific routing keys to listen for, especially for ``Topic`` queues
declared from other systems(as of 4.1.0) using one or more ``@RoutingKey`` annotations on a ``Listener`` class.

```java
    @RoutingKey("*.tacos.*")
    @RoutingKey("#.cuisine")
    public class MyEventListener implements Listener<MyEvent>{
        ....
    }
```

If no given ``RoutingKey`` is given, then it will listen on the ALL routing key (``#``) by default.



## Caveats

1. ~~All messages go on the main exchange~~ As of 2.2.0, you can use custom exchanges
2. ~~Each Event has its own channel open, which might not be optimal in a larger application~~ This is generally best practice it seems
3. Object De/Serialization is done using Jackson, which might not be ideal, and right now,
there is no way to customize the ```ObjectMapper``` class.
4. You need Getters/Setters for everything you want to de/serialize
5. You cant rely on object references - this makes callbacks a little more difficult.
6. All Queues are destroyable(no persistent queues)
