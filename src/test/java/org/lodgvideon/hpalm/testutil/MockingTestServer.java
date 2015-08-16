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

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import javax.servlet.Servlet;

public class MockingTestServer {

    public static final int HTTP_PORT = 11889;

    private Server server;

    private Servlet hpAlmServlet = new DefaultHpAlmServlet();

    public MockingTestServer() {
    }

    public String getBaseUrl() {
        return "http://localhost:" + HTTP_PORT + "/qcbin";
    }

    public Servlet getHpAlmServlet() {
        return hpAlmServlet;
    }

    public void setHpAlmServlet(Servlet hpAlmServlet) {
        this.hpAlmServlet = hpAlmServlet;
    }

    public void startServer() throws Exception {
        server = new Server(HTTP_PORT);

        ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        handler.setContextPath("/qcbin");
        ServletHolder holder = new ServletHolder("hpalm-mock", hpAlmServlet);
        handler.addServlet(holder, "/*");

        server.setHandler(handler);
        server.start();
        while (!server.isStarted()) {
            Thread.sleep(100);
        }
    }

    public void stopServer() throws Exception {
        server.stop();
        server.join();
        server = null;
    }

}
