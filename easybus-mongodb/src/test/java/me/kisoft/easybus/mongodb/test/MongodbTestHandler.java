package me.kisoft.easybus.mongodb.test;

import me.kisoft.easybus.Handler;

/**
 *
 * @author tareq
 */
public class MongodbTestHandler implements Handler<MongodbTestEvent> {

    public void handle(MongodbTestEvent event) {
        MongodbTestEvent.checked = true;
    }
}
