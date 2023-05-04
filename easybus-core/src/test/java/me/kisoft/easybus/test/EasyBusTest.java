package me.kisoft.easybus.test;

import me.kisoft.easybus.EasyBus;
import me.kisoft.easybus.memory.MemoryBackingBusImpl;
import me.kisoft.easybus.test.events.TestChildClassEvent;
import me.kisoft.easybus.test.events.TestParentClassEvent;
import me.kisoft.easybus.test.events.TestSyncEvent;
import me.kisoft.easybus.test.events.TestAsyncEvent;
import me.kisoft.easybus.test.events.TestNotHandledEvent;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

/**
 *
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
    public void packageScanningTest() {
        bus.search("me.kisoft.easybus.test.handlers");
        assertEquals(memBus.getHandlers().size(), 4);
    }

    @Test
    public void classScanningTest() {
        bus.search(this.getClass());
        assertEquals(memBus.getHandlers().size(), 4);
    }

    @Test
    public void subClassEventTest() {
        TestChildClassEvent.checked = false;
        TestChildClassEvent.checkedSpecific = false;
        TestParentClassEvent.checked = false;
        bus.search("me.kisoft.easybus.test.handlers");
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
        bus.search("me.kisoft.easybus.test.handlers");
        bus.post(new TestParentClassEvent());
        assertEquals(TestChildClassEvent.checked, true);
        assertEquals(TestChildClassEvent.checkedSpecific, false);
        assertEquals(TestParentClassEvent.checked, true);
    }

    @Test
    public void syncEventTest() {
        bus.search("me.kisoft.easybus.test.handlers");
        TestSyncEvent.checked = false;
        bus.post(new TestSyncEvent());
        assertEquals(TestSyncEvent.checked, true);
    }

    @Test(timeout = 1000)
    public void asyncEventTest() throws InterruptedException {
        bus.search("me.kisoft.easybus.test.handlers");
        TestAsyncEvent.checked = false;
        bus.post(new TestAsyncEvent());
        while (TestAsyncEvent.checked == false) {
            Thread.sleep(20);
        }
        assertEquals(TestAsyncEvent.checked, true);
    }

    @Test
    public void testNotHandledEvent() {
        bus.search("me.kisoft.easybus.test.handlers");
        TestNotHandledEvent.checked = false;
        bus.post(new TestNotHandledEvent());
        assertEquals(TestNotHandledEvent.checked, false);
        TestNotHandledEvent.checked = true;
        bus.post(new TestNotHandledEvent());
        assertEquals(TestNotHandledEvent.checked, true);
    }

}
