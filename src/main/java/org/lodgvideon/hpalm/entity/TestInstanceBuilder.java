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

public class TestInstanceBuilder extends AbstractEntityBuilder {

    public TestInstanceBuilder() {
        super("test-instance");
        setValue("subtype-id", "hp.qc.test-instance.MANUAL");
    }

    public TestInstanceBuilder setTestSetId(long testSetId) {
        setValue("cycle-id", DF_INTEGER.format(testSetId));
        return this;
    }

    public TestInstanceBuilder setTestId(long testId) {
        setValue("test-id", DF_INTEGER.format(testId));
        return this;
    }

    public TestInstanceBuilder setTestConfigId(long testConfigId) {
        setValue("test-config-id", DF_INTEGER.format(testConfigId));
        return this;
    }

    public TestInstanceBuilder setOrderNumber(long orderNumber) {
        setValue("test-order", DF_INTEGER.format(orderNumber));
        return this;
    }

    public TestInstanceBuilder setStatus(String status) {
        setValue("status", status);
        return this;
    }

    public TestInstanceBuilder setExecDateTimeFromEntity(Entity entity) {
        setValue("exec-date", entity.getStringFieldValue("exec-date"));
        setValue("exec-time", entity.getStringFieldValue("exec-time"));
        return this;
    }
}
