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
package org.lodgvideon.hpalm.testutil;

import org.apache.commons.io.IOUtils;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public abstract class AbstractXmlEndpoint implements XmlEndpoint {

    protected final Document getXml(HttpServletRequest request) throws IOException, JDOMException {
        InputStream in = request.getInputStream();
        if (in == null) {
            return null;
        }

        try {
            InputStreamReader reader = new InputStreamReader(in, "UTF-8");
            return new SAXBuilder().build(reader);
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

}
