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

import java.util.Date;

public final class TestRunBuilder extends AbstractEntityBuilder {

    public TestRunBuilder() {
        super("run");
        // HP does not seem to offer a subtype for AUTOMATED... strange.
        setSubtypeId("hp.qc.run.MANUAL");
    }

    public TestRunBuilder setName(String name) {
        setValue("name", name);
        return this;
    }

    public TestRunBuilder setTestInstanceId(long testInstanceId) {
        setValue("testcycl-id", DF_INTEGER.format(testInstanceId));
        setValue("test-instance", DF_INTEGER.format(testInstanceId));
        return this;
    }

    public TestRunBuilder setTestSetId(long testSetId) {
        setValue("cycle-id", DF_INTEGER.format(testSetId));
        return this;
    }

    public TestRunBuilder setTestId(long testId) {
        setValue("test-id", DF_INTEGER.format(testId));
        return this;
    }

    public TestRunBuilder setSubtypeId(String subtypeId) {
        setValue("subtype-id", subtypeId);
        return this;
    }

    public TestRunBuilder setStatus(String status) {
        setValue("status", status);
        return this;
    }

    public TestRunBuilder setOwner(String owner) {
        setValue("owner", owner);
        return this;
    }

    public TestRunBuilder setHost(String host) {
        setValue("host", host);
        return this;
    }

    public TestRunBuilder setComments(String comments) {
        setValue("comments", comments);
        return this;
    }

    public TestRunBuilder setDuration(long duration) {
        setValue("duration", DF_INTEGER.format(duration));
        return this;
    }

    public TestRunBuilder setExecutionDateAndTime(Date executionDateAndTime) {
        setValue("execution-date", DF_DATE.format(executionDateAndTime));
        setValue("execution-time", DF_TIME.format(executionDateAndTime));
        return this;
    }

    public TestRunBuilder setOSInfo(String osName, String osBuildNumber, String osServicePack) {
        setValue("os-name", osName);
        setValue("os-build", osBuildNumber);
        setValue("os-sp", osServicePack);
        return this;
    }

}
