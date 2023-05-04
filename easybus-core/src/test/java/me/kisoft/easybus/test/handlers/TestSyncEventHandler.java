/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.kisoft.easybus.test.handlers;

import me.kisoft.easybus.Handler;
import me.kisoft.easybus.test.events.TestSyncEvent;

/**
 *
 * @author tareq
 */
public class TestSyncEventHandler implements Handler<TestSyncEvent> {

    @Override
    public void handle(TestSyncEvent event) {
        TestSyncEvent.checked = true;
    }
}
