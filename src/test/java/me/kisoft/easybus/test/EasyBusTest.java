/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.kisoft.easybus.test;

import me.kisoft.easybus.EasyBus;
import me.kisoft.easybus.test.events.AsyncEvent;
import me.kisoft.easybus.test.events.SyncEvent;
import me.kisoft.easybus.test.handlers.SyncHandler;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author tareq
 */
public class EasyBusTest {

    EasyBus bus = new EasyBus();

    @Before
    public void clearBus() {
        bus.removeHandlers();
    }

    @Test
    public void packageScanningTest() {
        bus.search("me.kisoft.easybus.test.handlers");
        assertEquals(bus.getHandlers().size(), 2);
    }

    @Test
    public void classScanningTest() {
        bus.search(this.getClass());
        assertEquals(bus.getHandlers().size(), 2);
    }

    @Test
    public void syncEventTest() {
        bus.search("me.kisoft.easybus.test.handlers");
        SyncEvent.checked = false;
        bus.post(new SyncEvent());
        assertEquals(SyncEvent.checked,true);
    }

    @Test(timeout = 1000)
    public void asyncEventTest() throws InterruptedException {
        bus.search("me.kisoft.easybus.test.handlers");
        AsyncEvent.checked = false;
        bus.post(new AsyncEvent());
        while (AsyncEvent.checked == false) {
            Thread.sleep(20);
        }
        assertEquals(AsyncEvent.checked,true);
    }
    
}
