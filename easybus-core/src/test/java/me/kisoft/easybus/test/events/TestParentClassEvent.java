package me.kisoft.easybus.test.events;

import me.kisoft.easybus.memory.AsyncHandler;

/**
 *
 * @author tareq
 */
@AsyncHandler
public class TestParentClassEvent {

    public static boolean checked = false;

    public static void doChildCheck() {
        TestChildClassEvent.checked = true;
    }
}
