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

import org.lodgvideon.hpalm.infrastructure.HpAlmUtil;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

public abstract class AbstractEntityBuilder {

    protected static final DecimalFormat DF_INTEGER = HpAlmUtil.DF_ID;

    protected static final DateFormat DF_DATE = new SimpleDateFormat("yyyy-MM-dd");

    protected static final DateFormat DF_TIME = new SimpleDateFormat("HH:mm:ss");

    static {
        setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    private Entity entity;

    private String entityTypeName;

    public AbstractEntityBuilder(String entityTypeName) {
        this.entityTypeName = entityTypeName;
        create();
    }

    public static void setTimeZone(TimeZone timeZone) {
        DF_DATE.setTimeZone(timeZone);
        DF_TIME.setTimeZone(timeZone);
    }

    public final Entity create() {
        Entity e = entity;
        entity = entity == null ? new Entity() : new Entity(entity);
        entity.setType(entityTypeName);
        if (entity.getFields() == null) {
            entity.setFields(new Fields());
        }
        return e;
    }

    protected final void setValue(String fieldName, String value) {
        List<Field> fieldList = entity.getFields().getFieldList();
        for (Field f : fieldList) {
            if (f.getName().equals(fieldName)) {
                f.getValue().clear();
                f.getValue().add(value);
                return;
            }
        }

        Field f = new Field();
        f.setName(fieldName);
        f.getValue().add(value);
        entity.getFields().getFieldList().add(f);
    }

}
