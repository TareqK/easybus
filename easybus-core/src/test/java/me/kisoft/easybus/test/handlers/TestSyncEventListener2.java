package me.kisoft.easybus.test.handlers;

import me.kisoft.easybus.Listener;
import me.kisoft.easybus.test.events.TestSyncEvent;

public class TestSyncEventListener2 implements Listener<TestSyncEvent> {

    @Override
    public void on(TestSyncEvent event) {
        TestSyncEvent.checked = true;
    }
}
