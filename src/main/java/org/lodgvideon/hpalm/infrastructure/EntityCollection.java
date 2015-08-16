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

/**
 * An interface to query a collection of entities returned by HP ALM. This interface hides the internal data paging of the HP ALM
 * REST API, so clients can treat the collection as one huge set. All navigational issues on the set are performed automatically
 * by the implementation. <br>
 * Clients must <b>not</b> implement this interface! It is returned from operations on {@link HpAlmSession}.
 *
 * @author falbrech
 */
public interface EntityCollection extends Iterable<Entity> {

    /**
     * Returns the total count of elements in this collection, as returned from HP ALM.
     *
     * @return The total count of elements in this collection. 0 for an empty collection.
     */
    public int getTotalCount();

}
