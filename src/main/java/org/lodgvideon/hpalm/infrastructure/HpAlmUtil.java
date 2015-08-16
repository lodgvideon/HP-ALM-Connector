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

import org.lodgvideon.hpalm.entity.Entity;
import org.lodgvideon.hpalm.entity.TestInstanceBuilder;
import org.lodgvideon.hpalm.entity.TestSetBuilder;
import org.lodgvideon.hpalm.entity.TestSetFolderBuilder;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public final class HpAlmUtil {

    public static final DecimalFormat DF_ID = (DecimalFormat) NumberFormat.getInstance(Locale.US);

    static {
        DF_ID.setGroupingUsed(false);
        DF_ID.setMaximumFractionDigits(0);
        DF_ID.setDecimalSeparatorAlwaysShown(false);
    }

    private HpAlmUtil() {
    }

    /**
     * Ensures that there exists a given path in the HP ALM test set folder structure. If there are folders missing for this path,
     * these folders are created.
     *
     * @param session HP ALM session to use for object queries and creation.
     * @param path    Path to create or to check for existence, e.g. <code>Root/lodgvideon/UITest</code>.
     * @return The created or already existing folder at the path. This is an HP ALM "test-set-folder" org.lodggvideon.hpalm.entity.
     * @throws IOException              If any I/O exception occurs during REST communication.
     * @throws HpAlmException           If HP ALM reports any error.
     * @throws IllegalArgumentException If path is empty or invalid.
     */
    public static Entity createTestSetFolderPath(HpAlmSession session, String path) throws IOException, HpAlmException {
        if (path == null) {
            throw new IllegalArgumentException("Path is null");
        }
        String[] pathSegments = path.split("/");

        if (pathSegments.length == 0) {
            throw new IllegalArgumentException("Path is empty.");
        }

        // quick check on path validity
        for (String f : pathSegments) {
            if (f.length() == 0) {
                throw new IllegalArgumentException("Path is invalid. Path must not begin or end with a slash, and not contain double slashes.");
            }
        }

        String query = "name['" + pathSegments[0] + "']";

        EntityCollection rootFolders = session.queryEntities("test-set-folder", query);
        if (rootFolders.getTotalCount() != 1) {
            throw new HpAlmException("Found no or more than one root folder matching first segment " + pathSegments[0]);
        }
        Entity result = rootFolders.iterator().next();

        for (int i = 1; i < pathSegments.length; i++) {
            query = "name['" + pathSegments[i] + "']; parent-id[" + result.getId() + "]";
            EntityCollection folders = session.queryEntities("test-set-folder", query);
            if (folders.getTotalCount() > 1) {
                // should not be (not allowed by HP ALM)
                throw new HpAlmException("Found more than one folder matching segment " + pathSegments[i]);
            }
            if (folders.getTotalCount() == 0) {
                // create non-existing sub folder
                TestSetFolderBuilder builder = new TestSetFolderBuilder();
                Entity e = builder.setParentId(result.getId()).setName(pathSegments[i]).create();
                result = session.createEntity(e);
            } else {
                result = folders.iterator().next();
            }
        }

        return result;
    }

    /**
     * Checks if there already is a test set with the given name in the given test set folder, and returns it if such a test set
     * is found. Otherwise, a new test set with the given name is created and returned.
     *
     * @param session         HP ALM session to use for object queries and creation.
     * @param testSetFolderId ID of the test set folder containing the test set.
     * @param testSetName     Name of the test set to retrieve or create.
     * @return The already existing or newly created test set.
     * @throws IOException    If any I/O error occurs.
     * @throws HpAlmException If HP ALM reports any error.
     */
    public static Entity createOrGetTestSet(HpAlmSession session, long testSetFolderId, String testSetName) throws IOException,
            HpAlmException {
        // get all test sets in that folder with the given name
        EntityCollection testSets = session.queryEntities("test-set", "parent-id[" + DF_ID.format(testSetFolderId) + "]; name['"
                + testSetName + "']");

        if (testSets.getTotalCount() == 0) {
            // create test set
            Entity testSet = new TestSetBuilder().setParentId(testSetFolderId).setName(testSetName).create();
            return session.createEntity(testSet);
        }

        return testSets.iterator().next();
    }

    /**
     * Checks if there already is a test instance referencing the given test in the given test set, and returns it if such a test
     * instance is found. Otherwise, a new test instance for the given test is created and returned. The newly created test
     * instance is sorted last in the given test set.
     *
     * @param session      HP ALM session to use for object queries and creation.
     * @param testSetId    ID of the test set containing the test instance.
     * @param testId       ID of the referenced test.
     * @param testConfigId ID of the references test configuration. If <code>null</code>, config attribute will not be used.
     * @return The already existing or newly created test instance.
     * @throws IOException    If any I/O error occurs.
     * @throws HpAlmException If HP ALM reports any error.
     */
    public static Entity createOrGetTestInstance(HpAlmSession session, long testSetId, long testId, Long testConfigId)
            throws IOException,
            HpAlmException {
        return createOrGetTestInstance(session, testSetId, testId, testConfigId, true);
    }

    private static Entity createOrGetTestInstance(HpAlmSession session, long testSetId, long testId, Long testConfigId, boolean createIfNotFound)
            throws IOException,
            HpAlmException {
        // get all test instances in that test set with the given test ID
        EntityCollection testInstances = session.queryEntities("test-instance",
                "cycle-id[" + DF_ID.format(testSetId) + "]; test-id[" + DF_ID.format(testId) + "]"
                        + (testConfigId != null ? "; test-config-id[" + DF_ID.format(testConfigId) + "]" : ""));

        if (testInstances.getTotalCount() == 0 && createIfNotFound) {
            // determine next free order number
            testInstances = session.queryEntities("test-instance", "cycle-id[" + DF_ID.format(testSetId) + "]");
            long maxOrderNo = 0;
            for (Entity e : testInstances) {
                maxOrderNo = Math.max(e.getLongFieldValue("test-order"), maxOrderNo);
            }

            TestInstanceBuilder builder = new TestInstanceBuilder().setTestSetId(testSetId).setTestId(testId)
                    .setOrderNumber(maxOrderNo + 1);
            if (testConfigId != null) {
                builder.setTestConfigId(testConfigId.longValue());
            }

            // create test instance
            Entity testInstance = builder.create();
            testInstance = session.createEntity(testInstance);

            // do NOT return this test instance, but query again, as HP ALM could have auto-generated more than one
            // instance, and returns the last one (sometimes)
            return createOrGetTestInstance(session, testSetId, testId, testConfigId, false);
        }

        return testInstances.getTotalCount() == 0 ? null : testInstances.iterator().next();
    }

}
