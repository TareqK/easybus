/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.kisoft.easybus.test.handlers;

import me.kisoft.easybus.Handler;
import me.kisoft.easybus.memory.AsyncHandler;
import me.kisoft.easybus.test.events.TestAsyncEvent;


/**
 *
 * @author tareq
 */
@AsyncHandler
public class TestAsyncEventHandler implements Handler<TestAsyncEvent> {
    
    @Override
    public void handle(TestAsyncEvent event){
        TestAsyncEvent.checked = true;
    }
}
