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
package me.kisoft.easybus.memory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import me.kisoft.easybus.Bus;
import me.kisoft.easybus.Handler;

/**
 *
 * @author tareq
 */
public class MemoryBusImpl extends Bus {

    private final Map<Class, Set<Handler<Object>>> handlerMap = new HashMap<>();
    private final ExecutorService pool;

    public MemoryBusImpl() {
        pool = Executors.newCachedThreadPool();
    }

    @Override
    public void clear() {
        handlerMap.clear();
    }

    public Set<Handler> getHandlers() {
        return handlerMap.values().stream().flatMap(list -> list.stream()).distinct().collect(Collectors.toSet());
    }

    @Override
    public void addHandler(Class eventClass, Handler handler) {
        if (!handlerMap.containsKey(eventClass)) {
            handlerMap.put(eventClass, new HashSet<>());
        }
        if (handlerMap.get(eventClass).contains(handler)) {
            return;
        }
        handlerMap.get(eventClass).add(handler);
    }

    private <T extends Object> void doHandle(Handler<T> handler, T event) {
        if (handler.getClass().isAnnotationPresent(AsyncHandler.class)) {
            pool.submit(() -> this.handle(event, handler));
        } else {
            this.handle(event, handler);
        }
    }

    @Override
    public <T> void post(T event) {
        Set<Handler<Object>> eventHandlers = handlerMap.entrySet()
                .stream()
                .filter(entry -> entry.getKey().isAssignableFrom(event.getClass()))
                .flatMap(entry -> entry.getValue().stream())
                .collect(Collectors.toSet());
        if (eventHandlers == null || eventHandlers.isEmpty()) {
            return;
        }
        eventHandlers.stream().forEach(handler -> doHandle(handler, event));
    }
}
