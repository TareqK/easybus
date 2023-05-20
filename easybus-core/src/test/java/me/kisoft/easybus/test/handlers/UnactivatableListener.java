package me.kisoft.easybus.test.handlers;

import me.kisoft.easybus.Listener;
import me.kisoft.easybus.test.events.TestSyncEvent;

public class UnactivatableListener implements Listener<TestSyncEvent> {

    private UnactivatableListener() {

    }

    public UnactivatableListener(Object object) {

    }

    @Override
    public void on(TestSyncEvent event) {
        // no-op
    }
}
