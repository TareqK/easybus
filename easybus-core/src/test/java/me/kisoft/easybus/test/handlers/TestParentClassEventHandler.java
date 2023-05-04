package me.kisoft.easybus.test.handlers;

import me.kisoft.easybus.Handler;
import me.kisoft.easybus.test.events.TestParentClassEvent;

/**
 *
 * @author tareq
 */
public class TestParentClassEventHandler implements Handler<TestParentClassEvent> {

    @Override
    public void handle(TestParentClassEvent event) {
        TestParentClassEvent.checked = true;
        TestParentClassEvent.doChildCheck();
    }
}
