package me.kisoft.easybus.test.handlers;

import me.kisoft.easybus.Listener;
import me.kisoft.easybus.test.events.TestDateTimeEvent;

/**
 *
 * @author hussein
 */
public class TestDateTimeEventListener implements Listener<TestDateTimeEvent> {

    @Override
    public void on(TestDateTimeEvent event) {
        TestDateTimeEvent.eventDateTime = event.eventDateTime;
    }
}
