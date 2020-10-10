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
import java.util.logging.Level;
import lombok.extern.java.Log;
import me.kisoft.easybus.EventHandler;
import org.jongo.Jongo;

/**
 *
 * @author tareq
 */
@Log
public class MongodbCollectionPollRunnable implements Runnable {

    private final EventHandler handler;
    private final Jongo jongo;
    private final ObjectMapper mapper = new ObjectMapper();

    public MongodbCollectionPollRunnable(EventHandler handler, Jongo jongo) {
        if (handler == null) {
            throw new IllegalArgumentException("Attempting to start runnable with null handler");
        }
        if (jongo == null) {
            throw new IllegalArgumentException("Attempting to start runnable with null jongo");
        }
        this.handler = handler;
        this.jongo = jongo;
    }

    @Override
    public void run() {
        try {
            MongodbEvent event = this.jongo.getCollection(handler.getEventClassName())
                    .findAndModify("{processing:false,handled:false}")
                    .sort("{lastAccess:1}")
                    .with("{$set:{processing:true,lastAccess:#}}", new Date())
                    .as(MongodbEvent.class);

            if (event != null) {
                Object data = mapper.convertValue(event.getData(), this.handler.getEventClass());
                try {
                    this.handler.handle(data);
                    event = this.jongo.getCollection(handler.getEventClassName())
                            .findAndModify("{eventId:#}", event.getEventId())
                            .with("{$set:{processing:false,handled:true,lastAccess:#}}", new Date())
                            .as(MongodbEvent.class);

                } catch (RuntimeException ex) {
                    log.fine(ex.getMessage());
                    event = this.jongo.getCollection(handler.getEventClassName())
                            .findAndModify("{eventId:#}", event.getEventId())
                            .with("{$set:{processing:false,handled:false,lastAccess:#}}", new Date())
                            .as(MongodbEvent.class);
                    log.log(Level.FINE, "Re-Submitting event with id : {0}", event.getEventId());
                }

            }
        } catch (IllegalArgumentException ex) {
            log.severe(ex.getMessage());
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
            return this.handler.equals(casted.handler) && this.jongo.equals(casted.jongo);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.handler.hashCode();
    }
}
