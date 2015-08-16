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

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.codehaus.plexus.util.xml.PrettyPrintXMLWriter;
import org.codehaus.plexus.util.xml.XMLWriter;
import org.lodgvideon.hpalm.entity.Entity;
import org.lodgvideon.hpalm.entity.EntityResultSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBException;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HpAlmSession {

    private static final Logger LOG = LoggerFactory.getLogger(HpAlmSession.class);
    private static final String LWSSO_COOKIE_KEY = "LWSSO_COOKIE_KEY";
    private static final String QCESSION_COOKIE_KEY = "QCSession";
    private static final Map<String, String> JSON_ACCEPT_HEADER = new HashMap<String, String>();
    private static final Map<String, String> XML_ACCEPT_HEADER = new HashMap<String, String>();
    private static final Map<String, String> XML_POST_HEADERS = new HashMap<String, String>();
    private static final Map<String, String> FILE_POST_HEADERS = new HashMap<String, String>();
    private static final Pattern PATTERN_LWSSO_REALM = Pattern.compile("LWSSO realm=\"(http(s?)://[^\"]+)\"");

    static {
        JSON_ACCEPT_HEADER.put("Accept", "application/json");
        XML_ACCEPT_HEADER.put("Accept", "application/xml");

        XML_POST_HEADERS.putAll(XML_ACCEPT_HEADER);
        XML_POST_HEADERS.put("Content-Type", "application/xml");

        FILE_POST_HEADERS.putAll(XML_ACCEPT_HEADER);
        FILE_POST_HEADERS.put("Content-Type", "application/octet-stream");
    }

    private RestConnector connector;

    private HpAlmSession(RestConnector connector) {
        this.connector = connector;
    }

    public static HpAlmSession create(String serverUrl, String domain, String project, String userName, String password)
            throws IOException, HpAlmException {
        RestConnector connector = new RestConnector(new HashMap<String, String>(), serverUrl, domain, project);

        // query is-authenticated
        Response response = connector.httpGet(connector.buildUrl("rest/is-authenticated"), null, XML_ACCEPT_HEADER);

        if (response.getStatusCode() != HttpStatus.SC_UNAUTHORIZED) {
            throw new HpAlmException("Unexpected HTTP status code during authentication: " + response.getStatusCode());
        }

        String authInfo = getSingleHeaderValue(response, "WWW-Authenticate");
        if (authInfo == null) {
            throw new HpAlmException("Server did not return authentication method information");
        }
        authInfo = authInfo.trim();
        Matcher m = PATTERN_LWSSO_REALM.matcher(authInfo);
        if (!m.matches()) {
            throw new HpAlmException("Server returned unsupported authentication realm: " + authInfo);
        }

        String authUrl = m.group(1) + "/alm-authenticate";

        // build authentication XML
        StringWriter sw = new StringWriter();
        XMLWriter writer = new PrettyPrintXMLWriter(sw);
        writer.startElement("alm-authentication");
        writer.startElement("user");
        writer.writeText(userName);
        writer.endElement();
        writer.startElement("password");
        writer.writeText(password);
        writer.endElement();
        writer.endElement();

        // send it via POST
        response = connector.httpPost(authUrl, sw.toString().getBytes("UTF-8"), XML_POST_HEADERS);
        if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
            throw new HpAlmException("Invalid user name or password");
        }

        // extract Cookie
        String cookie = getCookie(response, LWSSO_COOKIE_KEY);
        if (cookie == null) {
            throw new HpAlmException("Login response did not contain required session cookie");
        }

        connector.getCookies().put(LWSSO_COOKIE_KEY, cookie);

        // request a session with a custom limited timeout
        sw = new StringWriter();
        writer = new PrettyPrintXMLWriter(sw);
        writer.startElement("session-parameters");
        writer.startElement("time-out");
        writer.writeText("5");
        writer.endElement();
        writer.endElement();

        response = connector.httpPost(connector.buildUrl("rest/site-session"), sw.toString().getBytes("UTF-8"), XML_POST_HEADERS);
        if (response.getStatusCode() != HttpStatus.SC_OK) {
            throw new HpAlmException("Could not start HP ALM Session for user " + userName);
        }

        cookie = getCookie(response, QCESSION_COOKIE_KEY);
        if (cookie == null) {
            throw new HpAlmException("Server did not send a session cookie");
        }

        connector.getCookies().put(QCESSION_COOKIE_KEY, cookie);

        return new HpAlmSession(connector);
    }

    private static String getSingleHeaderValue(Response response, String header) {
        Map<String, ? extends Iterable<String>> headers = response.getResponseHeaders();
        if (headers != null) {
            Iterable<String> values = headers.get(header);
            if (values != null) {
                Iterator<String> iter = values.iterator();
                if (iter.hasNext()) {
                    return iter.next();
                }
            }
        }

        return null;
    }

    private static String getCookie(Response response, String key) {
        Map<String, ? extends Iterable<String>> headers = response.getResponseHeaders();
        if (headers != null) {
            Iterable<String> values = headers.get("Set-Cookie");
            if (values != null) {
                for (String cookie : values) {
                    cookie = cookie.trim();
                    if (cookie.contains(";")) {
                        cookie = cookie.substring(0, cookie.indexOf(';')).trim();
                    }
                    if (cookie.contains("=")) {
                        String k = cookie.substring(0, cookie.indexOf('=')).trim();
                        if (key.equals(k)) {
                            return cookie.substring(cookie.indexOf('=') + 1).trim();
                        }
                    }
                }
            }
        }

        return null;
    }

    private static String encodeQuery(String query) {
        return new URLEncoder().encode(query);
    }

    public void extendTimeout() throws IOException {
        connector.httpGet(connector.buildUrl("rest/site-session"), null, XML_ACCEPT_HEADER);
    }

    public void logout() throws IOException {
        connector.httpGet(connector.buildUrl("authentication-point/logout"), null, XML_ACCEPT_HEADER);
    }

    // TODO only for testing and debugging
    public String getFieldsXml(String entityType, boolean required) throws IOException {
        Response response = connector.httpGet(
                connector.buildUrl("rest/domains/DEFAULT/projects/Training_and_Test/customization/entities/" + entityType
                        + "/fields"), "required=" + required, XML_ACCEPT_HEADER);

        return new String(response.getResponseData(), "UTF-8");
    }

    public ServerTime getServerTime() throws IOException, HpAlmException {
        Response response = connector.httpGet(connector.buildUrl("rest/server/time"), null, XML_ACCEPT_HEADER);
        if (response.getStatusCode() != HttpStatus.SC_OK) {
            raiseHpAlmException(response);
        }

        String xml = new String(response.getResponseData(), "UTF-8");
        try {
            return EntityMarshallingUtils.marshal(ServerTime.class, xml);
        } catch (JAXBException e) {
            throw new IOException("Could not unmarshal Server Time", e);
        }
    }

    public TimeZone determineServerTimeZone() throws IOException, HpAlmException {
        // get time values from server
        ServerTime serverTime = getServerTime();

        long millis = Long.parseLong(serverTime.getTimeInMillis());

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));

        long compare;
        try {
            compare = sdf.parse(serverTime.getDateTime()).getTime();
        } catch (ParseException pe) {
            throw new HpAlmException("HP ALM returned unparseable datetime string: " + serverTime.getDateTime());
        }
        long diff = compare - millis;
        int diffHours = (int) Math.round(diff / (1000l * 60 * 60.0));
        int diffMinutes = (int) Math.abs(Math.round((diff - (diffHours * 1000l * 60 * 60)) / (1000l * 60.0)));

        String determinedTimeZone = "" + diffMinutes;
        if (determinedTimeZone.length() == 1) {
            determinedTimeZone = "0" + determinedTimeZone;
        }
        determinedTimeZone = "GMT" + (diffHours < 0 ? "" : "+") + diffHours + ":" + determinedTimeZone;
        return TimeZone.getTimeZone(determinedTimeZone);
    }

    public Entity createEntity(Entity entity) throws IOException, HpAlmException {
        LOG.debug("Creating org.lodggvideon.hpalm.entity of type " + entity.getType() + " with name " + entity.getStringFieldValue("name"));
        try {
            String xml = EntityMarshallingUtils.unmarshal(Entity.class, entity);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Full creation XML: " + xml);
            }
            Response response = connector.httpPost(connector.buildEntityCollectionUrl(entity.getType()), xml.getBytes("UTF-8"),
                    XML_POST_HEADERS);
            if (response.getStatusCode() != HttpStatus.SC_CREATED) {
                raiseHpAlmException(response);
            }
            xml = new String(response.getResponseData(), "UTF-8");
            Entity result = EntityMarshallingUtils.marshal(Entity.class, xml);
            LOG.debug("Created org.lodggvideon.hpalm.entity got ID " + result.getId());
            return result;
        } catch (JAXBException e) {
            throw new IOException("Could not unmarshal Entity", e);
        }
    }

    public Entity updateEntity(long id, Entity updateValues) throws IOException, HpAlmException {
        LOG.debug("Updating org.lodggvideon.hpalm.entity of type " + updateValues.getType() + " with ID " + id);
        try {
            String xml = EntityMarshallingUtils.unmarshal(Entity.class, updateValues);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Full update XML: " + xml);
            }
            Response response = connector.httpPut(connector.buildEntityCollectionUrl(updateValues.getType()) + "/" + id,
                    xml.getBytes("UTF-8"), XML_POST_HEADERS);
            if (response.getStatusCode() != HttpStatus.SC_OK) {
                raiseHpAlmException(response);
            }
            xml = new String(response.getResponseData(), "UTF-8");
            return EntityMarshallingUtils.marshal(Entity.class, xml);
        } catch (JAXBException e) {
            throw new IOException("Could not unmarshal Entity", e);
        }
    }

    public void deleteEntity(String entityType, long id) throws IOException, HpAlmException {
        Response response = connector.httpDelete(connector.buildEntityCollectionUrl(entityType) + "/" + id,
                XML_ACCEPT_HEADER);
        if (response.getStatusCode() != HttpStatus.SC_OK) {
            raiseHpAlmException(response);
        }
    }

    public void deleteEntity(Entity entity) throws IOException, HpAlmException {
        deleteEntity(entity.getType(), entity.getId());
    }

    public Entity createAttachment(Entity entity, String fileName, InputStream attachmentData) throws IOException, HpAlmException {
        Map<String, String> headerMap = new LinkedHashMap<String, String>(FILE_POST_HEADERS);
        headerMap.put("Slug", fileName);

        // cache data
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        IOUtils.copy(attachmentData, baos);
        byte[] data = baos.toByteArray();

        String url = connector.buildEntityCollectionUrl(entity.getType());
        url += "/" + entity.getId() + "/attachments";

        Response response = connector.httpPost(url, data, headerMap);

        if (response.getStatusCode() != HttpStatus.SC_CREATED) {
            raiseHpAlmException(response);
        }

        String xml = new String(response.getResponseData(), "UTF-8");
        try {
            return EntityMarshallingUtils.marshal(Entity.class, xml);
        } catch (JAXBException e) {
            throw new HpAlmException("Invalid XML received after attachment creation", e);
        }
    }

    public EntityCollection getTestRuns() throws IOException, HpAlmException {
        return getEntityCollection("run", "test runs");
    }

    public EntityCollection getTests() throws IOException, HpAlmException {
        return getEntityCollection("test", "tests");
    }

    public EntityCollection getTestSets() throws IOException, HpAlmException {
        return getEntityCollection("test-set", "test sets");
    }

    public EntityCollection getTestFolders() throws IOException, HpAlmException {
        return getEntityCollection("test-folder", "test folders");
    }

    public EntityCollection getTestSetFolders() throws IOException, HpAlmException {
        return getEntityCollection("test-set-folder", "test set folders");
    }

    public Entity getTest(long id) throws IOException, HpAlmException {
        return getEntity("test", id);
    }

    public Entity getTestSet(long id) throws IOException, HpAlmException {
        return getEntity("test-set", id);
    }

    public Entity getTestFolder(long id) throws IOException, HpAlmException {
        return getEntity("test-folder", id);
    }

    public Entity getTestSetFolder(long id) throws IOException, HpAlmException {
        return getEntity("test-set-folder", id);
    }

    //
    // Private helpers
    //

    public EntityCollection queryEntities(String entityName, String query) throws IOException, HpAlmException {
        if (query != null) {
            query = encodeQuery(query);
        }

        String url = connector.buildEntityCollectionUrl(entityName) + (query != null ? ("?query={" + query + "}") : "");
        return new PagedEntityCollectionImpl(this, url, doGet(url));
    }

    public EntityCollection getAssetRelations(Entity entity) throws IOException, HpAlmException {
        String url = connector.buildEntityCollectionUrl(entity.getType()) + "/" + HpAlmUtil.DF_ID.format(entity.getId())
                + "/asset-relations";
        return new PagedEntityCollectionImpl(this, url, doGet(url));
    }

    EntityResultSet doGet(String url) throws IOException, HpAlmException {
        Response response = connector.httpGet(url, null, XML_ACCEPT_HEADER);
        if (response.getStatusCode() != HttpStatus.SC_OK) {
            raiseHpAlmException(response);
        }

        try {
            // unmarshal response
            String xml = new String(response.getResponseData(), "UTF-8");
            return EntityMarshallingUtils.marshal(EntityResultSet.class, xml);
        } catch (JAXBException e) {
            throw new HpAlmException("Invalid XML format returned by HP ALM", e);
        }
    }

    private void raiseHpAlmException(Response response) throws HpAlmException {
        try {
            if (response.getResponseData() != null && response.getResponseData().length > 0) {
                // unmarshal response
                String xml = new String(response.getResponseData(), "UTF-8");
                QCRestException qcex = EntityMarshallingUtils.marshal(QCRestException.class, xml);

                if (qcex.getTitle() != null && qcex.getId() != null) {
                    throw new HpAlmException(qcex.getTitle(), qcex.getId());
                }
                if (qcex.getTitle() != null) {
                    throw new HpAlmException(qcex.getTitle());
                }
                if (qcex.getId() != null) {
                    throw new HpAlmException(qcex.getId());
                }
            }
        } catch (JAXBException e) { // NOPMD
            // ignore; fallthrough to general exception
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("No UTF-8 available here. Cannot continue.");
        }
        throw new HpAlmException("Unexpected HP ALM status code, no further information: " + response.getStatusCode());
    }

    public Entity getEntity(String entityName, long id) throws IOException, HpAlmException {
        Response response = connector.httpGet(connector.buildEntityCollectionUrl(entityName) + "/" + HpAlmUtil.DF_ID.format(id),
                null, XML_ACCEPT_HEADER);
        if (response.getStatusCode() != HttpStatus.SC_OK) {
            raiseHpAlmException(response);
        }

        try {
            // unmarshal response
            String xml = new String(response.getResponseData(), "UTF-8");
            return EntityMarshallingUtils.marshal(Entity.class, xml);
        } catch (JAXBException e) {
            throw new HpAlmException("Invalid XML format in " + entityName, e);
        }
    }

    private EntityCollection getEntityCollection(String entityName, String entitiesDisplayName) throws IOException,
            HpAlmException {
        String url = connector.buildEntityCollectionUrl(entityName);
        EntityResultSet resultSet = doGet(url);
        return new PagedEntityCollectionImpl(this, url, resultSet);
    }

}
