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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class EntityTest {

    @Test
    public void testConstructor() {
        Entity e = new Entity();
        assertNull(e.getType());
        assertNull(e.getFields());
        assertEquals(0, e.getId());
    }

    @Test
    public void testCopyConstructor() {
        Entity e1 = new Entity();
        Fields fields = new Fields();
        Field f = new Field();
        f.setName("test");
        f.getValue().add("test1");
        fields.getFieldList().add(f);
        e1.setFields(fields);

        Entity e2 = new Entity(e1);
        assertEquals("test1", e1.getStringFieldValue("test"));
        assertEquals("test1", e2.getStringFieldValue("test"));

        // direct modification of field
        f.getValue().clear();
        f.getValue().add("test2");
        assertEquals("test2", e1.getStringFieldValue("test"));
        assertEquals("test1", e2.getStringFieldValue("test"));
    }

    @Test
    public void testGetStringFieldValue() {
        Entity e1 = new Entity();
        Fields fields = new Fields();
        Field f = new Field();
        f.setName("test");
        f.getValue().add("test1");
        f.getValue().add("test2");
        fields.getFieldList().add(f);
        e1.setFields(fields);

        assertEquals("test1", e1.getStringFieldValue("test"));
    }

    @Test
    public void testGetLongFieldValue() {
        Entity e1 = new Entity();
        Fields fields = new Fields();
        Field f = new Field();
        f.setName("test");
        f.getValue().add("1");
        f.getValue().add("2");
        fields.getFieldList().add(f);
        e1.setFields(fields);

        assertEquals(1, e1.getLongFieldValue("test"));
    }

    @Test
    public void testGetId() {
        Entity e1 = new Entity();
        Fields fields = new Fields();
        Field f = new Field();
        f.setName("id");
        f.getValue().add("23");
        fields.getFieldList().add(f);
        e1.setFields(fields);

        assertEquals(23, e1.getId());
    }

}
