package me.kisoft.easybus.test.events;

import java.time.ZonedDateTime;

/**
 *
 * @author hussein
 */
public class TestDateTimeEvent {

    public static ZonedDateTime eventDateTime;

    public TestDateTimeEvent(ZonedDateTime eventDateTime){
        this.eventDateTime = eventDateTime;
    }
}

