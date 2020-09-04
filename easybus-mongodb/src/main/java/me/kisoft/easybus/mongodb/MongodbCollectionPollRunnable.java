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
    private static final String START_HANDLE_QUERY = "{query:{$and:[processing:{$eq:false},handled:{$eq:false}]},update:{$set:{processing:true,lastAccess:#,handled:false}},sort:{lastAccess: 1}}";
    private static final String SUCCESS_HANDLE_QUERY = "{query:{id:{$eq:#}},update:{$set:{processing:true,lastAccess:#,handled:true}}}";
    private static final String FAILED_HANDLE_QUERY = "{query:{id:{$eq:#}},update:{$set:{processing:false,lastAccess:#,handled:false}}}";
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
        MongodbEvent event = this.jongo.getCollection(handler.getEventClassName()).findAndModify(START_HANDLE_QUERY).as(MongodbEvent.class);
        if (event != null) {
            Object data = mapper.convertValue(event.getData(), this.handler.getEventClass());
            try {
                this.handler.handle(data);
                this.jongo.getCollection(handler.getEventClassName()).findAndModify(SUCCESS_HANDLE_QUERY, event.getId(), new Date());

            } catch (RuntimeException ex) {
                log.fine(ex.getMessage());
                this.jongo.getCollection(handler.getEventClassName()).findAndModify(FAILED_HANDLE_QUERY, event.getId(), new Date());
            }
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
