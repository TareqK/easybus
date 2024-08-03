package me.kisoft.easybus.test;

import me.kisoft.easybus.EasyBus;
import me.kisoft.easybus.EasyBus.ActivationFailureException;
import me.kisoft.easybus.memory.MemoryBackingBusImpl;
import me.kisoft.easybus.test.events.*;
import me.kisoft.easybus.test.handlers.*;
import org.junit.Before;
import org.junit.Test;

import java.time.ZonedDateTime;

import static org.junit.Assert.assertEquals;

/**
 * @author tareq
 */
public class EasyBusTest {

    MemoryBackingBusImpl memBus = new MemoryBackingBusImpl();
    EasyBus bus = new EasyBus(memBus);

    @Before
    public void clearBus() {
        memBus = new MemoryBackingBusImpl();
        bus = new EasyBus(memBus);
        bus.clear();
    }

    @Test
    public void closeBusTestDoesntThrowException() throws Exception {
        bus.close();
    }

    @Test
    public void postNullEventShouldNotDoAnythingTest() {
        bus.post(null);
    }

    @Test(expected = ActivationFailureException.class)
    public void badActivatorShouldThrowExceptionTest() {
        bus.register(UnactivatableListener.class);
    }

    @Test
    public void addingTheSameHandlerTwiceIsANoOp() {
        bus.register(TestSyncEventListener.class);
        bus.register(TestSyncEventListener.class);
    }

    @Test
    public void addingMultipleHandlersForTheSameEventWorks() {
        bus.register(TestSyncEventListener.class);
        bus.register(TestSyncEventListener2.class);
    }

    @Test
    public void subClassEventTest() {
        TestChildClassEvent.checked = false;
        TestChildClassEvent.checkedSpecific = false;
        TestParentClassEvent.checked = false;
        bus.register(TestChildClassEventListener.class);
        bus.register(TestParentClassEventListener.class);
        bus.post(new TestChildClassEvent());
        assertEquals(TestChildClassEvent.checked, true);
        assertEquals(TestChildClassEvent.checkedSpecific, true);
        assertEquals(TestParentClassEvent.checked, true);
    }

    @Test
    public void specificityHandlerTest() {
        TestChildClassEvent.checked = false;
        TestChildClassEvent.checkedSpecific = false;
        TestParentClassEvent.checked = false;
        bus.register(TestChildClassEventListener.class);
        bus.register(TestParentClassEventListener.class);
        bus.post(new TestParentClassEvent());
        assertEquals(TestChildClassEvent.checked, true);
        assertEquals(TestChildClassEvent.checkedSpecific, false);
        assertEquals(TestParentClassEvent.checked, true);
    }

    @Test
    public void syncEventTest() {
        bus.register(TestSyncEventListener.class);
        TestSyncEvent.checked = false;
        bus.post(new TestSyncEvent());
        assertEquals(TestSyncEvent.checked, true);
    }

    @Test(timeout = 1000)
    public void asyncEventTest() throws InterruptedException {
        bus.register(TestAsyncEventListener.class);
        TestAsyncEvent.checked = false;
        bus.post(new TestAsyncEvent());
        while (TestAsyncEvent.checked == false) {
            Thread.sleep(20);
        }
        assertEquals(TestAsyncEvent.checked, true);
    }

    @Test
    public void testNotHandledEvent() {
        TestNotHandledEvent.checked = false;
        bus.post(new TestNotHandledEvent());
        assertEquals(TestNotHandledEvent.checked, false);
        TestNotHandledEvent.checked = true;
        bus.post(new TestNotHandledEvent());
        assertEquals(TestNotHandledEvent.checked, true);
    }

    @Test
    public void testDateTimeEventHandling() {
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime nowPlusDay = ZonedDateTime.now().plusDays(1);
        bus.register(TestDateTimeEventListener.class);

        bus.post(new TestDateTimeEvent(now));
        assertEquals(TestDateTimeEvent.eventDateTime, now);

        bus.post(new TestDateTimeEvent(nowPlusDay));
        assertEquals(TestDateTimeEvent.eventDateTime, nowPlusDay);
    }
}
