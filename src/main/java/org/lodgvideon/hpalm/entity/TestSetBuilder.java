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

public class TestSetBuilder extends AbstractEntityBuilder {

    public TestSetBuilder() {
        super("test-set");
        setValue("subtype-id", "hp.qc.test-set.default");
    }

    public TestSetBuilder setParentId(long parentId) {
        setValue("parent-id", DF_INTEGER.format(parentId));
        return this;
    }

    public TestSetBuilder setName(String name) {
        setValue("name", name);
        return this;
    }

    public TestSetBuilder setSubtypeId(String subtypeId) {
        setValue("subtype-id", subtypeId);
        return this;
    }
}
