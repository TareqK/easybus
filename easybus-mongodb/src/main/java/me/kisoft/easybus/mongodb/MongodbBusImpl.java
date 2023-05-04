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
package me.kisoft.easybus.mongodb;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import me.kisoft.easybus.Handler;
import me.kisoft.easybus.memory.MemoryBusImpl;
import org.jongo.Jongo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tareq
 */
public class MongodbBusImpl extends MemoryBusImpl {

    private final Logger log = LoggerFactory.getLogger(MongodbBusImpl.class);
    private final ScheduledExecutorService pool;
    private final long pollTime;
    private final Map<Class, ScheduledFuture> futureMap = new HashMap<>();
    private final Jongo jongo;
    private final MemoryBusImpl memoryBusImpl = new MemoryBusImpl();
    ObjectMapper mapper = new ObjectMapper();

    public MongodbBusImpl() {
        this.pool = Executors.newScheduledThreadPool(15);
        this.pollTime = 10l;
        this.jongo = null;

    }

    public MongodbBusImpl(Jongo jongo, int numberOfThreads, long pollTime) {
        this.pool = Executors.newScheduledThreadPool(numberOfThreads);
        this.pollTime = pollTime;
        this.jongo = jongo;
    }

    public MongodbBusImpl(Jongo jongo) {
        this.pool = Executors.newScheduledThreadPool(15);
        this.pollTime = 10l;
        this.jongo = jongo;
    }

    @Override
    public void post(Object object) {
        MongodbEvent event = new MongodbEvent(mapper.convertValue(object, Map.class));
        this.jongo.getCollection(object.getClass().getCanonicalName()).save(event);
    }

    @Override
    public void clear() {
        futureMap.values().stream().forEach(future -> future.cancel(false));
        futureMap.clear();
    }

    @Override
    public void addHandler(Class eventClass, Handler handler) {
        ScheduledFuture future = pool.scheduleAtFixedRate(new MongodbCollectionPollRunnable(eventClass, this.jongo), 0l, this.pollTime, TimeUnit.MILLISECONDS);
        futureMap.put(eventClass, future);
        this.memoryBusImpl.addHandler(eventClass, handler);
    }

    protected class MongodbCollectionPollRunnable implements Runnable {

        private final Logger log = LoggerFactory.getLogger(MongodbCollectionPollRunnable.class);
        private final Class eventClass;
        private final Jongo jongo;
        private final ObjectMapper mapper = new ObjectMapper();

        public MongodbCollectionPollRunnable(Class eventClass, Jongo jongo) {
            if (eventClass == null) {
                throw new IllegalArgumentException("Attempting to start runnable with null handler");
            }
            if (jongo == null) {
                throw new IllegalArgumentException("Attempting to start runnable with null jongo");
            }
            this.eventClass = eventClass;
            this.jongo = jongo;
        }

        @Override
        public void run() {
            try {
                MongodbEvent event = this.jongo.getCollection(eventClass.getCanonicalName())
                        .findAndModify("{processing:false,handled:false}")
                        .sort("{lastAccess:1}")
                        .with("{$set:{processing:true,lastAccess:#}}", new Date())
                        .as(MongodbEvent.class);

                if (event != null) {
                    Object receivedEvent = this.eventClass.cast(mapper.convertValue(event.getData(), this.eventClass));
                    try {
                        memoryBusImpl.post(receivedEvent);
                        this.jongo.getCollection(eventClass.getCanonicalName())
                                .findAndModify("{eventId:#}", event.getEventId())
                                .with("{$set:{processing:false,handled:true,lastAccess:#}}", new Date())
                                .as(MongodbEvent.class);

                    } catch (RuntimeException ex) {
                        log.debug(ex.getMessage());
                        this.jongo.getCollection(eventClass.getCanonicalName())
                                .findAndModify("{eventId:#}", event.getEventId())
                                .with("{$set:{processing:false,handled:false,lastAccess:#}}", new Date())
                                .as(MongodbEvent.class);
                        log.debug(String.format("Re-Submitting event with id : %s", event.getEventId()));
                    }

                }
            } catch (IllegalArgumentException ex) {
                log.error(ex.getMessage());
            }
        }

        @Override
        public boolean equals(Object other) {
            if (other == null) {
                return false;
            }
            if (this == other) {
                return true;
            }
            if (other instanceof MongodbCollectionPollRunnable) {
                MongodbCollectionPollRunnable casted = (MongodbCollectionPollRunnable) other;
                return this.eventClass.equals(casted.eventClass) && this.jongo.equals(casted.jongo);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return this.eventClass.hashCode();
        }
    }

}
