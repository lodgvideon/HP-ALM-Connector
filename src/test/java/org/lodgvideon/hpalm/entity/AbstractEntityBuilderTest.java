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
package org.lodgvideon.hpalm.entity;

import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import static org.junit.Assert.*;

public class AbstractEntityBuilderTest {

    @Test
    public void testSetValue() {
        AbstractEntityBuilder myBuilder = new AbstractEntityBuilder("testentity") {
        };

        myBuilder.setValue("test", "test1");
        assertEquals("test1", myBuilder.create().getStringFieldValue("test"));

        myBuilder.setValue("test", "test2");
        assertEquals("test2", myBuilder.create().getStringFieldValue("test"));
        assertEquals(1, myBuilder.create().getFields().getFieldList().size());
        assertEquals(1, myBuilder.create().getFields().getFieldList().get(0).getValue().size());
    }

    @Test
    public void testCreateEffects() {
        AbstractEntityBuilder myBuilder = new AbstractEntityBuilder("testentity") {
        };

        myBuilder.setValue("test", "test1");
        // double creates should always have same values
        assertEquals("test1", myBuilder.create().getStringFieldValue("test"));
        assertEquals("test1", myBuilder.create().getStringFieldValue("test"));

        assertFalse(myBuilder.create() == myBuilder.create());
        assertEquals("testentity", myBuilder.create().getType());
    }

    @Test
    public void testFormatters() throws Exception {
        // test time zone appliance
        AbstractEntityBuilder.setTimeZone(TimeZone.getTimeZone("PST"));
        assertEquals("PST", AbstractEntityBuilder.DF_DATE.getTimeZone().getID());
        assertEquals("PST", AbstractEntityBuilder.DF_TIME.getTimeZone().getID());

        // test correct format
        assertTrue(AbstractEntityBuilder.DF_DATE.format(new Date()).matches("[0-9]{4}\\-[0-1][0-9]\\-[0-3][0-9]"));
        // 0-6 for second's first digit because of leap seconds (for all the nitpickers)
        assertTrue(AbstractEntityBuilder.DF_TIME.format(new Date()).matches("[0-2][0-9]:[0-5][0-9]:[0-6][0-9]"));

        // just to be sure that no AM/PM is used
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("CET"));
        Date night = sdf.parse("23:50:00");
        AbstractEntityBuilder.setTimeZone(sdf.getTimeZone());
        assertEquals("23:50:00", AbstractEntityBuilder.DF_TIME.format(night));
    }

}
