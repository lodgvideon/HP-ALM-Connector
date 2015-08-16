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
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DefaultAuthenticationEndpoint extends AbstractXmlEndpoint {

    protected static final String LWSSO_COOKIE_KEY = "LWSSO_COOKIE_KEY";

    protected static final String QCSESSION_COOKIE_KEY = "QCSession";

    private static final int MAX_COOKIE_AGE = 60 * 60 * 24;

    /**
     * Map LWSSO_KEY -> User Name
     */
    private Map<String, String> activeUsers = new HashMap<String, String>();

    /**
     * Map LWSSO key -> QCSession key
     */
    private Map<String, String> activeSessions = new HashMap<String, String>();

    @Override
    public void handle(HttpServletRequest request, ResponseXmlBuilder builder) throws IOException, ServletException,
            HttpException {
        String path = request.getPathInfo();
        String method = request.getMethod();

        if ("/rest/is-authenticated".equals(path) && "GET".equals(method)) {
            handleIsAuthenticated(request, builder);
            return;
        }

        if ("/authentication-point/alm-authenticate".equals(path) && "POST".equals(method)) {
            handleAuthentication(request, builder);
            return;
        }

        if ("/authentication-point/logout".equals(path) && "GET".equals(method)) {
            handleLogout(request, builder);
            return;
        }

        if ("/rest/site-session".equals(path)) {
            if ("POST".equals(method)) {
                handleStartSession(request, builder);
            } else if ("GET".equals(method) || "PUT".equals(method)) {
                handleExtendSession(request, builder);
            } else if ("DELETE".equals(method)) {
                handleEndSession(request, builder);
            }
        }

    }

    /**
     * Handles the /rest/is-authenticated endpoint.
     *
     * @param req
     * @param resp
     * @throws IOException
     * @throws ServletException
     */
    protected void handleIsAuthenticated(HttpServletRequest req, ResponseXmlBuilder xml) throws ServletException, IOException,
            HttpException {
        // check for authentication cookie
        String key = getLwssoKey(req);
        if (key != null && activeUsers.containsKey(key)) {
            xml.startElement("AuthenticationInfo").startElement("Username").writeText(activeUsers.get(key)).endElement()
                    .endElement();
            return;
        }

        // no authentication found; send WWW-Authenticate
        String url = req.getRequestURL().toString();
        url = url.substring(0, url.indexOf("/rest/is-authenticated"));
        url += "/authentication-point";

        xml.setHeader("WWW-Authenticate", "LWSSO realm=\"" + url + "\"").setStatusCode(HttpServletResponse.SC_UNAUTHORIZED);
    }

    /**
     * Handles the /authentication-point/alm-authenticate endpoint.
     *
     * @param request
     * @param builder
     * @throws ServletException
     * @throws IOException
     * @throws HttpException
     */
    protected void handleAuthentication(HttpServletRequest request, ResponseXmlBuilder builder) throws ServletException,
            IOException, HttpException {

        // parse XML
        try {
            Document doc = getXml(request);
            if (doc == null) {
                throw new HttpException(HttpServletResponse.SC_BAD_REQUEST);
            }

            Element e = doc.getRootElement();
            if (!"alm-authentication".equals(e.getName())) {
                throw new HttpException(HttpServletResponse.SC_BAD_REQUEST);
            }
            String user = e.getChildText("user");
            String pwd = e.getChildText("password");
            if (!isValidAuthentication(user, pwd)) {
                throw new HttpException(HttpServletResponse.SC_UNAUTHORIZED);
            }

            String lwsso = generateLwssoKey();
            activeUsers.put(lwsso, user);

            Cookie cookie = new Cookie(LWSSO_COOKIE_KEY, lwsso);
            cookie.setMaxAge(MAX_COOKIE_AGE);

            builder.addCookie(cookie);
        } catch (JDOMException e) {
            throw new HttpException(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    protected void handleLogout(HttpServletRequest request, ResponseXmlBuilder builder) throws ServletException, IOException,
            HttpException {
        String key = getLwssoKey(request);
        if (key != null) {
            activeUsers.remove(key);
            activeSessions.remove(key);
            Cookie cookie = new Cookie(LWSSO_COOKIE_KEY, "");
            cookie.setMaxAge(0);
            builder.addCookie(cookie);
            cookie = new Cookie(QCSESSION_COOKIE_KEY, "");
            cookie.setMaxAge(0);
            builder.addCookie(cookie);
        }
    }

    protected void handleStartSession(HttpServletRequest request, ResponseXmlBuilder builder) throws HttpException {
        String lwssoKey = getLwssoKey(request);
        if (lwssoKey == null) {
            throw new HttpException(HttpServletResponse.SC_UNAUTHORIZED);
        }
        String sessionKey = generateLwssoKey();
        Cookie cookie = new Cookie(QCSESSION_COOKIE_KEY, sessionKey);
        cookie.setMaxAge(MAX_COOKIE_AGE);
        builder.addCookie(cookie);
        activeSessions.put(lwssoKey, sessionKey);
    }

    protected void handleExtendSession(HttpServletRequest request, ResponseXmlBuilder builder) throws HttpException {
        // can safely be ignored
    }

    protected void handleEndSession(HttpServletRequest request, ResponseXmlBuilder builder) throws HttpException {
        String sessionKey = getQCSessionKey(request);
        if (sessionKey == null) {
            throw new HttpException(HttpServletResponse.SC_BAD_REQUEST);
        }
        activeSessions.remove(sessionKey);
    }

    protected final String getQCSessionKey(HttpServletRequest request) {
        for (Cookie cookie : request.getCookies()) {
            if (QCSESSION_COOKIE_KEY.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    protected final String getLwssoKey(HttpServletRequest request) {
        for (Cookie cookie : request.getCookies()) {
            if (LWSSO_COOKIE_KEY.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    protected String generateLwssoKey() {
        return Integer.toHexString((int) (Math.random() * Integer.MAX_VALUE));
    }

    protected boolean isValidAuthentication(String user, String password) {
        return user != null && password != null && user.length() > 0 && password.length() > 0;
    }

    public boolean checkLwssoHeader(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String key = getLwssoKey(request);
        if (key != null && activeUsers.containsKey(key)) {
            return true;
        }

        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        return false;
    }

}
