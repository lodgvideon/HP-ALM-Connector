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

import org.eclipse.jetty.http.HttpException;
import org.lodgvideon.hpalm.infrastructure.HpAlmUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DefaultTimeEndpoint extends AbstractXmlEndpoint {

    private TimeZone timeZone = TimeZone.getDefault();

    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    @Override
    public void handle(HttpServletRequest request, ResponseXmlBuilder builder) throws IOException, ServletException,
            HttpException {
        Date now = new Date();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(timeZone);

        builder.startElement("ServerTime");
        builder.startElement("TimeInMillis").writeText(HpAlmUtil.DF_ID.format(now.getTime())).endElement();
        builder.startElement("DateTime").writeText(sdf.format(now)).endElement();
        builder.endElement();
    }

}
