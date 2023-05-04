package me.kisoft.easybus.test.events;

import me.kisoft.easybus.memory.AsyncHandler;

/**
 *
 * @author tareq
 */
@AsyncHandler
public class TestChildClassEvent extends TestParentClassEvent {

    public static boolean checked = false;
    public static boolean checkedSpecific = false;
}
