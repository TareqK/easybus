/*
 * Copyright 2020 tareq.
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
package me.kisoft.easybus;

/**
 *
 * @author tareq
 */
public interface Bus {

    /**
     * Posts an object to the event bus. 
     * @param object the Object to post. Must have the annotation @Event to work correctly
     */
    void post(Object object);

    /**
     * Clears all handlers from the event bus
     */
    public void clear();

    /**
     * Adds a handler to the event bus
     * @param handler  the event bus handler to add
     */
    public void addHandler(EventHandler handler);

    /**
     * Removes a handler from the event bus
     * @param handler the event bus handler to remove
     */
    public void removeHandler(EventHandler handler);

}
