package me.kisoft.easybus.test.handlers;

import me.kisoft.easybus.Handler;
import me.kisoft.easybus.test.events.TestChildClassEvent;

/**
 *
 * @author tareq
 */
public class TestChildClassEventHandler implements Handler<TestChildClassEvent> {

    @Override
    public void handle(TestChildClassEvent event) {
        TestChildClassEvent.checkedSpecific = true;
    }
}
