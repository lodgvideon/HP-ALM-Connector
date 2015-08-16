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


import java.util.Properties;

/**
 * Default implementation of the Configuration service for the HP ALM Connector.
 *
 * @author falbrech
 */
public class HpAlmConfigurationImpl implements HpAlmConfiguration {

    private Properties properties;


    public String getPropertiesBaseName() {
        return "hpalm";
    }


    public void configure(Properties properties) {
        this.properties = properties;
    }

    @Override
    public boolean isEnabled() {
        return Boolean.parseBoolean(
                properties.getProperty("enabled"));
    }

    @Override
    public String getHpAlmUrl() {
        return properties.getProperty("hpalmUrl");
    }

    @Override
    public String getUserName() {
        return properties.getProperty("userName");
    }

    @Override
    public String getPassword() {
        return properties.getProperty("password");
    }

    @Override
    public String getDomain() {
        return properties.getProperty("domain");
    }

    @Override
    public String getProject() {
        return properties.getProperty("project");
    }

    @Override
    public String getTestSetFolderPath() {
        return properties.getProperty("testSetFolderPath");
    }

    @Override
    public String getTestSetName() {
        return properties.getProperty("testSetName");
    }

    @Override
    public boolean isWriteDescriptionAndAttachments() {
        return Boolean.parseBoolean(properties.getProperty("writeDescriptionAndAttachments"));
    }

}
