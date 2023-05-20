package me.kisoft.easybus.test.handlers;

import me.kisoft.easybus.test.events.TestChildClassEvent;
import me.kisoft.easybus.Listener;

/**
 *
 * @author tareq
 */
public class TestChildClassEventListener implements Listener<TestChildClassEvent> {

    @Override
    public void on(TestChildClassEvent event) {
        TestChildClassEvent.checkedSpecific = true;
    }
}
