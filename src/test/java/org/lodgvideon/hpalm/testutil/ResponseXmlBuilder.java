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

import org.codehaus.plexus.util.xml.PrettyPrintXMLWriter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ResponseXmlBuilder {

    private StringWriter out = new StringWriter();

    private PrettyPrintXMLWriter writer = new PrettyPrintXMLWriter(out, "UTF-8", null);

    private Map<String, String> headers = new LinkedHashMap<String, String>();

    private int statusCode = HttpServletResponse.SC_OK;

    private List<Cookie> cookies = new ArrayList<Cookie>();

    public ResponseXmlBuilder setHeader(String name, String value) {
        headers.put(name, value);
        return this;
    }

    public ResponseXmlBuilder setStatusCode(int statusCode) {
        this.statusCode = statusCode;
        return this;
    }

    public ResponseXmlBuilder addCookie(Cookie cookie) {
        cookies.add(cookie);
        return this;
    }

    public ResponseXmlBuilder startElement(String element) {
        writer.startElement(element);
        return this;
    }

    public ResponseXmlBuilder endElement() {
        writer.endElement();
        return this;
    }

    public ResponseXmlBuilder addAttribute(String name, String value) {
        writer.addAttribute(name, value);
        return this;
    }

    public ResponseXmlBuilder writeText(String text) {
        writer.writeText(text);
        return this;
    }

    public ResponseXmlBuilder writeMarkup(String markup) {
        writer.writeMarkup(markup);
        return this;
    }

    public void applyTo(HttpServletResponse response) throws IOException {
        // set headers
        for (Map.Entry<String, String> header : headers.entrySet()) {
            response.addHeader(header.getKey(), header.getValue());
        }

        // set status code
        response.setStatus(statusCode);

        // add cookies
        for (Cookie cookie : cookies) {
            response.addCookie(cookie);
        }

        // write data, if any
        String data = out.toString();
        if (data.length() > 0) {
            // add content-type and length, and write data
            response.addHeader("Content-Type", "application/xml");
            byte[] bytes = data.getBytes("UTF-8");
            OutputStream os = response.getOutputStream();
            os.write(bytes);
            os.close();
        }
    }

    public String getXml() {
        out.flush();
        return out.toString();
    }


}
