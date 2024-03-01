package me.kisoft.easybus.rabbitmq;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
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
        memoryBusImpl.clear();
    }

    @Override
    public void close() throws Exception {
        try {
            memoryBusImpl.close();
            connection.close();
        } catch (Exception ex) {
            throw ex;
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

    protected class RabbitMQBackingBusConsumer extends DefaultConsumer {

        private ExecutorService executor;
        private final Class eventClass;
        private final Listener eventListener;
        private final String exchangeName;
        private final String queueName;
        private final ObjectReader reader;

        public RabbitMQBackingBusConsumer(Channel channel, Class eventClass, Listener eventListener) {
            super(channel);
            this.eventClass = eventClass;
            this.eventListener = eventListener;
            this.exchangeName = getExcahngeName(eventClass);
            this.queueName = getQueueName(eventListener);
            this.reader = mapper.reader().forType(eventClass);
        }

        @Override
        public void handleConsumeOk(String consumerTag) {
            executor = Executors.newFixedThreadPool(maxPrefetch);
            memoryBusImpl.addListener(eventClass, eventListener);//idempotent
        }

        @Override
        public void handleCancel(String consumerTag) throws IOException {
            log.warn("Forced Cancelling Consumer for Listener {} , event {}", queueName, exchangeName);
            executor.shutdown();
        }

        @Override
        public void handleCancelOk(String consumerTag) {
            log.warn("Cancelling Consumer for Listener {} , event {}", queueName, exchangeName);
            if (this.getChannel().isOpen()) {
                try {
                    this.getChannel().close();
                } catch (IOException | TimeoutException ex) {
                    log.error("Exception while attempting to close consumer : {}", ex.getMessage());
                }
            }
        }

        @Override
        public void handleShutdownSignal(String consumerTag, ShutdownSignalException sig) {
            log.info("Consumer for Queue(Event) Listener {} was shutdown : {}", queueName, sig.getMessage());
            executor.shutdown();
            if (sig.isInitiatedByApplication()) {
                log.warn("Consumer for Queue(Event) Listener {} was shutdown permanently by applicaiton : {}", queueName, sig.getMessage());
                return;
            }
            if (sig.isHardError()) {
                log.warn("Consumer for Queue(Event) Listener {} was closed abnormaly : {}", queueName, sig.getReason());
            } else {
                log.warn("Consumer for Queue(Event) Listener {} was closed normally : {}", queueName, sig.getReason());
            }
            rebindingExecutor.schedule(() -> {
                log.warn("Attempting to rebind Consumer for Queue(Event) Listener {}", queueName);
                doAddListener(eventClass, eventListener, 1, retries);
            }, retryThresholdMillis, TimeUnit.MILLISECONDS);
        }

        @Override
        public void handleDelivery(String string, Envelope envlp, AMQP.BasicProperties bp, byte[] bytes) throws IOException {
            final byte[] body = bytes;
            final long deliveryTag = envlp.getDeliveryTag();
            executor.submit(() -> {
                boolean doAck = false;
                log.trace("Received Message from Exchange {} Queue {} with Delivery Tag {}", exchangeName, queueName, deliveryTag);
                Object receivedEvent;
                try {
                    receivedEvent = reader.readValue(body);
                } catch (Throwable ex) {
                    log.warn("Error Decoding message from Exchange {}, class {} : {} ", exchangeName, eventClass, ex.getMessage());
                    doAck = true;
                    receivedEvent = null;
                }
                if (receivedEvent != null) {
                    try {
                        memoryBusImpl.post(receivedEvent);
                        doAck = true;
                    } catch (Throwable ex) {
                        log.warn("Failure when processing event of type {}, Listener {} : {}", eventClass, eventListener, ex.getMessage());
                        doAck = false;
                    } finally {
                        receivedEvent = null;
                        log.trace("Finished Receiving Message from Exchange {} Queue {} with Delivery Tag {}", exchangeName, queueName, deliveryTag);
                    }
                }
                try {
                    if (doAck) {
                        this.getChannel().basicAck(deliveryTag, false);
                    } else {
                        this.getChannel().basicNack(deliveryTag, false, requeue);
                    }
                } catch (IOException ex) {
                    log.error("RabbitMQ Exception when processing Message from Exchange {} Queue {} with Delivery Tag {} : {}", exchangeName, queueName, deliveryTag, ex.getMessage());
                } catch (Throwable ex) {
                    log.error("Exception when processing Message from Exchange {} Queue {} with Delivery Tag {} : {}", exchangeName, queueName, deliveryTag, ex.getMessage());
                }
            });
        }

    }

    private void doAddListener(Class eventClass, Listener eventListener, int retry, int maxRetries) {
        if (retry < 1 || maxRetries < 1) {
            rebindingExecutor.schedule(() -> doAddListener(eventClass, eventListener, 1, 1), 50, TimeUnit.MILLISECONDS);
            return;
        }

        if (retry > maxRetries) {
            log.error("Failure to add listener {} for event {} : too many retries({}/{})", eventListener, eventClass, retry, maxRetries);
        }

        try {
            log.warn("Attempting to add listener {} for event {} : attempt ({}/{})", eventListener, eventClass, retry, maxRetries);
            String exchangeName = getExcahngeName(eventClass);
            BuiltinExchangeType type = getExchangeType(eventClass);
            String queueName = getQueueName(eventListener);
            Set<String> routingKeys = getRoutingKeys(eventListener);

            Channel channel = connection.createChannel();
            channel.basicQos(maxPrefetch, false);
            verifyOrUpdateExchange(exchangeName, type);
            String queue = channel.queueDeclare(queueName, false, false, false, null).getQueue();

            for (String routingKey : routingKeys) {
                channel.queueBind(queue, exchangeName, routingKey);
            }
            Consumer consumer = new RabbitMQBackingBusConsumer(channel, eventClass, eventListener);
            channel.basicConsume(queueName, consumer);
            log.warn("Successfully added listener {} for event {} : attempt ({}/{})", eventListener, eventClass, retry, maxRetries);
        } catch (Throwable ex) {
            log.warn("Failed to add listener {} for event {} : {}, trying again", eventListener, eventClass, ex.getMessage());
            rebindingExecutor.schedule(() -> doAddListener(eventClass, eventListener, (retry + 1), maxRetries), retry * this.retryThresholdMillis, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    protected void addListener(Class eventClass, Listener listener) {
        doAddListener(eventClass, listener, 1, this.retries);
    }

}
