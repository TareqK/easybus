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
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import me.kisoft.easybus.Bus;
import me.kisoft.easybus.EventHandler;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tareq
 */
public class RabbitMQBusImpl implements Bus {

    private final Logger log = LoggerFactory.getLogger(RabbitMQBusImpl.class);
    private final Connection connection;
    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<EventHandler, String> tagMap = new HashMap<>();
    private final Map<EventHandler, Channel> channelMap = new HashMap<>();
    private final Map<String, Boolean> exchangeExistanceMap = new HashMap<>();

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
        try ( Channel channel = this.connection.createChannel()) {
            String exchangeName = getExcahngeName(object.getClass());
            if (!exchangeExistanceMap.getOrDefault(exchangeName, Boolean.FALSE)) {
                channel.exchangeDeclare(exchangeName, BuiltinExchangeType.FANOUT);
                log.debug(String.format("Declared Exchange %s", exchangeName));
                exchangeExistanceMap.put(exchangeName, Boolean.TRUE);
            }
            log.debug(String.format("Published Message to  Exchange %s", exchangeName));
            channel.basicPublish(getExcahngeName(object.getClass()), "all", null, mapper.writer().writeValueAsBytes(object));
        } catch (IOException | TimeoutException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void clear() {
        clearTags();
        clearChannels();
    }

    @Override
    public void addHandler(EventHandler handler) {
        String exchangeName = getExcahngeName(handler.getEventClass());
        String queueName = getQueueName(handler.getHandler());
        try {
            Channel channel = this.connection.createChannel();
            channel.addShutdownListener(cause -> {
                if (cause.isHardError()) {
                    log.error(String.format("Channel for Queue(Event) Handler %s was closed : %s", queueName, cause.getMessage()));
                } else {
                    log.warn(String.format("Channel for Queue(Event) Handler %s was closed normally : %s", queueName, cause.getMessage()));
                }
            });
            channel.exchangeDeclare(exchangeName, BuiltinExchangeType.FANOUT);
            channel.queueDeclare(queueName, false, false, false, null).getQueue();
            channel.queueBind(queueName, exchangeName, RandomStringUtils.randomAlphabetic(30));
            String tag = channel.basicConsume(getQueueName(handler.getHandler()), (consumerTag, delivery) -> {
                log.debug(String.format("Received Message from Exchange %s Queue %s with Delivery Tag %s", exchangeName, queueName, String.valueOf(delivery.getEnvelope().getDeliveryTag())));
                handler.handle(mapper.reader().forType(handler.getEventClass()).readValue(delivery.getBody()));
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            }, consumerTag -> {
                log.debug(String.format("Cancelling Consumer with Tag %s for Queue %s", consumerTag, queueName));
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
            try ( Channel channel = channelMap.get(handler)) {
                channel.basicCancel(consumerTag);
            }
        } catch (IOException | TimeoutException ex) {
            throw new RuntimeException(ex);
        } finally {
            channelMap.remove(handler);
            tagMap.remove(handler);
        }
    }

    @Override
    public void close() throws IOException {
        clear();
        connection.close();
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

    private String getExcahngeName(Class clazz) {
        ExchangeName exchangeName = (ExchangeName) clazz.getAnnotation(ExchangeName.class);
        if (exchangeName != null) {
            return exchangeName.value();
        }
        return clazz.getSimpleName();
    }

    private void clearChannels() {
        try {
            channelMap.values().forEach(usedChannel -> {
                try {
                    usedChannel.close();
                } catch (IOException | TimeoutException ex) {
                    log.warn(String.format("Failed to close channel %s : %s", usedChannel.getChannelNumber(), ex.getMessage()));
                }
            });
        } catch (Exception ex) {
            log.error(ex.getMessage());
        } finally {
            channelMap.clear();
        }
    }

    private void clearTags() {
        try ( Channel channel = this.connection.createChannel()) {
            tagMap.values().forEach(tag -> {
                try {
                    channel.basicCancel(tag);
                } catch (IOException ex) {
                    log.warn(String.format("Failed to close tag %s : %s", tag, ex.getMessage()));
                }
            });
        } catch (IOException | TimeoutException ex) {
            log.error(ex.getMessage());
        } finally {
            tagMap.clear();
        }
    }

}
