package me.kisoft.easybus.test.events;

import me.kisoft.easybus.memory.HandleAsync;

/**
 *
 * @author tareq
 */
@HandleAsync
public class TestParentClassEvent {

    public static boolean checked = false;

    public static void doChildCheck() {
        TestChildClassEvent.checked = true;
    }
}
