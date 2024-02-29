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
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.NonNull;
import me.kisoft.easybus.BackingBus;
import me.kisoft.easybus.memory.MemoryBackingBusImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import me.kisoft.easybus.Listener;

/**
 *
 * @author tareq
 */
@Builder
public class RabbitMQBackingBusImpl extends BackingBus {
    
    protected static final Logger log = LoggerFactory.getLogger(RabbitMQBackingBusImpl.class);
    
    @NonNull
    private final Connection connection;
    @Builder.Default
    private final ObjectMapper mapper = new ObjectMapper()
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    
    private final Map<Class, String> tagMap = new HashMap<>();
    private final Map<Class, Channel> channelMap = new HashMap<>();
    private final Map<Class, ExecutorService> executorMap = new HashMap<>();
    private final Set<String> exchangeSet = new HashSet<>();
    private final ReentrantLock declarationLock = new ReentrantLock();
    private final ScheduledExecutorService rebindingExecutor = Executors.newSingleThreadScheduledExecutor();
    
    @Builder.Default
    private final MemoryBackingBusImpl memoryBusImpl = new MemoryBackingBusImpl();
    @Builder.Default
    private final boolean allowUpdate = true;
    @Builder.Default
    private final int maxPrefetch = 10;
    @Builder.Default
    private final boolean requeue = true;
    @Builder.Default
    private final int retries = 3;
    @Builder.Default
    private final int retryThresholdMillis = 3000;
    
