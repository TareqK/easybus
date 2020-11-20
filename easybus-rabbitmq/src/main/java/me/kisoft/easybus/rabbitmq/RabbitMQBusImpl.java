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
package me.kisoft.easybus.rabbitmq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import lombok.extern.java.Log;
import me.kisoft.easybus.Bus;
import me.kisoft.easybus.EventHandler;

/**
 *
 * @author tareq
 */
@Log
public class RabbitMQBusImpl implements Bus {

    private final Connection connection;
    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<EventHandler, String> tagMap = new HashMap<>();
    private final Map<EventHandler, Channel> channelMap = new HashMap<>();

    public RabbitMQBusImpl(Connection connection) {
        this.connection = connection;
    }

    public RabbitMQBusImpl() {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            this.connection = factory.newConnection();
        } catch (IOException | TimeoutException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void post(Object object) {
        try (Channel channel = this.connection.createChannel()) {
            channel.basicPublish("", getQueueName(object), null, mapper.writer().writeValueAsBytes(object));
        } catch (IOException | TimeoutException ex) {
            throw new RuntimeException(ex);
        }
    }

    private String getQueueName(Object object) {
        return this.getQueueName(object.getClass());
    }

    private String getQueueName(Class clazz) {
        QueueName queueName = (QueueName) clazz.getAnnotation(QueueName.class);
        if (queueName != null) {
            return queueName.value();
        }
        return clazz.getSimpleName();
    }

    @Override
    public void clear() {
        try (Channel channel = this.connection.createChannel()) {
            tagMap.values().forEach(tag -> {
                try {
                    channel.basicCancel(tag);
                } catch (IOException ex) {
                    log.severe(ex.getMessage());
                }
            });
            channelMap.values().forEach(usedChannel -> {
                try {
                    usedChannel.close();
                } catch (IOException | TimeoutException ex) {
                    log.severe(ex.getMessage());
                }
            });
        } catch (IOException | TimeoutException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void addHandler(EventHandler handler) {
        try {
            Channel channel = this.connection.createChannel();
            channel.queueDeclare(getQueueName(handler.getEventClass()), false, false, false, null).getQueue();
            log.info(String.format("Declaring Queue %s for Event %s", getQueueName(handler.getEventClass()), handler.getEventClassName()));
            String tag = channel.basicConsume(getQueueName(handler.getEventClass()), (consumerTag, delivery) -> {
                log.fine(String.format("Received Message from Queue %s with Delivery Tag %s", getQueueName(handler.getEventClassName()), String.valueOf(delivery.getEnvelope().getDeliveryTag())));
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                handler.handle(mapper.reader().forType(handler.getEventClass()).readValue(delivery.getBody()));
            }, consumerTag -> {
            });
            tagMap.put(handler, tag);
            channelMap.put(handler, channel);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void removeHandler(EventHandler handler) {
        try {
            String consumerTag = tagMap.get(handler);
            Channel channel = channelMap.get(handler);
            channel.basicCancel(consumerTag);
            channel.close();
        } catch (IOException | TimeoutException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void close() throws IOException {
        channelMap.values().forEach(channel -> {
            try {
                channel.close();
            } catch (IOException | TimeoutException ex) {
                log.severe(ex.getMessage());
            }
        });
        connection.close();
    }
}
