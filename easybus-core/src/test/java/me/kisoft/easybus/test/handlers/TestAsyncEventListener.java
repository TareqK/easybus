package me.kisoft.easybus.test.handlers;

import me.kisoft.easybus.test.events.TestAsyncEvent;
import me.kisoft.easybus.Listener;
import me.kisoft.easybus.memory.ProcessAsync;

/**
 *
 * @author tareq
 */
@ProcessAsync
public class TestAsyncEventListener implements Listener<TestAsyncEvent> {

    @Override
    public void on(TestAsyncEvent event) {
        TestAsyncEvent.checked = true;
    }
}
