/*
 * Copyright 2020 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package entity_fetcher

import io.jmix.core.DataManager
import io.jmix.core.EntityStates
import io.jmix.core.FetchPlanBuilder
import io.jmix.core.SaveContext
import io.jmix.data.impl.JmixEntityFetchGroup
import org.eclipse.persistence.queries.FetchGroupTracker
import org.springframework.beans.factory.annotation.Autowired
import test_support.DataSpec
import test_support.entity.TestEntityWithNonPersistentRef
import test_support.entity.sales.Customer
import test_support.entity.sales.Status

class EntityFetcherTest extends DataSpec {

    @Autowired
    EntityStates entityStates
    @Autowired
    DataManager dataManager

    def "fetching entity with non-persistent reference"() {
        // setup the entity like it is stored in a custom datastore and linked as transient property
        def npCustomer = new Customer(status: Status.OK)
        entityStates.makeDetached(npCustomer)
        ((FetchGroupTracker) npCustomer)._persistence_setFetchGroup(new JmixEntityFetchGroup(['status']))

        def entity = new TestEntityWithNonPersistentRef(
                name: 'c',
                customer: npCustomer
        )
        def view = FetchPlanBuilder.of(TestEntityWithNonPersistentRef).addAll('name', 'customer.name').build()

        when:
        def committed = dataManager.save(new SaveContext().saving(entity, view)).get(entity)

        then:
        noExceptionThrown()
        committed == entity
    }
}
