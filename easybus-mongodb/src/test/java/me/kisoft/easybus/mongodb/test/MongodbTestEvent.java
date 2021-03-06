package me.kisoft.easybus.mongodb.test;

import lombok.Data;
import me.kisoft.easybus.Event;
import org.apache.commons.lang3.RandomStringUtils;

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
/**
 *
 * @author tareq
 */
@Event
@Data
public class MongodbTestEvent {

    public static boolean checked = false;
    private String field1 = RandomStringUtils.randomAlphabetic(30);
    private String field2 = RandomStringUtils.randomAlphabetic(30);

}
