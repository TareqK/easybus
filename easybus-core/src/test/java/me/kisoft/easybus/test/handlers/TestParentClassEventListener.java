package me.kisoft.easybus.test.handlers;

import me.kisoft.easybus.test.events.TestParentClassEvent;
import me.kisoft.easybus.Listener;

/**
 *
 * @author tareq
 */
public class TestParentClassEventListener implements Listener<TestParentClassEvent> {

    @Override
    public void on(TestParentClassEvent event) {
        TestParentClassEvent.checked = true;
        TestParentClassEvent.doChildCheck();
    }
}
