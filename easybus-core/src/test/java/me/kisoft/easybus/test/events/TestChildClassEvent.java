package me.kisoft.easybus.test.events;

import me.kisoft.easybus.memory.HandleAsync;

/**
 *
 * @author tareq
 */
@HandleAsync
public class TestChildClassEvent extends TestParentClassEvent {

    public static boolean checked = false;
    public static boolean checkedSpecific = false;
}
