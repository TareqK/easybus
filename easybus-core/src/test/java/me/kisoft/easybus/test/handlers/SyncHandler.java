/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.kisoft.easybus.test.handlers;

import me.kisoft.easybus.Handle;
import me.kisoft.easybus.test.events.SyncEvent;

/**
 *
 * @author tareq
 */
@Handle(event=SyncEvent.class,async=false)
public class SyncHandler {
    
    public void handle(SyncEvent event){
        SyncEvent.checked = true;
    }
}