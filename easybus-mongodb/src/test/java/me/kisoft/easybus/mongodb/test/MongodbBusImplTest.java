package me.kisoft.easybus.mongodb.test;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import me.kisoft.easybus.EasyBus;
import me.kisoft.easybus.mongodb.MongodbBusImpl;
import org.jongo.Jongo;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

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
public class MongodbBusImplTest {

    private EasyBus bus;
    private MongodbBusImpl busImpl;
    private static  MongoDBContainer mongoDBContainer;
    private static Jongo jongo;
    private static MongoClient client;

    @BeforeClass
    public static void preparePool() {
        mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:4.0.10"));
        mongoDBContainer.start();
        client = new MongoClient(new MongoClientURI(mongoDBContainer.getConnectionString()));
        jongo = new Jongo(client.getDB("events"));
    }

    @Before
    public void prepare() {

        jongo.getCollection(MongodbTestEvent.class.getCanonicalName()).drop();
        busImpl = new MongodbBusImpl(jongo);
        bus = new EasyBus(busImpl);
    }

    @Test(timeout = 10000)
    public void asyncEventTest() throws InterruptedException {
        bus.search("me.kisoft.easybus.mongodb.test");
        MongodbTestEvent.checked = false;
        bus.post(new MongodbTestEvent());
        while (MongodbTestEvent.checked == false) {
            Thread.sleep(20);
        }
        assertEquals(MongodbTestEvent.checked, true);
    }

    @AfterClass
    public static void teardownPool() throws InterruptedException {
        client.close();
    }
}
