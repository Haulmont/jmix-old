/*
 * Copyright (c) 2008-2018 Haulmont.
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

package spec.haulmont.cuba.web.datacontext

import com.haulmont.cuba.core.model.sales.Customer
import com.haulmont.cuba.core.model.sales.Order
import io.jmix.core.entity.KeyValueEntity
import io.jmix.ui.component.TextField
import io.jmix.ui.component.data.value.ContainerValueSource
import io.jmix.ui.model.KeyValueCollectionContainer
import io.jmix.ui.model.KeyValueCollectionLoader
import io.jmix.ui.model.KeyValueContainer
import io.jmix.ui.model.KeyValueInstanceLoader
import spec.haulmont.cuba.web.UiScreenSpec
import spock.lang.Ignore

class KeyValueContainersTest extends UiScreenSpec {

    private Customer customer1
    private Order order1

    @Override
    void setup() {
        customer1 = new Customer(name: 'customer1')
        order1 = new Order(number: '111', customer: customer1, amount: 100)
    }

    @Ignore
    def "load collection"() {
        KeyValueCollectionContainer container = dataComponents.createKeyValueCollectionContainer()
        container.addProperty('custName').addProperty('amount')

        KeyValueCollectionLoader loader = dataComponents.createKeyValueCollectionLoader()
        loader.setContainer(container)
        loader.setQuery('select o.customer.name, sum(o.amount) from test$Order o group by o.customer.name')

        /*TestServiceProxy.mock(DataService, Mock(DataService) {
            loadValues(_) >> {
                KeyValueEntity entity = new KeyValueEntity()
                entity.setValue('custName', 'customer1')
                entity.setValue('amount', 100)
                [entity]
            }
        })*/

        when:

        loader.load()

        then:

        container.items[0].getValue('custName') == 'customer1'
        container.items[0].getValue('amount') == 100

        container.items[0].getMetaClass().getProperty('custName') != null
        container.items[0].getMetaClass().getProperty('amount') != null
    }

    @Ignore
    def "load instance"() {
        KeyValueContainer container = dataComponents.createKeyValueContainer()
        container.addProperty('custName').addProperty('amount')

        KeyValueInstanceLoader loader = dataComponents.createKeyValueInstanceLoader()
        loader.setContainer(container)
        loader.setQuery('select o.customer.name, sum(o.amount) from test$Order o group by o.customer.name')

        /*TestServiceProxy.mock(DataService, Mock(DataService) {
            loadValues(_) >> {
                KeyValueEntity entity1 = new KeyValueEntity()
                entity1.setValue('custName', 'customer1')
                entity1.setValue('amount', 100)
                KeyValueEntity entity2 = new KeyValueEntity()
                entity2.setValue('custName', 'customer2')
                entity2.setValue('amount', 200)
                [entity1, entity2]
            }
        })*/

        when:

        loader.load()

        then:

        container.item.getValue('custName') == 'customer1'
        container.item.getValue('amount') == 100

        container.item.getMetaClass().getProperty('custName') != null
        container.item.getMetaClass().getProperty('amount') != null
    }

    def "binding"() {
        KeyValueContainer container = dataComponents.createKeyValueContainer()
        container.addProperty('custName').addProperty('amount')

        TextField field1 = uiComponents.create(TextField)
        field1.setValueSource(new ContainerValueSource(container, 'custName'))

        TextField field2 = uiComponents.create(TextField)
        field2.setValueSource(new ContainerValueSource(container, 'custName'))

        KeyValueEntity entity = new KeyValueEntity()
        entity.setValue('custName', 'customer1')
        entity.setValue('amount', 100)

        when:

        container.setItem(entity)

        then:

        field1.getValue() == 'customer1'
        field2.getValue() == 'customer1'

        when:

        field1.setValue('changed')

        then:

        field2.getValue() == 'changed'
        entity.getValue('custName') == 'changed'
    }

    def "entity has correct MetaClass when set to KeyValueContainer"() {
        KeyValueContainer container = dataComponents.createKeyValueContainer()
        container.addProperty('custName').addProperty('amount').setIdName('custName')

        when:

        KeyValueEntity entity = new KeyValueEntity()
        entity.setValue('custName', 'customer1')
        entity.setValue('amount', 100)

        container.setItem(entity)

        then:

        entity.getMetaClass() != null
        entity.getMetaClass().getProperty('custName') != null
        entity.getMetaClass().getProperty('amount') != null
        entity.getIdName() == 'custName'
    }

    def "entity has correct MetaClass when added to KeyValueCollectionContainer"() {
        KeyValueCollectionContainer container = dataComponents.createKeyValueCollectionContainer()
        container.addProperty('custName').addProperty('amount').setIdName('custName')

        when:

        KeyValueEntity entity = new KeyValueEntity()
        entity.setValue('custName', 'customer1')
        entity.setValue('amount', 100)

        container.setItems([entity])

        then:

        entity.getMetaClass() != null
        entity.getMetaClass().getProperty('custName') != null
        entity.getMetaClass().getProperty('amount') != null
        entity.getIdName() == 'custName'

        when:

        KeyValueEntity entity2 = new KeyValueEntity()
        entity2.setValue('custName', 'customer2')
        entity2.setValue('amount', 200)

        container.getMutableItems().add(entity2)

        then:

        entity2.getMetaClass() != null
        entity2.getMetaClass().getProperty('custName') != null
        entity2.getMetaClass().getProperty('amount') != null
        entity2.getIdName() == 'custName'
    }
}
