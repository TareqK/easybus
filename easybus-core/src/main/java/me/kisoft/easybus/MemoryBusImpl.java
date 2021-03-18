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
package me.kisoft.easybus;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author tareq
 */
public class MemoryBusImpl implements Bus {

    private final List<EventHandler> handlers = new ArrayList<>();
    private final ExecutorService pool;

    public MemoryBusImpl() {
        pool = Executors.newCachedThreadPool();
    }

    @Override
    public void post(Object event) {
        handlers.parallelStream()
                .filter(handler -> handler.getEventClass().isInstance(event))
                .collect(Collectors.toList())
                .stream()
                .forEach(handler -> doHandle(handler, event));
    }

    private void doHandle(EventHandler handler, Object event) {
        if (handler.isAsync()) {
            pool.submit(() -> handler.handle(event));
        } else {
            handler.handle(event);
        }
    }

    @Override
    public void clear() {
        handlers.clear();
    }

    @Override
    public void addHandler(EventHandler handler) {
        handlers.add(handler);
    }

    @Override
    public void removeHandler(EventHandler handler) {
        handlers.remove(handler);
    }

    public List<EventHandler> getHandlers() {
        return handlers;
    }

}
