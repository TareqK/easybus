/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.kisoft.easybus.test.handlers;

import me.kisoft.easybus.Handle;
import me.kisoft.easybus.test.events.AsyncEvent;

/**
 *
 * @author tareq
 */
@Handle(event=AsyncEvent.class,async=true)
public class AsyncHandler {
    
    public void handle(AsyncEvent event){
        AsyncEvent.checked = true;
    }
}
