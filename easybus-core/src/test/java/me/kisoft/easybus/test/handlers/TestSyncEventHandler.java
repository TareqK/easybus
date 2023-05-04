package me.kisoft.easybus.test.handlers;

import me.kisoft.easybus.Handler;
import me.kisoft.easybus.test.events.TestSyncEvent;

/**
 *
 * @author tareq
 */
public class TestSyncEventHandler implements Handler<TestSyncEvent> {

    @Override
    public void handle(TestSyncEvent event) {
        TestSyncEvent.checked = true;
    }
}
