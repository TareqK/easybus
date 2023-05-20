package me.kisoft.easybus.test.handlers;

import me.kisoft.easybus.test.events.TestSyncEvent;
import me.kisoft.easybus.Listener;

/**
 *
 * @author tareq
 */
public class TestSyncEventListener implements Listener<TestSyncEvent> {

    @Override
    public void on(TestSyncEvent event) {
        TestSyncEvent.checked = true;
    }
}
