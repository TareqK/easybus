package me.kisoft.easybus.rabbitmq.test;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import me.kisoft.easybus.EasyBus;
import me.kisoft.easybus.rabbitmq.RabbitMQBackingBusImpl;
import org.junit.AfterClass;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;

/**
 *
 * @author tareq
 */
public class RabbitMQBusImplTest {

    private static EasyBus sendingBus;
    private static EasyBus receivingBus;
    private static RabbitMQContainer rabbitMqContainer;

    @BeforeClass
    public static void scanForEvents() throws Exception {
        rabbitMqContainer = new RabbitMQContainer(DockerImageName.parse("rabbitmq:3.11.0"))
                .withUser("guest", "guest");
        rabbitMqContainer.start();
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(rabbitMqContainer.getHost());
        factory.setPort(rabbitMqContainer.getAmqpPort());
        factory.setUsername("guest");
        factory.setPassword("guest");
        Connection connection1 = factory.newConnection();
        Connection connection2 = factory.newConnection();
        sendingBus = new EasyBus(new RabbitMQBackingBusImpl(connection1));
        receivingBus = new EasyBus(new RabbitMQBackingBusImpl(connection2));
        receivingBus.search("me.kisoft.easybus.rabbitmq.test");
    }

    @Test(timeout = 10000)
    public void handleEvent() throws InterruptedException {
        RabbitMQTestEvent.handled = false;
        sendingBus.post(new RabbitMQTestEvent());
        while (!RabbitMQTestEvent.handled) {
            Thread.sleep(100);
        }
        assertTrue(RabbitMQTestEvent.handled);
    }

    @Test(timeout = 10000)
    public void handleNamedEvent() throws InterruptedException {
        RabbitMQNamedTestEvent.handled = false;
        sendingBus.post(new RabbitMQNamedTestEvent());
        while (!RabbitMQNamedTestEvent.handled) {
            Thread.sleep(100);
        }
        assertTrue(RabbitMQNamedTestEvent.handled);
    }

    @Test(timeout = 10000)
    public void multiEventHandlerTest() throws InterruptedException {
        RabbitMQTestEvent.handled = false;
        RabbitMQTestEvent.handled2 = false;
        sendingBus.post(new RabbitMQTestEvent());
        while (!RabbitMQTestEvent.handled) {
            Thread.sleep(100);
        }
        while (!RabbitMQTestEvent.handled2) {
            Thread.sleep(100);
        }
        assertTrue(RabbitMQTestEvent.handled);
        assertTrue(RabbitMQTestEvent.handled2);
    }

    @AfterClass
    public static void cleanup() throws Exception {
        sendingBus.close();
        receivingBus.close();
    }
}
