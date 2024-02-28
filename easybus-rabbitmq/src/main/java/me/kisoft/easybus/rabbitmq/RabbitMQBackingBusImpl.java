package me.kisoft.easybus.rabbitmq;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.CancelCallback;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConsumerShutdownSignalCallback;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.DeliverCallback;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import me.kisoft.easybus.BackingBus;
import me.kisoft.easybus.memory.MemoryBackingBusImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import me.kisoft.easybus.Listener;

/**
 *
 * @author tareq
 */
public class RabbitMQBackingBusImpl extends BackingBus {

    protected static final Logger log = LoggerFactory.getLogger(RabbitMQBackingBusImpl.class);
    protected final Connection connection;
    protected final ObjectMapper mapper = new ObjectMapper()
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    protected final Map<Class, String> tagMap = new HashMap<>();
    protected final Map<Class, Channel> channelMap = new HashMap<>();
    protected final Set<String> exchangeList = new HashSet<>();
    protected final MemoryBackingBusImpl memoryBusImpl = new MemoryBackingBusImpl();
    protected final ReentrantLock declarationLock = new ReentrantLock();
    protected final boolean allowUpdate;
    protected final int maxPrefetch;

    public RabbitMQBackingBusImpl(Connection connection, boolean allowUpdate, int maxPrefetch) {
        this.connection = connection;
        this.allowUpdate = allowUpdate;
        this.maxPrefetch = maxPrefetch;
    }

    public RabbitMQBackingBusImpl(Connection connection, boolean allowUpdate) {
        this(connection, allowUpdate, 10);
    }

    @Override
    public void post(Object object) {
        try (Channel channel = connection.createChannel()) {
            String exchangeName = getExcahngeName(object);
            BuiltinExchangeType type = getExchangeType(object);
            verifyOrUpdateExchange(exchangeName, type);
            log.debug(String.format("Published Message to  Exchange %s", exchangeName));
            channel.basicPublish(getExcahngeName(object.getClass()), "all", null, mapper.writer().writeValueAsBytes(object));
        } catch (IOException | TimeoutException ex) {
            throw new RuntimeException(ex);
        }
    }

    protected void verifyOrUpdateExchange(String exchangeName, BuiltinExchangeType type) throws IOException, TimeoutException {
        if (!exchangeList.contains(exchangeName)) {
            declarationLock.lock();
            try {
                boolean exchangeExists;
                if (!exchangeList.contains(exchangeName)) {
                    try (Channel verificationChannel = connection.createChannel()) {
                        verificationChannel.exchangeDeclarePassive(exchangeName);
                        log.debug(String.format("Exchange %s already exists", exchangeName));
                        exchangeExists = true;

                    } catch (IOException ex) {
                        //exchange does not exist, declare it 
                        exchangeExists = false;
                    }

                    boolean exchangeNeedsUpdate = false;
                    if (!exchangeExists) {
                        try (Channel creationChannel = connection.createChannel()) {
                            creationChannel.exchangeDeclare(exchangeName, type);
                            log.debug(String.format("Declared Exchange %s", exchangeName));
                            exchangeList.add(exchangeName);
                            exchangeNeedsUpdate = false;

                        } catch (IOException ex) {
                            //exchange type mismatched
                            exchangeNeedsUpdate = true;
                        }
                    }

                    if (exchangeNeedsUpdate && allowUpdate) {
                        try (Channel updateChannel = connection.createChannel()) {
                            updateChannel.exchangeDelete(exchangeName, true);
                            updateChannel.exchangeDeclare(exchangeName, type);
                            exchangeList.add(exchangeName);
                        }
                    }
                }

            } finally {
                declarationLock.unlock();
            }
        }
    }

    @Override
    public void clear() {
        clearTags();
        clearChannels();
    }

    @Override
    public void close() throws IOException {
        try (connection) {
            clear();
        }
    }

