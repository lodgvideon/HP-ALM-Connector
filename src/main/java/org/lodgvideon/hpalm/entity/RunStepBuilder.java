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

public final class RunStepBuilder extends AbstractEntityBuilder {

    public RunStepBuilder() {
        super("run-step");
    }

    public RunStepBuilder setTestRunId(long testRunId) {
        setValue("parent-id", DF_INTEGER.format(testRunId));
        return this;
    }

    public RunStepBuilder setName(String name) {
        setValue("name", name);
        return this;
    }

    public RunStepBuilder setStatus(RunStepStatus status) {
        setValue("status", status.displayName());
        return this;
    }

    public RunStepBuilder setExecutionDateTime(Date dateTime) {
        setValue("execution-date", DF_DATE.format(dateTime));
        setValue("execution-time", DF_TIME.format(dateTime));
        return this;
    }

    public RunStepBuilder setDescription(String description) {
        setValue("description", description);
        return this;
    }

}
