/*
 * Copyright (C) 2015 Hamburg Sud and the contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.lodgvideon.hpalm.infrastructure;

import org.junit.Test;
import org.lodgvideon.hpalm.entity.Entity;
import org.lodgvideon.hpalm.entity.Field;
import org.lodgvideon.hpalm.entity.TestInstanceBuilder;
import org.lodgvideon.hpalm.testutil.DefaultHpAlmServlet;
import org.lodgvideon.hpalm.testutil.DefaultTimeEndpoint;
import org.lodgvideon.hpalm.testutil.MockingTestServer;

import java.util.Collections;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;

public class HpAlmSessionTest {

    @Test
    public void testTimeZoneCalculation() throws Exception {
        MockingTestServer server = new MockingTestServer();

        DefaultHpAlmServlet servlet = new DefaultHpAlmServlet();
        server.setHpAlmServlet(servlet);

        DefaultTimeEndpoint timeEndpoint = new DefaultTimeEndpoint();
        TimeZone tz = TimeZone.getTimeZone("GMT+9:30");
        timeEndpoint.setTimeZone(tz);
        servlet.setTimeEndpoint(timeEndpoint);

        server.startServer();

        String url = server.getBaseUrl();
        HpAlmSession session = HpAlmSession.create(url, "DEFAULT", "Test", "test1", "test1234");

        assertEquals(tz, session.determineServerTimeZone());

        server.stopServer();
    }

    @Test
    public void testQueryCollection() throws Exception {
        MockingTestServer server = new MockingTestServer();

        DefaultHpAlmServlet servlet = new DefaultHpAlmServlet();
        server.setHpAlmServlet(servlet);
        server.startServer();

        // register a test instance
        TestInstanceBuilder builder = new TestInstanceBuilder();

        Entity e = builder.setStatus("Passed").create();
        Field id = new Field();
        id.setName("id");
        id.getValue().add("123");
        e.getFields().getFieldList().add(id);
        servlet.setEntities("test-instance", Collections.singletonList(e));

        String url = server.getBaseUrl();

        HpAlmSession session = HpAlmSession.create(url, "DEFAULT", "Test", "test1", "test1234");


        EntityCollection ec = session.queryEntities("test-instance", "id[>0]");
        assertEquals(1, ec.getTotalCount());

        Entity eReturn = ec.iterator().next();
        assertEquals(e.getLongFieldValue("id"), eReturn.getLongFieldValue("id"));

        session.logout();
        server.stopServer();
    }

}
