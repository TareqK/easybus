/*
 * Copyright 2021 tareq.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.kisoft.easybus.test.handlers;

import me.kisoft.easybus.Handle;
import me.kisoft.easybus.test.events.ParentClassEvent;

/**
 *
 * @author tareq
 */
@Handle(event = ParentClassEvent.class, async = false)
public class ParentClassHandler {

    public void handle(ParentClassEvent event) {
        ParentClassEvent.checked = true;
        ParentClassEvent.doChildCheck();
    }
}
