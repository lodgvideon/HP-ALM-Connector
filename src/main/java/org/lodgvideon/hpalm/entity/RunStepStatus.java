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

public enum RunStepStatus {

    BLOCKED("Blocked", "hp.qc.status.blocked"), FAILED("Failed", "hp.qc.status.failed"), N_A("N/A", "hp.qc.status.n-a"), NO_RUN(
            "No Run", "hp.qc.status.no-run"), NOT_COMPLETED("Not Completed", "hp.qc.status.not-completed"), PASSED("Passed",
            "hp.qc.status.passed");

    private String displayName;

    private String logicalName;

    private RunStepStatus(String displayName, String logicalName) {
        this.displayName = displayName;
        this.logicalName = logicalName;
    }

    public String displayName() {
        return displayName;
    }

    public String logicalName() {
        return logicalName;
    }

}
