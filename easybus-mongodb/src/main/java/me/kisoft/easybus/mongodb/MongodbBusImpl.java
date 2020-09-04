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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import me.kisoft.easybus.Bus;
import me.kisoft.easybus.EventHandler;
import org.jongo.Jongo;
import org.jongo.MongoCollection;

/**
 *
 * @author tareq
 */
public class MongodbBusImpl implements Bus {

    private final ScheduledExecutorService pool;
    private final long pollTime;
    private final Map<EventHandler, ScheduledFuture> futureMap = new HashMap<>();
    private final Jongo jongo;

    public MongodbBusImpl() {
        this.pool = Executors.newScheduledThreadPool(15);
        this.pollTime = 20l;
        this.jongo = null;

    }

    public MongodbBusImpl(Jongo jongo, int numberOfThreads, long pollTime) {
        this.pool = Executors.newScheduledThreadPool(numberOfThreads);
        this.pollTime = pollTime;
        this.jongo = jongo;
    }

    public MongodbBusImpl(Jongo jongo) {
        this.pool = Executors.newScheduledThreadPool(15);
        this.pollTime = 20l;
        this.jongo = jongo;
    }

    @Override
    public void post(Object object) {
        MongodbEvent event = new MongodbEvent(object);
        this.jongo.getCollection(object.getClass().getCanonicalName()).save(event);
    }

    @Override
    public void clear() {
        futureMap.values().stream().forEach(future -> future.cancel(false));
        futureMap.clear();
    }

    @Override
    public void addHandler(EventHandler handler) {
        ScheduledFuture future = pool.scheduleAtFixedRate(new MongodbCollectionPollRunnable(handler, this.jongo), 0l, this.pollTime, TimeUnit.SECONDS);
        futureMap.put(handler, future);
    }

    @Override
    public void removeHandler(EventHandler handler) {
        ScheduledFuture future = futureMap.get(handler);
        if (future != null) {
            future.cancel(false);
            futureMap.remove(handler);
        }
    }

}
