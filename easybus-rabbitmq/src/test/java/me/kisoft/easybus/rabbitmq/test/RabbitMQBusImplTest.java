package me.kisoft.easybus.rabbitmq.test;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import me.kisoft.easybus.EasyBus;
import me.kisoft.easybus.rabbitmq.RabbitMQBackingBusImpl;
import org.junit.After;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
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
    private static ConnectionFactory factory;

    @BeforeClass
    public static void setupContainer() throws Exception {
        rabbitMqContainer = new RabbitMQContainer(DockerImageName.parse("rabbitmq:3.11.0"))
                .withUser("guest", "guest");
        rabbitMqContainer.start();

    }

    @Before
    public void setup() throws IOException, TimeoutException {
        factory = new ConnectionFactory();
        factory.setHost(rabbitMqContainer.getHost());
        factory.setPort(rabbitMqContainer.getAmqpPort());
        factory.setUsername("guest");
        factory.setPassword("guest");
        Connection connection1 = factory.newConnection();
        Connection connection2 = factory.newConnection();
        sendingBus = new EasyBus(new RabbitMQBackingBusImpl(connection1,true));
        receivingBus = new EasyBus(new RabbitMQBackingBusImpl(connection2,true));
        receivingBus.register(RabbitMQNamedTestListener.class)
                .register(RabbitMQTestListener.class)
                .register(MultiHandlerTestListener.class);
    }

    @After
    public void teardown() throws Exception {
        sendingBus.close();
        receivingBus.close();
    }

    @Test(timeout = 10000)
    public void attemptSendingWhenExchangeAlreadyExistsShouldWork() throws IOException, TimeoutException, InterruptedException {
        factory.newConnection().createChannel().exchangeDeclare("declared.exchange", BuiltinExchangeType.TOPIC);
        receivingBus.register(RabbitMQNamedTestOnCratedExchangeEventListener.class);
        factory.newConnection().createChannel().basicPublish("declared.exchange", "all", null, "{}".getBytes());
        while (!RabbitMQNamedTestOnCreatedExchangeEvent.handled) {
            Thread.sleep(100);
        }
        assertTrue(RabbitMQNamedTestOnCreatedExchangeEvent.handled);
    }

    @Test
    public void attemptSendingNull() {
        sendingBus.post(null);
    }

    @Test(timeout = 10000)
    public void handleEvent() throws InterruptedException {
        RabbitMQTestEvent.handled = false;
        sendingBus.post(new RabbitMQTestEvent());
        while (!RabbitMQTestEvent.handled) {
            Thread.sleep(100);
        };
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

}
