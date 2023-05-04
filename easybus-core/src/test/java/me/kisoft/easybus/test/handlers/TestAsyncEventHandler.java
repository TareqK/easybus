package me.kisoft.easybus.test.handlers;

import me.kisoft.easybus.Handler;
import me.kisoft.easybus.test.events.TestAsyncEvent;
import me.kisoft.easybus.memory.HandleAsync;

/**
 *
 * @author tareq
 */
@HandleAsync
public class TestAsyncEventHandler implements Handler<TestAsyncEvent> {

    @Override
    public void handle(TestAsyncEvent event) {
        TestAsyncEvent.checked = true;
    }
}