    protected BuiltinExchangeType getExchangeType(Object object) {
        return getExchangeType(object.getClass());
    }

    protected BuiltinExchangeType getExchangeType(Class clazz) {
        ExchangeType annotation = (ExchangeType) clazz.getAnnotation(ExchangeType.class);
        if (annotation == null || annotation.value() == null) {
            return BuiltinExchangeType.FANOUT;
        }
        return annotation.value();
    }

    protected String getQueueName(Object object) {
        return getQueueName(object.getClass());
    }

    protected String getQueueName(Class clazz) {
        QueueName queueName = (QueueName) clazz.getAnnotation(QueueName.class);
        if (queueName != null) {
            return queueName.value();
        }
        return clazz.getSimpleName();
    }

    protected String getExcahngeName(Object object) {
        return getExcahngeName(object.getClass());
    }

    protected String getExcahngeName(Class clazz) {
        ExchangeName exchangeName = (ExchangeName) clazz.getAnnotation(ExchangeName.class);
        if (exchangeName != null) {
            return exchangeName.value();
        }
        return clazz.getSimpleName();
    }

    protected Set<String> getRoutingKeys(Object object) {
        return getRoutingKeys(object.getClass());
    }

    protected Set<String> getRoutingKeys(Class clazz) {
        RoutingKey[] routingKeys = (RoutingKey[]) clazz.getAnnotationsByType(RoutingKey.class);
        if (routingKeys == null || routingKeys.length == 0) {
            return Set.of("#"); //read all messages
        }
        return Arrays.stream(routingKeys)
                .map(RoutingKey::value)
                .distinct()
                .collect(Collectors.toSet());
    }

    protected void clearChannels() {
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

    protected void clearTags() {
        try (Channel channel = connection.createChannel()) {
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

    @Override
    protected void addHandler(Class eventClass, Listener listener) {
        String exchangeName = getExcahngeName(eventClass);
        BuiltinExchangeType type = getExchangeType(eventClass);
        String queueName = getQueueName(listener);
        Set<String> routingKeys = getRoutingKeys(listener);
        try {
            Channel channel = connection.createChannel();
            ObjectReader reader = mapper.reader().forType(eventClass);
            verifyOrUpdateExchange(exchangeName, type);
            String queue = channel.queueDeclare(queueName, false, false, false, null).getQueue();
            for (String routingKey : routingKeys) {
                channel.queueBind(queue, exchangeName, routingKey);
            }

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                try {
                    log.trace(String.format("Received Message from Exchange %s Queue %s with Delivery Tag %s", exchangeName, queueName, String.valueOf(delivery.getEnvelope().getDeliveryTag())));
                    Object receivedEvent = reader.readValue(delivery.getBody());
                    memoryBusImpl.post(receivedEvent);
                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                } catch (Throwable ex) {
                    log.info(ex.getMessage());
                    channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, true);
                }
            };

            CancelCallback cancelCallback = (tag) -> {
                tagMap.remove(eventClass);
                channelMap.remove(eventClass);
            };

            ConsumerShutdownSignalCallback shutdownCallback = (tag, cause) -> {
                if (cause.isHardError()) {
                    log.error(String.format("Channel for Queue(Event) Handler %s was closed : %s", queueName, cause.getMessage()));
                } else {
                    log.warn(String.format("Channel for Queue(Event) Handler %s was closed normally : %s", queueName, cause.getMessage()));
                }

            };
            channel.basicQos(maxPrefetch);
            channel.setDefaultConsumer(new DefaultConsumer(channel));
            String tag = channel.basicConsume(queueName, false, deliverCallback, cancelCallback, shutdownCallback);
            tagMap.put(eventClass, tag);
            channelMap.put(eventClass, channel);
            memoryBusImpl.addHandler(eventClass, listener);
        } catch (IOException | TimeoutException ex) {
            log.error("Failed to add listener {} : {}", listener, ex);
            throw new RuntimeException(ex);
        }
    }

}
