package me.kisoft.easybus.test.events;

/**
 *
 * @author tareq
 */
public class TestParentClassEvent {

    public static boolean checked = false;

    public static void doChildCheck() {
        TestChildClassEvent.checked = true;
    }
}