    @Override
    public void post(Object object) {
        try (Channel channel = connection.createChannel()) {
            String exchangeName = getExcahngeName(object);
            BuiltinExchangeType type = getExchangeType(object);
            verifyOrUpdateExchange(exchangeName, type);
            log.debug("Published Message to  Exchange {}", exchangeName);
            channel.basicPublish(getExcahngeName(object.getClass()), "all", null, mapper.writer().writeValueAsBytes(object));
        } catch (IOException | TimeoutException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    protected void verifyOrUpdateExchange(String exchangeName, BuiltinExchangeType type) throws IOException, TimeoutException {
        if (!exchangeSet.contains(exchangeName)) {
            declarationLock.lock();
            try {
                boolean exchangeExists;
                if (!exchangeSet.contains(exchangeName)) {
                    try (Channel verificationChannel = connection.createChannel()) {
                        verificationChannel.exchangeDeclarePassive(exchangeName);
                        log.debug("Exchange {} already exists", exchangeName);
                        exchangeExists = true;
                    } catch (IOException ex) {
                        exchangeExists = false;
                    }
                    
                    boolean exchangeNeedsUpdate = false;
                    if (!exchangeExists) {
                        try (Channel creationChannel = connection.createChannel()) {
                            creationChannel.exchangeDeclare(exchangeName, type);
                            log.debug("Declared Exchange {}", exchangeName);
                            exchangeSet.add(exchangeName);
                            exchangeNeedsUpdate = false;
                        } catch (IOException ex) {
                            exchangeNeedsUpdate = true;
                        }
                    }
                    
                    if (exchangeNeedsUpdate && allowUpdate) {
                        try (Channel updateChannel = connection.createChannel()) {
                            updateChannel.exchangeDelete(exchangeName, true);
                            updateChannel.exchangeDeclare(exchangeName, type);
                            exchangeSet.add(exchangeName);
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
        clearExecutors();
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
                    log.warn("Failed to close channel {} : {}", usedChannel.getChannelNumber(), ex);
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
                    log.warn("Failed to close tag {} : {}", tag, ex);
                }
            });
        } catch (IOException | TimeoutException ex) {
            log.error(ex.getMessage());
        } finally {
            tagMap.clear();
        }
    }
    
    private void doAddListener(Class eventClass, Listener listener, int retry, int maxRetries) {
        if (retry < 1 || maxRetries < 1) {
            doAddListener(eventClass, listener, 1, 1);
        }
        if (retry > maxRetries) {
            log.error("Failure to add listener {} for event {} : too many retries({}/{})", listener, eventClass, retry, maxRetries);
            throw new RuntimeException("Too Many retries, could not add listener");
        }
        log.warn("Attempting to add listener {} for event {} : attempt ({}/{})", listener, eventClass, retry, maxRetries);
        
        String exchangeName = getExcahngeName(eventClass);
        BuiltinExchangeType type = getExchangeType(eventClass);
        String queueName = getQueueName(listener);
        Set<String> routingKeys = getRoutingKeys(listener);
        ExecutorService executor = executorMap.computeIfAbsent(eventClass, item -> Executors.newFixedThreadPool(maxPrefetch < 1 ? 1 : maxPrefetch));
        try {
            Channel channel = connection.createChannel();
            channel.basicQos(maxPrefetch, false);
            channel.setDefaultConsumer(new DefaultConsumer(channel));
            ObjectReader reader = mapper.reader().forType(eventClass);
            verifyOrUpdateExchange(exchangeName, type);
            String queue = channel.queueDeclare(queueName, false, false, false, null).getQueue();
            for (String routingKey : routingKeys) {
                channel.queueBind(queue, exchangeName, routingKey);
            }
            
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                final byte[] body = delivery.getBody();
                final long deliveryTag = delivery.getEnvelope().getDeliveryTag();
                executor.submit(() -> {
                    boolean doAck = false;
                    log.trace("Received Message from Exchange {} Queue {} with Delivery Tag {}", exchangeName, queueName, deliveryTag);
                    Object receivedEvent;
                    try {
                        receivedEvent = reader.readValue(body);
                    } catch (IOException ex) {
                        log.warn("Error Decoding message from Exchange {}, class {}: {} ", exchangeName, eventClass, ex);
                        doAck = true;
                        receivedEvent = null;
                    }
                    if (receivedEvent != null) {
                        try {
                            memoryBusImpl.post(receivedEvent);
                            doAck = true;
                        } catch (Throwable ex) {
                            log.info("Failure when processing event of type {}, Listener {} : {}", eventClass, listener, ex);
                            doAck = false;
                        } finally {
                            receivedEvent = null;
                            log.trace("Finished Receiving Message from Exchange {} Queue {} with Delivery Tag {}", exchangeName, queueName, deliveryTag);
                        }
                    }
                    try {
                        if (doAck) {
                            channel.basicAck(deliveryTag, false);
                        } else {
                            channel.basicNack(deliveryTag, false, requeue);
                        }
                        
                    } catch (IOException ex) {
                        log.error("RabbitMQ Exception when processing Message from Exchange {} Queue {} with Delivery Tag {}: {}", exchangeName, queueName, deliveryTag, ex);
                    }
                });
            };
            
            CancelCallback cancelCallback = (tag) -> {
                log.warn("Cancelling Consumer for Listener {} , event {}", queueName, exchangeName);
                tagMap.remove(eventClass);
                channelMap.remove(eventClass);
                Optional.ofNullable(executorMap.remove(eventClass)).ifPresent(item -> item.shutdown());
            };
            
            ConsumerShutdownSignalCallback shutdownCallback = (tag, cause) -> {
                log.info("Consumer for Queue(Event) Listener {} was shutdown : {}", queueName, cause.getMessage());
                if (cause.isInitiatedByApplication()) {
                    log.warn("Consumer for Queue(Event) Listener {} was shutdown permanently by applicaiton : {}", queueName, cause.getMessage());
                    return;
                }
                if (cause.isHardError()) {
                    log.warn("Consumer for Queue(Event) Listener {} was closed abnormaly : {}", queueName, cause.getReason());
                } else {
                    log.warn("Consumer for Queue(Event) Listener {} was closed normally : {}", queueName, cause.getReason());
                }
                log.warn("Attempting to rebind Consumer for Queue(Event) Listener {}", queueName);
                rebindingExecutor.submit(() -> {
                    doAddListener(eventClass, listener, 1, maxRetries);
                });
            };
            
            memoryBusImpl.addListener(eventClass, listener);//idempotent
            String tag = channel.basicConsume(queueName, deliverCallback, cancelCallback, shutdownCallback);//ignorable
            tagMap.put(eventClass, tag);//idempotent
            Channel oldChannel = channelMap.put(eventClass, channel);//safe
            try {
                if (oldChannel != null) {
                    oldChannel.close();
                }
            } catch (Exception ex) {
                log.warn("Issue while attempting to close old channel for listener {} : {}", queueName, ex);
            }
        } catch (Exception ex) {
            log.warn("Failed to add listener {} for event {} : {}, trying again", listener, eventClass, ex);
            rebindingExecutor.schedule(() -> doAddListener(eventClass, listener, (retry + 1), maxRetries), retry * this.retryThresholdMillis, TimeUnit.MILLISECONDS);
        }
    }
    
    @Override
    protected void addListener(Class eventClass, Listener listener) {
        doAddListener(eventClass, listener, 1, this.retries);
    }
    
    private void clearExecutors() {
        executorMap.values().stream().forEach(item -> item.shutdown());
        executorMap.clear();
    }
    
}
