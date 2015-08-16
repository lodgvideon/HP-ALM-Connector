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
import org.lodgvideon.hpalm.entity.Entity;
import org.lodgvideon.hpalm.entity.EntityResultSet;
import org.lodgvideon.hpalm.entity.Field;
import org.lodgvideon.hpalm.infrastructure.EntityMarshallingUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DefaultHpAlmServlet extends HttpServlet {

    private static final long serialVersionUID = 3959682188622361279L;

    private static final Pattern PATTERN_COLLECTION = Pattern.compile("/rest/domains/([^/]+)/projects/([^/]+)/(.*)s");

    private static final Pattern PATTERN_FILTER = Pattern.compile("([^\\[]+)\\[([^\\]]+)\\]");

    private Map<String, List<Entity>> entities = new HashMap<String, List<Entity>>();

    private DefaultAuthenticationEndpoint authenticationEndpoint = new DefaultAuthenticationEndpoint();

    private DefaultTimeEndpoint timeEndpoint = new DefaultTimeEndpoint();

    public DefaultHpAlmServlet() {
        // default applications
    }

    public void setAuthenticationEndpoint(DefaultAuthenticationEndpoint authenticationEndpoint) {
        this.authenticationEndpoint = authenticationEndpoint;
    }

    public void setTimeEndpoint(DefaultTimeEndpoint timeEndpoint) {
        this.timeEndpoint = timeEndpoint;
    }

    public void setEntities(String typeName, List<Entity> entities) {
        this.entities.put(typeName, new ArrayList<Entity>(entities));
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();

        // authentication and session endpoints
        if (path.matches("/rest/is\\-authenticated|/authentication\\-point/.*|/rest/site\\-session")) {
            handleWithEndpoint(req, resp, authenticationEndpoint);
            return;
        }

        if (path.equals("/rest/server/time")) {
            handleWithEndpoint(req, resp, timeEndpoint);
            return;
        }

        // collections
        Matcher matcher = PATTERN_COLLECTION.matcher(path);
        if (matcher.matches()) {
            handleCollection(req, resp, matcher.group(1), matcher.group(2), matcher.group(3));
            return;
        }

        super.service(req, resp);
    }

    protected void handleCollection(HttpServletRequest request, HttpServletResponse response, String domain, String project,
                                    String typeName) throws ServletException, IOException {
        // extract potential parameters from query
        String query = request.getParameter("query");
        int pageSize;
        try {
            pageSize = Integer.parseInt(request.getParameter("page-size"));
        } catch (Exception e) {
            pageSize = 100;
        }
        int startIndex;
        try {
            startIndex = Integer.parseInt(request.getParameter("start-index")) - 1;
        } catch (Exception e) {
            startIndex = 0;
        }

        List<Entity> allEntities = new ArrayList<Entity>(getAllEntities(typeName));

        // filter according to query
        if (query != null) {
            query = query.trim();
            if (query.startsWith("{") && query.endsWith("}")) {
                query = query.substring(1, query.length() - 1);
                String[] filters = query.split(";");
                for (String filter : filters) {
                    filter = filter.trim();
                    Matcher m = PATTERN_FILTER.matcher(filter);
                    if (m.matches()) {
                        filterEntities(allEntities, m.group(1).trim(), m.group(2).trim());
                    }
                }
            }
        }
        int totalResults = allEntities.size();

        // scroll to start index
        while (startIndex > 0) {
            allEntities.remove(0);
        }
        // reduce to page size
        while (allEntities.size() > pageSize) {
            allEntities.remove(allEntities.size() - 1);
        }

        // build EntityResultSet
        EntityResultSet resultSet = new EntityResultSet(allEntities);
        resultSet.setTotalResults(totalResults);

        // render to XML
        try {
            String xml = EntityMarshallingUtils.unmarshal(EntityResultSet.class, resultSet);
            // remove XML header
            xml = xml.replaceAll("<\\?[^\\?]+\\?>", "");

            // write to response
            ResponseXmlBuilder builder = new ResponseXmlBuilder();
            builder.writeMarkup(xml);
            builder.applyTo(response);
        } catch (JAXBException e) {
            throw new HttpException(500);
        }
    }

    protected void filterEntities(List<Entity> allEntities, String fieldName, String criteria) {
        Iterator<Entity> iter = allEntities.iterator();
        while (iter.hasNext()) {
            Entity e = iter.next();
            boolean found = false;
            for (Field f : e.getFields().getFieldList()) {
                if (f.getName().equals(fieldName)) {
                    found = matchesCriteria(f, criteria.trim());
                    break;
                }
            }
            if (!found) {
                iter.remove();
            }
        }
    }

    /**
     * Simple implementation only supporting criteria of the form:
     * <ul>
     * <li>&gt;</li>
     * <li>'literal', "literal", literal</li>
     * <li>Literals with one wildcard at the end.</li>
     * </ul>
     * If any criterion is found which cannot be analyzed, <code>false</code> is returned for every field.<br>
     * Subclasses can override to implement more sophisticated filtering.
     *
     * @param f        Field to filter.
     * @param criteria Criteria to apply to the field.
     * @return <code>true</code> if the field value matches the criteria, <code>false</code> otherwise.
     */
    protected boolean matchesCriteria(Field f, String criteria) {
        if (f.getValue() == null || f.getValue().isEmpty()) {
            return false;
        }
        String fieldValue = f.getValue().get(0);

        if (criteria.startsWith(">")) {
            try {
                double value = Double.parseDouble(criteria.substring(1).trim());
                double fv = Double.parseDouble(fieldValue);
                return fv > value;
            } catch (Exception e) {
                return false;
            }
        }

        if ((criteria.startsWith("'") && criteria.endsWith("'")) || (criteria.startsWith("\"") && criteria.endsWith("\""))) {
            criteria = criteria.substring(1, criteria.length() - 1);
        }

        if (criteria.endsWith("*")) {
            return fieldValue.startsWith(criteria.substring(0, criteria.length() - 1));
        }

        return fieldValue.equals(criteria);
    }

    protected List<Entity> getAllEntities(String typeName) {
        return entities.containsKey(typeName) ? entities.get(typeName) : Collections.<Entity>emptyList();
    }

    protected void handleWithEndpoint(HttpServletRequest req, HttpServletResponse resp, XmlEndpoint endpoint)
            throws IOException, ServletException {
        ResponseXmlBuilder builder = new ResponseXmlBuilder();
        try {
            endpoint.handle(req, builder);
            builder.applyTo(resp);
        } catch (HttpException e) {
            resp.sendError(e.getStatus());
        }
    }

}
