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
import org.lodgvideon.hpalm.entity.EntityResultSet;

import java.util.Collections;
import java.util.Iterator;

public class PagedEntityCollectionImpl implements EntityCollection {

    private String queryUrl;

    private HpAlmSession session;

    private int totalCount;

    private EntityResultSet firstResultSet;

    public PagedEntityCollectionImpl(HpAlmSession session, String queryUrl, EntityResultSet firstResultSet) {
        this.session = session;
        this.queryUrl = queryUrl;
        this.firstResultSet = firstResultSet;
        this.totalCount = firstResultSet.getTotalResults();
    }

    @Override
    public Iterator<Entity> iterator() {
        if (firstResultSet == null || firstResultSet.getEntities() == null) {
            return Collections.<Entity>emptySet().iterator();
        }
        // optimization: for non-paging results, directly access collection
        // <=: Have seen results from HP ALM containing an empty additional object...
        if (totalCount <= firstResultSet.getEntities().size()) {
            return Collections.unmodifiableList(firstResultSet.getEntities()).iterator();
        }

        return new PagedEntityCollectionIterator();
    }

    @Override
    public int getTotalCount() {
        return totalCount;
    }

    private class PagedEntityCollectionIterator implements Iterator<Entity> {

        private EntityResultSet currentSet;

        // HP ALM "start-index" is 1-based... of course...
        private int offset = 1;

        private int totalCount;

        private int listIndex;

        private PagedEntityCollectionIterator() {
            currentSet = firstResultSet;
            totalCount = currentSet.getTotalResults();
        }

        private void querySet() {
            String url = queryUrl;
            url += (url.contains("?") ? "&" : "?");
            url += "start-index=" + offset;
            try {
                currentSet = session.doGet(url);
            } catch (Exception e) {
                throw new IllegalStateException("Could not retrieve next bunch of entities", e);
            }

            if (currentSet.getEntities() == null || currentSet.getEntities().isEmpty()) {
                currentSet = null;
                totalCount = 0;
            } else {
                totalCount = currentSet.getTotalResults();
            }
        }

        @Override
        public boolean hasNext() {
            return currentSet != null && offset + listIndex <= totalCount;
        }

        @Override
        public Entity next() {
            if (!hasNext()) {
                throw new IllegalStateException("No more elements available");
            }

            if (listIndex == currentSet.getEntities().size()) {
                offset += listIndex;
                querySet();
                listIndex = 0;
                if (currentSet == null) {
                    throw new IllegalStateException("No more elements returned by HP ALM, although expected");
                }
            }

            Entity e = currentSet.getEntities().get(listIndex++);
            return e;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

    }

}
