/*
 * Copyright 2019 Haulmont.
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

package data_components

import io.jmix.ui.model.DataComponents
import test_support.DataContextSpec
import test_support.entity.TestNullableIdEntity
import test_support.entity.TestNullableIdItemEntity

import org.springframework.beans.factory.annotation.Autowired

class CollectionContainerNullIdTest extends DataContextSpec {

    @Autowired DataComponents factory

    def "items with null id"() {
        def container = factory.createCollectionContainer(TestNullableIdEntity)

        def entity1 = new TestNullableIdEntity(name: '111')
        def entity2 = new TestNullableIdEntity(name: '222')

        when:
        container.getMutableItems().add(entity1)
        container.getMutableItems().add(entity2)

        then:
        container.getItems().contains(entity1)
        container.getItems().contains(entity2)
        container.containsItem(entity1)
        container.containsItem(entity2)
        container.getItemIndex(entity1) == 0
        container.getItemIndex(entity2) == 1
    }

    def "item changes id"() {
        def container = factory.createCollectionContainer(TestNullableIdEntity)
        def context = factory.createDataContext()

        when:
        def entity1 = context.merge(new TestNullableIdEntity(name: '111'))
        container.getMutableItems().add(entity1)

        then:
        entity1.id == null
        container.containsItem(entity1)
        context.contains(entity1)
        context.hasChanges()

        when:
        context.commit()

        then:
        entity1.id == 1
        container.containsItem(entity1)
        container.containsItem(1L)
    }

    def "property collection"() {
        def masterContainer = factory.createInstanceContainer(TestNullableIdEntity)
        def propertyContainer = factory.createCollectionContainer(TestNullableIdItemEntity, masterContainer, 'items')
        def context = factory.createDataContext()

        when:
        def master = context.merge(new TestNullableIdEntity(name: '111'))
        def item1 = context.merge(new TestNullableIdItemEntity(name: 'item1', master: master))
        masterContainer.setItem(master)
        propertyContainer.getMutableItems().add(item1)

        then:
        propertyContainer.containsItem(item1)
        master.items[0].is(item1)

        when:
        context.commit()

        then:
        master.id == 1
        item1.id == 1
        master.items[0].is(item1)
        propertyContainer.getItem(1L).is(item1)
    }

    def "replace item"() {
        def container = factory.createCollectionContainer(TestNullableIdEntity)
        def context = factory.createDataContext()

        def entity = context.merge(new TestNullableIdEntity(name: '111'))
        container.getMutableItems().add(entity)

        def entity2 = context.merge(new TestNullableIdEntity(name: '222'))

        when:
        container.replaceItem(entity2)

        then:
        thrown(IllegalArgumentException)

        when:
        def committedEntity = context.commit().get(entity)

        then:
        committedEntity.is(entity)

        when:
        container.replaceItem(committedEntity)

        then:
        container.getItem(entity.id).is(entity)
    }
}
