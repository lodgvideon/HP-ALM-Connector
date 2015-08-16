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
package org.lodgvideon.hpalm.infrastructure;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * A utility class for converting between jaxb annotated objects and xml.
 */
public class EntityMarshallingUtils {

    private EntityMarshallingUtils() {
    }

    /**
     * @param <T> the type we want to convert our xml into
     * @param c   the class of the parameterized type
     * @param xml the instance xml description
     * @return a deserialization of the xml into an object of type T of class Class<T>
     * @throws JAXBException
     */
    @SuppressWarnings("unchecked")
    public static <T> T marshal(Class<T> c, String xml) throws JAXBException {
        T res;

        if (c == xml.getClass()) {
            res = (T) xml;
        } else {
            JAXBContext ctx = JAXBContext.newInstance(c);
            Unmarshaller marshaller = ctx.createUnmarshaller();
            res = (T) marshaller.unmarshal(new StringReader(xml));
        }

        return res;
    }

    /**
     * @param <T> the type to serialize
     * @param c   the class of the type to serialize
     * @param o   the instance containing the data to serialize
     * @return a string representation of the data.
     * @throws JAXBException
     */
    public static <T> String unmarshal(Class<T> c, Object o) throws JAXBException {

        JAXBContext ctx = JAXBContext.newInstance(c);
        Marshaller marshaller = ctx.createMarshaller();
        StringWriter entityXml = new StringWriter();
        marshaller.marshal(o, entityXml);

        String entityString = entityXml.toString();

        return entityString;
    }

}
