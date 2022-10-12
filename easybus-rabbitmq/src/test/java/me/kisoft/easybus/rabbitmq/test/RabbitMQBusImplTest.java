/*
 * Copyright 2020 tareq.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.kisoft.easybus.rabbitmq.test;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import me.kisoft.easybus.EasyBus;
import me.kisoft.easybus.rabbitmq.RabbitMQBusImpl;
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

    private static EasyBus bus;
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
        Connection connection = factory.newConnection();
        bus = new EasyBus(new RabbitMQBusImpl(connection));
        bus.search("me.kisoft.easybus.rabbitmq.test");
    }

    @Test(timeout = 10000)
    public void handleEvent() throws InterruptedException {
        RabbitMQTestEvent.handled = false;
        bus.post(new RabbitMQTestEvent());
        while (!RabbitMQTestEvent.handled) {
            Thread.sleep(100);
        }
        assertTrue(RabbitMQTestEvent.handled);
    }

    @Test(timeout = 10000)
    public void handleNamedEvent() throws InterruptedException {
        RabbitMQNamedTestEvent.handled = false;
        bus.post(new RabbitMQNamedTestEvent());
        while (!RabbitMQNamedTestEvent.handled) {
            Thread.sleep(100);
        }
        assertTrue(RabbitMQNamedTestEvent.handled);
    }

    @Test(timeout = 10000)
    public void multiEventHandlerTest() throws InterruptedException {
        RabbitMQTestEvent.handled = false;
        RabbitMQTestEvent.handled2 = false;
        bus.post(new RabbitMQTestEvent());
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
        bus.close();
    }
}
