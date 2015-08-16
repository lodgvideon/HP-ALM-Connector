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

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"value"})
public class Field {

    @XmlElement(name = "Value", required = true)
    protected List<String> value;
    @XmlAttribute(name = "Name", required = true)
    protected String name;

    public Field() {
    }

    public Field(Field field) {
        name = field.name;
        value = field.value == null ? null : new ArrayList<String>(field.value);
    }

    /**
     * Gets the value of the value property.
     * <p>
     * This accessor method returns a reference to the live list, not a snapshot. Therefore, any modification you make to
     * the returned list will be present inside the JAXB object. This is why there is no set method for the value
     * property.
     * <p>
     * For example, to add a new item, do as follows:
     * <p>
     * getValue().add(newItem);
     * <p>
     * <p>
     * Objects of the following type(s) are allowed in the list {@link String }
     */
    public List<String> getValue() {
        if (value == null) {
            value = new ArrayList<String>();
        }
        return this.value;
    }

    /**
     * Gets the value of the name property.
     *
     * @return possible object is {@link String }
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     *
     * @param value allowed object is {@link String }
     */
    public void setName(String value) {
        this.name = value;
    }

}