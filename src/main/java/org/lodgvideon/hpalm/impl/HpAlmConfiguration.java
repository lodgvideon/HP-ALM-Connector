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
package org.lodgvideon.hpalm.impl;


/**
 * Configuration service for the lodgvideon HP ALM Connector. Use the provided configuration properties to configure the
 * connector.
 *
 * @author falbrech
 */

public interface HpAlmConfiguration {

    String hpalmUrl = "";
    String userName = "";
    String password = "";
    String domain = "DEFAULT";
    String project = "";
    String testSetFolderPath = "";
    String testSetName = "";
    boolean writeDescriptionAndAttachments = true;
    String enabled = "";


    public boolean isEnabled();

    public String getHpAlmUrl();

    public String getUserName();

    public String getPassword();

    public String getDomain();

    public String getProject();

    public String getTestSetFolderPath();

    public String getTestSetName();

    public boolean isWriteDescriptionAndAttachments();

}
