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
@XmlType(name = "", propOrder = {"fieldList"})
@XmlRootElement(name = "Fields")
public class Fields {

    @XmlElement(name = "Field", required = true)
    protected List<Field> fieldList;

    public Fields(Fields fields) {
        fieldList = new ArrayList<Field>();

        if (fields.fieldList != null) {
            for (Field f : fields.fieldList) {
                fieldList.add(new Field(f));
            }
        }
    }

    public Fields() {
        fieldList = new ArrayList<Field>();
    }

    /**
     * Gets the value of the field property.
     * <p>
     * <p>
     * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object. This is why there is no set method for the fieldList property.
     * <p>
     * For example, to add a new item, do as follows:
     * <p>
     * getFieldList().add(newItem);
     * <p>
     * Objects of the following type(s) are allowed in the list {@link Field }
     */
    public List<Field> getFieldList() {
        if (fieldList == null) {
            fieldList = new ArrayList<Field>();
        }
        return this.fieldList;
    }

}